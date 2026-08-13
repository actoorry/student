package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.module.product.api.ProductSkuApi;
import vip.appap.suxin.module.product.api.dto.ProductSkuRespDTO;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationActivityCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationActivityPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationActivityUpdateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationProductBaseVO;
import vip.appap.suxin.module.sales.convert.SalesCombinationActivityConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationProductDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesCombinationActivityMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesCombinationProductMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.filterList;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.SPU_NOT_EXISTS;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.*;
import static java.util.Collections.singletonList;

/**
 * 拼团活动 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class SalesCombinationActivityServiceImpl implements SalesCombinationActivityService {

    @Resource
    private SalesCombinationActivityMapper combinationActivityMapper;
    @Resource
    private SalesCombinationProductMapper combinationProductMapper;

    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductSkuApi productSkuApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCombinationActivity(SalesCombinationActivityCreateReqVO createReqVO) {
        // 校验商品 SPU 是否存在是否参加的别的活动
        validateProductConflict(createReqVO.getSpuId(), null);
        // 校验商品是否存在
        validateProductExists(createReqVO.getSpuId(), createReqVO.getProducts());

        // 插入拼团活动
        SalesCombinationActivityDO activity = SalesCombinationActivityConvert.INSTANCE.convert(createReqVO)
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        combinationActivityMapper.insert(activity);
        // 插入商品
        List<SalesCombinationProductDO> products = SalesCombinationActivityConvert.INSTANCE.convertList(createReqVO.getProducts(), activity);
        combinationProductMapper.insertBatch(products);
        return activity.getId();
    }

    /**
     * 校验拼团商品参与的活动是否存在冲突
     *
     * @param spuId      商品 SPU 编号
     * @param activityId 拼团活动编号
     */
    private void validateProductConflict(Long spuId, Long activityId) {
        // 查询所有开启的拼团活动
        List<SalesCombinationActivityDO> activityList = combinationActivityMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        if (activityId != null) { // 时排除自己
            activityList.removeIf(item -> ObjectUtil.equal(item.getId(), activityId));
        }
        // 查找是否有其它活动，选择了该产品
        List<SalesCombinationActivityDO> matchActivityList = filterList(activityList, activity -> ObjectUtil.equal(activity.getSpuId(), spuId));
        if (CollUtil.isNotEmpty(matchActivityList)) {
            throw exception(COMBINATION_ACTIVITY_SPU_CONFLICTS);
        }
    }

    /**
     * 校验拼团商品是否都存在
     *
     * @param spuId    商品 SPU 编号
     * @param products 拼团商品
     */
    private void validateProductExists(Long spuId, List<SalesCombinationProductBaseVO> products) {
        // 1. 校验商品 spu 是否存在
        ProductSpuRespDTO spu = productSpuApi.getSpu(spuId);
        if (spu == null) {
            throw exception(SPU_NOT_EXISTS);
        }

        // 2. 校验商品 sku 都存在
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuListBySpuId(singletonList(spuId));
        Map<Long, ProductSkuRespDTO> skuMap = convertMap(skus, ProductSkuRespDTO::getId);
        products.forEach(product -> {
            if (!skuMap.containsKey(product.getSkuId())) {
                throw exception(SKU_NOT_EXISTS);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCombinationActivity(SalesCombinationActivityUpdateReqVO updateReqVO) {
        // 校验存在
        SalesCombinationActivityDO activityDO = validateCombinationActivityExists(updateReqVO.getId());
        // 校验状态
        if (ObjectUtil.equal(activityDO.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            throw exception(COMBINATION_ACTIVITY_STATUS_DISABLE_NOT_UPDATE);
        }
        // 校验商品冲突
        validateProductConflict(updateReqVO.getSpuId(), updateReqVO.getId());
        // 校验商品是否存在
        validateProductExists(updateReqVO.getSpuId(), updateReqVO.getProducts());

        // 更新活动
        SalesCombinationActivityDO updateObj = SalesCombinationActivityConvert.INSTANCE.convert(updateReqVO);
        combinationActivityMapper.updateById(updateObj);
        // 更新商品
        updateCombinationProduct(updateObj, updateReqVO.getProducts());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeCombinationActivityById(Long id) {
        // 校验活动是否存在
        SalesCombinationActivityDO activity = validateCombinationActivityExists(id);
        if (CommonStatusEnum.isDisable(activity.getStatus())) {
            throw exception(COMBINATION_ACTIVITY_STATUS_DISABLE_NOT_UPDATE);
        }

        // 关闭活动
        combinationActivityMapper.updateById(new SalesCombinationActivityDO().setId(id)
                .setStatus(CommonStatusEnum.DISABLE.getStatus()));
    }

    /**
     * 更新拼团商品
     *
     * @param activity 拼团活动
     * @param products 该活动的最新商品配置
     */
    private void updateCombinationProduct(SalesCombinationActivityDO activity, List<SalesCombinationProductBaseVO> products) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<SalesCombinationProductDO> newList = SalesCombinationActivityConvert.INSTANCE.convertList(products, activity);
        List<SalesCombinationProductDO> oldList = combinationProductMapper.selectListByActivityIds(CollUtil.newArrayList(activity.getId()));
        List<List<SalesCombinationProductDO>> diffList = CollectionUtils.diffList(oldList, newList, (oldVal, newVal) -> {
            boolean same = ObjectUtil.equal(oldVal.getSkuId(), newVal.getSkuId());
            if (same) {
                newVal.setId(oldVal.getId());
            }
            return same;
        });

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            combinationProductMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            combinationProductMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            combinationProductMapper.deleteByIds(CollectionUtils.convertList(diffList.get(2), SalesCombinationProductDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCombinationActivity(Long id) {
        // 校验存在
        SalesCombinationActivityDO activity = validateCombinationActivityExists(id);
        // 校验状态
        if (CommonStatusEnum.isEnable(activity.getStatus())) {
            throw exception(COMBINATION_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END);
        }

        // 删除
        combinationActivityMapper.deleteById(id);
    }

    @Override
    public SalesCombinationActivityDO validateCombinationActivityExists(Long id) {
        SalesCombinationActivityDO activityDO = combinationActivityMapper.selectById(id);
        if (activityDO == null) {
            throw exception(COMBINATION_ACTIVITY_NOT_EXISTS);
        }
        return activityDO;
    }

    @Override
    public SalesCombinationActivityDO getCombinationActivity(Long id) {
        return validateCombinationActivityExists(id);
    }

    @Override
    public PageResult<SalesCombinationActivityDO> getCombinationActivityPage(SalesCombinationActivityPageReqVO pageReqVO) {
        return combinationActivityMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SalesCombinationProductDO> getCombinationProductListByActivityIds(Collection<Long> activityIds) {
        return combinationProductMapper.selectListByActivityIds(activityIds);
    }

    @Override
    public List<SalesCombinationActivityDO> getCombinationActivityListByIds(Collection<Long> ids) {
        return combinationActivityMapper.selectList(SalesCombinationActivityDO::getId, ids);
    }

    @Override
    public PageResult<SalesCombinationActivityDO> getCombinationActivityPage(PageParam pageParam) {
        return combinationActivityMapper.selectPage(pageParam, CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public SalesCombinationProductDO selectByActivityIdAndSkuId(Long activityId, Long skuId) {
        return combinationProductMapper.selectOne(
                SalesCombinationProductDO::getActivityId, activityId,
                SalesCombinationProductDO::getSkuId, skuId);
    }

    @Override
    public SalesCombinationActivityDO getMatchCombinationActivityBySpuId(Long spuId) {
        return combinationActivityMapper.selectBySpuIdAndStatusAndNow(spuId, CommonStatusEnum.ENABLE.getStatus());
    }

}
