package vip.appap.suxin.module.sales.dal.mysql;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.partner.dal.dataobject.SalesCartDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface SalesCartMapper extends BaseMapperX<SalesCartDO> {

    default SalesCartDO selectByUserIdAndSkuId(Long userId, Long skuId) {
        return selectOne(SalesCartDO::getUserId, userId, SalesCartDO::getSkuId, skuId);
    }

    default Integer selectSelectedCountByUserId(Long userId) {
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<SalesCartDO>()
                .select("SUM(count) AS sumCount")
                .eq("user_id", userId)
                .eq("selected", true));
        if (CollUtil.getFirst(result) == null) {
            return 0;
        }
        Integer count = MapUtil.getInt(result.get(0), "sumCount");
        return count != null ? count : 0;
    }

    default SalesCartDO selectByIdAndUserId(Long id, Long userId) {
        return selectOne(SalesCartDO::getId, id, SalesCartDO::getUserId, userId);
    }

    default List<SalesCartDO> selectListByIdsAndUserId(Collection<Long> ids, Long userId) {
        return selectList(new LambdaQueryWrapper<SalesCartDO>()
                .in(SalesCartDO::getId, ids)
                .eq(SalesCartDO::getUserId, userId));
    }

    default List<SalesCartDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapper<SalesCartDO>()
                .eq(SalesCartDO::getUserId, userId));
    }

    default List<SalesCartDO> selectListByUserIdAndIds(Long userId, Set<Long> ids) {
        return selectList(new LambdaQueryWrapper<SalesCartDO>()
                .eq(SalesCartDO::getUserId, userId)
                .in(SalesCartDO::getId, ids));
    }

    default void updateByIdsAndUserId(Collection<Long> ids, Long userId, SalesCartDO updateObject) {
        update(updateObject, new LambdaQueryWrapper<SalesCartDO>()
                .in(SalesCartDO::getId, ids)
                .eq(SalesCartDO::getUserId, userId));
    }

}
