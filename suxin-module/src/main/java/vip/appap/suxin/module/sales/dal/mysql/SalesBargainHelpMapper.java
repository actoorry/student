package vip.appap.suxin.module.sales.dal.mysql;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBargainHelpPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainHelpDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mapper
public interface SalesBargainHelpMapper extends BaseMapperX<SalesBargainHelpDO> {

    default Long selectCountByUserIdAndActivityId(Long userId, Long activityId) {
        return selectCount(new LambdaQueryWrapper<>(SalesBargainHelpDO.class)
                .eq(SalesBargainHelpDO::getUserId, userId)
                .eq(SalesBargainHelpDO::getActivityId, activityId));
    }

    default Long selectUserCountMapByRecordId(Long recordId) {
        return selectCount(SalesBargainHelpDO::getRecordId, recordId);
    }

    default SalesBargainHelpDO selectByUserIdAndRecordId(Long userId, Long recordId) {
        return selectOne(new LambdaQueryWrapper<>(SalesBargainHelpDO.class)
                .eq(SalesBargainHelpDO::getUserId, userId)
                .eq(SalesBargainHelpDO::getRecordId, recordId));
    }

    default Map<Long, Integer> selectUserCountMapByActivityId(Collection<Long> activityIds) {
        // SQL count 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<SalesBargainHelpDO>()
                .select("COUNT(DISTINCT(user_id)) AS userCount, activity_id AS activityId")
                .in("activity_id", activityIds)
                .groupBy("activity_id"));
        if (CollUtil.isEmpty(result)) {
            return Collections.emptyMap();
        }
        // 转换数据
        return CollectionUtils.convertMap(result,
                record -> MapUtil.getLong(record, "activityId"),
                record -> MapUtil.getInt(record, "userCount" ));
    }

    default Map<Long, Integer> selectUserCountMapByRecordId(Collection<Long> recordIds) {
        // SQL count 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<SalesBargainHelpDO>()
                .select("COUNT(1) AS userCount, record_id AS recordId")
                .in("record_id", recordIds)
                .groupBy("record_id"));
        if (CollUtil.isEmpty(result)) {
            return Collections.emptyMap();
        }
        // 转换数据
        return CollectionUtils.convertMap(result,
                record -> MapUtil.getLong(record, "recordId"),
                record -> MapUtil.getInt(record, "userCount" ));
    }

    default PageResult<SalesBargainHelpDO> selectPage(SalesBargainHelpPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesBargainHelpDO>()
                .eqIfPresent(SalesBargainHelpDO::getRecordId, reqVO.getRecordId())
                .orderByDesc(SalesBargainHelpDO::getId));
    }

    default List<SalesBargainHelpDO> selectListByRecordId(Long recordId) {
        return selectList(new LambdaQueryWrapperX<SalesBargainHelpDO>()
                .eq(SalesBargainHelpDO::getRecordId, recordId));
    }

}
