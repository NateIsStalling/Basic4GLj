package com.basic4gl.library.net;


//UDP Server
class NetServerWrapper {
    NetListenLowUDP m_server;

    public NetServerWrapper(int port) {

        try {
            m_server = new NetListenLowUDP(port);
        } catch (SocketException e) {
            e.printStackTrace();
            m_server.close();
            m_server = null;
        }
    }

    public void dispose() {
        m_server.close();
        m_server = null;
    }

    public NetListenLowUDP Server() {
        return m_server;
    }
}
