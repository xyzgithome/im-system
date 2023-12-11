package com.lld.im.codec.pack.friendship;

import lombok.Data;

@Data
public class AddFriendRequestPack {
    private Long id;

    private Integer appId;

    private String fromId;

    private String toId;

    /** 备注*/
    private String remark;

    //是否已读 1已读
    private Integer readStatus;

    /** 好友来源*/
    private String addSource;

    private String addWording;

    //审批状态 1同意 2拒绝
    private Integer approveStatus;

    private Long createTime;

    private Long updateTime;

    /** 序列号*/
    private Long sequence;
}
