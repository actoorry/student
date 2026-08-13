package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesAfterSaleDeliveryReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesAfterSaleDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesAfterSaleMapper;
import vip.appap.suxin.module.sales.enums.SalesAfterSaleStatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static vip.appap.suxin.framework.test.core.util.AssertUtils.assertServiceException;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.AFTER_SALE_DELIVERY_FAIL_LOGISTICS_NO_BLANK;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.AFTER_SALE_DELIVERY_FAIL_STATUS_NOT_SELLER_AGREE;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.EXPRESS_STATUS_NOT_ENABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SalesAfterSaleServiceImpl#deliveryAfterSale} 的单元测试
 *
 * 覆盖售后退货成功、空白单号、公司停用、状态不符与失败时售后状态不改变。
 */
public class SalesAfterSaleDeliveryValidationTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SalesAfterSaleServiceImpl service;

    @Mock
    private SalesAfterSaleMapper tradeAfterSaleMapper;
    @Mock
    private SalesDeliveryExpressService deliveryExpressService;

    @Test
    @DisplayName("退货成功：启用公司 + 非空单号更新售后物流")
    public void testDeliveryAfterSale_success() {
        SalesAfterSaleDO afterSale = new SalesAfterSaleDO().setId(10L).setStatus(SalesAfterSaleStatusEnum.SELLER_AGREE.getStatus());
        when(tradeAfterSaleMapper.selectByIdAndUserId(10L, 100L)).thenReturn(afterSale);
        when(deliveryExpressService.validateDeliveryExpress(1L))
                .thenReturn(new SalesDeliveryExpressDO().setId(1L).setName("顺丰"));
        when(tradeAfterSaleMapper.updateByIdAndStatus(eq(10L), eq(SalesAfterSaleStatusEnum.SELLER_AGREE.getStatus()), any()))
                .thenReturn(1);

        service.deliveryAfterSale(100L, deliveryReq(10L, 1L, " SF123 "));

        ArgumentCaptor<SalesAfterSaleDO> captor = ArgumentCaptor.forClass(SalesAfterSaleDO.class);
        verify(tradeAfterSaleMapper).updateByIdAndStatus(eq(10L), eq(SalesAfterSaleStatusEnum.SELLER_AGREE.getStatus()), captor.capture());
        SalesAfterSaleDO updateObj = captor.getValue();
        assertEquals(SalesAfterSaleStatusEnum.BUYER_DELIVERY.getStatus(), updateObj.getStatus());
        assertEquals(1L, updateObj.getLogisticsId());
        assertEquals("SF123", updateObj.getLogisticsNo()); // trim 后
    }

    @Test
    @DisplayName("退货：空白单号拒绝，售后状态不改变")
    public void testDeliveryAfterSale_blankLogisticsNo_rejects() {
        SalesAfterSaleDO afterSale = new SalesAfterSaleDO().setId(10L).setStatus(SalesAfterSaleStatusEnum.SELLER_AGREE.getStatus());
        when(tradeAfterSaleMapper.selectByIdAndUserId(10L, 100L)).thenReturn(afterSale);
        when(deliveryExpressService.validateDeliveryExpress(1L))
                .thenReturn(new SalesDeliveryExpressDO().setId(1L).setName("顺丰"));

        assertServiceException(() -> service.deliveryAfterSale(100L, deliveryReq(10L, 1L, "  ")),
                AFTER_SALE_DELIVERY_FAIL_LOGISTICS_NO_BLANK);
        verify(tradeAfterSaleMapper, never()).updateByIdAndStatus(any(), any(), any());
    }

    @Test
    @DisplayName("退货：公司被停用拒绝")
    public void testDeliveryAfterSale_disabledExpress_rejects() {
        SalesAfterSaleDO afterSale = new SalesAfterSaleDO().setId(10L).setStatus(SalesAfterSaleStatusEnum.SELLER_AGREE.getStatus());
        when(tradeAfterSaleMapper.selectByIdAndUserId(10L, 100L)).thenReturn(afterSale);
        when(deliveryExpressService.validateDeliveryExpress(1L)).thenThrow(
                new vip.appap.suxin.framework.common.exception.ServiceException(EXPRESS_STATUS_NOT_ENABLE));

        assertServiceException(() -> service.deliveryAfterSale(100L, deliveryReq(10L, 1L, "SF123")),
                EXPRESS_STATUS_NOT_ENABLE);
        verify(tradeAfterSaleMapper, never()).updateByIdAndStatus(any(), any(), any());
    }

    @Test
    @DisplayName("退货：售后状态不符拒绝")
    public void testDeliveryAfterSale_statusMismatch_rejects() {
        SalesAfterSaleDO afterSale = new SalesAfterSaleDO().setId(10L).setStatus(SalesAfterSaleStatusEnum.APPLY.getStatus());
        when(tradeAfterSaleMapper.selectByIdAndUserId(10L, 100L)).thenReturn(afterSale);

        assertServiceException(() -> service.deliveryAfterSale(100L, deliveryReq(10L, 1L, "SF123")),
                AFTER_SALE_DELIVERY_FAIL_STATUS_NOT_SELLER_AGREE);
        verify(deliveryExpressService, never()).validateDeliveryExpress(any());
    }

    private AppSalesAfterSaleDeliveryReqVO deliveryReq(Long id, Long logisticsId, String logisticsNo) {
        AppSalesAfterSaleDeliveryReqVO reqVO = new AppSalesAfterSaleDeliveryReqVO();
        reqVO.setId(id);
        reqVO.setLogisticsId(logisticsId);
        reqVO.setLogisticsNo(logisticsNo);
        return reqVO;
    }

}
