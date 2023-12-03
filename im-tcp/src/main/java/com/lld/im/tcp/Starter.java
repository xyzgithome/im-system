package com.lld.im.tcp;

import com.lld.im.codec.config.BootstrapConfig;
import com.lld.im.tcp.receiver.MessageReceiver;
import com.lld.im.tcp.redis.RedisManager;
import com.lld.im.tcp.register.ZKManager;
import com.lld.im.tcp.server.LimServer;
import com.lld.im.tcp.server.LimWebSocketServer;
import com.lld.im.tcp.utils.MqFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Starter {
    public static void main(String[] args) throws FileNotFoundException {
        if (args.length > 0) {
            start(args[0]);
        }
    }

    private static void start(String path) throws FileNotFoundException {
        Yaml yaml = new Yaml();

        FileInputStream inputStream = new FileInputStream(path);

        BootstrapConfig bootstrapConfig = yaml.loadAs(inputStream, BootstrapConfig.class);

        new LimServer(bootstrapConfig.getLim()).start();

        new LimWebSocketServer(bootstrapConfig.getLim()).start();

        RedisManager.init(bootstrapConfig.getLim().getRedis());

        MqFactory.init(bootstrapConfig.getLim().getRabbitmq());

        MessageReceiver.init(bootstrapConfig.getLim().getBrokerId());

        ZKManager.init(bootstrapConfig.getLim());
    }

}
