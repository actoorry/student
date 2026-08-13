package vip.appap.suxin.module.sales.convert;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.ip.core.Area;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesStatisticsDataComparisonRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesPartnerStatisticsAnalyseDataRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesPartnerStatisticsAnalyseRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesPartnerStatisticsAreaStatisticsRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesPartnerStatisticsSummaryRespVO;
import vip.appap.suxin.module.sales.service.bo.SalesPartnerStatisticsAreaStatisticsRespBO;
import vip.appap.suxin.module.sales.service.bo.SalesPayStatisticsRechargeSummaryRespBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 会员统计 Convert
 *
 * @author owen
 */
@Mapper
public interface SalesPartnerStatisticsConvert {

    SalesPartnerStatisticsConvert INSTANCE = Mappers.getMapper(SalesPartnerStatisticsConvert.class);

    default List<SalesPartnerStatisticsAreaStatisticsRespVO> convertList(List<Area> areaList,
                                                         Map<Integer, Integer> userCountMap,
                                                         Map<Integer, SalesPartnerStatisticsAreaStatisticsRespBO> orderMap) {
        return CollectionUtils.convertList(areaList, area -> {
            SalesPartnerStatisticsAreaStatisticsRespBO orderVo = Optional.ofNullable(orderMap.get(area.getId()))
                    .orElseGet(SalesPartnerStatisticsAreaStatisticsRespBO::new);
            return new SalesPartnerStatisticsAreaStatisticsRespVO()
                    .setAreaId(area.getId()).setAreaName(area.getName())
                    .setUserCount(MapUtil.getInt(userCountMap, area.getId(), 0))
                    .setOrderCreateUserCount(ObjUtil.defaultIfNull(orderVo.getOrderCreateUserCount(), 0))
                    .setOrderPayUserCount(ObjUtil.defaultIfNull(orderVo.getOrderPayUserCount(), 0))
                    .setOrderPayPrice(ObjUtil.defaultIfNull(orderVo.getOrderPayPrice(), 0));
        });
    }

    SalesPartnerStatisticsSummaryRespVO convert(SalesPayStatisticsRechargeSummaryRespBO rechargeSummary, Integer expensePrice, Integer userCount);

    SalesPartnerStatisticsAnalyseRespVO convert(Integer visitUserCount, Integer orderUserCount, Integer payUserCount, int atv,
                                SalesStatisticsDataComparisonRespVO<SalesPartnerStatisticsAnalyseDataRespVO> comparison);

}
