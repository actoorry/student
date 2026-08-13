package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageRecordDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageUserDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesConfigDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesBrokerageRecordMapper;
import vip.appap.suxin.module.sales.enums.SalesBrokerageRecordBizTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesBrokerageRecordStatusEnum;
import vip.appap.suxin.module.sales.service.bo.SalesBrokerageAddReqBO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SalesBrokerageRecordServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SalesBrokerageRecordServiceImpl brokerageRecordService;

    @Mock
    private SalesBrokerageRecordMapper brokerageRecordMapper;
    @Mock
    private SalesConfigService tradeConfigService;
    @Mock
    private SalesBrokerageUserService brokerageUserService;

    // ========== 支付计佣（任务 4.2） ==========

    @Test
    void addBrokerage_globalDisabled_noRecordsCreated() {
        when(tradeConfigService.getTradeConfig()).thenReturn(salesConfig(false, 0, 10, 0));

        brokerageRecordService.addBrokerage(100L, SalesBrokerageRecordBizTypeEnum.ORDER, List.of(reqBo()));

        verify(brokerageRecordMapper, never()).insertBatch(any());
        verify(brokerageUserService, never()).updateUserFrozenPrice(any(), any());
        verify(brokerageUserService, never()).updateUserPrice(any(), any());
    }

    @Test
    void addBrokerage_withoutFirstPromoter_noRecordsCreated() {
        when(tradeConfigService.getTradeConfig()).thenReturn(salesConfig(true, 0, 10, 0));
        when(brokerageUserService.getBindBrokerageUser(100L)).thenReturn(null);

        brokerageRecordService.addBrokerage(100L, SalesBrokerageRecordBizTypeEnum.ORDER, List.of(reqBo()));

        verify(brokerageRecordMapper, never()).insertBatch(any());
    }

    @Test
    void addBrokerage_firstPromoterDisabled_noRecordsCreated() {
        when(tradeConfigService.getTradeConfig()).thenReturn(salesConfig(true, 0, 10, 0));
        when(brokerageUserService.getBindBrokerageUser(100L))
                .thenReturn(new SalesBrokerageUserDO().setId(300L).setBrokerageEnabled(false));

        brokerageRecordService.addBrokerage(100L, SalesBrokerageRecordBizTypeEnum.ORDER, List.of(reqBo()));

        verify(brokerageRecordMapper, never()).insertBatch(any());
    }

    @Test
    void addBrokerage_withFrozenDays_updatesFrozenPrice() {
        // 有冻结期：一级 10%，10000 分基数 → 1000 分进入冻结
        when(tradeConfigService.getTradeConfig()).thenReturn(salesConfig(true, 30, 10, 5));
        when(brokerageUserService.getBindBrokerageUser(100L))
                .thenReturn(new SalesBrokerageUserDO().setId(300L).setBrokerageEnabled(true));

        brokerageRecordService.addBrokerage(100L, SalesBrokerageRecordBizTypeEnum.ORDER, List.of(reqBo()));

        ArgumentCaptor<List<SalesBrokerageRecordDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(brokerageRecordMapper).insertBatch(captor.capture());
        assertEquals(1, captor.getValue().size());
        SalesBrokerageRecordDO record = captor.getValue().get(0);
        assertEquals(1000, record.getPrice());
        assertEquals(SalesBrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus(), record.getStatus());
        assertEquals(30, record.getFrozenDays());
        assertEquals(1, record.getSourceUserLevel());
        verify(brokerageUserService).updateUserFrozenPrice(300L, 1000);
        verify(brokerageUserService, never()).updateUserPrice(eq(300L), any());
    }

    @Test
    void addBrokerage_withoutFrozenDays_updatesAvailablePrice() {
        // 无冻结期：佣金直接进入可用余额
        when(tradeConfigService.getTradeConfig()).thenReturn(salesConfig(true, 0, 10, 0));
        when(brokerageUserService.getBindBrokerageUser(100L))
                .thenReturn(new SalesBrokerageUserDO().setId(300L).setBrokerageEnabled(true));

        brokerageRecordService.addBrokerage(100L, SalesBrokerageRecordBizTypeEnum.ORDER, List.of(reqBo()));

        ArgumentCaptor<List<SalesBrokerageRecordDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(brokerageRecordMapper).insertBatch(captor.capture());
        SalesBrokerageRecordDO record = captor.getValue().get(0);
        assertEquals(SalesBrokerageRecordStatusEnum.SETTLEMENT.getStatus(), record.getStatus());
        assertTrue(record.getUnfreezeTime() == null);
        verify(brokerageUserService).updateUserPrice(300L, 1000);
        verify(brokerageUserService, never()).updateUserFrozenPrice(eq(300L), any());
    }

    @Test
    void addBrokerage_secondLevelPromoter_createsTwoLevelRecords() {
        // 一级 10% + 二级 5%：两笔佣金
        when(tradeConfigService.getTradeConfig()).thenReturn(salesConfig(true, 0, 10, 5));
        when(brokerageUserService.getBindBrokerageUser(100L))
                .thenReturn(new SalesBrokerageUserDO().setId(300L).setBrokerageEnabled(true).setBindUserId(400L));
        when(brokerageUserService.getBrokerageUser(400L))
                .thenReturn(new SalesBrokerageUserDO().setId(400L).setBrokerageEnabled(true));

        brokerageRecordService.addBrokerage(100L, SalesBrokerageRecordBizTypeEnum.ORDER, List.of(reqBo()));

        ArgumentCaptor<List<SalesBrokerageRecordDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(brokerageRecordMapper, times(2)).insertBatch(captor.capture());
        List<List<SalesBrokerageRecordDO>> batches = captor.getAllValues();
        SalesBrokerageRecordDO first = batches.get(0).get(0);
        SalesBrokerageRecordDO second = batches.get(1).get(0);
        assertEquals(300L, first.getUserId());
        assertEquals(1000, first.getPrice());
        assertEquals(1, first.getSourceUserLevel());
        assertEquals(400L, second.getUserId());
        assertEquals(500, second.getPrice());
        assertEquals(2, second.getSourceUserLevel());
        assertEquals(100L, first.getSourceUserId());
        verify(brokerageUserService).updateUserPrice(300L, 1000);
        verify(brokerageUserService).updateUserPrice(400L, 500);
    }

    @Test
    void addBrokerage_fixedCommission_prefersSkuFixedPrice() {
        // 商品独立佣金：SKU 固定佣金优先于租户比例
        when(tradeConfigService.getTradeConfig()).thenReturn(salesConfig(true, 0, 10, 0));
        when(brokerageUserService.getBindBrokerageUser(100L))
                .thenReturn(new SalesBrokerageUserDO().setId(300L).setBrokerageEnabled(true));

        SalesBrokerageAddReqBO bo = new SalesBrokerageAddReqBO();
        bo.setBizId("900");
        bo.setBasePrice(10000);
        bo.setFirstFixedPrice(5000);
        bo.setSecondFixedPrice(0);
        bo.setTitle("商品");
        bo.setSourceUserId(100L);
        brokerageRecordService.addBrokerage(100L, SalesBrokerageRecordBizTypeEnum.ORDER, List.of(bo));

        ArgumentCaptor<List<SalesBrokerageRecordDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(brokerageRecordMapper).insertBatch(captor.capture());
        assertEquals(5000, captor.getValue().get(0).getPrice());
    }

    @Test
    void calculatePrice_fixedPricePreferred_overPercent() {
        assertEquals(5000, brokerageRecordService.calculatePrice(10000, 10, 5000));
        assertEquals(1000, brokerageRecordService.calculatePrice(10000, 10, null));
        assertEquals(0, brokerageRecordService.calculatePrice(10000, 0, null));
        assertEquals(0, brokerageRecordService.calculatePrice(0, 10, null));
    }

    // ========== 取消/售后冲销（任务 4.4） ==========

    @Test
    void cancelBrokerage_waitSettlement_refundsFrozenPrice() {
        // 待结算记录：从冻结余额冲回
        SalesBrokerageRecordDO record = new SalesBrokerageRecordDO().setId(1L).setUserId(300L).setPrice(1000)
                .setStatus(SalesBrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus());
        when(brokerageRecordMapper.selectListByBizTypeAndBizId(SalesBrokerageRecordBizTypeEnum.ORDER.getType(), "900"))
                .thenReturn(List.of(record));
        when(brokerageRecordMapper.updateByIdAndStatus(eq(1L), eq(record.getStatus()), any()))
                .thenReturn(1);

        brokerageRecordService.cancelBrokerage(SalesBrokerageRecordBizTypeEnum.ORDER, "900");

        verify(brokerageUserService).updateUserFrozenPrice(300L, -1000);
        verify(brokerageUserService, never()).updateUserPrice(eq(300L), any());
    }

    @Test
    void cancelBrokerage_settled_refundsAvailablePrice() {
        // 已结算记录：从可用余额冲回
        SalesBrokerageRecordDO record = new SalesBrokerageRecordDO().setId(1L).setUserId(300L).setPrice(1000)
                .setStatus(SalesBrokerageRecordStatusEnum.SETTLEMENT.getStatus());
        when(brokerageRecordMapper.selectListByBizTypeAndBizId(SalesBrokerageRecordBizTypeEnum.ORDER.getType(), "900"))
                .thenReturn(List.of(record));
        when(brokerageRecordMapper.updateByIdAndStatus(eq(1L), eq(record.getStatus()), any()))
                .thenReturn(1);

        brokerageRecordService.cancelBrokerage(SalesBrokerageRecordBizTypeEnum.ORDER, "900");

        verify(brokerageUserService).updateUserPrice(300L, -1000);
        verify(brokerageUserService, never()).updateUserFrozenPrice(eq(300L), any());
    }

    @Test
    void cancelBrokerage_repeatedCallback_isIdempotent() {
        // 重复回调：记录已取消，状态更新 0 行，不重复扣减余额
        SalesBrokerageRecordDO record = new SalesBrokerageRecordDO().setId(1L).setUserId(300L).setPrice(1000)
                .setStatus(SalesBrokerageRecordStatusEnum.CANCEL.getStatus());
        when(brokerageRecordMapper.selectListByBizTypeAndBizId(SalesBrokerageRecordBizTypeEnum.ORDER.getType(), "900"))
                .thenReturn(List.of(record));
        when(brokerageRecordMapper.updateByIdAndStatus(eq(1L), eq(record.getStatus()), any()))
                .thenReturn(0);

        brokerageRecordService.cancelBrokerage(SalesBrokerageRecordBizTypeEnum.ORDER, "900");

        verify(brokerageUserService, never()).updateUserPrice(any(), any());
        verify(brokerageUserService, never()).updateUserFrozenPrice(any(), any());
    }

    @Test
    void cancelBrokerage_noRecords_returnsSilently() {
        when(brokerageRecordMapper.selectListByBizTypeAndBizId(SalesBrokerageRecordBizTypeEnum.ORDER.getType(), "900"))
                .thenReturn(List.of());

        brokerageRecordService.cancelBrokerage(SalesBrokerageRecordBizTypeEnum.ORDER, "900");

        verify(brokerageUserService, never()).updateUserPrice(any(), any());
        verify(brokerageUserService, never()).updateUserFrozenPrice(any(), any());
    }

    // ========== 到期解冻（任务 4.3） ==========

    @Test
    void unfreezeRecord_matured_movesFrozenToAvailable() {
        SalesBrokerageRecordDO record = new SalesBrokerageRecordDO().setId(1L).setUserId(300L).setPrice(1000)
                .setStatus(SalesBrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus());
        when(brokerageRecordMapper.updateByIdAndStatus(eq(1L), eq(record.getStatus()), any())).thenReturn(1);

        assertTrue(brokerageRecordService.unfreezeRecord(record));

        verify(brokerageUserService).updateFrozenPriceDecrAndPriceIncr(300L, -1000);
    }

    @Test
    void unfreezeRecord_alreadyUnfrozen_isIdempotent() {
        // 重复解冻：状态已变更，更新 0 行，不重复转移余额
        SalesBrokerageRecordDO record = new SalesBrokerageRecordDO().setId(1L).setUserId(300L).setPrice(1000)
                .setStatus(SalesBrokerageRecordStatusEnum.SETTLEMENT.getStatus());
        when(brokerageRecordMapper.updateByIdAndStatus(eq(1L), eq(record.getStatus()), any())).thenReturn(0);

        assertFalse(brokerageRecordService.unfreezeRecord(record));

        verify(brokerageUserService, never()).updateFrozenPriceDecrAndPriceIncr(any(), any());
    }

    private SalesConfigDO salesConfig(boolean enabled, int frozenDays, int firstPercent, int secondPercent) {
        SalesConfigDO config = new SalesConfigDO();
        config.setBrokerageEnabled(enabled);
        config.setBrokerageFrozenDays(frozenDays);
        config.setBrokerageFirstPercent(firstPercent);
        config.setBrokerageSecondPercent(secondPercent);
        return config;
    }

    private SalesBrokerageAddReqBO reqBo() {
        SalesBrokerageAddReqBO bo = new SalesBrokerageAddReqBO();
        bo.setBizId("900");
        bo.setBasePrice(10000);
        bo.setFirstFixedPrice(null);
        bo.setSecondFixedPrice(null);
        bo.setTitle("商品");
        bo.setSourceUserId(100L);
        return bo;
    }

}
