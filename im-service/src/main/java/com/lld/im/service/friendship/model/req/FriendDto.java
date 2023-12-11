package com.lld.im.service.friendship.model.req;

import lombok.Data;


@Data
public class FriendDto {

    private String toId;

    private String remark;

    private String addSource;

    private String extra;

    /**
     * 好友验证信息
     */
    private String addWording;

}
