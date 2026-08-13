package vip.appap.suxin.module.sales.enums;

import vip.appap.suxin.framework.common.exception.ErrorCode;

/**
 * Trade 错误码枚举类
 * trade 系统，使用 1-011-000-000 段
 *
 * @author LeeYan9
 * @since 2022-08-26
 */
public interface ErrorCodeConstants {

    // ========== Order 模块 1-011-000-000 ==========
    ErrorCode ORDER_ITEM_NOT_FOUND = new ErrorCode(1_011_000_010, "交易订单项不存在");
    ErrorCode ORDER_NOT_FOUND = new ErrorCode(1_011_000_011, "交易订单不存在");
    ErrorCode ORDER_ITEM_UPDATE_AFTER_SALE_STATUS_FAIL = new ErrorCode(1_011_000_012, "交易订单项更新售后状态失败，请重试");
    ErrorCode ORDER_UPDATE_PAID_STATUS_NOT_UNPAID = new ErrorCode(1_011_000_013, "交易订单更新支付状态失败，订单不是【未支付】状态");
    ErrorCode ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR = new ErrorCode(1_011_000_014, "交易订单更新支付状态失败，支付单编号不匹配");
    ErrorCode ORDER_UPDATE_PAID_FAIL_PAY_ORDER_STATUS_NOT_SUCCESS = new ErrorCode(1_011_000_015, "交易订单更新支付状态失败，支付单状态不是【支付成功】状态");
    ErrorCode ORDER_UPDATE_PAID_FAIL_PAY_PRICE_NOT_MATCH = new ErrorCode(1_011_000_016, "交易订单更新支付状态失败，支付单金额不匹配");
    ErrorCode ORDER_DELIVERY_FAIL_STATUS_NOT_UNDELIVERED = new ErrorCode(1_011_000_017, "交易订单发货失败，订单不是【待发货】状态");
    ErrorCode ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED = new ErrorCode(1_011_000_018, "交易订单收货失败，订单不是【待收货】状态");
    ErrorCode ORDER_COMMENT_FAIL_STATUS_NOT_COMPLETED = new ErrorCode(1_011_000_019, "创建交易订单项的评价失败，订单不是【已完成】状态");
    ErrorCode ORDER_COMMENT_STATUS_NOT_FALSE = new ErrorCode(1_011_000_020, "创建交易订单项的评价失败，订单已评价");
    ErrorCode ORDER_DELIVERY_FAIL_REFUND_STATUS_NOT_NONE = new ErrorCode(1_011_000_021, "交易订单发货失败，订单已退款或部分退款");
    ErrorCode ORDER_DELIVERY_FAIL_COMBINATION_RECORD_STATUS_NOT_SUCCESS = new ErrorCode(1_011_000_022, "交易订单发货失败，拼团未成功");
    ErrorCode ORDER_DELIVERY_FAIL_BARGAIN_RECORD_STATUS_NOT_SUCCESS = new ErrorCode(1_011_000_023, "交易订单发货失败，砍价未成功");
    ErrorCode ORDER_DELIVERY_FAIL_DELIVERY_TYPE_NOT_EXPRESS = new ErrorCode(1_011_000_024, "交易订单发货失败，发货类型不是快递");
    ErrorCode ORDER_DELIVERY_FAIL_LOGISTICS_NO_BLANK = new ErrorCode(1_011_000_045, "交易订单发货失败，请填写快递单号");
    ErrorCode ORDER_CANCEL_FAIL_STATUS_NOT_UNPAID = new ErrorCode(1_011_000_025, "交易订单取消失败，订单不是【待支付】状态");
    ErrorCode ORDER_UPDATE_PRICE_FAIL_PAID = new ErrorCode(1_011_000_026, "支付订单调价失败，原因：支付订单已付款,不能调价");
    ErrorCode ORDER_UPDATE_PRICE_FAIL_ALREADY = new ErrorCode(1_011_000_027, "支付订单调价失败，原因：已经修改过价格");
    ErrorCode ORDER_UPDATE_PRICE_FAIL_PRICE_ERROR = new ErrorCode(1_011_000_028, "支付订单调价失败，原因：调整后支付价格不能小于 0.01 元");
    ErrorCode ORDER_DELETE_FAIL_STATUS_NOT_CANCEL = new ErrorCode(1_011_000_029, "交易订单删除失败，订单不是【已取消】状态");
    ErrorCode ORDER_MIXED_SERVICE_PHYSICAL_UNSUPPORTED = new ErrorCode(1_011_000_040, "服务商品与实物商品不能混合下单，请拆单购买");
    ErrorCode ORDER_VIRTUAL_PRODUCT_PAY_APP_CONFLICT = new ErrorCode(1_011_000_041, "订单内虚拟商品的支付应用配置冲突，请拆单购买");
    ErrorCode ORDER_VIRTUAL_PRODUCT_HANDLER_NOT_FOUND = new ErrorCode(1_011_000_042, "未找到虚拟商品处理器({})");
    ErrorCode ORDER_DELIVERY_TYPES_INCOMPATIBLE = new ErrorCode(1_011_000_043, "所选商品没有共同配送方式，请拆单购买");
    ErrorCode ORDER_DELIVERY_TYPE_REQUIRED = new ErrorCode(1_011_000_044, "存在多个可用配送方式，请选择配送方式");
    ErrorCode ORDER_RECEIVE_FAIL_DELIVERY_TYPE_NOT_PICK_UP = new ErrorCode(1_011_000_030, "交易订单自提失败，收货方式不是【用户自提】");
    ErrorCode ORDER_UPDATE_ADDRESS_FAIL_STATUS_NOT_DELIVERED = new ErrorCode(1_011_000_031, "交易订单修改收货地址失败，原因：订单不是【待发货】状态");
    ErrorCode ORDER_CREATE_FAIL_EXIST_UNPAID = new ErrorCode(1_011_000_032, "交易订单创建失败，原因：存在未付款订单");
    ErrorCode ORDER_CANCEL_PAID_FAIL = new ErrorCode(1_011_000_033, "交易订单取消支付失败，原因：订单不是【{}】状态");
    ErrorCode ORDER_UPDATE_PAID_ORDER_REFUNDED_FAIL_REFUND_NOT_FOUND = new ErrorCode(1_011_000_034, "交易订单更新支付订单退款状态失败，原因：退款单不存在");
    ErrorCode ORDER_UPDATE_PAID_ORDER_REFUNDED_FAIL_REFUND_STATUS_NOT_SUCCESS = new ErrorCode(1_011_000_035, "交易订单更新支付订单退款状态失败，原因：退款单状态不是【退款成功】");
    ErrorCode ORDER_PICK_UP_FAIL_NOT_VERIFY_USER = new ErrorCode(1_011_000_036, "交易订单自提失败，原因：你没有核销该门店订单的权限");
    ErrorCode ORDER_PICK_UP_FAIL_COMBINATION_NOT_SUCCESS = new ErrorCode(1_011_000_037, "交易订单自提失败，原因：商品拼团记录不是【成功】状态");
    ErrorCode ORDER_CREATE_FAIL_INSUFFICIENT_USER_POINTS = new ErrorCode(1_011_000_038, "交易订单创建失败，原因：用户积分不足");
    ErrorCode ORDER_PICK_UP_FAIL_STATUS_NOT_UNDELIVERED = new ErrorCode(1_011_000_039, "交易订单自提失败，订单不是【待核销】状态");

    // ========== After Sale 模块 1-011-000-100 ==========
    ErrorCode AFTER_SALE_NOT_FOUND = new ErrorCode(1_011_000_100, "售后单不存在");
    ErrorCode AFTER_SALE_CREATE_FAIL_REFUND_PRICE_ERROR = new ErrorCode(1_011_000_101, "申请退款金额错误");
    ErrorCode AFTER_SALE_CREATE_FAIL_ORDER_STATUS_CANCELED = new ErrorCode(1_011_000_102, "订单已关闭，无法申请售后");
    ErrorCode AFTER_SALE_CREATE_FAIL_ORDER_STATUS_NO_PAID = new ErrorCode(1_011_000_103, "订单未支付，无法申请售后");
    ErrorCode AFTER_SALE_CREATE_FAIL_ORDER_STATUS_NO_DELIVERED = new ErrorCode(1_011_000_104, "订单未发货，无法申请【退货退款】售后");
    ErrorCode AFTER_SALE_CREATE_FAIL_ORDER_ITEM_APPLIED = new ErrorCode(1_011_000_105, "订单项已申请售后，无法重复申请");
    ErrorCode AFTER_SALE_AUDIT_FAIL_STATUS_NOT_APPLY = new ErrorCode(1_011_000_106, "审批失败，售后状态不处于审批中");
    ErrorCode AFTER_SALE_UPDATE_STATUS_FAIL = new ErrorCode(1_011_000_107, "操作售后单失败，请刷新后重试");
    ErrorCode AFTER_SALE_DELIVERY_FAIL_STATUS_NOT_SELLER_AGREE = new ErrorCode(1_011_000_108, "退货失败，售后单状态不处于【待买家退货】");
    ErrorCode AFTER_SALE_CONFIRM_FAIL_STATUS_NOT_BUYER_DELIVERY = new ErrorCode(1_011_000_109, "确认收货失败，售后单状态不处于【待确认收货】");
    ErrorCode AFTER_SALE_REFUND_FAIL_STATUS_NOT_WAIT_REFUND = new ErrorCode(1_011_000_110, "退款失败，售后单状态不是【待退款】");
    ErrorCode AFTER_SALE_REFUND_FAIL_REFUND_NOT_FOUND = new ErrorCode(1_011_000_111, "退款失败，退款单不存在");
    ErrorCode AFTER_SALE_REFUND_FAIL_REFUND_NOT_SUCCESS_OR_FAILURE = new ErrorCode(1_011_000_112, "退款失败，退款单未退款");
    ErrorCode AFTER_SALE_REFUND_FAIL_REFUND_PRICE_NOT_MATCH = new ErrorCode(1_011_000_113, "退款失败，退款金额不匹配");
    ErrorCode AFTER_SALE_REFUND_FAIL_REFUND_ORDER_ID_ERROR = new ErrorCode(1_011_000_114, "退款失败，退款单不匹配");
    ErrorCode AFTER_SALE_CANCEL_FAIL_STATUS_NOT_APPLY_OR_AGREE_OR_BUYER_DELIVERY =
            new ErrorCode(1_011_000_115, "取消售后单失败，售后单状态不是【待审核】或【卖家同意】或【商家待收货】");
    ErrorCode AFTER_SALE_CREATE_FAIL_ORDER_STATUS_COMBINATION_IN_PROGRESS = new ErrorCode(1_011_000_116, "订单拼团中，无法申请售后");
    ErrorCode AFTER_SALE_WECHAT_VIRTUAL_REFUND_UNSUPPORTED = new ErrorCode(1_011_000_117,
            "微信虚拟支付订单首期不支持普通售后退款，请联系客服通过微信平台工单处理");
    ErrorCode AFTER_SALE_DELIVERY_FAIL_LOGISTICS_NO_BLANK = new ErrorCode(1_011_000_118, "退货失败，请填写物流单号");

    // ========== Cart 模块 1-011-002-000 ==========
    ErrorCode CART_ITEM_NOT_FOUND = new ErrorCode(1_011_002_000, "购物车项不存在");
    ErrorCode CART_WECHAT_VIRTUAL_GOODS_NOT_ALLOWED = new ErrorCode(1_011_002_001,
            "微信小程序虚拟商品不能加入购物车，请直接购买");

    // ========== Price 相关 1-011-003-000 ============
    ErrorCode PRICE_CALCULATE_PAY_PRICE_ILLEGAL = new ErrorCode(1_011_003_000, "支付价格计算异常，原因：价格小于等于 0");
    ErrorCode PRICE_CALCULATE_POINT_GIVE_OVERFLOW = new ErrorCode(1_011_003_007, "赠送积分计算异常，原因：赠送积分超过最大允许值");
    ErrorCode PRICE_CALCULATE_DELIVERY_PRICE_TEMPLATE_NOT_FOUND = new ErrorCode(1_011_003_001, "计算快递运费异常，找不到对应的运费模板");
    ErrorCode PRICE_CALCULATE_COUPON_NOT_MATCH_NORMAL_ORDER = new ErrorCode(1_011_003_002, "参与秒杀、拼团、砍价的营销商品，无法使用优惠劵");
    ErrorCode PRICE_CALCULATE_SECKILL_TOTAL_LIMIT_COUNT = new ErrorCode(1_011_003_003, "参与秒杀的商品，超过了秒杀总限购数量");
    ErrorCode PRICE_CALCULATE_POINT_TOTAL_LIMIT_COUNT = new ErrorCode(1_011_003_004, "参与积分活动的商品，超过了积分活动商品总限购数量");
    ErrorCode PRICE_CALCULATE_DELIVERY_PRICE_TYPE_ILLEGAL = new ErrorCode(1_011_003_005, "计算快递运费异常，配送方式不匹配");
    ErrorCode PRICE_CALCULATE_COUPON_CAN_NOT_USE = new ErrorCode(1_011_003_006, "该优惠劵无法使用，原因：{}」");

    // 失败关闭：禁止缺失模板/规则或非法参数后按零运费继续
    ErrorCode EXPRESS_DELIVERY_TEMPLATE_MISSING = new ErrorCode(1_011_003_008, "计算快递运费异常，找不到 templateId({}) 对应的运费模板配置");
    ErrorCode EXPRESS_DELIVERY_CHARGE_RULE_NOT_FOUND = new ErrorCode(1_011_003_009, "计算快递运费异常，找不到计费规则");
    ErrorCode EXPRESS_DELIVERY_CHARGE_MODE_ILLEGAL = new ErrorCode(1_011_003_010, "计算快递运费异常，计费模式({})不合法");
    ErrorCode EXPRESS_DELIVERY_METRIC_MISSING = new ErrorCode(1_011_003_011, "计算快递运费异常，缺少商品重量或体积");
    ErrorCode EXPRESS_DELIVERY_CHARGE_VALUE_NOT_POSITIVE = new ErrorCode(1_011_003_012, "计算快递运费异常，总计费量必须大于 0");
    ErrorCode EXPRESS_DELIVERY_CHARGE_PARAM_NOT_POSITIVE = new ErrorCode(1_011_003_013, "计算快递运费异常，计费参数必须大于 0");

    // ========== 物流 Express 模块 1-011-004-000 ==========
    ErrorCode EXPRESS_NOT_EXISTS = new ErrorCode(1_011_004_000, "快递公司不存在");
    ErrorCode EXPRESS_CODE_DUPLICATE = new ErrorCode(1_011_004_001, "已经存在该编码的快递公司");
    ErrorCode EXPRESS_CLIENT_NOT_PROVIDE = new ErrorCode(1_011_004_002, "需要接入快递服务商，比如【快递100】");
    ErrorCode EXPRESS_STATUS_NOT_ENABLE = new ErrorCode(1_011_004_003, "快递公司未启用");
    ErrorCode EXPRESS_REFERENCED = new ErrorCode(1_011_004_004, "快递公司已被订单或售后使用，不能删除，请改为停用");

    ErrorCode EXPRESS_API_QUERY_ERROR = new ErrorCode(1_011_004_101, "快递查询接口异常");
    ErrorCode EXPRESS_API_QUERY_FAILED = new ErrorCode(1_011_004_102, "快递查询返回失败，原因：{}");

    // ========== 电子面单账户 模块 1-011-004-200 ==========
    ErrorCode WAYBILL_ACCOUNT_NOT_EXISTS = new ErrorCode(1_011_004_200, "电子面单账户不存在");
    ErrorCode WAYBILL_ACCOUNT_NAME_DUPLICATE = new ErrorCode(1_011_004_201, "已经存在该名称的电子面单账户");
    ErrorCode WAYBILL_ACCOUNT_STATUS_NOT_ENABLE = new ErrorCode(1_011_004_202, "电子面单账户未启用");
    ErrorCode WAYBILL_ACCOUNT_EXPRESS_NOT_ENABLE = new ErrorCode(1_011_004_203, "电子面单账户关联的快递公司未启用");
    ErrorCode WAYBILL_ACCOUNT_EXPRESS_NOT_MATCH = new ErrorCode(1_011_004_204, "订单配送快递公司与该电子面单账户不匹配");
    ErrorCode WAYBILL_ACCOUNT_ADDRESS_NOT_EXISTS = new ErrorCode(1_011_004_205, "电子面单账户默认寄件地址不存在");
    ErrorCode WAYBILL_ACCOUNT_ADDRESS_INCOMPLETE = new ErrorCode(1_011_004_206, "寄件地址信息不完整，请补齐姓名、手机号、地区与详细地址");
    ErrorCode WAYBILL_ACCOUNT_TEMPLATE_REQUIRED = new ErrorCode(1_011_004_207, "电子面单账户缺少快递100模板 ID");
    ErrorCode WAYBILL_ACCOUNT_ADDRESS_NOT_VISIBLE = new ErrorCode(1_011_004_208, "寄件地址对当前租户不可见，无法使用");
    ErrorCode WAYBILL_ACCOUNT_CREDENTIAL_MISSING = new ErrorCode(1_011_004_209, "电子面单账户缺少平台授权 key/密钥，无法下单");
    ErrorCode WAYBILL_ACCOUNT_REFERENCED = new ErrorCode(1_011_004_210, "电子面单账户已被有效面单引用，无法删除");

    // ========== 电子面单订单记录 模块 1-011-004-300 ==========
    ErrorCode WAYBILL_ORDER_NOT_FOUND = new ErrorCode(1_011_004_300, "电子面单记录不存在");
    ErrorCode WAYBILL_ORDER_STATUS_NOT_VALID = new ErrorCode(1_011_004_301, "电子面单不是有效状态，无法执行该操作");
    ErrorCode WAYBILL_ORDER_VALID_EXISTS = new ErrorCode(1_011_004_302, "该订单已存在有效电子面单，请勿重复下单");
    ErrorCode WAYBILL_ORDER_API_ERROR = new ErrorCode(1_011_004_303, "快递100电子面单接口调用异常");
    ErrorCode WAYBILL_ORDER_CREATE_FAILED = new ErrorCode(1_011_004_304, "快递100电子面单下单失败，原因：{}");
    ErrorCode WAYBILL_ORDER_REPRINT_FAIL = new ErrorCode(1_011_004_305, "电子面单复打失败，原因：{}");
    ErrorCode WAYBILL_ORDER_CANCEL_FAIL = new ErrorCode(1_011_004_306, "电子面单取消失败，原因：{}");
    ErrorCode WAYBILL_ORDER_CANCEL_FAIL_STATUS_NOT_VALID = new ErrorCode(1_011_004_307, "仅可取消有效电子面单");
    ErrorCode WAYBILL_ORDER_DELIVERY_OVERWRITE_FORBIDDEN = new ErrorCode(1_011_004_308, "订单已存在有效电子面单，禁止手工单号覆盖发货");
    ErrorCode WAYBILL_ORDER_PERSIST_FAIL = new ErrorCode(1_011_004_309, "电子面单本地持久化失败，请勿重复下单并联系管理员对账");
    ErrorCode WAYBILL_ORDER_DELIVERY_TYPE_NOT_EXPRESS = new ErrorCode(1_011_004_310, "仅快递配送订单可生成电子面单");
    ErrorCode WAYBILL_ORDER_CANCEL_FAIL_STATUS_NOT_DELIVERED = new ErrorCode(1_011_004_311, "电子面单取消失败，订单不是【已发货】状态，无法取消");
    ErrorCode WAYBILL_ORDER_RESULT_UNKNOWN = new ErrorCode(1_011_004_312,
            "快递100电子面单结果未知；48 小时内可按原订单号重试，超时后请先人工对账");

    // ========== 物流 Template 模块 1-011-005-000 ==========
    ErrorCode EXPRESS_TEMPLATE_NAME_DUPLICATE = new ErrorCode(1_011_005_000, "已经存在该运费模板名");
    ErrorCode EXPRESS_TEMPLATE_NOT_EXISTS = new ErrorCode(1_011_005_001, "运费模板不存在");
    ErrorCode EXPRESS_TEMPLATE_REFERENCED_BY_SPU = new ErrorCode(1_011_005_002, "运费模板已被商品使用，不能删除，请先换绑模板或移除商品的快递配送");
    ErrorCode EXPRESS_TEMPLATE_CHARGE_AREA_DUPLICATE = new ErrorCode(1_011_005_003, "运费模板计费规则区域不能重复");
    ErrorCode EXPRESS_TEMPLATE_FREE_AREA_DUPLICATE = new ErrorCode(1_011_005_004, "运费模板包邮规则区域不能重复");
    ErrorCode EXPRESS_TEMPLATE_NOT_COMPUTABLE = new ErrorCode(1_011_005_005, "运费模板不可用于快递结算，请至少配置一条计费规则");

    // ==========  物流 PICK_UP 模块 1-011-006-000 ==========
    ErrorCode PICK_UP_STORE_NOT_EXISTS = new ErrorCode(1_011_006_000, "自提门店不存在");
    ErrorCode PICK_UP_STORE_STAFF_NOT_EXISTS = new ErrorCode(1_011_006_000, "自提门店店员不存在");

    // ========== 分销用户 模块 1-011-007-000 ==========
    ErrorCode BROKERAGE_USER_NOT_EXISTS = new ErrorCode(1_011_007_000, "分销用户不存在");
    ErrorCode BROKERAGE_USER_FROZEN_PRICE_NOT_ENOUGH = new ErrorCode(1_011_007_001, "用户冻结佣金({})数量不足");
    ErrorCode BROKERAGE_BIND_SELF = new ErrorCode(1_011_007_002, "不能绑定自己");
    ErrorCode BROKERAGE_BIND_USER_NOT_ENABLED = new ErrorCode(1_011_007_003, "绑定用户没有推广资格");
    ErrorCode BROKERAGE_BIND_CONDITION_ADMIN = new ErrorCode(1_011_007_004, "仅可在后台绑定推广员");
    ErrorCode BROKERAGE_BIND_MODE_REGISTER = new ErrorCode(1_011_007_005, "只有在注册时可以绑定");
    ErrorCode BROKERAGE_BIND_OVERRIDE = new ErrorCode(1_011_007_006, "已绑定了推广人");
    ErrorCode BROKERAGE_BIND_LOOP = new ErrorCode(1_011_007_007, "下级不能绑定自己的上级");
    ErrorCode BROKERAGE_USER_LEVEL_NOT_SUPPORT = new ErrorCode(1_011_007_008, "目前只支持 level 小于等于 2");
    ErrorCode BROKERAGE_CREATE_USER_EXISTS = new ErrorCode(1_011_007_009, "分销用户已存在");
    ErrorCode BROKERAGE_POSTER_CONFIG_NOT_EXISTS = new ErrorCode(1_011_007_010, "请先配置分销海报背景图");
    ErrorCode BROKERAGE_ENABLED_GLOBAL = new ErrorCode(1_011_007_011, "分销功能暂未开放");

    // ========== 分销提现 模块 1-011-008-000 ==========
    ErrorCode BROKERAGE_WITHDRAW_NOT_EXISTS = new ErrorCode(1_011_008_000, "佣金提现记录不存在");
    ErrorCode BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING = new ErrorCode(1_011_008_001, "佣金提现记录状态不是审核中");
    ErrorCode BROKERAGE_WITHDRAW_MIN_PRICE = new ErrorCode(1_011_008_002, "提现金额不能低于 {} 元");
    ErrorCode BROKERAGE_WITHDRAW_USER_BALANCE_NOT_ENOUGH = new ErrorCode(1_011_008_003, "您当前最多可提现 {} 元");
    ErrorCode BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_TRANSFER_ID_ERROR = new ErrorCode(1_011_008_005, "提现单更新转账状态失败，转账单不匹配");
    ErrorCode BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_TRANSFER_STATUS_NOT_SUCCESS_OR_CLOSED = new ErrorCode(1_011_008_006, "提现单更新转账状态失败，转账单状态不是成功或关闭状态");
    ErrorCode BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_PRICE_NOT_MATCH = new ErrorCode(1_011_008_007, "提现单更新转账状态失败，转账单金额不匹配");
    ErrorCode BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_MERCHANT_EXISTS = new ErrorCode(1_011_008_008, "提现单更新转账状态失败，转账单的商户订单不匹配");
    ErrorCode BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_CHANNEL_NOT_MATCH = new ErrorCode(1_011_008_009, "提现单更新转账状态失败，转账渠道不匹配");

}
