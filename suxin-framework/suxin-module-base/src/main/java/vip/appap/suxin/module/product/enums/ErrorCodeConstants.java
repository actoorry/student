package vip.appap.suxin.module.product.enums;

import vip.appap.suxin.framework.common.exception.ErrorCode;

/**
 * Product 错误码枚举类
 *
 * product 系统，使用 1-008-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 商品分类相关 1-008-001-000 ============
    ErrorCode CATEGORY_NOT_EXISTS = new ErrorCode(1_008_001_000, "商品分类不存在");
    ErrorCode CATEGORY_PARENT_NOT_EXISTS = new ErrorCode(1_008_001_001, "产品父分类不存在");
    ErrorCode CATEGORY_PARENT_NOT_FIRST_LEVEL = new ErrorCode(1_008_001_002, "父分类不能是二级分类");
    ErrorCode CATEGORY_EXISTS_CHILDREN = new ErrorCode(1_008_001_003, "存在子分类，无法删除");
    ErrorCode CATEGORY_DISABLED = new ErrorCode(1_008_001_004, "商品分类({})已禁用，无法使用");
    ErrorCode CATEGORY_HAVE_BIND_SPU = new ErrorCode(1_008_001_005, "类别下存在商品，无法删除");
    ErrorCode CATEGORY_ROOT_NOT_ALLOWED = new ErrorCode(1_008_001_006, "商品分类必须归属销售分类或仓储分类");
    ErrorCode CATEGORY_VIRTUAL_ROOT_IMMUTABLE = new ErrorCode(1_008_001_007, "销售分类和仓储分类为系统维度，不能修改");
    ErrorCode CATEGORY_PARENT_CYCLE = new ErrorCode(1_008_001_008, "商品分类父级不能是自身或其子分类");
    ErrorCode CATEGORY_ANCESTRY_INVALID = new ErrorCode(1_008_001_009, "商品分类层级存在缺失父级或循环");
    ErrorCode CATEGORY_DIMENSION_INVALID = new ErrorCode(1_008_001_010, "商品分类不属于指定业务维度");

    // ========== 商品品牌相关编号 1-008-002-000 ==========
    ErrorCode BRAND_NOT_EXISTS = new ErrorCode(1_008_002_000, "品牌不存在");
    ErrorCode BRAND_DISABLED = new ErrorCode(1_008_002_001, "品牌已禁用");
    ErrorCode BRAND_NAME_EXISTS = new ErrorCode(1_008_002_002, "品牌名称已存在");

    // ========== 商品属性项 1-008-003-000 ==========
    ErrorCode PROPERTY_NOT_EXISTS = new ErrorCode(1_008_003_000, "属性项不存在");
    ErrorCode PROPERTY_EXISTS = new ErrorCode(1_008_003_001, "属性项的名称已存在");
    ErrorCode PROPERTY_DELETE_FAIL_VALUE_EXISTS = new ErrorCode(1_008_003_002, "属性项下存在属性值，无法删除");

    // ========== 商品属性值 1-008-004-000 ==========
    ErrorCode PROPERTY_VALUE_NOT_EXISTS = new ErrorCode(1_008_004_000, "属性值不存在");
    ErrorCode PROPERTY_VALUE_EXISTS = new ErrorCode(1_008_004_001, "属性值的名称已存在");

    // ========== 商品 SPU 1-008-005-000 ==========
    ErrorCode SPU_NOT_EXISTS = new ErrorCode(1_008_005_000, "商品 SPU 不存在");
    ErrorCode SPU_SAVE_FAIL_CATEGORY_LEVEL_ERROR = new ErrorCode(1_008_005_001, "商品分类不正确，原因：必须使用第二级的商品分类及以下");
    ErrorCode SPU_SAVE_FAIL_COUPON_TEMPLATE_NOT_EXISTS = new ErrorCode(1_008_005_002, "商品 SPU 保存失败，原因：优惠劵不存在");
    ErrorCode SPU_NOT_ENABLE = new ErrorCode(1_008_005_003, "商品 SPU【{}】不处于上架状态");
    ErrorCode SPU_NOT_RECYCLE = new ErrorCode(1_008_005_004, "商品 SPU 不处于回收站状态");
    ErrorCode SPU_DELIVERY_TEMPLATE_REQUIRED = new ErrorCode(1_008_005_006, "快递发货商品必须选择物流配置模板");
    ErrorCode SPU_DELIVERY_TYPE_INVALID = new ErrorCode(1_008_005_007, "配送方式不正确");
    ErrorCode SPU_WECHAT_VIRTUAL_FLAG_IMMUTABLE = new ErrorCode(1_008_005_008,
            "是否微信小程序虚拟商品创建后不可修改，请回收原商品后重新创建");
    ErrorCode SPU_WECHAT_VIRTUAL_SKU_NOT_READY = new ErrorCode(1_008_005_009,
            "微信虚拟商品无法上架，SKU 状态未就绪：{}");
    ErrorCode SPU_TYPE_INVALID = new ErrorCode(1_008_005_010, "产品类型不正确");
    ErrorCode SPU_PROVINCE_CITY_CONFLICT = new ErrorCode(1_008_005_011, "全国特产省份与同城城市筛选不能同时传入");
    ErrorCode SPU_PROVINCE_NOT_EXISTS = new ErrorCode(1_008_005_012, "省份不存在");

    // ========== 商品 SKU 1-008-006-000 ==========
    ErrorCode SKU_NOT_EXISTS = new ErrorCode(1_008_006_000, "商品 SKU 不存在");
    ErrorCode SKU_PROPERTIES_DUPLICATED = new ErrorCode(1_008_006_001, "商品 SKU 的属性组合存在重复");
    ErrorCode SPU_ATTR_NUMBERS_MUST_BE_EQUALS = new ErrorCode(1_008_006_002, "一个 SPU 下的每个 SKU，其属性项必须一致");
    ErrorCode SPU_SKU_NOT_DUPLICATE = new ErrorCode(1_008_006_003, "一个 SPU 下的每个 SKU，必须不重复");
    ErrorCode SKU_STOCK_NOT_ENOUGH = new ErrorCode(1_008_006_004, "商品 SKU 库存不足");
    ErrorCode SKU_QUANTITY_REQUIRED = new ErrorCode(1_008_006_005, "虚拟/会员/微信小程序虚拟商品 SKU 主单位数量必须大于 0");
    ErrorCode WECHAT_VIRTUAL_GOODS_UNSUPPORTED_PRODUCT_TYPE = new ErrorCode(1_008_006_006,
            "该商品未标记为微信小程序虚拟商品");
    ErrorCode WECHAT_VIRTUAL_GOODS_CHANNEL_CONFIG_ERROR = new ErrorCode(1_008_006_007, "微信虚拟支付渠道配置错误");
    ErrorCode WECHAT_VIRTUAL_GOODS_API_ERROR = new ErrorCode(1_008_006_008, "调用微信虚拟支付道具接口失败：{}");
    ErrorCode WECHAT_VIRTUAL_GOODS_CHANNEL_AMBIGUOUS = new ErrorCode(1_008_006_009, "存在多个启用的微信虚拟支付渠道，请指定支付应用编号");
    ErrorCode WECHAT_VIRTUAL_GOODS_CHANNEL_APP_ID_MISMATCH = new ErrorCode(1_008_006_010, "微信虚拟支付渠道 AppID 与小程序社交客户端不一致");
    ErrorCode WECHAT_VIRTUAL_GOODS_SKU_DATA_INVALID = new ErrorCode(1_008_006_011, "商品 SKU [{}] 数据不完整：{}");
    ErrorCode WECHAT_VIRTUAL_GOODS_UPLOAD_NOT_SUCCESS = new ErrorCode(1_008_006_012, "商品 SKU [{}] 尚未同步成功，请先同步或刷新");
    ErrorCode WECHAT_VIRTUAL_GOODS_ALREADY_PROCESSING = new ErrorCode(1_008_006_013, "商品 SKU [{}] 正在处理中，请勿重复操作");
    ErrorCode WECHAT_VIRTUAL_GOODS_QUERY_NO_TASK = new ErrorCode(1_008_006_014, "当前没有运行中的微信虚拟支付任务");

    // ========== 商品 评价 1-008-007-000 ==========
    ErrorCode COMMENT_NOT_EXISTS = new ErrorCode(1_008_007_000, "商品评价不存在");
    ErrorCode COMMENT_ORDER_EXISTS = new ErrorCode(1_008_007_001, "订单的商品评价已存在");

    // ========== 产品单位 1-008-008-000 ==========
    ErrorCode UNIT_NOT_EXISTS = new ErrorCode(1_008_008_000, "产品单位不存在");
    ErrorCode UNIT_NAME_EXISTS = new ErrorCode(1_008_008_001, "产品单位名称已存在");
    ErrorCode UNIT_RELATIVE_FACTOR_INVALID = new ErrorCode(1_008_008_002, "产品单位换算系数必须大于 0");
    ErrorCode UNIT_BASE_UNIT_NOT_EDITABLE = new ErrorCode(1_008_008_005, "基础单位由系统维护，不能新增、修改或删除");

    // ========== 商品 收藏 1-008-009-000 ==========
    ErrorCode FAVORITE_EXISTS = new ErrorCode(1_008_009_000, "该商品已经被收藏");
    ErrorCode FAVORITE_NOT_EXISTS = new ErrorCode(1_008_009_001, "商品收藏不存在");

    // ========== 商品展示配置 1-008-010-000 ==========
    ErrorCode DISPLAY_CONFIG_SCENE_NOT_SUPPORTED = new ErrorCode(1_008_010_000, "展示场景编码不支持");
    ErrorCode DISPLAY_CONFIG_CATEGORY_INVALID = new ErrorCode(1_008_010_001, "商品销售分类无效，请检查是否存在、已启用且属于销售分类");
    ErrorCode DISPLAY_CONFIG_STALE_UPDATE = new ErrorCode(1_008_010_002, "展示配置已被他人修改，请刷新后重试");
    ErrorCode DISPLAY_CONFIG_CONCURRENT_CREATE = new ErrorCode(1_008_010_003, "展示配置已被他人创建，请刷新后重试");

}
