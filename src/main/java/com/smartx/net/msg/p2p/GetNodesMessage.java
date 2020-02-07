/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net.msg.p2p;

import com.smartx.net.msg.Message;
import com.smartx.net.msg.MessageCode;
import com.smartx.util.SimpleEncoder;

// NOTE: GetNodesMessage is encoded into a single empty frame.
public class GetNodesMessage extends Message {
    /**
     * Create a GET_NODES message.
     *
     */
    public GetNodesMessage() {
        super(MessageCode.GET_NODES, NodesMessage.class);
        SimpleEncoder enc = new SimpleEncoder();
        this.body = enc.toBytes();
    }
    /**
     * Parse a GET_NODES message from byte array.
     *
     * @param body
     */
    public GetNodesMessage(byte[] uuid, byte[] body) {
        super(MessageCode.GET_NODES, uuid, NodesMessage.class);
        this.body = body;
    }
    @Override
    public String toString() {
        return "GetNodesMessage";
    }
}
