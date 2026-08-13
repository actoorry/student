package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderLogDO;
import vip.appap.suxin.module.sales.service.bo.SalesOrderLogCreateReqBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SalesOrderLogConvert {

    SalesOrderLogConvert INSTANCE = Mappers.getMapper(SalesOrderLogConvert.class);

    SalesOrderLogDO convert(SalesOrderLogCreateReqBO bean);

}
