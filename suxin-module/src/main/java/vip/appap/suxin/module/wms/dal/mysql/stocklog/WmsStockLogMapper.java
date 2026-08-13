package vip.appap.suxin.module.wms.dal.mysql.stocklog;

import java.util.*;

import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.wms.dal.dataobject.stocklog.WmsStockLogDO;
import org.apache.ibatis.annotations.Mapper;
import vip.appap.suxin.module.wms.controller.admin.stocklog.vo.*;

/**
 * 库存流水台账 Mapper
 *
 * @author admin
 */
@Mapper
public interface WmsStockLogMapper extends BaseMapperX<WmsStockLogDO> {

    default PageResult<WmsStockLogDO> selectPage(WmsStockLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsStockLogDO>()
                .eqIfPresent(WmsStockLogDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(WmsStockLogDO::getFromLocationId, reqVO.getFromLocationId())
                .eqIfPresent(WmsStockLogDO::getToLocationId, reqVO.getToLocationId())
                .eqIfPresent(WmsStockLogDO::getBatchNo, reqVO.getBatchNo())
                .betweenIfPresent(WmsStockLogDO::getProductionDate, reqVO.getProductionDate())
                .betweenIfPresent(WmsStockLogDO::getExpiryDate, reqVO.getExpiryDate())
                .eqIfPresent(WmsStockLogDO::getBizType, reqVO.getBizType())
                .eqIfPresent(WmsStockLogDO::getBizId, reqVO.getBizId())
                .eqIfPresent(WmsStockLogDO::getBizNo, reqVO.getBizNo())
                .eqIfPresent(WmsStockLogDO::getBeforeQuantity, reqVO.getBeforeQuantity())
                .eqIfPresent(WmsStockLogDO::getChangeQuantity, reqVO.getChangeQuantity())
                .eqIfPresent(WmsStockLogDO::getAfterQuantity, reqVO.getAfterQuantity())
                .betweenIfPresent(WmsStockLogDO::getCreateTime, reqVO.getCreateTime())
                .apply(StrUtil.isNotBlank(reqVO.getSkuId()),
                    "sku_id IN (SELECT ps.id FROM product_sku ps INNER JOIN product_spu sp ON ps.spu_id = sp.id WHERE sp.name LIKE CONCAT('%', {0}, '%'))",
                    StrUtil.trim(reqVO.getSkuId()))
                .orderByDesc(WmsStockLogDO::getId));
    }

}