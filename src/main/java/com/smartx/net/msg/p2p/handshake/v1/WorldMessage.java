/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net.msg.p2p.handshake.v1;

import com.smartx.crypto.Key;
import com.smartx.net.msg.MessageCode;

public class WorldMessage extends HandshakeMessage {
    /**
     * Create a WORLD message.
     */
    public WorldMessage(String network, long networkVersion, String peerId, String ip, int port, String clientId, long latestBlockNumber, Key coinbase) {
        super(MessageCode.WORLD, null, network, networkVersion, peerId, ip, port, clientId, latestBlockNumber, coinbase);
    }
    /**
     * Parse a WORLD message from byte array.
     *
     * @param body
     */
    public WorldMessage(byte[] uuid, byte[] body) {
        super(MessageCode.WORLD, null, uuid, body);
    }
    @Override
    public String toString() {
        return "WorldMessage [peer=" + peer + "]";
    }
}
