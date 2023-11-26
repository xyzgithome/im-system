package com.lld.im.study.netty.upload;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

public class UploadFileDecoder extends ByteToMessageDecoder {
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
        FileDto fileDto = new FileDto();

        // command 4个字节 fileNameLength 4个字节，总共大于等于8个字节
        if (in.readableBytes() < 8) {
            return;
        }

        int command = in.readInt();
        fileDto.setCommand(command);

        int fileNameLen = in.readInt();

        if (in.readableBytes() < fileNameLen) {
            in.resetReaderIndex();
            return;
        }

        byte[] data = new byte[fileNameLen];

        in.readBytes(data);

        String fileName = new String(data, CharsetUtil.UTF_8);
        fileDto.setFileName(fileName);

        // command =2  读文件数据
        if (command == 2) {
            int dataLen = in.readInt();

            if (in.readableBytes() < dataLen) {
                in.resetReaderIndex();
                return;
            }

            byte[] fileData = new byte[dataLen];
            in.readBytes(fileData);
            fileDto.setBytes(fileData);
        }
        in.markReaderIndex();

        out.add(fileDto);
    }
}
