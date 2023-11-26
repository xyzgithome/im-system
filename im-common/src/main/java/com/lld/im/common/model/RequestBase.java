package com.lld.im.common.model;

import lombok.Data;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class RequestBase {
    private Integer appId;

    private String operator;

    private Integer clientType;

    private String imei;
}
