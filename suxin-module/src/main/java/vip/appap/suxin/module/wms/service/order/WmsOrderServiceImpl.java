package vip.appap.suxin.module.wms.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import vip.appap.suxin.module.system.util.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import vip.appap.suxin.module.wms.controller.admin.order.vo.*;
import vip.appap.suxin.module.wms.controller.admin.orderitem.vo.WmsOrderItemSaveReqVO;
import vip.appap.suxin.module.wms.dal.dataobject.order.WmsOrderDO;
import vip.appap.suxin.module.wms.dal.dataobject.orderitem.WmsOrderItemDO;
import vip.appap.suxin.module.wms.dal.dataobject.ordertype.WmsOrderTypeDO;
import vip.appap.suxin.module.wms.dal.dataobject.stock.WmsStockDO;
import vip.appap.suxin.module.wms.dal.dataobject.stocklog.WmsStockLogDO;
import vip.appap.suxin.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;

import vip.appap.suxin.module.wms.dal.mysql.order.WmsOrderMapper;
import vip.appap.suxin.module.wms.dal.mysql.orderitem.WmsOrderItemMapper;
import vip.appap.suxin.module.wms.dal.mysql.ordertype.WmsOrderTypeMapper;
import vip.appap.suxin.module.wms.dal.mysql.stock.WmsStockMapper;
import vip.appap.suxin.module.wms.dal.mysql.stocklog.WmsStockLogMapper;
import vip.appap.suxin.module.wms.dal.mysql.warehouse.WmsWarehouseMapper;

import vip.appap.suxin.module.wms.service.product.WmsProductService;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.module.wms.enums.ErrorCodeConstants.*;

/**
 * 出入库/调拨单据头 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class WmsOrderServiceImpl extends BaseService implements WmsOrderService {

    @Resource
    private WmsOrderMapper orderMapper;
    @Resource
    private WmsOrderItemMapper orderItemMapper;
    @Resource
    private WmsOrderTypeMapper orderTypeMapper;
    @Resource
    private WmsStockMapper stockMapper;
    @Resource
    private WmsStockLogMapper stockLogMapper;
    @Resource
    private WmsWarehouseMapper warehouseMapper;
    @Resource
    private WmsProductService wmsProductService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(WmsOrderSaveReqVO createReqVO) {
        // 1. 生成单据编号（Redis INCR 自增），默认草稿状态
        WmsOrderDO order = BeanUtils.toBean(createReqVO, WmsOrderDO.class);
        order.setNo(getNo("WmsOrderNo"));
        if (order.getStatus() == null) {
            order.setStatus(0); // 默认草稿
        }
        if (order.getOrderTime() == null) {
            order.setOrderTime(LocalDateTime.now()); // 自动填充当前时间
        }
        // 2. 插入单据头
        orderMapper.insert(order);
        // 3. 插入明细行
        if (CollUtil.isNotEmpty(createReqVO.getItems())) {
            List<WmsOrderItemDO> items = convertList(createReqVO.getItems(), item -> {
                WmsOrderItemDO itemDO = BeanUtils.toBean(item, WmsOrderItemDO.class);
                itemDO.setOrderId(order.getId());
                return itemDO;
            });
            orderItemMapper.insertBatch(items);
        }
        // 4. 返回
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(WmsOrderSaveReqVO updateReqVO) {
        // 校验存在
        WmsOrderDO order = validateOrderExists(updateReqVO.getId());
        // 更新单据头
        WmsOrderDO updateObj = BeanUtils.toBean(updateReqVO, WmsOrderDO.class);
        orderMapper.updateById(updateObj);
        // 处理明细行：先删后增
        List<WmsOrderItemDO> oldItems = orderItemMapper.selectList(WmsOrderItemDO::getOrderId, updateReqVO.getId());
        // 如果单据已预占（待审核状态），先解占旧明细的库存
        if (order.getStatus() == 1 && CollUtil.isNotEmpty(oldItems)) {
            for (WmsOrderItemDO oldItem : oldItems) {
                unreserveStock(oldItem);
            }
            // 解占后回退到草稿状态，用户需重新提交预占
            WmsOrderDO resetStatus = new WmsOrderDO();
            resetStatus.setId(updateReqVO.getId());
            resetStatus.setStatus(0);
            orderMapper.updateById(resetStatus);
        }
        // 删除旧明细
        if (CollUtil.isNotEmpty(oldItems)) {
            orderItemMapper.deleteBatchIds(convertList(oldItems, WmsOrderItemDO::getId));
        }
        // 批量插入最新明细（清空id让数据库自增，避免主键冲突）
        if (CollUtil.isNotEmpty(updateReqVO.getItems())) {
            List<WmsOrderItemDO> items = convertList(updateReqVO.getItems(), item -> {
                WmsOrderItemDO itemDO = BeanUtils.toBean(item, WmsOrderItemDO.class);
                itemDO.setId(null);
                itemDO.setOrderId(updateReqVO.getId());
                return itemDO;
            });
            orderItemMapper.insertBatch(items);
        }
    }

    @Override
    public void deleteOrder(Long id) {
        // 校验存在
        validateOrderExists(id);
        // 删除
        orderMapper.deleteById(id);
    }

    @Override
    public void deleteOrderListByIds(List<Long> ids) {
        // 删除
        orderMapper.deleteByIds(ids);
    }


    private WmsOrderDO validateOrderExists(Long id) {
        WmsOrderDO order = orderMapper.selectById(id);
        if (order == null) {
            throw exception(ORDER_NOT_EXISTS);
        }
        return order;
    }

    @Override
    public WmsOrderDO getOrder(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public PageResult<WmsOrderDO> getOrderPage(WmsOrderPageReqVO pageReqVO) {
        return orderMapper.selectPage(pageReqVO);
    }

    // ========== 单据状态流转 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitOrder(Long id) {
        WmsOrderDO order = validateOrderExists(id);
        if (order.getStatus() != 0) {
            throw exception(ORDER_STATUS_NOT_ALLOW_SUBMIT);
        }
        // 预占库存
        List<WmsOrderItemDO> items = orderItemMapper.selectList(WmsOrderItemDO::getOrderId, id);
        for (WmsOrderItemDO item : items) {
            reserveStock(item);
        }
        WmsOrderDO updateObj = new WmsOrderDO();
        updateObj.setId(id);
        updateObj.setStatus(1);
        orderMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveOrder(Long id) {
        WmsOrderDO order = validateOrderExists(id);
        if (order.getStatus() != 1) {
            throw exception(ORDER_STATUS_NOT_ALLOW_APPROVE);
        }
        // 获取单据类型，确定库存影响方向
        WmsOrderTypeDO orderType = orderTypeMapper.selectById(order.getTypeId());
        if (orderType == null) {
            throw exception(ORDER_TYPE_NOT_EXISTS);
        }
        // 获取单据明细
        List<WmsOrderItemDO> items = orderItemMapper.selectList(WmsOrderItemDO::getOrderId, id);
        if (CollUtil.isEmpty(items)) {
            throw exception(ORDER_ITEM_NOT_EXISTS);
        }
        // 逐行处理库存（安全库存预警不在审核时提示，改由实时库存查询页按当前库存持续展示）
        for (WmsOrderItemDO item : items) {
            int stockImpact = orderType.getStockImpact();
            if (stockImpact == 1) {
                // ====== 入库：累加库存 + 移动平均单价 ======
                processInbound(item, order);
            } else if (stockImpact == 2) {
                // ====== 出库：FIFO 近效期优先扣减 ======
                processOutbound(item, order);
            } else if (stockImpact == 3) {
                // ====== 内部调拨：来源出库(FIFO) + 去向入库 ======
                processTransfer(item, order);
            }
        }
        // 更新单据状态为已审核
        WmsOrderDO updateObj = new WmsOrderDO();
        updateObj.setId(id);
        updateObj.setStatus(2);
        orderMapper.updateById(updateObj);
    }

    /**
     * 入库处理：按唯一键 upsert 累加 + 移动平均单价 + 写入库存管控方式
     */
    private void processInbound(WmsOrderItemDO item, WmsOrderDO order) {
        Long locationId = item.getToLocationId();
        Long warehouseId = getRootWarehouseId(locationId);
        if (warehouseId == null) return; // 虚拟仓不更新真实库存

        // 冗余库存管控方式（库存添加时写入，用于前端展示与后续批次校验）
        Integer stockMode = wmsProductService.getStockModeBySkuId(item.getSkuId());

        // 批次/序列号管控产品（stock_mode != 0）入库必须填写批次号
        validateBatchNoRequired(item, stockMode);

        // 查找库存记录（联合唯一键）
        WmsStockDO stock = findStockByUniqueKey(warehouseId, locationId, item);
        BigDecimal beforeQty = stock != null ? stock.getQuantity() : BigDecimal.ZERO;

        BigDecimal afterQty;
        if (stock != null) {
            // 移动平均单价 = (旧库存×旧均价 + 入库量×入库价) / (旧库存+入库量)
            if (item.getUnitPrice() != null) {
                BigDecimal oldTotalValue = stock.getQuantity().multiply(
                    stock.getUnitPrice() != null ? stock.getUnitPrice() : BigDecimal.ZERO);
                BigDecimal newTotalValue = item.getQuantity().multiply(item.getUnitPrice());
                BigDecimal totalQty = stock.getQuantity().add(item.getQuantity());
                if (totalQty.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal avgPrice = oldTotalValue.add(newTotalValue)
                        .divide(totalQty, 2, RoundingMode.HALF_UP);
                    stock.setUnitPrice(avgPrice);
                }
            }
            stock.setQuantity(stock.getQuantity().add(item.getQuantity()));
            if (stockMode != null) {
                stock.setStockMode(stockMode);
            }
            stockMapper.updateById(stock);
            afterQty = stock.getQuantity();
        } else {
            WmsStockDO newStock = new WmsStockDO();
            newStock.setSkuId(item.getSkuId());
            newStock.setWarehouseId(warehouseId);
            newStock.setLocationId(locationId);
            newStock.setBatchNo(item.getBatchNo());
            newStock.setProductionDate(item.getProductionDate());
            newStock.setExpiryDate(item.getExpiryDate());
            newStock.setQuantity(item.getQuantity());
            newStock.setUnitPrice(item.getUnitPrice());
            newStock.setStockMode(stockMode);
            stockMapper.insert(newStock);
            afterQty = item.getQuantity();
        }

        // 写入库存流水
        writeStockLog(item, order, warehouseId, locationId, item.getToLocationId(),
            beforeQty, item.getQuantity(), afterQty);
    }

    /**
     * 校验批次号必填：批次管理（stock_mode=1）/序列号管理（stock_mode=2）产品入库（含调拨入库）时必须填写批次号
     */
    private void validateBatchNoRequired(WmsOrderItemDO item, Integer stockMode) {
        if (stockMode != null && stockMode != 0 && StrUtil.isBlank(item.getBatchNo())) {
            throw exception(ORDER_ITEM_BATCH_REQUIRED);
        }
    }

    /**
     * 出库处理：FIFO 近效期优先逐批扣减（审核时消耗预占）
     */
    private void processOutbound(WmsOrderItemDO item, WmsOrderDO order) {
        Long locationId = item.getFromLocationId();
        Long warehouseId = getRootWarehouseId(locationId);
        if (warehouseId == null) return; // 虚拟仓不更新真实库存

        BigDecimal toDeduct = item.getQuantity();
        // FIFO 排序：按创建时间升序 + 有效期升序（近效期优先）
        LambdaQueryWrapperX<WmsStockDO> queryWrapper = (LambdaQueryWrapperX<WmsStockDO>) new LambdaQueryWrapperX<WmsStockDO>()
            .eq(WmsStockDO::getWarehouseId, warehouseId)
            .eq(WmsStockDO::getLocationId, locationId)
            .eq(WmsStockDO::getSkuId, item.getSkuId())
            .gt(WmsStockDO::getQuantity, 0)
            .orderByAsc(WmsStockDO::getCreateTime)
            .orderByAsc(WmsStockDO::getExpiryDate);
        if (item.getBatchNo() != null) {
            queryWrapper.eq(WmsStockDO::getBatchNo, item.getBatchNo());
        }
        List<WmsStockDO> stockList = stockMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(stockList)) {
            throw exception(STOCK_QUANTITY_INSUFFICIENT);
        }

        for (WmsStockDO batch : stockList) {
            if (toDeduct.compareTo(BigDecimal.ZERO) <= 0) break;
            // 可用量 = 库存量 - 预占量（审核时消耗预占，所以可用量包含了本单预占的部分）
            BigDecimal reserved = batch.getReservedQuantity() != null ? batch.getReservedQuantity() : BigDecimal.ZERO;
            BigDecimal available = batch.getQuantity().subtract(reserved);
            if (available.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal deductQty = available.compareTo(toDeduct) >= 0 ? toDeduct : available;
            BigDecimal beforeQty = batch.getQuantity();
            BigDecimal afterQty = beforeQty.subtract(deductQty);
            BigDecimal afterReserved = reserved.subtract(deductQty); // 消耗预占

            // 写入该批次的库存流水
            WmsStockLogDO log = new WmsStockLogDO();
            log.setSkuId(item.getSkuId());
            log.setWarehouseId(warehouseId);
            log.setFromLocationId(item.getFromLocationId());
            log.setToLocationId(item.getToLocationId());
            log.setBatchNo(batch.getBatchNo());
            log.setProductionDate(batch.getProductionDate());
            log.setExpiryDate(batch.getExpiryDate());
            log.setBizType("order");
            log.setBizId(order.getId());
            log.setBizNo(order.getNo());
            log.setBeforeQuantity(beforeQty);
            log.setChangeQuantity(deductQty.negate());
            log.setAfterQuantity(afterQty);
            stockLogMapper.insert(log);

            // 更新库存：扣减库存 + 消耗预占
            batch.setQuantity(afterQty);
            batch.setReservedQuantity(afterReserved);
            stockMapper.updateById(batch);

            toDeduct = toDeduct.subtract(deductQty);
        }
        if (toDeduct.compareTo(BigDecimal.ZERO) > 0) {
            throw exception(STOCK_QUANTITY_INSUFFICIENT);
        }
    }

    /**
     * 内部调拨处理：来源出库(FIFO) + 去向入库
     */
    private void processTransfer(WmsOrderItemDO item, WmsOrderDO order) {
        // 出库端：FIFO 扣减
        Long fromLocationId = item.getFromLocationId();
        Long fromWarehouseId = getRootWarehouseId(fromLocationId);
        if (fromWarehouseId == null) {
            throw exception(WAREHOUSE_NOT_EXISTS);
        }
        // 入库端：累加
        Long toLocationId = item.getToLocationId();
        Long toWarehouseId = getRootWarehouseId(toLocationId);
        if (toWarehouseId == null) {
            throw exception(WAREHOUSE_NOT_EXISTS);
        }

        // 先执行出库扣减
        // 批次/序列号管控产品（stock_mode != 0）调拨入库必须填写批次号
        validateBatchNoRequired(item, wmsProductService.getStockModeBySkuId(item.getSkuId()));

        BigDecimal toDeduct = item.getQuantity();
        LambdaQueryWrapperX<WmsStockDO> outQuery = (LambdaQueryWrapperX<WmsStockDO>) new LambdaQueryWrapperX<WmsStockDO>()
            .eq(WmsStockDO::getWarehouseId, fromWarehouseId)
            .eq(WmsStockDO::getLocationId, fromLocationId)
            .eq(WmsStockDO::getSkuId, item.getSkuId())
            .gt(WmsStockDO::getQuantity, 0)
            .orderByAsc(WmsStockDO::getCreateTime)
            .orderByAsc(WmsStockDO::getExpiryDate);
        if (item.getBatchNo() != null) {
            outQuery.eq(WmsStockDO::getBatchNo, item.getBatchNo());
        }
        List<WmsStockDO> fromStockList = stockMapper.selectList(outQuery);
        if (CollUtil.isEmpty(fromStockList)) {
            throw exception(STOCK_QUANTITY_INSUFFICIENT);
        }
        for (WmsStockDO batch : fromStockList) {
            if (toDeduct.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal reserved = batch.getReservedQuantity() != null ? batch.getReservedQuantity() : BigDecimal.ZERO;
            BigDecimal available = batch.getQuantity().subtract(reserved);
            if (available.compareTo(BigDecimal.ZERO) <= 0) continue;
            BigDecimal deductQty = available.compareTo(toDeduct) >= 0 ? toDeduct : available;
            BigDecimal afterReserved = reserved.subtract(deductQty);
            batch.setQuantity(batch.getQuantity().subtract(deductQty));
            batch.setReservedQuantity(afterReserved);
            stockMapper.updateById(batch);
            toDeduct = toDeduct.subtract(deductQty);
        }
        if (toDeduct.compareTo(BigDecimal.ZERO) > 0) {
            throw exception(STOCK_QUANTITY_INSUFFICIENT);
        }

        // 再执行入库累加
        WmsStockDO toStock = findStockByUniqueKey(toWarehouseId, toLocationId, item);
        BigDecimal beforeQty = toStock != null ? toStock.getQuantity() : BigDecimal.ZERO;
        if (toStock != null) {
            toStock.setQuantity(toStock.getQuantity().add(item.getQuantity()));
            stockMapper.updateById(toStock);
        } else {
            WmsStockDO newStock = new WmsStockDO();
            newStock.setSkuId(item.getSkuId());
            newStock.setWarehouseId(toWarehouseId);
            newStock.setLocationId(toLocationId);
            newStock.setBatchNo(item.getBatchNo());
            newStock.setProductionDate(item.getProductionDate());
            newStock.setExpiryDate(item.getExpiryDate());
            newStock.setQuantity(item.getQuantity());
            stockMapper.insert(newStock);
        }

        // 写入调拨流水
        WmsStockLogDO log = new WmsStockLogDO();
        log.setSkuId(item.getSkuId());
        log.setWarehouseId(fromWarehouseId);
        log.setFromLocationId(fromLocationId);
        log.setToLocationId(toLocationId);
        log.setBatchNo(item.getBatchNo());
        log.setProductionDate(item.getProductionDate());
        log.setExpiryDate(item.getExpiryDate());
        log.setBizType("order");
        log.setBizId(order.getId());
        log.setBizNo(order.getNo());
        log.setBeforeQuantity(beforeQty);
        log.setChangeQuantity(item.getQuantity());
        log.setAfterQuantity(beforeQty.add(item.getQuantity()));
        stockLogMapper.insert(log);
    }

    /**
     * 按联合唯一键查找库存记录
     */
    private WmsStockDO findStockByUniqueKey(Long warehouseId, Long locationId, WmsOrderItemDO item) {
        LambdaQueryWrapperX<WmsStockDO> queryWrapper = new LambdaQueryWrapperX<WmsStockDO>()
            .eq(WmsStockDO::getWarehouseId, warehouseId)
            .eq(WmsStockDO::getLocationId, locationId)
            .eq(WmsStockDO::getSkuId, item.getSkuId());
        if (item.getBatchNo() != null) {
            queryWrapper.eq(WmsStockDO::getBatchNo, item.getBatchNo());
        } else {
            queryWrapper.isNull(WmsStockDO::getBatchNo);
        }
        if (item.getProductionDate() != null) {
            queryWrapper.eq(WmsStockDO::getProductionDate, item.getProductionDate());
        } else {
            queryWrapper.isNull(WmsStockDO::getProductionDate);
        }
        if (item.getExpiryDate() != null) {
            queryWrapper.eq(WmsStockDO::getExpiryDate, item.getExpiryDate());
        } else {
            queryWrapper.isNull(WmsStockDO::getExpiryDate);
        }
        return stockMapper.selectOne(queryWrapper);
    }

    /**
     * 写入库存流水
     */
    private void writeStockLog(WmsOrderItemDO item, WmsOrderDO order, Long warehouseId,
                               Long fromLocationId, Long toLocationId,
                               BigDecimal beforeQty, BigDecimal changeQty, BigDecimal afterQty) {
        WmsStockLogDO log = new WmsStockLogDO();
        log.setSkuId(item.getSkuId());
        log.setWarehouseId(warehouseId);
        log.setFromLocationId(fromLocationId);
        log.setToLocationId(toLocationId);
        log.setBatchNo(item.getBatchNo());
        log.setProductionDate(item.getProductionDate());
        log.setExpiryDate(item.getExpiryDate());
        log.setBizType("order");
        log.setBizId(order.getId());
        log.setBizNo(order.getNo());
        log.setBeforeQuantity(beforeQty);
        log.setChangeQuantity(changeQty);
        log.setAfterQuantity(afterQty);
        stockLogMapper.insert(log);
    }

    /**
     * 查找库位所属的实体仓库根节点（parent_id=0）
     */
    private Long getRootWarehouseId(Long locationId) {
        if (locationId == null) return null;
        WmsWarehouseDO wh = warehouseMapper.selectById(locationId);
        if (wh == null) return null;
        // 虚拟类型（usage >= 4）不参与真实库存
        if (wh.getLocType() != null && wh.getLocType() >= 4) return null;
        // 向上追溯根节点
        Long currentId = wh.getId();
        Set<Long> visited = new HashSet<>();
        while (wh != null && wh.getParentId() != 0) {
            if (!visited.add(currentId)) break;
            currentId = wh.getParentId();
            wh = warehouseMapper.selectById(currentId);
        }
        return wh != null && wh.getParentId() == 0 ? wh.getId() : null;
    }

    @Override
    public void finishOrder(Long id) {
        WmsOrderDO order = validateOrderExists(id);
        if (order.getStatus() != 2) {
            throw exception(ORDER_STATUS_NOT_ALLOW_FINISH);
        }
        WmsOrderDO updateObj = new WmsOrderDO();
        updateObj.setId(id);
        updateObj.setStatus(3);
        orderMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long id) {
        WmsOrderDO order = validateOrderExists(id);
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw exception(ORDER_STATUS_NOT_ALLOW_CANCEL);
        }
        // 已预占则解占库存
        if (order.getStatus() == 1) {
            List<WmsOrderItemDO> items = orderItemMapper.selectList(WmsOrderItemDO::getOrderId, id);
            for (WmsOrderItemDO item : items) {
                unreserveStock(item);
            }
        }
        WmsOrderDO updateObj = new WmsOrderDO();
        updateObj.setId(id);
        updateObj.setStatus(-1);
        orderMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reverseOrder(Long id) {
        // 校验原单据存在且已审核
        WmsOrderDO originalOrder = validateOrderExists(id);
        if (originalOrder.getStatus() != 2) {
            throw exception(ORDER_STATUS_NOT_ALLOW_FINISH); // 仅已审核单据可退货
        }
        // 获取原单据类型
        WmsOrderTypeDO orderType = orderTypeMapper.selectById(originalOrder.getTypeId());
        if (orderType == null) {
            throw exception(ORDER_TYPE_NOT_EXISTS);
        }
        // 获取原单据明细
        List<WmsOrderItemDO> originalItems = orderItemMapper.selectList(
            WmsOrderItemDO::getOrderId, id);
        if (CollUtil.isEmpty(originalItems)) {
            throw exception(ORDER_ITEM_NOT_EXISTS);
        }

        // 创建反向单据头
        WmsOrderDO reverseOrder = new WmsOrderDO();
        reverseOrder.setNo(getNo("WmsOrderNo"));
        reverseOrder.setTypeId(originalOrder.getTypeId());
        reverseOrder.setStatus(0); // 草稿
        reverseOrder.setOrderTime(LocalDateTime.now());
        // 反向来源/去向
        reverseOrder.setFromWarehouseId(originalOrder.getToWarehouseId());
        reverseOrder.setToWarehouseId(originalOrder.getFromWarehouseId());
        // 供应商/客户互换
        reverseOrder.setSupplierId(originalOrder.getCustomerId());
        reverseOrder.setCustomerId(originalOrder.getSupplierId());
        reverseOrder.setOriginId(id); // 记录原单据ID
        reverseOrder.setRemark("退货单，源自单据#" + originalOrder.getNo());
        orderMapper.insert(reverseOrder);

        // 创建反向明细行
        List<WmsOrderItemDO> reverseItems = convertList(originalItems, item -> {
            WmsOrderItemDO newItem = new WmsOrderItemDO();
            newItem.setOrderId(reverseOrder.getId());
            newItem.setSkuId(item.getSkuId());
            // 反向来源/去向
            newItem.setFromLocationId(item.getToLocationId());
            newItem.setToLocationId(item.getFromLocationId());
            newItem.setQuantity(item.getQuantity());
            newItem.setBatchNo(item.getBatchNo());
            newItem.setProductionDate(item.getProductionDate());
            newItem.setExpiryDate(item.getExpiryDate());
            newItem.setUnitPrice(item.getUnitPrice());
            newItem.setRemark("退货，源自明细#" + item.getId());
            return newItem;
        });
        orderItemMapper.insertBatch(reverseItems);

        return reverseOrder.getId();
    }

    // ========== 预占/解占库存 ==========

    /**
     * 预占库存：出库/调拨时增加 reserved_quantity（原子操作，防止并发超预占）
     */
    private void reserveStock(WmsOrderItemDO item) {
        WmsOrderTypeDO orderType = orderTypeMapper.selectById(
            orderMapper.selectById(item.getOrderId()).getTypeId());
        if (orderType == null) return;
        int stockImpact = orderType.getStockImpact();
        if (stockImpact == 1) return; // 入库不预占

        // 出库(2)用 fromLocationId，调拨(3)也用 fromLocationId
        Long locationId = item.getFromLocationId();
        Long warehouseId = getRootWarehouseId(locationId);
        if (warehouseId == null) return;

        BigDecimal toReserve = item.getQuantity();
        // 按 FIFO 顺序查询有库存的记录（补全全部维度匹配）
        LambdaQueryWrapperX<WmsStockDO> queryWrapper = (LambdaQueryWrapperX<WmsStockDO>) new LambdaQueryWrapperX<WmsStockDO>()
            .eq(WmsStockDO::getWarehouseId, warehouseId)
            .eq(WmsStockDO::getLocationId, locationId)
            .eq(WmsStockDO::getSkuId, item.getSkuId())
            .gt(WmsStockDO::getQuantity, 0)
            .orderByAsc(WmsStockDO::getCreateTime)
            .orderByAsc(WmsStockDO::getExpiryDate);
        if (item.getBatchNo() != null) {
            queryWrapper.eq(WmsStockDO::getBatchNo, item.getBatchNo());
        }
        if (item.getProductionDate() != null) {
            queryWrapper.eq(WmsStockDO::getProductionDate, item.getProductionDate());
        }
        if (item.getExpiryDate() != null) {
            queryWrapper.eq(WmsStockDO::getExpiryDate, item.getExpiryDate());
        }
        List<WmsStockDO> stockList = stockMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(stockList)) {
            throw exception(STOCK_RESERVE_FAILED);
        }
        for (WmsStockDO batch : stockList) {
            if (toReserve.compareTo(BigDecimal.ZERO) <= 0) break;
            // 计算当前批次可用量 = quantity - reserved_quantity
            BigDecimal reserved = batch.getReservedQuantity() != null ? batch.getReservedQuantity() : BigDecimal.ZERO;
            BigDecimal available = batch.getQuantity().subtract(reserved);
            if (available.compareTo(BigDecimal.ZERO) <= 0) continue;
            // 本次实际预占量（取 可用量 和 待预占量 的较小值）
            BigDecimal reserveQty = available.compareTo(toReserve) >= 0 ? toReserve : available;
            // 原子预占：数据库层校验 quantity - reserved_quantity >= reserveQty
            int affected = stockMapper.atomicReserve(batch.getId(), reserveQty);
            if (affected > 0) {
                toReserve = toReserve.subtract(reserveQty);
            }
            // affected == 0 说明被并发修改了，跳过该批次继续尝试下一个
        }
        if (toReserve.compareTo(BigDecimal.ZERO) > 0) {
            throw exception(STOCK_RESERVE_FAILED);
        }
    }

    /**
     * 解占库存：取消单据时减少 reserved_quantity（原子操作）
     */
    private void unreserveStock(WmsOrderItemDO item) {
        WmsOrderTypeDO orderType = orderTypeMapper.selectById(
            orderMapper.selectById(item.getOrderId()).getTypeId());
        if (orderType == null) return;
        int stockImpact = orderType.getStockImpact();
        if (stockImpact == 1) return;

        Long locationId = item.getFromLocationId();
        Long warehouseId = getRootWarehouseId(locationId);
        if (warehouseId == null) return;

        BigDecimal toRelease = item.getQuantity();
        LambdaQueryWrapperX<WmsStockDO> queryWrapper = (LambdaQueryWrapperX<WmsStockDO>) new LambdaQueryWrapperX<WmsStockDO>()
            .eq(WmsStockDO::getWarehouseId, warehouseId)
            .eq(WmsStockDO::getLocationId, locationId)
            .eq(WmsStockDO::getSkuId, item.getSkuId())
            .gt(WmsStockDO::getQuantity, 0)
            .orderByAsc(WmsStockDO::getCreateTime)
            .orderByAsc(WmsStockDO::getExpiryDate);
        if (item.getBatchNo() != null) {
            queryWrapper.eq(WmsStockDO::getBatchNo, item.getBatchNo());
        }
        if (item.getProductionDate() != null) {
            queryWrapper.eq(WmsStockDO::getProductionDate, item.getProductionDate());
        }
        if (item.getExpiryDate() != null) {
            queryWrapper.eq(WmsStockDO::getExpiryDate, item.getExpiryDate());
        }
        List<WmsStockDO> stockList = stockMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(stockList)) return;
        for (WmsStockDO batch : stockList) {
            if (toRelease.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal reserved = batch.getReservedQuantity() != null ? batch.getReservedQuantity() : BigDecimal.ZERO;
            if (reserved.compareTo(BigDecimal.ZERO) <= 0) continue;
            BigDecimal releaseQty = reserved.compareTo(toRelease) >= 0 ? toRelease : reserved;
            // 原子解占：数据库层校验 reserved_quantity >= releaseQty
            int affected = stockMapper.atomicRelease(batch.getId(), releaseQty);
            if (affected > 0) {
                toRelease = toRelease.subtract(releaseQty);
            }
        }
    }

}