package vip.appap.suxin.module.accountant.dal.mysql;


import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountPageReqVO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper extends BaseMapperX<AccountDO> {

    default AccountDO selectByPartnerIdAndBalanceType(Long partnerId, String balanceType) {
        return selectOne(new LambdaQueryWrapperX<AccountDO>()
                .eq(AccountDO::getPartnerId, partnerId)
                .eq(AccountDO::getBalanceType, balanceType));
    }

    default PageResult<AccountDO> selectPage(AccountPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AccountDO>()
                .eqIfPresent(AccountDO::getPartnerId, reqVO.getPartnerId())
                .betweenIfPresent(AccountDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AccountDO::getId));
    }

    /**
     * 当消费退款时候， 更新钱包
     *
     * @param id 钱包 id
     * @param price 消费金额
     */
    default int updateWhenConsumptionRefund(Long id, Integer price) {
        LambdaUpdateWrapper<AccountDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<AccountDO>()
                .setSql(" balance = balance + " + price
                        + ", total_expense = total_expense - " + price)
                .eq(AccountDO::getId, id);
        return update(null, lambdaUpdateWrapper);
    }

    /**
     * 当消费时候， 更新钱包
     *
     * @param price 消费金额
     * @param id 钱包 id
     */
    default int updateWhenConsumption(Long id, Integer price){
        LambdaUpdateWrapper<AccountDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<AccountDO>()
                .setSql(" balance = balance - " + price
                        + ", total_expense = total_expense + " + price)
                .eq(AccountDO::getId, id)
                .ge(AccountDO::getBalance, price); // cas 逻辑
        return update(null, lambdaUpdateWrapper);
    }

    /**
     * 当充值的时候，更新钱包
     *
     * @param id 钱包 id
     * @param price 钱包金额
     */
    default int updateWhenRecharge(Long id, Integer price){
        LambdaUpdateWrapper<AccountDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<AccountDO>()
                .setSql(" balance = balance + " + price
                        + ", total_recharge = total_recharge + " + price)
                .eq(AccountDO::getId, id);
        return update(null, lambdaUpdateWrapper);
    }

    /**
     * 增加余额的时候，更新钱包
     *
     * @param id 钱包 id
     * @param price 钱包金额
     */
    default void updateWhenAdd(Long id, Integer price) {
        LambdaUpdateWrapper<AccountDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<AccountDO>()
             .setSql(" balance = balance + " + price)
             .eq(AccountDO::getId, id);
        update(null, lambdaUpdateWrapper);
    }

    /**
     * 冻结钱包部分余额
     *
     * @param id 钱包 id
     * @param price 冻结金额
     */
    default int freezePrice(Long id, Integer price){
        LambdaUpdateWrapper<AccountDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<AccountDO>()
                .setSql(" balance = balance - " + price
                        + ", freeze_price = freeze_price + " + price)
                .eq(AccountDO::getId, id)
                .ge(AccountDO::getBalance, price); // cas 逻辑
        return update(null, lambdaUpdateWrapper);
    }

    /**
     * 解冻钱包余额
     *
     * @param id 钱包 id
     * @param price 解冻金额
     */
    default int unFreezePrice(Long id, Integer price){
        LambdaUpdateWrapper<AccountDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<AccountDO>()
                .setSql(" balance = balance + " + price
                        + ", freeze_price = freeze_price - " + price)
                .eq(AccountDO::getId, id)
                .ge(AccountDO::getFreezePrice, price); // cas 逻辑
        return update(null, lambdaUpdateWrapper);
    }

    /**
     * 当充值退款时, 更新钱包
     *
     * @param id 钱包 id
     * @param price 退款金额
     */
    default  int updateWhenRechargeRefund(Long id, Integer price){
        LambdaUpdateWrapper<AccountDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<AccountDO>()
                .setSql(" freeze_price = freeze_price - " + price
                        + ", total_recharge = total_recharge - " + price)
                .eq(AccountDO::getId, id)
                .ge(AccountDO::getFreezePrice, price)
                .ge(AccountDO::getTotalRecharge, price);// cas 逻辑
        return update(null, lambdaUpdateWrapper);
    }

}





