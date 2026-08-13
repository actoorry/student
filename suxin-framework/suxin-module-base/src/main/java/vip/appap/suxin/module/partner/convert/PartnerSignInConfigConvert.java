package vip.appap.suxin.module.partner.convert;

import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSignInConfigRespVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSignInConfigSaveReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSignInConfigDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 会员签到配置 Convert
 *
 * @author 书心软件
 */
@Mapper
public interface PartnerSignInConfigConvert {

    PartnerSignInConfigConvert INSTANCE = Mappers.getMapper(PartnerSignInConfigConvert.class);

    PartnerSignInConfigDO convert(PartnerSignInConfigSaveReqVO bean);

    PartnerSignInConfigRespVO convert(PartnerSignInConfigDO bean);

    List<PartnerSignInConfigRespVO> convertList(List<PartnerSignInConfigDO> list);

}
