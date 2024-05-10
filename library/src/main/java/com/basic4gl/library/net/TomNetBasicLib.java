package com.basic4gl.library.net;
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








    ;



}*/