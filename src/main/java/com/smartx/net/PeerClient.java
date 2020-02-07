/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.smartx.config.Constants;
import com.smartx.config.SystemProperties;
import com.smartx.crypto.Key;
import com.smartx.net.NodeManager.Node;
import com.smartx.util.SystemUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Represents a client which connects to the Smartx network.
 */
public class PeerClient {
    private static final Logger logger = Logger.getLogger(PeerClient.class);
    private static final ThreadFactory factory = new ThreadFactory() {
        final AtomicInteger cnt = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "client-" + cnt.getAndIncrement());
        }
    };
    private final int port;
    private final Key coinbase;
    private final EventLoopGroup workerGroup;
    private ScheduledFuture<?> ipRefreshFuture = null;
    private String ip;
    /**
     * Create a new PeerClient instance.
     *
     * @param config
     * @param coinbase
     */
    public PeerClient(SystemProperties config, Key coinbase) {
        this(config.p2pDeclaredIp().orElse(SystemUtil.getIp()), config.p2pListenPort(), coinbase);
    }
    /**
     * Create a new PeerClient with the given public IP address and coinbase.
     *
     * @param ip
     * @param port
     * @param coinbase
     */
    public PeerClient(String ip, int port, Key coinbase) {
        logger.info(String.format("Peer client info: peerId = {%s}, ip = {%s}, port = {%d}", coinbase.toAddressString(), ip, port));
        this.ip = ip;
        this.port = port;
        this.coinbase = coinbase;
        this.workerGroup = new NioEventLoopGroup(4, factory);
    }
    /**
     * Returns this node.
     *
     * @return
     */
    public Node getNode() {
        return new Node(ip, port);
    }
    /**
     * Returns the listening IP address.
     *
     * @return
     */
    public String getIp() {
        return ip;
    }
    /**
     * Returns the listening IP port.
     *
     * @return
     */
    public int getPort() {
        return port;
    }
    /**
     * Returns the peerId of this client.
     *
     * @return
     */
    public String getPeerId() {
        return coinbase.toAddressString();
    }
    /**
     * Returns the coinbase.
     *
     * @return
     */
    public Key getCoinbase() {
        return coinbase;
    }
    /**
     * Connects to a remote peer asynchronously.
     *
     * @param remoteNode
     * @return
     */
    public ChannelFuture connect(Node remoteNode, SmartxChannelInitializer ci) {
        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Constants.DEFAULT_CONNECT_TIMEOUT);
        b.remoteAddress(remoteNode.toAddress());
        b.handler(ci);
        return b.connect();
    }
    /**
     * Closes this client.
     */
    public void close() {
        logger.info("Shutting down PeerClient");
        workerGroup.shutdownGracefully();
        // workerGroup.terminationFuture().sync();
        if (ipRefreshFuture != null) {
            ipRefreshFuture.cancel(true);
        }
    }
}
