package com.basic4gl.library.netlib4games;

/**
 * Net layer 1 connection types
 */
public enum NetL1Type {
/**
* User data
*/
l1User(0),
/**
* Keepalive packet to prevent connection timing out
*/
l1KeepAlive(1),
/**
* Confirm reliable packet received
*/
l1Confirm(2),
/**
* Request connection (client)
*/
l1Connect(3),
/**
* Accept connection (server)
*/
l1Accept(4),
/**
* Clean disconnect
*/
l1Disconnect(5);

private final int type;

NetL1Type(int type) {
	this.type = type;
}

public int getType() {
	return type;
}

public static NetL1Type fromInteger(int x) {
	switch (x) {
	case 0:
		return l1User;
	case 1:
		return l1KeepAlive;
	case 2:
		return l1Confirm;
	case 3:
		return l1Connect;
	case 4:
		return l1Accept;
	case 5:
		return l1Disconnect;
	}
	return null;
}
}
