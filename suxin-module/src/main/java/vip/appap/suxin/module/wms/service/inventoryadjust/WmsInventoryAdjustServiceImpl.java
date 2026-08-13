package vip.appap.suxin.module.wms.service.inventoryadjust;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import vip.appap.suxin.module.system.util.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import vip.appap.suxin.module.wms.controller.admin.inventoryadjust.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.inventoryadjust.WmsInventoryAdjustDO;
import vip.appap.suxin.module.wms.dal.dataobject.stock.WmsStockDO;
import vip.appap.suxin.module.wms.dal.dataobject.stocklog.WmsStockLogDO;
import vip.appap.suxin.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;

import vip.appap.suxin.module.wms.dal.mysql.inventoryadjust.WmsInventoryAdjustMapper;
import vip.appap.suxin.module.wms.dal.mysql.stock.WmsStockMapper;
import vip.appap.suxin.module.wms.dal.mysql.stocklog.WmsStockLogMapper;
import vip.appap.suxin.module.wms.dal.mysql.warehouse.WmsWarehouseMapper;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.wms.enums.ErrorCodeConstants.*;

/**
 * 盘点调整记录 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class WmsInventoryAdjustServiceImpl extends BaseService implements WmsInventoryAdjustService {

    @Resource
    private WmsInventoryAdjustMapper inventoryAdjustMapper;
    @Resource
    private WmsStockMapper stockMapper;
    @Resource
    private WmsStockLogMapper stockLogMapper;
    @Resource
    private WmsWarehouseMapper warehouseMapper;

    @Override
    public Long createInventoryAdjust(WmsInventoryAdjustSaveReqVO createReqVO) {
        // 1. 从 wms_stock 读取当前账面库存
        WmsStockDO stock = stockMapper.selectById(createReqVO.getStockId());
        if (stock == null) {
            throw exception(STOCK_NOT_EXISTS);
        }

        // 2. 防重复：检查同一库存行是否已有未审核的盘点草稿
        //    唯一键 = warehouseId + locationId + skuId + batchNo + productionDate + expiryDate
        LambdaQueryWrapperX<WmsInventoryAdjustDO> existQuery = new LambdaQueryWrapperX<WmsInventoryAdjustDO>()
                .eq(WmsInventoryAdjustDO::getWarehouseId, stock.getWarehouseId())
                .eq(WmsInventoryAdjustDO::getLocationId, stock.getLocationId())
                .eq(WmsInventoryAdjustDO::getSkuId, stock.getSkuId())
                .eq(WmsInventoryAdjustDO::getStatus, 0);
        if (stock.getBatchNo() != null) {
            existQuery.eq(WmsInventoryAdjustDO::getBatchNo, stock.getBatchNo());
        } else {
            existQuery.isNull(WmsInventoryAdjustDO::getBatchNo);
        }
        if (stock.getProductionDate() != null) {
            existQuery.eq(WmsInventoryAdjustDO::getProductionDate, stock.getProductionDate());
        } else {
            existQuery.isNull(WmsInventoryAdjustDO::getProductionDate);
        }
        if (stock.getExpiryDate() != null) {
            existQuery.eq(WmsInventoryAdjustDO::getExpiryDate, stock.getExpiryDate());
        } else {
            existQuery.isNull(WmsInventoryAdjustDO::getExpiryDate);
        }
        WmsInventoryAdjustDO existing = inventoryAdjustMapper.selectOne(existQuery);
        if (existing != null) {
            // 已有草稿，更新实盘数量和盘点类型
            WmsInventoryAdjustDO update = new WmsInventoryAdjustDO();
            update.setId(existing.getId());
            update.setActualQuantity(createReqVO.getActualQuantity());
            update.setCheckType(createReqVO.getCheckType() != null ? createReqVO.getCheckType() : 1);
            update.setRemark(createReqVO.getRemark());
            inventoryAdjustMapper.updateById(update);
            return existing.getId();
        }

        // 3. 创建盘点调整记录（草稿）
        WmsInventoryAdjustDO entity = new WmsInventoryAdjustDO();
        entity.setNo(getNo("WmsAdjustNo"));
        entity.setWarehouseId(stock.getWarehouseId());
        entity.setSkuId(stock.getSkuId());
        entity.setLocationId(stock.getLocationId());
        entity.setBatchNo(stock.getBatchNo());
        entity.setProductionDate(stock.getProductionDate());
        entity.setExpiryDate(stock.getExpiryDate());
        entity.setBookQuantity(stock.getQuantity() != null ? stock.getQuantity() : BigDecimal.ZERO);
        entity.setActualQuantity(createReqVO.getActualQuantity());
        entity.setStatus(0); // 草稿
        entity.setCheckType(createReqVO.getCheckType() != null ? createReqVO.getCheckType() : 1);
        entity.setCheckTime(createReqVO.getCheckTime() != null ? createReqVO.getCheckTime() : LocalDateTime.now());
        entity.setRemark(createReqVO.getRemark());
        inventoryAdjustMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void updateInventoryAdjust(WmsInventoryAdjustSaveReqVO updateReqVO) {
        // 校验存在且为草稿
        WmsInventoryAdjustDO adjust = validateAdjustDraft(updateReqVO.getId());
        // 只允许修改实盘数量和备注
        WmsInventoryAdjustDO update = new WmsInventoryAdjustDO();
        update.setId(updateReqVO.getId());
        update.setActualQuantity(updateReqVO.getActualQuantity());
        update.setCheckType(updateReqVO.getCheckType());
        update.setRemark(updateReqVO.getRemark());
        inventoryAdjustMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveInventoryAdjust(Long id) {
        // ====== [P0] 乐观锁 CAS：原子更新 status=0→1，防止并发重复审核 ======
        WmsInventoryAdjustDO updateStatus = new WmsInventoryAdjustDO();
        updateStatus.setStatus(1);
        int affected = inventoryAdjustMapper.update(updateStatus, new LambdaQueryWrapperX<WmsInventoryAdjustDO>()
                .eq(WmsInventoryAdjustDO::getId, id)
                .eq(WmsInventoryAdjustDO::getStatus, 0));
        if (affected == 0) {
            // 说明记录已被其他请求审核，或记录不存在
            WmsInventoryAdjustDO check = inventoryAdjustMapper.selectById(id);
            if (check == null) {
                throw exception(INVENTORY_ADJUST_NOT_EXISTS);
            }
            throw exception(INVENTORY_ADJUST_STATUS_NOT_ALLOW_UPDATE);
        }

        // 重新读取已更新状态的记录
        WmsInventoryAdjustDO adjust = inventoryAdjustMapper.selectById(id);
        // 此时 status 已为 1，后续逻辑不再更新状态

        // 2. 计算差异：diff = actual - book（正=盘盈，负=盘亏）
        BigDecimal bookQuantity = adjust.getBookQuantity() != null ? adjust.getBookQuantity() : BigDecimal.ZERO;
        BigDecimal actualQuantity = adjust.getActualQuantity() != null ? adjust.getActualQuantity() : BigDecimal.ZERO;
        BigDecimal diff = actualQuantity.subtract(bookQuantity);

        // 3. 查找或创建库存记录
        LambdaQueryWrapperX<WmsStockDO> queryWrapper = new LambdaQueryWrapperX<WmsStockDO>()
                .eq(WmsStockDO::getWarehouseId, adjust.getWarehouseId())
                .eq(WmsStockDO::getLocationId, adjust.getLocationId())
                .eq(WmsStockDO::getSkuId, adjust.getSkuId());
        if (adjust.getBatchNo() != null) {
            queryWrapper.eq(WmsStockDO::getBatchNo, adjust.getBatchNo());
        } else {
            queryWrapper.isNull(WmsStockDO::getBatchNo);
        }
        if (adjust.getProductionDate() != null) {
            queryWrapper.eq(WmsStockDO::getProductionDate, adjust.getProductionDate());
        } else {
            queryWrapper.isNull(WmsStockDO::getProductionDate);
        }
        if (adjust.getExpiryDate() != null) {
            queryWrapper.eq(WmsStockDO::getExpiryDate, adjust.getExpiryDate());
        } else {
            queryWrapper.isNull(WmsStockDO::getExpiryDate);
        }
        WmsStockDO stock = stockMapper.selectOne(queryWrapper);
        BigDecimal beforeQty = stock != null ? stock.getQuantity() : BigDecimal.ZERO;
        BigDecimal afterQty = beforeQty.add(diff);
        if (afterQty.compareTo(BigDecimal.ZERO) < 0) {
            throw exception(STOCK_QUANTITY_INSUFFICIENT);
        }

        // 4. 更新或创建库存记录（diff=0 时跳过）
        if (diff.compareTo(BigDecimal.ZERO) != 0) {
            if (stock != null) {
                stock.setQuantity(afterQty);
                stockMapper.updateById(stock);
            } else {
                WmsStockDO newStock = new WmsStockDO();
                newStock.setSkuId(adjust.getSkuId());
                newStock.setWarehouseId(adjust.getWarehouseId());
                newStock.setLocationId(adjust.getLocationId());
                newStock.setBatchNo(adjust.getBatchNo());
                newStock.setProductionDate(adjust.getProductionDate());
                newStock.setExpiryDate(adjust.getExpiryDate());
                newStock.setQuantity(afterQty);
                stockMapper.insert(newStock);
            }
        }

        // 5. [P2] 动态获取盘点差异虚拟仓 ID，避免硬编码
        Long virtualWarehouseId = getInventoryDiffVirtualWarehouseId();

        // 6. 写入库存流水（[P1] diff=0 时也写入 changeQuantity=0 的流水，保留审计轨迹）
        WmsStockLogDO log = new WmsStockLogDO();
        log.setSkuId(adjust.getSkuId());
        log.setWarehouseId(adjust.getWarehouseId());
        log.setFromLocationId(virtualWarehouseId);
        log.setToLocationId(adjust.getLocationId());
        log.setBatchNo(adjust.getBatchNo());
        log.setProductionDate(adjust.getProductionDate());
        log.setExpiryDate(adjust.getExpiryDate());
        log.setBizType("adjust");
        log.setBizId(adjust.getId());
        log.setBizNo(adjust.getNo());
        log.setBeforeQuantity(beforeQty);
        log.setChangeQuantity(diff); // diff=0 时也为 0，记录"无差异审核"
        log.setAfterQuantity(beforeQty.add(diff)); // diff=0 时 = beforeQty
        stockLogMapper.insert(log);

        // 注意：status 已在第 1 步 CAS 更新为 1，无需再次更新
    }

    @Override
    public void deleteInventoryAdjust(Long id) {
        WmsInventoryAdjustDO adjust = validateAdjustDraft(id);
        inventoryAdjustMapper.deleteById(id);
    }

    @Override
    public WmsInventoryAdjustDO getInventoryAdjust(Long id) {
        return inventoryAdjustMapper.selectById(id);
    }

    @Override
    public PageResult<WmsInventoryAdjustDO> getInventoryAdjustPage(WmsInventoryAdjustPageReqVO pageReqVO) {
        return inventoryAdjustMapper.selectPage(pageReqVO);
    }

    // ========== 私有方法 ==========

    /** 校验调整记录存在且为草稿 */
    private WmsInventoryAdjustDO validateAdjustDraft(Long id) {
        WmsInventoryAdjustDO adjust = inventoryAdjustMapper.selectById(id);
        if (adjust == null) {
            throw exception(INVENTORY_ADJUST_NOT_EXISTS);
        }
        if (adjust.getStatus() != 0) {
            throw exception(INVENTORY_ADJUST_STATUS_NOT_ALLOW_UPDATE);
        }
        return adjust;
    }

    /** 更新调整单状态 */
    private void updateAdjustStatus(Long id, Integer status) {
        WmsInventoryAdjustDO update = new WmsInventoryAdjustDO();
        update.setId(id);
        update.setStatus(status);
        inventoryAdjustMapper.updateById(update);
    }

    /**
     * 动态获取盘点差异虚拟仓 ID
     * 通过 locType=6 查询，兜底默认 3
     */
    private Long getInventoryDiffVirtualWarehouseId() {
        WmsWarehouseDO wh = warehouseMapper.selectOne(
                new LambdaQueryWrapperX<WmsWarehouseDO>()
                        .eq(WmsWarehouseDO::getLocType, 6).last("LIMIT 1"));
        return wh != null ? wh.getId() : 3L;
    }
}