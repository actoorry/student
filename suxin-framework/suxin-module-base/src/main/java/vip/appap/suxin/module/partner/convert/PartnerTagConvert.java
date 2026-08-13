package vip.appap.suxin.module.partner.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerTagCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerTagRespVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerTagUpdateReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerTagDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 会员标签 Convert
 *
 * @author 书心软件
 */
@Mapper
public interface PartnerTagConvert {

    PartnerTagConvert INSTANCE = Mappers.getMapper(PartnerTagConvert.class);

    PartnerTagDO convert(PartnerTagCreateReqVO bean);

    PartnerTagDO convert(PartnerTagUpdateReqVO bean);

    PartnerTagRespVO convert(PartnerTagDO bean);

    List<PartnerTagRespVO> convertList(List<PartnerTagDO> list);

    PageResult<PartnerTagRespVO> convertPage(PageResult<PartnerTagDO> page);

}
