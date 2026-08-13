package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "App - 当前婚恋会员信息 Response VO")
@Data
public class MarriageUserInfoRespVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String nickname;

    @Schema(description = "用户头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://thirdwx.qlogo.cn/mmopen/xxx/132")
    private String avatar;

    @Schema(description = "背景图", example = "https://www.appap.vip/bg.png")
    private String backgroundImage;

    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    @Schema(description = "是否会员", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean member;

    @Schema(description = "实名认证状态，0-未认证，1-已认证", example = "1")
    private Integer realVerified;

    @Schema(description = "实名认证展示文案", example = "已实名")
    private String verifiedLabel;

    @Schema(description = "资料完整度百分比", example = "68")
    private Integer profileCompletion;

    @Schema(description = "待完善字段列表")
    private List<String> incompleteFields;

    @Schema(description = "真实姓名（来自实名认证）", example = "张三")
    private String name;

    @Schema(description = "脱敏后的身份证号（来自实名认证）", example = "110101********1234")
    private String maskedIdCard;

}
