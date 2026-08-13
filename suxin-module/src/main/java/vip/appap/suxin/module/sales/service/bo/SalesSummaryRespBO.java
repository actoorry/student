package vip.appap.suxin.module.sales.service.bo;

import lombok.Data;

/**
 * 交易统计 Resp BO
 *
 * @author owen
 */
@Data
public class SalesSummaryRespBO {

    /**
     * 数量
     */
    private Integer count;

    /**
     * 合计
     */
    private Integer summary;

}
