package vip.appap.suxin.module.wms.service.inventoryadjust;

import jakarta.validation.*;
import vip.appap.suxin.module.wms.controller.admin.inventoryadjust.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.inventoryadjust.WmsInventoryAdjustDO;
import vip.appap.suxin.framework.common.pojo.PageResult;

/**
 * 盘点调整记录 Service 接口
 *
 * @author admin
 */
public interface WmsInventoryAdjustService {

    /**
     * 创建盘点调整（草稿），book_quantity 实时从 wms_stock 读取
     */
    Long createInventoryAdjust(@Valid WmsInventoryAdjustSaveReqVO createReqVO);

    /**
     * 更新盘点调整草稿
     */
    void updateInventoryAdjust(@Valid WmsInventoryAdjustSaveReqVO updateReqVO);

    /**
     * 审核盘点调整：
     * diff = actual - book
     * diff>0 盘盈：from=3 → to=库位，库存+diff
     * diff<0 盘亏：from=3 → to=库位，库存-|diff|
     * diff=0 跳过
     * 审核后：status=1，写流水 biz_type=adjust
     */
    void approveInventoryAdjust(Long id);

    /**
     * 删除盘点调整草稿
     */
    void deleteInventoryAdjust(Long id);

    /**
     * 获得盘点调整记录
     */
    WmsInventoryAdjustDO getInventoryAdjust(Long id);

    /**
     * 获得盘点调整记录分页
     */
    PageResult<WmsInventoryAdjustDO> getInventoryAdjustPage(WmsInventoryAdjustPageReqVO pageReqVO);
}