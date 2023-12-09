package com.lld.im.service.config;

import com.lld.im.common.config.AppConfig;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class BeanConfig {
    @Resource
    private AppConfig appConfig;

    @Bean
    public ZkClient zkClient() {
        return new ZkClient(appConfig.getZkAddr(),
                appConfig.getZkConnectTimeOut());
    }
}
