package vip.appap.suxin.module.sales.dal.mysql;

import cn.hutool.core.map.MapUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCouponPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCouponDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.yulichang.toolkit.MPJWrappers;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 优惠劵 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface SalesCouponMapper extends BaseMapperX<SalesCouponDO> {

    default PageResult<SalesCouponDO> selectPage(SalesCouponPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesCouponDO>()
                .eqIfPresent(SalesCouponDO::getTemplateId, reqVO.getTemplateId())
                .eqIfPresent(SalesCouponDO::getStatus, reqVO.getStatus())
                .inIfPresent(SalesCouponDO::getUserId, reqVO.getUserIds())
                .betweenIfPresent(SalesCouponDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SalesCouponDO::getId));
    }

    default List<SalesCouponDO> selectListByUserIdAndStatus(Long userId, Integer status) {
        return selectList(new LambdaQueryWrapperX<SalesCouponDO>()
                .eq(SalesCouponDO::getUserId, userId).eq(SalesCouponDO::getStatus, status));
    }

    default SalesCouponDO selectByIdAndUserId(Long id, Long userId) {
        return selectOne(new LambdaQueryWrapperX<SalesCouponDO>()
                .eq(SalesCouponDO::getId, id).eq(SalesCouponDO::getUserId, userId));
    }

    default int delete(Long id, Collection<Integer> whereStatuses) {
        return update(null, new LambdaUpdateWrapper<SalesCouponDO>()
                .eq(SalesCouponDO::getId, id).in(SalesCouponDO::getStatus, whereStatuses)
                .set(SalesCouponDO::getDeleted, 1));
    }

    default int updateByIdAndStatus(Long id, Integer status, SalesCouponDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<SalesCouponDO>()
                .eq(SalesCouponDO::getId, id).eq(SalesCouponDO::getStatus, status));
    }

    default Long selectCountByUserIdAndStatus(Long userId, Integer status) {
        return selectCount(new LambdaQueryWrapperX<SalesCouponDO>()
                .eq(SalesCouponDO::getUserId, userId)
                .eq(SalesCouponDO::getStatus, status));
    }

    default List<SalesCouponDO> selectListByTemplateIdAndUserId(Long templateId, Collection<Long> userIds) {
        return selectList(new LambdaQueryWrapperX<SalesCouponDO>()
                .eq(SalesCouponDO::getTemplateId, templateId)
                .in(SalesCouponDO::getUserId, userIds)
        );
    }

    default Map<Long, Integer> selectCountByUserIdAndTemplateIdIn(Long userId, Collection<Long> templateIds) {
        String templateIdAlias = "templateId";
        String countAlias = "count";
        List<Map<String, Object>> list = selectMaps(MPJWrappers.lambdaJoin(SalesCouponDO.class)
                .selectAs(SalesCouponDO::getTemplateId, templateIdAlias)
                .selectCount(SalesCouponDO::getId, countAlias)
                .eq(SalesCouponDO::getUserId, userId)
                .in(SalesCouponDO::getTemplateId, templateIds)
                .groupBy(SalesCouponDO::getTemplateId));
        return convertMap(list, map -> MapUtil.getLong(map, templateIdAlias), map -> MapUtil.getInt(map, countAlias));
    }

    default List<SalesCouponDO> selectListByStatusAndValidEndTimeLe(Integer status, LocalDateTime validEndTime) {
        return selectList(new LambdaQueryWrapperX<SalesCouponDO>()
                .eq(SalesCouponDO::getStatus, status)
                .le(SalesCouponDO::getValidEndTime, validEndTime)
        );
    }

}
