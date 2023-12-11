package com.lld.im.service.friendship.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.lld.im.service.friendship.model.req.FriendDto;
import com.lld.im.service.friendship.model.req.ReadFriendShipRequestReq;

/**
 * 好友申请
 */
public interface ImFriendShipRequestService {

    ResponseVO addFriendshipRequest(String fromId, FriendDto dto, Integer appId);

    /**
     * 审核好友申请
     *
     * @param req req
     * @return rsp
     */
    ResponseVO approveFriendRequest(ApproveFriendRequestReq req);

    /**
     * 已读所有的好友申请
     *
     *  @param req req
     * @return rsp
     */
    ResponseVO readFriendShipRequestReq(ReadFriendShipRequestReq req);

    ResponseVO getFriendRequest(String fromId, Integer appId);
}
