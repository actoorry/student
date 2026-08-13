package vip.appap.suxin.module.wms.service.stocklog;

import java.util.*;
import jakarta.validation.*;
import vip.appap.suxin.module.wms.controller.admin.stocklog.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.stocklog.WmsStockLogDO;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.pojo.PageParam;

/**
 * 库存流水台账 Service 接口
 *
 * @author admin
 */
public interface WmsStockLogService {

    /**
     * 创建库存流水台账
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStockLog(@Valid WmsStockLogSaveReqVO createReqVO);

    /**
     * 更新库存流水台账
     *
     * @param updateReqVO 更新信息
     */
    void updateStockLog(@Valid WmsStockLogSaveReqVO updateReqVO);

    /**
     * 删除库存流水台账
     *
     * @param id 编号
     */
    void deleteStockLog(Long id);

    /**
    * 批量删除库存流水台账
    *
    * @param ids 编号
    */
    void deleteStockLogListByIds(List<Long> ids);

    /**
     * 获得库存流水台账
     *
     * @param id 编号
     * @return 库存流水台账
     */
    WmsStockLogDO getStockLog(Long id);

    /**
     * 获得库存流水台账分页
     *
     * @param pageReqVO 分页查询
     * @return 库存流水台账分页
     */
    PageResult<WmsStockLogDO> getStockLogPage(WmsStockLogPageReqVO pageReqVO);

}