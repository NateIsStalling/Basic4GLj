package com.basic4gl.library.standard;

import static com.basic4gl.library.netlib4games.NetLogger.initDebugNetLogger;
import static com.basic4gl.runtime.types.BasicValType.VTP_INT;
import static com.basic4gl.runtime.types.BasicValType.VTP_STRING;
import static com.basic4gl.runtime.util.Assert.assertTrue;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.FunctionSpecification;
import com.basic4gl.lib.util.FileStreamResourceStore;
import com.basic4gl.lib.util.FunctionLibrary;
import com.basic4gl.lib.util.IAppSettings;
import com.basic4gl.lib.util.IServiceCollection;
import com.basic4gl.library.netlib4games.*;
import com.basic4gl.library.netlib4games.udp.NetConLowUDP;
import com.basic4gl.library.netlib4games.udp.NetListenLowUDP;
import com.basic4gl.library.standard.net.NetConnectionStore;
import com.basic4gl.library.standard.net.NetMessageStream;
import com.basic4gl.library.standard.net.NetServerStore;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.util.Function;
import java.io.*;
import java.net.DatagramPacket;
import java.util.*;

public class NetBasicLib implements FunctionLibrary {
  private static final int MAX_CHANNELS = 32;

  private static int serverCount = 0;

  private static final byte[] buffer = new byte[65536];

  private static String lastError;

  static void setLastError(String error) {
    System.out.println(error);
    lastError = error;
  }

  static void clearError() {
    lastError = "";
  }

  private FileStreamResourceStore fileStreams;
  private NetServerStore servers;

  private NetConnectionStore connections;

  private NetConReqValidator netConReqValidator;

  @Override
  public Map<String, Constant> constants() {
    Map<String, Constant> c = new HashMap<>();
    c.put("CHANNEL_UNORDERED", new Constant(0));
    c.put("CHANNEL_ORDERED", new Constant(1));
    c.put("CHANNEL_MAX", new Constant(MAX_CHANNELS - 1));

    return c;
  }

  @Override
  public Map<String, FunctionSpecification[]> specs() {
    Map<String, FunctionSpecification[]> s = new TreeMap<>();
    // Functions
    s.put(
        "NewServer",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapNewServer.class,
              new ParamTypeList(VTP_INT),
              true,
              true,
              VTP_INT,
              true,
              false,
              null)
        });
    s.put(
        "DeleteServer",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapDeleteServer.class,
              new ParamTypeList(VTP_INT),
              true,
              false,
              VTP_INT,
              true,
              false,
              null)
        });
    s.put(
        "ConnectionPending",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapConnectionPending.class,
              new ParamTypeList(VTP_INT),
              true,
              true,
              VTP_INT,
              false,
              false,
              null)
        });
    s.put(
        "AcceptConnection",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapAcceptConnection.class,
              new ParamTypeList(VTP_INT),
              true,
              true,
              VTP_INT,
              true,
              false,
              null)
        });
    s.put(
        "RejectConnection",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapRejectConnection.class,
              new ParamTypeList(VTP_INT),
              true,
              false,
              VTP_INT,
              true,
              false,
              null)
        });
    s.put(
        "NewConnection",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapNewConnection.class,
              new ParamTypeList(VTP_STRING, VTP_INT),
              true,
              true,
              VTP_INT,
              true,
              false,
              null)
        });
    s.put(
        "DeleteConnection",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapDeleteConnection.class,
              new ParamTypeList(VTP_INT),
              true,
              false,
              VTP_INT,
              true,
              false,
              null)
        });
    s.put(
        "ConnectionHandShaking",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapConnectionHandShaking.class,
              new ParamTypeList(VTP_INT),
              true,
              true,
              VTP_INT,
              false,
              false,
              null)
        });
    s.put(
        "ConnectionConnected",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapConnectionConnected.class,
              new ParamTypeList(VTP_INT),
              true,
              true,
              VTP_INT,
              false,
              false,
              null)
        });
    s.put(
        "MessagePending",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapMessagePending.class,
              new ParamTypeList(VTP_INT),
              true,
              true,
              VTP_INT,
              false,
              false,
              null)
        });
    s.put(
        "ReceiveMessage",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapReceiveMessage.class,
              new ParamTypeList(VTP_INT),
              true,
              true,
              VTP_INT,
              false,
              false,
              null)
        });
    s.put(
        "MessageChannel",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapMessageChannel.class,
              new ParamTypeList(VTP_INT),
              true,
              true,
              VTP_INT,
              false,
              false,
              null)
        });
    s.put(
        "MessageReliable",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapMessageReliable.class,
              new ParamTypeList(VTP_INT),
              true,
              true,
              VTP_INT,
              false,
              false,
              null)
        });
    s.put(
        "MessageSmoothed",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapMessageSmoothed.class,
              new ParamTypeList(VTP_INT),
              true,
              true,
              VTP_INT,
              false,
              false,
              null)
        });
    s.put(
        "SendMessage",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapSendMessage.class,
              new ParamTypeList(VTP_INT, VTP_INT, VTP_INT, VTP_INT),
              true,
              true,
              VTP_INT,
              false,
              false,
              null)
        });
    s.put(
        "ConnectionAddress",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapConnectionAddress.class,
              new ParamTypeList(VTP_INT),
              true,
              true,
              VTP_STRING,
              false,
              false,
              null)
        });

    // L1 settings
    s.put(
        "SetConnectionHandshakeTimeout",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapSetConnectionHandshakeTimeout.class,
              new ParamTypeList(VTP_INT, VTP_INT),
              true,
              false,
              VTP_INT,
              false,
              false,
              null)
        });
    s.put(
        "SetConnectionTimeout",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapSetConnectionTimeout.class,
              new ParamTypeList(VTP_INT, VTP_INT),
              true,
              false,
              VTP_INT,
              false,
              false,
              null)
        });
    s.put(
        "SetConnectionKeepAlive",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapSetConnectionKeepAlive.class,
              new ParamTypeList(VTP_INT, VTP_INT),
              true,
              false,
              VTP_INT,
              false,
              false,
              null)
        });
    s.put(
        "SetConnectionReliableResend",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapSetConnectionReliableResend.class,
              new ParamTypeList(VTP_INT, VTP_INT),
              true,
              false,
              VTP_INT,
              false,
              false,
              null)
        });
    s.put(
        "SetConnectionDuplicates",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapSetConnectionDuplicates.class,
              new ParamTypeList(VTP_INT, VTP_INT),
              true,
              false,
              VTP_INT,
              false,
              false,
              null)
        });

    // L2 settings
    s.put(
        "SetConnectionSmoothingPercentage",
        new FunctionSpecification[] {
          new FunctionSpecification(
              WrapSetConnectionSmoothingPercentage.class,
              new ParamTypeList(VTP_INT, VTP_INT),
              true,
              false,
              VTP_INT,
              false,
              false,
              null)
        });
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
  public void init(TomVM vm, IServiceCollection services, IAppSettings settings, String[] args) {
    if (fileStreams == null) {
      fileStreams = services.getService(FileStreamResourceStore.class);
    }
    if (servers == null) {
      servers = new NetServerStore();
    }
    if (connections == null) {
      connections = new NetConnectionStore();
    }

    fileStreams.clear();

    // Clear error state
    clearError();

    // Configure NetLib4Games logger
    initDebugNetLogger();
  }

  @Override
  public void init(TomBasicCompiler comp, IServiceCollection services) {
    if (fileStreams == null) {
      fileStreams = services.getService(FileStreamResourceStore.class);
    }
    if (servers == null) {
      servers = new NetServerStore();
    }
    if (connections == null) {
      connections = new NetConnectionStore();
    }

    // Register resources
    comp.getVM().addResources(fileStreams);
    comp.getVM().addResources(servers);
    comp.getVM().addResources(connections);

    // Hook into validator
    netConReqValidator = new NetConReqValidatorL1();
    NetLowLevel.setValidator(netConReqValidator);
  }

  @Override
  public void cleanup() {

    // Detach from validator
    NetLowLevel.removeValidator(netConReqValidator);
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
  boolean checkError(com.basic4gl.library.netlib4games.HasErrorState obj) {
    assertTrue(obj != null);

    if (obj.hasError()) {
      setLastError(obj.getError());
      return false;
    } else {
      clearError();
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
      if (!checkError(server)) {
        serverCount--;
        server.dispose();
        vm.getReg().setIntVal(0);
      } else {
        vm.getReg().setIntVal(servers.alloc(server));
      }
    }
  }

  public final class WrapDeleteServer implements Function {
    public void run(TomVM vm) {
      int index = vm.getIntParam(1);
      if (index > 0 && servers.isIndexValid(index)) {
        servers.free(index);
      } else {
        setLastError("Invalid network server handle");
      }
    }
  }

  public final class WrapConnectionPending implements Function {
    public void run(TomVM vm) {

      // Find server
      int index = vm.getIntParam(1);
      if (index > 0 && servers.isIndexValid(index)) {
        NetListenLow server = servers.getValueAt(index);
        vm.getReg().setIntVal(server.isConnectionPending() ? -1 : 0);
      } else {
        vm.getReg().setIntVal(0);
        setLastError("Invalid network server handle");
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
        if (server.isConnectionPending()) {
          NetConL2 connection = new NetConL2(server.acceptConnection());
          if (!checkError(connection)) {
            connection.dispose();
            vm.getReg().setIntVal(0);
          } else {
            // Store connection
            vm.getReg().setIntVal(connections.alloc(connection));
          }
        } else {
          vm.getReg().setIntVal(0);
        }
      } else {
        setLastError("Invalid network server handle");
      }
    }
  }

  public final class WrapRejectConnection implements Function {
    public void run(TomVM vm) {

      // Find server
      int index = vm.getIntParam(1);
      if (index > 0 && servers.isIndexValid(index)) {
        NetListenLow server = servers.getValueAt(index);

        // Reject connection
        if (server.isConnectionPending()) {
          server.rejectConnection();
          checkError(server);
        }
      } else {
        setLastError("Invalid network server handle");
      }
    }
  }

  public final class WrapNewConnection implements Function {
    public void run(TomVM vm) {

      // Calculate address string
      String addressString = vm.getStringParam(2) + ':' + String.valueOf(vm.getIntParam(1));

      // Create new connection
      NetConL2 connection = new NetConL2(new NetConLowUDP());
      connection.connect(addressString);
      if (!checkError(connection)) {
        connection.dispose();
        vm.getReg().setIntVal(0);
      } else {
        // Store connection
        vm.getReg().setIntVal(connections.alloc(connection));
      }
    }
  }

  public final class WrapDeleteConnection implements Function {
    public void run(TomVM vm) {
      int index = vm.getIntParam(1);
      if (index > 0 && connections.isIndexValid(index)) {
        connections.free(index);
      } else {
        setLastError("Invalid network connection handle");
      }
    }
  }

  public final class WrapConnectionHandShaking implements Function {
    public void run(TomVM vm) {

      // Find connection
      int index = vm.getIntParam(1);
      if (index > 0 && connections.isIndexValid(index)) {
        NetConL2 connection = connections.getValueAt(index);
        vm.getReg().setIntVal(connection.isHandShaking() ? 1 : 0);
      } else {
        vm.getReg().setIntVal(0);
        setLastError("Invalid network connection handle");
      }
    }
  }

  public final class WrapConnectionConnected implements Function {
    public void run(TomVM vm) {

      // Find connection
      int index = vm.getIntParam(1);
      if (index > 0 && connections.isIndexValid(index)) {
        NetConL2 connection = connections.getValueAt(index);
        vm.getReg().setIntVal(connection.isConnected() ? 1 : 0);
      } else {
        vm.getReg().setIntVal(0);
        setLastError("Invalid network connection handle");
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
        vm.getReg().setIntVal(connection.hasDataPending() ? -1 : 0);
      } else {
        vm.getReg().setIntVal(0);
        setLastError("Invalid network connection handle");
      }
    }
  }

  public final class WrapReceiveMessage implements Function {
    public void run(TomVM vm) {

      // Find connection
      int index = vm.getIntParam(1);
      if (index > 0 && connections.isIndexValid(index)) {
        NetConL2 connection = connections.getValueAt(index);

        if (connection.hasDataPending()) {

          // Get message properties
          int channel = connection.getPendingChannel();
          boolean reliable = connection.isPendingReliable(),
              smoothed = connection.isPendingSmoothed();

          // Get message data
          int size = 65536;
          DatagramPacket packet = new DatagramPacket(buffer, size);
          size = connection.receive(buffer, size);

          // Copy into string stream
          InputStream stream = new ByteArrayInputStream(Arrays.copyOf(buffer, size));

          // Create message
          NetMessageStream message =
              new NetMessageStream(connections, index, channel, reliable, smoothed, stream);

          // Store it
          vm.getReg().setIntVal(fileStreams.alloc(message));
          message.close();
        } else {
          vm.getReg().setIntVal(0);
        }
      } else {
        vm.getReg().setIntVal(0);
        setLastError("Invalid network connection handle");
      }
    }
  }

  public final class WrapMessageChannel implements Function {
    public void run(TomVM vm) {

      // Find connection
      int index = vm.getIntParam(1);
      if (index > 0 && connections.isIndexValid(index)) {
        NetConL2 connection = connections.getValueAt(index);
        if (connection.hasDataPending()) {
          vm.getReg().setIntVal(connection.getPendingChannel());
        } else {
          vm.getReg().setIntVal(0);
        }
      } else {
        vm.getReg().setIntVal(0);
        setLastError("Invalid network connection handle");
      }
    }
  }

  public final class WrapMessageReliable implements Function {
    public void run(TomVM vm) {

      // Find connection
      int index = vm.getIntParam(1);
      if (index > 0 && connections.isIndexValid(index)) {
        NetConL2 connection = connections.getValueAt(index);
        if (connection.hasDataPending()) {
          vm.getReg().setIntVal(connection.isPendingReliable() ? 1 : 0);
        } else {
          vm.getReg().setIntVal(0);
        }
      } else {
        vm.getReg().setIntVal(0);
        setLastError("Invalid network connection handle");
      }
    }
  }

  public final class WrapMessageSmoothed implements Function {
    public void run(TomVM vm) {

      // Find connection
      int index = vm.getIntParam(1);
      if (index > 0 && connections.isIndexValid(index)) {
        NetConL2 connection = connections.getValueAt(index);
        if (connection.hasDataPending()) {
          vm.getReg().setIntVal(connection.isPendingSmoothed() ? 1 : 0);
        } else {
          vm.getReg().setIntVal(0);
        }
      } else {
        vm.getReg().setIntVal(0);
        setLastError("Invalid network connection handle");
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
          NetMessageStream message =
              new NetMessageStream(
                  connections, index, channel, reliable, smoothed, new ByteArrayOutputStream());

          // Store message
          vm.getReg().setIntVal(fileStreams.alloc(message));
        } else {
          vm.getReg().setIntVal(0);
          setLastError("Invalid channel index. Must be 0 - 31.");
        }
      } else {
        vm.getReg().setIntVal(0);
        setLastError("Invalid network connection handle");
      }
    }
  }

  public final class WrapConnectionAddress implements Function {
    public void run(TomVM vm) {

      // Find connection
      int index = vm.getIntParam(1);
      if (index > 0 && connections.isIndexValid(index)) {

        NetConL2 connection = connections.getValueAt(index);
        vm.setRegString(connection.getAddress());
      } else {
        vm.setRegString("");
      }
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
        if (value < 1) {
          value = 1;
        }

        // Update settings
        NetSettingsL1 settings = connection.getL1Settings();
        settings.handshakeTimeout = value;
        connection.setL1Settings(settings);
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
        if (value < 1) {
          value = 1;
        }

        // Update settings
        NetSettingsL1 settings = connection.getL1Settings();
        settings.timeout = value;
        connection.setL1Settings(settings);
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
        if (value < 1) {
          value = 1;
        }

        // Update settings
        NetSettingsL1 settings = connection.getL1Settings();
        settings.keepAlive = value;
        connection.setL1Settings(settings);
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
        if (value < 1) {
          value = 1;
        }
        if (value > 10000) {
          value = 10000;
        }

        // Update settings
        NetSettingsL1 settings = connection.getL1Settings();
        settings.reliableResend = value;
        connection.setL1Settings(settings);
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
        if (value < 1) {
          value = 1;
        }
        if (value > 100) {
          value = 100;
        }

        // Update settings
        NetSettingsL1 settings = connection.getL1Settings();
        settings.dup = value;
        connection.setL1Settings(settings);
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
        if (value < 0) {
          value = 0;
        }
        if (value > 100) {
          value = 100;
        }

        // Update settings
        NetSettingsL2 settings = connection.getSettings();
        settings.smoothingPercentage = value;
        connection.setSettings(settings);
      }
    }
  }
}
