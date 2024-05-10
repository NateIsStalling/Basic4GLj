package com.basic4gl.library.net;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.FunctionSpecification;
import com.basic4gl.lib.util.FileStreamResourceStore;
import com.basic4gl.lib.util.FunctionLibrary;
import com.basic4gl.lib.util.IAppSettings;
import com.basic4gl.library.netlib4games.NetConL2;
import com.basic4gl.library.netlib4games.NetListenLow;
import com.basic4gl.library.netlib4games.NetSettingsL1;
import com.basic4gl.library.netlib4games.NetSettingsL2;
import com.basic4gl.library.netlib4games.udp.NetConLowUDP;
import com.basic4gl.library.netlib4games.udp.NetListenLowUDP;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.util.Function;

import java.io.*;
import java.net.DatagramPacket;
import java.util.*;

import static com.basic4gl.library.netlib4games.NetLogger.DebugNetLogger;
import static com.basic4gl.runtime.types.BasicValType.VTP_INT;
import static com.basic4gl.runtime.types.BasicValType.VTP_STRING;
import static com.basic4gl.runtime.util.Assert.assertTrue;

public class NetBasicLib implements FunctionLibrary {
    private final int MAX_CHANNELS = 32;


    static int serverCount = 0;

    static byte[] buffer = new byte[65536];

    static String lastError;

    FileStreamResourceStore fileStreams;
    NetServerStore servers;

    NetConnectionStore connections;

    @Override
    public Map<String, Constant> constants() {
        Map<String, Constant> c = new HashMap<String, Constant>();
        c.put("CHANNEL_UNORDERED", new Constant(0));
        c.put("CHANNEL_ORDERED", new Constant(1));
        c.put("CHANNEL_MAX", new Constant(MAX_CHANNELS - 1));

        return c;
    }

    @Override
    public Map<String, FunctionSpecification[]> specs() {
        Map<String, FunctionSpecification[]> s = new TreeMap<>();
        // Functions
        s.put("NewServer", new FunctionSpecification[]{new FunctionSpecification(WrapNewServer.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, true, false, null)});
        s.put("DeleteServer", new FunctionSpecification[]{new FunctionSpecification(WrapDeleteServer.class, new ParamTypeList(VTP_INT), true, false, VTP_INT, true, false, null)});
        s.put("ConnectionPending", new FunctionSpecification[]{new FunctionSpecification(WrapConnectionPending.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, false, false, null)});
        s.put("AcceptConnection", new FunctionSpecification[]{new FunctionSpecification(WrapAcceptConnection.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, true, false, null)});
        s.put("RejectConnection", new FunctionSpecification[]{new FunctionSpecification(WrapRejectConnection.class, new ParamTypeList(VTP_INT), true, false, VTP_INT, true, false, null)});
        s.put("NewConnection", new FunctionSpecification[]{new FunctionSpecification(WrapNewConnection.class, new ParamTypeList(VTP_STRING, VTP_INT), true, true, VTP_INT, true, false, null)});
        s.put("DeleteConnection", new FunctionSpecification[]{new FunctionSpecification(WrapDeleteConnection.class, new ParamTypeList(VTP_INT), true, false, VTP_INT, true, false, null)});
        s.put("ConnectionHandShaking", new FunctionSpecification[]{new FunctionSpecification(WrapConnectionHandShaking.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, false, false, null)});
        s.put("ConnectionConnected", new FunctionSpecification[]{new FunctionSpecification(WrapConnectionConnected.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, false, false, null)});
        s.put("MessagePending", new FunctionSpecification[]{new FunctionSpecification(WrapMessagePending.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, false, false, null)});
        s.put("ReceiveMessage", new FunctionSpecification[]{new FunctionSpecification(WrapReceiveMessage.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, false, false, null)});
        s.put("MessageChannel", new FunctionSpecification[]{new FunctionSpecification(WrapMessageChannel.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, false, false, null)});

        s.put("MessageReliable", new FunctionSpecification[]{new FunctionSpecification(WrapMessageReliable.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, false, false, null)});
        s.put("MessageSmoothed", new FunctionSpecification[]{new FunctionSpecification(WrapMessageSmoothed.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, false, false, null)});
        s.put("SendMessage", new FunctionSpecification[]{new FunctionSpecification(WrapSendMessage.class, new ParamTypeList(VTP_INT, VTP_INT, VTP_INT, VTP_INT), true, true, VTP_INT, false, false, null)});

        s.put("ConnectionAddress", new FunctionSpecification[]{new FunctionSpecification(WrapConnectionAddress.class, new ParamTypeList(VTP_INT), true, true, VTP_STRING, false, false, null)});

        // L1 settings
        s.put("SetConnectionHandshakeTimeout", new FunctionSpecification[]{new FunctionSpecification(WrapSetConnectionHandshakeTimeout.class, new ParamTypeList(VTP_INT, VTP_INT), true, false, VTP_INT, false, false, null)});
        s.put("SetConnectionTimeout", new FunctionSpecification[]{new FunctionSpecification(WrapSetConnectionTimeout.class, new ParamTypeList(VTP_INT, VTP_INT), true, false, VTP_INT, false, false, null)});
        s.put("SetConnectionKeepAlive", new FunctionSpecification[]{new FunctionSpecification(WrapSetConnectionKeepAlive.class, new ParamTypeList(VTP_INT, VTP_INT), true, false, VTP_INT, false, false, null)});
        s.put("SetConnectionReliableResend", new FunctionSpecification[]{new FunctionSpecification(WrapSetConnectionReliableResend.class, new ParamTypeList(VTP_INT, VTP_INT), true, false, VTP_INT, false, false, null)});
        s.put("SetConnectionDuplicates", new FunctionSpecification[]{new FunctionSpecification(WrapSetConnectionDuplicates.class, new ParamTypeList(VTP_INT, VTP_INT), true, false, VTP_INT, false, false, null)});

        // L2 settings
        s.put("SetConnectionSmoothingPercentage", new FunctionSpecification[]{new FunctionSpecification(WrapSetConnectionSmoothingPercentage.class, new ParamTypeList(VTP_INT, VTP_INT), true, false, VTP_INT, false, false, null)});
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
    public void init(TomVM vm, IAppSettings settings, String[] args) {
        if (fileStreams == null) {
            fileStreams = new FileStreamResourceStore();
        }
        if (servers == null) {
            servers = new NetServerStore();
        }
        if (connections == null) {
            connections = new NetConnectionStore();
        }

        fileStreams.clear();

        // Clear error state
        lastError = "";

        // Configure NetLib4Games logger
        DebugNetLogger();
    }

    @Override
    public void init(TomBasicCompiler comp) {
        if (fileStreams == null) {
            fileStreams = new FileStreamResourceStore();
        }
        if (servers == null) {
            servers = new NetServerStore();
        }
        if (connections == null) {
            connections = new NetConnectionStore();
        }


        // Register resources
        comp.VM().addResources(fileStreams);
        comp.VM().addResources(servers);
        comp.VM().addResources(connections);

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



    ////////////////////////////////////////////////////////////////////////////////
//  Helper functions
    boolean CheckError(com.basic4gl.library.netlib4games.HasErrorState obj) {
        assertTrue(obj != null);

        if (obj.error()) {
            lastError = obj.getError();
            return false;
        } else {
            lastError = "";
            return true;
        }
    }


////////////////////////////////////////////////////////////////////////////////
//  Function wrappers


    public final class WrapNewServer implements Function {
        public void run(TomVM vm) {

            // Create listener for socket
            NetListenLowUDP server = new NetListenLowUDP(vm.getIntParam(1));
            serverCount++;

            // Store it
            if (!CheckError(server)) {
                serverCount--;
                server.dispose();
                vm.getReg().setIntVal(0);
            } else
                vm.getReg().setIntVal(servers.alloc(server));
        }
    }

    public final class WrapDeleteServer implements Function {
        public void run(TomVM vm) {
            int index = vm.getIntParam(1);
            if (index > 0 && servers.isIndexValid(index))
                servers.free(index);
            else
                lastError = "Invalid network server handle";
        }
    }

    public final class WrapConnectionPending implements Function {
        public void run(TomVM vm) {

            // Find server
            int index = vm.getIntParam(1);
            if (index > 0 && servers.isIndexValid(index)) {
                NetListenLow server = servers.getValueAt(index);
                vm.getReg().setIntVal(server.ConnectionPending() ? -1 : 0);
            } else {
                vm.getReg().setIntVal(0);
                lastError = "Invalid network server handle";
            }
        }
    }

    public final class WrapAcceptConnection implements Function {
        public void run(TomVM vm) {

            // Find server
            int index = vm.getIntParam(1);
            if (index > 0 && servers.isIndexValid(index)) {
                NetListenLow server = servers.getValueAt(index);

                // Accept connection (if pending)
                if (server.ConnectionPending()) {
                    NetConL2 connection = new NetConL2(server.AcceptConnection());
                    if (!CheckError(connection)) {
                        connection.dispose();
                        vm.getReg().setIntVal(0);
                    } else

                        // Store connection
                        vm.getReg().setIntVal(connections.alloc(connection));
                } else
                    vm.getReg().setIntVal(0);
            } else
                lastError = "Invalid network server handle";
        }
    }

    public final class WrapRejectConnection implements Function {
        public void run(TomVM vm) {

            // Find server
            int index = vm.getIntParam(1);
            if (index > 0 && servers.isIndexValid(index)) {
                NetListenLow server = servers.getValueAt(index);

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
            String addressString = vm.getStringParam(2) + ':' +String.valueOf(vm.getIntParam(1));

            // Create new connection
            NetConL2 connection = new NetConL2(new NetConLowUDP());
            connection.Connect(addressString);
            if (!CheckError(connection)) {
                connection.dispose();
                vm.getReg().setIntVal(0);
            } else

                // Store connection
                vm.getReg().setIntVal(connections.alloc(connection));
        }
    }

    public final class WrapDeleteConnection implements Function {
        public void run(TomVM vm) {
            int index = vm.getIntParam(1);
            if (index > 0 && connections.isIndexValid(index))
                connections.free(index);
            else
                lastError = "Invalid network connection handle";
        }
    }

    public final class WrapConnectionHandShaking implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.getIntParam(1);
            if (index > 0 && connections.isIndexValid(index)) {
                NetConL2 connection = connections.getValueAt(index);
                vm.getReg().setIntVal(connection.HandShaking() ? 1 : 0);
            } else {
                vm.getReg().setIntVal(0);
                lastError = "Invalid network connection handle";
            }
        }
    }

    public final class WrapConnectionConnected implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.getIntParam(1);
            if (index > 0 && connections.isIndexValid(index)) {
                NetConL2 connection = connections.getValueAt(index);
                vm.getReg().setIntVal(connection.Connected() ? 1 : 0);
            } else {
                vm.getReg().setIntVal(0);
                lastError = "Invalid network connection handle";
            }
        }
    }

    public final class WrapMessagePending implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.getIntParam(1);
            if (index > 0 && connections.isIndexValid(index)) {
                NetConL2 connection = connections.getValueAt(index);

                // Check for data
                vm.getReg().setIntVal(connection.DataPending() ? -1 : 0);
            } else {
                vm.getReg().setIntVal(0);
                lastError = "Invalid network connection handle";
            }
        }
    }

    public final class WrapReceiveMessage implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.getIntParam(1);
            if (index > 0 && connections.isIndexValid(index)) {
                NetConL2 connection = connections.getValueAt(index);

                if (connection.DataPending()) {

                    // Get message properties
                    int channel = connection.PendingChannel();
                    boolean reliable = connection.PendingReliable(),
                            smoothed = connection.PendingSmoothed();

                    // Get message data
                    int size = 65536;
                    DatagramPacket packet = new DatagramPacket(buffer, size);
                    size = connection.Receive(buffer, size);

                    // Copy into string stream
                    InputStream stream = new ByteArrayInputStream(Arrays.copyOf(buffer, size));

                    // Create message
                    NetMessageStream message = new NetMessageStream(
                            connections,
                            index,
                            channel,
                            reliable,
                            smoothed,
                            stream);

                    // Store it
                    vm.getReg().setIntVal(fileStreams.alloc(message));
                } else
                    vm.getReg().setIntVal(0);
            } else {
                vm.getReg().setIntVal(0);
                lastError = "Invalid network connection handle";
            }
        }
    }

    public final class WrapMessageChannel implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.getIntParam(1);
            if (index > 0 && connections.isIndexValid(index)) {
                NetConL2 connection = connections.getValueAt(index);
                if (connection.DataPending())
                    vm.getReg().setIntVal(connection.PendingChannel());
                else
                    vm.getReg().setIntVal(0);
            } else {
                vm.getReg().setIntVal(0);
                lastError = "Invalid network connection handle";
            }
        }
    }

    public final class WrapMessageReliable implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.getIntParam(1);
            if (index > 0 && connections.isIndexValid(index)) {
                NetConL2 connection = connections.getValueAt(index);
                if (connection.DataPending())
                    vm.getReg().setIntVal(connection.PendingReliable() ? 1 : 0);
                else
                    vm.getReg().setIntVal(0);
            } else {
                vm.getReg().setIntVal(0);
                lastError = "Invalid network connection handle";
            }
        }
    }

    public final class WrapMessageSmoothed implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.getIntParam(1);
            if (index > 0 && connections.isIndexValid(index)) {
                NetConL2 connection = connections.getValueAt(index);
                if (connection.DataPending())
                    vm.getReg().setIntVal(connection.PendingSmoothed() ? 1 : 0);
                else
                    vm.getReg().setIntVal(0);
            } else {
                vm.getReg().setIntVal(0);
                lastError = "Invalid network connection handle";
            }
        }
    }

    public final class WrapSendMessage implements Function {
        public void run(TomVM vm) {
            int index = vm.getIntParam(4);
            int channel = vm.getIntParam(3);
            boolean reliable = vm.getIntParam(2) == 1;
            boolean smoothed = vm.getIntParam(1) == 1;

            // Find connection
            if (index > 0 && connections.isIndexValid(index)) {

                // Verify channel
                if (channel >= 0 && channel < MAX_CHANNELS) {

                    // Create message
                    NetMessageStream message = new NetMessageStream(
                            connections,
                            index,
                            channel,
                            reliable,
                            smoothed,
                            new ByteArrayOutputStream());

                    // Store message
                    vm.getReg().setIntVal(fileStreams.alloc(message));
                } else {
                    vm.getReg().setIntVal(0);
                    lastError = "Invalid channel index. Must be 0 - 31.";
                }
            } else {
                vm.getReg().setIntVal(0);
                lastError = "Invalid network connection handle";
            }
        }
    }

    public final class WrapConnectionAddress implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.getIntParam(1);
            if (index > 0 && connections.isIndexValid(index)) {

                NetConL2 connection = connections.getValueAt(index);
                vm.setRegString(connection.Address());
            } else
                vm.setRegString("");
        }
    }

    public final class WrapSetConnectionHandshakeTimeout implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.getIntParam(2);
            if (index > 0 && connections.isIndexValid(index)) {
                NetConL2 connection = connections.getValueAt(index);

                // Get value
                int value = vm.getIntParam(1);
                if (value < 1)
                    value = 1;

                // Update settings
                NetSettingsL1 settings = connection.L1Settings();
                settings.handshakeTimeout = value;
                connection.SetL1Settings(settings);
            }
        }
    }

    public final class WrapSetConnectionTimeout implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.getIntParam(2);
            if (index > 0 && connections.isIndexValid(index)) {
                NetConL2 connection = connections.getValueAt(index);

                // Get value
                int value = vm.getIntParam(1);
                if (value < 1)
                    value = 1;

                // Update settings
                NetSettingsL1 settings = connection.L1Settings();
                settings.timeout = value;
                connection.SetL1Settings(settings);
            }
        }
    }

    public final class WrapSetConnectionKeepAlive implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.getIntParam(2);
            if (index > 0 && connections.isIndexValid(index)) {
                NetConL2 connection = connections.getValueAt(index);

                // Get value
                int value = vm.getIntParam(1);
                if (value < 1)
                    value = 1;

                // Update settings
                NetSettingsL1 settings = connection.L1Settings();
                settings.keepAlive = value;
                connection.SetL1Settings(settings);
            }
        }
    }

    public final class WrapSetConnectionReliableResend implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.getIntParam(2);
            if (index > 0 && connections.isIndexValid(index)) {
                NetConL2 connection = connections.getValueAt(index);

                // Get value
                int value = vm.getIntParam(1);
                if (value < 1)
                    value = 1;
                if (value > 10000)
                    value = 10000;

                // Update settings
                NetSettingsL1 settings = connection.L1Settings();
                settings.reliableResend = value;
                connection.SetL1Settings(settings);
            }
        }
    }

    public final class WrapSetConnectionDuplicates implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.getIntParam(2);
            if (index > 0 && connections.isIndexValid(index)) {
                NetConL2 connection = connections.getValueAt(index);

                // Get value
                int value = vm.getIntParam(1);
                if (value < 1)
                    value = 1;
                if (value > 100)
                    value = 100;

                // Update settings
                NetSettingsL1 settings = connection.L1Settings();
                settings.dup = value;
                connection.SetL1Settings(settings);
            }
        }
    }

    public final class WrapSetConnectionSmoothingPercentage implements Function {
        public void run(TomVM vm) {

            // Find connection
            int index = vm.getIntParam(2);
            if (index > 0 && connections.isIndexValid(index)) {
                NetConL2 connection = connections.getValueAt(index);

                // Get value
                int value = vm.getIntParam(1);
                if (value < 0)
                    value = 0;
                if (value > 100)
                    value = 100;

                // Update settings
                NetSettingsL2 settings = connection.Settings();
                settings.smoothingPercentage = value;
                connection.SetSettings(settings);
            }
        }
    }
}
