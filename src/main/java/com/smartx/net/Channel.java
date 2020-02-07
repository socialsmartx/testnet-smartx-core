/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.smartx.Kernel;
import com.smartx.net.msg.Message;
import com.smartx.net.msg.MessageQueue;
import com.smartx.net.msg.SmartXMessage;
import com.smartx.net.sync.SyncRequestManager;
import com.smartx.net.sync.exception.SyncRequesterException;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class Channel {
    private final NioSocketChannel socket;
    private boolean isInbound;
    private InetSocketAddress remoteAddress;
    private Peer remotePeer;
    private MessageQueue msgQueue;
    private boolean isActive;
    /**
     * Creates a new channel instance.
     *
     */
    public Channel(NioSocketChannel socket) {
        this.socket = socket;
    }
    /**
     * Initializes this channel.
     *
     * @param pipe
     * @param isInbound
     * @param remoteAddress
     * @param kernel
     */
    public void init(ChannelPipeline pipe, boolean isInbound, InetSocketAddress remoteAddress, Kernel kernel) {
        this.isInbound = isInbound;
        this.remoteAddress = remoteAddress;
        this.remotePeer = null;
        this.msgQueue = new MessageQueue(kernel.getConfig());
        // register channel handlers
        if (isInbound) {
            pipe.addLast("inboundLimitHandler", new ConnectionLimitHandler(kernel.getConfig().netMaxInboundConnectionsPerIp()));
        }
        pipe.addLast("readTimeoutHandler", new ReadTimeoutHandler(kernel.getConfig().netChannelIdleTimeout(), TimeUnit.MILLISECONDS));
        pipe.addLast("frameHandler", new SmartxFrameHandler(kernel.getConfig()));
        pipe.addLast("messageHandler", new SmartxMessageHandler(kernel.getConfig()));
        pipe.addLast("p2pHandler", new SmartxP2pHandler(this, kernel));
    }
    /**
     * Closes the underlying socket channel.
     */
    public void close() {
        socket.close();
    }
    /**
     * Returns the message queue.
     *
     * @return
     */
    public MessageQueue getMessageQueue() {
        return msgQueue;
    }
    /**
     * Returns whether this is an inbound channel.
     *
     * @return
     */
    public boolean isInbound() {
        return isInbound;
    }
    /**
     * Returns whether this is an outbound channel.
     *
     * @return
     */
    public boolean isOutbound() {
        return !isInbound();
    }
    /**
     * Returns the remote peer.
     *
     * @return
     */
    public Peer getRemotePeer() {
        return remotePeer;
    }
    /**
     * Returns whether this channel is active.
     *
     * @return
     */
    public boolean isActive() {
        return isActive;
    }
    /**
     * Sets this channel to be active.
     *
     * @param remotePeer
     */
    public void setActive(Peer remotePeer) {
        this.remotePeer = remotePeer;
        this.isActive = true;
    }
    /**
     * Sets this channel to be inactive.
     */
    public void setInactive() {
        /*
         * Remote peer is not reset because other thread may still hold a reference to
         * this channel
         */
        // this.remotePeer = null;
        this.isActive = false;
    }
    /**
     * Returns the remote address.
     *
     * @return
     */
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }
    /**
     * Returns remote IP address.
     *
     * @return
     */
    public String getRemoteIp() {
        return remoteAddress.getAddress().getHostAddress();
    }
    /**
     * Returns remote port.
     *
     * @return
     */
    public int getRemotePort() {
        return remoteAddress.getPort();
    }
    @Override
    public String toString() {
        return "Channel [" + (isInbound ? "Inbound" : "Outbound") + ", remoteIp = " + getRemoteIp() + ", remotePeer = " + remotePeer + "]";
    }
    public void sendMessage(Message msg) throws SyncRequesterException {
        if (SmartXMessage.class.isInstance(msg)) ((SmartXMessage) msg).Encoder();
        this.getMessageQueue().sendMessage(msg);
    }
    // add by yuxj
    public SmartXMessage queryMessage(SmartXMessage msg, long timeout) throws SyncRequesterException {
        msg.Encoder();
        return (SmartXMessage) SyncRequestManager.Inst.sendMessageWait(this, msg, timeout);
    }
    public void replyMessage(SmartXMessage query, SmartXMessage reply) throws SyncRequesterException {
        reply.setUUID(query.getUUID());
        reply.Encoder();
        this.getMessageQueue().sendMessage(reply);
    }
    // end by yuxj
}
