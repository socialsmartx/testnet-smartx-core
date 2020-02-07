/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net.msg.p2p.handshake.v2;

import com.smartx.crypto.Hex;
import com.smartx.net.msg.Message;
import com.smartx.net.msg.MessageCode;
import com.smartx.util.SimpleDecoder;
import com.smartx.util.SimpleEncoder;

public class InitMessage extends Message {
    public static final int SECRET_LENGTH = 32;
    private final byte[] secret;
    private final long timestamp;
    public InitMessage(byte[] secret, long timestamp) {
        super(MessageCode.HANDSHAKE_INIT, null);
        this.secret = secret;
        this.timestamp = timestamp;
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeBytes(secret);
        enc.writeLong(timestamp);
        this.body = enc.toBytes();
    }
    public InitMessage(byte[] uuid, byte[] body) {
        super(MessageCode.HANDSHAKE_INIT, uuid, null);
        SimpleDecoder dec = new SimpleDecoder(body);
        this.secret = dec.readBytes();
        this.timestamp = dec.readLong();
        this.body = body;
    }
    public boolean validate() {
        return secret != null && secret.length == SECRET_LENGTH && timestamp > 0;
    }
    public byte[] getSecret() {
        return secret;
    }
    public long getTimestamp() {
        return timestamp;
    }
    @Override
    public String toString() {
        return "InitMessage{" + "secret=" + Hex.encode(secret) + ", timestamp=" + timestamp + '}';
    }
}
