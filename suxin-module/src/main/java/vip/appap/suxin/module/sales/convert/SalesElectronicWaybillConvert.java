package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.module.sales.controller.admin.vo.SalesElectronicWaybillRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillDO;
import vip.appap.suxin.module.sales.enums.SalesElectronicWaybillStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SalesElectronicWaybillConvert {

    SalesElectronicWaybillConvert INSTANCE = Mappers.getMapper(SalesElectronicWaybillConvert.class);

    @Mapping(target = "statusName", expression = "java(convertStatusName(bean.getStatus()))")
    SalesElectronicWaybillRespVO convert(SalesElectronicWaybillDO bean);

    List<SalesElectronicWaybillRespVO> convertList(List<SalesElectronicWaybillDO> list);

    default String convertStatusName(Integer status) {
        SalesElectronicWaybillStatusEnum statusEnum = SalesElectronicWaybillStatusEnum.getByStatus(status);
        return statusEnum != null ? statusEnum.getName() : "";
    }

}
