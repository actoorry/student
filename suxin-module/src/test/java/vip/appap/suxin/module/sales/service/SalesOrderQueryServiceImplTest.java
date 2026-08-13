package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.product.service.ProductDisplayConfigService;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesOrderActivityPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesOrderActivityRespVO;
import vip.appap.suxin.module.sales.dal.mysql.SalesOrderItemMapper;
import vip.appap.suxin.module.sales.enums.SalesOrderItemAfterSaleStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderStatusEnum;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SalesOrderQueryServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SalesOrderQueryServiceImpl service;

    @Mock
    private SalesOrderItemMapper tradeOrderItemMapper;
    @Mock
    private ProductDisplayConfigService productDisplayConfigService;

    @Test
    void hasPaidOrderItemOwnership_paidNonRefundedOrderItem_returnsTrue() {
        when(tradeOrderItemMapper.selectOwnedPaidOrderItemId(10L, 654L, 901L,
                List.of(SalesOrderStatusEnum.UNDELIVERED.getStatus(), SalesOrderStatusEnum.DELIVERED.getStatus(),
                        SalesOrderStatusEnum.COMPLETED.getStatus()),
                SalesOrderItemAfterSaleStatusEnum.NONE.getStatus())).thenReturn(1001L);

        boolean result = service.hasPaidOrderItemOwnership(10L, 654L, 901L);

        assertTrue(result);
    }

    @Test
    void hasPaidOrderItemOwnership_unpaidOrMissingOrderItem_returnsFalse() {
        when(tradeOrderItemMapper.selectOwnedPaidOrderItemId(10L, 654L, 901L,
                List.of(SalesOrderStatusEnum.UNDELIVERED.getStatus(), SalesOrderStatusEnum.DELIVERED.getStatus(),
                        SalesOrderStatusEnum.COMPLETED.getStatus()),
                SalesOrderItemAfterSaleStatusEnum.NONE.getStatus())).thenReturn(null);

        boolean result = service.hasPaidOrderItemOwnership(10L, 654L, 901L);

        assertFalse(result);
    }

    @Test
    void hasPaidOrderItemOwnership_refundedOrderItem_returnsFalse() {
        when(tradeOrderItemMapper.selectOwnedPaidOrderItemId(10L, 654L, 901L,
                List.of(SalesOrderStatusEnum.UNDELIVERED.getStatus(), SalesOrderStatusEnum.DELIVERED.getStatus(),
                        SalesOrderStatusEnum.COMPLETED.getStatus()),
                SalesOrderItemAfterSaleStatusEnum.NONE.getStatus())).thenReturn(null);

        boolean result = service.hasPaidOrderItemOwnership(10L, 654L, 901L);

        assertFalse(result);
    }

    @Test
    void hasPaidOrderItemOwnership_noProductIdentity_returnsFalseWithoutQuery() {
        boolean result = service.hasPaidOrderItemOwnership(10L, null, null);

        assertFalse(result);
        verify(tradeOrderItemMapper, never()).selectOwnedPaidOrderItemId(null, null, null, null, null);
    }

    @Test
    void getMyActivityPage_sceneConfigured_returnsOrderDerivedActivities() {
        AppSalesOrderActivityPageReqVO reqVO = new AppSalesOrderActivityPageReqVO();
        AppSalesOrderActivityRespVO item = new AppSalesOrderActivityRespVO();
        item.setOrderItemId(1001L);
        when(productDisplayConfigService.getEnabledCategoryIdsBySceneCode(
                ProductDisplayConfigService.SCENE_OFFLINE_ACTIVITY_PAGE)).thenReturn(List.of(95L));
        when(tradeOrderItemMapper.selectMyActivityPage(eq(reqVO), eq(10L), eq(95L),
                eq(List.of(SalesOrderStatusEnum.UNDELIVERED.getStatus(), SalesOrderStatusEnum.DELIVERED.getStatus(),
                        SalesOrderStatusEnum.COMPLETED.getStatus())),
                eq(SalesOrderItemAfterSaleStatusEnum.NONE.getStatus()))).thenReturn(new PageResult<>(List.of(item), 1L));

        PageResult<AppSalesOrderActivityRespVO> result = service.getMyActivityPage(10L, reqVO);

        assertEquals(1L, result.getTotal());
        assertEquals(1001L, result.getList().get(0).getOrderItemId());
    }

    @Test
    void getMyActivityPage_sceneMissing_returnsEmptyPage() {
        AppSalesOrderActivityPageReqVO reqVO = new AppSalesOrderActivityPageReqVO();
        when(productDisplayConfigService.getEnabledCategoryIdsBySceneCode(
                ProductDisplayConfigService.SCENE_OFFLINE_ACTIVITY_PAGE)).thenReturn(List.of());

        PageResult<AppSalesOrderActivityRespVO> result = service.getMyActivityPage(10L, reqVO);

        assertEquals(0L, result.getTotal());
        assertTrue(result.getList().isEmpty());
        verify(tradeOrderItemMapper, never()).selectMyActivityPage(any(), any(), any(), any(), any());
    }

}
