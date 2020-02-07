/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net.msg;

import org.apache.log4j.Logger;

import com.smartx.crypto.Hex;
import com.smartx.net.msg.consensus.*;
import com.smartx.net.msg.p2p.*;
import com.smartx.net.msg.p2p.handshake.v2.HelloMessage;
import com.smartx.net.msg.p2p.handshake.v2.InitMessage;
import com.smartx.net.msg.p2p.handshake.v2.WorldMessage;
import com.smartx.util.Bytes;
import com.smartx.util.exception.UnreachableException;

public class MessageFactory {
    private static final Logger logger = Logger.getLogger(MessageFactory.class);
    /**
     * Decode a raw message.
     *
     * @param code
     *            The message code
     * @param body
     *            The message body
     * @return The decoded message, or NULL if the message type is not unknown
     * @throws MessageException
     *             when the encoding is illegal
     */
    public Message create(byte code, byte[] uuid, byte[] body) throws MessageException {
        MessageCode c = MessageCode.of(code);
        if (c == null) {
            logger.debug("Invalid message code: {}" + Hex.encode0x(Bytes.of(code)));
            return null;
        }
        try {
            switch (c) {
                case DISCONNECT:
                    return new DisconnectMessage(uuid, body);
                case HELLO:
                    return new com.smartx.net.msg.p2p.handshake.v1.HelloMessage(uuid, body);
                case WORLD:
                    return new com.smartx.net.msg.p2p.handshake.v1.WorldMessage(uuid, body);
                case PING:
                    return new PingMessage(uuid, body);
                case PONG:
                    return new PongMessage(uuid, body);
                case GET_NODES:
                    return new GetNodesMessage(uuid, body);
                case NODES:
                    return new NodesMessage(uuid, body);
                //            case TRANSACTION:
                //                return new TransactionMessage(body);
                case HANDSHAKE_INIT:
                    return new InitMessage(uuid, body);
                case HANDSHAKE_HELLO:
                    return new HelloMessage(uuid, body);
                case HANDSHAKE_WORLD:
                    return new WorldMessage(uuid, body);
                case GET_BLOCK:
                    return new GetBlockMessage(uuid, body);
                case BLOCK:
                    return new BlockMessage(uuid, body);
                case GET_BLOCK_HEADER:
                    return new GetBlockHeaderMessage(uuid, body);
                case BLOCK_HEADER:
                    return new BlockHeaderMessage(uuid, body);
                case GET_BLOCK_PARTS:
                    return new GetBlockPartsMessage(uuid, body);
                case BLOCK_PARTS:
                    return new BlockPartsMessage(uuid, body);
                case BFT_NEW_HEIGHT:
                    return new NewHeightMessage(uuid, body);
                case BFT_NEW_VIEW:
                    return new NewViewMessage(uuid, body);
                case BFT_PROPOSAL:
                    return new ProposalMessage(uuid, body);
                case BFT_VOTE:
                    return new VoteMessage(uuid, body);
                case CORE:
                    return new SmartXMessage(uuid, body);
                case TEST_SYNC_REQ:
                    return new TestSyncRequest(uuid, body);
                case TEST_SYNC_RSP:
                    return new TestSyncResponse(uuid, body);
                default:
                    throw new UnreachableException();
            }
        } catch (Exception e) {
            throw new MessageException("Failed to decode message", e);
        }
    }
}
