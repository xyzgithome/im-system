package com.lld.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lld.im.codec.pack.friendship.AddFriendRequestPack;
import com.lld.im.codec.pack.friendship.ApproverFriendRequestPack;
import com.lld.im.codec.pack.friendship.ReadAllFriendRequestPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.command.FriendshipEventCommand;
import com.lld.im.common.enums.ApproverFriendRequestStatusEnum;
import com.lld.im.common.enums.FriendShipErrorCode;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.service.friendship.dao.ImFriendShipRequestEntity;
import com.lld.im.service.friendship.dao.mapper.ImFriendShipRequestMapper;
import com.lld.im.service.friendship.model.req.AddFriendReq;
import com.lld.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.lld.im.service.friendship.model.req.FriendDto;
import com.lld.im.service.friendship.model.req.ReadFriendShipRequestReq;
import com.lld.im.service.friendship.service.ImFriendService;
import com.lld.im.service.friendship.service.ImFriendShipRequestService;
import com.lld.im.service.utils.MessageProducer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


@Service
public class ImFriendShipRequestServiceImpl implements ImFriendShipRequestService {

    @Resource
    private ImFriendShipRequestMapper imFriendShipRequestMapper;

    @Resource
    private ImFriendService imFriendShipService;

    @Resource
    private MessageProducer messageProducer;

    @Override
    public ResponseVO getFriendRequest(String fromId, Integer appId) {
        QueryWrapper<ImFriendShipRequestEntity> query = new QueryWrapper<>();
        query.eq("app_id", appId);
        query.eq("to_id", fromId);

        List<ImFriendShipRequestEntity> requestList = imFriendShipRequestMapper.selectList(query);

        return ResponseVO.successResponse(requestList);
    }

    @Override
    public ResponseVO addFriendshipRequest(String fromId, FriendDto toItem, Integer appId) {
        QueryWrapper<ImFriendShipRequestEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", appId);
        queryWrapper.eq("from_id", fromId);
        queryWrapper.eq("to_id", toItem.getToId());

        ImFriendShipRequestEntity request = imFriendShipRequestMapper.selectOne(queryWrapper);
        if (request == null) {
            request = new ImFriendShipRequestEntity();
            request.setAddSource(toItem.getAddSource());
            request.setAddWording(toItem.getAddWording());
            request.setAppId(appId);
            request.setFromId(fromId);
            request.setToId(toItem.getToId());
            request.setReadStatus(0);
            request.setApproveStatus(0);
            request.setRemark(toItem.getRemark());
            request.setCreateTime(System.currentTimeMillis());
            imFriendShipRequestMapper.insert(request);
            return ResponseVO.success();
        }

        //修改记录内容 和更新时间
        if (StringUtils.isNotBlank(toItem.getAddSource())) {
            request.setAddWording(toItem.getAddWording());
        }
        if (StringUtils.isNotBlank(toItem.getRemark())) {
            request.setRemark(toItem.getRemark());
        }
        if (StringUtils.isNotBlank(toItem.getAddWording())) {
            request.setAddWording(toItem.getAddWording());
        }
        request.setApproveStatus(0);
        request.setReadStatus(0);
        imFriendShipRequestMapper.updateById(request);

        // 添加好友请求-多端数据同步
        // A加B好友，用户B开启好友请求验证，不用同步数据给A除去本端的其他端，需要同步数据给B的所有端
        AddFriendRequestPack addFriendRequestPack = new AddFriendRequestPack();
        BeanUtils.copyProperties(request, addFriendRequestPack);
        messageProducer.sendToUser(toItem.getToId(),
                FriendshipEventCommand.FRIEND_REQUEST, addFriendRequestPack, appId);

        return ResponseVO.success();
    }

    @Override
    @Transactional
    public ResponseVO approveFriendRequest(ApproveFriendRequestReq req) {
        ImFriendShipRequestEntity imFriendShipRequestEntity = imFriendShipRequestMapper.selectById(req.getId());
        if (imFriendShipRequestEntity == null) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_REQUEST_IS_NOT_EXIST);
        }

        if (!req.getOperator().equals(imFriendShipRequestEntity.getToId())) {
            //只能审批发给自己的好友请求
            throw new ApplicationException(FriendShipErrorCode.NOT_APPROVER_OTHER_MAN_REQUEST);
        }

        ImFriendShipRequestEntity friendShipRequestEntity = new ImFriendShipRequestEntity();
        friendShipRequestEntity.setApproveStatus(req.getStatus());
        friendShipRequestEntity.setUpdateTime(System.currentTimeMillis());
        friendShipRequestEntity.setId(req.getId());
        imFriendShipRequestMapper.updateById(friendShipRequestEntity);

        if (ApproverFriendRequestStatusEnum.AGREE.getCode() == req.getStatus()) {
            //同意 ===> 去执行添加好友逻辑
            FriendDto dto = new FriendDto();
            dto.setAddSource(imFriendShipRequestEntity.getAddSource());
            dto.setAddWording(imFriendShipRequestEntity.getAddWording());
            dto.setRemark(imFriendShipRequestEntity.getRemark());
            dto.setToId(imFriendShipRequestEntity.getToId());

            AddFriendReq addFriendReq = new AddFriendReq();
            addFriendReq.setFromId(imFriendShipRequestEntity.getFromId());
            addFriendReq.setToItem(dto);
            addFriendReq.setAppId(req.getAppId());
            addFriendReq.setClientType(req.getClientType());
            addFriendReq.setImei(req.getImei());

            ResponseVO responseVO = imFriendShipService.doAddFriend(addFriendReq);

            if (!responseVO.isOk() && responseVO.getCode() != FriendShipErrorCode.TO_IS_YOUR_FRIEND.getCode()) {
                return responseVO;
            }
        }

        // 审核好友申请-多端数据同步
        // B审核A的好友申请，需要通知用户B除去本端的其他客户端
        ApproverFriendRequestPack approverFriendRequestPack = new ApproverFriendRequestPack();
        BeanUtils.copyProperties(req, approverFriendRequestPack);
        messageProducer.sendToUser(imFriendShipRequestEntity.getToId(), req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_REQUEST_APPROVER, approverFriendRequestPack, req.getAppId());

        return ResponseVO.success();
    }

    @Override
    public ResponseVO readFriendShipRequestReq(ReadFriendShipRequestReq req) {
        QueryWrapper<ImFriendShipRequestEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("to_id", req.getFromId());

        ImFriendShipRequestEntity update = new ImFriendShipRequestEntity();
        update.setReadStatus(1);
        imFriendShipRequestMapper.update(update, query);

        // 已读所有的好友申请-多端数据同步
        // 已读所有的好友申请，需要通知该用户除去本端的其他客户端
        ReadAllFriendRequestPack readAllFriendRequestPack = new ReadAllFriendRequestPack();
        BeanUtils.copyProperties(req, readAllFriendRequestPack);
        messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_REQUEST_READ, readAllFriendRequestPack, req.getAppId());

        return ResponseVO.success();
    }

}
