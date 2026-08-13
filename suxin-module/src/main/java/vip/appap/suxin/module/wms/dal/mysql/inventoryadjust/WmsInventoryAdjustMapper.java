package vip.appap.suxin.module.wms.dal.mysql.inventoryadjust;

import java.util.*;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.wms.dal.dataobject.inventoryadjust.WmsInventoryAdjustDO;
import org.apache.ibatis.annotations.Mapper;
import vip.appap.suxin.module.wms.controller.admin.inventoryadjust.vo.*;

/**
 * 盘点调整记录 Mapper
 *
 * @author admin
 */
@Mapper
public interface WmsInventoryAdjustMapper extends BaseMapperX<WmsInventoryAdjustDO> {

    default PageResult<WmsInventoryAdjustDO> selectPage(WmsInventoryAdjustPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsInventoryAdjustDO>()
                .eqIfPresent(WmsInventoryAdjustDO::getNo, reqVO.getNo())
                .eqIfPresent(WmsInventoryAdjustDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(WmsInventoryAdjustDO::getStatus, reqVO.getStatus())
                .eqIfPresent(WmsInventoryAdjustDO::getCheckType, reqVO.getCheckType())
                .orderByDesc(WmsInventoryAdjustDO::getId));
    }
}