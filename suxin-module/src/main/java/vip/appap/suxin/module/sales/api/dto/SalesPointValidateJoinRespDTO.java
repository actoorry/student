package vip.appap.suxin.module.sales.api.dto;

import lombok.Data;

/**
 * 校验参与积分商城 Response DTO
 */
@Data
public class SalesPointValidateJoinRespDTO {

    /**
     * 可兑换次数
     */
    private Integer count;
    /**
     * 所需兑换积分
     */
    private Integer point;
    /**
     * 所需兑换金额，单位：分
     */
    private Integer price;

}
