package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.product.api.ProductSkuApi;
import vip.appap.suxin.module.product.api.dto.ProductSkuRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiscountActivityBaseVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiscountActivityCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiscountActivityPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiscountActivityUpdateReqVO;
import vip.appap.suxin.module.sales.convert.SalesDiscountActivityConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiscountActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiscountProductDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesDiscountActivityMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesDiscountProductMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.collection.CollUtil.intersectionDistinct;
import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.*;
import static vip.appap.suxin.framework.common.util.collection.MapUtils.findAndThen;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.*;

/**
 * 限时折扣 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class SalesDiscountActivityServiceImpl implements SalesDiscountActivityService {

    @Resource
    private SalesDiscountActivityMapper discountActivityMapper;
    @Resource
    private SalesDiscountProductMapper discountProductMapper;

    @Resource
    private ProductSkuApi productSkuApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDiscountActivity(SalesDiscountActivityCreateReqVO createReqVO) {
        // 校验商品是否冲突
        validateDiscountActivityProductConflicts(null, createReqVO.getProducts());
        // 校验商品是否存在
        validateProductExists(createReqVO.getProducts());

        // 插入活动
        SalesDiscountActivityDO discountActivity = SalesDiscountActivityConvert.INSTANCE.convert(createReqVO)
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        discountActivityMapper.insert(discountActivity);
        // 插入商品
        List<SalesDiscountProductDO> discountProducts = BeanUtils.toBean(createReqVO.getProducts(), SalesDiscountProductDO.class,
                product -> product.setActivityId(discountActivity.getId())
                        .setActivityName(discountActivity.getName()).setActivityStatus(discountActivity.getStatus())
                        .setActivityStartTime(createReqVO.getStartTime()).setActivityEndTime(createReqVO.getEndTime()));
        discountProductMapper.insertBatch(discountProducts);
        // 返回
        return discountActivity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDiscountActivity(SalesDiscountActivityUpdateReqVO updateReqVO) {
        // 校验存在
        SalesDiscountActivityDO discountActivity = validateDiscountActivityExists(updateReqVO.getId());
        if (discountActivity.getStatus().equals(CommonStatusEnum.DISABLE.getStatus())) { // 已关闭的活动，不能修改噢
            throw exception(DISCOUNT_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED);
        }
        // 校验商品是否冲突
        validateDiscountActivityProductConflicts(updateReqVO.getId(), updateReqVO.getProducts());
        // 校验商品是否存在
        validateProductExists(updateReqVO.getProducts());

        // 更新活动
        SalesDiscountActivityDO updateObj = SalesDiscountActivityConvert.INSTANCE.convert(updateReqVO);
        discountActivityMapper.updateById(updateObj);
        // 更新商品
        updateDiscountProduct(updateObj, updateReqVO.getProducts());
    }

    private void updateDiscountProduct(SalesDiscountActivityDO activity, List<SalesDiscountActivityCreateReqVO.Product> products) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<SalesDiscountProductDO> newList = BeanUtils.toBean(products, SalesDiscountProductDO.class,
                product -> product.setActivityId(activity.getId())
                        .setActivityName(activity.getName()).setActivityStatus(activity.getStatus())
                        .setActivityStartTime(activity.getStartTime()).setActivityEndTime(activity.getEndTime()));
        List<SalesDiscountProductDO> oldList = discountProductMapper.selectListByActivityId(activity.getId());
        List<List<SalesDiscountProductDO>> diffList = CollectionUtils.diffList(oldList, newList, (oldVal, newVal) -> {
            boolean same = ObjectUtil.equal(oldVal.getSkuId(), newVal.getSkuId());
            if (same) {
                newVal.setId(oldVal.getId());
            }
            return same;
        });

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            discountProductMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            discountProductMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            discountProductMapper.deleteByIds(convertList(diffList.get(2), SalesDiscountProductDO::getId));
        }
    }

    /**
     * 校验商品是否冲突
     *
     * @param id       编号
     * @param products 商品列表
     */
    private void validateDiscountActivityProductConflicts(Long id, List<SalesDiscountActivityBaseVO.Product> products) {
        // 1.1 查询所有开启的折扣活动
        List<SalesDiscountActivityDO> activityList = discountActivityMapper.selectList(SalesDiscountActivityDO::getStatus,
                CommonStatusEnum.ENABLE.getStatus());
        if (id != null) { // 时排除自己
            activityList.removeIf(item -> ObjectUtil.equal(item.getId(), id));
        }
        // 1.2 查询活动下的所有商品
        List<SalesDiscountProductDO> productList = discountProductMapper.selectListByActivityId(
                convertList(activityList, SalesDiscountActivityDO::getId));
        Map<Long, List<SalesDiscountProductDO>> productListMap = convertMultiMap(productList, SalesDiscountProductDO::getActivityId);

        // 2. 校验商品是否冲突
        activityList.forEach(item -> {
            findAndThen(productListMap, item.getId(), discountProducts -> {
                if (!intersectionDistinct(convertList(discountProducts, SalesDiscountProductDO::getSpuId),
                        convertList(products, SalesDiscountActivityBaseVO.Product::getSpuId)).isEmpty()) {
                    throw exception(DISCOUNT_ACTIVITY_SPU_CONFLICTS, item.getName());
                }
            });
        });
    }

    /**
     * 校验活动商品是否都存在
     *
     * @param products 活动商品
     */
    private void validateProductExists(List<SalesDiscountActivityBaseVO.Product> products) {
        // 1.获得商品所有的 sku
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuListBySpuId(
                convertList(products, SalesDiscountActivityBaseVO.Product::getSpuId));
        Map<Long, ProductSkuRespDTO> skuMap = convertMap(skus, ProductSkuRespDTO::getId);
        // 2. 校验商品 sku 都存在
        products.forEach(product -> {
            if (!skuMap.containsKey(product.getSkuId())) {
                throw exception(SKU_NOT_EXISTS);
            }
        });
    }

    @Override
    public void closeDiscountActivity(Long id) {
        // 校验存在
        SalesDiscountActivityDO activity = validateDiscountActivityExists(id);
        if (activity.getStatus().equals(CommonStatusEnum.DISABLE.getStatus())) { // 已关闭的活动，不能关闭噢
            throw exception(DISCOUNT_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED);
        }

        // 更新活动状态
        discountActivityMapper.updateById(new SalesDiscountActivityDO().setId(id).setStatus(CommonStatusEnum.DISABLE.getStatus()));
        // 更新活动商品状态
        discountProductMapper.updateByActivityId(new SalesDiscountProductDO().setActivityId(id).setActivityStatus(
                CommonStatusEnum.DISABLE.getStatus()));
    }

    @Override
    public void deleteDiscountActivity(Long id) {
        // 校验存在
        SalesDiscountActivityDO activity = validateDiscountActivityExists(id);
        if (CommonStatusEnum.isEnable(activity.getStatus())) { // 未关闭的活动，不能删除噢
            throw exception(DISCOUNT_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED);
        }

        // 删除活动
        discountActivityMapper.deleteById(id);
        // 删除活动商品
        discountProductMapper.deleteByActivityId(id);
    }

    private SalesDiscountActivityDO validateDiscountActivityExists(Long id) {
        SalesDiscountActivityDO discountActivity = discountActivityMapper.selectById(id);
        if (discountActivity == null) {
            throw exception(DISCOUNT_ACTIVITY_NOT_EXISTS);
        }
        return discountActivity;
    }

    @Override
    public SalesDiscountActivityDO getDiscountActivity(Long id) {
        return discountActivityMapper.selectById(id);
    }

    @Override
    public PageResult<SalesDiscountActivityDO> getDiscountActivityPage(SalesDiscountActivityPageReqVO pageReqVO) {
        return discountActivityMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SalesDiscountProductDO> getDiscountProductsByActivityId(Long activityId) {
        return discountProductMapper.selectListByActivityId(activityId);
    }

    @Override
    public List<SalesDiscountProductDO> getDiscountProductsByActivityId(Collection<Long> activityIds) {
        if (CollUtil.isEmpty(activityIds)) {
            return CollUtil.newArrayList();
        }
        return discountProductMapper.selectList(SalesDiscountProductDO::getActivityId, activityIds);
    }

    @Override
    public List<SalesDiscountProductDO> getMatchDiscountProductListBySkuIds(Collection<Long> skuIds) {
        if (CollUtil.isEmpty(skuIds)) {
            return CollUtil.newArrayList();
        }
        return discountProductMapper.selectListBySkuIdsAndStatusAndNow(skuIds, CommonStatusEnum.ENABLE.getStatus());
    }

}
