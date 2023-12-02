package com.lld.im.tcp.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ImConnectStatusEnum;
import com.lld.im.common.model.UserClientDto;
import com.lld.im.common.model.UserSession;
import com.lld.im.tcp.redis.RedisManager;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.lld.im.common.constant.Constants.RedisConstants.UserSessionConstants;

public class SessionSocketHolder {

    private static final Map<UserClientDto, NioSocketChannel> CHANNEL_MAP = new ConcurrentHashMap<>();

    public static void put(Integer appId, String userId, Integer clientType, NioSocketChannel channel){
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setUserId(userId);
        userClientDto.setClientType(clientType);
        userClientDto.setAppId(appId);
        CHANNEL_MAP.put(userClientDto, channel);
    }

    public static NioSocketChannel get(Integer appId, String userId, Integer clientType){
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setUserId(userId);
        userClientDto.setClientType(clientType);
        userClientDto.setAppId(appId);
        return CHANNEL_MAP.get(userClientDto);
    }

    public static void remove(Integer appId, String userId, Integer clientType){
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setUserId(userId);
        userClientDto.setClientType(clientType);
        userClientDto.setAppId(appId);
        CHANNEL_MAP.remove(userClientDto);
    }

    public static void remove(NioSocketChannel channel){
        CHANNEL_MAP.entrySet().stream()
                .filter(item -> Objects.equals(item.getValue(), channel))
                .forEach(entry -> CHANNEL_MAP.remove(entry.getKey()));
    }

    /**
     * 用户退出
     *
     * @param channel
     */
    public static void removeSession(NioSocketChannel channel){
        String userId = (String) channel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId = (Integer) channel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType = (Integer) channel.attr(AttributeKey.valueOf(Constants.ClientType)).get();

        // 删除内存中的session
        SessionSocketHolder.remove(appId, userId, clientType);

        // 删除redis中的session
        String sessionKey = appId + UserSessionConstants + userId;
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<String, String> map = redissonClient.getMap(sessionKey);
        map.remove(String.valueOf(clientType));

        // 关闭channel
        channel.close();
    }

    /**
     * 用户离线
     *
     * @param channel
     */
    public static void offlineUserSession(NioSocketChannel channel){
        String userId = (String) channel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId = (Integer) channel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType = (Integer) channel.attr(AttributeKey.valueOf(Constants.ClientType)).get();

        // 删除内存中的session
        SessionSocketHolder.remove(appId, userId, clientType);

        // 更新redis中session的状态为下线
        String sessionKey = appId + UserSessionConstants + userId;
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<String, String> map = redissonClient.getMap(sessionKey);

        String sessionValue = map.get(String.valueOf(clientType));

        if (StringUtils.isNotBlank(sessionValue)) {
            UserSession userSession = JSON.parseObject(sessionValue, new TypeReference<UserSession>() {
            }.getType());

            userSession.setConnectState(ImConnectStatusEnum.OFFLINE_STATUS.getCode());

            map.put(String.valueOf(clientType), JSONObject.toJSONString(userSession));
        }

        // 关闭channel
        channel.close();
    }

}
