package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesElectronicWaybillAccountCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesElectronicWaybillAccountRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesElectronicWaybillAccountUpdateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillAccountDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SalesElectronicWaybillAccountConvert {

    SalesElectronicWaybillAccountConvert INSTANCE = Mappers.getMapper(SalesElectronicWaybillAccountConvert.class);

    SalesElectronicWaybillAccountDO convert(SalesElectronicWaybillAccountCreateReqVO bean);

    SalesElectronicWaybillAccountDO convert(SalesElectronicWaybillAccountUpdateReqVO bean);

    @Mapping(target = "key", expression = "java(vip.appap.suxin.module.sales.service.SalesElectronicWaybillMaskUtil.mask(bean.getKey()))")
    @Mapping(target = "secret", expression = "java(vip.appap.suxin.module.sales.service.SalesElectronicWaybillMaskUtil.mask(bean.getSecret()))")
    @Mapping(target = "partnerId", expression = "java(vip.appap.suxin.module.sales.service.SalesElectronicWaybillMaskUtil.mask(bean.getPartnerId()))")
    @Mapping(target = "partnerKey", expression = "java(vip.appap.suxin.module.sales.service.SalesElectronicWaybillMaskUtil.mask(bean.getPartnerKey()))")
    @Mapping(target = "partnerSecret", expression = "java(vip.appap.suxin.module.sales.service.SalesElectronicWaybillMaskUtil.mask(bean.getPartnerSecret()))")
    SalesElectronicWaybillAccountRespVO convert(SalesElectronicWaybillAccountDO bean);

    @Mapping(target = "key", expression = "java(vip.appap.suxin.module.sales.service.SalesElectronicWaybillMaskUtil.mask(bean.getKey()))")
    @Mapping(target = "secret", expression = "java(vip.appap.suxin.module.sales.service.SalesElectronicWaybillMaskUtil.mask(bean.getSecret()))")
    @Mapping(target = "partnerId", expression = "java(vip.appap.suxin.module.sales.service.SalesElectronicWaybillMaskUtil.mask(bean.getPartnerId()))")
    @Mapping(target = "partnerKey", expression = "java(vip.appap.suxin.module.sales.service.SalesElectronicWaybillMaskUtil.mask(bean.getPartnerKey()))")
    @Mapping(target = "partnerSecret", expression = "java(vip.appap.suxin.module.sales.service.SalesElectronicWaybillMaskUtil.mask(bean.getPartnerSecret()))")
    List<SalesElectronicWaybillAccountRespVO> convertList(List<SalesElectronicWaybillAccountDO> list);

    @Mapping(target = "list", expression = "java(convertList(page.getList()))")
    PageResult<SalesElectronicWaybillAccountRespVO> convertPage(PageResult<SalesElectronicWaybillAccountDO> page);

}
