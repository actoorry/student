package vip.appap.suxin.module.accountant.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 会员钱包充值套餐 DO
 *
 * 通过充值套餐时，可以赠送一定金额；
 *
 * @author 书心软件
 */
@TableName("account_recharge_package")
@KeySequence("account_recharge_package_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AccountRechargePackageDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 套餐名
     */
    private String name;

    /**
     * 支付金额
     */
    private Integer payPrice;
    /**
     * 赠送金额
     */
    private Integer bonusPrice;

    /**
     * 状态
     *
     * 枚举 {@link vip.appap.suxin.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

}

