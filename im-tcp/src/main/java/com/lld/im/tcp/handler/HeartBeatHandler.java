package com.lld.im.tcp.handler;

import com.lld.im.common.constant.Constants;
import com.lld.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
    private Long heartBeatTime;

    public HeartBeatHandler(Long heartBeatTime) {
        this.heartBeatTime = heartBeatTime;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;

            if (Objects.equals(idleStateEvent.state(), IdleState.READER_IDLE)) {
                log.info("读空闲......");
            } else if (Objects.equals(idleStateEvent.state(), IdleState.WRITER_IDLE)) {
                log.info("写空闲......");
            } else if (Objects.equals(idleStateEvent.state(), IdleState.ALL_IDLE)){
                // 最后一次读写时间
                Long lastAllIdleTime = (Long) ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).get();

                // 判断是否需要把客户端踢出服务端, 首次lastAllIdleTime为null
                if (Objects.nonNull(lastAllIdleTime) && System.currentTimeMillis() - lastAllIdleTime > heartBeatTime) {
                    // 用户离线
                    SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
                }
            } else {
                log.info("。。。。。。");
            }
        }
    }
}
