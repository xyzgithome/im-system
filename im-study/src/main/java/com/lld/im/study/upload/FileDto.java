package com.lld.im.study.netty.upload;

import lombok.Data;

@Data
public class FileDto {
    // 文件名称
    private String fileName;

    // 1.请求创建文件 2.传输文件
    private Integer command;

    // 文件字节：再实际应用中可以使用非堆成加密，以保证传输信息的安全
    private byte[] bytes;
}
