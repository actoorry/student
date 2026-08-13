package vip.appap.suxin.module.wms.dal.mysql.order;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.wms.dal.dataobject.order.WmsOrderDO;
import org.apache.ibatis.annotations.Mapper;
import vip.appap.suxin.module.wms.controller.admin.order.vo.*;

/**
 * 出入库/调拨单据头 Mapper
 *
 * @author admin
 */
@Mapper
public interface WmsOrderMapper extends BaseMapperX<WmsOrderDO> {

    default PageResult<WmsOrderDO> selectPage(WmsOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsOrderDO>()
                .eqIfPresent(WmsOrderDO::getNo, reqVO.getNo())
                .eqIfPresent(WmsOrderDO::getTypeId, reqVO.getTypeId())
                .eqIfPresent(WmsOrderDO::getStatus, reqVO.getStatus())
                .eqIfPresent(WmsOrderDO::getSupplierId, reqVO.getSupplierId())
                .eqIfPresent(WmsOrderDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(WmsOrderDO::getFromWarehouseId, reqVO.getFromWarehouseId())
                .eqIfPresent(WmsOrderDO::getToWarehouseId, reqVO.getToWarehouseId())
                .eqIfPresent(WmsOrderDO::getOriginId, reqVO.getOriginId())
                .eqIfPresent(WmsOrderDO::getOperatorId, reqVO.getOperatorId())
                .betweenIfPresent(WmsOrderDO::getOrderTime, reqVO.getOrderTime())
                .eqIfPresent(WmsOrderDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(WmsOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsOrderDO::getId));
    }

}