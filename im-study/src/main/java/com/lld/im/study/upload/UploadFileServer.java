package com.lld.im.study.netty.upload;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

@Slf4j
public class UploadFileServer {

    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workGroup = new NioEventLoopGroup(0);

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);
            serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    // 入栈的handler或者出栈的handler顺序不能弄错
                    socketChannel.pipeline()
                            // 解码器：入栈handler
                            .addLast("decoder", new StringDecoder(Charset.forName("GBK")))
                            // 编码器：出栈handler
                            .addLast("encoder", new StringEncoder(Charset.forName("GBK")))
                            // 自定义的handler，入栈handler
                            .addLast(new UploadFileHandler());
                }
            });

            ChannelFuture channelFuture = serverBootstrap.bind(8001).sync();

            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("e: ", e);
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }


}
