package com.lld.im.codec;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.proto.Message;
import com.lld.im.codec.proto.MessageHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

/**
 * 定义私有协议：
 * 指令: HTTP GET POST PUT DELETE
 * 版本号: 1.0 1.1 2.0
 * clientType: IOS 安卓 pc(windows mac) web
 * 消息解析类型: 支持json 也支持 protobuf
 * imei长度(标识具体clientType的哪个客户端，比如IOS的iPhone15pro)
 * appId(标识具体接入IM系统的app)
 * bodylen(请求体长度)
 * 28 + imei + body
 * 请求头（指令 版本号 clientType 消息解析类型 imei长度 appId bodylen）+ imei号 + 请求体
 * len+body
 */
public class MessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 28) {
            return;
        }

        MessageHeader messageHeader = new MessageHeader();
        // 获取指令
        int command = in.readInt();
        messageHeader.setCommand(command);

        // 获取版本号
        int version = in.readInt();
        messageHeader.setVersion(version);

        int clientType = in.readInt();
        messageHeader.setClientType(clientType);

        int messageType = in.readInt();
        messageHeader.setMessageType(messageType);

        int appId = in.readInt();
        messageHeader.setAppId(appId);

        int imeiLength = in.readInt();
        messageHeader.setImeiLength(imeiLength);

        int bodyLen = in.readInt();
        messageHeader.setLength(bodyLen);

        // 解决粘包拆包问题
        if (in.readableBytes() < imeiLength + bodyLen) {
            in.resetReaderIndex();
            return;
        }

        byte[] imeiData = new byte[imeiLength];
        in.readBytes(imeiData);
        String imei = new String(imeiData, CharsetUtil.UTF_8);
        messageHeader.setImei(imei);

        Message message = new Message();
        // 设置消息头
        message.setMessageHeader(messageHeader);

        byte[] bodyData = new byte[bodyLen];
        in.readBytes(bodyData);
        String bodyStr = new String(bodyData, CharsetUtil.UTF_8);

        Object body = null;

        // 十六进制的0, 代表消息体格式是json
        if (messageType == 0x0) {
            body = JSONObject.parseObject(bodyStr);
        }
        // 设置消息体
        message.setMessagePack(body);

        // 更新读索引
        in.markReaderIndex();

        // 将message写入到管道pipeline中
        out.add(message);
    }
}
