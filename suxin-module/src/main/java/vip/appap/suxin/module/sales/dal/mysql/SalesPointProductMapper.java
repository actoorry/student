package vip.appap.suxin.module.sales.dal.mysql;

import cn.hutool.core.lang.Assert;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.sales.dal.dataobject.SalesPointProductDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 积分商城商品 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface SalesPointProductMapper extends BaseMapperX<SalesPointProductDO> {

    default List<SalesPointProductDO> selectListByActivityId(Collection<Long> activityIds) {
        return selectList(SalesPointProductDO::getActivityId, activityIds);
    }

    default List<SalesPointProductDO> selectListByActivityId(Long activityId) {
        return selectList(SalesPointProductDO::getActivityId, activityId);
    }

    default void updateByActivityId(SalesPointProductDO pointProductDO) {
        update(pointProductDO, new LambdaUpdateWrapper<SalesPointProductDO>()
                .eq(SalesPointProductDO::getActivityId, pointProductDO.getActivityId()));
    }

    default SalesPointProductDO selectListByActivityIdAndSkuId(Long activityId, Long skuId) {
        return selectOne(SalesPointProductDO::getActivityId, activityId,
                SalesPointProductDO::getSkuId, skuId);
    }

    /**
     * 更新活动库存（减少）
     *
     * @param id    活动编号
     * @param count 扣减的库存数量(减少库存)
     * @return 影响的行数
     */
    default int updateStockDecr(Long id, int count) {
        Assert.isTrue(count > 0);
        return update(null, new LambdaUpdateWrapper<SalesPointProductDO>()
                .eq(SalesPointProductDO::getId, id)
                .ge(SalesPointProductDO::getStock, count)
                .setSql("stock = stock - " + count));
    }

    /**
     * 更新活动库存（增加）
     *
     * @param id    活动编号
     * @param count 需要增加的库存（增加库存）
     * @return 影响的行数
     */
    default int updateStockIncr(Long id, int count) {
        Assert.isTrue(count > 0);
        return update(null, new LambdaUpdateWrapper<SalesPointProductDO>()
                .eq(SalesPointProductDO::getId, id)
                .setSql("stock = stock + " + count));
    }
}