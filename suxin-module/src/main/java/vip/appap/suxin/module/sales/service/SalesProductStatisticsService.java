package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.pojo.SortablePageParam;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesStatisticsDataComparisonRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesProductStatisticsReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesProductStatisticsRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesProductStatisticsDO;

import java.util.List;

/**
 * 商品统计 Service 接口
 *
 * @author owen
 */
public interface SalesProductStatisticsService {

    /**
     * 获得商品统计排行榜分页
     *
     * @param reqVO     查询条件
     * @param pageParam 分页排序查询
     * @return 商品统计分页
     */
    PageResult<SalesProductStatisticsDO> getProductStatisticsRankPage(SalesProductStatisticsReqVO reqVO, SortablePageParam pageParam);

    /**
     * 获得商品状况统计分析
     *
     * @param reqVO 查询条件
     * @return 统计数据对照
     */
    SalesStatisticsDataComparisonRespVO<SalesProductStatisticsRespVO> getProductStatisticsAnalyse(SalesProductStatisticsReqVO reqVO);

    /**
     * 获得商品状况明细
     *
     * @param reqVO 查询条件
     * @return 统计数据对照
     */
    List<SalesProductStatisticsDO> getProductStatisticsList(SalesProductStatisticsReqVO reqVO);

    /**
     * 统计指定天数的商品数据
     *
     * @return 统计结果
     */
    String statisticsProduct(Integer days);

}