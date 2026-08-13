package vip.appap.suxin.module.wms.dal.mysql.ordertype;

import java.util.*;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.wms.dal.dataobject.ordertype.WmsOrderTypeDO;
import org.apache.ibatis.annotations.Mapper;
import vip.appap.suxin.module.wms.controller.admin.ordertype.vo.*;

/**
 * 出入库单据类型配置 Mapper
 *
 * @author admin
 */
@Mapper
public interface WmsOrderTypeMapper extends BaseMapperX<WmsOrderTypeDO> {

    default PageResult<WmsOrderTypeDO> selectPage(WmsOrderTypePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsOrderTypeDO>()
                .likeIfPresent(WmsOrderTypeDO::getName, reqVO.getName())
                .eqIfPresent(WmsOrderTypeDO::getFromLocationId, reqVO.getFromLocationId())
                .eqIfPresent(WmsOrderTypeDO::getToLocationId, reqVO.getToLocationId())
                .eqIfPresent(WmsOrderTypeDO::getStockImpact, reqVO.getStockImpact())
                .eqIfPresent(WmsOrderTypeDO::getNeedSupplier, reqVO.getNeedSupplier())
                .eqIfPresent(WmsOrderTypeDO::getNeedCustomer, reqVO.getNeedCustomer())
                .eqIfPresent(WmsOrderTypeDO::getActive, reqVO.getActive())
                .eqIfPresent(WmsOrderTypeDO::getSort, reqVO.getSort())
                .eqIfPresent(WmsOrderTypeDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(WmsOrderTypeDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsOrderTypeDO::getId));
    }

}