/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net.msg;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.smartx.config.SystemProperties;
import com.smartx.net.msg.p2p.DisconnectMessage;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultChannelPromise;

/**
 * This class contains the logic for sending messages.
 */
public class MessageQueue {
    private static final Logger logger = Logger.getLogger(MessageQueue.class);
    private static final ScheduledExecutorService timer = Executors.newScheduledThreadPool(4, new ThreadFactory() {
        private final AtomicInteger cnt = new AtomicInteger(0);
        public Thread newThread(Runnable r) {
            return new Thread(r, "msg-" + cnt.getAndIncrement());
        }
    });
    private final SystemProperties config;
    private final Queue<Message> queue = new ConcurrentLinkedQueue<>();
    private final Queue<Message> prioritized = new ConcurrentLinkedQueue<>();
    private ChannelHandlerContext ctx;
    private ScheduledFuture<?> timerTask;
    private AtomicBoolean isClosed = new AtomicBoolean(false);
    /**
     * Create a message queue with the specified maximum queue size.
     *
     * @param config
     */
    public MessageQueue(SystemProperties config) {
        this.config = config;
    }
    /**
     * Activates this message queue and binds it to the channel.
     *
     * @param ctx
     */
    public synchronized void activate(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.timerTask = timer.scheduleAtFixedRate(() -> {
            try {
                nudgeQueue();
            } catch (Exception t) {
                logger.error("Exception in MessageQueue", t);
            }
        }, 10, 10, TimeUnit.MILLISECONDS);
    }
    /**
     * Deactivates this message queue.
     */
    public synchronized void deactivate() {
        this.timerTask.cancel(false);
    }
    /**
     * Returns if this message queue is idle.
     *
     * NOTE that requests are no longer kept in the queue after we send them out.
     * even through the message queue is idle, from our perspective, the peer may
     * still be busy responding our requests.
     *
     * @return true if message queues are empty, otherwise false
     */
    public boolean isIdle() {
        return size() == 0;
    }
    /**
     * Disconnects aggressively.
     *
     * @param code
     */
    public void disconnect(ReasonCode code) {
        logger.debug("Actively closing the connection: reason = {}" + code);
        // avoid repeating close requests
        if (isClosed.compareAndSet(false, true)) {
            ctx.writeAndFlush(new DisconnectMessage(code)).addListener((ChannelFutureListener) future -> ctx.close());
        }
    }
    /**
     * Adds a message to the sending queue.
     *
     * @param msg
     *            the message to be sent
     * @return true if the message is successfully added to the queue, otherwise
     *         false
     */
    public boolean sendMessage(Message msg) {
        if (size() >= config.netMaxMessageQueueSize()) {
            disconnect(ReasonCode.MESSAGE_QUEUE_FULL);
            return false;
        }
        if (config.netPrioritizedMessages().contains(msg.getCode())) {
            prioritized.add(msg);
        } else {
            queue.add(msg);
        }
        return true;
    }
    //发送消息并且等待回复
    public DefaultChannelPromise sendMessageSync(Message msg) {
        //加锁保护，防止多线程同时写一个channel导致数据包混乱
        synchronized (this) {
            return new DefaultChannelPromise(ctx.writeAndFlush(msg).channel());
        }
    }
    /**
     * Returns the number of messages in queue.
     *
     * @return
     */
    public int size() {
        return queue.size() + prioritized.size();
    }
    protected void nudgeQueue() {
        //加锁保护，防止多线程同时写一个channel导致数据包混乱
        synchronized (this) {
            // 1000 / 10 * 5 = 500 messages per second
            int n = Math.min(5, size());
            if (n == 0) {
                return;
            }
            // write out n messages
            for (int i = 0; i < n; i++) {
                Message msg = !prioritized.isEmpty() ? prioritized.poll() : queue.poll();
                logger.debug(String.format("Wiring message: {%s}", msg.getCode()));
                ctx.write(msg).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            }
            // flush
            ctx.flush();
        }
    }
    public final ChannelHandlerContext getChannelHandlerContext() {
        return this.ctx;
    }
}
