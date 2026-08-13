package vip.appap.suxin.module.sales.dal.mysql;

import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesRewardActivityPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesRewardActivityDO;
import vip.appap.suxin.module.sales.enums.SalesProductScopeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 满减送活动 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface SalesRewardActivityMapper extends BaseMapperX<SalesRewardActivityDO> {

    default PageResult<SalesRewardActivityDO> selectPage(SalesRewardActivityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesRewardActivityDO>()
                .likeIfPresent(SalesRewardActivityDO::getName, reqVO.getName())
                .eqIfPresent(SalesRewardActivityDO::getStatus, reqVO.getStatus())
                .orderByDesc(SalesRewardActivityDO::getId));
    }

    default List<SalesRewardActivityDO> selectListBySpuIdAndStatusAndNow(Collection<Long> spuIds,
                                                                    Collection<Long> categoryIds,
                                                                    Integer status) {
        LocalDateTime now = LocalDateTime.now();
        Function<Collection<Long>, String> productScopeValuesFindInSetFunc = ids -> ids.stream()
                .map(id -> StrUtil.format("FIND_IN_SET({}, product_scope_values) ", id))
                .collect(Collectors.joining(" OR "));
        return selectList(new LambdaQueryWrapperX<SalesRewardActivityDO>()
                .eq(SalesRewardActivityDO::getStatus, status)
                .lt(SalesRewardActivityDO::getStartTime, now)
                .gt(SalesRewardActivityDO::getEndTime, now)
                .and(i -> i.eq(SalesRewardActivityDO::getProductScope, SalesProductScopeEnum.SPU.getScope())
                            .and(i1 -> i1.apply(productScopeValuesFindInSetFunc.apply(spuIds)))
                        .or(i1 -> i1.eq(SalesRewardActivityDO::getProductScope, SalesProductScopeEnum.ALL.getScope()))
                        .or(i1 -> i1.eq(SalesRewardActivityDO::getProductScope, SalesProductScopeEnum.CATEGORY.getScope())
                                .and(i2 -> i2.apply(productScopeValuesFindInSetFunc.apply(categoryIds)))))
                .orderByDesc(SalesRewardActivityDO::getId)
        );
    }

}
