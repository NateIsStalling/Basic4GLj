//package com.basic4gl.library.net2;
//
//import com.basic4gl.library.net.NetListenLowUDP;
//
//import java.io.IOException;
//
//public class NetServerWrapper {
//    NetServer m_server;
//
//    public NetServerWrapper(int port) {
//
//
//        try {
//            m_server = new NetServer(port);
//        } catch (IOException e) {
//            e.printStackTrace();
//            m_server.close();
//            m_server = null;
//        }
//    }
//
//    public void dispose() {
//        m_server.close();
//        m_server = null;
//    }
//
//    public NetServer Server() {
//        return m_server;
//    }
//}
