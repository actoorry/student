package vip.appap.suxin.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 微信多媒体内容安全检查场景枚举
 *
 * 参考文档：https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/sec-check/security.mediaCheckAsync.html
 *
 * @author 书心软件
 */
@Getter
@AllArgsConstructor
public enum WxMediaCheckSceneEnum {

    PROFILE(1, "资料"),
    COMMENT(2, "评论"),
    FORUM(3, "论坛"),
    SOCIAL_LOG(4, "社交日志");

    /**
     * 场景值
     */
    private final Integer scene;
    /**
     * 场景名称
     */
    private final String name;

    /**
     * 根据场景值获取枚举
     *
     * @param scene 场景值
     * @return 枚举
     */
    public static WxMediaCheckSceneEnum valueOfScene(Integer scene) {
        for (WxMediaCheckSceneEnum value : values()) {
            if (value.getScene().equals(scene)) {
                return value;
            }
        }
        return null;
    }

}
