package vip.appap.suxin.module.product.enums;

import java.util.Set;

/**
 * 商品分类的代码拥有维度根。
 *
 * <p>这些编号是层级协议的一部分，不对应、也不读取任何 {@code product_category} 实体行。</p>
 */
public final class ProductCategoryDimensionConstants {

    public static final Long SALES_ROOT_ID = 1L;
    public static final Long WAREHOUSE_ROOT_ID = 2L;
    public static final Set<Long> VIRTUAL_ROOT_IDS = Set.of(SALES_ROOT_ID, WAREHOUSE_ROOT_ID);

    private ProductCategoryDimensionConstants() {
    }

    public static boolean isVirtualRoot(Long categoryId) {
        return categoryId != null && VIRTUAL_ROOT_IDS.contains(categoryId);
    }

}
