package com.lld.im.tcp.receiver;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.proto.MessagePack;
import com.lld.im.common.ClientType;
import com.lld.im.common.command.SystemCommand;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.DeviceMultiLoginEnum;
import com.lld.im.common.model.UserClientDto;
import com.lld.im.tcp.redis.RedisManager;
import com.lld.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;

import java.util.List;

/**
 * 多端登录：
 * 1单端登录：仅可有一种平台在线，也即踢掉除了本clientType+imel 的设备
 * 2双端登录：移动或者桌面平台可有一种平台在线+web可同时在线，也即允许pc/mobile 其中一端登录 + web端 踢掉除了本clinetType + imel 以外的web端设备
 * 3 三端登录：允许mobile+pc+web，踢掉同端的其他imei 除了web
 * 4 不做任何处理
 */
@Slf4j
public class UserLoginMessageListener {
    private Integer loginModel;

    public UserLoginMessageListener(Integer loginModel) {
        this.loginModel = loginModel;
    }

    public void listenerUserLogin(){
        RTopic topic = RedisManager.getRedissonClient().getTopic(Constants.RedisConstants.UserLoginChannel);
        topic.addListener(String.class, (charSequence, msg) -> {
            log.info("收到用户上线通知：" + msg);
            UserClientDto dto = JSONObject.parseObject(msg, UserClientDto.class);
            List<NioSocketChannel> nioSocketChannels = SessionSocketHolder.get(dto.getAppId(), dto.getUserId());

            for (NioSocketChannel nioSocketChannel : nioSocketChannels) {
                Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();

                if(loginModel == DeviceMultiLoginEnum.ONE.getLoginMode()){
                    // 单端登录: 踢掉除了本clientType+imel 的设备
                    if(!(clientType + ":" + imei).equals(dto.getClientType()+":"+dto.getImei())){
                        kickClient(nioSocketChannel);
                    }
                }else if(loginModel == DeviceMultiLoginEnum.TWO.getLoginMode()){
                    // 如果是web端，不做处理，因为双端登录 支持web端多设备登录
                    if(dto.getClientType() == ClientType.WEB.getCode()){
                        continue;
                    }
                    if (clientType == ClientType.WEB.getCode()){
                        continue;
                    }
                    // windows和mobile: 踢掉除了本clientType+imel 的设备
                    if(!(clientType + ":" + imei).equals(dto.getClientType()+":"+dto.getImei())){
                        kickClient(nioSocketChannel);
                    }
                }else if(loginModel == DeviceMultiLoginEnum.THREE.getLoginMode()){
                    // 如果是web端，不做处理，因为双端登录 支持web端多设备登录
                    if(dto.getClientType() == ClientType.WEB.getCode()){
                        continue;
                    }
                    if (clientType == ClientType.WEB.getCode()){
                        continue;
                    }

                    boolean isSameClient = false;
                    if((clientType == ClientType.IOS.getCode() ||
                            clientType == ClientType.ANDROID.getCode()) &&
                            (dto.getClientType() == ClientType.IOS.getCode() ||
                                    dto.getClientType() == ClientType.ANDROID.getCode())){
                        isSameClient = true;
                    }

                    if((clientType == ClientType.MAC.getCode() ||
                            clientType == ClientType.WINDOWS.getCode()) &&
                            (dto.getClientType() == ClientType.MAC.getCode() ||
                                    dto.getClientType() == ClientType.WINDOWS.getCode())){
                        isSameClient = true;
                    }

                    if(isSameClient && !(clientType + ":" + imei).equals(dto.getClientType()+":"+dto.getImei())){
                        kickClient(nioSocketChannel);
                    }
                }
            }
        });
    }

    /**
     * 服务端只会在心跳超时、客户端退出登录会主动关闭客户端连接，其他情况不会主动关闭客户端连接
     * 所以在此处多端登录提出客户端我们选择的策略就是 告诉客户端，有其他设备登录，由客户端选择断开连接，还是其他操作
     *
     * 踢掉客户端
     *
     * @param nioSocketChannel channel
     */
    private void kickClient(NioSocketChannel nioSocketChannel) {
        MessagePack<Object> pack = new MessagePack<>();
        pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
        pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
        pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
        nioSocketChannel.writeAndFlush(pack);
    }
}
