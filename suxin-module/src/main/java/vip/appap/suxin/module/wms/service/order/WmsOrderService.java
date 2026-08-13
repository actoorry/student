package vip.appap.suxin.module.wms.service.order;

import java.util.*;
import jakarta.validation.*;
import vip.appap.suxin.module.wms.controller.admin.order.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.order.WmsOrderDO;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.pojo.PageParam;

/**
 * 出入库/调拨单据头 Service 接口
 *
 * @author admin
 */
public interface WmsOrderService {

    /**
     * 创建出入库/调拨单据头
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOrder(@Valid WmsOrderSaveReqVO createReqVO);

    /**
     * 更新出入库/调拨单据头
     *
     * @param updateReqVO 更新信息
     */
    void updateOrder(@Valid WmsOrderSaveReqVO updateReqVO);

    /**
     * 删除出入库/调拨单据头
     *
     * @param id 编号
     */
    void deleteOrder(Long id);

    /**
    * 批量删除出入库/调拨单据头
    *
    * @param ids 编号
    */
    void deleteOrderListByIds(List<Long> ids);

    /**
     * 获得出入库/调拨单据头
     *
     * @param id 编号
     * @return 出入库/调拨单据头
     */
    WmsOrderDO getOrder(Long id);

    /**
     * 获得出入库/调拨单据头分页
     *
     * @param pageReqVO 分页查询
     * @return 出入库/调拨单据头分页
     */
    PageResult<WmsOrderDO> getOrderPage(WmsOrderPageReqVO pageReqVO);

    // ========== 单据状态流转 ==========

    /**
     * 提交审核（草稿 → 待审核）
     */
    void submitOrder(Long id);

    /**
     * 审核通过（待审核 → 已审核）
     *
     * 说明：安全库存上下限预警不在审核时提示，改由实时库存查询页按当前库存持续展示
     *
     * @param id 单据编号
     */
    void approveOrder(Long id);

    /**
     * 完成（已审核 → 已完成）
     */
    void finishOrder(Long id);

    /**
     * 取消（草稿/待审核 → 已取消）
     */
    void cancelOrder(Long id);

    /**
     * 生成退货单（已审核单据 → 生成反向单据）
     *
     * @param id 原单据编号
     * @return 新退货单编号
     */
    Long reverseOrder(Long id);

}