package com.lld.im.tcp.server;

import com.lld.im.codec.config.BootstrapConfig.TcpConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LimWebSocketServer {
    private int port;

    private TcpConfig tcpConfig;

    private NioEventLoopGroup boosGroup;

    private NioEventLoopGroup workGroup;

    private ServerBootstrap serverBootstrap;

    public LimWebSocketServer(TcpConfig tcpConfig) {
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
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        // websocket 基于http协议，所以要有http编解码器
                        pipeline.addLast("http-codec", new HttpServerCodec());
                        // 对写大数据流的支持
                        pipeline.addLast("http-chunked", new ChunkedWriteHandler());
                        // 几乎在netty中的编程，都会使用到此hanler
                        pipeline.addLast("aggregator", new HttpObjectAggregator(65535));
                        /**
                         * websocket 服务器处理的协议，用于指定给客户端连接访问的路由 : /ws
                         * 本handler会帮你处理一些繁重的复杂的事
                         * 会帮你处理握手动作： handshaking（close, ping, pong） ping + pong = 心跳
                         * 对于websocket来讲，都是以frames进行传输的，不同的数据类型对应的frames也不同
                         */
                        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
//                        pipeline.addLast(new WebSocketMessageDecoder());
//                        pipeline.addLast(new WebSocketMessageEncoder());
//                        pipeline.addLast(new NettyServerHandler(config.getBrokerId(),config.getLogicUrl()));
                    }
                });
    }

    public void start() {
        serverBootstrap.bind(port);
    }


}
