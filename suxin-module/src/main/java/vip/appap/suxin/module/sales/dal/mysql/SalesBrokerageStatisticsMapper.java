package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.sales.dal.dataobject.SalesStatisticsDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 订单分销的统计 Mapper
 *
 * @author owen
 */
@Mapper
public interface SalesBrokerageStatisticsMapper extends BaseMapperX<SalesStatisticsDO> {

    Integer selectSummaryPriceByStatusAndUnfreezeTimeBetween(@Param("bizType") Integer bizType,
                                                             @Param("status") Integer status,
                                                             @Param("beginTime") LocalDateTime beginTime,
                                                             @Param("endTime") LocalDateTime endTime);

    Long selectWithdrawCountByStatus(@Param("status") Integer status);

}
