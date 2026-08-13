package vip.appap.suxin.module.sales.dal.mysql;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 快递运费引用查询 Mapper
 *
 * 职责单一：只返回当前租户、未删除数据的存在性计数，供快递公司/运费模板删除保护使用。
 * 不映射 product / order / after-sale 的 DO，不执行任何写入。
 * 查询均显式携带 {@code tenant_id} 与 {@code deleted = 0}，不依赖外域 Mapper 或 Service。
 */
@Mapper
public interface SalesDeliveryReferenceMapper {

    /**
     * 统计引用指定运费模板的未删除商品数
     *
     * @param templateId 运费模板编号
     * @param tenantId   租户编号
     * @return 引用商品数
     */
    @Select("SELECT COUNT(1) FROM product_spu " +
            "WHERE delivery_template_id = #{templateId} " +
            "AND deleted = 0 " +
            "AND tenant_id = #{tenantId}")
    long selectCountProductByTemplateId(@Param("templateId") Long templateId,
                                        @Param("tenantId") Long tenantId);

    /**
     * 统计引用指定快递公司的未删除订单数
     *
     * @param expressId 快递公司编号
     * @param tenantId  租户编号
     * @return 引用订单数
     */
    @Select("SELECT COUNT(1) FROM sales_order " +
            "WHERE logistics_id = #{expressId} " +
            "AND deleted = 0 " +
            "AND tenant_id = #{tenantId}")
    long selectCountOrderByLogisticsId(@Param("expressId") Long expressId,
                                       @Param("tenantId") Long tenantId);

    /**
     * 统计引用指定快递公司的未删除售后数
     *
     * @param expressId 快递公司编号
     * @param tenantId  租户编号
     * @return 引用售后数
     */
    @Select("SELECT COUNT(1) FROM sales_after_sale " +
            "WHERE logistics_id = #{expressId} " +
            "AND deleted = 0 " +
            "AND tenant_id = #{tenantId}")
    long selectCountAfterSaleByLogisticsId(@Param("expressId") Long expressId,
                                           @Param("tenantId") Long tenantId);

}
