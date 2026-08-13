package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.module.sales.controller.admin.vo.SalesPayStatisticsSummaryRespVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 支付统计 Convert
 *
 * @author owen
 */
@Mapper
public interface SalesPayStatisticsConvert {

    SalesPayStatisticsConvert INSTANCE = Mappers.getMapper(SalesPayStatisticsConvert.class);

    SalesPayStatisticsSummaryRespVO convert(Integer rechargePrice);

}
