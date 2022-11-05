package com.basic4gl.library.desktopgl;
//---------------------------------------------------------------------------
// Created 26-Feb-2005: Thomas Mulgrew (tmulgrew@slingshot.co.nz)
// Copyright (C) Thomas Mulgrew
/*#pragma hdrstop

#include "TomNetBasicLib.h"
#include "TomFileIOBasicLib.h"

#include <winsock2.h>
#include "UDP/NetLowLevelUDP.h"
#include "NetLayer2.h"

#include <sstream>

using NetLib4Games.NetConL2;
using NetLib4Games.NetListenLow;
using NetLib4Games.NetListenLowUDP;
using NetLib4Games.NetConLow;
using NetLib4Games.NetConLowUDP;
*/
//---------------------------------------------------------------------------

/*
//#pragma package(smart_init)
public class TomNetBasicLib implements FunctionLibrary {
////////////////////////////////////////////////////////////////////////////////
//  Error handling
//
/// We will use the FileError mechanism created in TomFileIOBasicLib.
//extern String lastError;
String lastError;
////////////////////////////////////////////////////////////////////////////////
//  NetServerWrapper
//
/// Wraps around a NetListenLowUDP.
/// Was originally intended to boost the main thread priority when any servers
/// are running. However this behaviour turned out to be unwanted, and was
/// disabled. The wrapper currently adds NO functionality at all..

    static int serverCount = 0;

    static byte[] buffer = new byte[65536];

    @Override
    public Map<String, Constant> constants() {
        Map<String, Constant> c = new HashMap<>();
        // Constants
        c.put("CHANNEL_UNORDERED", new Constant(0));
        c.put("CHANNEL_ORDERED", new Constant(1));
        c.put("CHANNEL_MAX", NETL2_MAXCHANNELS - 1);
        return c;
    }

    @Override
    public Map<String, FuncSpec[]> specs() {
        Map<String, FuncSpec[]> s = new TreeMap<>();
        // Functions
        s.put("NewServer", new FuncSpec[]{new FuncSpec(WrapNewServer.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, true, false, null)});
        s.put("DeleteServer", new FuncSpec[]{new FuncSpec(WrapDeleteServer.class, new ParamTypeList(VTP_INT), true, false, VTP_INT, true, false, null)});
        s.put("ConnectionPending", new FuncSpec[]{new FuncSpec(WrapConnectionPending.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, false, false, null)});
        s.put("AcceptConnection", new FuncSpec[]{new FuncSpec(WrapAcceptConnection.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, true, false, null)});
        s.put("RejectConnection", new FuncSpec[]{new FuncSpec(WrapRejectConnection.class, new ParamTypeList(VTP_INT), true, false, VTP_INT, true, false, null)});
        s.put("NewConnection", new FuncSpec[]{new FuncSpec(WrapNewConnection.class, new ParamTypeList(VTP_STRING, VTP_INT), true, true, VTP_INT, true, false, null)});
        s.put("DeleteConnection", new FuncSpec[]{new FuncSpec(WrapDeleteConnection.class, new ParamTypeList(VTP_INT), true, false, VTP_INT, true, false, null)});
        s.put("ConnectionHandShaking", new FuncSpec[]{new FuncSpec(WrapConnectionHandShaking.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, false, false, null)});
        s.put("ConnectionConnected", new FuncSpec[]{new FuncSpec(WrapConnectionConnected.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, false, false, null)});
        s.put("MessagePending", new FuncSpec[]{new FuncSpec(WrapMessagePending.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, false, false, null)});
        s.put("ReceiveMessage", new FuncSpec[]{new FuncSpec(WrapReceiveMessage.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, false, false, null)});
        s.put("MessageChannel", new FuncSpec[]{new FuncSpec(WrapMessageChannel.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, false, false, null)});
        s.put("MessageReliable", new FuncSpec[]{new FuncSpec(WrapMessageReliable.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, false, false, null)});
        s.put("MessageSmoothed", new FuncSpec[]{new FuncSpec(WrapMessageSmoothed.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, false, false, null)});
        s.put("SendMessage", new FuncSpec[]{new FuncSpec(WrapSendMessage.class, new ParamTypeList(VTP_INT, VTP_INT, VTP_INT, VTP_INT), true, true, VTP_INT, false, false, null)});
        s.put("ConnectionAddress", new FuncSpec[]{new FuncSpec(WrapConnectionAddress.class, new ParamTypeList(VTP_INT), true, true, VTP_STRING, false, false, null)});

        // L1 settings
        s.put("SetConnectionHandshakeTimeout", new FuncSpec[]{new FuncSpec(WrapSetConnectionHandshakeTimeout.class, new ParamTypeList(VTP_INT, VTP_INT), true, false, VTP_INT, false, false, null)});
        s.put("SetConnectionTimeout", new FuncSpec[]{new FuncSpec(WrapSetConnectionTimeout.class, new ParamTypeList(VTP_INT, VTP_INT), true, false, VTP_INT, false, false, null)});
        s.put("SetConnectionKeepAlive", new FuncSpec[]{new FuncSpec(WrapSetConnectionKeepAlive.class, new ParamTypeList(VTP_INT, VTP_INT), true, false, VTP_INT, false, false, null)});
        s.put("SetConnectionReliableResend", new FuncSpec[]{new FuncSpec(WrapSetConnectionReliableResend.class, new ParamTypeList(VTP_INT, VTP_INT), true, false, VTP_INT, false, false, null)});
        s.put("SetConnectionDuplicates", new FuncSpec[]{new FuncSpec(WrapSetConnectionDuplicates.class, new ParamTypeList(VTP_INT, VTP_INT), true, false, VTP_INT, false, false, null)});

        // L2 settings
        s.put("SetConnectionSmoothingPercentage", new FuncSpec[]{new FuncSpec(WrapSetConnectionSmoothingPercentage.class, new ParamTypeList(VTP_INT, VTP_INT), true, false, VTP_INT, false, false, null)});
        return s;
    }

    @Override
    public HashMap<String, String> getTokenTips() {
        return null;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public void init(TomVM vm) {

    }

    @Override
    public void init(TomBasicCompiler comp) {

    }

    @Override
    public void cleanup() {

    }

    @Override
    public List<String> getDependencies() {
        return null;
    }

    @Override
    public List<String> getClassPathObjects() {
        return null;
    }

    //UDP Server
    class NetServerWrapper {
        DatagramSocket m_server;

        public NetServerWrapper(int port) {

            try {
                m_server = new DatagramSocket(port);
            } catch (SocketException e) {
                e.printStackTrace();
                m_server.close();
                m_server = null;
            }
            serverCount++;
        }

        public void dispose() {
            serverCount--;
            m_server.close();
            m_server = null;
        }

        public DatagramSocket Server() {
            return m_server;
        }
    }

////////////////////////////////////////////////////////////////////////////////
//  NetServerStore
//
//  Stores NetListenLow network servers.

    //typedef vmPointerResourceStore<NetServerWrapper> NetServerStore;
    PointerResourceStore<NetServerWrapper> servers;

////////////////////////////////////////////////////////////////////////////////
//  NetConnectionStore
//
//  Stores NetConL2 network connections.

    //typedef vmPointerResourceStore<NetConL2> NetConnectionStore;
    PointerResourceStore<Socket> connections;

////////////////////////////////////////////////////////////////////////////////
//  NetMessageStream
//
//  A network message wrapped up as an input or output stream.
//  By inheriting from FileStream, we can plug into the file I/O mechanism and
//  allow users to use the same code as for accessing files to read/write network
//  messages.

    public class NetMessageStream extends FileStream {
        public int connectionHandle;   // Note: We use connection handles rather than
        // references, so that we can fail gracefully
        // if the connection is deleted before we
        // have finalised the message.
        public int channel;
        public boolean reliable, smoothed;

        public NetMessageStream(
                int _connectionHandle,
                int _channel,
                boolean _reliable,
                boolean _smoothed,
                FileInputStream _in) {

            super(_in);
            this.connectionHandle = _connectionHandle;
            this.channel = _channel;
            this.reliable = _reliable;
            this.smoothed = _smoothed;
        }

        public NetMessageStream(
                int _connectionHandle,
                int _channel,
                boolean _reliable,
                boolean _smoothed,
                FileOutputStream _out) {

            super(_out);
            this.connectionHandle = _connectionHandle;
            this.channel = _channel;
            this.reliable = _reliable;
            this.smoothed = _smoothed;
        }

        //virtual ~NetMessageStream ();
        public void dispose() {
            if (out != null && connections.IndexStored(connectionHandle)) {

                // Send pending packet
                std.stringstream * stream = (std.stringstream *) out;                // (Net messages are always string streams)
                NetConL2 connection = connections.Value(connectionHandle);

                if (channel >= 0 && channel < NETL2_MAXCHANNELS && stream.str().c_str() != null)
                    connection.Send(
                            (char*)stream.str().c_str(),
                        stream.str().length(),
                        channel,
                        reliable,
                        smoothed);
            }
        }
    }

    ;


    ////////////////////////////////////////////////////////////////////////////////
//  Helper functions
    boolean CheckError(HasErrorState obj) {
        assertTrue(obj != null);

        if (obj.hasError()) {
            lastError = obj.getError();
            return false;
        } else {
            lastError = "";
            return true;
        }
    }

////////////////////////////////////////////////////////////////////////////////
//  Initialisation

    void InitTomNetBasicLib(TomBasicCompiler comp) {

        // Register resources
        comp.VM().AddResources(servers);
        comp.VM().AddResources(connections);


    }


////////////////////////////////////////////////////////////////////////////////
//  Function wrappers


    public final class WrapNewServer implements Function {
        public void run(TomVM vm) {

            // Create listener for socket
            NetServerWrapper server = new NetServerWrapper(vm.GetIntParam(1));

            // Store it
            if (!CheckError(server.Server())) {
                server.dispose();
                vm.Reg().setIntVal(0);
            } else
                vm.Reg().setIntVal(servers.Alloc(server));
        }
    }

    public final class WrapDeleteServer implements Function {
        public void run(TomVM vm) {
            int index = vm.GetIntParam(1);
            if (index > 0 && servers.IndexValid(index))
                servers.Free(index);
            else
                lastError = "Invalid network server handle";
        }
    }

    public final class WrapConnectionPending implements Function {
        public void run(TomVM vm) {

            // Find server
            int index = vm.GetIntParam(1);
            if (index > 0 && servers.IndexValid(index)) {
                NetListenLow * server = servers.Value(index).Server();
                vm.Reg().setIntVal(server.ConnectionPending() ? -1 : 0);
            } else {
                vm.Reg().setIntVal(0);
                lastError = "Invalid network server handle";
            }
        }
    }

    public final class WrapAcceptConnection implements Function {
        public void run(TomVM vm) {

            // Find server
            int index = vm.GetIntParam(1);
            if (index > 0 && servers.IndexValid(index)) {
                NetListenLow * server = servers.Value(index).Server();

                // Accept connection (if pending)
                if (server.ConnectionPending()) {
                    NetConL2 connection = new NetConL2(server.AcceptConnection());
                    if (!CheckError(connection)) {
                        connection.dispose();
                        vm.Reg().setIntVal(0);
                    } else

                        // Store connection
                        vm.Reg().setIntVal(connections.Alloc(connection));
                } else
                    vm.Reg().setIntVal(0);
            } else
                lastError = "Invalid network server handle";
        }
    }

    public final class WrapRejectConnection implements Function {
        public void run(TomVM vm) {

            // Find server
            int index = vm.GetIntParam(1);
            if (index > 0 && servers.IndexValid(index)) {
                NetListenLow * server = servers.Value(index).Server();

                // Reject connection
                if (server.ConnectionPending()) {
                    server.RejectConnection();
                    CheckError(server);
                }
            } else
                lastError = "Invalid network server handle";
        }
    }

    public final class WrapNewConnection implements Function {
        public void run(TomVM vm) {

            // Calculate address string
            String addressString = vm.GetStringParam(2) + ':' +String.valueOf(vm.GetIntParam(1));

            // Create new connection
            NetConL2 connection = new NetConL2(new NetConLowUDP());
            connection.Connect(addressString);
            if (!CheckError(connection)) {
                connection.dispose();
                vm.Reg().setIntVal(0);
            } else

                // Store connection
                vm.Reg().setIntVal(connections.Alloc(connection));
        }
    }

    public final class WrapDeleteConnection implements Function {
        public void run(TomVM vm) {
            int index = vm.GetIntParam(1);
            if (index > 0 && connections.IndexValid(index))
                connections.Free(index);
            else
                lastError = "Invalid network connection handle";
        }
    }

    public final class WrapConnectionHandShaking implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.GetIntParam(1);
            if (index > 0 && connections.IndexValid(index)) {
                Socket connection = connections.Value(index);
                vm.Reg().setIntVal(connection.HandShaking());
            } else {
                vm.Reg().setIntVal(0);
                lastError = "Invalid network connection handle";
            }
        }
    }

    public final class WrapConnectionConnected implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.GetIntParam(1);
            if (index > 0 && connections.IndexValid(index)) {
                Socket connection = connections.Value(index);
                vm.Reg().setIntVal(connection.isConnected() ? 1 : 0);
            } else {
                vm.Reg().setIntVal(0);
                lastError = "Invalid network connection handle";
            }
        }
    }

    public final class WrapMessagePending implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.GetIntParam(1);
            if (index > 0 && connections.IndexValid(index)) {
                Socket connection = connections.Value(index);

                // Check for data
                vm.Reg().setIntVal(connection.DataPending() ? -1 : 0);
            } else {
                vm.Reg().setIntVal(0);
                lastError = "Invalid network connection handle";
            }
        }
    }

    public final class WrapReceiveMessage implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.GetIntParam(1);
            if (index > 0 && connections.IndexValid(index)) {
                DatagramSocket connection = connections.Value(index);

                if (connection.DataPending()) {

                    // Get message properties
                    int channel = connection.PendingChannel();
                    boolean reliable = connection.PendingReliable(),
                            smoothed = connection.PendingSmoothed();

                    // Get message data
                    int size = 65536;
                    DatagramPacket packet = new DatagramPacket(buffer, size);
                    connection.receive(packet);

                    // Copy into string stream
                    Stream stream = new std.stringstream;
                    stream.write(buffer, size);

                    // Create message
                    NetMessageStream message = new NetMessageStream(
                            index,
                            channel,
                            reliable,
                            smoothed,
                            (FileInputStream)stream);

                    // Store it
                    vm.Reg().setIntVal(fileStreams.Alloc(message));
                } else
                    vm.Reg().setIntVal(0);
            } else {
                vm.Reg().setIntVal(0);
                lastError = "Invalid network connection handle";
            }
        }
    }

    public final class WrapMessageChannel implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.GetIntParam(1);
            if (index > 0 && connections.IndexValid(index)) {
                NetConL2 connection = connections.Value(index);
                if (connection.DataPending())
                    vm.Reg().setIntVal(connection.PendingChannel());
                else
                    vm.Reg().setIntVal(0);
            } else {
                vm.Reg().setIntVal(0);
                lastError = "Invalid network connection handle";
            }
        }
    }

    public final class WrapMessageReliable implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.GetIntParam(1);
            if (index > 0 && connections.IndexValid(index)) {
                NetConL2 connection = connections.Value(index);
                if (connection.DataPending())
                    vm.Reg().setIntVal(connection.PendingReliable());
                else
                    vm.Reg().setIntVal(0);
            } else {
                vm.Reg().setIntVal(0);
                lastError = "Invalid network connection handle";
            }
        }
    }

    public final class WrapMessageSmoothed implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.GetIntParam(1);
            if (index > 0 && connections.IndexValid(index)) {
                NetConL2 connection = connections.Value(index);
                if (connection.DataPending())
                    vm.Reg().setIntVal(connection.PendingSmoothed());
                else
                    vm.Reg().setIntVal(0);
            } else {
                vm.Reg().setIntVal(0);
                lastError = "Invalid network connection handle";
            }
        }
    }

    public final class WrapSendMessage implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.GetIntParam(4);
            if (index > 0 && connections.IndexValid(index)) {

                // Verify channel
                int channel = vm.GetIntParam(3);
                if (channel >= 0 && channel < NETL2_MAXCHANNELS) {

                    // Create message
                    NetMessageStream * message = new NetMessageStream(
                            index,
                            channel,
                            vm.GetIntParam(2),
                            vm.GetIntParam(1),
                            (GenericOStream *) new std.stringstream);

                    // Store message
                    vm.Reg().IntVal(fileStreams.Alloc(message));
                } else {
                    vm.Reg().setIntVal(0);
                    lastError = "Invalid channel index. Must be 0 - 31.";
                }
            } else {
                vm.Reg().setIntVal(0);
                lastError = "Invalid network connection handle";
            }
        }
    }

    public final class WrapConnectionAddress implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.GetIntParam(1);
            if (index > 0 && connections.IndexValid(index)) {

                try {
                    Socket connection = connections.Value(index);
                    vm.setRegString(new String(connection.getInetAddress().getAddress(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    vm.setRegString("0");
                }
            } else
                vm.setRegString("");
        }
    }

    public final class WrapSetConnectionHandshakeTimeout implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.GetIntParam(2);
            if (index > 0 && connections.IndexValid(index)) {
                NetConL2 connection = connections.Value(index);

                // Get value
                int value = vm.GetIntParam(1);
                if (value < 1)
                    value = 1;

                // Update settings
                NetLib4Games.NetSettingsL1 settings = connection.L1Settings();
                settings.handshakeTimeout = value;
                connection.SetL1Settings(settings);
            }
        }
    }

    public final class WrapSetConnectionTimeout implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.GetIntParam(2);
            if (index > 0 && connections.IndexValid(index)) {
                NetConL2 connection = connections.Value(index);

                // Get value
                int value = vm.GetIntParam(1);
                if (value < 1)
                    value = 1;

                // Update settings
                NetLib4Games.NetSettingsL1 settings = connection.L1Settings();
                settings.timeout = value;
                connection.SetL1Settings(settings);
            }
        }
    }

    public final class WrapSetConnectionKeepAlive implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.GetIntParam(2);
            if (index > 0 && connections.IndexValid(index)) {
                NetConL2 connection = connections.Value(index);

                // Get value
                int value = vm.GetIntParam(1);
                if (value < 1)
                    value = 1;

                // Update settings
                NetLib4Games.NetSettingsL1 settings = connection.L1Settings();
                settings.keepAlive = value;
                connection.SetL1Settings(settings);
            }
        }
    }

    public final class WrapSetConnectionReliableResend implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.GetIntParam(2);
            if (index > 0 && connections.IndexValid(index)) {
                NetConL2 connection = connections.Value(index);

                // Get value
                int value = vm.GetIntParam(1);
                if (value < 1)
                    value = 1;
                if (value > 10000)
                    value = 10000;

                // Update settings
                NetLib4Games.NetSettingsL1 settings = connection.L1Settings();
                settings.reliableResend = value;
                connection.SetL1Settings(settings);
            }
        }
    }

    public final class WrapSetConnectionDuplicates implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.GetIntParam(2);
            if (index > 0 && connections.IndexValid(index)) {
                NetConL2 connection = connections.Value(index);

                // Get value
                int value = vm.GetIntParam(1);
                if (value < 1)
                    value = 1;
                if (value > 100)
                    value = 100;

                // Update settings
                NetLib4Games.NetSettingsL1 settings = connection.L1Settings();
                settings.dup = value;
                connection.SetL1Settings(settings);
            }
        }
    }

    public final class WrapSetConnectionSmoothingPercentage implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.GetIntParam(2);
            if (index > 0 && connections.IndexValid(index)) {
                NetConL2 connection = connections.Value(index);

                // Get value
                int value = vm.GetIntParam(1);
                if (value < 0)
                    value = 0;
                if (value > 100)
                    value = 100;

                // Update settings
                NetLib4Games.NetSettingsL2 settings = connection.Settings();
                settings.smoothingPercentage = value;
                connection.SetSettings(settings);
            }
        }
    }
}*/