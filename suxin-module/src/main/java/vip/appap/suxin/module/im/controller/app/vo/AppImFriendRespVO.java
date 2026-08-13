package vip.appap.suxin.module.im.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * App 端 - IM 好友 Response VO
 */
@Schema(description = "App 端 - IM 好友 Response VO")
@Data
public class AppImFriendRespVO {

    @Schema(description = "关系记录编号", example = "1024")
    private Long id;

    @Schema(description = "好友的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long friendUserId;

    @Schema(description = "好友昵称", example = "张三")
    private String friendNickName;

    @Schema(description = "好友头像")
    private String friendHeadImage;

    @Schema(description = "是否免打扰", example = "false")
    private Boolean isDnd;

    @Schema(description = "好友展示备注（仅自己可见）", example = "老张")
    private String displayName;

    @Schema(description = "添加来源", example = "1")
    private Integer addSource;

    @Schema(description = "是否置顶联系人", example = "false")
    private Boolean pinned;

    @Schema(description = "是否拉黑（仅自己可见）", example = "false")
    private Boolean blocked;

    @Schema(description = "好友状态", example = "0")
    private Integer status;

    @Schema(description = "添加好友时间")
    private LocalDateTime addTime;

}
