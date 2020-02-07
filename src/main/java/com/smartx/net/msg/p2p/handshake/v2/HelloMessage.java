/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net.msg.p2p.handshake.v2;

import java.util.Arrays;

import com.smartx.crypto.Hex;
import com.smartx.crypto.Key;
import com.smartx.net.msg.MessageCode;

public class HelloMessage extends HandshakeMessage {
    public HelloMessage(String network, long networkVersion, String peerId, int port, String clientId, String[] capabilities, long latestBlockNumber, byte[] secret, Key coinbase) {
        super(MessageCode.HANDSHAKE_HELLO, WorldMessage.class, network, networkVersion, peerId, port, clientId, capabilities, latestBlockNumber, secret, coinbase);
    }
    public HelloMessage(byte[] uuid, byte[] encoded) {
        super(MessageCode.HANDSHAKE_HELLO, WorldMessage.class, uuid, encoded);
    }
    @Override
    public String toString() {
        return "HelloMessage{" + "peer=" + network + ", networkVersion=" + networkVersion + ", peerId='" + peerId + '\'' + ", port=" + port + ", clientId='" + clientId + '\'' + ", capabilities=" + Arrays.toString(capabilities) + ", latestBlockNumber=" + latestBlockNumber + ", secret=" + Hex.encode(secret) + ", timestamp=" + timestamp + '}';
    }
}
