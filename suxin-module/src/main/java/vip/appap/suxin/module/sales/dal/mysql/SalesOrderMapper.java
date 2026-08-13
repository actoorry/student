package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.framework.mybatis.core.query.MPJLambdaWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesOrderPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.enums.SalesOrderStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderTypeEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface SalesOrderMapper extends BaseMapperX<SalesOrderDO> {

    default int updateByIdAndStatus(Long id, Integer status, SalesOrderDO update) {
        return update(update, new LambdaUpdateWrapper<SalesOrderDO>()
                .eq(SalesOrderDO::getId, id).eq(SalesOrderDO::getStatus, status));
    }

    default SalesOrderDO selectByIdAndUserId(Long id, Long userId) {
        return selectOne(SalesOrderDO::getId, id, SalesOrderDO::getUserId, userId);
    }

    default PageResult<SalesOrderDO> selectPage(SalesOrderPageReqVO reqVO, Set<Long> userIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesOrderDO>()
                .likeIfPresent(SalesOrderDO::getNo, reqVO.getNo())
                .eqIfPresent(SalesOrderDO::getUserId, reqVO.getUserId())
                .eqIfPresent(SalesOrderDO::getDeliveryType, reqVO.getDeliveryType())
                .inIfPresent(SalesOrderDO::getUserId, userIds)
                .eqIfPresent(SalesOrderDO::getType, reqVO.getType())
                .eqIfPresent(SalesOrderDO::getStatus, reqVO.getStatus())
                .eqIfPresent(SalesOrderDO::getPayChannelCode, reqVO.getPayChannelCode())
                .eqIfPresent(SalesOrderDO::getTerminal, reqVO.getTerminal())
                .eqIfPresent(SalesOrderDO::getLogisticsId, reqVO.getLogisticsId())
                .inIfPresent(SalesOrderDO::getPickUpStoreId, reqVO.getPickUpStoreIds())
                .likeIfPresent(SalesOrderDO::getPickUpVerifyCode, reqVO.getPickUpVerifyCode())
                .betweenIfPresent(SalesOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SalesOrderDO::getId));
    }

    // TODO @疯狂：如果用 map 返回，要不这里直接用 SalesOrderSummaryRespVO 返回？也算合理，就当  sql 查询出这么个玩意~~
    default List<Map<String, Object>> selectOrderSummaryGroupByRefundStatus(SalesOrderPageReqVO reqVO, Set<Long> userIds) {
        return selectMaps(new MPJLambdaWrapperX<SalesOrderDO>()
                .selectAs(SalesOrderDO::getRefundStatus, SalesOrderDO::getRefundStatus)  // 售后状态
                .selectCount(SalesOrderDO::getId, "count") // 售后状态对应的数量
                .selectSum(SalesOrderDO::getPayPrice, "price")  // 售后状态对应的支付金额
                .likeIfPresent(SalesOrderDO::getNo, reqVO.getNo())
                .eqIfPresent(SalesOrderDO::getUserId, reqVO.getUserId())
                .eqIfPresent(SalesOrderDO::getDeliveryType, reqVO.getDeliveryType())
                .inIfPresent(SalesOrderDO::getUserId, userIds)
                .eqIfPresent(SalesOrderDO::getType, reqVO.getType())
                .eqIfPresent(SalesOrderDO::getStatus, reqVO.getStatus())
                .eqIfPresent(SalesOrderDO::getPayChannelCode, reqVO.getPayChannelCode())
                .eqIfPresent(SalesOrderDO::getTerminal, reqVO.getTerminal())
                .eqIfPresent(SalesOrderDO::getLogisticsId, reqVO.getLogisticsId())
                .inIfPresent(SalesOrderDO::getPickUpStoreId, reqVO.getPickUpStoreIds())
                .likeIfPresent(SalesOrderDO::getPickUpVerifyCode, reqVO.getPickUpVerifyCode())
                .betweenIfPresent(SalesOrderDO::getCreateTime, reqVO.getCreateTime())
                .groupBy(SalesOrderDO::getRefundStatus)); // 按售后状态分组
    }

    default PageResult<SalesOrderDO> selectPage(AppSalesOrderPageReqVO reqVO, Long userId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesOrderDO>()
                .eq(SalesOrderDO::getUserId, userId)
                .eqIfPresent(SalesOrderDO::getStatus, reqVO.getStatus())
                .eqIfPresent(SalesOrderDO::getCommentStatus, reqVO.getCommentStatus())
                .orderByDesc(SalesOrderDO::getId)); // TODO 芋艿：未来不同的 status，不同的排序
    }

    default Long selectCountByUserIdAndStatus(Long userId, Integer status, Boolean commentStatus) {
        return selectCount(new LambdaQueryWrapperX<SalesOrderDO>()
                .eq(SalesOrderDO::getUserId, userId)
                .eqIfPresent(SalesOrderDO::getStatus, status)
                .eqIfPresent(SalesOrderDO::getCommentStatus, commentStatus));
    }

    default SalesOrderDO selectOrderByIdAndUserId(Long orderId, Long loginUserId) {
        return selectOne(new LambdaQueryWrapperX<SalesOrderDO>()
                .eq(SalesOrderDO::getId, orderId)
                .eq(SalesOrderDO::getUserId, loginUserId));
    }

    default List<SalesOrderDO> selectListByStatusAndCreateTimeLt(Integer status, LocalDateTime createTime) {
        return selectList(new LambdaUpdateWrapper<SalesOrderDO>()
                .eq(SalesOrderDO::getStatus, status)
                .lt(SalesOrderDO::getCreateTime, createTime));
    }

    default List<SalesOrderDO> selectListByStatusAndDeliveryTimeLt(Integer status, LocalDateTime deliveryTime) {
        return selectList(new LambdaUpdateWrapper<SalesOrderDO>()
                .eq(SalesOrderDO::getStatus, status)
                .lt(SalesOrderDO::getDeliveryTime, deliveryTime));
    }

    default List<SalesOrderDO> selectListByStatusAndReceiveTimeLt(Integer status, LocalDateTime receive,
                                                                  Boolean commentStatus) {
        return selectList(new LambdaUpdateWrapper<SalesOrderDO>()
                .eq(SalesOrderDO::getStatus, status)
                .lt(SalesOrderDO::getReceiveTime, receive)
                .eq(SalesOrderDO::getCommentStatus, commentStatus));
    }

    default List<SalesOrderDO> selectListByUserIdAndActivityId(Long userId, Long activityId, SalesOrderTypeEnum type) {
        LambdaQueryWrapperX<SalesOrderDO> queryWrapperX = new LambdaQueryWrapperX<>();
        queryWrapperX.eq(SalesOrderDO::getUserId, userId);
        if (SalesOrderTypeEnum.isSeckill(type.getType())) {
            queryWrapperX.eq(SalesOrderDO::getSeckillActivityId, activityId);
        }
        if (SalesOrderTypeEnum.isBargain(type.getType())) {
            queryWrapperX.eq(SalesOrderDO::getBargainActivityId, activityId);
        }
        if (SalesOrderTypeEnum.isCombination(type.getType())) {
            queryWrapperX.eq(SalesOrderDO::getCombinationActivityId, activityId);
        }
        if (SalesOrderTypeEnum.isPoint(type.getType())) {
            queryWrapperX.eq(SalesOrderDO::getPointActivityId, activityId);
        }
        return selectList(queryWrapperX);
    }

    default SalesOrderDO selectOneByPickUpVerifyCode(String pickUpVerifyCode) {
        return selectOne(SalesOrderDO::getPickUpVerifyCode, pickUpVerifyCode);
    }

    default SalesOrderDO selectByUserIdAndCombinationActivityIdAndStatus(Long userId, Long combinationActivityId, Integer status) {
        return selectOne(new LambdaQueryWrapperX<SalesOrderDO>()
                .eq(SalesOrderDO::getUserId, userId)
                .eq(SalesOrderDO::getStatus, status)
                .eq(SalesOrderDO::getCombinationActivityId, combinationActivityId)
        );
    }

    default List<SalesOrderDO> selectCompletedPaidOrdersByIdGt(Long idGt, Integer limit) {
        return selectList(new LambdaQueryWrapperX<SalesOrderDO>()
                .gtIfPresent(SalesOrderDO::getId, idGt)
                .eq(SalesOrderDO::getPayStatus, true)
                .eq(SalesOrderDO::getStatus, SalesOrderStatusEnum.COMPLETED.getStatus())
                .orderByAsc(SalesOrderDO::getId)
                .last(limit != null && limit > 0, "LIMIT " + limit));
    }

}
