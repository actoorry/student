package vip.appap.suxin.module.sales.service;

import cn.hutool.core.util.ObjectUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.date.LocalDateTimeUtils;
import vip.appap.suxin.module.product.api.ProductSkuApi;
import vip.appap.suxin.module.product.api.dto.ProductSkuRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBargainActivityCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBargainActivityPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBargainActivityUpdateReqVO;
import vip.appap.suxin.module.sales.convert.SalesBargainActivityConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainActivityDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesBargainActivityMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.anyMatch;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.*;

/**
 * 砍价活动 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class SalesBargainActivityServiceImpl implements SalesBargainActivityService {

    @Resource
    private SalesBargainActivityMapper bargainActivityMapper;

    @Resource
    private ProductSkuApi productSkuApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBargainActivity(SalesBargainActivityCreateReqVO createReqVO) {
        // 校验商品 SPU 是否存在是否参加的别的活动
        validateBargainConflict(createReqVO.getSpuId(), null);
        // 校验商品 sku 是否存在
        validateSku(createReqVO.getSkuId());

        // 插入砍价活动
        SalesBargainActivityDO activityDO = SalesBargainActivityConvert.INSTANCE.convert(createReqVO)
                .setTotalStock(createReqVO.getStock())
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        bargainActivityMapper.insert(activityDO);
        return activityDO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBargainActivity(SalesBargainActivityUpdateReqVO updateReqVO) {
        // 校验存在
        SalesBargainActivityDO activity = validateBargainActivityExists(updateReqVO.getId());
        // 校验状态
        if (ObjectUtil.equal(activity.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            throw exception(BARGAIN_ACTIVITY_STATUS_DISABLE);
        }
        // 校验商品冲突
        validateBargainConflict(updateReqVO.getSpuId(), updateReqVO.getId());
        // 校验商品 sku 是否存在
        validateSku(updateReqVO.getSkuId());

        // 更新
        SalesBargainActivityDO updateObj = SalesBargainActivityConvert.INSTANCE.convert(updateReqVO);
        if (updateObj.getStock() > activity.getTotalStock()) { // 如果更新的库存大于原来的库存，则更新总库存
            updateObj.setTotalStock(updateObj.getStock());
        }
        bargainActivityMapper.updateById(updateObj);
    }

    @Override
    public void updateBargainActivityStock(Long id, Integer count) {
        if (count < 0) {
            // 更新库存。如果更新失败，则抛出异常
            int updateCount = bargainActivityMapper.updateStock(id, count);
            if (updateCount == 0) {
                throw exception(BARGAIN_ACTIVITY_STOCK_NOT_ENOUGH);
            }
        } else if (count > 0) {
            bargainActivityMapper.updateStock(id, count);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeBargainActivityById(Long id) {
        // 校验砍价活动是否存在
        SalesBargainActivityDO activity = validateBargainActivityExists(id);
        if (CommonStatusEnum.isDisable(activity.getStatus())) {
            throw exception(BARGAIN_ACTIVITY_STATUS_DISABLE);
        }

        bargainActivityMapper.updateById(new SalesBargainActivityDO().setId(id)
                .setStatus(CommonStatusEnum.DISABLE.getStatus()));
    }

    private void validateBargainConflict(Long spuId, Long activityId) {
        // 查询所有开启的砍价活动
        List<SalesBargainActivityDO> activityList = bargainActivityMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        if (activityId != null) { // 更新时排除自己
            activityList.removeIf(item -> ObjectUtil.equal(item.getId(), activityId));
        }
        // 校验商品 spu 是否参加了其它活动
        if (anyMatch(activityList, activity -> ObjectUtil.equal(activity.getSpuId(), spuId))) {
            throw exception(BARGAIN_ACTIVITY_SPU_CONFLICTS);
        }
    }

    private void validateSku(Long skuId) {
        ProductSkuRespDTO sku = productSkuApi.getSku(skuId);
        if (sku == null) {
            throw exception(SKU_NOT_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBargainActivity(Long id) {
        // 校验存在
        SalesBargainActivityDO activityDO = validateBargainActivityExists(id);
        // 校验状态
        if (CommonStatusEnum.isEnable(activityDO.getStatus())) {
            throw exception(BARGAIN_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END);
        }

        // 删除
        bargainActivityMapper.deleteById(id);
    }

    private SalesBargainActivityDO validateBargainActivityExists(Long id) {
        SalesBargainActivityDO activityDO = bargainActivityMapper.selectById(id);
        if (activityDO == null) {
            throw exception(BARGAIN_ACTIVITY_NOT_EXISTS);
        }
        return activityDO;
    }

    @Override
    public SalesBargainActivityDO getBargainActivity(Long id) {
        return bargainActivityMapper.selectById(id);
    }

    @Override
    public List<SalesBargainActivityDO> getBargainActivityList(Set<Long> ids) {
        return bargainActivityMapper.selectByIds(ids);
    }

    @Override
    public SalesBargainActivityDO validateBargainActivityCanJoin(Long id) {
        SalesBargainActivityDO activity = bargainActivityMapper.selectById(id);
        if (activity == null) {
            throw exception(BARGAIN_ACTIVITY_NOT_EXISTS);
        }
        if (CommonStatusEnum.isDisable(activity.getStatus())) {
            throw exception(BARGAIN_ACTIVITY_STATUS_CLOSED);
        }
        if (activity.getStock() <= 0) {
            throw exception(BARGAIN_ACTIVITY_STOCK_NOT_ENOUGH);
        }
        if (!LocalDateTimeUtils.isBetween(activity.getStartTime(), activity.getEndTime())) {
            throw exception(BARGAIN_ACTIVITY_TIME_END);
        }
        return activity;
    }

    @Override
    public PageResult<SalesBargainActivityDO> getBargainActivityPage(SalesBargainActivityPageReqVO pageReqVO) {
        return bargainActivityMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<SalesBargainActivityDO> getBargainActivityPage(PageParam pageReqVO) {
        // 只查询进行中，且在时间范围内的
        return bargainActivityMapper.selectPage(pageReqVO, CommonStatusEnum.ENABLE.getStatus(), LocalDateTime.now());
    }

    @Override
    public List<SalesBargainActivityDO> getBargainActivityListByCount(Integer count) {
        return bargainActivityMapper.selectList(count, CommonStatusEnum.ENABLE.getStatus(), LocalDateTime.now());
    }

    @Override
    public SalesBargainActivityDO getMatchBargainActivityBySpuId(Long spuId) {
        return bargainActivityMapper.selectBySpuIdAndStatusAndNow(spuId, CommonStatusEnum.ENABLE.getStatus());
    }

}
