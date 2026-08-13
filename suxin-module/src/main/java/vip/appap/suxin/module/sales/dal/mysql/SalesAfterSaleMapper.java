package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesAfterSalePageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesAfterSalePageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesAfterSaleDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

@Mapper
public interface SalesAfterSaleMapper extends BaseMapperX<SalesAfterSaleDO> {

    default PageResult<SalesAfterSaleDO> selectPage(SalesAfterSalePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesAfterSaleDO>()
                .eqIfPresent(SalesAfterSaleDO::getUserId, reqVO.getUserId())
                .likeIfPresent(SalesAfterSaleDO::getNo, reqVO.getNo())
                .eqIfPresent(SalesAfterSaleDO::getStatus, reqVO.getStatus())
                .eqIfPresent(SalesAfterSaleDO::getType, reqVO.getType())
                .eqIfPresent(SalesAfterSaleDO::getWay, reqVO.getWay())
                .likeIfPresent(SalesAfterSaleDO::getOrderNo, reqVO.getOrderNo())
                .likeIfPresent(SalesAfterSaleDO::getSpuName, reqVO.getSpuName())
                .betweenIfPresent(SalesAfterSaleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SalesAfterSaleDO::getId));
    }

    default PageResult<SalesAfterSaleDO> selectPage(Long userId, AppSalesAfterSalePageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<SalesAfterSaleDO>()
                .eq(SalesAfterSaleDO::getUserId, userId)
                .inIfPresent(SalesAfterSaleDO::getStatus, pageReqVO.getStatuses())
                .orderByDesc(SalesAfterSaleDO::getId));
    }

    default int updateByIdAndStatus(Long id, Integer status, SalesAfterSaleDO update) {
        return update(update, new LambdaUpdateWrapper<SalesAfterSaleDO>()
                .eq(SalesAfterSaleDO::getId, id).eq(SalesAfterSaleDO::getStatus, status));
    }

    default SalesAfterSaleDO selectByIdAndUserId(Long id, Long userId) {
        return selectOne(SalesAfterSaleDO::getId, id,
                SalesAfterSaleDO::getUserId, userId);
    }

    default Long selectCountByUserIdAndStatus(Long userId, Collection<Integer> statuses) {
        return selectCount(new LambdaQueryWrapperX<SalesAfterSaleDO>()
                .eq(SalesAfterSaleDO::getUserId, userId)
                .in(SalesAfterSaleDO::getStatus, statuses));
    }

}
