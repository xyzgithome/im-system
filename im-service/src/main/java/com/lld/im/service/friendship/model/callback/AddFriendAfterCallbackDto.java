package com.lld.im.service.friendship.model.callback;

import com.lld.im.service.friendship.model.req.FriendDto;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
@Accessors(chain = true)
public class AddFriendAfterCallbackDto {

    private String fromId;

    private FriendDto toItem;
}
