package com.lld.im.service.group.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.group.model.req.*;
import com.lld.im.service.group.service.ImGroupMemberService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("v1/group/member")
public class ImGroupMemberController {

    @Resource
    private ImGroupMemberService groupMemberService;

    @PostMapping("/importGroupMember")
    @ApiOperation("导入群成员")
    public ResponseVO importGroupMember(@RequestBody @Validated ImportGroupMemberReq req)  {
        return groupMemberService.importGroupMember(req);
    }

    @PostMapping("/add")
    @ApiOperation("拉人入群")
    public ResponseVO addMember(@RequestBody @Validated AddGroupMemberReq req)  {
        return groupMemberService.addMember(req);
    }

    @PostMapping("/remove")
    @ApiOperation("踢人出群")
    public ResponseVO removeMember(@RequestBody @Validated RemoveGroupMemberReq req)  {
        return groupMemberService.removeMember(req);
    }

    @PostMapping("/update")
    @ApiOperation("修改群成员信息")
    public ResponseVO updateGroupMember(@RequestBody @Validated UpdateGroupMemberReq req)  {
        return groupMemberService.updateGroupMember(req);
    }

    @PostMapping("/speak")
    @ApiOperation("禁言群成员")
    public ResponseVO speak(@RequestBody @Validated SpeaMemberReq req)  {
        return groupMemberService.speak(req);
    }

    @PostMapping
    @ApiOperation("退出群聊")
    public ResponseVO exitGroup(@RequestBody @Validated ExitGroupReq req)  {
        return groupMemberService.exitGroup(req);
    }

}
