package com.smartx.net.sync;

import org.apache.log4j.Logger;

import com.smartx.net.Channel;
import com.smartx.net.msg.Message;
import com.smartx.net.sync.exception.SyncRequesterException;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultChannelPromise;

public class SyncRequester {
    private static final Logger logger = Logger.getLogger(SyncRequester.class);
    private final Channel channel;              //发送同步请求的链接
    private final Message reqMsg;               //同步等待被唤醒后的
    //private final MessageCode rspMsgCode;       //期望获取对端回复的消息类型
    private Message rspMsg;                     //对端回复的消息
    private DefaultChannelPromise promise = null;//等待的promise，作用类似于pthread_cond_t
    /**
     请求构造函数

     @param channel channel mannager当中的某个channel
     @param reqMsg  要发送的消息
     */
    public SyncRequester(Channel channel, Message reqMsg) {
        this.channel = channel;
        this.reqMsg = reqMsg;
        this.rspMsg = null;
    }
    //发送请求并且返回promise
    public DefaultChannelPromise sendRequest() {
        this.promise = channel.getMessageQueue().sendMessageSync(reqMsg);
        return this.promise;
    }
    //唤醒等待，并传消息给等待的链接
    public void wakeUp(ChannelHandlerContext ctx, Message msg) throws SyncRequesterException {
        //如果没有开启等待，则抛出异常
        if (this.promise == null) {
            logger.warn(String.format("receive message from remote {}:{}, sync request is not awaiting", ctx.channel().remoteAddress()));
            //throw (new SyncRequesterException("sync request is not awaiting")); by sxh
            return;
        }
        //TODO:如果所需要的消息类型跟自己预期的不一致则抛出异常
        //        if(this.rspMsgCode != message.getCode()){
        //            logger.error("receive message from remote {}:{},message code not equal with expect",
        //                    ctx.channel().remoteAddress());
        //
        //            throw(new SyncRequesterException("message code not equal with expect"));
        //        }
        //如果是同一个链接发过来的类型匹配的消息，则唤醒等待
        if (ctx.channel().equals(this.promise.channel())) {
            this.rspMsg = msg;
            this.promise.setSuccess();
            this.promise = null;
        }
    }
    //得到的消息
    public Message getResponseMessage() {
        return this.rspMsg;
    }
    //得到ChannelManager里面的Channel
    public ChannelHandlerContext getChannelHandlerContext() {
        return this.channel.getMessageQueue().getChannelHandlerContext();
    }
    public byte[] getRequestMessageUUID() {
        return this.reqMsg.getUUID();
    }
}
