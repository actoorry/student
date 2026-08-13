package vip.appap.suxin.module.rongjh.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 战友会状态 Response VO（兼容 Odoo 前端字段）")
@Data
public class AppWarriorStatusRespVO {

    @Schema(description = "是否有申请记录")
    private Boolean hasApplication;

    @Schema(description = "是否战友会成员")
    private Boolean isMember;

    @Schema(description = "全国战友总数")
    private Long totalCount;

    @Schema(description = "成员总数（或当前省成员数）")
    private Long memberCount;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "电话")
    private String phone;

    @Schema(description = "省份编号")
    private Integer stateId;

    @Schema(description = "省份名称")
    private String stateName;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "服役单位")
    private String serviceUnit;

    @Schema(description = "服役年份")
    private String serviceYear;

    @Schema(description = "说明")
    private String description;

    @Schema(description = "状态: approved/pending/rejected")
    private String state;

    @Schema(description = "状态展示文案")
    private String stateDisplay;

    @Schema(description = "创建时间")
    private String createDate;

    @Schema(description = "通过时间")
    private String approveDate;

}
