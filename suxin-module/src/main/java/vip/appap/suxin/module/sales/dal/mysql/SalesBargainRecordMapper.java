package vip.appap.suxin.module.sales.dal.mysql;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBargainRecordPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainRecordDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 砍价记录 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface SalesBargainRecordMapper extends BaseMapperX<SalesBargainRecordDO> {

    default SalesBargainRecordDO selectByIdAndUserId(Long id, Long userId) {
        return selectOne(SalesBargainRecordDO::getId, id,
                SalesBargainRecordDO::getUserId, userId);
    }

    default List<SalesBargainRecordDO> selectListByUserIdAndActivityIdAndStatus(
            Long userId, Long activityId, Integer status) {
        return selectList(new LambdaQueryWrapper<>(SalesBargainRecordDO.class)
                .eq(SalesBargainRecordDO::getUserId, userId)
                .eq(SalesBargainRecordDO::getActivityId, activityId)
                .eq(SalesBargainRecordDO::getStatus, status));
    }

    default SalesBargainRecordDO selectLastByUserIdAndActivityId(Long userId, Long activityId) {
        return selectOne(new LambdaQueryWrapper<>(SalesBargainRecordDO.class)
                .eq(SalesBargainRecordDO::getUserId, userId)
                .eq(SalesBargainRecordDO::getActivityId, activityId)
                .orderByDesc(SalesBargainRecordDO::getId)
                .last("LIMIT 1"));
    }

    default Long selectCountByUserIdAndActivityIdAndStatus(
            Long userId, Long activityId, Integer status) {
        return selectCount(new LambdaQueryWrapper<>(SalesBargainRecordDO.class)
                .eq(SalesBargainRecordDO::getUserId, userId)
                .eq(SalesBargainRecordDO::getActivityId, activityId)
                .eq(SalesBargainRecordDO::getStatus, status));
    }

    default int updateByIdAndBargainPrice(Long id, Integer whereBargainPrice, SalesBargainRecordDO updateObj) {
        return update(updateObj, new LambdaQueryWrapper<>(SalesBargainRecordDO.class)
                .eq(SalesBargainRecordDO::getId, id)
                .eq(SalesBargainRecordDO::getBargainPrice, whereBargainPrice));
    }

    default Map<Long, Integer> selectUserCountByActivityIdsAndStatus(Collection<Long> activityIds, Integer status) {
        // SQL count 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<SalesBargainRecordDO>()
                .select("COUNT(DISTINCT(user_id)) AS userCount, activity_id AS activityId")
                .in("activity_id", activityIds)
                .eq(status != null, "status", status)
                .groupBy("activity_id"));
        if (CollUtil.isEmpty(result)) {
            return Collections.emptyMap();
        }
        // 转换数据
        return CollectionUtils.convertMap(result,
                record -> MapUtil.getLong(record, "activityId"),
                record -> MapUtil.getInt(record, "userCount" ));
    }

    @Select("SELECT COUNT(DISTINCT(user_id)) FROM sales_bargain_record " +
            "WHERE status = #{status}")
    Integer selectUserCountByStatus(@Param("status") Integer status);

    @Select("SELECT COUNT(DISTINCT(user_id)) FROM sales_bargain_record " +
            "WHERE activity_id = #{activityId} " +
            "AND status = #{status}")
    Integer selectUserCountByActivityIdAndStatus(@Param("activityId") Long activityId,
                                                 @Param("status") Integer status);

    default PageResult<SalesBargainRecordDO> selectPage(SalesBargainRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesBargainRecordDO>()
                .eqIfPresent(SalesBargainRecordDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SalesBargainRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SalesBargainRecordDO::getId));
    }

    default PageResult<SalesBargainRecordDO> selectBargainRecordPage(Long userId, PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<SalesBargainRecordDO>()
                .eq(SalesBargainRecordDO::getUserId, userId)
                .orderByDesc(SalesBargainRecordDO::getId));
    }

    default List<SalesBargainRecordDO> selectListByStatusAndCount(Integer status, Integer count) {
        return selectList(new LambdaQueryWrapper<>(SalesBargainRecordDO.class)
                .eq(SalesBargainRecordDO::getStatus, status)
                .last("LIMIT " + count));
    }

    /**
     * 更新砍价的订单编号，前提是 orderId 原本是空的
     *
     * @param id 砍价记录编号
     * @param orderId 订单编号
     * @return 更新数量
     */
    default int updateOrderIdById(Long id, Long orderId) {
        return update(new SalesBargainRecordDO().setOrderId(orderId).setEndTime(LocalDateTime.now()),
                new LambdaQueryWrapper<>(SalesBargainRecordDO.class)
                        .eq(SalesBargainRecordDO::getId, id)
                        .isNull(SalesBargainRecordDO::getOrderId));
    }

}
