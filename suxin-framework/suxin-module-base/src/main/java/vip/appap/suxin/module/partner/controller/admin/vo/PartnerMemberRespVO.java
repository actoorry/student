package vip.appap.suxin.module.partner.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 会员 Response VO")
@Data
public class PartnerMemberRespVO {

    @Schema(description = "会员记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23788")
    private Long id;

    @Schema(description = "用户编号", example = "1024")
    private Long userId;

    @Schema(description = "会员类型", example = "1")
    private Integer memberType;

    @Schema(description = "会员开始时间")
    private LocalDateTime startTime;

    @Schema(description = "会员结束时间")
    private LocalDateTime endTime;

    @Schema(description = "会员状态", example = "1")
    private Integer status;

    @Schema(description = "来源类型", example = "1")
    private Integer sourceType;

    @Schema(description = "订单编号", example = "1000")
    private Long orderId;

    @Schema(description = "订单项编号", example = "1001")
    private Long orderItemId;

    @Schema(description = "SPU 编号", example = "2000")
    private Long spuId;

    @Schema(description = "SKU 编号", example = "2001")
    private Long skuId;

    @Schema(description = "会员时长（月）", example = "12")
    private BigDecimal durationQuantity;

    @Schema(description = "浼氬憳鏃堕暱鍗曚綅缂栧彿", example = "102")
    private Long durationUnitId;

    @Schema(description = "手机号", example = "15601691300")
    private String mobile;

    @Schema(description = "用户昵称", example = "李四")
    private String nickname;

    @Schema(description = "头像", example = "https://www.appap.vip/x.png")
    private String avatar;

    @Schema(description = "真实名字", example = "张三")
    private String name;

    @Schema(description = "性别", example = "1")
    private Integer sex;

    @Schema(description = "出生日期")
    private LocalDateTime birthday;

    @Schema(description = "所在地编号", example = "4371")
    private Integer areaId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "最后登录 IP", example = "127.0.0.1")
    private String loginIp;

    @Schema(description = "最后登录时间")
    private LocalDateTime loginDate;

    @Schema(description = "积分", example = "100")
    private Integer point;

    @Schema(description = "会员等级编号", example = "1")
    private Long customerLevel;

    @Schema(description = "会员等级", example = "黄金会员")
    private String levelName;

    @Schema(description = "经验值", example = "200")
    private Integer experience;

    @Schema(description = "用户分组编号", example = "1")
    private Long groupId;

    @Schema(description = "用户分组", example = "购物达人")
    private String groupName;

    @Schema(description = "会员标签，逗号分隔", example = "1,2,3")
    private String tagIds;

    @Schema(description = "会员标签名称", example = "[红色, 快乐]")
    private List<String> tagNames;

    @Schema(description = "注册 IP", example = "127.0.0.1")
    private String registerIp;

    @Schema(description = "注册终端")
    private Integer registerTerminal;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
