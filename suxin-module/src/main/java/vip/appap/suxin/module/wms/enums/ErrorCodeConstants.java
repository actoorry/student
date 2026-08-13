package vip.appap.suxin.module.wms.enums;

import vip.appap.suxin.framework.common.exception.ErrorCode;

/**
 * Wms 错误码枚举类
 *
 * wms 系统，使用 1-006-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 仓库 1-006-001-000 ==========
    ErrorCode WAREHOUSE_NOT_EXISTS = new ErrorCode(1_006_001_000, "仓库不存在");
    ErrorCode WAREHOUSE_NAME_DUPLICATE = new ErrorCode(1_006_001_001, "仓库名称已存在");
    ErrorCode WAREHOUSE_CODE_DUPLICATE = new ErrorCode(1_006_001_002, "仓库编码已存在");
    ErrorCode WAREHOUSE_DISABLED = new ErrorCode(1_006_001_003, "仓库已禁用");
    ErrorCode WAREHOUSE_PARENT_NOT_EXISTS = new ErrorCode(1_006_001_004, "父仓库不存在");
    ErrorCode WAREHOUSE_EXISTS_CHILDREN = new ErrorCode(1_006_001_005, "存在子仓库，无法删除");
    ErrorCode WAREHOUSE_AREA_NOT_EXISTS = new ErrorCode(1_006_001_006, "归属城市区县ID不存在，请检查Area表");
    ErrorCode WAREHOUSE_HAS_STOCK = new ErrorCode(1_006_001_007, "该仓库下存在库存记录，无法删除");
    ErrorCode WAREHOUSE_HAS_ORDER = new ErrorCode(1_006_001_008, "该仓库存在关联单据，无法删除");
    ErrorCode WAREHOUSE_PARTNER_NOT_COMPANY = new ErrorCode(1_006_001_009, "仓库负责人仅允许选择类型为【公司】的客商");
    ErrorCode WAREHOUSE_PARTNER_NOT_ALLOW = new ErrorCode(1_006_001_010, "仅一级实体仓库可配置负责人，库区/库位不存储负责人信息");

    // ========== 出入库/调拨单据头 1-006-002-000 ==========
    ErrorCode ORDER_NOT_EXISTS = new ErrorCode(1_006_002_000, "出入库/调拨单据头不存在");
    ErrorCode ORDER_STATUS_NOT_ALLOW_UPDATE = new ErrorCode(1_006_002_001, "单据状态不允许修改");
    ErrorCode ORDER_STATUS_NOT_ALLOW_DELETE = new ErrorCode(1_006_002_002, "单据状态不允许删除");
    ErrorCode ORDER_STATUS_NOT_ALLOW_FINISH = new ErrorCode(1_006_002_003, "单据状态不允许完成");
    ErrorCode ORDER_STATUS_NOT_ALLOW_CANCEL = new ErrorCode(1_006_002_004, "单据状态不允许取消");
    ErrorCode ORDER_STATUS_NOT_ALLOW_SUBMIT = new ErrorCode(1_006_002_005, "单据状态不允许提交");
    ErrorCode ORDER_STATUS_NOT_ALLOW_APPROVE = new ErrorCode(1_006_002_006, "单据状态不允许审核");
    ErrorCode ORDER_TYPE_MISMATCH = new ErrorCode(1_006_002_006, "单据类型与操作不匹配");
    ErrorCode ORDER_NO_GENERATE_FAILED = new ErrorCode(1_006_002_007, "单据编号生成失败");

    // ========== 出入库/调拨单据明细 1-006-003-000 ==========
    ErrorCode ORDER_ITEM_NOT_EXISTS = new ErrorCode(1_006_003_000, "出入库/调拨单据明细不存在");
    ErrorCode ORDER_ITEM_SKU_REQUIRED = new ErrorCode(1_006_003_001, "单据明细必须指定产品SKU");
    ErrorCode ORDER_ITEM_QUANTITY_INVALID = new ErrorCode(1_006_003_002, "单据明细数量必须大于0");
    ErrorCode ORDER_ITEM_LOCATION_REQUIRED = new ErrorCode(1_006_003_003, "单据明细必须指定库位");
    ErrorCode ORDER_ITEM_EXISTS = new ErrorCode(1_006_003_004, "单据明细已存在");
    ErrorCode ORDER_ITEM_BATCH_REQUIRED = new ErrorCode(1_006_003_005, "批次管理产品必须在单据明细中填写批次号");

    // ========== 单据类型 1-006-004-000 ==========
    ErrorCode ORDER_TYPE_NOT_EXISTS = new ErrorCode(1_006_004_000, "单据类型不存在");
    ErrorCode ORDER_TYPE_NAME_DUPLICATE = new ErrorCode(1_006_004_001, "单据类型名称已存在");
    ErrorCode ORDER_TYPE_DISABLED = new ErrorCode(1_006_004_002, "单据类型已禁用");
    ErrorCode ORDER_TYPE_ACTIVE_ORDER_EXISTS = new ErrorCode(1_006_004_003, "该单据类型下存在活跃单据，无法禁用");

    // ========== 库存 1-006-005-000 ==========
    ErrorCode STOCK_NOT_EXISTS = new ErrorCode(1_006_005_000, "库存记录不存在");
    ErrorCode STOCK_QUANTITY_INSUFFICIENT = new ErrorCode(1_006_005_001, "库存数量不足");
    ErrorCode STOCK_LOCK_FAILED = new ErrorCode(1_006_005_002, "库存锁定失败");
    ErrorCode STOCK_UNLOCK_FAILED = new ErrorCode(1_006_005_003, "库存解锁失败");
    ErrorCode STOCK_DEDUCT_FAILED = new ErrorCode(1_006_005_004, "库存扣减失败");
    ErrorCode STOCK_REVERT_FAILED = new ErrorCode(1_006_005_005, "库存回退失败");
    ErrorCode STOCK_RESERVE_FAILED = new ErrorCode(1_006_005_006, "库存预占失败，可用量不足");

    // ========== 库存流水 1-006-006-000 ==========
    ErrorCode STOCK_LOG_NOT_EXISTS = new ErrorCode(1_006_006_000, "库存流水不存在");
    ErrorCode STOCK_LOG_TYPE_INVALID = new ErrorCode(1_006_006_001, "库存流水业务类型无效");

    // ========== 盘点调整 1-006-007-000 ==========
    ErrorCode INVENTORY_ADJUST_NOT_EXISTS = new ErrorCode(1_006_007_000, "盘点调整记录不存在");
    ErrorCode INVENTORY_ADJUST_STATUS_NOT_ALLOW_UPDATE = new ErrorCode(1_006_007_001, "盘点调整状态不允许修改");

}