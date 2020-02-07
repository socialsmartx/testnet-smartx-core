/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net.msg.consensus;

import com.smartx.block.BlockHeader;
import com.smartx.net.msg.Message;
import com.smartx.net.msg.MessageCode;

public class BlockHeaderMessage extends Message {
    private final BlockHeader header;
    public BlockHeaderMessage(BlockHeader header) {
        super(MessageCode.BLOCK_HEADER, null);
        this.header = header;
        this.body = header.toBytes();
    }
    public BlockHeaderMessage(byte[] uuid, byte[] body) {
        super(MessageCode.BLOCK_HEADER, uuid, null);
        this.header = BlockHeader.fromBytes(body);
        this.body = body;
    }
    public BlockHeader getHeader() {
        return header;
    }
    @Override
    public String toString() {
        return "BlockHeaderMessage [header=" + header + "]";
    }
}
