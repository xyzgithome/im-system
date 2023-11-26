package com.lld.im.tcp.handler;

import com.lld.im.codec.proto.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
        log.info(message.toString());
    }
}
