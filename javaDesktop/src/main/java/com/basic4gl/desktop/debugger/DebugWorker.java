//package com.basic4gl.desktop.debugger;
//
//import javax.swing.*;
//
//public class DebugWorker extends SwingWorker<Object, String> {
//
//
//    @Override
//    protected Object doInBackground() throws Exception {
//        DebugClientAdapter adapter = null;
//        try {
//            adapter = new DebugClientAdapter();
//            adapter.connect();
//
//        } finally {
//            adapter.stop();
//        }
//
//        return null;
//    }
//
//    @Override
//    protected void done() {
//        super.done();
//    }
//}
