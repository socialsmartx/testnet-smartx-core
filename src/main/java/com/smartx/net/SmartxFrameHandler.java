/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.smartx.config.SystemProperties;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

public class SmartxFrameHandler extends ByteToMessageCodec<Frame> {
    private static final Logger logger = Logger.getLogger(SmartxFrameHandler.class);
    private final SystemProperties config;
    public SmartxFrameHandler(SystemProperties config) {
        this.config = config;
    }
    @Override
    protected void encode(ChannelHandlerContext ctx, Frame frame, ByteBuf out) throws Exception {
        // check version
        if (frame.getVersion() != Frame.VERSION) {
            logger.error("Invalid frame version: {}" + frame.getVersion());
            return;
        }
        // check body size
        int bodySize = frame.getBodySize();
        if (bodySize < 0 || bodySize > config.netMaxFrameBodySize()) {
            logger.error("Invalid frame body size: {}" + bodySize);
            return;
        }
        // create a buffer
        ByteBuf buf = out.alloc().buffer(Frame.HEADER_SIZE + bodySize);
        frame.writeHeader(buf);
        buf.writeBytes(frame.getBody());
        // NOTE: write() operation does not flush automatically
        // write to context
        ctx.write(buf);
    }
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < Frame.HEADER_SIZE) {
            return;
        }
        // read frame header
        int readerIndex = in.readerIndex();
        Frame frame = Frame.readHeader(in);
        // check version
        if (frame.getVersion() != Frame.VERSION) {
            throw new IOException("Invalid frame version: " + frame.getVersion());
        }
        // check body size
        int bodySize = frame.getBodySize();
        if (bodySize < 0 || bodySize > config.netMaxFrameBodySize()) {
            throw new IOException("Invalid frame body size: " + bodySize);
        }
        if (in.readableBytes() < bodySize) {
            // reset reader index if not available
            in.readerIndex(readerIndex);
        } else {
            // read body
            byte[] body = new byte[bodySize];
            in.readBytes(body);
            frame.setBody(body);
            // deliver
            out.add(frame);
        }
    }
}
