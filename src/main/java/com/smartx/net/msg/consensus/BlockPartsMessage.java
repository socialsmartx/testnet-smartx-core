/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net.msg.consensus;

import java.util.ArrayList;
import java.util.List;

import com.smartx.net.msg.Message;
import com.smartx.net.msg.MessageCode;
import com.smartx.util.SimpleDecoder;
import com.smartx.util.SimpleEncoder;

public class BlockPartsMessage extends Message {
    private final long number;
    private final int parts;
    private final List<byte[]> data;
    public BlockPartsMessage(long number, int parts, List<byte[]> data) {
        super(MessageCode.BLOCK_PARTS, null);
        this.number = number;
        this.parts = parts;
        this.data = data;
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeLong(number);
        enc.writeInt(parts);
        enc.writeInt(data.size());
        for (byte[] b : data) {
            enc.writeBytes(b);
        }
        this.body = enc.toBytes();
    }
    public BlockPartsMessage(byte[] uuid, byte[] body) {
        super(MessageCode.BLOCK_PARTS, uuid, null);
        SimpleDecoder dec = new SimpleDecoder(body);
        this.number = dec.readLong();
        this.parts = dec.readInt();
        this.data = new ArrayList<>();
        int n = dec.readInt();
        for (int i = 0; i < n; i++) {
            data.add(dec.readBytes());
        }
        this.body = body;
    }
    public long getNumber() {
        return number;
    }
    public int getParts() {
        return parts;
    }
    public List<byte[]> getData() {
        return data;
    }
    @Override
    public String toString() {
        return "BlockPartsMessage [number=" + number + ", parts=" + parts + "]";
    }
}
