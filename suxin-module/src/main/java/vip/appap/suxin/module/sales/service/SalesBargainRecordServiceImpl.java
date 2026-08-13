package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.api.dto.SalesBargainValidateJoinRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBargainRecordPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBargainRecordCreateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainRecordDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesBargainRecordMapper;
import vip.appap.suxin.module.sales.enums.SalesBargainRecordStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.*;

/**
 * 砍价记录 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class SalesBargainRecordServiceImpl implements SalesBargainRecordService {

    @Resource
    private SalesBargainActivityService bargainActivityService;

    @Resource
    private SalesBargainRecordMapper bargainRecordMapper;

    @Override
    public Long createBargainRecord(Long userId, AppSalesBargainRecordCreateReqVO reqVO) {
        // 1. 校验砍价活动（包括库存）
        SalesBargainActivityDO activity = bargainActivityService.validateBargainActivityCanJoin(reqVO.getActivityId());

        // 2.1 校验当前是否已经有参与中的砍价活动
        if (CollUtil.isNotEmpty(bargainRecordMapper.selectListByUserIdAndActivityIdAndStatus(
                userId, reqVO.getActivityId(), SalesBargainRecordStatusEnum.IN_PROGRESS.getStatus()))) {
            throw exception(BARGAIN_RECORD_CREATE_FAIL_EXISTS);
        }
        // 2.2 是否超过参与的上限
        if (bargainRecordMapper.selectCountByUserIdAndActivityIdAndStatus(
                userId, reqVO.getActivityId(), SalesBargainRecordStatusEnum.SUCCESS.getStatus()) >= activity.getTotalLimitCount()) {
            throw exception(BARGAIN_RECORD_CREATE_FAIL_LIMIT);
        }

        // 3. 创建砍价记录
        SalesBargainRecordDO record = SalesBargainRecordDO.builder().userId(userId)
                .activityId(reqVO.getActivityId()).spuId(activity.getSpuId()).skuId(activity.getSkuId())
                .bargainFirstPrice(activity.getBargainFirstPrice()).bargainPrice(activity.getBargainFirstPrice())
                .status(SalesBargainRecordStatusEnum.IN_PROGRESS.getStatus()).build();
        bargainRecordMapper.insert(record);
        return record.getId();
    }

    @Override
    public Boolean updateBargainRecordBargainPrice(Long id, Integer whereBargainPrice,
                                                   Integer reducePrice, Boolean success) {
        SalesBargainRecordDO updateObj = new SalesBargainRecordDO().setBargainPrice(whereBargainPrice - reducePrice);
        if (success) {
            updateObj.setStatus(SalesBargainRecordStatusEnum.SUCCESS.getStatus());
        }
        return bargainRecordMapper.updateByIdAndBargainPrice(id, whereBargainPrice, updateObj) > 0;
    }

    @Override
    public SalesBargainValidateJoinRespDTO validateJoinBargain(Long userId, Long bargainRecordId, Long skuId) {
        // 1.1 砍价记录不存在
        SalesBargainRecordDO record = bargainRecordMapper.selectByIdAndUserId(bargainRecordId, userId);
        if (record == null) {
            throw exception(BARGAIN_RECORD_NOT_EXISTS);
        }
        // 1.2 砍价记录未在进行中
        if (ObjUtil.notEqual(record.getStatus(), SalesBargainRecordStatusEnum.SUCCESS.getStatus())) {
            throw exception(BARGAIN_JOIN_RECORD_NOT_SUCCESS);
        }
        // 1.3 砍价记录已经下单
        if (record.getOrderId() != null) {
            throw exception(BARGAIN_JOIN_RECORD_ALREADY_ORDER);
        }

        // 2.1 校验砍价活动（包括库存）
        SalesBargainActivityDO activity = bargainActivityService.validateBargainActivityCanJoin(record.getActivityId());
        Assert.isTrue(Objects.equals(skuId, activity.getSkuId()), "砍价商品不匹配"); // 防御性校验
        return new SalesBargainValidateJoinRespDTO().setActivityId(activity.getId()).setName(activity.getName())
                .setBargainPrice(record.getBargainPrice());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBargainRecordOrderId(Long id, Long orderId) {
        // 更新失败，说明已经下单
        int updateCount = bargainRecordMapper.updateOrderIdById(id, orderId);
        if (updateCount == 0) {
            throw exception(BARGAIN_JOIN_RECORD_ALREADY_ORDER);
        }
    }

    @Override
    public SalesBargainRecordDO getBargainRecord(Long id) {
        return bargainRecordMapper.selectById(id);
    }

    @Override
    public SalesBargainRecordDO getLastBargainRecord(Long userId, Long activityId) {
        return bargainRecordMapper.selectLastByUserIdAndActivityId(userId, activityId);
    }

    @Override
    public Map<Long, Integer> getBargainRecordUserCountMap(Collection<Long> activityIds, @Nullable Integer status) {
        return bargainRecordMapper.selectUserCountByActivityIdsAndStatus(activityIds, status);
    }

    @Override
    public Integer getBargainRecordUserCount(Integer status) {
        return bargainRecordMapper.selectUserCountByStatus(status);
    }

    @Override
    public Integer getBargainRecordUserCount(Long activityId, Integer status) {
        return bargainRecordMapper.selectUserCountByActivityIdAndStatus(activityId, status);
    }

    @Override
    public PageResult<SalesBargainRecordDO> getBargainRecordPage(SalesBargainRecordPageReqVO pageReqVO) {
        return bargainRecordMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<SalesBargainRecordDO> getBargainRecordPage(Long userId, PageParam pageParam) {
        return bargainRecordMapper.selectBargainRecordPage(userId, pageParam);
    }

    @Override
    public List<SalesBargainRecordDO> getBargainRecordList(Integer status, Integer count) {
        return bargainRecordMapper.selectListByStatusAndCount(status, count);
    }

}
