package com.lld.im.service.friendship.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.model.RequestBase;
import com.lld.im.service.friendship.model.req.*;

import java.util.List;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
public interface ImFriendService {

    ResponseVO importFriendShip(ImportFriendShipReq req);

    ResponseVO addFriend(AddFriendReq req);

    ResponseVO updateFriend(UpdateFriendReq req);

    ResponseVO deleteFriend(DeleteFriendReq req);

    ResponseVO deleteAllFriend(DeleteFriendReq req);

    ResponseVO getAllFriendShip(GetAllFriendShipReq req);

    ResponseVO getRelation(GetRelationReq req);

    ResponseVO doAddFriend(AddFriendReq req);

    ResponseVO checkFriendship(CheckFriendShipReq req);

    ResponseVO addBlack(AddFriendShipBlackReq req);

    ResponseVO deleteBlack(DeleteBlackReq req);

    ResponseVO checkBlack(CheckFriendShipReq req);

}
