package vip.appap.suxin.module.partner.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerMemberRespVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerMemberDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PartnerMemberConvert {

    PartnerMemberConvert INSTANCE = Mappers.getMapper(PartnerMemberConvert.class);

    PartnerMemberRespVO convert(PartnerMemberDO bean);

    PageResult<PartnerMemberRespVO> convertPage(PageResult<PartnerMemberDO> page);

}
