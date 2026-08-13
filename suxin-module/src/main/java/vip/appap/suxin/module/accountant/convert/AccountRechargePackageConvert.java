package vip.appap.suxin.module.accountant.convert;

import java.util.*;

import vip.appap.suxin.framework.common.pojo.PageResult;

import vip.appap.suxin.module.accountant.controller.admin.vo.AccountRechargePackageCreateReqVO;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountRechargePackageRespVO;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountRechargePackageUpdateReqVO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountRechargePackageDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccountRechargePackageConvert {

    AccountRechargePackageConvert INSTANCE = Mappers.getMapper(AccountRechargePackageConvert.class);

    AccountRechargePackageDO convert(AccountRechargePackageCreateReqVO bean);

    AccountRechargePackageDO convert(AccountRechargePackageUpdateReqVO bean);

    AccountRechargePackageRespVO convert(AccountRechargePackageDO bean);

    List<AccountRechargePackageRespVO> convertList(List<AccountRechargePackageDO> list);

    PageResult<AccountRechargePackageRespVO> convertPage(PageResult<AccountRechargePackageDO> page);

}

