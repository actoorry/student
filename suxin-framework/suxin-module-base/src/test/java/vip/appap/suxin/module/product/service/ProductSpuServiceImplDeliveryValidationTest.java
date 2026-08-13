package vip.appap.suxin.module.product.service;

import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.product.controller.admin.vo.ProductSkuSaveReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductSpuSaveReqVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSpuDO;
import vip.appap.suxin.module.product.dal.mysql.ProductSpuMapper;
import vip.appap.suxin.module.product.enums.ProductTypeEnum;
import vip.appap.suxin.module.product.enums.SalesDeliveryTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static vip.appap.suxin.framework.test.core.util.AssertUtils.assertServiceException;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.SPU_DELIVERY_TEMPLATE_REQUIRED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ProductSpuServiceImpl#validateDeliverySettings} 的单元测试
 *
 * 验证商品快递配送只绑定有效模板，且通过 {@link ProductDeliveryTemplateValidator} 端口执行，
 * 不因 product 与 sales 的模块依赖被跳过；非快递方式强制清空模板 ID。
 */
public class ProductSpuServiceImplDeliveryValidationTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ProductSpuServiceImpl spuService;

    @Mock
    private ProductSpuMapper productSpuMapper;
    @Mock
    private ProductSkuService productSkuService;
    @Mock
    private ProductBrandService brandService;
    @Mock
    private ProductCategoryService categoryService;
    @Mock
    private ProductUnitService productUnitService;
    @Mock
    private ProductDeliveryTemplateValidator productDeliveryTemplateValidator;

    @BeforeEach
    public void setUp() {
        // validateSalesCategory 要求分类层级 >= CATEGORY_LEVEL(3)
        when(categoryService.getCategoryLevel(10L)).thenReturn(3);
    }

    @Test
    @DisplayName("快递 + 有效模板：调用端口校验且保存成功")
    public void testCreateSpu_expressValidTemplate_callsValidator() {
        ProductSpuSaveReqVO reqVO = spuReq(List.of(SalesDeliveryTypeEnum.EXPRESS.getType()), 100L);
        when(productSpuMapper.insert(any(ProductSpuDO.class))).thenReturn(1);

        spuService.createSpu(reqVO);

        verify(productDeliveryTemplateValidator).validateDeliveryTemplate(eq(100L));
    }

    @Test
    @DisplayName("快递 + 模板为空：拒绝保存")
    public void testCreateSpu_expressNullTemplate_rejects() {
        ProductSpuSaveReqVO reqVO = spuReq(List.of(SalesDeliveryTypeEnum.EXPRESS.getType()), null);
        assertServiceException(() -> spuService.createSpu(reqVO), SPU_DELIVERY_TEMPLATE_REQUIRED);
        verify(productDeliveryTemplateValidator, never()).validateDeliveryTemplate(anyLong());
    }

    @Test
    @DisplayName("快递 + 不可计算/不存在模板：端口异常向上传播")
    public void testCreateSpu_expressInvalidTemplate_propagates() {
        ProductSpuSaveReqVO reqVO = spuReq(List.of(SalesDeliveryTypeEnum.EXPRESS.getType()), 100L);
        ServiceException invalidTemplate = new ServiceException(1_011_005_005, "运费模板不可用于快递结算");
        doThrow(invalidTemplate).when(productDeliveryTemplateValidator).validateDeliveryTemplate(eq(100L));

        ServiceException actual = org.junit.jupiter.api.Assertions.assertThrows(ServiceException.class,
                () -> spuService.createSpu(reqVO));

        assertEquals(invalidTemplate.getCode(), actual.getCode());
    }

    @Test
    @DisplayName("非快递 + 残留模板 ID：强制清空且不调用端口")
    public void testCreateSpu_nonExpress_clearsResidueTemplate() {
        ProductSpuSaveReqVO reqVO = spuReq(List.of(SalesDeliveryTypeEnum.ONLINE.getType()), 100L);
        when(productSpuMapper.insert(any(ProductSpuDO.class))).thenReturn(1);

        spuService.createSpu(reqVO);

        assertNull(reqVO.getDeliveryTemplateId());
        verify(productDeliveryTemplateValidator, never()).validateDeliveryTemplate(anyLong());
    }

    private ProductSpuSaveReqVO spuReq(List<Integer> deliveryTypes, Long templateId) {
        ProductSpuSaveReqVO reqVO = new ProductSpuSaveReqVO();
        reqVO.setType(ProductTypeEnum.ENTITY.getValue());
        reqVO.setName("test-spu");
        reqVO.setCategorySales(10L);
        reqVO.setBrandId(1L);
        reqVO.setDeliveryTypes(deliveryTypes);
        reqVO.setDeliveryTemplateId(templateId);
        reqVO.setSkus(List.of(new ProductSkuSaveReqVO()));
        return reqVO;
    }

}
