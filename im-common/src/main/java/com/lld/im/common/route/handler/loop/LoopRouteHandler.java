package com.lld.im.common.route.handler.loop;

import com.lld.im.common.enums.UserErrorCode;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.common.route.handler.RouteHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component(value = "routeHandler-2")
public class LoopRouteHandler implements RouteHandler {
    private AtomicInteger index = new AtomicInteger();

    @Override
    public String routeServer(List<String> nodeList, String userId) {
        if (nodeList.size() == 0) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }

        return nodeList.get(index.incrementAndGet() % nodeList.size());
    }
}
