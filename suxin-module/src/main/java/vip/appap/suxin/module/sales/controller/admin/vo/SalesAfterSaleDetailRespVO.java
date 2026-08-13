package vip.appap.suxin.module.sales.controller.admin.vo;

import vip.appap.suxin.module.sales.controller.admin.vo.SalesAfterSaleLogRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderPartnerRespVO;
import vip.appap.suxin.module.sales.controller.admin.SalesProductPropertyValueDetailRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderBaseVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderItemBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 售后订单的详情 Response VO")
@Data
public class SalesAfterSaleDetailRespVO extends SalesAfterSaleBaseVO {

    @Schema(description = "售后编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;



    /**
     * 订单基本信息
     */
    private SalesOrderBaseVO order;
    /**
     * 订单项列表
     */
    private OrderItem orderItem;

    /**
     * 用户信息
     */
    private SalesOrderPartnerRespVO user;

    /**
     * 售后日志
     */
    private List<SalesAfterSaleLogRespVO> logs;

    @Schema(description = "管理后台 - 交易订单的详情的订单项目")
    @Data
    public static class OrderItem extends SalesOrderItemBaseVO {

        /**
         * 属性数组
         */
        private List<SalesProductPropertyValueDetailRespVO> properties;

    }

}
