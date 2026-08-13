package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "App - 婚恋完整资料 Response VO")
@Data
public class MarriageProfileRespVO {

    // ========== 用户基础信息（来自 partner 表）==========

    @Schema(description = "用户编号", example = "1024")
    private Long userId;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "用户头像", example = "https://xxx/avatar.jpg")
    private String avatar;

    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    @Schema(description = "真实姓名", example = "张三")
    private String name;

    @Schema(description = "脱敏身份证号", example = "110101********0011")
    private String maskedIdCard;

    @Schema(description = "性别", example = "1")
    private Integer sex;

    @Schema(description = "出生日期", example = "1995-01-01")
    private String birthday;

    @Schema(description = "籍贯地区ID", example = "1024")
    private Integer areaId;

    // ========== 婚恋资料（来自 partner_marriage 表）==========

    @Schema(description = "婚姻状态", example = "1")
    private Integer maritalStatus;

    @Schema(description = "身高(cm)", example = "175")
    private Integer heightCm;

    @Schema(description = "体重(kg)", example = "70")
    private Integer weightKg;

    @Schema(description = "学历", example = "3")
    private Integer education;

    @Schema(description = "月收入水平", example = "5")
    private Integer incomeLevel;

    @Schema(description = "现居地ID", example = "1024")
    private Long liveAreaId;

    @Schema(description = "房产状况", example = "1")
    private Integer houseStatus;

    @Schema(description = "车辆状况", example = "1")
    private Integer carStatus;

    @Schema(description = "职业", example = "软件工程师")
    private String jobTitle;

    @Schema(description = "自我介绍", example = "热爱生活")
    private String bio;

    @Schema(description = "背景图", example = "https://xxx/bg.jpg")
    private String backgroundImage;

    @Schema(description = "实名认证状态，0-未认证，1-已认证", example = "1")
    private Integer realVerified;

    @Schema(description = "婚姻认证状态，0-未认证，1-已认证", example = "1")
    private Integer singleVerified;

    @Schema(description = "实名认证展示文案", example = "已实名")
    private String verifiedLabel;

    // ========== 择偶条件（来自 partner_marriage 表 mate_* 字段）==========

    @Schema(description = "期望婚姻状态", example = "1")
    private Integer mateMaritalStatus;

    @Schema(description = "期望最小身高(cm)", example = "160")
    private Integer mateMinHeightCm;

    @Schema(description = "期望最大身高(cm)", example = "175")
    private Integer mateMaxHeightCm;

    @Schema(description = "期望最小体重(kg)", example = "45")
    private Integer mateMinWeightKg;

    @Schema(description = "期望最大体重(kg)", example = "65")
    private Integer mateMaxWeightKg;

    @Schema(description = "期望最低学历", example = "3")
    private Integer mateMinEducation;

    @Schema(description = "期望现居地ID", example = "1024")
    private Long mateLiveAreaId;

    @Schema(description = "期望最低月收入水平", example = "5")
    private Integer mateMinIncomeLevel;

    @Schema(description = "期望房产状况", example = "1")
    private Integer mateHouseStatus;

    @Schema(description = "期望车辆状况", example = "1")
    private Integer mateCarStatus;

    @Schema(description = "期望职业", example = "不限")
    private String mateJobTitle;

    @Schema(description = "其他要求", example = "性格开朗")
    private String mateRemark;

    // ========== 资料完整度 ==========

    @Schema(description = "资料完整度百分比", example = "68")
    private Integer profileCompletion;

    @Schema(description = "待完善字段列表")
    private List<String> incompleteFields;

}
