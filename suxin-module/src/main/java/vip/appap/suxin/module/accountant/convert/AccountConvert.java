package vip.appap.suxin.module.accountant.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountRespVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppAccountRespVO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccountConvert {

    AccountConvert INSTANCE = Mappers.getMapper(AccountConvert.class);

    AppAccountRespVO convert(AccountDO bean);

    AccountRespVO convert02(AccountDO bean);

    PageResult<AccountRespVO> convertPage(PageResult<AccountDO> page);

}

