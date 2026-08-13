package vip.appap.suxin.module.accountant.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 钱包余额增加 Request DTO
 *
 * @author 书心软件
 */
@Data
public class AccountAddBalanceReqDTO {

    /**
     * 用户编号
     *
     * 关联 partner 表的 id
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    /**
     * 关联业务分类
     */
    @NotNull(message = "关联业务分类不能为空")
    private Integer bizType;
    /**
     * 关联业务编号
     */
    @NotNull(message = "关联业务编号不能为空")
    private String bizId;

    /**
     * 交易金额，单位分
     *
     * 正值表示余额增加，负值表示余额减少
     */
    @NotNull(message = "交易金额不能为空")
    private Integer price;

}

