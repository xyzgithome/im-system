package com.lld.im.study.netty.client;

import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class ChatClient {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 8001);
        OutputStream outputStream = socket.getOutputStream();

        String msg = "我是中国人123";

        byte[] bytes = msg.getBytes(CharsetUtil.UTF_8);
        int length = bytes.length;
        byte[] lengthBytes = int2Bytes(length);
        byte[] bytes1 = Arrays.copyOf(lengthBytes, length + lengthBytes.length);
        System.arraycopy(bytes, 0, bytes1, lengthBytes.length, length);
        // 复现粘包拆包问题
        for (int i = 0; i < 100; i++) {
            outputStream.write(bytes1);
        }

        System.in.read();
    }

    private static byte[] int2Bytes(int integer) {
//        return ByteBuffer.allocate(4).putInt(integer).array();
        return new byte[]{(byte)(integer>>24), (byte)(integer>>16), (byte)(integer>>8), (byte)(integer)};
    }
}
