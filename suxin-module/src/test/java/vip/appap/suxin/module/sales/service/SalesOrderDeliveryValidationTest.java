package vip.appap.suxin.module.sales.service;

import cn.hutool.extra.spring.SpringUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderDeliveryReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesOrderMapper;
import vip.appap.suxin.module.sales.enums.SalesDeliveryTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderRefundStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.List;

import static vip.appap.suxin.framework.test.core.util.AssertUtils.assertServiceException;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.EXPRESS_STATUS_NOT_ENABLE;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.ORDER_DELIVERY_FAIL_DELIVERY_TYPE_NOT_EXPRESS;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.ORDER_DELIVERY_FAIL_LOGISTICS_NO_BLANK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SalesOrderUpdateServiceImpl#deliveryOrder} 的单元测试
 *
 * 覆盖实际快递成功、公司禁用、空白单号、无需发货（logisticsId=0）与非快递订单拒绝。
 */
public class SalesOrderDeliveryValidationTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SalesOrderUpdateServiceImpl service;

    @Mock
    private SalesOrderMapper tradeOrderMapper;
    @Mock
    private List<SalesOrderHandler> tradeOrderHandlers;
    @Mock
    private SalesDeliveryExpressService deliveryExpressService;
    @Mock
    private SalesMessageService tradeMessageService;
    @Mock
    private SalesElectronicWaybillService waybillService;

    @BeforeEach
    void stubNoValidWaybill() {
        // 手工发货路径会先校验无有效电子面单（deliveryOrder 第 1.3 步）；
        // nonExpress 用例在第 1.2 步提前返回，故用 lenient 避免 UnnecessaryStubbing
        lenient().when(waybillService.getValidByOrderId(any())).thenReturn(null);
    }

    @Test
    @DisplayName("非快递订单：拒绝快递发货")
    public void testDeliveryOrder_nonExpress_rejects() {
        SalesOrderDO order = undeliveredOrder(SalesDeliveryTypeEnum.ONLINE.getType());
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);

        assertServiceException(() -> service.deliveryOrder(deliveryReq(1L, 1L, "SF123")),
                ORDER_DELIVERY_FAIL_DELIVERY_TYPE_NOT_EXPRESS);
        verify(tradeOrderMapper, never()).updateByIdAndStatus(any(), any(), any());
    }

    @Test
    @DisplayName("实际快递：空白运单号拒绝")
    public void testDeliveryOrder_blankLogisticsNo_rejects() {
        SalesOrderDO order = undeliveredOrder(SalesDeliveryTypeEnum.EXPRESS.getType());
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        when(deliveryExpressService.validateDeliveryExpress(1L))
                .thenReturn(new SalesDeliveryExpressDO().setId(1L).setName("顺丰"));

        assertServiceException(() -> service.deliveryOrder(deliveryReq(1L, 1L, "   ")),
                ORDER_DELIVERY_FAIL_LOGISTICS_NO_BLANK);
        verify(tradeOrderMapper, never()).updateByIdAndStatus(any(), any(), any());
    }

    @Test
    @DisplayName("实际快递：公司被禁用拒绝")
    public void testDeliveryOrder_disabledExpress_rejects() {
        SalesOrderDO order = undeliveredOrder(SalesDeliveryTypeEnum.EXPRESS.getType());
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        when(deliveryExpressService.validateDeliveryExpress(1L)).thenThrow(
                new vip.appap.suxin.framework.common.exception.ServiceException(EXPRESS_STATUS_NOT_ENABLE));

        assertServiceException(() -> service.deliveryOrder(deliveryReq(1L, 1L, "SF123")), EXPRESS_STATUS_NOT_ENABLE);
        verify(tradeOrderMapper, never()).updateByIdAndStatus(any(), any(), any());
    }

    @Test
    @DisplayName("实际快递：启用公司 + 非空单号发货成功")
    public void testDeliveryOrder_expressSuccess() {
        SalesOrderDO order = undeliveredOrder(SalesDeliveryTypeEnum.EXPRESS.getType());
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        when(deliveryExpressService.validateDeliveryExpress(1L))
                .thenReturn(new SalesDeliveryExpressDO().setId(1L).setName("顺丰"));
        when(tradeOrderMapper.updateByIdAndStatus(eq(1L), eq(SalesOrderStatusEnum.UNDELIVERED.getStatus()), any()))
                .thenReturn(1);

        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            SalesOrderUpdateServiceImpl deliveryProxy = mock(SalesOrderUpdateServiceImpl.class);
            springUtil.when(() -> SpringUtil.getBean(SalesOrderUpdateServiceImpl.class)).thenReturn(deliveryProxy);

            service.deliveryOrder(deliveryReq(1L, 1L, " SF123 "));

            ArgumentCaptor<SalesOrderDO> captor = ArgumentCaptor.forClass(SalesOrderDO.class);
            verify(tradeOrderMapper).updateByIdAndStatus(eq(1L), eq(SalesOrderStatusEnum.UNDELIVERED.getStatus()), captor.capture());
            SalesOrderDO updateObj = captor.getValue();
            assertEquals(1L, updateObj.getLogisticsId());
            assertEquals("SF123", updateObj.getLogisticsNo()); // trim 后
            assertEquals(SalesOrderStatusEnum.DELIVERED.getStatus(), updateObj.getStatus());
        }
    }

    @Test
    @DisplayName("无需发货：logisticsId=0 保留空单号，不查询快递公司")
    public void testDeliveryOrder_noDelivery_keepsZero() {
        SalesOrderDO order = undeliveredOrder(SalesDeliveryTypeEnum.EXPRESS.getType());
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        when(tradeOrderMapper.updateByIdAndStatus(eq(1L), eq(SalesOrderStatusEnum.UNDELIVERED.getStatus()), any()))
                .thenReturn(1);

        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            SalesOrderUpdateServiceImpl deliveryProxy = mock(SalesOrderUpdateServiceImpl.class);
            springUtil.when(() -> SpringUtil.getBean(SalesOrderUpdateServiceImpl.class)).thenReturn(deliveryProxy);

            service.deliveryOrder(deliveryReq(1L, 0L, ""));

            verify(deliveryExpressService, never()).validateDeliveryExpress(any());
            ArgumentCaptor<SalesOrderDO> captor = ArgumentCaptor.forClass(SalesOrderDO.class);
            verify(tradeOrderMapper).updateByIdAndStatus(eq(1L), eq(SalesOrderStatusEnum.UNDELIVERED.getStatus()), captor.capture());
            assertEquals(0L, captor.getValue().getLogisticsId());
            assertEquals("", captor.getValue().getLogisticsNo());
        }
    }

    // ========== 工具方法 ==========

    private SalesOrderDO undeliveredOrder(Integer deliveryType) {
        return SalesOrderDO.builder()
                .id(1L)
                .userId(100L)
                .status(SalesOrderStatusEnum.UNDELIVERED.getStatus())
                .refundStatus(SalesOrderRefundStatusEnum.NONE.getStatus())
                .deliveryType(deliveryType)
                .build();
    }

    private SalesOrderDeliveryReqVO deliveryReq(Long id, Long logisticsId, String logisticsNo) {
        SalesOrderDeliveryReqVO reqVO = new SalesOrderDeliveryReqVO();
        reqVO.setId(id);
        reqVO.setLogisticsId(logisticsId);
        reqVO.setLogisticsNo(logisticsNo);
        return reqVO;
    }

}
