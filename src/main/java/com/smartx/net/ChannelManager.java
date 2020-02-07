/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.smartx.Kernel;
import com.smartx.net.filter.SmartxIpFilter;
import com.smartx.net.msg.Message;
import com.smartx.net.sync.SyncRequestManager;

/**
 * Channel Manager.
 */
public class ChannelManager {
    private static final Logger logger = Logger.getLogger(ChannelManager.class);
    /**
     * All channels, indexed by the <code>remoteAddress (ip + port)</code>, not
     * necessarily the listening address.
     */
    protected ConcurrentHashMap<InetSocketAddress, Channel> channels = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<String, Channel> activeChannels = new ConcurrentHashMap<>();
    protected final SmartxIpFilter ipFilter;
    SyncRequestManager syncReqMgr = null;
    /**
     * Returns the node manager.
     *
     * @return
     */
    public SyncRequestManager getSyncRequestManager() {
        return syncReqMgr;
    }
    public ChannelManager(Kernel kernel) {
        ipFilter = new SmartxIpFilter.Loader().load(new File(kernel.getConfig().configDir(), SmartxIpFilter.CONFIG_FILE).toPath());
        // SyncRequestManager要在p2p模块启动前初始化
        syncReqMgr = new SyncRequestManager(kernel);
    }
    /**
     * Returns the IP filter if enabled.
     *
     * @return
     */
    public SmartxIpFilter getIpFilter() {
        return ipFilter;
    }
    /**
     * Returns whether a connection from the given address is acceptable or not.
     *
     * @param address
     * @return
     */
    public boolean isAcceptable(InetSocketAddress address) {
        return ipFilter == null || ipFilter.isAcceptable(address);
    }
    /**
     * Returns whether a socket address is connected.
     *
     * @param address
     * @return
     */
    public boolean isConnected(InetSocketAddress address) {
        return channels.containsKey(address);
    }
    /**
     * Returns whether the specified IP is connected.
     *
     * @param ip
     * @return
     */
    public boolean isActiveIP(String ip) {
        for (Channel c : activeChannels.values()) {
            if (c.getRemoteIp().equals(ip)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns whether the specified peer is connected.
     *
     * @param peerId
     * @return
     */
    public boolean isActivePeer(String peerId) {
        return activeChannels.containsKey(peerId);
    }
    /**
     * Returns the number of channels.
     *
     * @return
     */
    public int size() {
        return channels.size();
    }
    /**
     * Adds a new channel to this manager.
     *
     * @param ch
     *            channel instance
     */
    public void add(Channel ch) {
        logger.debug(String.format("Channel added: remoteAddress = {%s}:{%d}", ch.getRemoteIp(), ch.getRemotePort()));
        channels.put(ch.getRemoteAddress(), ch);
    }
    /**
     * Removes a disconnected channel from this manager.
     *
     * @param ch
     *            channel instance
     */
    public void remove(Channel ch) {
        logger.debug(String.format("Channel removed: remoteAddress = {%s}:{%d}", ch.getRemoteIp(), ch.getRemotePort()));
        channels.remove(ch.getRemoteAddress());
        if (ch.isActive()) {
            activeChannels.remove(ch.getRemotePeer().getPeerId());
            ch.setInactive();
        }
    }
    /**
     * Closes all blacklisted channels.
     */
    public void closeBlacklistedChannels() {
        for (Map.Entry<InetSocketAddress, Channel> entry : channels.entrySet()) {
            Channel channel = entry.getValue();
            if (!isAcceptable(channel.getRemoteAddress())) {
                remove(channel);
                channel.close();
            }
        }
    }
    /**
     * When a channel becomes active.
     *
     * @param channel
     * @param peer
     */
    public void onChannelActive(Channel channel, Peer peer) {
        channel.setActive(peer);
        activeChannels.put(peer.getPeerId(), channel);
    }
    /**
     * Returns a copy of the active peers.
     *
     * @return
     */
    public List<Peer> getActivePeers() {
        List<Peer> list = new ArrayList<>();
        for (Channel c : activeChannels.values()) {
            list.add(c.getRemotePeer());
        }
        return list;
    }
    /**
     * Returns the listening IP addresses of active peers.
     *
     * @return
     */
    public Set<InetSocketAddress> getActiveAddresses() {
        Set<InetSocketAddress> set = new HashSet<>();
        for (Channel c : activeChannels.values()) {
            Peer p = c.getRemotePeer();
            set.add(new InetSocketAddress(p.getIp(), p.getPort()));
        }
        return set;
    }
    /**
     * Returns the active channels.
     *
     * @return
     */
    public List<Channel> getActiveChannels() {
        return new ArrayList<>(activeChannels.values());
    }
    /**
     * Returns the active channels, filtered by peerId.
     *
     * @param peerIds
     *            peerId filter
     * @return
     */
    public List<Channel> getActiveChannels(List<String> peerIds) {
        List<Channel> list = new ArrayList<>();
        for (String peerId : peerIds) {
            if (activeChannels.containsKey(peerId)) {
                list.add(activeChannels.get(peerId));
            }
        }
        return list;
    }
    public Channel getChannels(String remoteIp) {
        for (Channel c : activeChannels.values()) {
            String str = c.getRemoteAddress().toString();
            str = str.replace("/", "");
            if (str.equals(remoteIp)) {
                return c;
            }
        }
        return null;
    }
    /**
     * Returns the active channels, whose message queue is idle.
     *
     * @return
     */
    public List<Channel> getIdleChannels() {
        List<Channel> list = new ArrayList<>();
        for (Channel c : activeChannels.values()) {
            if (c.getMessageQueue().isIdle()) {
                list.add(c);
            }
        }
        return list;
    }
    // add by yuxj
    NetMessageMrg netMessageMrg = new NetMessageMrg();
    public void process(Channel channel, Message msg) {
        netMessageMrg.process(channel, msg);
    }
    public boolean RegMessage(String op, Object obj, String methodName) {
        return netMessageMrg.RegMessage(op, obj, methodName);
    }
    public boolean UnRegMessage(String op, Object obj, String methodName) {
        return netMessageMrg.UnRegMessage(op, obj, methodName);
    }
    // end by yuxj
}
