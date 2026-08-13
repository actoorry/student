package vip.appap.suxin.module.sales.dal.mysql;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationRecordReqPageVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCombinationRecordPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationRecordDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 拼团记录 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface SalesCombinationRecordMapper extends BaseMapperX<SalesCombinationRecordDO> {

    default SalesCombinationRecordDO selectByUserIdAndOrderId(Long userId, Long orderId) {
        return selectOne(SalesCombinationRecordDO::getUserId, userId,
                SalesCombinationRecordDO::getOrderId, orderId);
    }

    /**
     * 查询拼团记录
     *
     * @param headId 团长编号
     * @return 拼团记录
     */
    default SalesCombinationRecordDO selectByHeadId(Long headId, Integer status) {
        return selectOne(new LambdaQueryWrapperX<SalesCombinationRecordDO>()
                .eq(SalesCombinationRecordDO::getId, headId)
                .eq(SalesCombinationRecordDO::getStatus, status));
    }

    /**
     * 查询拼团记录
     *
     * @param userId     用户 id
     * @param activityId 活动 id
     * @return 拼团记录
     */
    default List<SalesCombinationRecordDO> selectListByUserIdAndActivityId(Long userId, Long activityId) {
        return selectList(new LambdaQueryWrapperX<SalesCombinationRecordDO>()
                .eq(SalesCombinationRecordDO::getUserId, userId)
                .eq(SalesCombinationRecordDO::getActivityId, activityId));
    }

    /**
     * 获取最近的 count 条数据
     *
     * @param count 数量
     * @return 拼团记录列表
     */
    default List<SalesCombinationRecordDO> selectLatestList(int count) {
        return selectList(new LambdaQueryWrapperX<SalesCombinationRecordDO>()
                .orderByDesc(SalesCombinationRecordDO::getId)
                .last("LIMIT " + count));
    }

    default List<SalesCombinationRecordDO> selectListByActivityIdAndStatusAndHeadId(Long activityId, Integer status,
                                                                               Long headId, Integer count) {
        return selectList(new LambdaQueryWrapperX<SalesCombinationRecordDO>()
                .eqIfPresent(SalesCombinationRecordDO::getActivityId, activityId)
                .eqIfPresent(SalesCombinationRecordDO::getStatus, status)
                .eq(SalesCombinationRecordDO::getHeadId, headId)
                .orderByDesc(SalesCombinationRecordDO::getId)
                .last("LIMIT " + count));
    }

    default Map<Long, Integer> selectCombinationRecordCountMapByActivityIdAndStatusAndHeadId(Collection<Long> activityIds,
                                                                                             Integer status, Long headId) {
        // SQL count 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<SalesCombinationRecordDO>()
                .select("COUNT(DISTINCT(user_id)) AS recordCount, activity_id AS activityId")
                .in("activity_id", activityIds)
                .eq(status != null, "status", status)
                .eq(headId != null, "head_id", headId)
                .groupBy("activity_id"));
        if (CollUtil.isEmpty(result)) {
            return Collections.emptyMap();
        }
        // 转换数据
        return CollectionUtils.convertMap(result,
                record -> MapUtil.getLong(record, "activityId"),
                record -> MapUtil.getInt(record, "recordCount"));
    }

    default PageResult<SalesCombinationRecordDO> selectPage(SalesCombinationRecordReqPageVO pageVO) {
        LambdaQueryWrapperX<SalesCombinationRecordDO> queryWrapper = new LambdaQueryWrapperX<SalesCombinationRecordDO>()
                .eqIfPresent(SalesCombinationRecordDO::getStatus, pageVO.getStatus())
                .betweenIfPresent(SalesCombinationRecordDO::getCreateTime, pageVO.getCreateTime());
        // 如果 headId 非空，说明查询指定团的团长 + 团员的拼团记录
        if (pageVO.getHeadId() != null) {
            queryWrapper.eq(SalesCombinationRecordDO::getId, pageVO.getHeadId()) // 团长
                    .or().eq(SalesCombinationRecordDO::getHeadId, pageVO.getHeadId()); // 团员
        }
        return selectPage(pageVO, queryWrapper);
    }

    /**
     * 查询指定条件的记录数
     *
     * @param status       状态，可为 null
     * @param virtualGroup 是否虚拟成团，可为 null
     * @param headId       团长编号，可为 null
     * @return 记录数
     */
    default Long selectCountByHeadAndStatusAndVirtualGroup(Integer status, Boolean virtualGroup, Long headId) {
        return selectCount(new LambdaQueryWrapperX<SalesCombinationRecordDO>()
                .eqIfPresent(SalesCombinationRecordDO::getStatus, status)
                .eqIfPresent(SalesCombinationRecordDO::getVirtualGroup, virtualGroup)
                .eqIfPresent(SalesCombinationRecordDO::getHeadId, headId));
    }

    /**
     * 查询用户拼团记录（DISTINCT 去重），也就是说查询会员表中的用户有多少人参与过拼团活动每个人只统计一次
     *
     * @return 参加过拼团的用户数
     */
    default Long selectUserCount() {
        return selectCount(new QueryWrapper<SalesCombinationRecordDO>()
                .select("DISTINCT (user_id)"));
    }

    default List<SalesCombinationRecordDO> selectListByHeadIdAndStatusAndExpireTimeLt(Long headId, Integer status, LocalDateTime dateTime) {
        return selectList(new LambdaQueryWrapperX<SalesCombinationRecordDO>()
                .eq(SalesCombinationRecordDO::getHeadId, headId)
                .eq(SalesCombinationRecordDO::getStatus, status)
                .lt(SalesCombinationRecordDO::getExpireTime, dateTime));
    }

    default List<SalesCombinationRecordDO> selectListByHeadId(Long headId) {
        return selectList(SalesCombinationRecordDO::getHeadId, headId);
    }

    default PageResult<SalesCombinationRecordDO> selectPage(Long userId, AppSalesCombinationRecordPageReqVO pageReqVO) {
        LambdaQueryWrapperX<SalesCombinationRecordDO> queryWrapper = new LambdaQueryWrapperX<SalesCombinationRecordDO>()
                .eq(SalesCombinationRecordDO::getUserId, userId)
                .eqIfPresent(SalesCombinationRecordDO::getStatus, pageReqVO.getStatus());
        return selectPage(pageReqVO, queryWrapper);
    }

}
