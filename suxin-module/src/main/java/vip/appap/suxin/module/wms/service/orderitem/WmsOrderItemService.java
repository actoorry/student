package vip.appap.suxin.module.wms.service.orderitem;

import java.util.*;
import jakarta.validation.*;
import vip.appap.suxin.module.wms.controller.admin.orderitem.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.orderitem.WmsOrderItemDO;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.pojo.PageParam;

/**
 * 出入库/调拨单据明细 Service 接口
 *
 * @author admin
 */
public interface WmsOrderItemService {

    /**
     * 创建出入库/调拨单据明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOrderItem(@Valid WmsOrderItemSaveReqVO createReqVO);

    /**
     * 更新出入库/调拨单据明细
     *
     * @param updateReqVO 更新信息
     */
    void updateOrderItem(@Valid WmsOrderItemSaveReqVO updateReqVO);

    /**
     * 删除出入库/调拨单据明细
     *
     * @param id 编号
     */
    void deleteOrderItem(Long id);

    /**
    * 批量删除出入库/调拨单据明细
    *
    * @param ids 编号
    */
    void deleteOrderItemListByIds(List<Long> ids);

    /**
     * 获得出入库/调拨单据明细
     *
     * @param id 编号
     * @return 出入库/调拨单据明细
     */
    WmsOrderItemDO getOrderItem(Long id);

    /**
     * 获得出入库/调拨单据明细分页
     *
     * @param pageReqVO 分页查询
     * @return 出入库/调拨单据明细分页
     */
    PageResult<WmsOrderItemDO> getOrderItemPage(WmsOrderItemPageReqVO pageReqVO);

}