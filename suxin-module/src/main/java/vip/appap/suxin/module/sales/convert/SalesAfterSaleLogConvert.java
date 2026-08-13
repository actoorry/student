package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.module.sales.dal.dataobject.SalesAfterSaleLogDO;
import vip.appap.suxin.module.sales.service.bo.SalesAfterSaleLogCreateReqBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SalesAfterSaleLogConvert {

    SalesAfterSaleLogConvert INSTANCE = Mappers.getMapper(SalesAfterSaleLogConvert.class);

    SalesAfterSaleLogDO convert(SalesAfterSaleLogCreateReqBO bean);

}
