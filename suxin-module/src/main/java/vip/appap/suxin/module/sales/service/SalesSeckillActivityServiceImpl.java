package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.date.LocalDateTimeUtils;
import vip.appap.suxin.module.product.api.ProductSkuApi;
import vip.appap.suxin.module.product.api.dto.ProductSkuRespDTO;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.api.dto.SalesSeckillValidateJoinRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesSeckillActivityCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesSeckillActivityPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesSeckillActivityUpdateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesSeckillProductBaseVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesSeckillActivityPageReqVO;
import vip.appap.suxin.module.sales.convert.SalesSeckillActivityConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesSeckillActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesSeckillConfigDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesSeckillProductDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesSeckillActivityMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesSeckillProductMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.collection.CollUtil.isNotEmpty;
import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.*;
import static vip.appap.suxin.framework.common.util.date.LocalDateTimeUtils.isBetween;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.SPU_NOT_EXISTS;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.*;
import static java.util.Collections.singletonList;

/**
 * 秒杀活动 Service 实现类
 *
 * @author halfninety
 */
@Service
@Validated
public class SalesSeckillActivityServiceImpl implements SalesSeckillActivityService {

    @Resource
    private SalesSeckillActivityMapper seckillActivityMapper;
    @Resource
    private SalesSeckillProductMapper seckillProductMapper;

    @Resource
    private SalesSeckillConfigService seckillConfigService;

    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductSkuApi productSkuApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSeckillActivity(SalesSeckillActivityCreateReqVO createReqVO) {
        // 1.1 校验商品秒杀时段是否冲突
        validateProductConflict(createReqVO.getConfigIds(), createReqVO.getSpuId(), null);
        // 1.2 校验商品是否存在
        validateProductExists(createReqVO.getSpuId(), createReqVO.getProducts());

        // 2.1 插入秒杀活动
        SalesSeckillActivityDO activity = SalesSeckillActivityConvert.INSTANCE.convert(createReqVO)
                .setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setStock(getSumValue(createReqVO.getProducts(), SalesSeckillProductBaseVO::getStock, Integer::sum));
        activity.setTotalStock(activity.getStock());
        seckillActivityMapper.insert(activity);
        // 2.2 插入商品
        List<SalesSeckillProductDO> products = SalesSeckillActivityConvert.INSTANCE.convertList(createReqVO.getProducts(), activity);
        seckillProductMapper.insertBatch(products);
        return activity.getId();
    }

    /**
     * 校验秒杀商品参与的活动是否存在冲突
     *
     * 1. 校验秒杀时段是否存在
     * 2. 秒杀商品是否参加其它活动
     *
     * @param configIds  秒杀时段数组
     * @param spuId      商品 SPU 编号
     * @param activityId 秒杀活动编号
     */
    private void validateProductConflict(List<Long> configIds, Long spuId, Long activityId) {
        // 1. 校验秒杀时段是否存在
        seckillConfigService.validateSeckillConfigExists(configIds);

        // 2.1 查询所有开启的秒杀活动
        List<SalesSeckillActivityDO> activityList = seckillActivityMapper.selectListBySpuIdAndStatus(spuId, CommonStatusEnum.ENABLE.getStatus());
        if (activityId != null) { // 排除自己
            activityList.removeIf(item -> ObjectUtil.equal(item.getId(), activityId));
        }
        // 2.2 过滤出所有 configIds 有交集的活动，判断是否存在重叠
        List<SalesSeckillActivityDO> conflictActivityList = filterList(activityList, s -> containsAny(s.getConfigIds(), configIds));
        if (isNotEmpty(conflictActivityList)) {
            throw exception(SECKILL_ACTIVITY_SPU_CONFLICTS);
        }
    }

    /**
     * 校验秒杀商品是否都存在
     *
     * @param spuId    商品 SPU 编号
     * @param products 秒杀商品
     */
    private void validateProductExists(Long spuId, List<SalesSeckillProductBaseVO> products) {
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
    public void updateSeckillActivity(SalesSeckillActivityUpdateReqVO updateReqVO) {
        // 1.1 校验存在
        SalesSeckillActivityDO activity = validateSeckillActivityExists(updateReqVO.getId());
        if (CommonStatusEnum.DISABLE.getStatus().equals(activity.getStatus())) {
            throw exception(SECKILL_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED);
        }
        // 1.2 校验商品是否冲突
        validateProductConflict(updateReqVO.getConfigIds(), updateReqVO.getSpuId(), updateReqVO.getId());
        // 1.3 校验商品是否存在
        validateProductExists(updateReqVO.getSpuId(), updateReqVO.getProducts());

        // 2.1 更新活动
        SalesSeckillActivityDO updateObj = SalesSeckillActivityConvert.INSTANCE.convert(updateReqVO)
                .setStock(getSumValue(updateReqVO.getProducts(), SalesSeckillProductBaseVO::getStock, Integer::sum));
        if (updateObj.getStock() > activity.getTotalStock()) { // 如果更新的库存大于原来的库存，则更新总库存
            updateObj.setTotalStock(updateObj.getStock());
        }
        seckillActivityMapper.updateById(updateObj);
        // 2.2 更新商品
        updateSeckillProduct(updateObj, updateReqVO.getProducts());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSeckillStockDecr(Long id, Long skuId, Integer count) {
        // 1.1 校验活动库存是否充足
        SalesSeckillActivityDO seckillActivity = validateSeckillActivityExists(id);
        if (count > seckillActivity.getStock()) {
            throw exception(SECKILL_ACTIVITY_UPDATE_STOCK_FAIL);
        }
        // 1.2 校验商品库存是否充足
        SalesSeckillProductDO product = seckillProductMapper.selectByActivityIdAndSkuId(id, skuId);
        if (product == null || count > product.getStock()) {
            throw exception(SECKILL_ACTIVITY_UPDATE_STOCK_FAIL);
        }

        // 2.1 更新活动商品库存
        int updateCount = seckillProductMapper.updateStockDecr(product.getId(), count);
        if (updateCount == 0) {
            throw exception(SECKILL_ACTIVITY_UPDATE_STOCK_FAIL);
        }

        // 2.2 更新活动库存
        updateCount = seckillActivityMapper.updateStockDecr(seckillActivity.getId(), count);
        if (updateCount == 0) {
            throw exception(SECKILL_ACTIVITY_UPDATE_STOCK_FAIL);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSeckillStockIncr(Long id, Long skuId, Integer count) {
        SalesSeckillProductDO product = seckillProductMapper.selectByActivityIdAndSkuId(id, skuId);
        // 更新活动商品库存
        seckillProductMapper.updateStockIncr(product.getId(), count);
        // 更新活动库存
        seckillActivityMapper.updateStockIncr(id, count);
    }

    /**
     * 更新秒杀商品
     *
     * @param activity 秒杀活动
     * @param products 该活动的最新商品配置
     */
    private void updateSeckillProduct(SalesSeckillActivityDO activity, List<SalesSeckillProductBaseVO> products) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<SalesSeckillProductDO> newList = SalesSeckillActivityConvert.INSTANCE.convertList(products, activity);
        List<SalesSeckillProductDO> oldList = seckillProductMapper.selectListByActivityId(activity.getId());
        List<List<SalesSeckillProductDO>> diffList = diffList(oldList, newList, (oldVal, newVal) -> {
            boolean same = ObjectUtil.equal(oldVal.getSkuId(), newVal.getSkuId());
            if (same) {
                newVal.setId(oldVal.getId());
            }
            return same;
        });

        // 第二步，批量添加、修改、删除
        if (isNotEmpty(diffList.get(0))) {
            seckillProductMapper.insertBatch(diffList.get(0));
        }
        if (isNotEmpty(diffList.get(1))) {
            seckillProductMapper.updateBatch(diffList.get(1));
        }
        if (isNotEmpty(diffList.get(2))) {
            seckillProductMapper.deleteByIds(convertList(diffList.get(2), SalesSeckillProductDO::getId));
        }
    }

    @Override
    public void closeSeckillActivity(Long id) {
        // 校验存在
        SalesSeckillActivityDO activity = validateSeckillActivityExists(id);
        if (CommonStatusEnum.DISABLE.getStatus().equals(activity.getStatus())) {
            throw exception(SECKILL_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED);
        }

        // 更新
        SalesSeckillActivityDO updateObj = new SalesSeckillActivityDO().setId(id).setStatus(CommonStatusEnum.DISABLE.getStatus());
        seckillActivityMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSeckillActivity(Long id) {
        // 校验存在
        SalesSeckillActivityDO seckillActivity = this.validateSeckillActivityExists(id);
        if (CommonStatusEnum.ENABLE.getStatus().equals(seckillActivity.getStatus())) {
            throw exception(SECKILL_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END);
        }

        // 删除活动
        seckillActivityMapper.deleteById(id);
        // 删除活动商品
        List<SalesSeckillProductDO> products = seckillProductMapper.selectListByActivityId(id);
        seckillProductMapper.deleteByIds(convertSet(products, SalesSeckillProductDO::getId));
    }

    private SalesSeckillActivityDO validateSeckillActivityExists(Long id) {
        SalesSeckillActivityDO seckillActivity = seckillActivityMapper.selectById(id);
        if (seckillActivity == null) {
            throw exception(SECKILL_ACTIVITY_NOT_EXISTS);
        }
        return seckillActivity;
    }

    @Override
    public SalesSeckillActivityDO getSeckillActivity(Long id) {
        return seckillActivityMapper.selectById(id);
    }

    @Override
    public PageResult<SalesSeckillActivityDO> getSeckillActivityPage(SalesSeckillActivityPageReqVO pageReqVO) {
        return seckillActivityMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SalesSeckillProductDO> getSeckillProductListByActivityId(Long activityId) {
        return seckillProductMapper.selectListByActivityId(activityId);
    }

    @Override
    public List<SalesSeckillProductDO> getSeckillProductListByActivityIds(Collection<Long> activityIds) {
        return seckillProductMapper.selectListByActivityId(activityIds);
    }

    @Override
    public List<SalesSeckillActivityDO> getSeckillActivityListByConfigIdAndStatus(Long configId, Integer status) {
        return filterList(seckillActivityMapper.selectList(SalesSeckillActivityDO::getStatus, status),
                item -> anyMatch(item.getConfigIds(), id -> ObjectUtil.equal(id, configId)) // 校验时段
                        && isBetween(item.getStartTime(), item.getEndTime())); // 追加当前日期是否处在活动日期之间的校验条件
    }

    @Override
    public PageResult<SalesSeckillActivityDO> getSeckillActivityAppPageByConfigId(AppSalesSeckillActivityPageReqVO pageReqVO) {
        return seckillActivityMapper.selectPage(pageReqVO, CommonStatusEnum.ENABLE.getStatus(), LocalDateTime.now());
    }

    @Override
    public SalesSeckillValidateJoinRespDTO validateJoinSeckill(Long activityId, Long skuId, Integer count) {
        // 1.1 校验秒杀活动是否存在
        SalesSeckillActivityDO activity = validateSeckillActivityExists(activityId);
        if (CommonStatusEnum.isDisable(activity.getStatus())) {
            throw exception(SECKILL_JOIN_ACTIVITY_STATUS_CLOSED);
        }
        // 1.2 是否在活动时间范围内
        if (!LocalDateTimeUtils.isBetween(activity.getStartTime(), activity.getEndTime())) {
            throw exception(SECKILL_JOIN_ACTIVITY_TIME_ERROR);
        }
        SalesSeckillConfigDO config = seckillConfigService.getCurrentSeckillConfig();
        if (config == null
                || !CollectionUtil.contains(activity.getConfigIds(), config.getId())
                || !LocalDateTimeUtils.isBetween(config.getStartTime(), config.getEndTime())) {
            throw exception(SECKILL_JOIN_ACTIVITY_TIME_ERROR);
        }
        // 1.3 超过单次购买限制
        if (count > activity.getSingleLimitCount()) {
            throw exception(SECKILL_JOIN_ACTIVITY_SINGLE_LIMIT_COUNT_EXCEED);
        }

        // 2.1 校验秒杀商品是否存在
        SalesSeckillProductDO product = seckillProductMapper.selectByActivityIdAndSkuId(activityId, skuId);
        if (product == null) {
            throw exception(SECKILL_JOIN_ACTIVITY_PRODUCT_NOT_EXISTS);
        }
        // 2.2 校验库存是否充足
        if (count > product.getStock()) {
            throw exception(SECKILL_ACTIVITY_UPDATE_STOCK_FAIL);
        }
        return SalesSeckillActivityConvert.INSTANCE.convert02(activity, product);
    }

    @Override
    public SalesSeckillActivityDO getMatchSeckillActivityBySpuId(Long spuId) {
        return seckillActivityMapper.selectBySpuIdAndStatusAndNow(spuId, CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public List<SalesSeckillActivityDO> getSeckillActivityListByIds(Collection<Long> ids) {
        return seckillActivityMapper.selectList(SalesSeckillActivityDO::getId, ids);
    }

}
