# 钱包页微信提现确认收款设计更新

- 日期：2026-06-26
- 目标：将“确认收款”能力放到用户真实可达的佣金页提现 tab 中，而不是依赖独立提现记录页。

## 1. 背景修正

前一版设计把“确认收款”入口放在独立提现记录页：
- `UI/uniapp-marriage/pages/distributor/withdraw-record/index.uvue`

但真实用户路径显示：
- 用户主要查看的是 `UI/uniapp-marriage/pages/distributor/wallet/index.uvue`
- 该页面顶部标题为“佣金”，并在内部通过 tab 展示“分佣 / 提现”两类记录
- 独立提现记录页缺少清晰入口，用户日常无法自然到达

因此，前一版设计虽然技术上可行，但不符合当前产品真实路径。

## 2. 本次修正后的目标

在 `wallet/index.uvue` 的“提现”tab 中，直接为符合条件的微信提现记录显示“确认收款”按钮，并在该页面内完成：

1. 按钮展示
2. 微信确认收款调用
3. 操作结果提示
4. 列表刷新

## 3. 方案选择

### 采用方案 A：提现 tab 单独渲染

保留“分佣”tab 使用当前通用列表模型；将“提现”tab 改为直接渲染原始 `BrokerageWithdrawRecord` 列表。

**原因：**
- 提现记录具备额外业务字段：`type`、`status`、`payTransferId`、`transferChannelPackageInfo`、`transferChannelMchId`
- 如果继续复用 `WalletListItem`，需要强行塞入大量提现特有字段，污染通用模型
- 单独渲染提现 tab 后，确认收款逻辑与提现展示逻辑保持在同一层，结构更清晰

### 不采用方案 B：继续共用 `WalletListItem`

**不采用原因：**提现业务字段过多，会让通用记录结构失去边界。

### 不采用方案 C：按钮跳转至独立提现记录页

**不采用原因：**依然要求用户进入一个当前没有明确入口的页面，不符合真实路径需求。

## 4. 页面结构调整

目标文件：
- `UI/uniapp-marriage/pages/distributor/wallet/index.uvue`

### 4.1 当前结构

当前页面使用一个统一的 `items` 数组渲染两类记录：
- 分佣记录 → `WalletListItem`
- 提现记录 → `WalletListItem`

### 4.2 调整后结构

改为按 tab 区分渲染：

- `currentTab == 0`：继续渲染分佣记录
- `currentTab == 1`：单独渲染提现记录

因此页面内部需要分成两套数据：
- 分佣数据：保持现状，可继续使用通用映射结构
- 提现数据：保留原始 `BrokerageWithdrawRecord[]`

## 5. 提现 tab 的交互设计

### 5.1 展示内容

每条提现记录继续展示：
- 标题（如“提现到微信零钱”）
- 金额
- 时间
- 状态

### 5.2 按钮显示条件

仅当以下条件同时满足时显示“确认收款”：
- 提现方式为微信零钱：`type == BROKERAGE_WITHDRAW_TYPE_WECHAT_API`
- 状态仍处于审核通过 / 待确认阶段：`status == BROKERAGE_WITHDRAW_STATUS_AUDIT_SUCCESS`
- `payTransferId` 存在且有效
- 当前平台为微信小程序，且支持 `requestMerchantTransfer`

### 5.3 点击后的流程

1. 用户点击“确认收款”
2. 调用 `getBrokerageWithdraw(item.id)` 获取最新提现详情
3. 校验：
   - `transferChannelMchId`
   - `transferChannelPackageInfo`
4. 调用 `requestWechatMerchantTransfer(...)`
5. 根据结果提示：
   - 成功：提示并刷新列表
   - 取消：提示已取消
   - 失败：提示失败原因
6. 恢复按钮状态

## 6. 技术实现边界

### 6.1 继续复用已有能力

直接复用：
- `UI/uniapp-marriage/utils/wechat-transfer.uts`
- `UI/uniapp-marriage/utils/brokerage-api.uts`
  - `getBrokerageWithdraw(id)`
  - `BROKERAGE_WITHDRAW_TYPE_WECHAT_API`
  - `BROKERAGE_WITHDRAW_STATUS_AUDIT_SUCCESS`

### 6.2 本次仅改一个主页面

主改动集中在：
- `UI/uniapp-marriage/pages/distributor/wallet/index.uvue`

独立提现记录页可以保留已有实现，但它不再是本次主入口。

## 7. 错误处理

钱包页提现 tab 中的“确认收款”需要覆盖以下情况：

1. 不是微信小程序环境
   - 不显示按钮

2. 当前设备微信版本不支持
   - 点击前即判定不可用，按钮不显示或提示不可用

3. 详情接口失败
   - 提示“获取提现详情失败，请稍后重试”

4. 渠道参数缺失
   - 提示“收款参数未准备完成，请稍后重试”

5. 用户取消确认
   - 提示“已取消确认收款”

6. 微信接口失败
   - 展示微信原始错误信息；没有则提示默认失败文案

## 8. 测试与验证

需要验证以下真实路径：

1. 打开“佣金”页
2. 切换到“提现”tab
3. 对符合条件的微信提现记录看到“确认收款”按钮
4. 点击按钮后拉起微信确认收款
5. 成功返回后列表刷新
6. 非微信提现记录不出现按钮
7. 已完成/失败/审核拒绝记录不出现按钮
8. 分佣 tab 的原有展示不受影响

## 9. 范围边界

本次只做：
- 钱包页提现 tab 单独渲染
- 钱包页提现 tab 增加“确认收款”按钮
- 钱包页内完成确认收款与刷新

本次不做：
- 新增页面入口设计
- 长轮询状态同步
- 后端接口调整
- 重新设计独立提现记录页

## 10. 受影响文件（更新后）

主要修改：
- `UI/uniapp-marriage/pages/distributor/wallet/index.uvue`

复用已有：
- `UI/uniapp-marriage/utils/wechat-transfer.uts`
- `UI/uniapp-marriage/utils/brokerage-api.uts`

## 11. 实施摘要

最终以钱包页为真实入口：

1. 让“提现”tab不再压平成通用列表，而是直接渲染提现记录
2. 对符合条件的微信提现记录显示“确认收款”按钮
3. 点击后调用微信提现确认收款能力
4. 操作完成后刷新当前页
