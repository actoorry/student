package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderDeliveryReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesOrderMapper;
import vip.appap.suxin.module.sales.enums.SalesDeliveryTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderRefundStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderStatusEnum;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * 销售订单发货-电子面单保护规则测试
 *
 * 覆盖：手工单号发货不得覆盖有效电子面单；仅快递配送可发货。
 */
class SalesOrderUpdateServiceImplWaybillRuleTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SalesOrderUpdateServiceImpl service;

    @Mock
    private SalesOrderMapper tradeOrderMapper;
    @Mock
    private SalesElectronicWaybillService waybillService;
    @Mock
    private List<SalesOrderHandler> tradeOrderHandlers;

    private SalesOrderDO undeliveredExpressOrder() {
        SalesOrderDO order = new SalesOrderDO();
        order.setId(1L);
        order.setStatus(SalesOrderStatusEnum.UNDELIVERED.getStatus());
        order.setDeliveryType(SalesDeliveryTypeEnum.EXPRESS.getType());
        order.setRefundStatus(SalesOrderRefundStatusEnum.NONE.getStatus());
        return order;
    }

    private SalesOrderDeliveryReqVO manualDeliveryReq() {
        SalesOrderDeliveryReqVO reqVO = new SalesOrderDeliveryReqVO();
        reqVO.setId(1L);
        reqVO.setLogisticsId(1L);
        reqVO.setLogisticsNo("SF123");
        return reqVO;
    }

    @Test
    void manualDelivery_validWaybillExists_rejectsOverwrite() {
        when(tradeOrderMapper.selectById(1L)).thenReturn(undeliveredExpressOrder());
        when(waybillService.getValidByOrderId(1L)).thenReturn(new SalesElectronicWaybillDO());
        ServiceException ex = assertThrows(ServiceException.class, () -> service.deliveryOrder(manualDeliveryReq()));
        assertEquals(1_011_004_308, ex.getCode()); // WAYBILL_ORDER_DELIVERY_OVERWRITE_FORBIDDEN
    }

    @Test
    void manualDelivery_nonExpress_rejects() {
        SalesOrderDO order = undeliveredExpressOrder();
        order.setDeliveryType(SalesDeliveryTypeEnum.PICK_UP.getType());
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.deliveryOrder(manualDeliveryReq()));
        assertEquals(1_011_000_024, ex.getCode()); // ORDER_DELIVERY_FAIL_DELIVERY_TYPE_NOT_EXPRESS
    }

}
