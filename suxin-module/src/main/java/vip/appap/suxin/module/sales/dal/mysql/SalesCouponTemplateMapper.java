package vip.appap.suxin.module.sales.dal.mysql;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCouponTemplatePageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCouponTemplateDO;
import vip.appap.suxin.module.sales.enums.SalesCouponTemplateValidityTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

/**
 * 优惠劵模板 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface SalesCouponTemplateMapper extends BaseMapperX<SalesCouponTemplateDO> {

    default PageResult<SalesCouponTemplateDO> selectPage(SalesCouponTemplatePageReqVO reqVO) {
        // 构建可领取的查询条件
        Consumer<LambdaQueryWrapper<SalesCouponTemplateDO>> canTakeConsumer = buildCanTakeQueryConsumer(reqVO.getCanTakeTypes());
        // 执行分页查询
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesCouponTemplateDO>()
                .likeIfPresent(SalesCouponTemplateDO::getName, reqVO.getName())
                .eqIfPresent(SalesCouponTemplateDO::getStatus, reqVO.getStatus())
                .eqIfPresent(SalesCouponTemplateDO::getDiscountType, reqVO.getDiscountType())
                .betweenIfPresent(SalesCouponTemplateDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(SalesCouponTemplateDO::getProductScope, reqVO.getProductScope())
                .and(reqVO.getProductScopeValue() != null, w -> w.apply("FIND_IN_SET({0}, product_scope_values)",
                        reqVO.getProductScopeValue()))
                .and(canTakeConsumer != null, canTakeConsumer)
                .orderByDesc(SalesCouponTemplateDO::getId));
    }

    default int updateTakeCount(Long id, Integer incrCount) {
        LambdaUpdateWrapper<SalesCouponTemplateDO> updateWrapper = new LambdaUpdateWrapper<SalesCouponTemplateDO>()
                .eq(SalesCouponTemplateDO::getId, id)
                .setSql("take_count = take_count + " + incrCount);
        // 增加已领取的数量（incrCount 为正数），需要考虑发放数量 totalCount 的限制
        if (incrCount > 0) {
            updateWrapper.and(i -> i.apply("take_count < total_count")
                    .or().eq(SalesCouponTemplateDO::getTotalCount, SalesCouponTemplateDO.TOTAL_COUNT_MAX));
        }
        return update(updateWrapper);
    }

    default List<SalesCouponTemplateDO> selectListByTakeType(Integer takeType) {
        return selectList(SalesCouponTemplateDO::getTakeType, takeType, SalesCouponTemplateDO::getStatus, CommonStatusEnum.ENABLE.getStatus());
    }

    default List<SalesCouponTemplateDO> selectList(List<Integer> canTakeTypes, Integer productScope, Long productScopeValue, Integer count) {
        // 构建可领取的查询条件
        Consumer<LambdaQueryWrapper<SalesCouponTemplateDO>> canTakeConsumer = buildCanTakeQueryConsumer(canTakeTypes);
        return selectList(new LambdaQueryWrapperX<SalesCouponTemplateDO>()
                .eqIfPresent(SalesCouponTemplateDO::getProductScope, productScope)
                .and(productScopeValue != null, w -> w.apply("FIND_IN_SET({0}, product_scope_values)",
                        productScopeValue))
                .and(canTakeConsumer != null, canTakeConsumer)
                .last(" LIMIT " + count)
                .orderByDesc(SalesCouponTemplateDO::getId));
    }

    static Consumer<LambdaQueryWrapper<SalesCouponTemplateDO>> buildCanTakeQueryConsumer(List<Integer> canTakeTypes) {
        Consumer<LambdaQueryWrapper<SalesCouponTemplateDO>> canTakeConsumer = null;
        if (CollUtil.isNotEmpty(canTakeTypes)) {
            canTakeConsumer = w ->
                    w.eq(SalesCouponTemplateDO::getStatus, CommonStatusEnum.ENABLE.getStatus()) // 1. 状态为可用的
                            .in(SalesCouponTemplateDO::getTakeType, canTakeTypes) // 2. 领取方式一致
                            .and(ww -> ww.gt(SalesCouponTemplateDO::getValidEndTime, LocalDateTime.now())  // 3.1 未过期
                                    .or().eq(SalesCouponTemplateDO::getValidityType, SalesCouponTemplateValidityTypeEnum.TERM.getType())) // 3.2 领取之后
                            .apply(" (take_count < total_count OR total_count = " + SalesCouponTemplateDO.TOTAL_COUNT_MAX + ")"); // 4. 剩余数量大于 0，或者无限领取
        }
        return canTakeConsumer;
    }

}
