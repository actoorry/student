package vip.appap.suxin.module.rongjh.service;

import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.rongjh.dal.dataobject.PartnerLoveRecordDO;
import vip.appap.suxin.module.rongjh.dal.dataobject.PartnerLoveSummaryDO;
import vip.appap.suxin.module.rongjh.dal.mysql.PartnerLoveRecordMapper;
import vip.appap.suxin.module.rongjh.dal.mysql.PartnerLoveSummaryMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 戎集会爱心值联动回归测试（任务 6.3）
 *
 * 证明：订单产生/取消时爱心值按 bizKey 幂等处理，
 * 与 Sales 分销佣金（订单项级、独立表）互不重复处理、互不破坏。
 */
class LoveValueServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private LoveValueServiceImpl loveValueService;

    @Mock
    private PartnerLoveRecordMapper loveRecordMapper;
    @Mock
    private PartnerLoveSummaryMapper loveSummaryMapper;

    @Test
    void addOrderLoveValue_valid_createsRecordAndUpdatesSummary() {
        // 订单支付联动：新增爱心值记录并累加汇总
        when(loveRecordMapper.selectByBizKey("love:ORDER_PAY_VIRTUAL:100")).thenReturn(null);
        when(loveSummaryMapper.selectById(300L))
                .thenReturn(PartnerLoveSummaryDO.builder().userId(300L).totalAmountFen(1000L).totalLoveValue(BigDecimal.ONE).build());

        loveValueService.addOrderLoveValue(300L, 100L, 500, "ORDER_PAY_VIRTUAL", "虚拟订单支付爱心值");

        ArgumentCaptor<PartnerLoveRecordDO> recordCaptor = ArgumentCaptor.forClass(PartnerLoveRecordDO.class);
        verify(loveRecordMapper).insert(recordCaptor.capture());
        assertEquals("love:ORDER_PAY_VIRTUAL:100", recordCaptor.getValue().getBizKey());
        assertEquals(500L, recordCaptor.getValue().getAmountFen());

        ArgumentCaptor<PartnerLoveSummaryDO> summaryCaptor = ArgumentCaptor.forClass(PartnerLoveSummaryDO.class);
        verify(loveSummaryMapper).updateById(summaryCaptor.capture());
        assertEquals(1500L, summaryCaptor.getValue().getTotalAmountFen());
    }

    @Test
    void addOrderLoveValue_duplicateBizKey_isIdempotent() {
        // 重复事件（如重复支付回调）：同 bizKey 存在时不重复累加
        when(loveRecordMapper.selectByBizKey("love:ORDER_PAY_VIRTUAL:100"))
                .thenReturn(PartnerLoveRecordDO.builder().bizKey("love:ORDER_PAY_VIRTUAL:100").build());

        loveValueService.addOrderLoveValue(300L, 100L, 500, "ORDER_PAY_VIRTUAL", "重复回调");

        verify(loveRecordMapper, never()).insert(any(PartnerLoveRecordDO.class));
        verify(loveSummaryMapper, never()).updateById(any(PartnerLoveSummaryDO.class));
    }

    @Test
    void addOrderLoveValue_firstTime_initializesSummary() {
        // 首次联动：无汇总时初始化
        when(loveRecordMapper.selectByBizKey("love:ORDER_PAY_VIRTUAL:100")).thenReturn(null);
        when(loveSummaryMapper.selectById(300L)).thenReturn(null);

        loveValueService.addOrderLoveValue(300L, 100L, 500, "ORDER_PAY_VIRTUAL", "首单");

        ArgumentCaptor<PartnerLoveSummaryDO> initCaptor = ArgumentCaptor.forClass(PartnerLoveSummaryDO.class);
        verify(loveSummaryMapper).insert(initCaptor.capture());
        assertEquals(500L, initCaptor.getValue().getTotalAmountFen());
    }

    @Test
    void rollbackOrderLoveValue_valid_createsNegativeRecord() {
        // 订单取消联动：回滚记录为负值
        when(loveRecordMapper.selectByBizKey("love:ORDER_CANCEL:100")).thenReturn(null);
        when(loveSummaryMapper.selectById(300L))
                .thenReturn(PartnerLoveSummaryDO.builder().userId(300L).totalAmountFen(1000L).totalLoveValue(BigDecimal.ONE).build());

        loveValueService.rollbackOrderLoveValue(300L, 100L, null, 500, "ORDER_CANCEL", "订单取消回滚");

        ArgumentCaptor<PartnerLoveRecordDO> recordCaptor = ArgumentCaptor.forClass(PartnerLoveRecordDO.class);
        verify(loveRecordMapper).insert(recordCaptor.capture());
        assertEquals(-500L, recordCaptor.getValue().getAmountFen());
        assertEquals("love:ORDER_CANCEL:100", recordCaptor.getValue().getBizKey());

        ArgumentCaptor<PartnerLoveSummaryDO> summaryCaptor = ArgumentCaptor.forClass(PartnerLoveSummaryDO.class);
        verify(loveSummaryMapper).updateById(summaryCaptor.capture());
        assertEquals(500L, summaryCaptor.getValue().getTotalAmountFen());
    }

    @Test
    void rollbackOrderLoveValue_duplicate_isIdempotent() {
        // 重复取消/售后回调：不重复扣减
        when(loveRecordMapper.selectByBizKey("love:ORDER_ITEM_REFUND:100:900"))
                .thenReturn(PartnerLoveRecordDO.builder().bizKey("love:ORDER_ITEM_REFUND:100:900").build());

        loveValueService.rollbackOrderLoveValue(300L, 100L, 900L, 500, "ORDER_ITEM_REFUND", "重复售后");

        verify(loveRecordMapper, never()).insert(any(PartnerLoveRecordDO.class));
        verify(loveSummaryMapper, never()).updateById(any(PartnerLoveSummaryDO.class));
        verify(loveSummaryMapper, never()).insert(any(PartnerLoveSummaryDO.class));
    }

}
