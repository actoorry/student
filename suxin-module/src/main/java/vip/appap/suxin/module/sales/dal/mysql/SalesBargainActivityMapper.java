package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBargainActivityPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainActivityDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 砍价活动 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface SalesBargainActivityMapper extends BaseMapperX<SalesBargainActivityDO> {

    default PageResult<SalesBargainActivityDO> selectPage(SalesBargainActivityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesBargainActivityDO>()
                .likeIfPresent(SalesBargainActivityDO::getName, reqVO.getName())
                .eqIfPresent(SalesBargainActivityDO::getStatus, reqVO.getStatus())
                .orderByDesc(SalesBargainActivityDO::getId));
    }

    default List<SalesBargainActivityDO> selectListByStatus(Integer status) {
        return selectList(SalesBargainActivityDO::getStatus, status);
    }

    /**
     * 更新活动库存
     *
     * @param id    活动编号
     * @param count 扣减的库存数量
     * @return 影响的行数
     */
    default int updateStock(Long id, int count) {
        // 情况一：增加库存
        if (count > 0) {
            return update(null, new LambdaUpdateWrapper<SalesBargainActivityDO>()
                    .eq(SalesBargainActivityDO::getId, id)
                    .setSql("stock = stock + " + count));
        }
        // 情况二：扣减库存
        count = -count; // 取正
        return update(null, new LambdaUpdateWrapper<SalesBargainActivityDO>()
                .eq(SalesBargainActivityDO::getId, id)
                .ge(SalesBargainActivityDO::getStock, count)
                .setSql("stock = stock - " + count));
    }

    /**
     * 查询处在 now 日期时间且是 status 状态的活动分页
     *
     * @param pageReqVO 分页参数
     * @param status    状态
     * @param now       当前日期时间
     * @return 活动分页
     */
    default PageResult<SalesBargainActivityDO> selectPage(PageParam pageReqVO, Integer status, LocalDateTime now) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<SalesBargainActivityDO>()
                .eq(SalesBargainActivityDO::getStatus, status)
                .le(SalesBargainActivityDO::getStartTime, now)
                .ge(SalesBargainActivityDO::getEndTime, now));
    }

    /**
     * 查询处在 now 日期时间且是 status 状态的活动分页
     *
     * @param status 状态
     * @param now    当前日期时间
     * @return 活动分页
     */
    default List<SalesBargainActivityDO> selectList(Integer count, Integer status, LocalDateTime now) {
        return selectList(new LambdaQueryWrapperX<SalesBargainActivityDO>()
                .eq(SalesBargainActivityDO::getStatus, status)
                .le(SalesBargainActivityDO::getStartTime, now)
                .ge(SalesBargainActivityDO::getEndTime, now)
                .last("LIMIT " + count));
    }

    default SalesBargainActivityDO selectBySpuIdAndStatusAndNow(Long spuId, Integer status) {
        LocalDateTime now = LocalDateTime.now();
        return selectOne(new LambdaQueryWrapperX<SalesBargainActivityDO>()
                .eq(SalesBargainActivityDO::getSpuId, spuId)
                .eq(SalesBargainActivityDO::getStatus, status)
                .lt(SalesBargainActivityDO::getStartTime, now)
                .gt(SalesBargainActivityDO::getEndTime, now)); // 开始时间 < now < 结束时间，也就是说获取指定时间段的活动
    }

}
