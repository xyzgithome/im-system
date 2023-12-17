package com.lld.im.service.group.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
public class UpdateGroupMemberReq extends RequestBase {

    @NotBlank(message = "群id不能为空")
    private String groupId;

    @NotBlank(message = "memberId不能为空")
    private String memberId;

    // 群昵称
    private String alias;

    // 群成员类型，0 普通成员, 1 管理员, 2 群主， 3 离开
    private Integer role;

    // 附件信息
    private String extra;

}
