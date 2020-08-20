/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net;

import static com.smartx.net.msg.p2p.NodesMessage.MAX_NODES;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomUtils;
import org.apache.log4j.Logger;

import com.smartx.Kernel;
import com.smartx.config.Network;
import com.smartx.config.SystemProperties;
import com.smartx.net.NodeManager.Node;
import com.smartx.net.msg.Message;
import com.smartx.net.msg.MessageCode;
import com.smartx.net.msg.MessageQueue;
import com.smartx.net.msg.ReasonCode;
import com.smartx.net.msg.consensus.TestSyncResponse;
import com.smartx.net.msg.p2p.*;
import com.smartx.net.msg.p2p.handshake.v2.HelloMessage;
import com.smartx.net.msg.p2p.handshake.v2.InitMessage;
import com.smartx.net.msg.p2p.handshake.v2.WorldMessage;
import com.smartx.net.sync.SyncRequestManager;
import com.smartx.util.Bytes;
import com.smartx.util.SystemUtil;
import com.smartx.util.TimeUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

// import com.smartx.core.sync.SyncManager;
/**
 * Smartx P2P message handler
 */
public class SmartxP2pHandler extends SimpleChannelInboundHandler<Message> {
    private final static Logger logger = Logger.getLogger(SmartxP2pHandler.class);
    private static final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        private final AtomicInteger cnt = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "p2p-" + cnt.getAndIncrement());
        }
    });
    private final Channel channel;
    private final SystemProperties config;
    //    private final Blockchain chain;
    //    private final PendingManager pendingMgr;
    private final ChannelManager channelMgr;
    private final NodeManager nodeMgr;
    private final SyncRequestManager syncReqMgr;
    private final PeerClient client;
    //    private final SyncManager sync;
    //    private final BftManager bft;
    private final MessageQueue msgQueue;
    private AtomicBoolean isHandshakeDone = new AtomicBoolean(false);
    private ScheduledFuture<?> getNodes = null;
    private ScheduledFuture<?> pingPong = null;
    private byte[] secret = Bytes.random(InitMessage.SECRET_LENGTH);
    private long timestamp = TimeUtil.currentTimeMillis();
    // whether to use new handshake for this channel
    private boolean useNewHandShake;
    /**
     * Creates a new P2P handler.
     *
     * @param channel
     */
    public SmartxP2pHandler(Channel channel, Kernel kernel) {
        this.channel = channel;
        this.config = kernel.getConfig();
        //        this.chain = kernel.getBlockchain();
        //        this.pendingMgr = kernel.getPendingManager();
        this.channelMgr = kernel.getChannelManager();
        this.nodeMgr = kernel.getNodeManager();
        this.syncReqMgr = channelMgr.getSyncRequestManager();
        this.client = kernel.getClient();
        //        this.sync = kernel.getSyncManager();
        //        this.bft = kernel.getBftManager();
        this.msgQueue = channel.getMessageQueue();
        this.useNewHandShake = isNewHandShakeEnabled(config.network());
    }
    /**
     * Client prior to v1.3.0 does not accept unknown message type. To make sure
     * they are able to connect to the network; we're partially applying the new
     * handshake protocol.
     *
     * Especially, after receiving an inbound connection, do not send the INIT
     * message immediately; otherwise the connection will be killed by the peer.
     *
     * @return
     */
    protected boolean isNewHandShakeEnabled(String network) {
        if (SystemUtil.isJUnitTest() || !network.equals(Network.MAINNET)) {
            return true;
        }
        // To developer: set 0 to test old handshake, or set 1 to test new handshake.
        return Math.random() < 1;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug(String.format("P2P handler active, remoteIp = {%s}", channel.getRemoteIp()));
        // activate message queue
        // 激活消息队列，消息队列内部有线程轮询ConCurrentList，周期性的写ctx发送消息
        msgQueue.activate(ctx);
        // disconnect if too many connections
        if (channel.isInbound() && channelMgr.size() >= config.netMaxInboundConnections()) {
            msgQueue.disconnect(ReasonCode.TOO_MANY_PEERS);
            return;
        }
        if (useNewHandShake) {
            logger.debug("use new hand shake");
            if (channel.isInbound()) {
                msgQueue.sendMessage(new InitMessage(secret, timestamp));
            } else {
                // in this case, the connection will never be established if the peer
                // doesn't enable new handshake protocol.
            }
        } else {
            logger.debug("use old hand shake");
            if (channel.isOutbound()) {
                //TODO: 获取最新的区块高度 chain.getLatestBlockNumber()
                //TODO:
                Message helloMessage = new com.smartx.net.msg.p2p.handshake.v1.HelloMessage(config.network(), config.networkVersion(), client.getPeerId(), client.getIp(), client.getPort(), config.getClientId(), 0, client.getCoinbase());
                msgQueue.sendMessage(helloMessage);
            }
        }
        super.channelActive(ctx);
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug(String.format("P2P handler inactive, remoteIp = {%s}", channel.getRemoteIp()));
        // deactivate the message queue
        msgQueue.deactivate();
        // stop scheduled workers
        if (getNodes != null) {
            getNodes.cancel(false);
            getNodes = null;
        }
        if (pingPong != null) {
            pingPong.cancel(false);
            pingPong = null;
        }
        super.channelInactive(ctx);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.debug(String.format("Exception in P2P handler, remoteIp = {%s %s}", channel.getRemoteIp(), cause));
        // close connection on exception
        ctx.close();
    }
    @Override
    public void channelRead0(final ChannelHandlerContext ctx, Message msg) throws InterruptedException {
        //logger.debug(String.format("Received message: {%s}", msg.getCode()));
        switch (msg.getCode()) {
            /* p2p */
            case DISCONNECT:
                onDisconnect(ctx, (DisconnectMessage) msg);
                break;
            case HELLO:
                if (!useNewHandShake) onHello((com.smartx.net.msg.p2p.handshake.v1.HelloMessage) msg);
                break;
            case WORLD:
                if (!useNewHandShake) onWorld((com.smartx.net.msg.p2p.handshake.v1.WorldMessage) msg);
                break;
            case PING:
                onPing(ctx);
                break;
            case PONG:
                onPong(ctx);
                break;
            case GET_NODES:
                onGetNodes(ctx);
                break;
            case NODES:
                onNodes(ctx, (NodesMessage) msg);
                break;
            //        case TRANSACTION:
            //            onTransaction((TransactionMessage) msg);
            //            break;
            case HANDSHAKE_INIT:
                if (useNewHandShake) onHandshakeInit((InitMessage) msg);
                break;
            case HANDSHAKE_HELLO:
                if (useNewHandShake) onHandshakeHello((HelloMessage) msg);
                break;
            case HANDSHAKE_WORLD:
                if (useNewHandShake) onHandshakeWorld((WorldMessage) msg);
                break;
            case CORE:
                syncReqMgr.onMessage(ctx, msg);
                channelMgr.process(channel, msg);
                break;
            default:
                ctx.fireChannelRead(msg);
                break;
        }
    }
    //TODO:这个函数只是为了做测试，后期要删掉
    protected void onTestSync(ChannelHandlerContext ctx, Message msg) {
        //如果是收到request，则延迟5-10秒后返回，以模拟延迟的效果
        if (msg.getCode() == MessageCode.TEST_SYNC_REQ) {
            try {
                String uuid = new String(msg.getUUID());
                SocketAddress address = ctx.channel().remoteAddress();
                logger.debug("receive test sync request from address {} uuid {}, {}" + address + " " + uuid + TimeUtil.getCurrentTimeFormat());
                long millis = RandomUtils.nextInt(5000, 5000 * 2);
                Thread.sleep(millis);
                logger.debug("send test respone request to address {} uuid {}, {}" + address + uuid + TimeUtil.getCurrentTimeFormat());
                msgQueue.sendMessage(new TestSyncResponse(msg.getUUID()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }
        //如果收到的是response，则唤醒等待
        this.syncReqMgr.onMessage(ctx, msg);
    }
    protected void onDisconnect(ChannelHandlerContext ctx, DisconnectMessage msg) {
        ReasonCode reason = msg.getReason();
        logger.info(String.format("Received a DISCONNECT message: reason = {%s}, remoteIP = {%s}", reason, channel.getRemoteIp()));
        ctx.close();
    }
    protected void onHello(com.smartx.net.msg.p2p.handshake.v1.HelloMessage msg) {
        Peer peer = msg.getPeer();
        // check peer
        ReasonCode code = checkPeer(peer, false);
        if (code != null) {
            logger.debug("check peer on hello failed disconnected");
            msgQueue.disconnect(code);
            return;
        }
        // check message
        if (!msg.validate(config)) {
            logger.debug("validate message on hello failed invalid handshake");
            msgQueue.disconnect(ReasonCode.INVALID_HANDSHAKE);
            return;
        }
        // check main net ip
        if ((config.network().equals(Network.MAINNET) && !channel.getRemoteIp().equals(msg.getPeer().getIp()))) {
            logger.debug("network is main net by remote ip not equals peer ip");
            msgQueue.disconnect(ReasonCode.INVALID_HANDSHAKE);
            return;
        }
        // reply with a WORLD message
        // TODO: 获取最新块高度 chain.getLatestBlockNumber()
        msgQueue.sendMessage(new com.smartx.net.msg.p2p.handshake.v1.WorldMessage(config.network(), config.networkVersion(), client.getPeerId(), client.getIp(), client.getPort(), config.getClientId(), 0, client.getCoinbase()));
        // handshake done
        onHandshakeDone(peer);
    }
    protected void onWorld(com.smartx.net.msg.p2p.handshake.v1.WorldMessage msg) {
        Peer peer = msg.getPeer();
        // check peer
        ReasonCode code = checkPeer(peer, false);
        if (code != null) {
            logger.debug("check peer on world message failed invalid handshake");
            msgQueue.disconnect(code);
            return;
        }
        // check message
        if ((config.network().equals(Network.MAINNET) && !channel.getRemoteIp().equals(msg.getPeer().getIp())) || !msg.validate(config)) {
            logger.debug("validate message on world message failed invalid handshake");
            msgQueue.disconnect(ReasonCode.INVALID_HANDSHAKE);
            return;
        }
        // handshake done
        onHandshakeDone(peer);
    }
    private long lastPing;
    protected void onPing(final ChannelHandlerContext ctx) {
        logger.debug(String.format("on ping message from {%s} ", ctx.channel().remoteAddress()));
        PongMessage pong = new PongMessage();
        msgQueue.sendMessage(pong);
        lastPing = TimeUtil.currentTimeMillis();
    }
    protected void onPong(final ChannelHandlerContext ctx) {
        logger.debug(String.format("on pong message from {%s} ", ctx.channel().remoteAddress()));
        if (lastPing > 0) {
            long latency = TimeUtil.currentTimeMillis() - lastPing;
            channel.getRemotePeer().setLatency(latency);
        }
    }
    protected void onGetNodes(final ChannelHandlerContext ctx) {
        logger.debug(String.format("on get nodes message from {%s} ", ctx.channel().remoteAddress()));
        List<InetSocketAddress> activeAddresses = new ArrayList<>(channelMgr.getActiveAddresses());
        Collections.shuffle(activeAddresses); // shuffle the list to balance the load on nodes
        NodesMessage nodesMsg = new NodesMessage(activeAddresses.stream().limit(MAX_NODES).map(Node::new).collect(Collectors.toList()));
        msgQueue.sendMessage(nodesMsg);
    }
    protected void onNodes(final ChannelHandlerContext ctx, NodesMessage msg) {
        logger.debug(String.format("on nodes message from {%s} ", ctx.channel().remoteAddress()));
        if (msg.validate()) {
            // 去除跟自己ip地址和端口一样的node，防止回环连到自己
            List<Node> nodeList = new ArrayList<>();
            nodeList.addAll(msg.getNodes());
            for (int i = 0; i < nodeList.size(); i++) {
                Node node = nodeList.get(i);
                if (node.getIp().equals(config.p2pListenIp()) && node.getPort() == config.p2pListenPort()) {
                    logger.debug(String.format("same ip {%s} port {%d} as local p2p listened remove it", node.getIp(), node.getPort()));
                    nodeList.remove(i);
                }
            }
            if (nodeList.size() > 0) {
                nodeMgr.addNodes(nodeList);
            }
        }
    }
    //    protected void onTransaction(TransactionMessage msg) {
    //        pendingMgr.addTransaction(msg.getTransaction());
    //    }
    protected void onHandshakeInit(InitMessage msg) {
        // unexpected
        if (channel.isInbound()) {
            return;
        }
        // check message
        if (!msg.validate()) {
            logger.debug("validate message hand shake init failed invalid handshake");
            this.msgQueue.disconnect(ReasonCode.INVALID_HANDSHAKE);
            return;
        }
        // record the secret
        this.secret = msg.getSecret();
        this.timestamp = msg.getTimestamp();
        // send the HELLO message
        // TODO: 获取最新块高度 chain.getLatestBlockNumber()
        this.msgQueue.sendMessage(new HelloMessage(config.network(), config.networkVersion(), client.getPeerId(), client.getPort(), config.getClientId(), config.getClientCapabilities().toArray(), 0, secret, client.getCoinbase()));
    }
    protected void onHandshakeHello(HelloMessage msg) {
        // unexpected
        if (channel.isOutbound()) {
            return;
        }
        Peer peer = msg.getPeer(channel.getRemoteIp());
        // check peer
        ReasonCode code = checkPeer(peer, true);
        if (code != null) {
            msgQueue.disconnect(code);
            return;
        }
        // check message
        if (!Arrays.equals(secret, msg.getSecret())) {
            logger.debug("check secret on handshake hello failed invalid handshake");
            msgQueue.disconnect(ReasonCode.INVALID_HANDSHAKE);
            return;
        }
        if (!msg.validate(config)) {
            logger.debug("validate message on handshake hello failed invalid handshake");
            msgQueue.disconnect(ReasonCode.INVALID_HANDSHAKE);
            return;
        }
        // send the WORLD message
        // TODO: 获取最新块高度 chain.getLatestBlockNumber()
        this.msgQueue.sendMessage(new WorldMessage(config.network(), config.networkVersion(), client.getPeerId(), client.getPort(), config.getClientId(), config.getClientCapabilities().toArray(), 0, secret, client.getCoinbase()));
        // handshake done
        onHandshakeDone(peer);
    }
    protected void onHandshakeWorld(WorldMessage msg) {
        // unexpected
        if (channel.isInbound()) {
            return;
        }
        Peer peer = msg.getPeer(channel.getRemoteIp());
        // check peer
        ReasonCode code = checkPeer(peer, true);
        if (code != null) {
            msgQueue.disconnect(code);
            return;
        }
        // check message
        if (!Arrays.equals(secret, msg.getSecret()) || !msg.validate(config)) {
            logger.debug("check message on hand shake failed invalid handshake");
            msgQueue.disconnect(ReasonCode.INVALID_HANDSHAKE);
            return;
        }
        // handshake done
        onHandshakeDone(peer);
    }
    // =========================
    // Helper methods below
    // =========================
    /**
     * Check whether the peer is valid to connect.
     */
    private ReasonCode checkPeer(Peer peer, boolean newHandShake) {
        // has to be same network
        if (newHandShake && !config.network().equals(peer.getNetwork())) {
            return ReasonCode.BAD_NETWORK;
        }
        // has to be compatible version
        if (config.networkVersion() != peer.getNetworkVersion()) {
            return ReasonCode.BAD_NETWORK_VERSION;
        }
        // not connected
        if (client.getPeerId().equals(peer.getPeerId()) || channelMgr.isActivePeer(peer.getPeerId())) {
            logger.debug(String.format("client peer id is {%s} remote peer id is {%s} duplicated peer id" ,
                    client.getPeerId(), peer.getPeerId()));
            return ReasonCode.DUPLICATED_PEER_ID;
        }
        // validator can't share IP address
        //        if (chain.getValidators().contains(peer.getPeerId()) // is a validator
        //                && channelMgr.isActiveIP(channel.getRemoteIp()) // already connected
        //                && config.network() == Network.MAINNET) { // on main net
        //            return ReasonCode.VALIDATOR_IP_LIMITED;
        //        }
        return null;
    }
    /**
     * When handshake is done.
     */
    private void onHandshakeDone(Peer peer) {
        if (isHandshakeDone.compareAndSet(false, true)) {
            // register into channel manager
            channelMgr.onChannelActive(channel, peer);
            // notify bft about peer height
            //bft.onMessage(channel, new NewHeightMessage(peer.getLatestBlockNumber() + 1));
            // start peers exchange
            getNodes = exec.scheduleAtFixedRate(() -> msgQueue.sendMessage(new GetNodesMessage()), channel.isInbound() ? 2 : 0, 2, TimeUnit.MINUTES);
            // start ping pong
            pingPong = exec.scheduleAtFixedRate(() -> msgQueue.sendMessage(new PingMessage()), channel.isInbound() ? 1 : 0, 1, TimeUnit.MINUTES);
        } else {
            msgQueue.disconnect(ReasonCode.HANDSHAKE_EXISTS);
        }
    }
}
