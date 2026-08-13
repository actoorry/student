package vip.appap.suxin.module.partner.api;

import lombok.Data;

/**
 * Partner 用户资料更新消息
 *
 * 当用户资料（昵称、头像等）更新时，通过此消息通知其他模块（如 IM）
 */
@Data
public class PartnerProfileUpdateMessage {

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
