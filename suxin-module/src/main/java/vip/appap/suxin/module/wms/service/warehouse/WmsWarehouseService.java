package vip.appap.suxin.module.wms.service.warehouse;

import java.util.*;
import jakarta.validation.*;
import vip.appap.suxin.module.wms.controller.admin.warehouse.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.pojo.PageParam;

/**
 * 仓库位置表（仓库/库区/库位树形结构） Service 接口
 *
 * @author admin
 */
public interface WmsWarehouseService {

    /**
     * 创建仓库位置表（仓库/库区/库位树形结构）
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWarehouse(@Valid WmsWarehouseSaveReqVO createReqVO);

    /**
     * 更新仓库位置表（仓库/库区/库位树形结构）
     *
     * @param updateReqVO 更新信息
     */
    void updateWarehouse(@Valid WmsWarehouseSaveReqVO updateReqVO);

    /**
     * 删除仓库位置表（仓库/库区/库位树形结构）
     *
     * @param id 编号
     */
    void deleteWarehouse(Long id);

    /**
    * 批量删除仓库位置表（仓库/库区/库位树形结构）
    *
    * @param ids 编号
    */
    void deleteWarehouseListByIds(List<Long> ids);

    /**
     * 获得仓库位置表（仓库/库区/库位树形结构）
     *
     * @param id 编号
     * @return 仓库位置表（仓库/库区/库位树形结构）
     */
    WmsWarehouseDO getWarehouse(Long id);

    /**
     * 获得仓库位置表（仓库/库区/库位树形结构）分页
     *
     * @param pageReqVO 分页查询
     * @return 仓库位置表（仓库/库区/库位树形结构）分页
     */
    PageResult<WmsWarehouseDO> getWarehousePage(WmsWarehousePageReqVO pageReqVO);

}