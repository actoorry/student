package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.sales.controller.admin.vo.SalesStatisticsDataComparisonRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会员信息的统计 Service 接口
 *
 * @author owen
 */
public interface SalesPartnerStatisticsService {

    /**
     * 获取会员统计（实时统计）
     *
     * @return 会员统计
     */
    SalesPartnerStatisticsSummaryRespVO getMemberSummary();

    /**
     * 获取会员分析对照数据
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 会员分析对照数据
     */
    SalesStatisticsDataComparisonRespVO<SalesPartnerStatisticsAnalyseDataRespVO> getMemberAnalyseComparisonData(LocalDateTime beginTime,
                                                                                 LocalDateTime endTime);

    /**
     * 按照省份，获得会员统计列表
     *
     * @return 会员统计列表
     */
    List<SalesPartnerStatisticsAreaStatisticsRespVO> getMemberAreaStatisticsList();

    /**
     * 按照性别，获得会员统计列表
     *
     * @return 会员统计列表
     */
    List<SalesPartnerStatisticsSexStatisticsRespVO> getMemberSexStatisticsList();

    /**
     * 按照终端，获得会员统计列表
     *
     * @return 会员统计列表
     */
    List<SalesPartnerStatisticsTerminalStatisticsRespVO> getMemberTerminalStatisticsList();

    /**
     * 获取用户注册数量列表
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 注册数量列表
     */
    List<SalesPartnerStatisticsRegisterCountRespVO> getMemberRegisterCountList(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获得用户数量量统计对照
     *
     * @return 用户数量量统计对照
     */
    SalesStatisticsDataComparisonRespVO<SalesPartnerStatisticsCountRespVO> getUserCountComparison();

}
