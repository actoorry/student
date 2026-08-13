package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Schema(description = "用户 App - 首页推荐会员 Response VO")
@Data
@Accessors(chain = true)
public class AppPartnerMarriageProfileRespVO {

    @Schema(description = "档案 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long id;

    @Schema(description = "展示名", requiredMode = Schema.RequiredMode.REQUIRED, example = "林薇")
    private String name;

    @Schema(description = "性别，1-男，2-女", example = "2")
    private Integer sex;

    @Schema(description = "年龄", requiredMode = Schema.RequiredMode.REQUIRED, example = "26")
    private Integer age;

    @Schema(description = "城市", requiredMode = Schema.RequiredMode.REQUIRED, example = "深圳")
    private String city;

    @Schema(description = "学历", requiredMode = Schema.RequiredMode.REQUIRED, example = "本科")
    private String education;

    @Schema(description = "职业", example = "UI设计师")
    private String job;

    @Schema(description = "身高，单位 cm", requiredMode = Schema.RequiredMode.REQUIRED, example = "165")
    private Integer height;

    @Schema(description = "体重，单位 kg", example = "55")
    private Integer weight;

    @Schema(description = "收入文案", example = "15k-20k")
    private String income;

    @Schema(description = "婚况", example = "未婚")
    private String maritalStatus;

    @Schema(description = "住房状态", example = "已购房")
    private String houseStatus;

    @Schema(description = "车辆状态", example = "已购车")
    private String carStatus;

    @Schema(description = "实名认证状态，0-未认证，1-已认证", example = "1")
    private Integer realVerified;

    @Schema(description = "婚恋认证状态，0-未认证，1-已认证", example = "1")
    private Integer marriageVerified;

    @Schema(description = "当前有效会员状态，0-否，1-是", example = "1")
    private Integer memberActive;

    @Schema(description = "脱敏身份证号，仅保留前四位", example = "4403**************")
    private String maskedIdCard;

    @Schema(description = "认证标签", requiredMode = Schema.RequiredMode.REQUIRED, example = "已实名")
    private String verifiedLabel;

    @Schema(description = "头像")
    private String avatarImage;

    @Schema(description = "主展示图（大图卡片背景）")
    private String mainImage;

    @Schema(description = "在线状态文案", example = "在线")
    private String onlineLabel;

    @Schema(description = "热度文案", example = "8人心动")
    private String viewerLabel;

    @Schema(description = "个人介绍", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bio;

    @Schema(description = "顶部标签", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> tags;

    @Schema(description = "择偶条件标签", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> interestTags;

    @Schema(description = "相册图片", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> albumImages;

    @Schema(description = "最近一条动态（可能为 null）")
    private AppPartnerMomentSimpleRespVO latestMoment;

}
