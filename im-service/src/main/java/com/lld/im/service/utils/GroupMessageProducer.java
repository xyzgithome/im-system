package com.lld.im.service.utils;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.pack.group.AddGroupMemberPack;
import com.lld.im.codec.pack.group.RemoveGroupMemberPack;
import com.lld.im.codec.pack.group.UpdateGroupMemberPack;
import com.lld.im.common.ClientType;
import com.lld.im.common.command.Command;
import com.lld.im.common.command.GroupEventCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.service.group.model.req.GroupMemberDto;
import com.lld.im.service.group.service.ImGroupMemberService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class GroupMessageProducer {

    @Resource
    private MessageProducer messageProducer;

    @Resource
    private ImGroupMemberService imGroupMemberService;

    public void producer(String userId, Command command, Object data, ClientInfo clientInfo){
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(data);
        String groupId = jsonObject.getString("groupId");

        if(command.equals(GroupEventCommand.ADDED_MEMBER)){
            // 新增群成员-发送给管理员和被加入人本身
            List<String> managerUserIdList = imGroupMemberService.getGroupManager(groupId, clientInfo.getAppId())
                    .stream().map(GroupMemberDto::getMemberId).collect(Collectors.toList());

            AddGroupMemberPack addGroupMemberPack = jsonObject.toJavaObject(AddGroupMemberPack.class);
            List<String> memberUserIdList = addGroupMemberPack.getMembers();

            loopSendMsgToUser(userId, command, data, clientInfo,
                    CollectionUtil.addAll(managerUserIdList, memberUserIdList));
        }else if(command.equals(GroupEventCommand.DELETED_MEMBER)){
            // 踢人-发送给群内的群成员和被踢的人
            RemoveGroupMemberPack pack = jsonObject.toJavaObject(RemoveGroupMemberPack.class);
            String kickMemberUserId = pack.getMember();

            List<String> memberUserIdList = imGroupMemberService.getGroupMemberId(groupId, clientInfo.getAppId());
            memberUserIdList.add(kickMemberUserId);

            loopSendMsgToUser(userId, command, data, clientInfo, memberUserIdList);
        }else if(command.equals(GroupEventCommand.UPDATED_MEMBER)){
            // 更新群成员资料-发送给管理员和修改人本身
            UpdateGroupMemberPack pack = jsonObject.toJavaObject(UpdateGroupMemberPack.class);
            String updateMemberUserId = pack.getMemberId();

            List<String> managerUserIdList = imGroupMemberService.getGroupManager(groupId, clientInfo.getAppId())
                    .stream().map(GroupMemberDto::getMemberId).collect(Collectors.toList());
            managerUserIdList.add(updateMemberUserId);

            loopSendMsgToUser(userId, command, data, clientInfo, managerUserIdList);
        }else {
            // 其他command默认发送给所有群成员
            List<String> memberUserIdList = imGroupMemberService.getGroupMemberId(groupId, clientInfo.getAppId());
            loopSendMsgToUser(userId, command, data, clientInfo, memberUserIdList);
        }
    }

    private void loopSendMsgToUser(String userId, Command command, Object data,
                                   ClientInfo clientInfo, Collection<String> userIdList) {
        for (String member : userIdList) {
            if(Objects.nonNull(clientInfo.getClientType())
                    && !Objects.equals(ClientType.WEBAPI.getCode(), clientInfo.getClientType())
                    && member.equals(userId)){
                messageProducer.sendToUserExceptClient(member,command,data,clientInfo);
            }else{
                messageProducer.sendToUser(member,command,data,clientInfo.getAppId());
            }
        }
    }

}
