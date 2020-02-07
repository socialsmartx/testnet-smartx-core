/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net.msg.p2p.handshake.v1;

import static com.smartx.config.Network.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.smartx.config.SystemProperties;
import com.smartx.crypto.Hex;
import com.smartx.crypto.Key;
import com.smartx.net.Peer;
import com.smartx.net.msg.Message;
import com.smartx.net.msg.MessageCode;
import com.smartx.util.SimpleDecoder;
import com.smartx.util.SimpleEncoder;
import com.smartx.util.TimeUtil;

public class HandshakeMessage extends Message {
    protected final Peer peer;
    protected final long timestamp;
    protected final Key.Signature signature;
    private final static Logger logger = Logger.getLogger(HandshakeMessage.class);
    /**
     * Create a message instance.
     *
     * @param code
     * @param responseMessageClass
     */
    public HandshakeMessage(MessageCode code, Class<?> responseMessageClass, String network, long networkVersion, String peerId, String ip, int port, String clientId, long latestBlockNumber, Key coinbase) {
        super(code, responseMessageClass);
        this.peer = new Peer(network, networkVersion, peerId, ip, port, clientId, mandatoryCapabilities(network), latestBlockNumber);
        this.timestamp = TimeUtil.currentTimeMillis();
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeBytes(encodePeer(peer));
        enc.writeLong(timestamp);
        this.signature = coinbase.sign(enc.toBytes());
        enc.writeBytes(signature.toBytes());
        this.body = enc.toBytes();
    }
    public HandshakeMessage(MessageCode code, Class<?> responseMessageClass, byte[] uuid, byte[] body) {
        super(code, uuid, responseMessageClass);
        SimpleDecoder dec = new SimpleDecoder(body);
        this.peer = decodePeer(dec.readBytes());
        this.timestamp = dec.readLong();
        this.signature = Key.Signature.fromBytes(dec.readBytes());
        this.body = body;
    }
    /**
     * Validates this message.
     *
     * <p>
     * NOTE: only data format and signature is checked here.
     * </p>
     *
     * @param config
     * @return true if success, otherwise false
     */
    public boolean validate(SystemProperties config) {
        if (peer != null && validatePeer(peer) && Math.abs(TimeUtil.currentTimeMillis() - timestamp) <= config.netHandshakeExpiry() && signature != null && peer.getPeerId().equals(Hex.encode(signature.getAddress()))) {
            SimpleEncoder enc = new SimpleEncoder();
            enc.writeBytes(encodePeer(peer));
            enc.writeLong(timestamp);
            boolean result = Key.verify(enc.toBytes(), signature);
            if (!result) {
                logger.debug("validate message failed");
            }
            return result;
        } else {
            logger.debug("validate message failed");
            return false;
        }
    }
    public Peer getPeer() {
        return peer;
    }
    public long getTimestamp() {
        return timestamp;
    }
    private static String[] mandatoryCapabilities(String network) {
        switch (network) {
            case MAINNET:
                return new String[]{"SAT"};
            case TESTNET:
            case DEVNET:
            default:
                return new String[]{"SAT_TESTNET"};
        }
    }
    private static boolean validatePeer(Peer peer) {
        return peer.getIp() != null && peer.getIp().length() <= 128 && peer.getPort() >= 0 && peer.getNetworkVersion() >= 0 && peer.getClientId() != null && peer.getClientId().length() < 128 && peer.getPeerId() != null && peer.getPeerId().length() == 40 && peer.getLatestBlockNumber() >= 0 && peer.getCapabilities() != null && peer.getCapabilities().length <= 128;
    }
    private static byte[] encodePeer(Peer peer) {
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeString(peer.getIp());
        enc.writeInt(peer.getPort());
        enc.writeLong(peer.getNetworkVersion());
        enc.writeString(peer.getClientId());
        enc.writeString(peer.getPeerId());
        enc.writeLong(peer.getLatestBlockNumber());
        // encode capabilities
        enc.writeInt(peer.getCapabilities().length);
        for (String capability : peer.getCapabilities()) {
            enc.writeString(capability);
        }
        return enc.toBytes();
    }
    private static Peer decodePeer(byte[] bytes) {
        SimpleDecoder dec = new SimpleDecoder(bytes);
        String ip = dec.readString();
        int port = dec.readInt();
        long p2pVersion = dec.readLong();
        String clientId = dec.readString();
        String peerId = dec.readString();
        long latestBlockNumber = dec.readLong();
        // decode capabilities
        List<String> capabilities = new ArrayList<>();
        for (int i = 0, size = dec.readInt(); i < size; i++) {
            capabilities.add(dec.readString());
        }
        return new Peer(null, p2pVersion, peerId, ip, port, clientId, capabilities.toArray(new String[0]), latestBlockNumber);
    }
}
