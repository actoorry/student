package vip.appap.suxin.module.sales.controller.admin.vo;

import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderPartnerRespVO;
import vip.appap.suxin.module.sales.controller.admin.SalesProductPropertyValueDetailRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 交易售后分页的每一条记录 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesAfterSaleRespPageItemVO extends SalesAfterSaleBaseVO {

    @Schema(description = "售后编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27630")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    /**
     * 商品属性数组
     */
    private List<SalesProductPropertyValueDetailRespVO> properties;

    /**
     * 用户信息
     */
    private SalesOrderPartnerRespVO user;

}
