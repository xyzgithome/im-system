package com.lld.im.tcp.redis;

import com.lld.im.codec.config.BootstrapConfig;
import com.lld.im.tcp.receiver.UserLoginMessageListener;
import org.redisson.api.RedissonClient;

public class RedisManager {

    private static RedissonClient redissonClient;

    public static void init(BootstrapConfig.TcpConfig tcpConfig){
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();

        redissonClient = singleClientStrategy.getRedissonClient(tcpConfig.getRedis());

        // 启动redis队列监听
        new UserLoginMessageListener(tcpConfig.getLoginModel()).listenerUserLogin();
    }

    public static RedissonClient getRedissonClient(){
        return redissonClient;
    }
}
