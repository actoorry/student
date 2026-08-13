package vip.appap.suxin.module.sales.service;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.ip.core.Area;
import vip.appap.suxin.framework.ip.core.enums.AreaTypeEnum;
import vip.appap.suxin.framework.ip.core.utils.AreaUtils;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesStatisticsDataComparisonRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.convert.SalesPartnerStatisticsConvert;
import vip.appap.suxin.module.sales.dal.mysql.SalesPartnerStatisticsMapper;
import vip.appap.suxin.module.sales.service.SalesApiAccessLogStatisticsService;
import vip.appap.suxin.module.sales.service.bo.SalesPartnerStatisticsAreaStatisticsRespBO;
import vip.appap.suxin.module.sales.service.SalesPayStatisticsWalletService;
import vip.appap.suxin.module.sales.service.bo.SalesPayStatisticsRechargeSummaryRespBO;
import vip.appap.suxin.module.sales.service.SalesOrderStatisticsService;
import vip.appap.suxin.module.sales.service.SalesStatisticsService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 会员信息的统计 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class SalesPartnerStatisticsServiceImpl implements SalesPartnerStatisticsService {

    @Resource
    private SalesPartnerStatisticsMapper memberStatisticsMapper;

    @Resource
    private SalesPayStatisticsWalletService payWalletStatisticsService;
    @Resource
    private SalesStatisticsService tradeStatisticsService;
    @Resource
    private SalesOrderStatisticsService tradeOrderStatisticsService;
    @Resource
    private SalesApiAccessLogStatisticsService apiAccessLogStatisticsService;

    @Override
    public SalesPartnerStatisticsSummaryRespVO getMemberSummary() {
        SalesPayStatisticsRechargeSummaryRespBO rechargeSummary = payWalletStatisticsService.getUserRechargeSummary(null, null);
        // TODO @疯狂：1）这里是实时统计，不好走走 SalesStatistics 表；2）因为这个放在商城下，所以只考虑订单数据，即按照 trade_order 的 pay_price 并且已支付来计算；
        Integer expensePrice = tradeStatisticsService.getExpensePrice(null, null);
        Integer userCount = memberStatisticsMapper.selectUserCount(null, null);
        return SalesPartnerStatisticsConvert.INSTANCE.convert(rechargeSummary, expensePrice, userCount);
    }

    @Override
    public List<SalesPartnerStatisticsAreaStatisticsRespVO> getMemberAreaStatisticsList() {
        // 统计用户
        // TODO @疯狂：可能得把每个省的用户，都查询出来，然后去 order 那边 in；因为要按照这些人为基础来计算；；用户规模量大可能不太好，但是暂时就先这样搞吧 = =
        Map<Integer, Integer> userCountMap = convertMap(memberStatisticsMapper.selectSummaryListByAreaId(),
                vo -> AreaUtils.getParentIdByType(vo.getAreaId(), AreaTypeEnum.PROVINCE),
                SalesPartnerStatisticsAreaStatisticsRespBO::getUserCount, Integer::sum);
        // 统计订单
        Map<Integer, SalesPartnerStatisticsAreaStatisticsRespBO> orderMap = convertMap(tradeOrderStatisticsService.getSummaryListByAreaId(),
                bo -> AreaUtils.getParentIdByType(bo.getAreaId(), AreaTypeEnum.PROVINCE),
                bo -> bo,
                (a, b) -> new SalesPartnerStatisticsAreaStatisticsRespBO()
                        .setOrderCreateUserCount(ObjectUtil.defaultIfNull(a.getOrderCreateUserCount(), 0)
                                + ObjectUtil.defaultIfNull(b.getOrderCreateUserCount(), 0))
                        .setOrderPayUserCount(ObjectUtil.defaultIfNull(a.getOrderPayUserCount(), 0)
                                + ObjectUtil.defaultIfNull(b.getOrderPayUserCount(), 0))
                        .setOrderPayPrice(ObjectUtil.defaultIfNull(a.getOrderPayPrice(), 0)
                                + ObjectUtil.defaultIfNull(b.getOrderPayPrice(), 0)));
        // 拼接数据
        List<Area> areaList = AreaUtils.getByType(AreaTypeEnum.PROVINCE, area -> area);
        areaList.add(new Area().setId(null).setName("未知"));
        return SalesPartnerStatisticsConvert.INSTANCE.convertList(areaList, userCountMap, orderMap);
    }

    @Override
    public SalesStatisticsDataComparisonRespVO<SalesPartnerStatisticsAnalyseDataRespVO> getMemberAnalyseComparisonData(LocalDateTime beginTime, LocalDateTime endTime) {
        // 当前数据
        SalesPartnerStatisticsAnalyseDataRespVO vo = getMemberAnalyseData(beginTime, endTime);
        // 对照数据
        LocalDateTime referenceEndDate = beginTime.minusDays(1); // 减少1天，防止出现时间重叠
        LocalDateTime referenceBeginDate = referenceEndDate.minus(Duration.between(beginTime, endTime));
        SalesPartnerStatisticsAnalyseDataRespVO reference = getMemberAnalyseData(
                LocalDateTimeUtil.beginOfDay(referenceBeginDate), LocalDateTimeUtil.endOfDay(referenceEndDate));
        return new SalesStatisticsDataComparisonRespVO<>(vo, reference);
    }

    private SalesPartnerStatisticsAnalyseDataRespVO getMemberAnalyseData(LocalDateTime beginTime, LocalDateTime endTime) {
        Integer rechargeUserCount = Optional.ofNullable(payWalletStatisticsService.getUserRechargeSummary(beginTime, endTime))
                .map(SalesPayStatisticsRechargeSummaryRespBO::getRechargeUserCount).orElse(0);
        return new SalesPartnerStatisticsAnalyseDataRespVO()
                .setRegisterUserCount(memberStatisticsMapper.selectUserCount(beginTime, endTime))
                .setVisitUserCount(apiAccessLogStatisticsService.getUserCount(UserTypeEnum.MEMBER.getValue(), beginTime, endTime))
                .setRechargeUserCount(rechargeUserCount);
    }

    @Override
    public List<SalesPartnerStatisticsSexStatisticsRespVO> getMemberSexStatisticsList() {
        return memberStatisticsMapper.selectSummaryListBySex();
    }

    @Override
    public List<SalesPartnerStatisticsTerminalStatisticsRespVO> getMemberTerminalStatisticsList() {
        return memberStatisticsMapper.selectSummaryListByRegisterTerminal();
    }

    @Override
    public List<SalesPartnerStatisticsRegisterCountRespVO> getMemberRegisterCountList(LocalDateTime beginTime, LocalDateTime endTime) {
        return memberStatisticsMapper.selectListByCreateTimeBetween(beginTime, endTime);
    }

    @Override
    public SalesStatisticsDataComparisonRespVO<SalesPartnerStatisticsCountRespVO> getUserCountComparison() {
        // 今日时间范围
        LocalDateTime beginOfToday = LocalDateTimeUtil.beginOfDay(LocalDateTime.now());
        LocalDateTime endOfToday = LocalDateTimeUtil.endOfDay(beginOfToday);
        // 昨日时间范围
        LocalDateTime beginOfYesterday = LocalDateTimeUtil.beginOfDay(beginOfToday.minusDays(1));
        LocalDateTime endOfYesterday = LocalDateTimeUtil.endOfDay(beginOfYesterday);
        return new SalesStatisticsDataComparisonRespVO<SalesPartnerStatisticsCountRespVO>()
                .setValue(getUserCount(beginOfToday, endOfToday))
                .setReference(getUserCount(beginOfYesterday, endOfYesterday));
    }

    private SalesPartnerStatisticsCountRespVO getUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
        return new SalesPartnerStatisticsCountRespVO()
                .setRegisterUserCount(memberStatisticsMapper.selectUserCount(beginTime, endTime))
                .setVisitUserCount(apiAccessLogStatisticsService.getIpCount(UserTypeEnum.MEMBER.getValue(), beginTime, endTime));
    }

}
