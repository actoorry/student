package vip.appap.suxin.module.accountant.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountTransactionRespVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppAccountTransactionRespVO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountTransactionDO;
import vip.appap.suxin.module.accountant.service.bo.AccountTransactionCreateReqBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccountTransactionConvert {

    AccountTransactionConvert INSTANCE = Mappers.getMapper(AccountTransactionConvert.class);

    PageResult<AccountTransactionRespVO> convertPage2(PageResult<AccountTransactionDO> page);

    AccountTransactionDO convert(AccountTransactionCreateReqBO bean);

}

