package vip.appap.suxin.module.system.api;

import lombok.Data;

/**
 * 用户资料更新消息
 *
 * 当用户资料（昵称、头像等）更新时，通过此消息通知其他模块
 */
@Data
public class AdminUserProfileUpdateMessage {

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

}
