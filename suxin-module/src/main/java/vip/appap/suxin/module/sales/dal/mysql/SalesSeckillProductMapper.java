package vip.appap.suxin.module.sales.dal.mysql;

import cn.hutool.core.lang.Assert;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.sales.dal.dataobject.SalesSeckillProductDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 秒杀活动商品 Mapper
 *
 * @author halfninety
 */
@Mapper
public interface SalesSeckillProductMapper extends BaseMapperX<SalesSeckillProductDO> {

    default List<SalesSeckillProductDO> selectListByActivityId(Long activityId) {
        return selectList(SalesSeckillProductDO::getActivityId, activityId);
    }

    default SalesSeckillProductDO selectByActivityIdAndSkuId(Long activityId, Long skuId) {
        return selectOne(SalesSeckillProductDO::getActivityId, activityId,
                SalesSeckillProductDO::getSkuId, skuId);
    }

    default List<SalesSeckillProductDO> selectListByActivityId(Collection<Long> ids) {
        return selectList(SalesSeckillProductDO::getActivityId, ids);
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
        return update(null, new LambdaUpdateWrapper<SalesSeckillProductDO>()
                .eq(SalesSeckillProductDO::getId, id)
                .ge(SalesSeckillProductDO::getStock, count)
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
        return update(null, new LambdaUpdateWrapper<SalesSeckillProductDO>()
                .eq(SalesSeckillProductDO::getId, id)
                .setSql("stock = stock + " + count));
    }

}
