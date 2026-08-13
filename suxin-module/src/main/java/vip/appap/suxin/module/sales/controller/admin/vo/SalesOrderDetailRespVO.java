package vip.appap.suxin.module.sales.controller.admin.vo;

import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderPartnerRespVO;
import vip.appap.suxin.module.sales.controller.admin.SalesProductPropertyValueDetailRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 交易订单的详情 Response VO")
@Data
public class SalesOrderDetailRespVO extends SalesOrderBaseVO {

    /**
     * 订单项列表
     */
    private List<Item> items;

    /**
     * 下单用户信息
     */
    private SalesOrderPartnerRespVO user;
    /**
     * 推广用户信息
     */
    private SalesOrderPartnerRespVO brokerageUser;

    /**
     * 操作日志列表
     */
    private List<OrderLog> logs;

    @Schema(description = "收件人地区名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "上海 上海市 普陀区")
    private String receiverAreaName;

    @Schema(description = "电子面单信息（无有效面单时为空）")
    private SalesElectronicWaybillRespVO waybill;

    @Schema(description = "管理后台 - 交易订单的操作日志")
    @Data
    public static class OrderLog {

        @Schema(description = "操作详情", requiredMode = Schema.RequiredMode.REQUIRED, example = "订单发货")
        private String content;

        @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023-06-01 10:50:20")
        private LocalDateTime createTime;

        @Schema(description = "用户类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer userType;

    }

    @Schema(description = "管理后台 - 交易订单的详情的订单项目")
    @Data
    public static class Item extends SalesOrderItemBaseVO {

        /**
         * 属性数组
         */
        private List<SalesProductPropertyValueDetailRespVO> properties;

    }

}
