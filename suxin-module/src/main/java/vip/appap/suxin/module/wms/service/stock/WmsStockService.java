package vip.appap.suxin.module.wms.service.stock;

import java.util.*;
import jakarta.validation.*;
import vip.appap.suxin.module.wms.controller.admin.stock.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.stock.WmsStockDO;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.pojo.PageParam;

/**
 * 库存快照 Service 接口
 *
 * @author admin
 */
public interface WmsStockService {

    /**
     * 创建库存快照
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStock(@Valid WmsStockSaveReqVO createReqVO);

    /**
     * 更新库存快照
     *
     * @param updateReqVO 更新信息
     */
    void updateStock(@Valid WmsStockSaveReqVO updateReqVO);

    /**
     * 删除库存快照
     *
     * @param id 编号
     */
    void deleteStock(Long id);

    /**
    * 批量删除库存快照
    *
    * @param ids 编号
    */
    void deleteStockListByIds(List<Long> ids);

    /**
     * 获得库存快照
     *
     * @param id 编号
     * @return 库存快照
     */
    WmsStockDO getStock(Long id);

    /**
     * 获得库存快照分页
     *
     * @param pageReqVO 分页查询
     * @return 库存快照分页
     */
    PageResult<WmsStockDO> getStockPage(WmsStockPageReqVO pageReqVO);

    /**
     * 清理零库存记录：删除库存数量与预占数量均为 0 的库存快照
     * <p>
     * 由定时任务（WmsStockCleanupJob）定期调用，避免 wms_stock 堆积无意义的零库存行
     *
     * @return 清理的记录数
     */
    int cleanZeroStock();

}