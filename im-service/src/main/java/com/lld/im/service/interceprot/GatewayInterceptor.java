package com.lld.im.service.interceprot;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.common.BaseErrorCode;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.enums.GateWayErrorCode;
import com.lld.im.common.exception.ApplicationExceptionEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Objects;

@Slf4j
@Component
public class GatewayInterceptor implements HandlerInterceptor {
    @Resource
    private IdentityCheck identityCheck;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取appId 操作人 userSign
        String appIdStr = request.getParameter("appId");
        if(StringUtils.isBlank(appIdStr)){
            resp(ResponseVO.fail(GateWayErrorCode.APPID_NOT_EXIST),response);
            return false;
        }

        String identifier = request.getParameter("identifier");
        if(StringUtils.isBlank(identifier)){
            resp(ResponseVO.fail(GateWayErrorCode.OPERATER_NOT_EXIST),response);
            return false;
        }

        // 接口调用方获取userSign的方式：用户通过appService登录app成功后，appService会给该用户生成一个userSign并返回，
        // 然后用户在请求IMService的接口时带上该userSign进行接口鉴权
        String userSign = request.getParameter("userSign");
        if(StringUtils.isBlank(userSign)){
            resp(ResponseVO.fail(GateWayErrorCode.USERSIGN_NOT_EXIST),response);
            return false;
        }

        // 签名和操作人和appId是否匹配
        ApplicationExceptionEnum applicationExceptionEnum = identityCheck.checkUserSig(identifier, appIdStr, userSign);
        if (!Objects.equals(applicationExceptionEnum, BaseErrorCode.SUCCESS)) {
            resp(ResponseVO.fail(applicationExceptionEnum), response);
            return false;
        }

        return true;
    }

    private void resp(ResponseVO respVo ,HttpServletResponse response){
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try {
            String resp = JSONObject.toJSONString(respVo);

            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-type", "application/json;charset=UTF-8");
            response.setHeader("Access-Control-Allow-Origin","*");
            response.setHeader("Access-Control-Allow-Credentials","true");
            response.setHeader("Access-Control-Allow-Methods","*");
            response.setHeader("Access-Control-Allow-Headers","*");
            response.setHeader("Access-Control-Max-Age","3600");
            writer = response.getWriter();
            writer.write(resp);
        } catch (Exception e){
            log.error("interceptor error, e: ", e);
        } finally {
            if(writer != null){
                writer.checkError();
            }
        }
    }
}
