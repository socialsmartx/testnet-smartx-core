package com.smartx.net.msg.consensus;

import com.smartx.net.msg.Message;
import com.smartx.net.msg.MessageCode;
import com.smartx.net.msg.p2p.PongMessage;
import com.smartx.util.SimpleDecoder;
import com.smartx.util.SimpleEncoder;
import com.smartx.util.TimeUtil;

public class TestSyncResponse extends Message {
    private final long timestamp;
    /**
     Create a TestSyncResponse message.
     */
    public TestSyncResponse() {
        super(MessageCode.TEST_SYNC_RSP, TestSyncResponse.class);
        this.timestamp = TimeUtil.currentTimeMillis();
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeLong(timestamp);
        this.body = enc.toBytes();
    }
    public TestSyncResponse(byte[] uuid) {
        super(MessageCode.TEST_SYNC_RSP, uuid, TestSyncResponse.class);
        this.timestamp = TimeUtil.currentTimeMillis();
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeLong(timestamp);
        this.body = enc.toBytes();
    }
    /**
     Parse a TestSyncResponse message from byte array.

     @param body
     */
    public TestSyncResponse(byte[] uuid, byte[] body) {
        super(MessageCode.TEST_SYNC_RSP, uuid, TestSyncResponse.class);
        SimpleDecoder dec = new SimpleDecoder(body);
        this.timestamp = dec.readLong();
        this.body = body;
    }
    @Override
    public String toString() {
        return "TestSyncResponse [timestamp=" + timestamp + "]";
    }
}
