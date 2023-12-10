package com.lld.im.service.utils;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.utils.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CallbackService {

    @Resource
    private HttpRequestUtils httpRequestUtils;

    @Resource
    private AppConfig appConfig;

    @Resource
    private ShareThreadPool shareThreadPool;

    /**
     * 操作执行之后回调
     *
     * @param appId appId
     * @param callbackCommand 回调指令
     * @param jsonBody 回调数据
     */
    public void afterCallback(Integer appId, String callbackCommand, String jsonBody) {
        shareThreadPool.submit(() -> {
            try {
                httpRequestUtils.doPost(appConfig.getCallbackUrl(), Object.class,
                        builderUrlParams(appId, callbackCommand), jsonBody, StandardCharsets.UTF_8.name());
            } catch (Exception e) {
                log.error("callback 回调{} : {}出现异常 ： {} ", callbackCommand, appId, e.getMessage());
            }
        });
    }

    /**
     * 操作执行之前回调
     *
     * @param appId appId
     * @param callbackCommand 回调指令
     * @param jsonBody 回调数据
     */
    public ResponseVO beforeCallback(Integer appId, String callbackCommand, String jsonBody) {
        try {
            return httpRequestUtils.doPost(appConfig.getCallbackUrl(), ResponseVO.class,
                    builderUrlParams(appId, callbackCommand), jsonBody, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            log.error("callback 之前 回调{} : {}出现异常 ： {} ", callbackCommand, appId, e.getMessage());
            return ResponseVO.fail();
        }
    }

    private Map<String, Object> builderUrlParams(Integer appId, String command) {
        Map<String, Object> map = new HashMap<>();
        map.put("appId", appId);
        map.put("command", command);
        return map;
    }
}
