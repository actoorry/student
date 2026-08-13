package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.extra.spring.SpringUtil;
import vip.appap.suxin.framework.common.core.KeyValue;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.json.JsonUtils;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.product.api.ProductSkuApi;
import vip.appap.suxin.module.product.api.dto.ProductSkuRespDTO;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.api.dto.SalesCombinationRecordCreateReqDTO;
import vip.appap.suxin.module.sales.api.dto.SalesCombinationValidateJoinRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationRecordReqPageVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCombinationRecordPageReqVO;
import vip.appap.suxin.module.sales.convert.SalesCombinationActivityConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationProductDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationRecordDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesCombinationRecordMapper;
import vip.appap.suxin.module.sales.enums.SalesCombinationRecordStatusEnum;
import vip.appap.suxin.module.system.api.SocialClientApi;
import vip.appap.suxin.module.system.api.dto.SocialWxaSubscribeMessageSendReqDTO;
import vip.appap.suxin.module.sales.api.SalesOrderApi;
import vip.appap.suxin.module.sales.enums.SalesOrderCancelTypeEnum;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.*;
import static vip.appap.suxin.framework.common.util.date.LocalDateTimeUtils.afterNow;
import static vip.appap.suxin.framework.common.util.date.LocalDateTimeUtils.beforeNow;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.*;
import static vip.appap.suxin.module.sales.enums.SalesMessageTemplateConstants.COMBINATION_SUCCESS;

/**
 * 拼团记录 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Slf4j
@Validated
public class SalesCombinationRecordServiceImpl implements SalesCombinationRecordService {

    @Resource
    private SalesCombinationActivityService combinationActivityService;
    @Resource
    private SalesCombinationRecordMapper combinationRecordMapper;

    @Resource
    private PartnerApi PartnerApi;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductSkuApi productSkuApi;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private SalesOrderApi tradeOrderApi;
    @Resource
    public SocialClientApi socialClientApi;

    // TODO @芋艿：在详细预览下；
    @Override
    public KeyValue<SalesCombinationActivityDO, SalesCombinationProductDO> validateCombinationRecord(
            Long userId, Long activityId, Long headId, Long skuId, Integer count) {
        // 1. 校验拼团活动是否存在
        SalesCombinationActivityDO activity = combinationActivityService.validateCombinationActivityExists(activityId);
        // 1.1 校验活动是否开启
        if (ObjUtil.equal(activity.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            throw exception(COMBINATION_ACTIVITY_STATUS_DISABLE);
        }
        // 1.2. 校验活动开始时间
        if (afterNow(activity.getStartTime())) {
            throw exception(COMBINATION_RECORD_FAILED_TIME_NOT_START);
        }
        // 1.3 校验是否超出单次限购数量
        if (count > activity.getSingleLimitCount()) {
            throw exception(COMBINATION_RECORD_FAILED_SINGLE_LIMIT_COUNT_EXCEED);
        }

        // 2. 父拼团是否存在,是否已经满了
        if (headId != null) {
            // 2.1. 查询进行中的父拼团
            SalesCombinationRecordDO record = combinationRecordMapper.selectByHeadId(headId, SalesCombinationRecordStatusEnum.IN_PROGRESS.getStatus());
            if (record == null) {
                throw exception(COMBINATION_RECORD_HEAD_NOT_EXISTS);
            }
            // 2.2. 校验拼团是否已满
            if (ObjUtil.equal(record.getUserCount(), record.getUserSize())) {
                throw exception(COMBINATION_RECORD_USER_FULL);
            }
            // 2.3 校验拼团是否过期（有父拼团的时候只校验父拼团的过期时间）
            if (beforeNow(record.getExpireTime())) {
                throw exception(COMBINATION_RECORD_FAILED_TIME_END);
            }
        } else {
            // 3. 校验当前活动是否结束(自己是父拼团的时候才校验活动是否结束)
            if (beforeNow(activity.getEndTime())) {
                throw exception(COMBINATION_RECORD_FAILED_TIME_END);
            }
        }

        // 4.1 校验活动商品是否存在
        SalesCombinationProductDO product = combinationActivityService.selectByActivityIdAndSkuId(activityId, skuId);
        if (product == null) {
            throw exception(COMBINATION_JOIN_ACTIVITY_PRODUCT_NOT_EXISTS);
        }
        // 4.2 校验 sku 是否存在
        ProductSkuRespDTO sku = productSkuApi.getSku(skuId);
        if (sku == null) {
            throw exception(COMBINATION_JOIN_ACTIVITY_PRODUCT_NOT_EXISTS);
        }
        // 4.3 校验库存是否充足
        if (count >= sku.getStock()) {
            throw exception(COMBINATION_ACTIVITY_UPDATE_STOCK_FAIL);
        }

        // 6.1 校验是否有拼团记录
        List<SalesCombinationRecordDO> recordList = combinationRecordMapper.selectListByUserIdAndActivityId(userId, activityId);
        recordList.removeIf(record -> SalesCombinationRecordStatusEnum.isFailed(record.getStatus())); // 取消的订单，不算数
        if (CollUtil.isEmpty(recordList)) { // 如果为空，说明可以参与，直接返回
            return new KeyValue<>(activity, product);
        }
        // 6.2 校验用户是否有该活动正在进行的拼团
        SalesCombinationRecordDO inProgressRecord = findFirst(recordList,
                record -> SalesCombinationRecordStatusEnum.isInProgress(record.getStatus()));
        if (inProgressRecord != null) {
            throw exception(COMBINATION_RECORD_FAILED_HAVE_JOINED);
        }
        // 6.3 校验是否超出总限购数量
        Integer sumValue = getSumValue(recordList, SalesCombinationRecordDO::getCount, Integer::sum);
        if (sumValue != null && sumValue + count > activity.getTotalLimitCount()) {
            throw exception(COMBINATION_RECORD_FAILED_TOTAL_LIMIT_COUNT_EXCEED);
        }
        return new KeyValue<>(activity, product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalesCombinationRecordDO createCombinationRecord(SalesCombinationRecordCreateReqDTO reqDTO) {
        // 1. 校验拼团活动
        KeyValue<SalesCombinationActivityDO, SalesCombinationProductDO> keyValue = validateCombinationRecord(reqDTO.getUserId(),
                reqDTO.getActivityId(), reqDTO.getHeadId(), reqDTO.getSkuId(), reqDTO.getCount());

        // 2. 组合数据创建拼团记录
        PartnerRespDTO user = PartnerApi.getUser(reqDTO.getUserId());
        ProductSpuRespDTO spu = productSpuApi.getSpu(reqDTO.getSpuId());
        ProductSkuRespDTO sku = productSkuApi.getSku(reqDTO.getSkuId());
        SalesCombinationRecordDO record = SalesCombinationActivityConvert.INSTANCE.convert(reqDTO, keyValue.getKey(), user, spu, sku);
        // 2.1. 如果是团长需要设置 headId 为 SalesCombinationRecordDO#HEAD_ID_GROUP
        if (record.getHeadId() == null) {
            record.setStartTime(LocalDateTime.now())
                    .setExpireTime(LocalDateTime.now().plusHours(keyValue.getKey().getLimitDuration()))
                    .setHeadId(SalesCombinationRecordDO.HEAD_ID_GROUP);
        } else {
            // 2.2.有团长的情况下需要设置开始时间和过期时间为团长的
            SalesCombinationRecordDO headRecord = combinationRecordMapper.selectByHeadId(record.getHeadId(),
                    SalesCombinationRecordStatusEnum.IN_PROGRESS.getStatus()); // 查询进行中的父拼团
            record.setStartTime(headRecord.getStartTime()).setExpireTime(headRecord.getExpireTime());
        }
        combinationRecordMapper.insert(record);

        // 3. 更新拼团记录
        if (ObjUtil.notEqual(SalesCombinationRecordDO.HEAD_ID_GROUP, record.getHeadId())) {
            updateCombinationRecordWhenCreate(reqDTO.getHeadId(), keyValue.getKey());
        }
        return record;
    }

    /**
     * 当新增拼团时，更新拼团记录的进展
     *
     * @param headId   团长编号
     * @param activity 活动
     */
    private void updateCombinationRecordWhenCreate(Long headId, SalesCombinationActivityDO activity) {
        // 1. 团长 + 团员
        List<SalesCombinationRecordDO> records = getCombinationRecordListByHeadId(headId);
        if (CollUtil.isEmpty(records)) {
            return;
        }
        SalesCombinationRecordDO headRecord = combinationRecordMapper.selectById(headId);

        // 2. 批量更新记录
        List<SalesCombinationRecordDO> updateRecords = new ArrayList<>();
        records.add(headRecord); // 加入团长，团长也需要更新
        boolean isFull = records.size() >= activity.getUserSize();
        LocalDateTime now = LocalDateTime.now();
        records.forEach(item -> {
            SalesCombinationRecordDO updateRecord = new SalesCombinationRecordDO();
            updateRecord.setId(item.getId()).setUserCount(records.size());
            if (isFull) {
                updateRecord.setStatus(SalesCombinationRecordStatusEnum.SUCCESS.getStatus());
                updateRecord.setEndTime(now);
            }
            updateRecords.add(updateRecord);
        });
        Boolean updateSuccess = combinationRecordMapper.updateBatch(updateRecords);

        // 3. 拼团成功发送订阅消息
        if (updateSuccess && isFull) {
            records.forEach(item -> getSelf().sendCombinationResultMessage(item));
        }
    }

    @Async
    public void sendCombinationResultMessage(SalesCombinationRecordDO record) {
        // 构建并发送模版消息
        socialClientApi.sendWxaSubscribeMessage(new SocialWxaSubscribeMessageSendReqDTO()
                .setUserId(record.getUserId()).setUserType(UserTypeEnum.MEMBER.getValue())
                .setTemplateTitle(COMBINATION_SUCCESS)
                .setPage("pages/order/detail?id=" + record.getOrderId()) // 订单详情页
                .addMessage("thing1", "商品拼团活动") // 活动标题
                .addMessage("thing2", "恭喜您拼团成功！我们将尽快为您发货。")); // 温馨提示
    }

    @Override
    public SalesCombinationRecordDO getCombinationRecord(Long userId, Long orderId) {
        return combinationRecordMapper.selectByUserIdAndOrderId(userId, orderId);
    }

    @Override
    public SalesCombinationValidateJoinRespDTO validateJoinCombination(Long userId, Long activityId, Long headId,
                                                                  Long skuId, Integer count) {
        KeyValue<SalesCombinationActivityDO, SalesCombinationProductDO> keyValue = validateCombinationRecord(userId, activityId,
                headId, skuId, count);
        return new SalesCombinationValidateJoinRespDTO().setActivityId(keyValue.getKey().getId())
                .setName(keyValue.getKey().getName()).setCombinationPrice(keyValue.getValue().getCombinationPrice());
    }

    @Override
    public Long getCombinationRecordCount(@Nullable Integer status, @Nullable Boolean virtualGroup, @Nullable Long headId) {
        return combinationRecordMapper.selectCountByHeadAndStatusAndVirtualGroup(status, virtualGroup, headId);
    }

    @Override
    public Long getCombinationUserCount() {
        return combinationRecordMapper.selectUserCount();
    }

    @Override
    public List<SalesCombinationRecordDO> getLatestCombinationRecordList(int count) {
        return combinationRecordMapper.selectLatestList(count);
    }

    @Override
    public List<SalesCombinationRecordDO> getHeadCombinationRecordList(Long activityId, Integer status, Integer count) {
        return combinationRecordMapper.selectListByActivityIdAndStatusAndHeadId(activityId, status,
                SalesCombinationRecordDO.HEAD_ID_GROUP, count);
    }

    @Override
    public SalesCombinationRecordDO getCombinationRecordById(Long id) {
        return combinationRecordMapper.selectById(id);
    }

    @Override
    public List<SalesCombinationRecordDO> getCombinationRecordListByHeadId(Long headId) {
        return combinationRecordMapper.selectList(SalesCombinationRecordDO::getHeadId, headId);
    }

    @Override
    public PageResult<SalesCombinationRecordDO> getCombinationRecordPage(SalesCombinationRecordReqPageVO pageVO) {
        return combinationRecordMapper.selectPage(pageVO);
    }

    @Override
    public Map<Long, Integer> getCombinationRecordCountMapByActivity(Collection<Long> activityIds,
                                                                     @Nullable Integer status, @Nullable Long headId) {
        return combinationRecordMapper.selectCombinationRecordCountMapByActivityIdAndStatusAndHeadId(activityIds, status, headId);
    }

    @Override
    public KeyValue<Integer, Integer> expireCombinationRecord() {
        // 1. 获取所有正在进行中的过期的父拼团
        List<SalesCombinationRecordDO> headExpireRecords = combinationRecordMapper.selectListByHeadIdAndStatusAndExpireTimeLt(
                SalesCombinationRecordDO.HEAD_ID_GROUP, SalesCombinationRecordStatusEnum.IN_PROGRESS.getStatus(), LocalDateTime.now());
        if (CollUtil.isEmpty(headExpireRecords)) {
            return new KeyValue<>(0, 0);
        }

        // 2. 获取拼团活动
        List<SalesCombinationActivityDO> activities = combinationActivityService.getCombinationActivityListByIds(
                convertSet(headExpireRecords, SalesCombinationRecordDO::getActivityId));
        Map<Long, SalesCombinationActivityDO> activityMap = convertMap(activities, SalesCombinationActivityDO::getId);

        // 3. 逐个处理拼团，过期 or 虚拟成团
        KeyValue<Integer, Integer> keyValue = new KeyValue<>(0, 0); // 统计过期拼团和虚拟成团
        for (SalesCombinationRecordDO record : headExpireRecords) {
            try {
                SalesCombinationActivityDO activity = activityMap.get(record.getActivityId());
                if (activity == null || !activity.getVirtualGroup()) { // 取不到活动的或者不是虚拟拼团的
                    // 3.1. 处理过期的拼团
                    getSelf().handleExpireRecord(record);
                    keyValue.setKey(keyValue.getKey() + 1);
                } else {
                    // 3.2. 处理虚拟成团
                    getSelf().handleVirtualGroupRecord(record);
                    keyValue.setValue(keyValue.getValue() + 1);
                }
            } catch (Exception ignored) { // 处理异常继续循环
                log.error("[expireCombinationRecord][record({}) 处理异常，请进行处理！record 数据是：{}]",
                        record.getId(), JsonUtils.toJsonString(record));
            }
        }
        return keyValue;
    }

    /**
     * 处理过期拼团
     *
     * @param headRecord 过期拼团团长记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleExpireRecord(SalesCombinationRecordDO headRecord) {
        // 1. 更新拼团记录
        List<SalesCombinationRecordDO> headAndRecords = updateBatchCombinationRecords(headRecord,
                SalesCombinationRecordStatusEnum.FAILED);
        // 2. 订单取消
        headAndRecords.forEach(item -> tradeOrderApi.cancelPaidOrder(item.getUserId(), item.getOrderId(),
                SalesOrderCancelTypeEnum.COMBINATION_CLOSE.getType()));
    }

    /**
     * 处理虚拟拼团
     *
     * @param headRecord 虚拟成团团长记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleVirtualGroupRecord(SalesCombinationRecordDO headRecord) {
        // 1. 团员补齐
        combinationRecordMapper.insertBatch(SalesCombinationActivityConvert.INSTANCE.convertVirtualRecordList(headRecord));
        // 2. 更新拼团记录
        updateBatchCombinationRecords(headRecord, SalesCombinationRecordStatusEnum.SUCCESS);
    }

    /**
     * 更新拼团记录
     *
     * @param headRecord 团长记录
     * @param status     状态-拼团失败 FAILED 成功 SUCCESS
     * @return 整团记录（包含团长和团成员）
     */
    private List<SalesCombinationRecordDO> updateBatchCombinationRecords(SalesCombinationRecordDO headRecord, SalesCombinationRecordStatusEnum status) {
        // 1. 查询团成员（包含团长）
        List<SalesCombinationRecordDO> records = combinationRecordMapper.selectListByHeadId(headRecord.getId());
        records.add(headRecord);// 把团长加进去

        // 2. 批量更新拼团记录 status 和 endTime
        List<SalesCombinationRecordDO> updateRecords = new ArrayList<>(records.size());
        LocalDateTime now = LocalDateTime.now();
        records.forEach(item -> {
            SalesCombinationRecordDO updateRecord = new SalesCombinationRecordDO().setId(item.getId())
                    .setStatus(status.getStatus()).setEndTime(now);
            if (SalesCombinationRecordStatusEnum.isSuccess(status.getStatus())) { // 虚拟成团完事更改状态成功后还需要把参与人数修改为成团需要人数
                updateRecord.setUserCount(records.size()).setVirtualGroup(Boolean.TRUE); // 标记为虚拟成团
            }
            updateRecords.add(updateRecord);
        });
        combinationRecordMapper.updateBatch(updateRecords);
        return records;
    }

    @Override
    public PageResult<SalesCombinationRecordDO> getCombinationRecordPage(Long userId, AppSalesCombinationRecordPageReqVO pageReqVO) {
        return combinationRecordMapper.selectPage(userId, pageReqVO);
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private SalesCombinationRecordServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
