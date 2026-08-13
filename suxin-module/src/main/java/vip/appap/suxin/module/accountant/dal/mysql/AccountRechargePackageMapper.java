package vip.appap.suxin.module.accountant.dal.mysql;


import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountRechargePackagePageReqVO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountRechargePackageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AccountRechargePackageMapper extends BaseMapperX<AccountRechargePackageDO> {

    default PageResult<AccountRechargePackageDO> selectPage(AccountRechargePackagePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AccountRechargePackageDO>()
                .likeIfPresent(AccountRechargePackageDO::getName, reqVO.getName())
                .eqIfPresent(AccountRechargePackageDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(AccountRechargePackageDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AccountRechargePackageDO::getPayPrice));
    }

    default AccountRechargePackageDO selectByName(String name) {
        return selectOne(AccountRechargePackageDO::getName, name);
    }

    default List<AccountRechargePackageDO> selectListByStatus(Integer status) {
        return selectList(AccountRechargePackageDO::getStatus, status);
    }

}

