package com.lld.im.tcp.server;

import com.lld.im.codec.MessageDecoder;
import com.lld.im.codec.config.BootstrapConfig.TcpConfig;
import com.lld.im.tcp.handler.HeartBeatHandler;
import com.lld.im.tcp.handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LimServer {
    private int port;

    private TcpConfig tcpConfig;

    private NioEventLoopGroup boosGroup;

    private NioEventLoopGroup workGroup;

    private ServerBootstrap serverBootstrap;

    public LimServer(TcpConfig tcpConfig) {
        this.port = tcpConfig.getTcpPort();
        this.tcpConfig = tcpConfig;
        this.boosGroup = new NioEventLoopGroup(tcpConfig.getBossThreadSize());
        this.workGroup = new NioEventLoopGroup(tcpConfig.getWorkThreadSize());
        this.serverBootstrap = new ServerBootstrap()
                .group(boosGroup, workGroup).channel(NioServerSocketChannel.class)
                // 服务端可连接队列大小
                .option(ChannelOption.SO_BACKLOG, 10240)
                // 参数表示允许重复使用本地地址和端口
                .option(ChannelOption.SO_REUSEADDR, true)
                // 是否禁用Nagle算法 简单点说是否批量发送数据 true关闭 false开启。 开启的话可以减少一定的网络开销，但影响消息实时性
                .childOption(ChannelOption.TCP_NODELAY, true)
                // 保活开关2h没有数据服务端会发送心跳包
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new MessageDecoder())
                                .addLast(new IdleStateHandler(0,0,1))
                                .addLast(new HeartBeatHandler(tcpConfig.getHeartBeatTime()))
                                .addLast(new NettyServerHandler());
                    }
                });
    }

    public void start() {
        serverBootstrap.bind(port);
    }
}
