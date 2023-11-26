package com.lld.im.study.netty.codec;

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

/**
 * 粘包拆包解决方案
 * 1. 读固定长度的字符
 *     .addLast(new FixedLengthFrameDecoder(9))
 *     缺点：限制了消息的字符数
 * 2. 添加字符串分隔符
 *      .addLast(new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer("_".getBytes())))
 *      缺点：消息中不能再使用_下划线，不然会导致消息错乱
 * 3. 自定义数据发送协议MyDecoder
 */
@Slf4j
public class ChatServer {

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
                            .addLast("decoder", new MyDecoder());
                            // 自定义的handler，入栈handler
//                            .addLast(new ChatHandler());
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
