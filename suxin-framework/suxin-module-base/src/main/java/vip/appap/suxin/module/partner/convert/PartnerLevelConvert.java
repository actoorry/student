package vip.appap.suxin.module.partner.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerLevelCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerLevelRespVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerLevelUpdateReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerLevelDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 会员等级 Convert
 *
 * @author 书心软件
 */
@Mapper
public interface PartnerLevelConvert {

    PartnerLevelConvert INSTANCE = Mappers.getMapper(PartnerLevelConvert.class);

    PartnerLevelDO convert(PartnerLevelCreateReqVO bean);

    PartnerLevelDO convert(PartnerLevelUpdateReqVO bean);

    PartnerLevelRespVO convert(PartnerLevelDO bean);

    List<PartnerLevelRespVO> convertList(List<PartnerLevelDO> list);

    PageResult<PartnerLevelRespVO> convertPage(PageResult<PartnerLevelDO> page);

}
