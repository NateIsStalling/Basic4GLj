/*	NetLayer1.cpp

	Created 7-Jan-2004: Thomas Mulgrew (tmulgrew@slingshot.co.nz)

	NetLib4Games layer 1.
*/

#include "NetLayer1.h"
#include "NetLog.h"

namespace NetLib4Games {

#define WAITEXTENSION 2		
#define MAX_CON_REQ_SIZE 4096

#include <windows.h>
// Defines "GetTickCount"
// If porting to another platform, just need to replace GetTickCount() with another function.

#ifdef NETLOG
inline static std::string Desc (NetPacketHeaderL1 *header) {
	bool			reliable	= (header->flags & NETL1_RELIABLE) != 0;
    bool            resent      = (header->flags & NETL1_RESENT) != 0;
	NetL1Type		type		= (NetL1Type) NETL1_GETTYPE (header->flags);
	unsigned long id			= header->id;
	std::string		typeStr;
	switch (type) {
	case l1User:		typeStr = "User"; break;
	case l1KeepAlive:	typeStr = "KeepAlive"; break;
	case l1Confirm:		typeStr = "Confirm"; break;
	case l1Connect:		typeStr = "Connect"; break;
	case l1Accept:		typeStr = "Accept"; break;
	case l1Disconnect:	typeStr = "Disconnect"; break;
	default:			typeStr = "UNKNOWN!?!";
	};
	return (reliable ? (std::string)"Reliable, " : (std::string)"Unreliable, ") +
        (resent ? (std::string)"Resent, " : (std::string)"") +
        typeStr + (std::string)", id: " + IntToString(id);
}
inline static std::string Desc (NetSimplePacket *packet) {
	return Desc ((NetPacketHeaderL1 *) packet->data);
}
#endif

///////////////////////////////////////////////////////////////////////////////
//	Globals
NetConReqValidatorL1 validatorL1;

///////////////////////////////////////////////////////////////////////////////
//	NetConL1
NetConL1::NetConL1 (NetConLow *connection, NetSettingsStaticL1& settings) : 
	m_connection			(connection),
	m_settingsStatic		(settings),
	m_reliableReceived		(m_settingsStatic.reliableBitBufSize,	0),
	m_unreliableReceived	(m_settingsStatic.unreliableBitBufSize, 0),
	m_sendIDReliable		(0),
	m_sendIDUnreliable		(0),
	m_wakeupTime			(INFINITE) {

	assert (m_connection != NULL); 

	NetLog ("Create L1 connection");

	// Init state
	m_handShaking	= m_connection->Connected ();

	// Init timing
	m_lastSent		= 
	m_lastReceived	= GetTickCount ();

	// Validate 
	Validate ();
}

NetConL1::NetConL1 (NetConLow *connection) :
	m_connection			(connection),
	m_settingsStatic		(),
	m_reliableReceived		(m_settingsStatic.reliableBitBufSize,	0),
	m_unreliableReceived	(m_settingsStatic.unreliableBitBufSize, 0),
	m_sendIDReliable		(0),
	m_sendIDUnreliable		(0),
	m_wakeupTime			(INFINITE) {

	assert (m_connection != NULL); 

	NetLog ("Create L1 connection");

	// Init state
	m_handShaking	= m_connection->Connected ();

	// Init timing
	m_lastSent		= 
	m_lastReceived	= GetTickCount ();

	// Validate 
	Validate ();
}

NetConL1::~NetConL1 () {

}

void NetConL1::Validate () {

}

bool NetConL1::Connect (std::string address, std::string connectionRequest) {

}

void NetConL1::Disconnect (bool clean) {

}

bool NetConL1::Connected () {
}

void NetConL1::Send (char *data, unsigned int size, bool reliable) {	

}

bool NetConL1::DataPending () {
}

unsigned int NetConL1::PendingDataSize () {

}

bool NetConL1::PendingIsResent() {

}

void NetConL1::ReceivePart (char *data, unsigned int offset, unsigned int& size) {

}
	
void NetConL1::DonePendingData () {

};

void NetConL1::ThreadExecute () {

}


}