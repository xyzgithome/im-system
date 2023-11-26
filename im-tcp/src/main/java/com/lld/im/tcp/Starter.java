package com.lld.im.tcp;

import com.lld.im.codec.config.BootstrapConfig;
import com.lld.im.tcp.server.LimServer;
import com.lld.im.tcp.server.LimWebSocketServer;
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
    }

}
