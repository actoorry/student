package vip.appap.suxin.module.wms.dal.mysql.stock;

import java.util.*;

import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.wms.dal.dataobject.stock.WmsStockDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import vip.appap.suxin.module.wms.controller.admin.stock.vo.*;

import java.math.BigDecimal;

/**
 * 库存快照 Mapper
 *
 * @author admin
 */
@Mapper
public interface WmsStockMapper extends BaseMapperX<WmsStockDO> {

    default PageResult<WmsStockDO> selectPage(WmsStockPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsStockDO>()
                .eqIfPresent(WmsStockDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(WmsStockDO::getLocationId, reqVO.getLocationId())
                .eqIfPresent(WmsStockDO::getStockMode, reqVO.getStockMode())
                .eqIfPresent(WmsStockDO::getBatchNo, reqVO.getBatchNo())
                .betweenIfPresent(WmsStockDO::getProductionDate, reqVO.getProductionDate())
                .betweenIfPresent(WmsStockDO::getExpiryDate, reqVO.getExpiryDate())
                .eqIfPresent(WmsStockDO::getQuantity, reqVO.getQuantity())
                .eqIfPresent(WmsStockDO::getUnitPrice, reqVO.getUnitPrice())
                .betweenIfPresent(WmsStockDO::getCreateTime, reqVO.getCreateTime())
                .apply(StrUtil.isNotBlank(reqVO.getSkuId()),
                    "sku_id IN (SELECT ps.id FROM product_sku ps INNER JOIN product_spu sp ON ps.spu_id = sp.id WHERE sp.name LIKE CONCAT('%', {0}, '%'))",
                    StrUtil.trim(reqVO.getSkuId()))
                .orderByDesc(WmsStockDO::getCreateTime));
    }

    /**
     * 原子预占：数据库层校验可用量，避免并发超预占
     * 仅在 quantity - reserved_quantity >= #{reserveQty} 时更新
     *
     * @return 影响行数（0=预占失败，可用量不足）
     */
    @Update("UPDATE wms_stock SET reserved_quantity = reserved_quantity + #{reserveQty} " +
            "WHERE id = #{id} AND quantity - reserved_quantity >= #{reserveQty}")
    int atomicReserve(@Param("id") Long id, @Param("reserveQty") BigDecimal reserveQty);

    /**
     * 原子解占：数据库层校验预占量充足
     * 仅在 reserved_quantity >= #{releaseQty} 时更新
     *
     * @return 影响行数（0=解占失败，预占量不足）
     */
    @Update("UPDATE wms_stock SET reserved_quantity = reserved_quantity - #{releaseQty} " +
            "WHERE id = #{id} AND reserved_quantity >= #{releaseQty}")
    int atomicRelease(@Param("id") Long id, @Param("releaseQty") BigDecimal releaseQty);

}