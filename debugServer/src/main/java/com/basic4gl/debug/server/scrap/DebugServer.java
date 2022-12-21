package com.basic4gl.debug.server.scrap;

public class DebugServer {



//    public static Server createServer(int port)
//    {
//        Server server = new Server();
//
//        ServerConnector connector = new ServerConnector(server);
//        connector.setPort(port);
//        server.setConnectors(new Connector[]{connector});
//
//        ServletContextHandler context = new ServletContextHandler();
//        context.setContextPath("/");
//        context.addServlet(HelloServlet.class, "/hello");
//        context.addServlet(AsyncEchoServlet.class, "/echo/*");
//
//        HandlerCollection handlers = new HandlerCollection();
//        handlers.setHandlers(new Handler[]{context, new DefaultHandler()});
//        server.setHandler(handlers);
//
//        return server;
//    }
//
    public static void main(String[] args) throws Exception
    {
//        int port = ExampleUtil.getPort(args, "jetty.http.port", 8080);
        JettyServer server = new JettyServer();
        server.start();
        server.join();
    }
//    public static void main(String[] args) {
////        if (args.length != 1) {
////            System.err.println("Usage: java KKMultiServer <port number>");
////            System.exit(1);
////        }
//
//        int portNumber = 4444;//Integer.parseInt(args[0]);
//        boolean listening = true;
//
//        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
//
//
//            while (listening) {
//                System.out.println("Server has started on 127.0.0.1:80.\r\nWaiting for a connection…");
//                Socket client = serverSocket.accept();
//                System.out.println("A client connected.");
//
//                InputStream in = client.getInputStream();
//                OutputStream out = client.getOutputStream();
//                Scanner s = new Scanner(in, "UTF-8");
//
//                String data = s.useDelimiter("\\r\\n\\r\\n").next();
//                Matcher get = Pattern.compile("^GET").matcher(data);
//
//                if (get.find()) {
//                    Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
//                    match.find();
//                    byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
//                            + "Connection: Upgrade\r\n"
//                            + "Upgrade: websocket\r\n"
//                            + "Sec-WebSocket-Accept: "
//                            + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
//                            + "\r\n\r\n").getBytes("UTF-8");
//                    out.write(response, 0, response.length);
//                }
//
//                new KKMultiServerThread(client).start();
//            }
//        } catch (IOException e) {
//            System.err.println("Could not listen on port " + portNumber);
//            System.exit(-1);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//
//            System.err.println("Could not listen on port " + portNumber);
//            System.exit(-1);
//        }
//
//    }
}

