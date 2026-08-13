package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.framework.tenant.core.context.TenantContextHolder;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressTemplateChargeBaseVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressTemplateCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressTemplateFreeBaseVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressTemplateChargeDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressTemplateDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesDeliveryExpressTemplateChargeMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesDeliveryExpressTemplateFreeMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesDeliveryExpressTemplateMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesDeliveryReferenceMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static vip.appap.suxin.framework.test.core.util.AssertUtils.assertServiceException;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.EXPRESS_TEMPLATE_CHARGE_AREA_DUPLICATE;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.EXPRESS_TEMPLATE_FREE_AREA_DUPLICATE;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.EXPRESS_TEMPLATE_NAME_DUPLICATE;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.EXPRESS_TEMPLATE_NOT_EXISTS;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.EXPRESS_TEMPLATE_REFERENCED_BY_SPU;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SalesDeliveryExpressTemplateServiceImpl} 的单元测试
 *
 * 覆盖名称唯一、计费/包邮区域重复、单事务保存异常传播、商品引用删除保护与未引用删除。
 */
public class SalesDeliveryExpressTemplateServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SalesDeliveryExpressTemplateServiceImpl templateService;

    @Mock
    private SalesDeliveryExpressTemplateMapper expressTemplateMapper;
    @Mock
    private SalesDeliveryExpressTemplateChargeMapper expressTemplateChargeMapper;
    @Mock
    private SalesDeliveryExpressTemplateFreeMapper expressTemplateFreeMapper;
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
    @DisplayName("创建：名称重复拒绝")
    public void testCreateTemplate_nameDuplicate() {
        when(expressTemplateMapper.selectByName("模板A")).thenReturn(new SalesDeliveryExpressTemplateDO().setId(1L));
        assertServiceException(() -> templateService.createDeliveryExpressTemplate(createReqVO("模板A")),
                EXPRESS_TEMPLATE_NAME_DUPLICATE);
        verify(expressTemplateMapper, never()).insert(any(SalesDeliveryExpressTemplateDO.class));
    }

    @Test
    @DisplayName("创建：计费规则区域重复拒绝")
    public void testCreateTemplate_chargeAreaDuplicate() {
        SalesDeliveryExpressTemplateCreateReqVO reqVO = createReqVO("模板A");
        reqVO.setCharges(List.of(
                charge("[1,120000]", 5, 1000, 5, 500),
                charge("[1,120000]", 10, 1000, 5, 500)
        ));
        assertServiceException(() -> templateService.createDeliveryExpressTemplate(reqVO),
                EXPRESS_TEMPLATE_CHARGE_AREA_DUPLICATE);
        verify(expressTemplateMapper, never()).insert(any(SalesDeliveryExpressTemplateDO.class));
    }

    @Test
    @DisplayName("创建：包邮规则区域重复拒绝")
    public void testCreateTemplate_freeAreaDuplicate() {
        SalesDeliveryExpressTemplateCreateReqVO reqVO = createReqVO("模板A");
        reqVO.setFrees(List.of(
                free("[1,120000]", 10, 5000),
                free("[1,120000]", 20, 5000)
        ));
        assertServiceException(() -> templateService.createDeliveryExpressTemplate(reqVO),
                EXPRESS_TEMPLATE_FREE_AREA_DUPLICATE);
        verify(expressTemplateMapper, never()).insert(any(SalesDeliveryExpressTemplateDO.class));
    }

    @Test
    @DisplayName("创建：子规则写入失败时异常传播（单事务回滚由 @Transactional 保证）")
    public void testCreateTemplate_chargeInsertFailure_propagates() {
        when(expressTemplateMapper.insert(any(SalesDeliveryExpressTemplateDO.class))).thenReturn(1);
        RuntimeException dbFailure = new RuntimeException("charge insert failed");
        org.mockito.Mockito.doThrow(dbFailure).when(expressTemplateChargeMapper)
                .insertBatch(org.mockito.ArgumentMatchers.anyList());

        RuntimeException actual = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> templateService.createDeliveryExpressTemplate(createReqVO("模板A")));

        org.junit.jupiter.api.Assertions.assertEquals(dbFailure, actual);
    }

    @Test
    @DisplayName("删除：被商品引用拒绝删除")
    public void testDeleteTemplate_referencedBySpu_rejects() {
        when(expressTemplateMapper.selectById(1L)).thenReturn(new SalesDeliveryExpressTemplateDO().setId(1L));
        when(deliveryReferenceMapper.selectCountProductByTemplateId(1L, 1L)).thenReturn(3L);

        assertServiceException(() -> templateService.deleteDeliveryExpressTemplate(1L),
                EXPRESS_TEMPLATE_REFERENCED_BY_SPU);
        verify(expressTemplateMapper, never()).deleteById(1L);
    }

    @Test
    @DisplayName("删除：未引用时主表与两类子规则一起逻辑删除")
    public void testDeleteTemplate_unreferenced_deletesAll() {
        when(expressTemplateMapper.selectById(1L)).thenReturn(new SalesDeliveryExpressTemplateDO().setId(1L));
        when(deliveryReferenceMapper.selectCountProductByTemplateId(1L, 1L)).thenReturn(0L);

        templateService.deleteDeliveryExpressTemplate(1L);

        verify(expressTemplateMapper).deleteById(1L);
        verify(expressTemplateChargeMapper).deleteByTemplateId(1L);
        verify(expressTemplateFreeMapper).deleteByTemplateId(1L);
    }

    @Test
    @DisplayName("删除：模板不存在拒绝")
    public void testDeleteTemplate_notExists_rejects() {
        when(expressTemplateMapper.selectById(1L)).thenReturn(null);
        assertServiceException(() -> templateService.deleteDeliveryExpressTemplate(1L), EXPRESS_TEMPLATE_NOT_EXISTS);
        verify(expressTemplateMapper, never()).deleteById(1L);
    }

    // ========== 工具方法 ==========

    private SalesDeliveryExpressTemplateCreateReqVO createReqVO(String name) {
        SalesDeliveryExpressTemplateCreateReqVO reqVO = new SalesDeliveryExpressTemplateCreateReqVO();
        reqVO.setName(name);
        reqVO.setChargeMode(1);
        reqVO.setSort(0);
        reqVO.setCharges(List.of(charge("[1,120000]", 5, 1000, 5, 500)));
        return reqVO;
    }

    private SalesDeliveryExpressTemplateChargeBaseVO charge(String areas, Integer startCount, Integer startPrice,
                                                            Integer extraCount, Integer extraPrice) {
        return new SalesDeliveryExpressTemplateChargeBaseVO()
                .setAreaIds(List.of(1, 120000))
                .setStartCount(startCount.doubleValue())
                .setStartPrice(startPrice)
                .setExtraCount(extraCount.doubleValue())
                .setExtraPrice(extraPrice);
    }

    private SalesDeliveryExpressTemplateFreeBaseVO free(String areas, Integer freeCount, Integer freePrice) {
        return new SalesDeliveryExpressTemplateFreeBaseVO()
                .setAreaIds(List.of(1, 120000))
                .setFreeCount(freeCount)
                .setFreePrice(freePrice);
    }

}
