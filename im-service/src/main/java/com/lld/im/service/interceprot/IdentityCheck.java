package com.lld.im.service.interceprot;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.common.BaseErrorCode;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.GateWayErrorCode;
import com.lld.im.common.exception.ApplicationExceptionEnum;
import com.lld.im.common.utils.SigAPI;
import com.lld.im.service.user.service.ImUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class IdentityCheck {
    @Resource
    private ImUserService imUserService;

    /***
     * 使用配置文件的方式替代用表记录appId和其对应的接口加密密钥key的关系
     */
    @Resource
    private AppConfig appConfig;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public ApplicationExceptionEnum checkUserSig(String identifier, String appId, String userSig) {
        String userSignExpireTimeKey = appId + ":" + Constants.RedisConstants.userSign + ":" + identifier + userSig;
        String userSignExpireTimeValue = stringRedisTemplate.opsForValue().get(userSignExpireTimeKey);
        if (StringUtils.isNotBlank(userSignExpireTimeValue)
                && Long.parseLong(userSignExpireTimeValue) > System.currentTimeMillis() / 1000) {
//            this.setIsAdmin(identifier, Integer.valueOf(appId));
            return BaseErrorCode.SUCCESS;
        }

        //获取秘钥
        String privateKey = appConfig.getPrivateKey();

        //根据appId + 秘钥创建sigApi
        SigAPI sigAPI = new SigAPI(Long.parseLong(appId), privateKey);

        //调用sigApi对userSig解密
        JSONObject jsonObject = sigAPI.decodeUserSig(userSig);

        //取出解密后的appid 和 操作人 和 过期时间做匹配，不通过则提示错误
        long expireTime = 0L;
        long expireSec = 0L;
        long time = 0L;
        String decoerAppId = "";
        String decoderidentifier = "";
        try {
            // 应用id
            decoerAppId = jsonObject.getString("TLS.appId");

            // 用户身份标识 项目中指userId
            decoderidentifier = jsonObject.getString("TLS.identifier");

            // 密钥生成时的时间戳 s
            String expireTimeStr = jsonObject.get("TLS.expireTime").toString();

            // 密钥多久失效时间 s
            time = Long.parseLong(expireTimeStr);
            expireSec = Long.parseLong(jsonObject.get("TLS.expire").toString());

            // 密钥过期时间戳
            expireTime = time + expireSec;
        } catch (Exception e) {
            log.error("checkUserSig-error:{}", e.getMessage());
        }

        if (!decoderidentifier.equals(identifier)) {
            return GateWayErrorCode.USERSIGN_OPERATE_NOT_MATE;
        }

        if (!decoerAppId.equals(appId)) {
            return GateWayErrorCode.USERSIGN_IS_ERROR;
        }

        if (expireSec == 0L) {
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }

        if (expireTime < System.currentTimeMillis() / 1000) {
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }

        //appId + "xxx" + userId + sign
        String genSig = sigAPI.genUserSig(identifier, expireSec, time, null);
        if (!StringUtils.equalsIgnoreCase(genSig, userSig)) {
            return GateWayErrorCode.USERSIGN_IS_ERROR;
        }

        stringRedisTemplate.opsForValue().set(userSignExpireTimeKey, Long.toString(expireTime),
                expireTime - System.currentTimeMillis() / 1000, TimeUnit.SECONDS);
//        this.setIsAdmin(identifier, Integer.valueOf(appId));
        return BaseErrorCode.SUCCESS;
    }


    /**
     * 根据appid,identifier判断是否App管理员,并设置到RequestHolder
     *
     * @param identifier
     * @param appId
     * @return
     */
//    public void setIsAdmin(String identifier, Integer appId) {
//        //去DB或Redis中查找, 后面写
//        ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(identifier, appId);
//        if (singleUserInfo.isOk()) {
//            RequestHolder.set(singleUserInfo.getData().getUserType() == ImUserTypeEnum.APP_ADMIN.getCode());
//        } else {
//            RequestHolder.set(false);
//        }
//    }
}
