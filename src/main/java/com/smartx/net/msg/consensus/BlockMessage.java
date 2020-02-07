/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net.msg.consensus;

import com.smartx.block.Block;
import com.smartx.net.msg.Message;
import com.smartx.net.msg.MessageCode;

public class BlockMessage extends Message {
    private final Block block = null;
    public BlockMessage(Block block) {
        super(MessageCode.BLOCK, null);
        //this.block = block;
        this.body = block.toBytes(true);
    }
    public BlockMessage(byte[] uuid, byte[] body) {
        super(MessageCode.BLOCK, uuid, null);
        //this.block = Block.fromBytes(body);
        this.body = body;
    }
    public Block getBlock() {
        return block;
    }
    @Override
    public String toString() {
        return "BlockMessage [block=" + block + "]";
    }
}
