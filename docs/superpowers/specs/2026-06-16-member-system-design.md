# 会员系统设计方案

> 日期: 2026-06-16
> 状态: 可进入实施计划拆解

## 1. 背景与目标

### 现状

- `partner` 表有 `is_member` 布尔字段，仅表示"是否为注册用户"，不代表付费会员。
- `partner_level` 是经验值等级体系，与付费会员无关。
- `product_spu` 已有 `type=2` 服务产品类型，但 `TradeOrderUpdateServiceImpl.updateOrderPaid()` 当前固定把支付成功订单更新为 `UNDELIVERED`。
- 小程序 `UI/uniapp-marriage` 的"会员服务"入口仍是 toast 占位。

### 目标

- 建立付费会员体系，与经验值等级体系并行。
- 第一阶段只支持 `member_type=1` 婚恋高级会员，后续通过同一张会员记录表扩展其他会员类型。
- 会员套餐作为服务商品复用现有链路：`ProductSpu/ProductSku -> TradeOrder -> PayOrder -> pay notify -> TradeOrderHandler`。
- 新增 `partner_member` 记录会员购买、续期、赠送和过期历史。
- 支付成功后幂等激活会员，并把当前婚恋会员状态投影到 `partner_marriage`。

## 2. 范围与归属决策

### 2.1 领域归属

- 会员记录属于 `partner` 通用域，代码放在 `vip.appap.suxin.module.partner`。
- 婚恋业务只读取和维护 `partner_marriage` 上的会员投影字段，不拥有会员购买记录。
- 订单支付后的识别和激活逻辑放在 `sales` 域的 `TradeOrderHandler`，通过调用 `partner` 服务完成会员激活。
- 前端本次只改 `UI/uniapp-marriage`。

### 2.2 API 路径归属

会员记录 Controller 路径统一使用 `partner/member`：

- C 端：`GET /partner/member/get-my-membership`
- 管理端：`GET /partner/member/page`
- 管理端赠送：`POST /partner/member/grant`

不使用 `/marriage/member/*`，避免 API 路径与领域归属冲突。

### 2.3 现有真实类名

`partner_marriage` 表当前对应实体不是 `PartnerMarriageDO`，而是：

- DO：`marriage/dal/dataobject/PartnerMarriageProfileDO.java`
- Mapper：`marriage/dal/mysql/PartnerMarriageProfileMapper.java`
- Service：`marriage/service/PartnerMarriageProfileService.java`
- ServiceImpl：`marriage/service/PartnerMarriageProfileServiceImpl.java`

本规格所有涉及 `partner_marriage` 的代码变更都必须修改这些真实文件。

## 3. 配置来源

### 3.1 商品配置

会员套餐仍通过现有商品体系配置，不新增商品表结构：

```
product_category:
  └── 会员服务          (一级分类, parent_id=0)
        └── 高级会员      (二级分类, parent_id=会员服务.id)
```

- SPU 的 `category_id` 指向"高级会员"分类。
- SPU 的 `type = 2`，对应服务产品。
- SKU 的 `properties` 设置"会员时长"规格，如 `1个月`、`3个月`、`12个月`。

### 3.2 会员识别配置

不按分类名称判断会员商品。使用 infra 配置表 `infra_config` 存放可运营配置，后端通过 `ConfigApi.getConfigValueByKey()` 读取：

| 配置 key | 类型 | 示例 | 用途 |
|---|---|---|---|
| `partner.member.marriage.advanced.category-id` | Long | `123` | 高级会员商品分类 ID |
| `partner.member.marriage.advanced.member-type` | Integer | `1` | 高级会员会员类型 |
| `partner.member.duration.property-name` | String | `会员时长` | 从 SKU properties 中识别时长的属性名 |

实现要求：

- `TradeMemberOrderHandler` 启动时不缓存配置，支付处理时读取配置，避免后台改配置后必须重启。
- 配置缺失、配置值无法解析、商品分类不匹配时，抛出明确业务异常。
- SKU 时长解析基于 `ProductSkuDO#getProperties()` 返回的 `List<ProductSkuDO.Property>`，不是手写 JSON 字符串解析。

## 4. 数据模型设计

### 4.1 新增表 `partner_member`

```sql
CREATE TABLE `partner_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID(partner.id)',
  `member_type` tinyint NOT NULL COMMENT '会员类型: 1-婚恋高级会员',
  `start_time` datetime NOT NULL COMMENT '会员开始时间',
  `end_time` datetime NOT NULL COMMENT '会员到期时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 1-有效 2-已过期',
  `source_type` tinyint NOT NULL DEFAULT 1 COMMENT '来源: 1-订单购买 2-后台赠送',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID(sales_order.id)',
  `order_item_id` bigint DEFAULT NULL COMMENT '关联订单项ID(sales_order_item.id)',
  `spu_id` bigint DEFAULT NULL COMMENT '商品SPU ID',
  `sku_id` bigint DEFAULT NULL COMMENT '商品SKU ID',
  `duration_months` int NOT NULL COMMENT '时长(月)',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_item_member_type` (`order_item_id`, `member_type`),
  KEY `idx_user_type_status_end_time` (`user_id`, `member_type`, `status`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会员记录表';
```

说明：

- `uk_order_item_member_type` 用于支付回调幂等。后台赠送记录的 `order_item_id` 为 `NULL`，MySQL 允许多条 `NULL`。
- 有效会员记录允许多条，用于保留续期历史；查询当前权益时取 `status=1` 且 `end_time > NOW()` 的最大 `end_time`。
- 所有 DDL 只输出 SQL 脚本，由用户手动执行。

### 4.2 扩展 `partner_marriage`

```sql
ALTER TABLE `partner_marriage`
  ADD COLUMN `member_type` tinyint NOT NULL DEFAULT 0 COMMENT '会员类型: 0-普通用户 1-高级会员',
  ADD COLUMN `member_expire_time` datetime DEFAULT NULL COMMENT '会员到期时间';
```

Java 修改位置：

- `PartnerMarriageProfileDO` 新增 `memberType`、`memberExpireTime`。
- `PartnerMarriageProfileMapper` 新增按 ID 更新会员投影的方法，或复用 `updateById`。
- `PartnerMarriageProfileService` / `PartnerMarriageProfileServiceImpl` 新增 `updateMemberProjection(Long userId, Integer memberType, LocalDateTime expireTime)`。

## 5. 业务流转设计

### 5.1 端到端流程

```
[Admin 后台配置]
  1. 创建"会员服务"分类和"高级会员"分类
  2. 创建高级会员 SPU: type=2, categoryId=高级会员分类ID
  3. 创建 SKU: properties 中设置"会员时长"规格
  4. 在 infra_config 配置高级会员分类 ID、memberType、时长属性名
  5. 上架商品

[小程序用户购买]
  1. 进入会员套餐页
  2. 调用商品接口获取高级会员分类下的 SPU/SKU
  3. 调用 POST /trade/order/create 创建订单
  4. 调用支付接口发起微信支付
  5. 支付成功后等待回调激活会员

[支付回调处理]
  1. PayOrderServiceImpl.notifyOrder() 将 pay_order 更新为 SUCCESS
  2. PayNotifyService 通知 POST /trade/order/update-paid
  3. TradeOrderUpdateServiceImpl.updateOrderPaid() 更新订单支付状态
  4. TradeMemberOrderHandler.afterPayOrder() 判断会员商品并调用 PartnerMemberService.activateMemberByOrderItem()
  5. PartnerMemberService 幂等写入 partner_member，并更新 partner_marriage 投影

[会员状态查询]
  1. 前端调用 GET /partner/member/get-my-membership
  2. 后端返回 partner_marriage 当前投影和 partner_member 历史记录
```

### 5.2 续期规则

`activateMemberByOrderItem(userId, memberType, durationMonths, orderId, orderItemId, spuId, skuId)` 在同一事务内执行：

1. 先按 `order_item_id + member_type` 查询是否已有 `partner_member`。
2. 已存在则直接返回该记录，不重复延长会员。
3. 对 `user_id + member_type` 做事务内锁定，推荐使用 Mapper `selectLatestForUpdate(userId, memberType)` 查询该用户该类型最后一条有效记录。
4. 若存在 `status=1` 且 `end_time > NOW()` 的记录，取最大 `end_time` 作为新记录 `start_time`。
5. 否则 `start_time = NOW()`。
6. `end_time = start_time.plusMonths(durationMonths)`。
7. 插入新 `partner_member`。
8. 更新 `partner_marriage.member_type = memberType`、`member_expire_time = 当前所有有效记录最大 end_time`。

### 5.3 幂等与并发策略

- 支付重复回调：由 `uk_order_item_member_type` 和插入前查询双重保证，同一订单项只激活一次。
- 并发购买同一会员：同一事务内锁定该用户该会员类型的最新记录，第二个事务必须基于第一个事务提交后的最大 `end_time` 续期。
- Handler 部分成功：每个会员订单项在事务内独立调用激活；如果任一订单项激活失败，`updateOrderPaid()` 事务回滚，避免订单已支付但会员未激活。
- 后台赠送：不使用 `order_item_id` 幂等，Controller 层只允许一次请求创建一次赠送记录；误操作通过后续人工补偿，不做自动撤回。
- 过期 Job 可重复执行：只更新 `status=1 AND end_time <= NOW()` 的记录，天然幂等。

### 5.4 过期处理

定时任务 `PartnerMemberExpireJob` 每日凌晨执行：

1. 查询 `partner_member` 中 `status=1 AND end_time <= NOW()` 的记录。
2. 批量更新为 `status=2`。
3. 按 `user_id + member_type` 重新计算最大有效 `end_time`。
4. 若无有效记录，更新 `partner_marriage.member_type = 0, member_expire_time = NULL`。
5. 若仍有有效记录，保持 `member_type`，更新 `member_expire_time = 最大 end_time`。

## 6. 订单状态矩阵

| 订单内容 | 创建参数 | 支付成功后的订单状态 | 是否触发会员激活 | 后续动作 |
|---|---|---|---|---|
| 仅会员服务商品 | `deliveryType` 为空 | `COMPLETED` | 是 | 写会员记录，订单无需发货 |
| 仅其他服务商品 | `deliveryType` 为空 | `COMPLETED` | 否 | 不发货，不激活会员 |
| 仅实物商品 | `deliveryType=EXPRESS/PICK_UP` | `UNDELIVERED` | 否 | 保持现有发货/核销流程 |
| 会员服务商品 + 其他服务商品 | `deliveryType` 为空 | `COMPLETED` | 是 | 激活会员，其他服务商品不做额外处理 |
| 服务商品 + 实物商品混合 | 不支持 | 创建订单时拒绝 | 否 | 返回业务错误，要求拆单 |

实现要求：

- 判断服务商品基于 `ProductSpuDO.type == ProductTypeEnum.SERVICE.getType()`，不要仅依赖请求中的 `deliveryType`。
- `TradeOrderUpdateServiceImpl.updateOrderPaid()` 在更新订单状态前查询订单项 SPU 类型：
  - 全部服务商品：支付成功后直接更新为 `COMPLETED`，设置 `payStatus/payTime/payChannelCode/finishTime`。
  - 非全部服务商品：沿用现有 `UNDELIVERED` 流程。
- 创建订单阶段需要拒绝"服务商品 + 实物商品"混合订单，避免支付后状态无法表达。
- 订单日志必须记录从旧状态到目标状态；全部服务商品的日志目标状态为 `COMPLETED`。

## 7. API 设计

### 7.1 C 端：获取我的会员状态

```
GET /partner/member/get-my-membership
```

响应：

```json
{
  "code": 0,
  "data": {
    "memberType": 1,
    "memberExpireTime": "2026-09-16 00:00:00",
    "active": true,
    "members": [
      {
        "id": 1,
        "memberType": 1,
        "startTime": "2026-06-16 00:00:00",
        "endTime": "2026-09-16 00:00:00",
        "status": 1,
        "sourceType": 1,
        "durationMonths": 3
      }
    ]
  }
}
```

### 7.2 C 端：获取会员套餐列表

复用商品接口。规格要求前端从后端配置或页面初始化接口获取高级会员分类 ID 后再查询，不在前端硬编码分类 ID。

```
GET /product/spu/page?categoryId={高级会员分类ID}
```

若现有小程序商品接口只支持其他路径，以实际 `AppProductSpuController` 为准，实施计划中必须先确认。

### 7.3 C 端：购买会员

复用订单接口：

```http
POST /trade/order/create
Content-Type: application/json

{
  "items": [{"skuId": 2002, "count": 1}]
}
```

### 7.4 管理端：会员记录分页

```
GET /partner/member/page?userId=123&memberType=1&status=1
```

权限：`partner:member:query`

### 7.5 管理端：手动赠送会员

```http
POST /partner/member/grant
Content-Type: application/json

{
  "userId": 123,
  "memberType": 1,
  "durationMonths": 12,
  "remark": "运营赠送"
}
```

权限：`partner:member:grant`

## 8. 代码变更清单

### 8.1 新增文件（partner 域）

| 文件 | 说明 |
|---|---|
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/partner/dal/dataobject/PartnerMemberDO.java` | 会员记录 DO，继承 `BaseDO` |
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/partner/dal/mysql/PartnerMemberMapper.java` | Mapper，包含分页、按订单项查询、最新有效记录锁定查询 |
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/partner/service/PartnerMemberService.java` | 会员服务接口 |
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/partner/service/PartnerMemberServiceImpl.java` | 激活、赠送、过期、查询实现 |
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/partner/controller/app/AppPartnerMemberController.java` | C 端会员状态接口 |
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/partner/controller/admin/PartnerMemberController.java` | 管理端分页和赠送接口 |
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/partner/controller/admin/vo/PartnerMemberPageReqVO.java` | 管理端分页请求 |
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/partner/controller/admin/vo/PartnerMemberRespVO.java` | 管理端响应 |
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/partner/controller/admin/vo/PartnerMemberGrantReqVO.java` | 管理端赠送请求 |
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/partner/controller/app/vo/AppPartnerMemberRespVO.java` | C 端响应 |
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/partner/convert/PartnerMemberConvert.java` | MapStruct 转换 |
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/partner/enums/MemberStatusEnum.java` | 会员状态枚举 |
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/partner/enums/MemberSourceTypeEnum.java` | 会员来源枚举 |
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/partner/job/PartnerMemberExpireJob.java` | 过期 Job |

### 8.2 新增文件（sales 域）

| 文件 | 说明 |
|---|---|
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/sales/service/order/handler/TradeMemberOrderHandler.java` | 支付后识别会员订单项并调用会员服务 |

### 8.3 修改文件

| 文件 | 变更 |
|---|---|
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/marriage/dal/dataobject/PartnerMarriageProfileDO.java` | 新增 `memberType`, `memberExpireTime` |
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/marriage/dal/mysql/PartnerMarriageProfileMapper.java` | 增加会员投影更新方法或复用 `updateById` |
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/marriage/service/PartnerMarriageProfileService.java` | 增加 `updateMemberProjection` |
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/marriage/service/PartnerMarriageProfileServiceImpl.java` | 实现会员投影更新 |
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/sales/service/order/TradeOrderUpdateServiceImpl.java` | 支付后按商品类型决定 `UNDELIVERED` 或 `COMPLETED` |
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/sales/service/order/handler/TradeOrderHandler.java` | 无需改签名，继续使用 `afterPayOrder` |
| `suxin-module-system/src/main/java/cn/iocoder/suxin/module/partner/enums/ErrorCodeConstants.java` | 新增会员错误码 |
| `UI/uniapp-marriage/pages.json` | 注册会员套餐页面 |
| `UI/uniapp-marriage/pages/member-packages/index.uvue` | 新增会员套餐列表页 |
| `UI/uniapp-marriage/pages/mine/index.uvue` | 展示会员状态 |
| `UI/uniapp-marriage/components/mine-profile-actions.uvue` | 会员入口跳转套餐页 |
| `UI/uniapp-marriage/utils/member-api.uts` | 新增会员接口封装 |

## 9. 关键实现细节

### 9.1 `TradeMemberOrderHandler`

```java
@Component
public class TradeMemberOrderHandler implements TradeOrderHandler {

    @Resource
    private ConfigApi configApi;
    @Resource
    private ProductSpuService productSpuService;
    @Resource
    private ProductSkuService productSkuService;
    @Resource
    private PartnerMemberService partnerMemberService;

    @Override
    public void afterPayOrder(TradeOrderDO order, List<TradeOrderItemDO> items) {
        Long memberCategoryId = getRequiredLongConfig("partner.member.marriage.advanced.category-id");
        Integer memberType = getRequiredIntConfig("partner.member.marriage.advanced.member-type");
        String durationPropertyName = getRequiredStringConfig("partner.member.duration.property-name");

        for (TradeOrderItemDO item : items) {
            ProductSpuDO spu = productSpuService.getSpu(item.getSpuId());
            if (spu == null || !Objects.equals(spu.getCategoryId(), memberCategoryId)) {
                continue;
            }
            ProductSkuDO sku = productSkuService.getSku(item.getSkuId());
            int durationMonths = parseDurationMonths(sku.getProperties(), durationPropertyName);
            partnerMemberService.activateMemberByOrderItem(
                    order.getUserId(), memberType, durationMonths,
                    order.getId(), item.getId(), spu.getId(), sku.getId());
        }
    }

    private int parseDurationMonths(List<ProductSkuDO.Property> properties, String propertyName) {
        // 从 properties 中找到 propertyName 相等的属性，解析 valueName 中的正整数月份。
        // 仅支持 "1个月"、"3个月"、"12个月" 这类月单位；无法解析时抛 MEMBER_DURATION_PARSE_FAILED。
    }
}
```

### 9.2 `PartnerMemberService.activateMemberByOrderItem`

核心行为：

- `@Transactional(rollbackFor = Exception.class)`
- 先查 `orderItemId + memberType`，存在则返回。
- 查询当前用户该会员类型最大有效 `end_time` 并锁定。
- 插入 `sourceType=1` 的记录。
- 更新 `PartnerMarriageProfileService.updateMemberProjection()`。

### 9.3 后台赠送

后台赠送复用同一个续期算法，但：

- `sourceType=2`
- `orderId/orderItemId/spuId/skuId` 为空
- `durationMonths` 必填且大于 0
- `memberType` 第一阶段只允许 `1`
- `remark` 必填，便于审计

## 10. 错误码定义

在 `partner/enums/ErrorCodeConstants.java` 新增：

```java
ErrorCode MEMBER_NOT_EXISTS = new ErrorCode(1_002_001, "会员记录不存在");
ErrorCode MEMBER_CONFIG_NOT_FOUND = new ErrorCode(1_002_002, "会员配置缺失");
ErrorCode MEMBER_CONFIG_INVALID = new ErrorCode(1_002_003, "会员配置格式错误");
ErrorCode MEMBER_DURATION_PARSE_FAILED = new ErrorCode(1_002_004, "会员时长解析失败");
ErrorCode MEMBER_ORDER_DUPLICATED = new ErrorCode(1_002_005, "会员订单已处理");
ErrorCode MEMBER_GRANT_DURATION_INVALID = new ErrorCode(1_002_006, "赠送会员时长必须大于 0");
ErrorCode MEMBER_MIXED_ORDER_UNSUPPORTED = new ErrorCode(1_002_007, "服务商品与实物商品不能混合下单");
```

## 11. 测试清单

### 11.1 后端单元/集成测试

- `PartnerMemberService.activateMemberByOrderItem` 首次购买：生成一条有效记录，`partner_marriage` 投影为高级会员。
- `activateMemberByOrderItem` 未过期续期：新记录从旧最大 `end_time` 开始累加。
- `activateMemberByOrderItem` 已过期后购买：新记录从当前时间开始。
- 重复支付回调：同一 `order_item_id + member_type` 不重复插入，不重复延长。
- 并发续期：两个事务购买同一会员，最终 `end_time` 连续累加，不重叠丢失。
- 配置缺失：抛 `MEMBER_CONFIG_NOT_FOUND`。
- SKU 无会员时长属性：抛 `MEMBER_DURATION_PARSE_FAILED`。
- 后台赠送：写入 `sourceType=2`，并延长投影到期时间。
- 过期 Job：过期记录变为 `status=2`；无有效记录时清空 `partner_marriage` 投影。

### 11.2 订单流程测试

- 仅会员服务商品支付成功：订单变为 `COMPLETED`，会员激活。
- 仅其他服务商品支付成功：订单变为 `COMPLETED`，不激活会员。
- 仅实物商品支付成功：订单仍变为 `UNDELIVERED`。
- 服务商品和实物商品混合创建订单：创建阶段失败，提示不支持混合下单。
- 支付后 handler 异常：`updateOrderPaid()` 事务回滚，订单状态不应变为已支付完成。

### 11.3 API 测试

- `GET /partner/member/get-my-membership` 未登录时被拦截。
- `GET /partner/member/get-my-membership` 普通用户返回 `active=false`。
- `GET /partner/member/get-my-membership` 高级会员返回 `active=true` 和历史记录。
- `GET /partner/member/page` 需要 `partner:member:query` 权限。
- `POST /partner/member/grant` 需要 `partner:member:grant` 权限，且校验 `durationMonths > 0`、`remark` 非空。

### 11.4 前端测试

- `pages/member-packages/index.uvue` 能加载会员套餐、展示 SKU 时长和价格。
- 点击套餐后能创建订单并进入支付流程。
- `pages/mine/index.uvue` 能展示普通用户、高级会员、过期会员三种状态。
- `components/mine-profile-actions.uvue` 会员入口跳转到会员套餐页，不再 toast 占位。
- 接口失败时显示明确错误提示，不出现空白页。

## 12. 实施步骤建议

1. 输出 DDL 和 infra_config 初始化 SQL，用户手动执行。
2. 后端增加 `partner_member` DO/Mapper/Service/Controller/Convert/Enum。
3. 修改 `PartnerMarriageProfileDO/Mapper/Service`，增加会员投影字段和更新方法。
4. 实现 `TradeMemberOrderHandler` 和配置读取、SKU 时长解析。
5. 修改订单创建和支付成功状态流转，支持服务商品完成态并拒绝混合订单。
6. 实现 `PartnerMemberExpireJob`。
7. 实现管理端分页和赠送接口。
8. 实现小程序会员套餐页、会员状态展示和入口跳转。
9. 按测试清单补充测试和联调。
