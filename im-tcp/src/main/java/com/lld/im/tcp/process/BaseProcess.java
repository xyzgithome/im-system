package com.lld.im.tcp.process;

import com.lld.im.codec.proto.MessagePack;
import com.lld.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Objects;

public abstract class BaseProcess {
    public abstract void processBefore();

    public void process(MessagePack messagePack){
        processBefore();
        NioSocketChannel nioSocketChannel = SessionSocketHolder.get(messagePack.getAppId(),
                messagePack.getToId(), messagePack.getClientType(), messagePack.getImei());

        if (Objects.nonNull(nioSocketChannel)) {
            nioSocketChannel.writeAndFlush(messagePack);
        }
        processAfter();
    }

    public abstract void processAfter();
}
