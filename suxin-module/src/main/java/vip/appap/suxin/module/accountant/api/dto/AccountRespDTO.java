package vip.appap.suxin.module.accountant.api.dto;

import lombok.Data;

/**
 * 钱包 Response DTO
 *
 * @author jason
 */
@Data
public class AccountRespDTO {

    /**
     * 编号
     */
    private Long id;

    /**
     * 客商编号
     *
     * 关联 partner 表的 id
     */
    private Long partnerId;

    /**
     * 余额，单位分
     */
    private Integer balance;

    /**
     * 冻结金额，单位分
     */
    private Integer freezePrice;

    /**
     * 累计支出，单位分
     */
    private Integer totalExpense;
    /**
     * 累计充值，单位分
     */
    private Integer totalRecharge;

}

