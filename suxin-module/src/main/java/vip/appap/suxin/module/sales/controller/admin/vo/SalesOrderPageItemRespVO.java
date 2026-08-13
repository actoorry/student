package vip.appap.suxin.module.sales.controller.admin.vo;

import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderPartnerRespVO;
import vip.appap.suxin.module.sales.controller.admin.SalesProductPropertyValueDetailRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 交易订单的分页项 Response VO")
@Data
public class SalesOrderPageItemRespVO extends SalesOrderBaseVO {

    @Schema(description = "收件人地区名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "上海 上海市 普陀区")
    private String receiverAreaName;

    @Schema(description = "电子面单信息（无有效面单时为空）")
    private SalesElectronicWaybillRespVO waybill;

    @Schema(description = "订单项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Item> items;

    @Schema(description = "用户信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private SalesOrderPartnerRespVO user;

    @Schema(description = "推广人信息")
    private SalesOrderPartnerRespVO brokerageUser;

    @Schema(description = "管理后台 - 交易订单的分页项的订单项目")
    @Data
    public static class Item extends SalesOrderItemBaseVO {

        @Schema(description = "属性列表", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<SalesProductPropertyValueDetailRespVO> properties;

    }

}
