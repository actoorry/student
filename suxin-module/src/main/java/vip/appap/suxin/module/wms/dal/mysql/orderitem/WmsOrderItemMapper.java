package vip.appap.suxin.module.wms.dal.mysql.orderitem;

import java.util.*;

import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.wms.dal.dataobject.orderitem.WmsOrderItemDO;
import org.apache.ibatis.annotations.Mapper;
import vip.appap.suxin.module.wms.controller.admin.orderitem.vo.*;

/**
 * 出入库/调拨单据明细 Mapper
 *
 * @author admin
 */
@Mapper
public interface WmsOrderItemMapper extends BaseMapperX<WmsOrderItemDO> {

    default PageResult<WmsOrderItemDO> selectPage(WmsOrderItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsOrderItemDO>()
                .eqIfPresent(WmsOrderItemDO::getOrderId, reqVO.getOrderId())
                .eqIfPresent(WmsOrderItemDO::getOriginId, reqVO.getOriginId())
                .eqIfPresent(WmsOrderItemDO::getFromLocationId, reqVO.getFromLocationId())
                .eqIfPresent(WmsOrderItemDO::getToLocationId, reqVO.getToLocationId())
                .eqIfPresent(WmsOrderItemDO::getBatchNo, reqVO.getBatchNo())
                .betweenIfPresent(WmsOrderItemDO::getProductionDate, reqVO.getProductionDate())
                .betweenIfPresent(WmsOrderItemDO::getExpiryDate, reqVO.getExpiryDate())
                .eqIfPresent(WmsOrderItemDO::getQuantity, reqVO.getQuantity())
                .eqIfPresent(WmsOrderItemDO::getUnitPrice, reqVO.getUnitPrice())
                .eqIfPresent(WmsOrderItemDO::getSort, reqVO.getSort())
                .eqIfPresent(WmsOrderItemDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(WmsOrderItemDO::getCreateTime, reqVO.getCreateTime())
                .apply(StrUtil.isNotBlank(reqVO.getSkuId()),
                    "sku_id IN (SELECT ps.id FROM product_sku ps INNER JOIN product_spu sp ON ps.spu_id = sp.id WHERE sp.name LIKE CONCAT('%', {0}, '%'))",
                    StrUtil.trim(reqVO.getSkuId()))
                .orderByDesc(WmsOrderItemDO::getId));
    }

}