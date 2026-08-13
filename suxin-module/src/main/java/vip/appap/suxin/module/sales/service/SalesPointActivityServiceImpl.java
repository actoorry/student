package vip.appap.suxin.module.sales.service;

import cn.hutool.core.util.ObjectUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.product.api.ProductSkuApi;
import vip.appap.suxin.module.product.api.dto.ProductSkuRespDTO;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.api.dto.SalesPointValidateJoinRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesPointActivityPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesPointActivitySaveReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesPointProductSaveReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesPointActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesPointProductDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesPointActivityMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesPointProductMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.collection.CollUtil.intersectionDistinct;
import static cn.hutool.core.collection.CollUtil.isNotEmpty;
import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.*;
import static vip.appap.suxin.framework.common.util.collection.MapUtils.findAndThen;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.SPU_NOT_EXISTS;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.*;
import static java.util.Collections.singletonList;

/**
 * 积分商城活动 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class SalesPointActivityServiceImpl implements SalesPointActivityService {

    @Resource
    private SalesPointActivityMapper pointActivityMapper;
    @Resource
    private SalesPointProductMapper pointProductMapper;

    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductSkuApi productSkuApi;

    private static List<SalesPointProductDO> buildPointProductDO(SalesPointActivityDO pointActivity, List<SalesPointProductSaveReqVO> products) {
        return BeanUtils.toBean(products, SalesPointProductDO.class, product ->
                product.setSpuId(pointActivity.getSpuId()).setActivityId(pointActivity.getId())
                        .setActivityStatus(pointActivity.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPointActivity(SalesPointActivitySaveReqVO createReqVO) {
        // 1.1 校验商品是否存在
        validateProductExists(createReqVO.getSpuId(), createReqVO.getProducts());
        // 1.2 校验商品是否已经参加别的活动
        validatePointActivityProductConflicts(null, createReqVO.getProducts());

        // 2.1 插入积分商城活动
        SalesPointActivityDO pointActivity = BeanUtils.toBean(createReqVO, SalesPointActivityDO.class)
                .setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setStock(getSumValue(createReqVO.getProducts(), SalesPointProductSaveReqVO::getStock, Integer::sum));
        pointActivity.setTotalStock(pointActivity.getStock());
        pointActivityMapper.insert(pointActivity);
        // 2.2 插入积分商城活动商品
        pointProductMapper.insertBatch(buildPointProductDO(pointActivity, createReqVO.getProducts()));
        return pointActivity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePointActivity(SalesPointActivitySaveReqVO updateReqVO) {
        // 1.1 校验存在
        SalesPointActivityDO activity = validatePointActivityExists(updateReqVO.getId());
        if (CommonStatusEnum.DISABLE.getStatus().equals(activity.getStatus())) {
            throw exception(POINT_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED);
        }
        // 1.2 校验商品是否存在
        validateProductExists(updateReqVO.getSpuId(), updateReqVO.getProducts());
        // 1.3 校验商品是否已经参加别的活动
        validatePointActivityProductConflicts(updateReqVO.getId(), updateReqVO.getProducts());

        // 2.1 更新积分商城活动
        SalesPointActivityDO updateObj = BeanUtils.toBean(updateReqVO, SalesPointActivityDO.class)
                .setStock(getSumValue(updateReqVO.getProducts(), SalesPointProductSaveReqVO::getStock, Integer::sum));
        if (updateObj.getStock() > activity.getTotalStock()) { // 如果更新的库存大于原来的库存，则更新总库存
            updateObj.setTotalStock(updateObj.getStock());
        }
        pointActivityMapper.updateById(updateObj);
        // 2.2 更新商品
        updatePointProduct(updateObj, updateReqVO.getProducts());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePointStockDecr(Long id, Long skuId, Integer count) {
        // 1.1 校验活动库存是否充足
        SalesPointActivityDO activity = validatePointActivityExists(id);
        if (count > activity.getStock()) {
            throw exception(POINT_ACTIVITY_UPDATE_STOCK_FAIL);
        }
        // 1.2 校验商品库存是否充足
        SalesPointProductDO product = pointProductMapper.selectListByActivityIdAndSkuId(id, skuId);
        if (product == null || count > product.getStock()) {
            throw exception(POINT_ACTIVITY_UPDATE_STOCK_FAIL);
        }

        // 2.1 更新活动商品库存
        int updateCount = pointProductMapper.updateStockDecr(product.getId(), count);
        if (updateCount == 0) {
            throw exception(POINT_ACTIVITY_UPDATE_STOCK_FAIL);
        }

        // 2.2 更新活动库存
        updateCount = pointActivityMapper.updateStockDecr(id, count);
        if (updateCount == 0) {
            throw exception(POINT_ACTIVITY_UPDATE_STOCK_FAIL);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePointStockIncr(Long id, Long skuId, Integer count) {
        SalesPointProductDO product = pointProductMapper.selectListByActivityIdAndSkuId(id, skuId);
        // 更新活动商品库存
        pointProductMapper.updateStockIncr(product.getId(), count);
        // 更新活动库存
        pointActivityMapper.updateStockIncr(id, count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closePointActivity(Long id) {
        // 校验存在
        SalesPointActivityDO pointActivity = validatePointActivityExists(id);
        if (CommonStatusEnum.DISABLE.getStatus().equals(pointActivity.getStatus())) {
            throw exception(POINT_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED);
        }

        // 更新
        pointActivityMapper.updateById(new SalesPointActivityDO().setId(id).setStatus(CommonStatusEnum.DISABLE.getStatus()));
        // 更新活动商品状态
        pointProductMapper.updateByActivityId(new SalesPointProductDO().setActivityId(id).setActivityStatus(
                CommonStatusEnum.DISABLE.getStatus()));
    }

    /**
     * 更新积分商品
     *
     * @param activity 积分活动
     * @param products 该活动的最新商品配置
     */
    private void updatePointProduct(SalesPointActivityDO activity, List<SalesPointProductSaveReqVO> products) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<SalesPointProductDO> newList = buildPointProductDO(activity, products);
        List<SalesPointProductDO> oldList = pointProductMapper.selectListByActivityId(activity.getId());
        List<List<SalesPointProductDO>> diffList = diffList(oldList, newList, (oldVal, newVal) -> {
            boolean same = ObjectUtil.equal(oldVal.getSkuId(), newVal.getSkuId());
            if (same) {
                newVal.setId(oldVal.getId());
            }
            return same;
        });

        // 第二步，批量添加、修改、删除
        if (isNotEmpty(diffList.get(0))) {
            pointProductMapper.insertBatch(diffList.get(0));
        }
        if (isNotEmpty(diffList.get(1))) {
            pointProductMapper.updateBatch(diffList.get(1));
        }
        if (isNotEmpty(diffList.get(2))) {
            pointProductMapper.deleteByIds(convertList(diffList.get(2), SalesPointProductDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePointActivity(Long id) {
        // 校验存在
        SalesPointActivityDO pointActivity = validatePointActivityExists(id);
        if (CommonStatusEnum.ENABLE.getStatus().equals(pointActivity.getStatus())) {
            throw exception(POINT_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END);
        }

        // 删除商城活动
        pointActivityMapper.deleteById(id);
        // 删除活动商品
        List<SalesPointProductDO> products = pointProductMapper.selectListByActivityId(id);
        pointProductMapper.deleteByIds(convertSet(products, SalesPointProductDO::getId));
    }

    private SalesPointActivityDO validatePointActivityExists(Long id) {
        SalesPointActivityDO pointActivityDO = pointActivityMapper.selectById(id);
        if (pointActivityDO == null) {
            throw exception(POINT_ACTIVITY_NOT_EXISTS);
        }
        return pointActivityDO;
    }

    /**
     * 校验积分商品是否都存在
     *
     * @param spuId    商品 SPU 编号
     * @param products 积分商品
     */
    private void validateProductExists(Long spuId, List<SalesPointProductSaveReqVO> products) {
        // 1. 校验商品 spu 是否存在
        ProductSpuRespDTO spu = productSpuApi.getSpu(spuId);
        if (spu == null) {
            throw exception(SPU_NOT_EXISTS);
        }
        products.forEach(product -> product.setSpuId(spuId));

        // 2. 校验商品 sku 都存在
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuListBySpuId(singletonList(spuId));
        Map<Long, ProductSkuRespDTO> skuMap = convertMap(skus, ProductSkuRespDTO::getId);
        products.forEach(product -> {
            if (!skuMap.containsKey(product.getSkuId())) {
                throw exception(SKU_NOT_EXISTS);
            }
        });
    }

    /**
     * 校验商品是否冲突
     *
     * @param id       编号
     * @param products 商品列表
     */
    private void validatePointActivityProductConflicts(Long id, List<SalesPointProductSaveReqVO> products) {
        // 1.1 查询所有开启的积分商城活动
        List<SalesPointActivityDO> activityList = pointActivityMapper.selectList(SalesPointActivityDO::getStatus,
                CommonStatusEnum.ENABLE.getStatus());
        if (id != null) { // 更新时排除自己
            activityList.removeIf(item -> ObjectUtil.equal(item.getId(), id));
        }
        // 1.2 查询活动下的所有商品
        List<SalesPointProductDO> productList = pointProductMapper.selectListByActivityId(
                convertList(activityList, SalesPointActivityDO::getId));
        Map<Long, List<SalesPointProductDO>> productListMap = convertMultiMap(productList, SalesPointProductDO::getActivityId);

        // 2. 校验商品是否冲突
        activityList.forEach(item -> {
            findAndThen(productListMap, item.getId(), discountProducts -> {
                if (!intersectionDistinct(convertList(discountProducts, SalesPointProductDO::getSpuId),
                        convertList(products, SalesPointProductSaveReqVO::getSpuId)).isEmpty()) {
                    throw exception(POINT_ACTIVITY_SPU_CONFLICTS);
                }
            });
        });
    }

    @Override
    public SalesPointActivityDO getPointActivity(Long id) {
        return pointActivityMapper.selectById(id);
    }

    @Override
    public PageResult<SalesPointActivityDO> getPointActivityPage(SalesPointActivityPageReqVO pageReqVO) {
        return pointActivityMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SalesPointActivityDO> getPointActivityListByIds(Collection<Long> ids) {
        return pointActivityMapper.selectList(SalesPointActivityDO::getId, ids);
    }

    @Override
    public List<SalesPointProductDO> getPointProductListByActivityIds(Collection<Long> activityIds) {
        return pointProductMapper.selectListByActivityId(activityIds);
    }

    @Override
    public SalesPointValidateJoinRespDTO validateJoinPointActivity(Long activityId, Long skuId, Integer count) {
        // 1. 校验积分商城活动是否存在
        SalesPointActivityDO activity = validatePointActivityExists(activityId);
        if (CommonStatusEnum.isDisable(activity.getStatus())) {
            throw exception(POINT_ACTIVITY_JOIN_ACTIVITY_STATUS_CLOSED);
        }

        // 2.1 校验积分商城商品是否存在
        SalesPointProductDO product = pointProductMapper.selectListByActivityIdAndSkuId(activityId, skuId);
        if (product == null) {
            throw exception(POINT_ACTIVITY_JOIN_ACTIVITY_PRODUCT_NOT_EXISTS);
        }
        // 2.2 超过单次购买限制
        if (count > product.getCount()) {
            throw exception(POINT_ACTIVITY_JOIN_ACTIVITY_SINGLE_LIMIT_COUNT_EXCEED);
        }
        // 2.2 校验库存是否充足
        if (count > product.getStock()) {
            throw exception(POINT_ACTIVITY_UPDATE_STOCK_FAIL);
        }
        return BeanUtils.toBean(product, SalesPointValidateJoinRespDTO.class);
    }

}