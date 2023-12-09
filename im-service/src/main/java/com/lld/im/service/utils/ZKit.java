package com.lld.im.service.utils;

import com.lld.im.common.ClientType;
import com.lld.im.common.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class ZKit {
    @Autowired
    private ZkClient zkClient;

    public List<String> getAllNode(Integer nodeType) {
        try {
            return Objects.equals(ClientType.WEB.getCode(), nodeType)
                    ? zkClient.getChildren(Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb)
                    : zkClient.getChildren(Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp);
        } catch (Exception e) {
            log.error("Query all node fail, e:", e);
            return new ArrayList<>(0);
        }
    }
}
