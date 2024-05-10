//package com.basic4gl.library.net2;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.net.ProtocolFamily;
//import java.nio.channels.DatagramChannel;
//
//public class NetServer {
//    private DatagramChannel channel;
//    public NetServer(int port) throws IOException {
//        InetSocketAddress address = new InetSocketAddress("localhost", port);
//        channel = DatagramChannel.open();
//        channel.bind(address);
//    }
//
//    public void close() {
//        try {
//            channel.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
