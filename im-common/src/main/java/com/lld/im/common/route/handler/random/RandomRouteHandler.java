package com.lld.im.common.route.handler.random;

import com.lld.im.common.enums.UserErrorCode;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.common.route.handler.RouteHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component(value = "routeHandler-1")
public class RandomRouteHandler implements RouteHandler {
    @Override
    public String routeServer(List<String> nodeList, String userId) {
        if (nodeList.size() == 0) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }

        return nodeList.get(ThreadLocalRandom.current().nextInt(nodeList.size()));
    }
}
