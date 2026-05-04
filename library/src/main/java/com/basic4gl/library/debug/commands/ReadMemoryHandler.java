package com.basic4gl.library.debug.commands;

import com.basic4gl.debug.protocol.callbacks.ReadMemoryCallback;
import com.basic4gl.debug.protocol.commands.ReadMemoryCommand;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.Value;
import com.google.gson.Gson;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;
import javax.websocket.Session;

public class ReadMemoryHandler {
    private static final int BYTES_PER_WORD = 4;

    private final TomVM vm;
    private final Gson gson;

    public ReadMemoryHandler(TomVM vm, Gson gson) {
        this.vm = vm;
        this.gson = gson;
    }

    public void handle(ReadMemoryCommand command, int requestId, Session session) {
        String memoryReference = null;
        Integer offset = 0;
        int count = 0;
        if (command != null && command.arguments != null) {
            memoryReference = command.arguments.memoryReference;
            offset = command.arguments.offset != null ? command.arguments.offset : 0;
            count = Math.max(0, command.arguments.count);
        }

        int heapBase = vm.getData().getPermanent();
        int heapEnd = vm.getData().size();

        int baseAddress = parseAddress(memoryReference, heapBase);
        int clampedBase = Math.max(heapBase, Math.min(baseAddress, heapEnd));
        int safeOffsetBytes = Math.max(0, offset != null ? offset : 0);
        int startAddress = clampedBase + (safeOffsetBytes / BYTES_PER_WORD);
        startAddress = Math.max(heapBase, Math.min(startAddress, heapEnd));

        int availableWords = Math.max(0, heapEnd - startAddress);
        int availableBytes = availableWords * BYTES_PER_WORD;
        int readableBytes = Math.min(count, availableBytes);

        ByteBuffer buffer = ByteBuffer.allocate(readableBytes).order(ByteOrder.LITTLE_ENDIAN);
        int readableWords = readableBytes / BYTES_PER_WORD;
        for (int i = 0; i < readableWords; i++) {
            int dataIndex = startAddress + i;
            Value value = vm.getData().data().get(dataIndex);
            buffer.putInt(value.getIntVal());
        }

        int remainderBytes = readableBytes % BYTES_PER_WORD;
        if (remainderBytes > 0) {
            int dataIndex = startAddress + readableWords;
            if (dataIndex < heapEnd) {
                ByteBuffer wordBuffer = ByteBuffer.allocate(BYTES_PER_WORD).order(ByteOrder.LITTLE_ENDIAN);
                wordBuffer.putInt(vm.getData().data().get(dataIndex).getIntVal());
                byte[] wordBytes = wordBuffer.array();
                buffer.put(wordBytes, 0, remainderBytes);
            }
        }

        ReadMemoryCallback callback = new ReadMemoryCallback();
        callback.setRequestId(requestId);
        callback.setAddress(Integer.toString(startAddress));
        callback.setData(Base64.getEncoder().encodeToString(buffer.array()));

        int unreadableBytes = Math.max(0, count - readableBytes);
        if (unreadableBytes > 0) {
            callback.setUnreadableBytes(unreadableBytes);
        }

        message(session, gson.toJson(callback));
    }

    private int parseAddress(String memoryReference, int defaultAddress) {
        if (memoryReference == null || memoryReference.trim().isEmpty()) {
            return defaultAddress;
        }

        String normalized = memoryReference.trim();
        if ("heap".equalsIgnoreCase(normalized)) {
            return defaultAddress;
        }

        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException ex) {
            return defaultAddress;
        }
    }

    private void message(Session session, String json) {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(json);
            } catch (Exception e) {
                throw new RuntimeException("Failed to send readMemory callback", e);
            }
        }
    }
}

