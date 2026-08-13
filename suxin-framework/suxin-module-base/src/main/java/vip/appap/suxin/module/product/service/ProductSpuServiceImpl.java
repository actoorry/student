package vip.appap.suxin.module.product.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ObjectUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.ip.core.Area;
import vip.appap.suxin.framework.ip.core.enums.AreaTypeEnum;
import vip.appap.suxin.framework.ip.core.utils.AreaUtils;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.product.controller.admin.vo.ProductCategoryListReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductSkuSaveReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductSpuPageReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductSpuSaveReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductSpuUpdateStatusReqVO;
import vip.appap.suxin.module.product.controller.app.vo.AppProductSpuPageReqVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductCategoryDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSkuDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSpuDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductUnitDO;
import vip.appap.suxin.module.product.dal.mysql.ProductSpuMapper;
import vip.appap.suxin.module.product.enums.ProductSpuStatusEnum;
import vip.appap.suxin.module.product.enums.ProductSkuWechatVirtualStatusEnum;
import vip.appap.suxin.module.product.enums.ProductTypeEnum;
import vip.appap.suxin.module.product.enums.SalesDeliveryTypeEnum;
import vip.appap.suxin.module.product.service.ProductBrandService;
import vip.appap.suxin.module.product.service.ProductCategoryService;
import vip.appap.suxin.module.product.service.ProductSkuService;
import vip.appap.suxin.module.product.service.ProductUnitService;

import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.*;
import static vip.appap.suxin.module.product.dal.dataobject.ProductCategoryDO.CATEGORY_LEVEL;
import static vip.appap.suxin.module.product.enums.ProductCategoryDimensionConstants.SALES_ROOT_ID;
import static vip.appap.suxin.module.product.enums.ProductCategoryDimensionConstants.WAREHOUSE_ROOT_ID;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.*;

/**
 * 商品 SPU Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class ProductSpuServiceImpl implements ProductSpuService {

    @Resource
    private ProductSpuMapper productSpuMapper;

    @Resource
    @Lazy // 循环依赖，避免报错
    private ProductSkuService productSkuService;
    @Resource
    private ProductBrandService brandService;
    @Resource
    private ProductCategoryService categoryService;
    @Resource
    private ProductUnitService productUnitService;
    @Resource
    private ProductDeliveryTemplateValidator productDeliveryTemplateValidator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSpu(ProductSpuSaveReqVO createReqVO) {
        validateProductType(createReqVO.getType());
        if (createReqVO.getIsWechatMiniappVirtualGoods() == null) {
            createReqVO.setIsWechatMiniappVirtualGoods(false);
        }
        // 历史商品/未设置军创区属性的商品，默认不属于军创区
        if (createReqVO.getIsMilitary() == null) {
            createReqVO.setIsMilitary(false);
        }
        // 校验分类、品牌
        validateSalesCategory(createReqVO.getCategorySales());
        validateWarehouseCategory(createReqVO.getCategoryStore());
        brandService.validateProductBrand(createReqVO.getBrandId());
        // 校验 SKU
        List<ProductSkuSaveReqVO> skuSaveReqList = createReqVO.getSkus();
        productSkuService.validateSkuList(skuSaveReqList, createReqVO.getSpecType());
        validateFulfillmentUnitAndQuantity(createReqVO, skuSaveReqList);
        validateDeliverySettings(createReqVO);

        ProductSpuDO spu = BeanUtils.toBean(createReqVO, ProductSpuDO.class);
        // 初始化 SPU 中 SKU 相关属性
        initSpuFromSkus(spu, skuSaveReqList);
        if (Boolean.TRUE.equals(spu.getIsWechatMiniappVirtualGoods())) {
            spu.setStatus(ProductSpuStatusEnum.DISABLE.getStatus());
        }
        // 插入 SPU
        productSpuMapper.insert(spu);
        // 插入 SKU
        productSkuService.createSkuList(spu.getId(), skuSaveReqList);
        // 返回
        return spu.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpu(ProductSpuSaveReqVO updateReqVO) {
        // 校验 SPU 是否存在
        ProductSpuDO spu = validateSpuExists(updateReqVO.getId());
        validateProductType(updateReqVO.getType());
        if (!Objects.equals(spu.getIsWechatMiniappVirtualGoods(),
                updateReqVO.getIsWechatMiniappVirtualGoods())) {
            throw exception(SPU_WECHAT_VIRTUAL_FLAG_IMMUTABLE);
        }
        // 校验分类、品牌
        validateSalesCategory(updateReqVO.getCategorySales());
        validateWarehouseCategory(updateReqVO.getCategoryStore());
        brandService.validateProductBrand(updateReqVO.getBrandId());
        // 校验SKU
        List<ProductSkuSaveReqVO> skuSaveReqList = updateReqVO.getSkus();
        productSkuService.validateSkuList(skuSaveReqList, updateReqVO.getSpecType());
        validateFulfillmentUnitAndQuantity(updateReqVO, skuSaveReqList);
        validateDeliverySettings(updateReqVO);

        // 更新 SPU
        ProductSpuDO updateObj = BeanUtils.toBean(updateReqVO, ProductSpuDO.class)
                .setStatus(spu.getStatus())
                .setIsWechatMiniappVirtualGoods(spu.getIsWechatMiniappVirtualGoods());
        initSpuFromSkus(updateObj, skuSaveReqList);
        productSpuMapper.updateById(updateObj);
        // 批量更新 SKU
        productSkuService.updateSkuList(updateObj.getId(), updateReqVO.getSkus());
    }

    /**
     * 基于 SKU 的信息，初始化 SPU 的信息
     * 主要是计数相关的字段，例如说市场价、最大最小价、库存等等
     *
     * @param spu  商品 SPU
     * @param skus 商品 SKU 数组
     */
    private void initSpuFromSkus(ProductSpuDO spu, List<ProductSkuSaveReqVO> skus) {
        // sku 单价最低的商品的价格
        spu.setPrice(getMinValue(skus, ProductSkuSaveReqVO::getPrice));
        // sku 单价最低的商品的市场价格
        spu.setMarketPrice(getMinValue(skus, ProductSkuSaveReqVO::getMarketPrice));
        // sku 单价最低的商品的成本价格
        spu.setCostPrice(getMinValue(skus, ProductSkuSaveReqVO::getCostPrice));
        // skus 库存总数
        spu.setStock(getSumValue(skus, ProductSkuSaveReqVO::getStock, Math::addExact));
        // 若是 spu 已有状态则不处理
        if (spu.getStatus() == null) {
            spu.setStatus(ProductSpuStatusEnum.ENABLE.getStatus()); // 默认状态为上架
            spu.setSalesCount(0); // 默认商品销量
            spu.setBrowseCount(0); // 默认商品浏览量
        }
    }

    /**
     * 校验商品分类是否合法
     *
     * @param id 商品分类编号
     */
    private void validateSalesCategory(Long id) {
        categoryService.validateCategoryInDimension(id, SALES_ROOT_ID);
        // 校验层级
        if (categoryService.getCategoryLevel(id) < CATEGORY_LEVEL) {
            throw exception(SPU_SAVE_FAIL_CATEGORY_LEVEL_ERROR);
        }
    }

    private void validateFulfillmentUnitAndQuantity(ProductSpuSaveReqVO reqVO, List<ProductSkuSaveReqVO> skus) {
        if (!ProductTypeEnum.requiresFulfillmentUnit(reqVO.getType())) {
            return;
        }
        if (reqVO.getUnitId() != null) {
            ProductUnitDO unit = productUnitService.getUnit(reqVO.getUnitId());
            if (unit == null || !CommonStatusEnum.ENABLE.getStatus().equals(unit.getStatus())) {
                throw exception(UNIT_NOT_EXISTS);
            }
        }
        for (ProductSkuSaveReqVO sku : skus) {
            if (sku.getQuantity() == null || sku.getQuantity().signum() <= 0) {
                throw exception(SKU_QUANTITY_REQUIRED);
            }
        }
    }

    private void validateProductType(Integer type) {
        if (ProductTypeEnum.valueOf(type) == null) {
            throw exception(SPU_TYPE_INVALID);
        }
    }

    private void validateWarehouseCategory(Long id) {
        if (id != null) {
            categoryService.validateCategoryInDimension(id, WAREHOUSE_ROOT_ID);
        }
    }

    private void validateDeliverySettings(ProductSpuSaveReqVO reqVO) {
        if (CollUtil.isEmpty(reqVO.getDeliveryTypes())
                || !Set.of(SalesDeliveryTypeEnum.ARRAYS).containsAll(reqVO.getDeliveryTypes())) {
            throw exception(SPU_DELIVERY_TYPE_INVALID);
        }
        boolean hasExpress = reqVO.getDeliveryTypes().contains(SalesDeliveryTypeEnum.EXPRESS.getType());
        // 配送方式不根据商品类型过滤，允许选择全部四种配送方式（快递发货/用户自提/线上发货/自动发货）
        if (hasExpress) {
            // 通过无环校验端口验证模板存在、同租户、未删除且至少有一条计费规则
            if (reqVO.getDeliveryTemplateId() == null) {
                throw exception(SPU_DELIVERY_TEMPLATE_REQUIRED);
            }
            productDeliveryTemplateValidator.validateDeliveryTemplate(reqVO.getDeliveryTemplateId());
        } else {
            // 非快递方式强制清空客户端残留的模板 ID
            reqVO.setDeliveryTemplateId(null);
        }
    }

    @Override
    public List<ProductSpuDO> validateSpuList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        // 获得商品信息
        List<ProductSpuDO> list = productSpuMapper.selectByIds(ids);
        Map<Long, ProductSpuDO> spuMap = CollectionUtils.convertMap(list, ProductSpuDO::getId);
        // 校验
        ids.forEach(id -> {
            ProductSpuDO spu = spuMap.get(id);
            if (spu == null) {
                throw exception(SPU_NOT_EXISTS);
            }
            if (!ProductSpuStatusEnum.isEnable(spu.getStatus())) {
                throw exception(SPU_NOT_ENABLE, spu.getName());
            }
        });
        return list;
    }

    @Override
    public void updateBrowseCount(Long id, int incrCount) {
        productSpuMapper.updateBrowseCount(id , incrCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSpu(Long id) {
        // 校验存在
        validateSpuExists(id);
        // 校验商品状态不是回收站不能删除
        ProductSpuDO spuDO = productSpuMapper.selectById(id);
        // 判断 SPU 状态是否为回收站
        if (ObjectUtil.notEqual(spuDO.getStatus(), ProductSpuStatusEnum.RECYCLE.getStatus())) {
            throw exception(SPU_NOT_RECYCLE);
        }
        // TODO 芋艿：【可选】参与活动中的商品，不允许删除？？？

        // 删除 SPU
        productSpuMapper.deleteById(id);
        // 删除关联的 SKU
        productSkuService.deleteSkuBySpuId(id);
    }

    private ProductSpuDO validateSpuExists(Long id) {
        ProductSpuDO spuDO = productSpuMapper.selectById(id);
        if (spuDO == null) {
            throw exception(SPU_NOT_EXISTS);
        }
        return spuDO;
    }

    @Override
    public ProductSpuDO getSpu(Long id) {
        return productSpuMapper.selectById(id);
    }

    @Override
    public ProductSpuDO getSpu(Long id, boolean includeDeleted) {
        if (includeDeleted) {
            return productSpuMapper.selectByIdIncludeDeleted(id);
        }
        return getSpu(id);
    }

    @Override
    public List<ProductSpuDO> getSpuList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        Map<Long, ProductSpuDO> spuMap = convertMap(productSpuMapper.selectByIds(ids), ProductSpuDO::getId);
        // 需要按照 ids 顺序返回。例如说：店铺装修选择了 [3, 1, 2] 三个商品，返回结果还是 [3, 1, 2]  这样的顺序
        return convertList(ids, spuMap::get);
    }

    @Override
    public List<ProductSpuDO> getSpuListByStatus(Integer status) {
        return productSpuMapper.selectList(ProductSpuDO::getStatus, status);
    }

    @Override
    public List<ProductSpuDO> getSpuListByName(String name, Integer limit) {
        if (name == null || name.trim().isEmpty()) {
            return Collections.emptyList();
        }
        if (limit == null || limit <= 0) limit = 20;
        return productSpuMapper.selectList(new LambdaQueryWrapperX<ProductSpuDO>()
                .like(ProductSpuDO::getName, name.trim())
                .eq(ProductSpuDO::getStatus, ProductSpuStatusEnum.ENABLE.getStatus())
                .orderByDesc(ProductSpuDO::getSalesCount)
                .last("LIMIT " + limit));
    }

    @Override
    public PageResult<ProductSpuDO> getSpuPage(ProductSpuPageReqVO pageReqVO) {
        return productSpuMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<ProductSpuDO> getSpuPage(AppProductSpuPageReqVO pageReqVO) {
        // 校验：全国特产（provinceId）与同城特产（cityId/areaId）不能同时传入，避免筛选含义不明确
        Integer cityId = pageReqVO.getCityId() != null ? pageReqVO.getCityId() : pageReqVO.getAreaId();
        if (pageReqVO.getProvinceId() != null && cityId != null) {
            throw exception(SPU_PROVINCE_CITY_CONFLICT);
        }
        // 全国特产：展开省份下全部市级节点，组装 city_id IN (provinceCityIds) 查询
        Set<Integer> provinceCityIds = null;
        if (pageReqVO.getProvinceId() != null) {
            Area province = AreaUtils.getArea(pageReqVO.getProvinceId());
            if (province == null) {
                throw exception(SPU_PROVINCE_NOT_EXISTS);
            }
            provinceCityIds = convertSet(province.getChildren(), Area::getId,
                    area -> AreaTypeEnum.CITY.getType().equals(area.getType()));
        }
        // 查找时，如果查找某个分类编号，则包含它的子分类。因为顶级分类不包含商品
        Set<Long> categoryIds = new HashSet<>();
        if (pageReqVO.getCategorySales() != null && pageReqVO.getCategorySales() > 0) {
            categoryIds.add(pageReqVO.getCategorySales());
            List<ProductCategoryDO> categoryChildren = categoryService.getCategoryList(new ProductCategoryListReqVO()
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()).setParentId(pageReqVO.getCategorySales()));
            categoryIds.addAll(convertList(categoryChildren, ProductCategoryDO::getId));
        }
        if (CollUtil.isNotEmpty(pageReqVO.getCategoryIds())) {
            categoryIds.addAll(pageReqVO.getCategoryIds());
            List<ProductCategoryDO> categoryChildren = categoryService.getCategoryList(new ProductCategoryListReqVO()
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()).setParentIds(pageReqVO.getCategoryIds()));
            categoryIds.addAll(convertList(categoryChildren, ProductCategoryDO::getId));
        }
        // 分页查询
        return productSpuMapper.selectPage(pageReqVO, categoryIds, provinceCityIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpuStock(Map<Long, Integer> stockIncrCounts) {
        stockIncrCounts.forEach((id, incCount) -> productSpuMapper.updateStock(id, incCount));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpuStatus(ProductSpuUpdateStatusReqVO updateReqVO) {
        // 校验存在
        ProductSpuDO productSpuDO = validateSpuExists(updateReqVO.getId());
        if (Boolean.TRUE.equals(productSpuDO.getIsWechatMiniappVirtualGoods())
                && Objects.equals(updateReqVO.getStatus(), ProductSpuStatusEnum.ENABLE.getStatus())) {
            validateWechatVirtualSkuReadiness(productSpuDO.getId());
        }
        // TODO 芋艿：【可选】参与活动中的商品，不允许下架？？？

        // 更新状态
        productSpuDO.setStatus(updateReqVO.getStatus());
        productSpuMapper.updateById(productSpuDO);
    }

    private void validateWechatVirtualSkuReadiness(Long spuId) {
        List<ProductSkuDO> skus = productSkuService.getSkuListBySpuId(spuId);
        if (CollUtil.isEmpty(skus)) {
            throw exception(SPU_WECHAT_VIRTUAL_SKU_NOT_READY, "商品没有有效 SKU");
        }
        List<String> blockingStates = new ArrayList<>();
        for (ProductSkuDO sku : skus) {
            List<String> reasons = new ArrayList<>();
            if (StrUtil.isBlank(sku.getWechatVirtualProductId())) {
                reasons.add("缺少 ProductId");
            }
            if (!Objects.equals(sku.getWechatVirtualUploadStatus(),
                    ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus())) {
                reasons.add("上传状态=" + sku.getWechatVirtualUploadStatus());
            }
            if (!Objects.equals(sku.getWechatVirtualPublishStatus(),
                    ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus())) {
                reasons.add("发布状态=" + sku.getWechatVirtualPublishStatus());
            }
            if (!Objects.equals(sku.getWechatVirtualReviewStatus(),
                    ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus())) {
                reasons.add("审核状态=" + sku.getWechatVirtualReviewStatus());
            }
            if (!reasons.isEmpty()) {
                blockingStates.add("SKU " + sku.getId() + " [" + String.join(", ", reasons) + "]");
            }
        }
        if (!blockingStates.isEmpty()) {
            throw exception(SPU_WECHAT_VIRTUAL_SKU_NOT_READY, String.join("; ", blockingStates));
        }
    }

    @Override
    public Map<Integer, Long> getTabsCount() {
        Map<Integer, Long> counts = Maps.newLinkedHashMapWithExpectedSize(5);
        // 查询销售中的商品数量
        counts.put(ProductSpuPageReqVO.FOR_SALE,
                productSpuMapper.selectCount(ProductSpuDO::getStatus, ProductSpuStatusEnum.ENABLE.getStatus()));
        // 查询仓库中的商品数量
        counts.put(ProductSpuPageReqVO.IN_WAREHOUSE,
                productSpuMapper.selectCount(ProductSpuDO::getStatus, ProductSpuStatusEnum.DISABLE.getStatus()));
        // 查询售空的商品数量
        counts.put(ProductSpuPageReqVO.SOLD_OUT,
                productSpuMapper.selectCount(ProductSpuDO::getStock, 0));
        // 查询触发警戒库存的商品数量
        counts.put(ProductSpuPageReqVO.ALERT_STOCK,
                productSpuMapper.selectCount());
        // 查询回收站中的商品数量
        counts.put(ProductSpuPageReqVO.RECYCLE_BIN,
                productSpuMapper.selectCount(ProductSpuDO::getStatus, ProductSpuStatusEnum.RECYCLE.getStatus()));
        return counts;
    }

    @Override
    public Long getSpuCountByCategoryId(Long categorySales) {
        return productSpuMapper.selectCount(ProductSpuDO::getCategorySales, categorySales);
    }

}
