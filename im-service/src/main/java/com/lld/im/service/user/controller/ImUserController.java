package com.lld.im.service.user.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.route.handler.RouteHandler;
import com.lld.im.common.route.RouteInfo;
import com.lld.im.common.utils.RouteInfoParseUtil;
import com.lld.im.service.user.model.req.*;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.utils.ZKit;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("v1/user")
public class ImUserController {
    private static final String ROUTE_HANDLER_PREFIX = "routeHandler-";

    @Resource
    private ImUserService imUserService;

    @Resource
    private Map<String, RouteHandler> routeHandlerMap;

    @Resource
    private ZKit zKit;

    @Resource
    private AppConfig appConfig;

    @PostMapping("importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req) {
        return imUserService.importUser(req);
    }

    @PostMapping("/deleteUser")
    public ResponseVO deleteUser(@RequestBody @Validated DeleteUserReq req) {
        return imUserService.deleteUser(req);
    }

    @PostMapping("/login")
    public ResponseVO login(@RequestBody @Validated LoginReq req) {
        ResponseVO login = imUserService.login(req);

        if (!login.isOk()) {
            return ResponseVO.fail();
        }

        List<String> allNode = zKit.getAllNode(req.getClientType());

        // 通过策略模式获取到routeHandler实例
        RouteHandler routeHandler = routeHandlerMap.get(ROUTE_HANDLER_PREFIX + appConfig.getImRouteWay());
        String zkNode = routeHandler.routeServer(allNode, req.getUserId());

        // 将node节点信息处理成ip+port形式
        RouteInfo routeInfo = RouteInfoParseUtil.parse(zkNode);
        return ResponseVO.successResponse(routeInfo);
    }

}
