/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net.msg.p2p;

import java.util.ArrayList;
import java.util.List;

import com.smartx.net.NodeManager.Node;
import com.smartx.net.msg.Message;
import com.smartx.net.msg.MessageCode;
import com.smartx.util.SimpleDecoder;
import com.smartx.util.SimpleEncoder;

public class NodesMessage extends Message {
    public static final int MAX_NODES = 256;
    private final List<Node> nodes;
    /**
     * Create a NODES message.
     *
     * @param nodes
     */
    public NodesMessage(List<Node> nodes) {
        super(MessageCode.NODES, null);
        this.nodes = nodes;
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeInt(nodes.size());
        for (Node n : nodes) {
            enc.writeString(n.getIp());
            enc.writeInt(n.getPort());
        }
        this.body = enc.toBytes();
    }
    /**
     * Parse a NODES message from byte array.
     *
     * @param body
     */
    public NodesMessage(byte[] uuid, byte[] body) {
        super(MessageCode.NODES, null);
        this.nodes = new ArrayList<>();
        SimpleDecoder dec = new SimpleDecoder(body);
        for (int i = 0, size = dec.readInt(); i < size; i++) {
            String host = dec.readString();
            int port = dec.readInt();
            nodes.add(new Node(host, port));
        }
        this.body = body;
    }
    public boolean validate() {
        return nodes != null && nodes.size() <= MAX_NODES;
    }
    public List<Node> getNodes() {
        return nodes;
    }
    @Override
    public String toString() {
        return "NodesMessage [# nodes =" + nodes.size() + "]";
    }
}
