package vip.appap.suxin.module.sales.dal.mysql;

import cn.hutool.core.bean.BeanUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBrokerageRecordPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageUserRankByPriceRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageRecordDO;
import vip.appap.suxin.module.sales.service.bo.SalesBrokerageUserSummaryRespBO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.yulichang.toolkit.MPJWrappers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 佣金记录 Mapper
 *
 * @author owen
 */
@Mapper
public interface SalesBrokerageRecordMapper extends BaseMapperX<SalesBrokerageRecordDO> {

    default PageResult<SalesBrokerageRecordDO> selectPage(SalesBrokerageRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesBrokerageRecordDO>()
                .eqIfPresent(SalesBrokerageRecordDO::getUserId, reqVO.getUserId())
                .eqIfPresent(SalesBrokerageRecordDO::getBizType, reqVO.getBizType())
                .eqIfPresent(SalesBrokerageRecordDO::getStatus, reqVO.getStatus())
                .eqIfPresent(SalesBrokerageRecordDO::getSourceUserLevel, reqVO.getSourceUserLevel())
                .betweenIfPresent(SalesBrokerageRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SalesBrokerageRecordDO::getId));
    }

    default List<SalesBrokerageRecordDO> selectListByStatusAndUnfreezeTimeLt(Integer status, LocalDateTime unfreezeTime) {
        return selectList(new LambdaQueryWrapper<SalesBrokerageRecordDO>()
                .eq(SalesBrokerageRecordDO::getStatus, status)
                .lt(SalesBrokerageRecordDO::getUnfreezeTime, unfreezeTime));
    }

    default int updateByIdAndStatus(Long id, Integer status, SalesBrokerageRecordDO updateObj) {
        return update(updateObj, new LambdaQueryWrapper<SalesBrokerageRecordDO>()
                .eq(SalesBrokerageRecordDO::getId, id)
                .eq(SalesBrokerageRecordDO::getStatus, status));
    }

    default List<SalesBrokerageRecordDO> selectListByBizTypeAndBizId(Integer bizType, String bizId) {
        return selectList(SalesBrokerageRecordDO::getBizType, bizType,
                SalesBrokerageRecordDO::getBizId, bizId);
    }

    default List<SalesBrokerageUserSummaryRespBO> selectCountAndSumPriceByUserIdInAndBizTypeAndStatus(Collection<Long> userIds,
                                                                                                 Integer bizType,
                                                                                                 Integer status) {
        List<Map<String, Object>> list = selectMaps(MPJWrappers.lambdaJoin(SalesBrokerageRecordDO.class)
                .select(SalesBrokerageRecordDO::getUserId)
                .selectCount(SalesBrokerageRecordDO::getId, SalesBrokerageUserSummaryRespBO::getCount)
                .selectSum(SalesBrokerageRecordDO::getPrice)
                .in(SalesBrokerageRecordDO::getUserId, userIds)
                .eq(SalesBrokerageRecordDO::getBizType, bizType)
                .eq(SalesBrokerageRecordDO::getStatus, status)
                .groupBy(SalesBrokerageRecordDO::getUserId)); // 按照 userId 聚合
        return BeanUtil.copyToList(list, SalesBrokerageUserSummaryRespBO.class);
        // selectJoinList有BUG，会与租户插件冲突：解析SQL时，发生异常 https://gitee.com/best_handsome/mybatis-plus-join/issues/I84GYW
//            return selectJoinList(UserBrokerageSummaryBO.class, MPJWrappers.lambdaJoin(SalesBrokerageRecordDO.class)
//                    .select(SalesBrokerageRecordDO::getUserId)
//                    .selectCount(SalesBrokerageRecordDO::getId, UserBrokerageSummaryBO::getCount)
//                    .selectSum(SalesBrokerageRecordDO::getPrice)
//                    .in(SalesBrokerageRecordDO::getUserId, userIds)
//                    .eq(SalesBrokerageRecordDO::getBizType, bizType)
//                    .eq(SalesBrokerageRecordDO::getStatus, status)
//                    .groupBy(SalesBrokerageRecordDO::getUserId));
    }

    @Select("SELECT SUM(price) FROM sales_brokerage_record " +
            "WHERE user_id = #{userId} AND biz_type = #{bizType} AND status = #{status} " +
            "AND unfreeze_time BETWEEN #{beginTime} AND #{endTime} AND deleted = FALSE")
    Integer selectSummaryPriceByUserIdAndBizTypeAndCreateTimeBetween(@Param("userId") Long userId,
                                                                     @Param("bizType") Integer bizType,
                                                                     @Param("status") Integer status,
                                                                     @Param("beginTime") LocalDateTime beginTime,
                                                                     @Param("endTime") LocalDateTime endTime);

    // TODO @芋艿：收敛掉 @Select 注解操作，统一成 MyBatis-Plus 的方式，或者 xml
    @Select("SELECT user_id AS id, SUM(price) AS brokeragePrice FROM sales_brokerage_record " +
            "WHERE biz_type = #{bizType} AND status = #{status} AND deleted = FALSE " +
            "AND unfreeze_time BETWEEN #{beginTime} AND #{endTime} " +
            "GROUP BY user_id " +
            "ORDER BY brokeragePrice DESC")
    IPage<AppSalesBrokerageUserRankByPriceRespVO> selectSummaryPricePageGroupByUserId(IPage<?> page,
                                                                                 @Param("bizType") Integer bizType,
                                                                                 @Param("status") Integer status,
                                                                                 @Param("beginTime") LocalDateTime beginTime,
                                                                                 @Param("endTime") LocalDateTime endTime);

    @Select("SELECT COUNT(1) FROM sales_brokerage_record " +
            "WHERE biz_type = #{bizType} AND status = #{status} AND deleted = FALSE " +
            "AND unfreeze_time BETWEEN #{beginTime} AND #{endTime} " +
            "GROUP BY user_id HAVING SUM(price) > #{brokeragePrice}")
    Integer selectCountByPriceGt(@Param("brokeragePrice") Integer brokeragePrice,
                                 @Param("bizType") Integer bizType,
                                 @Param("status") Integer status,
                                 @Param("beginTime") LocalDateTime beginTime,
                                 @Param("endTime") LocalDateTime endTime);

}
