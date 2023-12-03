package com.lld.im.tcp.register;

import com.lld.im.codec.config.BootstrapConfig;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import sun.security.jgss.HttpCaller;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class ZKManager {

    public static void init(BootstrapConfig.TcpConfig config) {
        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            ZkClient zkClient = new ZkClient(config.getZkConfig().getZkAddr(),
                    config.getZkConfig().getZkConnectTimeOut());
            ZKit zKit = new ZKit(zkClient);
            RegistryZK registryZK = new RegistryZK(zKit, hostAddress, config);
            Thread thread = new Thread(registryZK);
            thread.start();
        } catch (Exception e) {
            log.error("init zk error, e: ", e);
        }
    }
}
