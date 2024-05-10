/*	NetLowLevel.cpp

	Created 4-Jan-2005: Thomas Mulgrew (tmulgrew@slingshot.co.nz)

	Abstract low level networking classes.
*/

#include "NetLowLevel.h"
#include "NetLog.h"

namespace NetLib4Games {

///////////////////////////////////////////////////////////////////////////////
// Globals
NetConReqValidator *validator = NULL;
ThreadLock validatorLock;

///////////////////////////////////////////////////////////////////////////////
//	NetHasErrorState
void NetHasErrorState::SetError (std::string text) {
	NetLog ("ERROR!: " + text);
	HasErrorState::SetError (text);
}

void NetHasErrorStateThreadsafe::SetError (std::string text) {
	NetLog ("ERROR!: " + text);
	HasErrorStateThreadsafe::SetError (text);
}

///////////////////////////////////////////////////////////////////////////////
//	NetConLow
NetConLow::~NetConLow () {
	;
}

///////////////////////////////////////////////////////////////////////////////
//	NetListenLow
NetListenLow::~NetListenLow () {
	;
}

bool NetListenLow::IsConnectionRequest (NetSimplePacket *packet, std::string& requestString) {
	requestString = "";
	validatorLock.Lock ();
		bool result;
		if (validator != NULL)
			result = validator->IsConnectionRequest (packet, requestString);
		else
			result = false;
	validatorLock.Unlock ();
	return result;
}

///////////////////////////////////////////////////////////////////////////////
//	NetConReqValidator

NetConReqValidator::NetConReqValidator () {

	// Hook into validator
	validatorLock.Lock ();
		validator = this;
	validatorLock.Unlock ();
}

NetConReqValidator::~NetConReqValidator () {

	// Detach from validator
	validatorLock.Lock ();
		if (validator == this)
			validator = NULL;
	validatorLock.Unlock ();
}

}