package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationActivityPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationActivityDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 拼团活动 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface SalesCombinationActivityMapper extends BaseMapperX<SalesCombinationActivityDO> {

    default PageResult<SalesCombinationActivityDO> selectPage(SalesCombinationActivityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesCombinationActivityDO>()
                .likeIfPresent(SalesCombinationActivityDO::getName, reqVO.getName())
                .eqIfPresent(SalesCombinationActivityDO::getStatus, reqVO.getStatus())
                .orderByDesc(SalesCombinationActivityDO::getId));
    }

    default List<SalesCombinationActivityDO> selectListByStatus(Integer status) {
        return selectList(SalesCombinationActivityDO::getStatus, status);
    }

    default PageResult<SalesCombinationActivityDO> selectPage(PageParam pageParam, Integer status) {
        return selectPage(pageParam, new LambdaQueryWrapperX<SalesCombinationActivityDO>()
                .eq(SalesCombinationActivityDO::getStatus, status));
    }

    default SalesCombinationActivityDO selectBySpuIdAndStatusAndNow(Long spuId, Integer status) {
        LocalDateTime now = LocalDateTime.now();
        return selectOne(new LambdaQueryWrapperX<SalesCombinationActivityDO>()
                .eq(SalesCombinationActivityDO::getSpuId, spuId)
                .eq(SalesCombinationActivityDO::getStatus, status)
                .lt(SalesCombinationActivityDO::getStartTime, now)
                .gt(SalesCombinationActivityDO::getEndTime, now)); // 开始时间 < now < 结束时间，也就是说获取指定时间段的活动
    }

}