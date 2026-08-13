package vip.appap.suxin.module.partner.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerGroupCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerGroupRespVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerGroupUpdateReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerGroupDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用户分组 Convert
 *
 * @author 书心软件
 */
@Mapper
public interface PartnerGroupConvert {

    PartnerGroupConvert INSTANCE = Mappers.getMapper(PartnerGroupConvert.class);

    PartnerGroupDO convert(PartnerGroupCreateReqVO bean);

    PartnerGroupDO convert(PartnerGroupUpdateReqVO bean);

    PartnerGroupRespVO convert(PartnerGroupDO bean);

    List<PartnerGroupRespVO> convertList(List<PartnerGroupDO> list);

    PageResult<PartnerGroupRespVO> convertPage(PageResult<PartnerGroupDO> page);

}
