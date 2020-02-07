/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net;

import static com.smartx.config.Network.MAINNET;
import static com.smartx.config.Network.TESTNET;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.smartx.Kernel;
import com.smartx.config.Constants;
import com.smartx.config.SystemProperties;
import com.smartx.util.TimeUtil;

public class NodeManager {
    private static final Logger logger = Logger.getLogger(NodeManager.class);
    private static final ThreadFactory factory = new ThreadFactory() {
        private final AtomicInteger cnt = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "node-" + cnt.getAndIncrement());
        }
    };
    private static final long MAX_QUEUE_SIZE = 1024;
    private static final int LRU_CACHE_SIZE = 1024;
    private static final long RECONNECT_WAIT = 60L * 1000L;
    private final Kernel kernel;
    private final SystemProperties config;
    private final ChannelManager channelMgr;
    private final PeerClient client;
    private final Deque<Node> deque = new ConcurrentLinkedDeque<>();
    private final Cache<Node, Long> lastConnect = Caffeine.newBuilder().maximumSize(LRU_CACHE_SIZE).build();
    private final ScheduledExecutorService exec;
    private ScheduledFuture<?> connectFuture;
    private ScheduledFuture<?> fetchFuture;
    private volatile boolean isRunning;
    /**
     * Creates a node manager instance.
     *
     * @param kernel
     */
    public NodeManager(Kernel kernel) {
        this.kernel = kernel;
        this.config = kernel.getConfig();
        this.channelMgr = kernel.getChannelManager();
        this.client = kernel.getClient();
        this.exec = Executors.newSingleThreadScheduledExecutor(factory);
    }
    /**
     * Starts the node manager
     */
    public synchronized void start() {
        if (!isRunning) {
            //添加配置文件中的p2p.SeedNodes
            addNodes(config.p2pSeedNodes());
            // every 0.5 seconds, delayed by 1 seconds (kernel boot up)
            connectFuture = exec.scheduleAtFixedRate(this::doConnect, 1000, 500, TimeUnit.MILLISECONDS);
            // every 100 seconds, delayed by 5 seconds (public IP lookup)
            // 每过100秒，通过解析域名获取新的种子节点
            //fetchFuture = exec.scheduleAtFixedRate(this::doFetch, 5, 100, TimeUnit.SECONDS);
            isRunning = true;
            logger.debug("Node manager started");
        }
    }
    /**
     * Stops this node manager.
     */
    public synchronized void stop() {
        if (isRunning) {
            if (null != connectFuture) connectFuture.cancel(true);
            if (null != fetchFuture) fetchFuture.cancel(false);
            isRunning = false;
            logger.debug("Node manager stopped");
        }
    }
    /**
     * Returns if the node manager is running or not.
     *
     * @return true if running, otherwise false
     */
    public boolean isRunning() {
        return isRunning;
    }
    /**
     * Add a node to the connection queue.
     *
     * @param node
     */
    public void addNode(Node node) {
        deque.addFirst(node);
        while (queueSize() > MAX_QUEUE_SIZE) {
            deque.removeLast();
        }
    }
    /**
     * Add a collection of nodes to the connection queue.
     *
     * @param nodes
     */
    public void addNodes(Collection<Node> nodes) {
        for (Node node : nodes) {
            addNode(node);
        }
    }
    /**
     * Get the connection queue size.
     *
     * @return
     */
    public int queueSize() {
        return deque.size();
    }
    /**
     * Get seed nodes from DNS records.
     *
     * @param network
     * @return
     */
    public Set<Node> getSeedNodes(String network) {
        Set<Node> nodes = new HashSet<>();
        List<String> names;
        switch (network) {
            case MAINNET:
                names = kernel.getConfig().netDnsSeedsMainNet();
                break;
            case TESTNET:
                names = kernel.getConfig().netDnsSeedsTestNet();
                break;
            default:
                return nodes;
        }
        names.parallelStream().filter(Objects::nonNull).map(String::trim).map(name -> {
            try {
                return InetAddress.getAllByName(name);
            } catch (UnknownHostException e) {
                logger.warn(String.format("Failed to get seed nodes from {%s}", name));
                return new InetAddress[0];
            }
        }).flatMap(Stream::of).forEach(address -> nodes.add(new Node(address.getHostAddress(), Constants.DEFAULT_P2P_PORT)));
        return nodes;
    }
    /**
     * Connect to a node in the queue.
     */
    protected void doConnect() {
        Set<InetSocketAddress> activeAddresses = channelMgr.getActiveAddresses();
        Node node;
        while ((node = deque.pollFirst()) != null && channelMgr.size() < config.netMaxOutboundConnections()) {
            Long lastTouch = lastConnect.getIfPresent(node);
            long now = TimeUtil.currentTimeMillis();
            if (!client.getNode().equals(node) // self
                    && !(Objects.equals(node.getIp(), client.getIp()) && node.getPort() == client.getPort()) // self
                    && !activeAddresses.contains(node.toAddress()) // connected
                    && (lastTouch == null || lastTouch + RECONNECT_WAIT < now)) {
                SmartxChannelInitializer ci = new SmartxChannelInitializer(kernel, node);
                client.connect(node, ci);
                lastConnect.put(node, now);
                break;
            }
        }
    }
    /**
     * Fetches seed nodes from DNS records or configuration.
     */
    protected void doFetch() {
        addNodes(getSeedNodes(config.network()));
    }
    /**
     * Represents a node in the smartx network.
     */
    public static class Node {
        private final InetSocketAddress address;
        /**
         * Construct a node with the given socket address.
         *
         * @param address
         */
        public Node(InetSocketAddress address) {
            this.address = address;
        }
        /**
         * Construct a node with the given IP address and port.
         *
         * @param ip
         * @param port
         */
        public Node(InetAddress ip, int port) {
            this(new InetSocketAddress(ip, port));
        }
        /**
         * Construct a node with the given IP address and port.
         *
         * @param ip
         *            IP address, or hostname (not encouraged to use)
         * @param port
         *            port number
         */
        public Node(String ip, int port) {
            this(new InetSocketAddress(ip, port));
        }
        /**
         * Returns the IP address.
         *
         * @return
         */
        public String getIp() {
            return address.getAddress().getHostAddress();
        }
        /**
         * Returns the port number
         *
         * @return
         */
        public int getPort() {
            return address.getPort();
        }
        /**
         * Converts into a socket address.
         *
         * @return
         */
        public InetSocketAddress toAddress() {
            return address;
        }
        @Override
        public int hashCode() {
            return address.hashCode();
        }
        @Override
        public boolean equals(Object o) {
            return o instanceof Node && address.equals(((Node) o).toAddress());
        }
        @Override
        public String toString() {
            return getIp() + ":" + getPort();
        }
    }
}
