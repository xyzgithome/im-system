package com.lld.im.study.netty.upload;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Objects;

public class UploadFileHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileDto) {
            FileDto dto = (FileDto) msg;

            if (dto.getCommand() == 1) {
                // ...
            }

            if (dto.getCommand() == 2) {
                // ...
            }
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }
}
