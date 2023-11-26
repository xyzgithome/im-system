package com.lld.im.service.friendship.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.lld.im.service.friendship.model.req.AddFriendShipGroupReq;
import com.lld.im.service.friendship.model.req.DeleteFriendShipGroupMemberReq;
import com.lld.im.service.friendship.model.req.DeleteFriendShipGroupReq;
import com.lld.im.service.friendship.service.ImFriendShipGroupMemberService;
import com.lld.im.service.friendship.service.ImFriendShipGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author: Chackylee
 * @description:
 **/
@RestController
@RequestMapping("v1/friendship/group")
public class ImFriendShipGroupController {

    @Resource
    private ImFriendShipGroupService imFriendShipGroupService;

    @Resource
    private ImFriendShipGroupMemberService imFriendShipGroupMemberService;


    @PostMapping("/add")
    public ResponseVO add(@RequestBody @Validated AddFriendShipGroupReq req)  {
        
        return imFriendShipGroupService.addGroup(req);
    }

    @PostMapping("/del")
    public ResponseVO del(@RequestBody @Validated DeleteFriendShipGroupReq req)  {
        
        return imFriendShipGroupService.deleteGroup(req);
    }

    @PostMapping("/member/add")
    public ResponseVO memberAdd(@RequestBody @Validated AddFriendShipGroupMemberReq req)  {
        
        return imFriendShipGroupMemberService.addGroupMember(req);
    }

    @PostMapping("/member/del")
    public ResponseVO memberdel(@RequestBody @Validated DeleteFriendShipGroupMemberReq req)  {
        
        return imFriendShipGroupMemberService.delGroupMember(req);
    }


}
