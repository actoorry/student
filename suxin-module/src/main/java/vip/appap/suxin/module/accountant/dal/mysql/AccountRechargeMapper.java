package vip.appap.suxin.module.accountant.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountRechargeDO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountRechargePackageDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountRechargeMapper extends BaseMapperX<AccountRechargeDO> {

    default int updateByIdAndPaid(Long id, boolean wherePayStatus, AccountRechargeDO updateObj) {
        return update(updateObj, new LambdaQueryWrapperX<AccountRechargeDO>()
                .eq(AccountRechargeDO::getId, id).eq(AccountRechargeDO::getPayStatus, wherePayStatus));
    }

    default int updateByIdAndRefunded(Long id, Integer whereRefundStatus, AccountRechargeDO updateObj) {
        return update(updateObj, new LambdaQueryWrapperX<AccountRechargeDO>()
                .eq(AccountRechargeDO::getId, id).eq(AccountRechargeDO::getRefundStatus, whereRefundStatus));
    }

    default PageResult<AccountRechargeDO> selectPage(PageParam pageReqVO, Long accountId, Boolean payStatus) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<AccountRechargeDO>()
                .eq(AccountRechargeDO::getAccountId, accountId)
                .eq(AccountRechargeDO::getPayStatus, payStatus)
                .orderByDesc(AccountRechargeDO::getId));
    }

}