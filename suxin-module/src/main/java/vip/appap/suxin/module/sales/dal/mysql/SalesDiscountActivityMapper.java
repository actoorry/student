package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiscountActivityPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiscountActivityDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 限时折扣活动 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface SalesDiscountActivityMapper extends BaseMapperX<SalesDiscountActivityDO> {

    default PageResult<SalesDiscountActivityDO> selectPage(SalesDiscountActivityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesDiscountActivityDO>()
                .likeIfPresent(SalesDiscountActivityDO::getName, reqVO.getName())
                .eqIfPresent(SalesDiscountActivityDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SalesDiscountActivityDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SalesDiscountActivityDO::getId));
    }

    /**
     * 获取指定活动编号的活动列表且
     * 开始时间和结束时间小于给定时间 dateTime 的活动列表
     *
     * @param ids      活动编号
     * @param dateTime 指定日期
     * @return 活动列表
     */
    default List<SalesDiscountActivityDO> selectListByIdsAndDateTimeLt(Collection<Long> ids, LocalDateTime dateTime) {
        return selectList(new LambdaQueryWrapperX<SalesDiscountActivityDO>()
                .in(SalesDiscountActivityDO::getId, ids)
                .lt(SalesDiscountActivityDO::getStartTime, dateTime)
                .gt(SalesDiscountActivityDO::getEndTime, dateTime)// 开始时间 < 指定时间 < 结束时间，也就是说获取指定时间段的活动
                .orderByDesc(SalesDiscountActivityDO::getCreateTime));
    }

}
