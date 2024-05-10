/*	NetUtils.cpp

	Created 9-Jan-2004: Thomas Mulgrew (tmulgrew@slingshot.co.nz)

	Utility classes for net engine.
*/

#include "NetUtils.h"
#include <assert.h>

namespace NetLib4Games {

///////////////////////////////////////////////////////////////////////////////
//	NetRevolvingBitBuffer
NetRevolvingBitBuffer::NetRevolvingBitBuffer (unsigned int size, unsigned int initialTop) :

}

NetRevolvingBitBuffer::~NetRevolvingBitBuffer () {
	delete[] m_data;
}

void NetRevolvingBitBuffer::SetTop (unsigned int index, bool initialValue, bool& truesRemoved, bool& falsesRemoved) {

}

}