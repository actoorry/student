package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.framework.tenant.core.context.TenantContextHolder;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressCreateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesDeliveryExpressMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesDeliveryReferenceMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static vip.appap.suxin.framework.test.core.util.AssertUtils.assertServiceException;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.EXPRESS_CODE_DUPLICATE;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.EXPRESS_NOT_EXISTS;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.EXPRESS_REFERENCED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SalesDeliveryExpressServiceImpl} 的单元测试
 *
 * 覆盖编码唯一、状态、订单/售后引用删除保护、未引用删除与启用列表排序。
 */
public class SalesDeliveryExpressServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SalesDeliveryExpressServiceImpl expressService;

    @Mock
    private SalesDeliveryExpressMapper deliveryExpressMapper;
    @Mock
    private SalesDeliveryReferenceMapper deliveryReferenceMapper;

    @BeforeEach
    public void setUp() {
        TenantContextHolder.setTenantId(1L);
    }

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("创建：编码重复拒绝")
    public void testCreateExpress_codeDuplicate() {
        when(deliveryExpressMapper.selectByCode("shentong")).thenReturn(new SalesDeliveryExpressDO().setId(1L));
        SalesDeliveryExpressCreateReqVO reqVO = new SalesDeliveryExpressCreateReqVO();
        reqVO.setCode("shentong");

        assertServiceException(() -> expressService.createDeliveryExpress(reqVO), EXPRESS_CODE_DUPLICATE);
        verify(deliveryExpressMapper, never()).insert(org.mockito.ArgumentMatchers.any(SalesDeliveryExpressDO.class));
    }

    @Test
    @DisplayName("删除：被订单引用拒绝删除")
    public void testDeleteExpress_referencedByOrder_rejects() {
        when(deliveryExpressMapper.selectById(1L)).thenReturn(new SalesDeliveryExpressDO().setId(1L));
        when(deliveryReferenceMapper.selectCountOrderByLogisticsId(1L, 1L)).thenReturn(3L);

        assertServiceException(() -> expressService.deleteDeliveryExpress(1L), EXPRESS_REFERENCED);
        verify(deliveryExpressMapper, never()).deleteById(1L);
    }

    @Test
    @DisplayName("删除：被售后引用拒绝删除")
    public void testDeleteExpress_referencedByAfterSale_rejects() {
        when(deliveryExpressMapper.selectById(1L)).thenReturn(new SalesDeliveryExpressDO().setId(1L));
        when(deliveryReferenceMapper.selectCountOrderByLogisticsId(1L, 1L)).thenReturn(0L);
        when(deliveryReferenceMapper.selectCountAfterSaleByLogisticsId(1L, 1L)).thenReturn(1L);

        assertServiceException(() -> expressService.deleteDeliveryExpress(1L), EXPRESS_REFERENCED);
        verify(deliveryExpressMapper, never()).deleteById(1L);
    }

    @Test
    @DisplayName("删除：未引用时逻辑删除")
    public void testDeleteExpress_unreferenced_deletes() {
        when(deliveryExpressMapper.selectById(1L)).thenReturn(new SalesDeliveryExpressDO().setId(1L));
        when(deliveryReferenceMapper.selectCountOrderByLogisticsId(1L, 1L)).thenReturn(0L);
        when(deliveryReferenceMapper.selectCountAfterSaleByLogisticsId(1L, 1L)).thenReturn(0L);

        expressService.deleteDeliveryExpress(1L);

        verify(deliveryExpressMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除：公司不存在拒绝")
    public void testDeleteExpress_notExists_rejects() {
        when(deliveryExpressMapper.selectById(1L)).thenReturn(null);
        assertServiceException(() -> expressService.deleteDeliveryExpress(1L), EXPRESS_NOT_EXISTS);
        verify(deliveryExpressMapper, never()).deleteById(1L);
    }

    @Test
    @DisplayName("启用列表按排序返回")
    public void testListByStatus_sorted() {
        SalesDeliveryExpressDO shunfeng = new SalesDeliveryExpressDO().setId(2L).setCode("shunfeng").setSort(50);
        SalesDeliveryExpressDO shentong = new SalesDeliveryExpressDO().setId(1L).setCode("shentong").setSort(20);
        when(deliveryExpressMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(List.of(shentong, shunfeng));

        List<SalesDeliveryExpressDO> list = expressService.getDeliveryExpressListByStatus(CommonStatusEnum.ENABLE.getStatus());

        assertEquals(2, list.size());
        assertEquals("shentong", list.get(0).getCode());
        assertEquals("shunfeng", list.get(1).getCode());
    }

}
