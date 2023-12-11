package com.lld.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.proto.MessagePack;
import com.lld.im.common.command.Command;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class MessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private UserSessionUtils userSessionUtils;

    public void sendToUser(String toId, Integer clientType, String imei, Command command,
                           Object data, Integer appId) {
        if (clientType != null && StringUtils.isNotBlank(imei)) {
            ClientInfo clientInfo = new ClientInfo(appId, clientType, imei);
            sendToUserExceptClient(toId, command, data, clientInfo);
        } else {
            sendToUser(toId, command, data, appId);
        }
    }

    /**
     * 发送给所有端的方法
     *
     * @param toId 发送给谁的userId
     * @param command 指令
     * @param data 发送数据
     * @param appId appId
     * @return client信息集合
     */
    public List<ClientInfo> sendToUser(String toId, Command command, Object data, Integer appId) {
        List<UserSession> userSession = userSessionUtils.getUserSession(appId, toId);

        List<ClientInfo> list = new ArrayList<>();
        for (UserSession session : userSession) {
            if (sendPack(toId, command, data, session)) {
                list.add(new ClientInfo(session.getAppId(), session.getClientType(), session.getImei()));
            }
        }
        return list;
    }

    /**
     * 发送给某个用户的指定客户端
     *
     * @param toId 发送给谁的userId
     * @param command 指令
     * @param data 发送数据
     * @param clientInfo client信息
     */
    public void sendToUserAppointClient(String toId, Command command, Object data, ClientInfo clientInfo) {
        UserSession userSession = userSessionUtils.getUserSession(
                clientInfo.getAppId(), toId, clientInfo.getClientType(), clientInfo.getImei());

        sendPack(toId, command, data, userSession);
    }

    /**
     * 发送给除了某一端的其他端
     *
     * @param toId 发送给谁的userId
     * @param command 指令
     * @param data 发送数据
     * @param clientInfo client信息
     */
    public void sendToUserExceptClient(String toId, Command command, Object data, ClientInfo clientInfo) {
        List<UserSession> userSession = userSessionUtils.getUserSession(clientInfo.getAppId(), toId);

        for (UserSession session : userSession) {
            if (!isMatch(session, clientInfo)) {
                sendPack(toId, command, data, session);
            }
        }
    }

    //包装数据，调用sendMessage
    private boolean sendPack(String toId, Command command, Object msg, UserSession session) {
        MessagePack<JSONObject> messagePack = new MessagePack<>();
        messagePack.setCommand(command.getCommand());
        messagePack.setToId(toId);
        messagePack.setClientType(session.getClientType());
        messagePack.setAppId(session.getAppId());
        messagePack.setImei(session.getImei());
        messagePack.setData(JSONObject.parseObject(JSONObject.toJSONString(msg)));
        return sendMessage(session, JSONObject.toJSONString(messagePack));
    }

    private boolean sendMessage(UserSession session, String msg) {
        try {
            log.info("send message == " + msg);
            String exchangeName = Constants.RabbitConstants.MessageService2Im;
            rabbitTemplate.convertAndSend(exchangeName, String.valueOf(session.getBrokerId()), msg);
            return true;
        } catch (Exception e) {
            log.error("send error :" + e.getMessage());
            return false;
        }
    }

    private boolean isMatch(UserSession sessionDto, ClientInfo clientInfo) {
        return Objects.equals(sessionDto.getAppId(), clientInfo.getAppId())
                && Objects.equals(sessionDto.getImei(), clientInfo.getImei())
                && Objects.equals(sessionDto.getClientType(), clientInfo.getClientType());
    }
}
