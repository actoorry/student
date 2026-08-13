package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiscountProductDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 限时折扣商城 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface SalesDiscountProductMapper extends BaseMapperX<SalesDiscountProductDO> {

    default List<SalesDiscountProductDO> selectListByActivityId(Long activityId) {
        return selectList(SalesDiscountProductDO::getActivityId, activityId);
    }

    default List<SalesDiscountProductDO> selectListByActivityId(Collection<Long> activityIds) {
        return selectList(SalesDiscountProductDO::getActivityId, activityIds);
    }

    default void updateByActivityId(SalesDiscountProductDO discountProductDO) {
        update(discountProductDO, new LambdaUpdateWrapper<SalesDiscountProductDO>()
                .eq(SalesDiscountProductDO::getActivityId, discountProductDO.getActivityId()));
    }

    default void deleteByActivityId(Long activityId) {
        delete(SalesDiscountProductDO::getActivityId, activityId);
    }

    default List<SalesDiscountProductDO> selectListBySkuIdsAndStatusAndNow(Collection<Long> skuIds, Integer status) {
        LocalDateTime now = LocalDateTime.now();
        return selectList(new LambdaQueryWrapperX<SalesDiscountProductDO>()
                .in(SalesDiscountProductDO::getSkuId, skuIds)
                .eq(SalesDiscountProductDO::getActivityStatus,status)
                .lt(SalesDiscountProductDO::getActivityStartTime, now)
                .gt(SalesDiscountProductDO::getActivityEndTime, now));
    }

}
