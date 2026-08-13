package vip.appap.suxin.module.sales.dal.mysql;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesSeckillActivityPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesSeckillActivityPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesSeckillActivityDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀活动 Mapper
 *
 * @author halfninety
 */
@Mapper
public interface SalesSeckillActivityMapper extends BaseMapperX<SalesSeckillActivityDO> {

    default PageResult<SalesSeckillActivityDO> selectPage(SalesSeckillActivityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesSeckillActivityDO>()
                .likeIfPresent(SalesSeckillActivityDO::getName, reqVO.getName())
                .eqIfPresent(SalesSeckillActivityDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SalesSeckillActivityDO::getCreateTime, reqVO.getCreateTime())
                .apply(ObjectUtil.isNotNull(reqVO.getConfigId()), "FIND_IN_SET(" + reqVO.getConfigId() + ", config_ids) > 0")
                .orderByDesc(SalesSeckillActivityDO::getId));
    }

    default List<SalesSeckillActivityDO> selectListBySpuIdAndStatus(Long spuId, Integer status) {
        return selectList(SalesSeckillActivityDO::getSpuId, spuId,
                SalesSeckillActivityDO::getStatus, status);
    }

    /**
     * 更新活动库存(减少)
     *
     * @param id    活动编号
     * @param count 扣减的库存数量(正数)
     * @return 影响的行数
     */
    default int updateStockDecr(Long id, int count) {
        Assert.isTrue(count > 0);
        return update(null, new LambdaUpdateWrapper<SalesSeckillActivityDO>()
                .eq(SalesSeckillActivityDO::getId, id)
                .ge(SalesSeckillActivityDO::getStock, count)
                .setSql("stock = stock - " + count));
    }

    /**
     * 更新活动库存（增加）
     *
     * @param id    活动编号
     * @param count 增加的库存数量(正数)
     * @return 影响的行数
     */
    default int updateStockIncr(Long id, int count) {
        Assert.isTrue(count > 0);
        return update(null, new LambdaUpdateWrapper<SalesSeckillActivityDO>()
                .eq(SalesSeckillActivityDO::getId, id)
                .setSql("stock = stock + " + count));
    }

    default PageResult<SalesSeckillActivityDO> selectPage(AppSalesSeckillActivityPageReqVO pageReqVO, Integer status, LocalDateTime dateTime) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<SalesSeckillActivityDO>()
                .eqIfPresent(SalesSeckillActivityDO::getStatus, status)
                .lt(SalesSeckillActivityDO::getStartTime, dateTime)
                .gt(SalesSeckillActivityDO::getEndTime, dateTime)// 开始时间 < 指定时间 < 结束时间，也就是说获取指定时间段的活动
                .apply(ObjectUtil.isNotNull(pageReqVO.getConfigId()), "FIND_IN_SET(" + pageReqVO.getConfigId() + ",config_ids) > 0"));
    }

    default SalesSeckillActivityDO selectBySpuIdAndStatusAndNow(Long spuId, Integer status) {
        LocalDateTime now = LocalDateTime.now();
        return selectOne(new LambdaQueryWrapperX<SalesSeckillActivityDO>()
                .eq(SalesSeckillActivityDO::getSpuId, spuId)
                .eq(SalesSeckillActivityDO::getStatus, status)
                .lt(SalesSeckillActivityDO::getStartTime, now)
                .gt(SalesSeckillActivityDO::getEndTime, now)); // 开始时间 < now < 结束时间，也就是说获取指定时间段的活动
    }

}
