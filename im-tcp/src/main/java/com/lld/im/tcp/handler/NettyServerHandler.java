package com.lld.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lld.im.codec.proto.Message;
import com.lld.im.codec.proto.MessageHeader;
import com.lld.im.common.command.SystemCommand;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ImConnectStatusEnum;
import com.lld.im.common.model.UserSession;
import com.lld.im.common.pack.LoginPack;
import com.lld.im.tcp.redis.RedisManager;
import com.lld.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.Objects;

import static com.lld.im.common.constant.Constants.RedisConstants.UserSessionConstants;

@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        MessageHeader header = message.getMessageHeader();

        Integer command = header.getCommand();

        // 登录command
        if (Objects.equals(command, SystemCommand.LOGIN.getCommand())) {
            // 解析请求体
            LoginPack loginPack = JSON.parseObject(JSONObject
                    .toJSONString(message.getMessagePack()), new TypeReference<LoginPack>() {
            }.getType());

            // 为channel设置userId属性
            ctx.channel().attr(AttributeKey.valueOf(Constants.UserId)).set(loginPack.getUserId());
            ctx.channel().attr(AttributeKey.valueOf(Constants.AppId)).set(header.getAppId());
            ctx.channel().attr(AttributeKey.valueOf(Constants.ClientType)).set(header.getClientType());

            // 将channel分布式存储起来
            UserSession userSession = new UserSession();
            userSession.setUserId(loginPack.getUserId());
            userSession.setAppId(header.getAppId());
            userSession.setClientType(header.getClientType());
            userSession.setConnectState(ImConnectStatusEnum.ONLINE_STATUS.getCode());

            // 存入redis map中
            RedissonClient redissonClient = RedisManager.getRedissonClient();
            // key: 用户session，appId + UserSessionConstants + 用户id 例如10000：userSession：lld
            String sessionKey = header.getAppId() + UserSessionConstants + loginPack.getUserId();
            RMap<String, String> map = redissonClient.getMap(sessionKey);
            // field: clientType  value: userSession
            map.put(String.valueOf(header.getClientType()), JSONObject.toJSONString(userSession));

            SessionSocketHolder.put(header.getAppId(), loginPack.getUserId(),
                    header.getClientType(), ((NioSocketChannel) ctx.channel()));
        } else if (Objects.equals(command, SystemCommand.LOGOUT.getCommand())) {

            SessionSocketHolder.removeSession(((NioSocketChannel) ctx.channel()));

        } else if (Objects.equals(command, SystemCommand.PING.getCommand())) {
            ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).set(System.currentTimeMillis());
        }
    }
}
