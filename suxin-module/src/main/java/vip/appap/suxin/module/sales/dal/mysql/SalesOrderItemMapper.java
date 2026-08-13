package vip.appap.suxin.module.sales.dal.mysql;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.framework.mybatis.core.query.MPJLambdaWrapperX;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSpuDO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesOrderActivityPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesOrderActivityRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface SalesOrderItemMapper extends BaseMapperX<SalesOrderItemDO> {

    default int updateAfterSaleStatus(Long id, Integer oldAfterSaleStatus, Integer newAfterSaleStatus,
                                      Long afterSaleId) {
        return update(new SalesOrderItemDO().setAfterSaleStatus(newAfterSaleStatus).setAfterSaleId(afterSaleId),
                new LambdaUpdateWrapper<>(new SalesOrderItemDO().setId(id).setAfterSaleStatus(oldAfterSaleStatus)));
    }

    default List<SalesOrderItemDO> selectListByOrderId(Long orderId) {
        return selectList(SalesOrderItemDO::getOrderId, orderId);
    }

    default List<SalesOrderItemDO> selectListByOrderId(Collection<Long> orderIds) {
        return selectList(SalesOrderItemDO::getOrderId, orderIds);
    }

    default SalesOrderItemDO selectByIdAndUserId(Long orderItemId, Long loginUserId) {
        return selectOne(new LambdaQueryWrapperX<SalesOrderItemDO>()
                .eq(SalesOrderItemDO::getId, orderItemId)
                .eq(SalesOrderItemDO::getUserId, loginUserId));
    }

    default List<SalesOrderItemDO> selectListByOrderIdAndCommentStatus(Long orderId, Boolean commentStatus) {
        return selectList(new LambdaQueryWrapperX<SalesOrderItemDO>()
                .eq(SalesOrderItemDO::getOrderId, orderId)
                .eq(SalesOrderItemDO::getCommentStatus, commentStatus));
    }

    default int selectProductSumByOrderId(@Param("orderIds") Set<Long> orderIds) {
        // SQL sum 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<SalesOrderItemDO>()
                .select("SUM(count) AS sumCount")
                .in("order_id", orderIds)); // 只计算选中的
        // 获得数量
        return CollUtil.getFirst(result) != null ? MapUtil.getInt(result.get(0), "sumCount") : 0;
    }

    @Select("""
            <script>
            SELECT oi.id
            FROM sales_order_item oi
            INNER JOIN sales_order o ON o.id = oi.order_id
            WHERE o.user_id = #{userId}
              AND o.pay_status = 1
              AND o.status IN
              <foreach collection="paidStatuses" item="status" open="(" separator="," close=")">
                  #{status}
              </foreach>
              AND oi.after_sale_status = #{afterSaleStatus}
              AND o.deleted = 0
              AND oi.deleted = 0
              <if test="spuId != null">
                  AND oi.spu_id = #{spuId}
              </if>
              <if test="skuId != null">
                  AND oi.sku_id = #{skuId}
              </if>
            LIMIT 1
            </script>
            """)
    Long selectOwnedPaidOrderItemId(@Param("userId") Long userId,
                                    @Param("spuId") Long spuId,
                                    @Param("skuId") Long skuId,
                                    @Param("paidStatuses") Collection<Integer> paidStatuses,
                                    @Param("afterSaleStatus") Integer afterSaleStatus);

    default PageResult<AppSalesOrderActivityRespVO> selectMyActivityPage(AppSalesOrderActivityPageReqVO reqVO,
                                                                         Long userId, Long categorySales,
                                                                         Collection<Integer> paidStatuses,
                                                                         Integer afterSaleStatus) {
        MPJLambdaWrapperX<SalesOrderItemDO> query = new MPJLambdaWrapperX<>();
        query.selectAs(SalesOrderItemDO::getId, AppSalesOrderActivityRespVO::getOrderItemId)
                .selectAs(SalesOrderItemDO::getOrderId, AppSalesOrderActivityRespVO::getOrderId)
                .selectAs(SalesOrderDO::getStatus, AppSalesOrderActivityRespVO::getOrderStatus)
                .selectAs(SalesOrderDO::getPayTime, AppSalesOrderActivityRespVO::getPayTime)
                .selectAs(SalesOrderItemDO::getSpuId, AppSalesOrderActivityRespVO::getSpuId)
                .selectAs(SalesOrderItemDO::getSkuId, AppSalesOrderActivityRespVO::getSkuId)
                .selectAs(SalesOrderItemDO::getSpuName, AppSalesOrderActivityRespVO::getSpuName)
                .selectAs(SalesOrderItemDO::getPicUrl, AppSalesOrderActivityRespVO::getPicUrl)
                .selectAs(SalesOrderItemDO::getCount, AppSalesOrderActivityRespVO::getCount)
                .selectAs(SalesOrderItemDO::getPayPrice, AppSalesOrderActivityRespVO::getPayPrice)
                .innerJoin(SalesOrderDO.class, SalesOrderDO::getId, SalesOrderItemDO::getOrderId)
                .innerJoin(ProductSpuDO.class, ProductSpuDO::getId, SalesOrderItemDO::getSpuId)
                .eq(SalesOrderItemDO::getUserId, userId)
                .eq(SalesOrderDO::getPayStatus, true)
                .in(SalesOrderDO::getStatus, paidStatuses)
                .eq(SalesOrderItemDO::getAfterSaleStatus, afterSaleStatus)
                .eq(ProductSpuDO::getCategorySales, categorySales)
                .eq(SalesOrderDO::getDeleted, false)
                .eq(SalesOrderItemDO::getDeleted, false)
                .eq(ProductSpuDO::getDeleted, false)
                .orderByDesc(SalesOrderDO::getPayTime)
                .orderByDesc(SalesOrderItemDO::getId);
        return selectJoinPage(reqVO, AppSalesOrderActivityRespVO.class, query);
    }

}
