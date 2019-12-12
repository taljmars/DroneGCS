package com.dronegcs.console_plugin.simulator;

import sun.nio.cs.US_ASCII;

public class MessageBuffer {
    private byte[] buffer;
    private long timestamp;
    private int ptr = 0;

    public MessageBuffer(long timestamp) {
        this.timestamp = timestamp;
        buffer = new byte[64000];
    }

    public void appendBuffer(String src, int len) {
        if (len == 0) return;
        System.arraycopy(src.getBytes(), 0, buffer, ptr, len);
        ptr += len;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getLength() {
        return ptr;
    }
}
