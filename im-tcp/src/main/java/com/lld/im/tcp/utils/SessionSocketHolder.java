package com.lld.im.tcp.utils;

import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionSocketHolder {

    private static final Map<String, NioSocketChannel> CHANNEL_MAP = new ConcurrentHashMap<>();

    public static void put(String userId, NioSocketChannel channel){
        CHANNEL_MAP.put(userId, channel);
    }

    public static NioSocketChannel get(String userId){
        return CHANNEL_MAP.get(userId);
    }
}
