/*	NetLowLevelUDP.cpp

	Created 4-Jan-2004: Thomas Mulgrew (tmulgrew@slingshot.co.nz)

	UDP low level networking implementation
*/

#include "Udp/NetLowLevelUDP.h"
#include "NetMiscRoutines.h"
#include "NetLog.h"

namespace NetLib4Games {

#define TIMEOUTSECS  0
#define TIMEOUTUSECS 500000

// Winsock initialisation
ThreadLock winsockLock;
int winsockRefCount = 0;

bool WinsockAddRef () {
	WSADATA wsa;
	winsockLock.Lock ();

		// Reference counted approach to initialising and shutting down
		// winsock. Once the count drops to 0, winsock library is closed again.
		NetLog ("Winsock++");
		if (winsockRefCount == 0) {
			NetLog ("Start winsock");
			if (WSAStartup (0x0202, &wsa) == SOCKET_ERROR) {
				NetLog ("Startup failed!");
				WSACleanup ();
				winsockLock.Unlock ();
				return false;
			}
		}
		winsockRefCount++;
	winsockLock.Unlock ();
	return true;
}

void WinsockRelease () {
	winsockLock.Lock ();
		winsockRefCount--;
		NetLog ("Winsock--");
		if (winsockRefCount == 0) {
			NetLog ("Stop winsock");
			WSACleanup ();
		}
	winsockLock.Unlock ();
}

void FlushSocket (SOCKET sock) {

    // Try to flush through any buffered messages waiting on the winsock socket.

    while (true) {

        // Look for incoming packet
        fd_set readSet;
        FD_ZERO (&readSet);
        FD_SET (sock, &readSet);
        TIMEVAL timeout;
        timeout.tv_sec	= 0;
        timeout.tv_usec = 0;
        select (0, &readSet, NULL, NULL, &timeout);

        // Exit if none
		if (!FD_ISSET (sock, &readSet))
            return;

        // Read and discard packet
        char buffer;
		recvfrom (sock, &buffer, 1, 0, NULL, NULL);
    }
}

///////////////////////////////////////////////////////////////////////////////
//	WinsockUser
bool WinsockUser::CheckWinsock () {
	if (!m_winsockRef) {
		m_winsockRef = WinsockAddRef ();
		return m_winsockRef;
	}
	else
		return true;
}

WinsockUser::~WinsockUser () {
	if (m_winsockRef) {
		WinsockRelease ();
		m_winsockRef = false;
	}
}

///////////////////////////////////////////////////////////////////////////////
//	NetConLowUDP
NetConLowUDP::NetConLowUDP () :
	m_connected (false),
	m_listen (NULL),
	m_ownSocket (true),
	m_connectedEvent (true, false),
	m_buffer (NULL) {

	NetLog ("Create UDP client connection");

	// Start the socket service thread
	m_socketServiceThread.Start (this);
    if (m_socketServiceThread.Running())                         // Thread can fail to start if OS refuses to create it
        m_socketServiceThread.RaisePriority ();
    else
        SetError((std::string)"Unable to create thread. Error: " + IntToString(GetLastError()));
}

NetConLowUDP::NetConLowUDP (SOCKET sharedSocket, sockaddr_in addr, unsigned int maxPacketSize, NetListenLowUDP *listen) :

}

NetConLowUDP::~NetConLowUDP () {

}

void NetConLowUDP::UnhookFromListen () {

}

void NetConLowUDP::ClearQueue () {

}

void NetConLowUDP::Disconnect () {

}

bool NetConLowUDP::Connect (std::string address) {

}

bool NetConLowUDP::Connected () {

}

unsigned int NetConLowUDP::MaxPacketSize () {

}

void NetConLowUDP::Send (char *data, unsigned int size) {

}

bool NetConLowUDP::Client () {

}

bool NetConLowUDP::DataPending () {

}

unsigned int NetConLowUDP::PendingDataSize () {

}

void NetConLowUDP::ReceivePart (char *data, unsigned int offset, unsigned int &size) {

}

void NetConLowUDP::DonePendingData () {

}

void NetConLowUDP::ThreadExecute () {


}

ThreadEvent& NetConLowUDP::Event () {
}

std::string NetConLowUDP::Address() {

}

///////////////////////////////////////////////////////////////////////////////
//	NetListenLowUDP
NetListenLowUDP::NetListenLowUDP (unsigned int port) :{

}

NetListenLowUDP::~NetListenLowUDP () {


}








}