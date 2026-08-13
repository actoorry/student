package vip.appap.suxin.module.product.service;

import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.product.controller.admin.vo.ProductSkuSaveReqVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductPropertyDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductPropertyValueDO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ProductSkuServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ProductSkuServiceImpl service;

    @Mock
    private ProductPropertyService productPropertyService;
    @Mock
    private ProductPropertyValueService productPropertyValueService;

    @Test
    void validateSkuList_twoDimensionsWithFourUniqueCombinations_accepts() {
        when(productPropertyService.getPropertyList(any())).thenReturn(List.of(
                ProductPropertyDO.builder().id(1L).name("颜色").build(),
                ProductPropertyDO.builder().id(2L).name("尺码").build()));
        when(productPropertyValueService.getPropertyValueListByPropertyId(any())).thenReturn(List.of(
                ProductPropertyValueDO.builder().id(11L).propertyId(1L).name("红").build(),
                ProductPropertyValueDO.builder().id(12L).propertyId(1L).name("蓝").build(),
                ProductPropertyValueDO.builder().id(21L).propertyId(2L).name("M").build(),
                ProductPropertyValueDO.builder().id(22L).propertyId(2L).name("L").build()));

        assertDoesNotThrow(() -> service.validateSkuList(List.of(
                sku(11L, 21L), sku(11L, 22L), sku(12L, 21L), sku(12L, 22L)), true));
    }

    @Test
    void validateSkuList_duplicateCombination_rejects() {
        when(productPropertyService.getPropertyList(any())).thenReturn(List.of(
                ProductPropertyDO.builder().id(1L).name("颜色").build(),
                ProductPropertyDO.builder().id(2L).name("尺码").build()));
        when(productPropertyValueService.getPropertyValueListByPropertyId(any())).thenReturn(List.of(
                ProductPropertyValueDO.builder().id(11L).propertyId(1L).name("红").build(),
                ProductPropertyValueDO.builder().id(21L).propertyId(2L).name("M").build()));

        assertThrows(RuntimeException.class, () -> service.validateSkuList(List.of(
                sku(11L, 21L), sku(11L, 21L)), true));
    }

    private ProductSkuSaveReqVO sku(Long colorValueId, Long sizeValueId) {
        return new ProductSkuSaveReqVO()
                .setName("实体商品")
                .setPrice(19900)
                .setMarketPrice(21900)
                .setCostPrice(10000)
                .setPicUrl("https://example.com/product.png")
                .setStock(10)
                .setProperties(List.of(
                        new ProductSkuSaveReqVO.Property().setPropertyId(1L).setPropertyName("颜色")
                                .setValueId(colorValueId),
                        new ProductSkuSaveReqVO.Property().setPropertyId(2L).setPropertyName("尺码")
                                .setValueId(sizeValueId)));
    }
}
