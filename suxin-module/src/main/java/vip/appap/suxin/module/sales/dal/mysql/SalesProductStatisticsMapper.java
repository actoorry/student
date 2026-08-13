package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.pojo.SortablePageParam;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.framework.mybatis.core.query.MPJLambdaWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesProductStatisticsReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesProductStatisticsRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesProductStatisticsDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

import static vip.appap.suxin.framework.mybatis.core.util.MyBatisUtils.toUnderlineCase;

/**
 * 商品统计 Mapper
 *
 * @author owen
 */
@Mapper
public interface SalesProductStatisticsMapper extends BaseMapperX<SalesProductStatisticsDO> {

    default PageResult<SalesProductStatisticsDO> selectPageGroupBySpuId(SalesProductStatisticsReqVO reqVO, SortablePageParam pageParam) {
        return selectPage(pageParam, buildWrapper(reqVO)
                .groupBy(SalesProductStatisticsDO::getSpuId)
                .select(SalesProductStatisticsDO::getSpuId)
        );
    }

    default List<SalesProductStatisticsDO> selectListByTimeBetween(SalesProductStatisticsReqVO reqVO) {
        return selectList(buildWrapper(reqVO)
                .groupBy(SalesProductStatisticsDO::getTime)
                .select(SalesProductStatisticsDO::getTime));
    }

    default SalesProductStatisticsRespVO selectVoByTimeBetween(SalesProductStatisticsReqVO reqVO) {
        return selectJoinOne(SalesProductStatisticsRespVO.class, buildWrapper(reqVO));
    }

    /**
     * 构建 LambdaWrapper
     *
     * @param reqVO 查询参数
     * @return LambdaWrapper
     */
    private static MPJLambdaWrapperX<SalesProductStatisticsDO> buildWrapper(SalesProductStatisticsReqVO reqVO) {
        return new MPJLambdaWrapperX<SalesProductStatisticsDO>()
                .betweenIfPresent(SalesProductStatisticsDO::getTime, reqVO.getTimes())
                .selectSum(SalesProductStatisticsDO::getBrowseCount, toUnderlineCase(SalesProductStatisticsDO::getBrowseCount))
                .selectSum(SalesProductStatisticsDO::getBrowseUserCount, toUnderlineCase(SalesProductStatisticsDO::getBrowseUserCount))
                .selectSum(SalesProductStatisticsDO::getFavoriteCount, toUnderlineCase(SalesProductStatisticsDO::getFavoriteCount))
                .selectSum(SalesProductStatisticsDO::getCartCount, toUnderlineCase(SalesProductStatisticsDO::getCartCount))
                .selectSum(SalesProductStatisticsDO::getOrderCount, toUnderlineCase(SalesProductStatisticsDO::getOrderCount))
                .selectSum(SalesProductStatisticsDO::getOrderPayCount, toUnderlineCase(SalesProductStatisticsDO::getOrderPayCount))
                .selectSum(SalesProductStatisticsDO::getOrderPayPrice, toUnderlineCase(SalesProductStatisticsDO::getOrderPayPrice))
                .selectSum(SalesProductStatisticsDO::getAfterSaleCount, toUnderlineCase(SalesProductStatisticsDO::getAfterSaleCount))
                .selectSum(SalesProductStatisticsDO::getAfterSaleRefundPrice, toUnderlineCase(SalesProductStatisticsDO::getAfterSaleRefundPrice))
                .selectAvg(SalesProductStatisticsDO::getBrowseConvertPercent, toUnderlineCase(SalesProductStatisticsDO::getBrowseConvertPercent));
    }

    /**
     * 根据时间范围统计商品信息
     *
     * @param page      分页参数
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 统计
     */
    IPage<SalesProductStatisticsDO> selectStatisticsResultPageByTimeBetween(IPage<SalesProductStatisticsDO> page,
                                                                       @Param("beginTime") LocalDateTime beginTime,
                                                                       @Param("endTime") LocalDateTime endTime);

    default Long selectCountByTimeBetween(LocalDateTime beginTime, LocalDateTime endTime) {
        return selectCount(new LambdaQueryWrapperX<SalesProductStatisticsDO>().between(SalesProductStatisticsDO::getTime, beginTime, endTime));
    }

}