package com.smartx.net.sync;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.smartx.Kernel;
import com.smartx.net.Channel;
import com.smartx.net.ChannelManager;
import com.smartx.net.msg.Message;
import com.smartx.net.msg.consensus.TestSyncRequest;
import com.smartx.net.msg.consensus.TestSyncResponse;
import com.smartx.net.sync.exception.SyncRequesterException;
import com.smartx.util.StringUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultChannelPromise;

public class SyncRequestManager {
    private static final Logger logger = Logger.getLogger(SyncRequestManager.class);
    private Kernel kernel;
    //一个请求的uuid对应一个requester
    private ConcurrentHashMap<String, SyncRequester> uuidRequestMap;
    public static SyncRequestManager Inst = null;
    public SyncRequestManager(Kernel kernel) {
        Inst = this;
        this.kernel = kernel;
        uuidRequestMap = new ConcurrentHashMap<>();
    }
    //将ChannelHandlerContext和SyncRequester对应起来放到一个Map里面，返回Promise
    //直接调用Promise.await就可以阻塞等待
    private DefaultChannelPromise addSyncRequest(SyncRequester requester) throws SyncRequesterException {
        String uuid = new String(requester.getRequestMessageUUID());
        //如果uuid对应的requester已经存在了，则抛出异常
        if (uuidRequestMap.get(uuid) != null) {
            logger.error(String.format("requester uuid {} is already exist" + String.valueOf(uuid)));
            throw (new SyncRequesterException("requester uuid is already exist"));
        }
        //将uuid,request键制对放入到map当中
        uuidRequestMap.put(uuid, requester);
        //返回promise
        return requester.sendRequest();
    }
    /**
     @param ctx 收到消息的连接上下文
     @param msg 收到的消息
     @Description 收到消息并且根据消息类型来唤醒对应的requester
     */
    public void onMessage(ChannelHandlerContext ctx, Message msg) {
        String uuid = new String(msg.getUUID());
        //根据UUID获取requester
        SyncRequester requester = uuidRequestMap.get(uuid);
        if (requester == null) {
            //logger.error("on ctx {} uuid {} no sync requester found",ctx,uuid);
            return;
        }
        try {
            //logger.debug("wake up requester by ctx {} uuid {} ",ctx,uuid);
            requester.wakeUp(ctx, msg);
        } catch (SyncRequesterException e) {
            e.printStackTrace();
            return;
        }
        return;
    }
    /**
     @param channel 接收消息的对端
     @param reqMsg  需要发送的消息
     @param timeout 等待超时时间，单位million seconds，0表示不等待
     @return Message
     对端返回的消息，超时返回为空
     @throws SyncRequesterException 传入参数为空，或者消息的uuid为空时，均抛出异常
     @Description
     */
    public Message sendMessageWait(Channel channel, Message reqMsg, long timeout) throws SyncRequesterException {
        try {
            if (channel == null) throw (new SyncRequesterException("bad parameter channel can not be null"));
            if (reqMsg == null) throw (new SyncRequesterException("bad parameter channel message can not be null"));
            String uuid = new String(reqMsg.getUUID());
            if (StringUtil.isNullOrEmpty(uuid)) {
                throw (new SyncRequesterException("bad parameter channel message uuid can not be null"));
            }
            SyncRequester requester = new SyncRequester(channel, reqMsg);
            this.addSyncRequest(requester).await(timeout);
            Message rspMsg = requester.getResponseMessage();
            uuidRequestMap.remove(uuid);
            return rspMsg;
        } catch (InterruptedException e) {
            logger.error("sync request await interrupted");
            e.printStackTrace();
            throw (new SyncRequesterException("sync request await interrupted"));
        }
    }
    //TODO:这个函数只是为了做测试，后期要删掉
    public void testRequestSync() {
        ChannelManager channelManager = this.kernel.getChannelManager();
        List<Channel> channels = channelManager.getActiveChannels();
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);
        //等待ChannelMannger当中的活跃节点数达到3个以上
        while (channels.size() < 3) {
            channels = channelManager.getActiveChannels();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        final List<Channel> finalchannels = channelManager.getActiveChannels();
        for (int i = 0; i < 10; i++) {
            final int channelIndex = i % channels.size();
            final Channel channel = finalchannels.get(channelIndex);
            //[1]new一个消息
            TestSyncRequest reqMsg = new TestSyncRequest();
            fixedThreadPool.execute(() -> {
                try {
                    String uuid = new String(reqMsg.getUUID());
                    logger.debug(String.format("request sync uuid {%s} at thread {%s}", uuid, Thread.currentThread()));
                    //[2]发送消息并等待15s，然后取出返回结果
                    TestSyncResponse rsp = (TestSyncResponse) sendMessageWait(channel, reqMsg, 15 * 1000);
                    logger.debug(String.format("awake sync from uuid {%s} at thread {%s} current cached request number {%d} ", uuid, Thread.currentThread(), uuidRequestMap.size()));
                } catch (SyncRequesterException e) {
                    logger.error("request uuid already exist");
                }
            });
        }
    }
}
