package com.lld.im.study.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Objects;

public class ChatHandler extends ChannelInboundHandlerAdapter {
    private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channels.forEach(channel -> {
            if (Objects.equals(ctx.channel(), channel)) {
                channel.writeAndFlush("【自己】" + ctx.channel().remoteAddress() + "：" + "下线");
            } else {
                channel.writeAndFlush("【客户端】" + ctx.channel().remoteAddress() + "：" + "下线");
            }
        });

        channels.remove(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = msg.toString();
        System.out.println("服务端打印：" + message);

        channels.forEach(channel -> {
             // 是自己
             if (Objects.equals(ctx.channel(), channel)) {
                 channel.writeAndFlush("【自己】" + ctx.channel().remoteAddress() + "：" + message);
             } else {
                 channel.writeAndFlush("【客户端】" + ctx.channel().remoteAddress() + "："+message);
             }
         });
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channels.forEach(channel -> {
            if (Objects.equals(ctx.channel(), channel)) {
                channel.writeAndFlush("【自己】" + ctx.channel().remoteAddress() + "：" + "上线");
            } else {
                channel.writeAndFlush("【客户端】" + ctx.channel().remoteAddress() + "：" + "上线");
            }
        });

        channels.add(ctx.channel());
    }
}
