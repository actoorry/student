package vip.appap.suxin.module.partner.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerAddressCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerAddressRespVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerAddressUpdateReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerAddressDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 会员地址 Convert
 *
 * @author 书心软件
 */
@Mapper
public interface PartnerAddressConvert {

    PartnerAddressConvert INSTANCE = Mappers.getMapper(PartnerAddressConvert.class);

    PartnerAddressDO convert(PartnerAddressCreateReqVO bean);

    PartnerAddressDO convert(PartnerAddressUpdateReqVO bean);

    PartnerAddressRespVO convert(PartnerAddressDO bean);

    List<PartnerAddressRespVO> convertList(List<PartnerAddressDO> list);

    PageResult<PartnerAddressRespVO> convertPage(PageResult<PartnerAddressDO> page);

}
