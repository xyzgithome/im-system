package com.lld.im.tcp.utils;

import com.lld.im.common.model.UserClientDto;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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
}
