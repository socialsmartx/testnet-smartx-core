package com.smartx.net.msg.consensus;

import com.smartx.net.msg.Message;
import com.smartx.net.msg.MessageCode;
import com.smartx.util.SimpleDecoder;
import com.smartx.util.SimpleEncoder;
import com.smartx.util.TimeUtil;

public class TestSyncRequest extends Message {
    private final long timestamp;
    /**
     Create a TestSyncRequest message.
     */
    public TestSyncRequest() {
        super(MessageCode.TEST_SYNC_REQ, TestSyncRequest.class);
        this.timestamp = TimeUtil.currentTimeMillis();
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeLong(timestamp);
        this.body = enc.toBytes();
    }
    /**
     Parse a TestSyncRequest message from byte array.

     @param body
     */
    public TestSyncRequest(byte[] uuid, byte[] body) {
        super(MessageCode.TEST_SYNC_REQ, uuid, TestSyncRequest.class);
        SimpleDecoder dec = new SimpleDecoder(body);
        this.timestamp = dec.readLong();
        this.body = body;
    }
    @Override
    public String toString() {
        return "TestSyncRequest [timestamp=" + timestamp + "]";
    }
}
