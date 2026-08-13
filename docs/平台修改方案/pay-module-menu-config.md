# Pay 模块菜单与路由配置

## 说明

- 当前后端实际可用模块基于 `accountant` 落地，但 HTTP 路径仍是 `/pay/**`。
- 前端菜单仍使用 `pay` 目录，不改成 `accountant`。
- `demo-order`、`demo-withdraw` 对应后端未落地，不建议配置菜单。
- 渠道配置已集成在“支付应用”页面内，不单独配置“支付渠道”菜单。

## 可配置菜单

| 名称 | 菜单类型 | 路由地址 | 组件路径 | 权限标识 | 备注 |
| --- | --- | --- | --- | --- | --- |
| 支付管理 | 目录 | `/pay` | `#` | `""` | 一级目录 |
| 支付应用 | 菜单 | `app` | `pay/app/index` | `pay:app:query` | 页面内含渠道配置 |
| 支付订单 | 菜单 | `order` | `pay/order/index` | `pay:order:query` | 支持导出 |
| 退款订单 | 菜单 | `refund` | `pay/refund/index` | `pay:refund:query` | 支持导出 |
| 回调通知 | 菜单 | `notify` | `pay/notify/index` | `pay:notify:query` | 查看通知日志 |
| 转账订单 | 菜单 | `transfer` | `pay/transfer/index` | `pay:transfer:query` | 支持导出 |
| 钱包余额 | 菜单 | `wallet/balance` | `pay/wallet/balance/index` | `pay:wallet:query` | 钱包流水在弹窗内查看 |
| 充值套餐 | 菜单 | `wallet/recharge-package` | `pay/wallet/rechargePackage/index` | `pay:wallet-recharge-package:query` | 套餐 CRUD |

## 按钮权限

### 支付应用

| 名称 | 权限 |
| --- | --- |
| 新增 | `pay:app:create` |
| 编辑 | `pay:app:update` |
| 删除 | `pay:app:delete` |
| 渠道配置 | `pay:channel:query` / `pay:channel:create` / `pay:channel:update` |

### 支付订单

| 名称 | 权限 |
| --- | --- |
| 查询 | `pay:order:query` |
| 导出 | `pay:order:export` |

### 退款订单

| 名称 | 权限 |
| --- | --- |
| 查询 | `pay:refund:query` |
| 导出 | `pay:refund:export` |

### 回调通知

| 名称 | 权限 |
| --- | --- |
| 查询 | `pay:notify:query` |

### 转账订单

| 名称 | 权限 |
| --- | --- |
| 查询 | `pay:transfer:query` |
| 导出 | `pay:transfer:export` |

### 钱包余额

| 名称 | 权限 |
| --- | --- |
| 查询 | `pay:wallet:query` |
| 修改余额 | `pay:wallet:update-balance` |

### 充值套餐

| 名称 | 权限 |
| --- | --- |
| 查询 | `pay:wallet-recharge-package:query` |
| 新增 | `pay:wallet-recharge-package:create` |
| 编辑 | `pay:wallet-recharge-package:update` |
| 删除 | `pay:wallet-recharge-package:delete` |

## 隐藏路由

| 名称 | 路由地址 | 组件路径 | 说明 |
| --- | --- | --- | --- |
| 收银台 | `/pay/cashier` | `pay/cashier/index` | 已在 `remaining.ts` 中注册，供支付跳转使用 |

## 菜单 SQL 模板

```sql
-- 1. 一级目录：支付管理
-- parent_id 请替换为你的上级目录 ID，例如“销售中心”或“系统工具”
INSERT INTO system_menu
(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show)
VALUES
('支付管理', '', 1, 90, {PARENT_ID}, 'pay', 'ep:wallet', '', '', 0, b'1', b'1', b'1');

-- 2. 二级菜单示例
-- {PAY_DIR_ID} 替换为上一步生成的支付管理目录 ID
INSERT INTO system_menu
(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show)
VALUES
('支付应用', 'pay:app:query', 2, 10, {PAY_DIR_ID}, 'app', 'ep:credit-card', 'pay/app/index', 'PayApp', 0, b'1', b'1', b'0'),
('支付订单', 'pay:order:query', 2, 20, {PAY_DIR_ID}, 'order', 'ep:tickets', 'pay/order/index', 'PayOrder', 0, b'1', b'1', b'0'),
('退款订单', 'pay:refund:query', 2, 30, {PAY_DIR_ID}, 'refund', 'ep:refresh-left', 'pay/refund/index', 'PayRefund', 0, b'1', b'1', b'0'),
('回调通知', 'pay:notify:query', 2, 40, {PAY_DIR_ID}, 'notify', 'ep:bell', 'pay/notify/index', 'PayNotify', 0, b'1', b'1', b'0'),
('转账订单', 'pay:transfer:query', 2, 50, {PAY_DIR_ID}, 'transfer', 'ep:switch', 'pay/transfer/index', 'PayTransfer', 0, b'1', b'1', b'0'),
('钱包余额', 'pay:wallet:query', 2, 60, {PAY_DIR_ID}, 'wallet/balance', 'ep:coin', 'pay/wallet/balance/index', 'WalletBalance', 0, b'1', b'1', b'0'),
('充值套餐', 'pay:wallet-recharge-package:query', 2, 70, {PAY_DIR_ID}, 'wallet/recharge-package', 'ep:shopping-bag', 'pay/wallet/rechargePackage/index', 'WalletRechargePackage', 0, b'1', b'1', b'0');
```
