/*	NetLayer2.h

	Created 10-Feb-2005: Thomas Mulgrew (tmulgrew@slingshot.co.nz)

	NetLib4Games layer 2.

*/

#include "NetLayer2.h"
#include "NetLog.h"

namespace NetLib4Games {

#ifdef NETLOG
inline static std::string Desc (NetPacketHeaderL2 *header) {
	int		channel		= NETL2_GETCHANNEL (header->channelFlags);
	bool	reliable	= (header->channelFlags & NETL2_RELIABLE) != 0,
			smoothed	= (header->channelFlags & NETL2_SMOOTHED) != 0,
			ordered		= (header->channelFlags & NETL2_ORDERED) != 0;
	return "Channel " + IntToString (channel)
		+ ", Packet " + IntToString (header->packetIndex) + " of " + IntToString (header->packetCount)
		+ (ordered  ? ", Ordered "	: ", Unordered")
		+ (reliable ? ", Reliable " : ", Unreliable")
		+ (smoothed ? ", Smoothed " : ", Unsmoothed")
		+ ", # " + IntToString (header->messageIndex)
		+ ", Reliable # " + IntToString (header->reliableIndex);
}
#endif

///////////////////////////////////////////////////////////////////////////////
//	NetMessageL2

NetMessageL2::NetMessageL2 (int _channel,
							bool _reliable,
							bool _smoothed,
							bool _ordered,
							unsigned long _messageIndex,
							unsigned long _reliableIndex,
							unsigned short _packetCount,
							unsigned int _tickCount) :
	channel			(_channel),
	reliable		(_reliable),
	smoothed		(_smoothed),
	ordered			(_ordered),
	messageIndex	(_messageIndex),
	reliableIndex	(_reliableIndex),
	packetCount		(_packetCount),
	tickCount		(_tickCount),
	tickCountRegistered (false),
	receivedCount	(0),
	dataSize		(0)	{

	// Allocate packet space
	if (packetCount > 0) {
		packets = new NetSimplePacket *[packetCount];
		for (int i = 0; i < packetCount; i++)
			packets [i] = NULL;
	}
	else
		packets = NULL;
}

bool NetMessageL2::Buffer (NetSimplePacket *packet, int packetIndex) {
	assert (packet != NULL);

	if (packetIndex >= 0 && packetIndex < packetCount && packets [packetIndex] == NULL) {

		// Add packet to message
		packets [packetIndex] = packet;
		receivedCount++;
		dataSize += packet->size;
		return true;
	}
	else {

		// Packet already received... or index is bad
		delete packet;
		return false;
	}
}

void NetMessageL2::CopyData (char *data, unsigned int offset, unsigned int& size) {
	assert (data != NULL);
	assert (offset <= dataSize);

	// Adjust size
	if (offset + size > dataSize)
		size = dataSize - offset;

	// Find start packet
	int packet = 0;
	while (offset > packets [packet]->size) {
		offset -= packets [packet]->size;
		packet++;
	}

	// Copy data from packets
	unsigned int destOffset = 0, left = size;
	while (left > 0) {
		int copySize = Min (left, packets [packet]->size - offset);

		// Copy data
		memcpy (data + destOffset, packets [packet]->data + offset, copySize);

		left		-= copySize;
		destOffset	+= copySize;
		packet++;
		offset		= 0;
	}
}

///////////////////////////////////////////////////////////////////////////////
//	NetOutChannelL2

void NetOutChannelL2::Send (
	NetConL1&		connection,
	const char		*data,
	int				size,
	bool			reliable,
	bool			smoothed,
	unsigned int	tickCount) {
	assert (data != NULL);
	assert (size >= 0);

	// Calculate maximum packet data size, allowing for packet header data.
	int maxPacketSize	= connection.MaxPacketSize ();
	int maxDataSize		= maxPacketSize - sizeof (NetPacketHeaderL2);
	if (maxDataSize <= 0) {
		SetError ("Bad max packet size. No room for header and data!");
		return;
	}

	// Allocate room to build each packet
	char *buffer = new char [maxPacketSize];

	// Divide message into packets
    int packets = (size == 0) ? 1 : (size - 1) / maxDataSize + 1;
	int packet = 0, offset = 0;
	while (packet < packets) {
		int packetSize = Min (size, maxDataSize);

		// Encode L2 header
		NetPacketHeaderL2 *header = (NetPacketHeaderL2 *) buffer;
		header->channelFlags =
				(reliable ? NETL2_RELIABLE : 0)
			|	(smoothed ? NETL2_SMOOTHED : 0)
			|	(m_ordered ? NETL2_ORDERED : 0)
			|	(m_channel & NETL2_CHANNELMASK);
		header->messageIndex	= m_messageIndex;
		header->reliableIndex	= m_reliableIndex;
		header->packetCount		= packets;
		header->packetIndex		= packet;
		header->tickCount		= tickCount;

		// Append data
        if (packetSize > 0)
    		memcpy (buffer + sizeof (NetPacketHeaderL2), data + offset, packetSize);
		offset	+= packetSize;
		size	-= packetSize;

		// Send L2 packet
		connection.Send (buffer, sizeof (NetPacketHeaderL2) + packetSize, reliable);
		packet++;
	}

	// Done with buffer
	delete[] buffer;

	// Update packet indices
	m_messageIndex++;
	if (reliable)
		m_reliableIndex++;
}

///////////////////////////////////////////////////////////////////////////////
//	NetInChannelL2

void NetInChannelL2::Clear () {

	// Delete pending messages
	for (	std::list<NetMessageL2 *>::iterator i = m_messages.begin ();
			i != m_messages.end ();
			i++)
		delete *i;
}

void NetInChannelL2::Buffer (
	NetSimplePacket *packet,
	bool reliable,
	bool smoothed,
    bool resent,
	int messageIndex,
	int reliableIndex,
	int packetIndex,
	int packetCount,
	unsigned int tickCount) {

	assert (packet != NULL);

	// Ordered channels can reject messages older than the last one promoted.
	if (m_ordered && messageIndex < m_messageIndex)
		return;

	// Find corresponding message
	std::list<NetMessageL2 *>::iterator i;
	for	(	i =  m_messages.begin ();
			i != m_messages.end () && (*i)->messageIndex < messageIndex;
			i++)
		;

	// If none exists, create a new one
	NetMessageL2 *message;
	if (i == m_messages.end () || (*i)->messageIndex > messageIndex) {
		message = new NetMessageL2 (	m_channel,
										reliable,
										smoothed,
										m_ordered,
										messageIndex,
										reliableIndex,
										packetCount,
										tickCount);

		// Insert at correct position
		m_messages.insert (i, message);
	}
	else
		message = *i;

	// Add packet to message
	if (message->Buffer (packet, packetIndex)) {
		m_packetCount++;

        // Disable smoothing if any part of the message was resent.
        // We don't want to include spikes from dropped packets in our timing
        // calculations.
        if (resent)
            message->smoothed = false;
    }
}

void NetInChannelL2::PromoteMessages (
	NetInChannelL2Callback *callback,
	unsigned int tickCount,
	int adjustment,
	bool doSmoothing) {

	// Register tickCount differences
	int wakeup = INFINITE;
	std::list<NetMessageL2 *>::iterator i;
	for (	i =	m_messages.begin ();
			i != m_messages.end ();
			i++) {

		if ((*i)->smoothed && (*i)->Complete ()) {

			if (!(*i)->tickCountRegistered) {

				// Register tick count difference
				callback->RegisterTickCountDifference (tickCount - (*i)->tickCount);
				(*i)->tickCountRegistered = true;

				// Adjust tick count on message
				if (doSmoothing)
					(*i)->tickCount += adjustment;
				else
					(*i)->smoothed = false;
			}

			// Check if wakeup is required
			if (doSmoothing && (*i)->tickCount >= tickCount) {
				int newWakeup = (*i)->tickCount - tickCount;
				if (wakeup == INFINITE || newWakeup < wakeup)
					wakeup = newWakeup;
			}
		}
	}

	// Request wakeup
	if (wakeup != INFINITE)
		callback->RequestWakeup (wakeup);

	bool found;
	do {
		found = false;

		// Search for complete message. Oldest first
		for (	i =  m_messages.begin ();
				i != m_messages.end ()
					&& !(       (*i)->Complete ()												    // Message is complete
					        &&  (!doSmoothing || !(*i)->smoothed || tickCount >= (*i)->tickCount));	// Either not smoothed, or packet is due
				i++)
			;

		if (i != m_messages.end ()) {

			// If the channels is ordered, then make sure there aren't any older reliable messages
			// waiting to be promoted. If there are, then we can't promote this message yet.
			if (	!m_ordered
				||	(*i)->reliableIndex == m_reliableIndex ) {

				// Promote message
				NetMessageL2 *msg = *i;
				callback->QueueMessage (msg);
				m_messages.erase (i);
				m_packetCount -= msg->receivedCount;

				NetLog ("Promote completed L2 message. Channel " + IntToString (m_channel) + ", Msg " + IntToString (msg->messageIndex) + ", " + IntToString (msg->packetCount) + " packets, " + IntToString (msg->dataSize) + " bytes");

				// Delete older packets (from ordered channels)
				if (m_ordered) {

					// Update message index and reliable index
					m_messageIndex = msg->messageIndex;
					if (msg->reliable)
						m_reliableIndex++;

					// Remove any older messages from queue
					while (m_messages.size () > 0 && m_messages.front ()->messageIndex < m_messageIndex) {
						NetMessageL2 *otherMsg = m_messages.front ();
						m_messages.pop_front ();
						m_packetCount -= otherMsg->receivedCount;

						NetLog ("Discard incomplete ordered unreliable L2 message. Channel " + IntToString (m_channel) + ", Msg " + IntToString (msg->messageIndex));

						delete otherMsg;
					}
				}
				found = true;
			}
		}
	} while (found);
}

void NetInChannelL2::CullMessages () {

	// Remove network messages to keep the buffer to a maximum size.
	// (Note: This is not a strictly enforced maximum in that the buffer will periodically
	//	grow larger than the maximum. But we enforce that it must be cullable to the
	//	maximum size.)
	while (m_packetCount > m_maxBufferPackets) {

		// Search for oldest unreliable message
		std::list<NetMessageL2 *>::iterator i;
		for (	i = m_messages.begin ();
				i != m_messages.end () && (*i)->reliable;
				i++)
			;

		// None found
		if (i == m_messages.end ()) {
			SetError ("Layer 2 network buffer overflow. No unreliable messages could be dropped");
			return;
		}

		// Drop the message
		NetMessageL2 *msg = *i;
		m_messages.erase (i);
		m_packetCount -= msg->receivedCount;
		delete msg;
	}
}

///////////////////////////////////////////////////////////////////////////////
// NetConL2

void NetConL2::Init () {

	NetLog ("Create L2 connection");

	// Clear channel pointers
	for (int i = 0; i < NETL2_MAXCHANNELS; i++) {
		m_outChannels [i] = NULL;
		m_inChannels  [i] = NULL;
	}

	// Start the processing thread
	m_connection.HookCallback (this);
	m_connection.StartThread ();
}

NetConL2::NetConL2 (NetConLow *connection, NetSettingsStaticL2& settings) : m_settingsStatic (settings), m_connection (connection, settings.l1Settings) {
	Init ();
}

NetConL2::NetConL2 (NetConLow *connection) : m_connection (connection) {
	Init ();
}

NetConL2::~NetConL2 () {

	// Unhook from service thread callback
	m_connection.HookCallback (NULL);

	// Free channels
	for (int i = 0; i < NETL2_MAXCHANNELS; i++) {
		if (m_outChannels [i] != NULL)
			delete m_outChannels [i];
		if (m_inChannels [i] != NULL)
			delete m_inChannels [i];
	}

	// Clear pending queue
	for (	std::list<NetMessageL2 *>::iterator itor = m_messageQueue.begin ();
			itor != m_messageQueue.end ();
			itor++)
		delete *itor;

	NetLog ("Delete L2 connection");
}

void NetConL2::ProcessThreadCallback () {

	// Handle incoming packets
	while (m_connection.Connected () && m_connection.DataPending ()) {

		int size = m_connection.PendingDataSize ();
		NetLog ("Receive L2 packet, " + IntToString (size) + " bytes");
		if (size >= sizeof (NetPacketHeaderL2)) {

			// Read header
			NetPacketHeaderL2 header;
			unsigned int partSize = sizeof (header);
			m_connection.ReceivePart ((char *) &header, 0, partSize);

			NetLog ("Incoming L2 packet. " + Desc (&header));

			// Decode header
			int		channel		= NETL2_GETCHANNEL (header.channelFlags);
			bool	reliable	= (header.channelFlags & NETL2_RELIABLE) != 0,
					smoothed	= (header.channelFlags & NETL2_SMOOTHED) != 0,
					ordered		= (header.channelFlags & NETL2_ORDERED)	 != 0;

            // Get resent flag (from layer 1 header)
            bool    resent      = m_connection.PendingIsResent();

			if (channel >= 0 && channel < NETL2_MAXCHANNELS) {

				// If channel does not exist, create it
				if (m_inChannels [channel] == NULL) {
					NetLog ("Create incoming channel #" + IntToString (channel) + (ordered ? ", ordered" : ", unordered"));
					m_inChannels [channel] = new NetInChannelL2 (channel, ordered, m_settingsStatic.maxBufferPackets);
				}

				// Read data
				NetSimplePacket *packet = new NetSimplePacket (size - sizeof (header));
                if (packet->size > 0)
    				m_connection.ReceivePart (packet->data, sizeof (header), packet->size);

				// Buffer packet
				m_inChannels [channel]->Buffer (packet, reliable, smoothed, resent, header.messageIndex, header.reliableIndex, header.packetIndex, header.packetCount, header.tickCount);
				CheckObject (m_inChannels [channel]);
			}
		}

		m_connection.DonePendingData ();
	}

	// Promote completed messages
	int i;
	m_inQueueLock.Lock ();

		// Calculate adjusted tick count, based on smoothing data.
		int tickCount = GetTickCount (),
			adjustment = 0;
		bool doSmoothing = false;

		// Timing buffer must be full
		if (m_timingBuffer.BufferFull ()) {

			// Can apply smoothing
			doSmoothing = true;

			// Find sorted position
			assert (m_settings.smoothingPercentage >= 0);
			unsigned int index = (NET_L2TIMINGBUFFERSIZE * m_settings.smoothingPercentage) / 100;
			if (index >= NET_L2TIMINGBUFFERSIZE)
				index = NET_L2TIMINGBUFFERSIZE - 1;

			// Adjust tick count by sorted difference
			adjustment = m_timingBuffer.Difference (index);
		}

		for (i = 0; i < NETL2_MAXCHANNELS; i++)
			if (m_inChannels [i] != NULL)
				m_inChannels [i]->PromoteMessages (this, tickCount, adjustment, doSmoothing);
	m_inQueueLock.Unlock ();

	// Cull old messages to prevent buffer overflowing
	for (i = 0; i < NETL2_MAXCHANNELS; i++)
		if (m_inChannels [i] != NULL)
			m_inChannels [i]->CullMessages ();
}

void NetConL2::Send (const char *data, unsigned int size, int channel, bool reliable, bool smoothed) {
	assert (data != NULL);
	assert (channel >= 0);
	assert (channel < NETL2_MAXCHANNELS);

	// Create channel if necessary
	// By convention: Channel 0 is unordered, all other channels are ordered.
	if (m_outChannels [channel] == NULL) {
		NetLog ("Create outgoing channel #" + IntToString (channel) + (channel == 0 ? ", unordered" : ", ordered"));
		m_outChannels [channel] = new NetOutChannelL2 (channel, channel != 0);
	}

	// Send data through channel
	NetLog (	"Send L2 message, "
			+ IntToString (size) + " bytes, channel # " + IntToString (channel)
			+ (reliable ? ", reliable" : ", unreliable")
			+ (smoothed ? ", smoothed" : ", unsmoothed"));
	m_outChannels [channel]->Send (m_connection, data, size, reliable, smoothed, GetTickCount ());
	CheckObject (m_outChannels [channel]);
}

bool NetConL2::DataPending () {

    // Note:
    // _Must_ return true if data in the buffer, even if not connected.
    // Otherwise the following code would not work:
    //
    //  if (connection.DataPending ()) {
    //      connection.Receive(...)
    //      ...
    //
    // Because connection status is volatile, and hence DataPending() would also
    // be.
	m_inQueueLock.Lock ();
		bool result = m_messageQueue.size () > 0;
	m_inQueueLock.Unlock ();
	return result;
}

unsigned int NetConL2::PendingDataSize () {
	assert (DataPending ());
	m_inQueueLock.Lock ();
		unsigned int result = m_messageQueue.front ()->dataSize;
	m_inQueueLock.Unlock ();
	return result;
}

void NetConL2::ReceivePart (char *data, unsigned int offset, unsigned int& size) {
	assert (DataPending ());
	assert (data != NULL);
	assert (offset <= PendingDataSize ());
	m_inQueueLock.Lock ();
		m_messageQueue.front ()->CopyData (data, offset, size);
	m_inQueueLock.Unlock ();
}

void NetConL2::DonePendingData () {
    assert (DataPending ());
	m_inQueueLock.Lock ();
		NetMessageL2 *message = m_messageQueue.front ();
		m_messageQueue.pop_front ();
	m_inQueueLock.Unlock ();
	delete message;
}

int NetConL2::PendingChannel () {
	assert (DataPending ());
	m_inQueueLock.Lock ();
		int result = m_messageQueue.front ()->channel;
	m_inQueueLock.Unlock ();
	return result;
}

bool NetConL2::PendingReliable () {
	assert (DataPending ());
	m_inQueueLock.Lock ();
		bool result = m_messageQueue.front ()->reliable;
	m_inQueueLock.Unlock ();
	return result;
}

bool NetConL2::PendingSmoothed () {
	assert (DataPending ());
	m_inQueueLock.Lock ();
		bool result = m_messageQueue.front ()->smoothed;
	m_inQueueLock.Unlock ();
	return result;
}

/*bool NetConL2::PendingOrdered () {
	assert (DataPending ());
	m_inQueueLock.Lock ();
		bool result = m_messageQueue.front ()->ordered;
	m_inQueueLock.Unlock ();
	return result;
}*/

void NetConL2::QueueMessage (NetMessageL2 *msg) {
	m_messageQueue.push_back (msg);
}

void NetConL2::RegisterTickCountDifference (int difference) {
	m_timingBuffer.LogDifference (difference);
}

void NetConL2::RequestWakeup (unsigned int msec) {
	m_connection.RequestWakeup (msec);
}

///////////////////////////////////////////////////////////////////////////////
//	NetTimingBufferL2
//
unsigned int NetTimingBufferL2::SortedPos (int difference) {

}

void NetTimingBufferL2::LogDifference (int difference) {

}

}