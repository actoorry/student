package vip.appap.suxin.module.marriage.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AppInteractionNotificationSceneEnum {

    FOLLOW("FOLLOW", "关注提醒"),
    MUTUAL_FOLLOW("MUTUAL_FOLLOW", "互相关注"),
    VIEW_ME_SUMMARY("VIEW_ME_SUMMARY", "谁看过我");

    public static final String BIZ_TYPE_MARRIAGE = "marriage";

    private final String scene;
    private final String title;

}
