package vip.appap.suxin.module.accountant.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 会员钱包 DO
 *
 * @author jason
 */
@TableName("account")
@KeySequence("account_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDO extends BaseDO {

    /**
     * 编号，数据库自增
     */
    @TableId
    private Long id;

    /**
     * 客商编号，关联 partner 表的 id
     */
    private Long partnerId;

    /**
     * 余额类型
     *
     * 枚举 {@link AccountBalanceTypeEnum}
     */
    private String balanceType;

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