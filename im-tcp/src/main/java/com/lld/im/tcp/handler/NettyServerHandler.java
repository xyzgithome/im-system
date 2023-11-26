package com.lld.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lld.im.codec.proto.Message;
import com.lld.im.codec.proto.MessageHeader;
import com.lld.im.common.command.SystemCommand;
import com.lld.im.common.pack.LoginPack;
import com.lld.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        MessageHeader header = message.getMessageHeader();

        Integer command = header.getCommand();

        if (Objects.equals(command, SystemCommand.LOGIN.getCommand())) {
            // 解析请求体
            LoginPack loginPack = JSON.parseObject(JSONObject
                    .toJSONString(message.getMessagePack()), new TypeReference<LoginPack>() {
            }.getType());

            // 为channel设置userId属性
            ctx.channel().attr(AttributeKey.valueOf("userId")).set(loginPack.getUserId());

            // 将channel存储起来
            SessionSocketHolder.put(loginPack.getUserId(), ((NioSocketChannel) ctx.channel()));
        }
    }
}
