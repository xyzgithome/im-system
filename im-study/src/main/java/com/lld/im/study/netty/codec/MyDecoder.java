package com.lld.im.study.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;
import java.util.List;

public class MyDecoder extends ByteToMessageDecoder {
    /**
     * 数据长度 + 数据
     *
     * @param channelHandlerContext
     * @param in
     * @param out
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        // int字节数大于4
        if (in.readableBytes() < 4) {
            return;
        }

        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }

        byte[] data = new byte[dataLength];

        in.readBytes(data);
        System.out.println(new String(data, CharsetUtil.UTF_8));
        in.markReaderIndex();
    }
}
