package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.module.sales.controller.admin.vo.SalesConfigRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesConfigSaveReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesConfigRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesConfigDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 交易中心配置 Convert
 *
 * @author owen
 */
@Mapper
public interface SalesConfigConvert {

    SalesConfigConvert INSTANCE = Mappers.getMapper(SalesConfigConvert.class);

    SalesConfigDO convert(SalesConfigSaveReqVO bean);

    SalesConfigRespVO convert(SalesConfigDO bean);

    AppSalesConfigRespVO convert02(SalesConfigDO tradeConfig);
}
