package vip.appap.suxin.module.sales.dal.mysql;

import cn.hutool.core.lang.Assert;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesPointActivityPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesPointActivityDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分商城活动 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface SalesPointActivityMapper extends BaseMapperX<SalesPointActivityDO> {

    default PageResult<SalesPointActivityDO> selectPage(SalesPointActivityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesPointActivityDO>()
                .eqIfPresent(SalesPointActivityDO::getStatus, reqVO.getStatus())
                .orderByDesc(SalesPointActivityDO::getSort));
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
        return update(null, new LambdaUpdateWrapper<SalesPointActivityDO>()
                .eq(SalesPointActivityDO::getId, id)
                .ge(SalesPointActivityDO::getStock, count)
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
        return update(null, new LambdaUpdateWrapper<SalesPointActivityDO>()
                .eq(SalesPointActivityDO::getId, id)
                .setSql("stock = stock + " + count));
    }

}