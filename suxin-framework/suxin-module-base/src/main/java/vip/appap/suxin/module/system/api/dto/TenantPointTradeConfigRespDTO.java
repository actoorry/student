package vip.appap.suxin.module.system.api.dto;

import lombok.Data;

/**
 * 租户积分交易配置 DTO
 *
 * @author 书心软件
 */
@Data
public class TenantPointTradeConfigRespDTO {

    private Boolean pointTradeDeductEnable;
    private Integer pointTradeDeductUnitPrice;
    private Integer pointTradeDeductMaxPrice;
    private Integer pointTradeGivePoint;
}
