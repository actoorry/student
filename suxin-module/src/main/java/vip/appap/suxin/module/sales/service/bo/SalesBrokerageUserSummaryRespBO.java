package vip.appap.suxin.module.sales.service.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户佣金合计 BO
 *
 * @author owen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesBrokerageUserSummaryRespBO {

    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 推广数量
     */
    private Integer count;
    /**
     * 佣金总额
     */
    private Integer price;

}
