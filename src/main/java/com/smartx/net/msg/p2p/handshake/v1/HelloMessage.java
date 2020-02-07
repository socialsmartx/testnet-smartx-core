/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net.msg.p2p.handshake.v1;

import com.smartx.crypto.Key;
import com.smartx.net.msg.MessageCode;

public class HelloMessage extends HandshakeMessage {
    /**
     * Create a HELLO message.
     */
    public HelloMessage(String network, long networkVersion, String peerId, String ip, int port, String clientId, long latestBlockNumber, Key coinbase) {
        super(MessageCode.HELLO, WorldMessage.class, network, networkVersion, peerId, ip, port, clientId, latestBlockNumber, coinbase);
    }
    /**
     * Parse a HELLO message from byte array.
     *
     * @param body
     */
    public HelloMessage(byte[] uuid, byte[] body) {
        super(MessageCode.HELLO, WorldMessage.class, uuid, body);
    }
    @Override
    public String toString() {
        return "WorldMessage [peer=" + peer + "]";
    }
}
