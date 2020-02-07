/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net.msg.p2p;

import com.smartx.net.msg.Message;
import com.smartx.net.msg.MessageCode;
import com.smartx.net.msg.ReasonCode;
import com.smartx.util.SimpleDecoder;
import com.smartx.util.SimpleEncoder;

public class DisconnectMessage extends Message {
    private final ReasonCode reason;
    /**
     * Create a DISCONNECT message.
     *
     * @param reason
     */
    public DisconnectMessage(ReasonCode reason) {
        super(MessageCode.DISCONNECT, null);
        this.reason = reason;
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeByte(reason.toByte());
        this.body = enc.toBytes();
    }
    /**
     * Parse a DISCONNECT message from byte array.
     *
     * @param body
     */
    public DisconnectMessage(byte[] uuid, byte[] body) {
        super(MessageCode.DISCONNECT, uuid, null);
        SimpleDecoder dec = new SimpleDecoder(body);
        this.reason = ReasonCode.of(dec.readByte());
        this.body = body;
    }
    public ReasonCode getReason() {
        return reason;
    }
    @Override
    public String toString() {
        return "DisconnectMessage [reason=" + reason + "]";
    }
}
