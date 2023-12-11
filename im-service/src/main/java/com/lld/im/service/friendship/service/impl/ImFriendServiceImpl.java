package com.lld.im.service.friendship.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lld.im.codec.pack.friendship.*;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.command.FriendshipEventCommand;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.AllowFriendTypeEnum;
import com.lld.im.common.enums.CheckFriendShipTypeEnum;
import com.lld.im.common.enums.FriendShipErrorCode;
import com.lld.im.common.enums.FriendShipStatusEnum;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.common.model.RequestBase;
import com.lld.im.service.friendship.dao.ImFriendShipEntity;
import com.lld.im.service.friendship.dao.mapper.ImFriendShipMapper;
import com.lld.im.service.friendship.model.callback.AddFriendAfterCallbackDto;
import com.lld.im.service.friendship.model.req.*;
import com.lld.im.service.friendship.model.resp.CheckFriendShipResp;
import com.lld.im.service.friendship.model.resp.ImportFriendShipResp;
import com.lld.im.service.friendship.service.ImFriendService;
import com.lld.im.service.friendship.service.ImFriendShipRequestService;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.utils.CallbackService;
import com.lld.im.service.utils.MessageProducer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Service
public class ImFriendServiceImpl implements ImFriendService {

    @Resource
    private ImFriendShipMapper imFriendShipMapper;

    @Resource
    private ImUserService imUserService;

    @Resource
    private AppConfig appConfig;

    @Resource
    private CallbackService callbackService;

    @Resource
    private ImFriendShipRequestService imFriendShipRequestService;

    @Resource
    private MessageProducer messageProducer;

    @Override
    public ResponseVO importFriendShip(ImportFriendShipReq req) {

        if (req.getFriendItem().size() > 100) {
            return ResponseVO.fail(FriendShipErrorCode.IMPORT_SIZE_BEYOND);
        }
        ImportFriendShipResp resp = new ImportFriendShipResp();
        List<String> successId = new ArrayList<>();
        List<String> errorId = new ArrayList<>();

        for (ImportFriendShipReq.ImportFriendDto dto :
                req.getFriendItem()) {
            ImFriendShipEntity entity = new ImFriendShipEntity();
            BeanUtils.copyProperties(dto, entity);
            entity.setAppId(req.getAppId());
            entity.setFromId(req.getFromId());
            try {
                int insert = imFriendShipMapper.insert(entity);
                if (insert == 1) {
                    successId.add(dto.getToId());
                } else {
                    errorId.add(dto.getToId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorId.add(dto.getToId());
            }

        }

        resp.setErrorId(errorId);
        resp.setSuccessId(successId);

        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO addFriend(AddFriendReq req) {
        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }

        // 添加好友之前回调
        if (appConfig.isAddFriendBeforeCallback()) {
            ResponseVO responseVO = callbackService.beforeCallback(req.getAppId(),
                    Constants.CallbackCommand.AddFriendBefore, JSONObject.toJSONString(req));
            if (!responseVO.isOk()) {
                return responseVO;
            }
        }

        ImUserDataEntity data = toInfo.getData();

        // 没有开启好友验证，A可以直接加B为好友
        if (data.getFriendAllowType() != null && data.getFriendAllowType() == AllowFriendTypeEnum.NOT_NEED.getCode()) {
            return this.doAddFriend(req);
        } else {
            // 用户B开启好友请求验证，不用多端同步数据给A的其他端，需要同步数据给B的所有端
            QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
            query.eq("app_id", req.getAppId());
            query.eq("from_id", req.getFromId());
            query.eq("to_id", req.getToItem().getToId());
            ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
            if (fromItem == null
                    || fromItem.getStatus() != FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                //插入一条好友申请的数据
                ResponseVO responseVO = imFriendShipRequestService
                        .addFriendshipRequest(req.getFromId(), req.getToItem(), req.getAppId());
                if (!responseVO.isOk()) {
                    return responseVO;
                }
            } else {
                return ResponseVO.fail(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            }
        }
        return ResponseVO.success();
    }

    @Override
    public ResponseVO updateFriend(UpdateFriendReq req) {
        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }

        return this.doUpdate(req);
    }

    @Transactional
    public ResponseVO doUpdate(UpdateFriendReq req) {
        String fromId = req.getFromId();
        Integer appId = req.getAppId();
        FriendDto toItem = req.getToItem();

        UpdateWrapper<ImFriendShipEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(ImFriendShipEntity::getAddSource, toItem.getAddSource())
                .set(ImFriendShipEntity::getExtra, toItem.getExtra())
                .set(ImFriendShipEntity::getRemark, toItem.getRemark())
                .eq(ImFriendShipEntity::getAppId, appId)
                .eq(ImFriendShipEntity::getToId, toItem.getToId())
                .eq(ImFriendShipEntity::getFromId, fromId);

        int update = imFriendShipMapper.update(null, updateWrapper);
        if (update != 1) {
            return ResponseVO.fail();
        }

        // 更新好友-多端数据同步
        // A更新B好友信息，需要通知用户A除去本端的其他客户端
        UpdateFriendPack updateFriendPack = new UpdateFriendPack();
        BeanUtils.copyProperties(toItem, updateFriendPack);
        messageProducer.sendToUser(fromId, req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_UPDATE, updateFriendPack, appId);

        // 更新好友后回调
        if (appConfig.isModifyFriendAfterCallback()) {
            callbackService.afterCallback(appId, Constants.CallbackCommand.UpdateFriendAfter,
                    JSONObject.toJSONString(new AddFriendAfterCallbackDto().setFromId(fromId).setToItem(toItem)));
        }
        return ResponseVO.success();
    }

    @Override
    @Transactional
    public ResponseVO doAddFriend(AddFriendReq req) {
        Integer appId = req.getAppId();
        String fromId = req.getFromId();
        FriendDto dto = req.getToItem();
        String toId = dto.getToId();

        //A-B
        //Friend表插入A 和 B 两条记录
        //查询是否有记录存在，如果存在则判断状态，如果是已添加，则提示已添加，如果是未添加，则修改状态
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", appId);
        query.eq("from_id", fromId);
        query.eq("to_id", toId);
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        if (fromItem == null) {
            // 走添加逻辑
            fromItem = new ImFriendShipEntity();
            fromItem.setAppId(appId);
            fromItem.setFromId(fromId);
            BeanUtils.copyProperties(dto, fromItem);
            fromItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
            int insert = imFriendShipMapper.insert(fromItem);
            if (insert != 1) {
                return ResponseVO.fail(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }
        } else {
            // 如果存在则判断状态，如果是已添加，则提示已添加，如果是未添加，则修改状态
            if (fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                return ResponseVO.fail(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            } else {
                ImFriendShipEntity update = new ImFriendShipEntity();

                if (StringUtils.isNotBlank(dto.getAddSource())) {
                    update.setAddSource(dto.getAddSource());
                }

                if (StringUtils.isNotBlank(dto.getRemark())) {
                    update.setRemark(dto.getRemark());
                }

                if (StringUtils.isNotBlank(dto.getExtra())) {
                    update.setExtra(dto.getExtra());
                }
                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

                int result = imFriendShipMapper.update(update, query);
                if (result != 1) {
                    return ResponseVO.fail(FriendShipErrorCode.ADD_FRIEND_ERROR);
                }
            }
        }

        QueryWrapper<ImFriendShipEntity> toQuery = new QueryWrapper<>();
        toQuery.eq("app_id", appId);
        toQuery.eq("from_id", toId);
        toQuery.eq("to_id", fromId);
        ImFriendShipEntity toItem = imFriendShipMapper.selectOne(toQuery);
        if (toItem == null) {
            toItem = new ImFriendShipEntity();
            toItem.setAppId(appId);
            toItem.setFromId(toId);
            BeanUtils.copyProperties(dto, toItem);
            toItem.setToId(fromId);
            toItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            toItem.setCreateTime(System.currentTimeMillis());
            toItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
            imFriendShipMapper.insert(toItem);
        } else {
            if (FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() != toItem.getStatus()) {
                ImFriendShipEntity update = new ImFriendShipEntity();
                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
                imFriendShipMapper.update(update, toQuery);
            }
        }

        // 添加好友-多端数据同步
        // A添加B为好友，需要通知用户A除去本端的其他客户端
        AddFriendPack addFriendPackFrom = new AddFriendPack();
        BeanUtils.copyProperties(fromItem, addFriendPackFrom);
        messageProducer.sendToUser(fromId, req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_ADD, addFriendPackFrom, appId);

        // 需要通知用户B的所有客户端
        AddFriendPack addFriendPackTo = new AddFriendPack();
        BeanUtils.copyProperties(toItem, addFriendPackTo);
        messageProducer.sendToUser(toId,
                FriendshipEventCommand.FRIEND_ADD, addFriendPackTo, appId);

        // 添加好友后回调
        if (appConfig.isAddFriendAfterCallback()) {
            callbackService.afterCallback(appId, Constants.CallbackCommand.AddFriendAfter,
                    JSONObject.toJSONString(new AddFriendAfterCallbackDto().setFromId(fromId).setToItem(dto)));
        }

        return ResponseVO.success();
    }


    @Override
    public ResponseVO deleteFriend(DeleteFriendReq req) {
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("to_id", req.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        if (fromItem == null) {
            return ResponseVO.fail(FriendShipErrorCode.TO_IS_NOT_YOUR_FRIEND);
        }

        if (Objects.isNull(fromItem.getStatus())
                || Objects.equals(fromItem.getStatus(), FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode())) {
            return ResponseVO.fail(FriendShipErrorCode.FRIEND_IS_DELETED);
        }

        ImFriendShipEntity update = new ImFriendShipEntity();
        update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
        imFriendShipMapper.update(update, query);

        // 删除好友-多端数据同步
        // A删除B，需要通知用户A除去本端的其他客户端
        DeleteFriendPack deleteFriendPack = new DeleteFriendPack();
        BeanUtils.copyProperties(req, deleteFriendPack);
        messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_DELETE, deleteFriendPack, req.getAppId());

        // 删除好友后回调
        if (appConfig.isDeleteFriendAfterCallback()) {
            callbackService.afterCallback(req.getAppId(),
                    Constants.CallbackCommand.DeleteFriendAfter,
                    JSONObject.toJSONString(req));
        }
        return ResponseVO.success();
    }

    @Override
    public ResponseVO deleteAllFriend(DeleteFriendReq req) {
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("status", FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

        ImFriendShipEntity update = new ImFriendShipEntity();
        update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
        imFriendShipMapper.update(update, query);

        // 删除所有好友-多端数据同步
        // A删除B C D，需要通知用户A除去本端的其他客户端
        DeleteAllFriendPack deleteAllFriendPack = new DeleteAllFriendPack();
        BeanUtils.copyProperties(req, deleteAllFriendPack);
        messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_ALL_DELETE, deleteAllFriendPack, req.getAppId());

        return ResponseVO.success();
    }

    @Override
    public ResponseVO getAllFriendShip(GetAllFriendShipReq req) {
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        return ResponseVO.successResponse(imFriendShipMapper.selectList(query));
    }

    @Override
    public ResponseVO getRelation(GetRelationReq req) {

        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("to_id", req.getToId());

        ImFriendShipEntity entity = imFriendShipMapper.selectOne(query);
        if (entity == null) {
            return ResponseVO.fail(FriendShipErrorCode.REPEATSHIP_IS_NOT_EXIST);
        }
        return ResponseVO.successResponse(entity);
    }

    @Override
    public ResponseVO checkBlack(CheckFriendShipReq req) {

        Map<String, Integer> toIdMap
                = req.getToIds().stream().collect(Collectors
                .toMap(Function.identity(), s -> 0));
        List<CheckFriendShipResp> result;
        if (req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            result = imFriendShipMapper.checkFriendShipBlack(req);
        } else {
            result = imFriendShipMapper.checkFriendShipBlackBoth(req);
        }

        Map<String, Integer> collect = result.stream()
                .collect(Collectors
                        .toMap(CheckFriendShipResp::getToId,
                                CheckFriendShipResp::getStatus));
        for (String toId : toIdMap.keySet()) {
            if (!collect.containsKey(toId)) {
                CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                checkFriendShipResp.setToId(toId);
                checkFriendShipResp.setFromId(req.getFromId());
                checkFriendShipResp.setStatus(toIdMap.get(toId));
                result.add(checkFriendShipResp);
            }
        }

        return ResponseVO.successResponse(result);
    }


    @Override
    public ResponseVO addBlack(AddFriendShipBlackReq req) {
        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("to_id", req.getToId());

        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        if (fromItem == null) {
            //走添加逻辑。
            fromItem = new ImFriendShipEntity();
            fromItem.setFromId(req.getFromId());
            fromItem.setToId(req.getToId());
            fromItem.setAppId(req.getAppId());
            fromItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
            int insert = imFriendShipMapper.insert(fromItem);
            if (insert != 1) {
                return ResponseVO.fail(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }

        } else {
            //如果存在则判断状态，如果是拉黑，则提示已拉黑，如果是未拉黑，则修改状态
            if (fromItem.getBlack() != null && fromItem.getBlack() == FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode()) {
                return ResponseVO.fail(FriendShipErrorCode.FRIEND_IS_BLACK);
            } else {
                ImFriendShipEntity update = new ImFriendShipEntity();
                update.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
                int result = imFriendShipMapper.update(update, query);
                if (result != 1) {
                    return ResponseVO.fail(FriendShipErrorCode.ADD_BLACK_ERROR);
                }
            }
        }

        // 添加黑名单-多端数据同步
        // A添加B到黑名单，需要通知用户A除去本端的其他客户端
        AddFriendBlackPack addFriendBlackPack = new AddFriendBlackPack();
        BeanUtils.copyProperties(req, addFriendBlackPack);
        messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_BLACK_ADD, addFriendBlackPack, req.getAppId());

        // 新增黑名单后回调
        if (appConfig.isAddFriendShipBlackAfterCallback()) {
            callbackService.afterCallback(req.getAppId(),
                    Constants.CallbackCommand.AddBlackAfter,
                    JSONObject.toJSONString(req));
        }

        return ResponseVO.success();
    }

    @Override
    public ResponseVO deleteBlack(DeleteBlackReq req) {
        QueryWrapper<ImFriendShipEntity> queryFrom =
                new QueryWrapper<ImFriendShipEntity>().eq("from_id", req.getFromId())
                .eq("app_id", req.getAppId()).eq("to_id", req.getToId());

        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(queryFrom);
        if (fromItem.getBlack() != null && fromItem.getBlack() == FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_IS_NOT_YOUR_BLACK);
        }

        ImFriendShipEntity friendShipEntity = new ImFriendShipEntity();
        friendShipEntity.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
        int updateCount = imFriendShipMapper.update(friendShipEntity, queryFrom);
        if (updateCount != 1) {
            return ResponseVO.fail();
        }

        // 新增黑名单后回调
        if (appConfig.isDeleteFriendShipBlackAfterCallback()) {
            callbackService.afterCallback(req.getAppId(),
                    Constants.CallbackCommand.DeleteBlack,
                    JSONObject.toJSONString(req));
        }

        // 删除黑名单-多端数据同步
        // A从黑名单中将B删除，需要通知用户A除去本端的其他客户端
        DeleteBlackPack deleteBlackPack = new DeleteBlackPack();
        BeanUtils.copyProperties(req, deleteBlackPack);
        messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_BLACK_DELETE, deleteBlackPack, req.getAppId());

        return ResponseVO.success();
    }

    @Override
    public ResponseVO checkFriendship(CheckFriendShipReq req) {

        Map<String, Integer> result
                = req.getToIds().stream()
                .collect(Collectors.toMap(Function.identity(), s -> 0));

        List<CheckFriendShipResp> resp;

        if (req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            resp = imFriendShipMapper.checkFriendShip(req);
        } else {
            resp = imFriendShipMapper.checkFriendShipBoth(req);
        }

        Map<String, Integer> collect = resp.stream()
                .collect(Collectors.toMap(CheckFriendShipResp::getToId
                        , CheckFriendShipResp::getStatus));

        for (String toId : result.keySet()) {
            if (!collect.containsKey(toId)) {
                CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                checkFriendShipResp.setFromId(req.getFromId());
                checkFriendShipResp.setToId(toId);
                checkFriendShipResp.setStatus(result.get(toId));
                resp.add(checkFriendShipResp);
            }
        }

        return ResponseVO.successResponse(resp);
    }

}
