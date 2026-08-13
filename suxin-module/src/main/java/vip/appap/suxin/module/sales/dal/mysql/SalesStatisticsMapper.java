package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesTrendSummaryRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesStatisticsDO;
import vip.appap.suxin.module.sales.service.bo.SalesSummaryRespBO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易统计 Mapper
 *
 * @author owen
 */
@Mapper
public interface SalesStatisticsMapper extends BaseMapperX<SalesStatisticsDO> {

    SalesSummaryRespBO selectOrderCreateCountSumAndOrderPayPriceSumByTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                                                                 @Param("endTime") LocalDateTime endTime);

    SalesTrendSummaryRespVO selectVoByTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                                  @Param("endTime") LocalDateTime endTime);

    default List<SalesStatisticsDO> selectListByTimeBetween(LocalDateTime beginTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<SalesStatisticsDO>()
                .between(SalesStatisticsDO::getTime, beginTime, endTime));
    }

    Integer selectExpensePriceByTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                            @Param("endTime") LocalDateTime endTime);

    default SalesStatisticsDO selectByTimeBetween(LocalDateTime beginTime, LocalDateTime endTime) {
        return selectOne(new LambdaQueryWrapperX<SalesStatisticsDO>()
                .between(SalesStatisticsDO::getTime, beginTime, endTime));
    }

}
