package vip.appap.suxin.module.product.service;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.product.controller.admin.vo.ProductCategoryListReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductCategorySaveReqVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductCategoryDO;
import vip.appap.suxin.module.product.dal.mysql.ProductCategoryMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static vip.appap.suxin.module.product.enums.ProductCategoryDimensionConstants.SALES_ROOT_ID;
import static vip.appap.suxin.module.product.enums.ProductCategoryDimensionConstants.WAREHOUSE_ROOT_ID;

class ProductCategoryServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ProductCategoryServiceImpl service;

    @Mock
    private ProductCategoryMapper productCategoryMapper;
    @Mock
    private ProductSpuService productSpuService;

    @Test
    void createCategory_virtualSalesParentWithoutPhysicalRow_allowsCreate() {
        ProductCategorySaveReqVO reqVO = categoryReq(null, SALES_ROOT_ID);

        assertDoesNotThrow(() -> service.createCategory(reqVO));
    }

    @Test
    void createCategory_rootZero_rejectsPersistedRoot() {
        assertThrows(RuntimeException.class, () -> service.createCategory(categoryReq(null, 0L)));
    }

    @Test
    void updateAndDeleteCategory_virtualRoot_rejectsMutation() {
        assertThrows(RuntimeException.class, () -> service.updateCategory(categoryReq(SALES_ROOT_ID, SALES_ROOT_ID)));
        assertThrows(RuntimeException.class, () -> service.deleteCategory(WAREHOUSE_ROOT_ID));
    }

    @Test
    void updateCategory_missingParent_rejectsCrossTenantParent() {
        when(productCategoryMapper.selectById(89L)).thenReturn(category(89L, SALES_ROOT_ID));

        assertThrows(RuntimeException.class, () -> service.updateCategory(categoryReq(89L, 999L)));
    }

    @Test
    void updateCategory_selfOrDescendantParent_rejectsCycle() {
        when(productCategoryMapper.selectById(89L)).thenReturn(category(89L, SALES_ROOT_ID));
        assertThrows(RuntimeException.class, () -> service.updateCategory(categoryReq(89L, 89L)));

        when(productCategoryMapper.selectById(90L)).thenReturn(category(90L, 89L));
        assertThrows(RuntimeException.class, () -> service.updateCategory(categoryReq(89L, 90L)));
    }

    @Test
    void validateCategoryInDimension_requiresEnabledCurrentTenantSalesDescendant() {
        when(productCategoryMapper.selectById(101L)).thenReturn(category(101L, 100L));
        when(productCategoryMapper.selectById(100L)).thenReturn(category(100L, SALES_ROOT_ID));

        assertDoesNotThrow(() -> service.validateCategoryInDimension(101L, SALES_ROOT_ID));
        assertThrows(RuntimeException.class, () -> service.validateCategoryInDimension(101L, WAREHOUSE_ROOT_ID));
        assertThrows(RuntimeException.class, () -> service.validateCategoryInDimension(SALES_ROOT_ID, SALES_ROOT_ID));
    }

    @Test
    void getCategoryLevel_cycleOrMissingParent_rejectsMalformedExistingTree() {
        when(productCategoryMapper.selectById(10L)).thenReturn(category(10L, 11L));
        when(productCategoryMapper.selectById(11L)).thenReturn(category(11L, 10L));
        assertThrows(RuntimeException.class, () -> service.getCategoryLevel(10L));

        when(productCategoryMapper.selectById(12L)).thenReturn(category(12L, 999L));
        assertThrows(RuntimeException.class, () -> service.getCategoryLevel(12L));
    }

    @Test
    void getCategoryList_omittedScopeCombinesDimensionsAndIncludesDisabledManagementRows() {
        ProductCategoryDO sales = category(10L, SALES_ROOT_ID);
        ProductCategoryDO warehouse = category(20L, WAREHOUSE_ROOT_ID);
        ProductCategoryDO disabledWarehouse = category(21L, WAREHOUSE_ROOT_ID);
        disabledWarehouse.setStatus(CommonStatusEnum.DISABLE.getStatus());
        when(productCategoryMapper.selectList(any(ProductCategoryListReqVO.class)))
                .thenReturn(List.of(sales, warehouse, disabledWarehouse));

        List<ProductCategoryDO> all = service.getCategoryList(new ProductCategoryListReqVO());
        List<ProductCategoryDO> enabledWarehouse = service.getCategoryList(new ProductCategoryListReqVO()
                .setParentId(WAREHOUSE_ROOT_ID).setStatus(CommonStatusEnum.ENABLE.getStatus()));

        assertEquals(List.of(10L, 20L, 21L), all.stream().map(ProductCategoryDO::getId).toList());
        assertEquals(List.of(20L), enabledWarehouse.stream().map(ProductCategoryDO::getId).toList());
        assertDoesNotThrow(() -> all.sort(Comparator.comparing(ProductCategoryDO::getId)));
    }

    private ProductCategorySaveReqVO categoryReq(Long id, Long parentId) {
        ProductCategorySaveReqVO reqVO = new ProductCategorySaveReqVO();
        reqVO.setId(id);
        reqVO.setParentId(parentId);
        reqVO.setName("分类");
        reqVO.setSort(1);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }

    private ProductCategoryDO category(Long id, Long parentId) {
        return ProductCategoryDO.builder().id(id).parentId(parentId).name("分类" + id)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
    }

}
