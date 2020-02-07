/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net;
/**
 * Represents a Peer in the smartx network, including both static and dynamic
 * info.
 */
public class Peer {
    /**
     * The network id;
     */
    private final String network;
    /**
     * The network version;
     */
    private final long networkVersion;
    /**
     * The peer id.
     */
    private final String peerId;
    /**
     * The IP address.
     */
    private final String ip;
    /**
     * The listening port.
     */
    private final int port;
    /**
     * The client software id.
     */
    private final String clientId;
    /**
     * The supported capabilities.
     */
    private final String[] capabilities;
    // ===============================
    // Variables below are volatile
    // ===============================
    private long latestBlockNumber;
    private long latency;
    /**
     * Create a new Peer instance.
     *
     * @param network
     * @param peerId
     * @param ip
     * @param port
     * @param clientId
     * @param capabilities
     * @param latestBlockNumber
     */
    public Peer(String network, long networkVersion, String peerId, String ip, int port, String clientId, String[] capabilities, long latestBlockNumber) {
        this.network = network;
        this.networkVersion = networkVersion;
        this.ip = ip;
        this.port = port;
        this.peerId = peerId;
        this.clientId = clientId;
        this.capabilities = capabilities;
        this.latestBlockNumber = latestBlockNumber;
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
     * Returns the listening port number.
     *
     * @return
     */
    public int getPort() {
        return port;
    }
    /**
     * Returns the network.
     *
     * @return
     */
    public String getNetwork() {
        return network;
    }
    /**
     * Returns the networkVersion.
     *
     * @return
     */
    public long getNetworkVersion() {
        return networkVersion;
    }
    /**
     * Returns the client id.
     *
     * @return
     */
    public String getClientId() {
        return clientId;
    }
    /**
     * Returns the peerId.
     *
     * @return
     */
    public String getPeerId() {
        return peerId;
    }
    /**
     * Returns the capabilities.
     */
    public String[] getCapabilities() {
        return capabilities;
    }
    /**
     * Returns the latest block number.
     *
     * @return
     */
    public long getLatestBlockNumber() {
        return latestBlockNumber;
    }
    /**
     * Sets the latest block number.
     *
     * @param number
     */
    public void setLatestBlockNumber(long number) {
        this.latestBlockNumber = number;
    }
    /**
     * Returns peer latency.
     *
     * @return
     */
    public long getLatency() {
        return latency;
    }
    /**
     * Sets peer latency.
     *
     * @param latency
     */
    public void setLatency(long latency) {
        this.latency = latency;
    }
    @Override
    public String toString() {
        return getPeerId() + "@" + ip + ":" + port;
    }
}