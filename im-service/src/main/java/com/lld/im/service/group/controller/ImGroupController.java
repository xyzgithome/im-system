package com.lld.im.service.group.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.group.model.req.*;
import com.lld.im.service.group.service.ImGroupService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@RestController
@RequestMapping("v1/group")
public class ImGroupController {

    @Resource
    private ImGroupService groupService;

    @PostMapping("/importGroup")
    @ApiOperation("导入群-单个导入")
    public ResponseVO importGroup(@RequestBody @Validated ImportGroupReq req)  {
        return groupService.importGroup(req);
    }

    @PostMapping("/createGroup")
    @ApiOperation("创建群")
    public ResponseVO createGroup(@RequestBody @Validated CreateGroupReq req)  {
        return groupService.createGroup(req);
    }

    @PostMapping("/getGroupInfo")
    @ApiOperation("获取群信息")
    public ResponseVO getGroupInfo(@RequestBody @Validated GetGroupReq req)  {
        return groupService.getGroup(req);
    }

    @PostMapping("/update")
    @ApiOperation("更新群信息")
    public ResponseVO update(@RequestBody @Validated UpdateGroupReq req)  {
        return groupService.updateBaseGroupInfo(req);
    }

    @PostMapping("/getJoinedGroup")
    @ApiOperation("获取用户已经加入的群组列表")
    public ResponseVO getJoinedGroup(@RequestBody @Validated GetJoinedGroupReq req)  {
        return groupService.getJoinedGroup(req);
    }


    @PostMapping("/destroyGroup")
    @ApiOperation("解散群组")
    public ResponseVO destroyGroup(@RequestBody @Validated DestroyGroupReq req)  {
        return groupService.destroyGroup(req);
    }

    @PostMapping("/transferGroup")
    @ApiOperation("转让群组")
    public ResponseVO transferGroup(@RequestBody @Validated TransferGroupReq req)  {
        return groupService.transferGroup(req);
    }

    @PostMapping("/forbidSendMessage")
    @ApiOperation("设置群禁言")
    public ResponseVO forbidSendMessage(@RequestBody @Validated MuteGroupReq req)  {
        return groupService.muteGroup(req);
    }

}
