package vip.appap.suxin.module.partner.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

/**
 * 合作伙伴 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class PartnerBaseVO {

    @Schema(description = "客户/伙伴名称", example = "赵六")
    private String name;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15601691300")
    @NotNull(message = "手机号不能为空")
    private String mobile;

    @Schema(description = "固定电话", example = "021-12345678")
    private String telephone;

    @Schema(description = "QQ", example = "123456789")
    private String qq;

    @Schema(description = "微信", example = "wx123456")
    private String wechat;

    @Schema(description = "所属行业", example = "1")
    private Integer industryId;

    @Schema(description = "用户邮箱", example = "test@example.com")
    private String email;

    @Schema(description = "状态", example = "2")
    private Byte status;

    @Schema(description = "是否客户", example = "true")
    private Boolean isCustomer;

    @Schema(description = "是否供应商", example = "false")
    private Boolean isSupplier;

    @Schema(description = "供应商等级（关联 partner_level.id，type=supplier）", example = "1")
    private Long supplierLevel;

    @Schema(description = "供应商评分", example = "95")
    private Integer supplierScore;

    @Schema(description = "是否公司（true=公司，false=个人）", example = "false")
    private Boolean isCompany;

    @Schema(description = "是否会员", example = "true")
    private Boolean isMember;

    @Schema(description = "用户昵称", example = "李四")
    private String nickname;

    @Schema(description = "头像", example = "https://www.appap.vip/x.png")
    @URL(message = "头像必须是 URL 格式")
    private String avatar;

    @Schema(description = "用户性别", example = "1")
    private Integer sex;

    @Schema(description = "所在地编号", example = "4371")
    private Long areaId;

    @Schema(description = "所在地全程", example = "上海上海市普陀区")
    private String areaName;

    @Schema(description = "详细地址", example = "XX路XX号")
    private String detailAddress;


    @Schema(description = "出生日期", example = "2023-03-12")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDateTime birthday;

    @Schema(description = "备注", example = "我是小备注")
    private String remark;

}
