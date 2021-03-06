/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net.msg.consensus;

import com.smartx.net.msg.Message;
import com.smartx.net.msg.MessageCode;
import com.smartx.util.SimpleDecoder;
import com.smartx.util.SimpleEncoder;

public class GetBlockHeaderMessage extends Message {
    private final long number;
    public GetBlockHeaderMessage(long number) {
        super(MessageCode.GET_BLOCK_HEADER, BlockHeaderMessage.class);
        this.number = number;
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeLong(number);
        this.body = enc.toBytes();
    }
    public GetBlockHeaderMessage(byte[] uuid, byte[] body) {
        super(MessageCode.GET_BLOCK_HEADER, uuid, BlockHeaderMessage.class);
        SimpleDecoder dec = new SimpleDecoder(body);
        this.number = dec.readLong();
        this.body = body;
    }
    public long getNumber() {
        return number;
    }
    @Override
    public String toString() {
        return "GetBlockHeaderMessage [number=" + number + "]";
    }
}
