# 婚恋端“我的支付/我的订单”轻量页设计说明

**日期：** 2026-06-27  
**范围：** `UI/uniapp-marriage` 前端订单入口与轻量订单列表页新增  
**目标：** 在婚恋端“我的”页面接通“我的支付”入口，新建一个轻量“我的订单”页面，复用现有通用交易/支付接口，先支持订单列表展示、订单状态展示与继续支付能力。

## 1. 背景与现状

当前 `UI/uniapp-marriage` 已经接入统一商品与支付链路，但“我的支付”入口仍未落地为真实页面。

现有婚恋端相关能力：

- `UI/uniapp-marriage/components/mine-profile-actions.uvue` 已经存在 `payment` 入口项，文案为“我的支付”
- `UI/uniapp-marriage/pages/mine/index.uvue` 当前未处理 `payment` 分支，点击后会落入“该功能页面待补充”
- `UI/uniapp-marriage/pages/pay-cashier/index.uvue` 已存在通用收银台页
- `UI/uniapp-marriage/pages/pay-result/index.uvue` 已存在通用支付结果页
- `UI/uniapp-marriage/utils/pay-api.uts` 已封装支付单查询、支付渠道查询、支付提交、订单详情查询等能力
- `UI/uniapp-marriage/utils/product-api.uts`、`member-api.uts`、`activity-api.uts` 均通过 `/trade/order/create` 创建统一交易订单

商城端 `UI/uniapp-mall` 已存在完整的“我的订单”实现，可确认以下后端接口已在前端被实际使用：

- `/trade/order/page`
- `/trade/order/get-detail`
- `/trade/order/cancel`
- `/trade/order/delete`
- `/trade/order/receive`
- `/accountant/order/get`

本次需求不是重建订单体系，而是在婚恋端补齐“订单入口 + 列表承接页”，复用现有统一商品订单与支付单能力。

## 2. 产品目标

第一版“我的支付”功能需要满足以下目标：

1. **入口可用**：用户在“我的”页面点击“我的支付”后，可以进入真实页面，而不是看到占位提示
2. **订单可见**：用户可以查看自己当前的商品订单列表与核心状态
3. **支付可续**：用户可以从订单列表对待支付订单继续发起支付
4. **改造可控**：不引入商城端组件体系，不打乱婚恋端现有 `uvue + uts` 结构

本次重点是把“支付入口”落在“订单列表页”上，而不是先做“支付记录页”或完整订单中心。

## 3. 方案对比与设计决策

### 3.1 选用方案

采用如下方案：

- 在 `UI/uniapp-marriage` 新建一个轻量“我的订单”页
- 新增婚恋端订单接口封装文件，独立调用 `/trade/order/page`
- 参考商城端的筛选、状态与支付跳转逻辑，但页面 UI 使用婚恋端现有风格重写
- 继续复用婚恋端现有 `pay-cashier` 与 `pay-result` 页面承接支付流程

### 3.2 不采用的方案

以下方案本次不采用：

- **直接搬运 mall 订单页**：商城页依赖 `sheep`、`s-layout`、`su-tabs`、`s-goods-item`、`useGoods` 等体系，和婚恋端 `uvue/uts` 架构不一致，直接迁移会引入高耦合
- **先做支付记录页**：虽然名义上与“我的支付”更贴近，但当前实际业务链路基于统一商品订单，订单列表承接更自然
- **只做待支付极简页**：实现会更快，但很快会因为缺少完整订单视图而返工
- **一次性做完整订单中心**：包含订单详情、取消订单、删除订单、确认收货、售后等，超出本次“简单接通入口”的目标范围

### 3.3 设计结论

第一版以“订单列表页”作为“我的支付”的承接页，先解决入口可用、订单可见、支付可续这三个核心问题。后续如果需要扩展订单详情、取消、售后等能力，可在此页面基础上继续叠加。

## 4. 页面与文件边界

### 4.1 新增文件

#### `UI/uniapp-marriage/pages/mine-payment/index.uvue`

职责：

- 承载“我的订单”页面 UI
- 负责 tab 切换、分页加载、空态与错误提示
- 展示订单卡片、状态文案、金额与商品项
- 对待支付订单发起“继续支付”跳转

#### `UI/uniapp-marriage/utils/order-api.uts`

职责：

- 封装订单分页查询接口 `/trade/order/page`
- 定义页面需要的订单列表数据结构
- 统一处理通用返回结构解包与错误文案
- 在必要时对商城端订单字段与婚恋端页面展示所需结构做轻量兼容映射

### 4.2 修改文件

#### `UI/uniapp-marriage/pages.json`

职责：

- 注册 `pages/mine-payment/index` 路由
- 保持与婚恋端现有页面一致的 `navigationStyle: custom`

#### `UI/uniapp-marriage/pages/mine/index.uvue`

职责：

- 在 `handleAction` 中新增 `payment` 分支
- 登录后跳转至 `/pages/mine-payment/index`
- 与现有 `checkin`、`points`、`share` 等入口保持一致的调用风格

### 4.3 复用文件

#### `UI/uniapp-marriage/utils/pay-api.uts`

继续复用：

- `getPayOrder`
- `getTradeOrderDetail`
- `formatPayAmount`
- `resolvePayError`

#### `UI/uniapp-marriage/pages/pay-cashier/index.uvue`

继续作为待支付订单的支付收银台。

#### `UI/uniapp-marriage/pages/pay-result/index.uvue`

继续作为支付完成后的结果承接页。

## 5. 页面信息架构

“我的订单”页采用轻量列表结构，不做商城级复杂订单中心。

建议页面从上到下包含以下区块：

### 5.1 顶部标题区

- 页面标题：`我的订单`
- 副标题：可选，建议简洁说明“查看订单状态并继续支付”
- 返回按钮：风格对齐婚恋端 `pay-cashier`、`pay-result` 页

### 5.2 状态筛选区

使用横向 tab 切换以下筛选项：

- 全部
- 待付款
- 待发货
- 待收货
- 待评价

tab 区域应保持轻量，不引入商城端现成 tabs 组件。

### 5.3 订单列表区

每个订单卡片第一版展示以下信息：

- 订单号 `no`
- 订单状态文案
- 商品列表 `items`
  - 商品图片
  - 商品名称
  - SKU/规格描述
  - 单价
  - 数量
- 订单金额 `payPrice`
- 底部操作区

页面视觉上延续婚恋端当前暖色、米白、深色文字体系，弱化商城感，避免使用 mall 的 UI 元素。

### 5.4 空态区

当订单列表为空时，展示统一空态：

- 文案：`暂无订单`

第一版不需要额外引导去购买商品，保持简单。

## 6. 数据模型与接口设计

### 6.1 订单分页接口

新页面列表数据来源：

- 接口：`/trade/order/page`
- 方法：`GET`

请求参数建议包含：

- `pageNo`
- `pageSize`
- `status`（可选，全部时不传）

### 6.2 订单数据结构

婚恋端 `order-api.uts` 以商城端已使用的订单结构为基础，页面默认依赖以下字段：

- `id`
- `no`
- `status`
- `payPrice`
- `payOrderId`
- `items`

其中每个 `item` 默认需要以下字段：

- `id`
- `picUrl`
- `spuName`
- `price`
- `count`
- `properties`

如果后端实际返回字段与商城端存在细微差异，适配逻辑应放在 `order-api.uts` 内部，不把兼容分散到页面层。

### 6.3 状态筛选映射

页面 tab 与请求参数映射保持与商城端一致：

- 全部：不传 `status`
- 待付款：`status = 0`
- 待发货：`status = 10`
- 待收货：`status = 20`
- 待评价：`status = 30`

这样可最大化复用统一订单接口的既有行为，并降低前后端对齐成本。

### 6.4 状态文案展示

第一版状态展示建议优先基于后端 `status` 字段做前端映射，映射结果对齐商城常见状态语义：

- `0` → 待付款
- `10` → 待发货
- `20` → 待收货
- `30` → 待评价
- 其它状态 → 根据既有字段补充展示，若无法准确区分则显示通用文案，如“已完成”或“已关闭”

具体映射函数放在 `mine-payment/index.uvue` 内局部实现即可；若后续多页复用，再抽离公共方法。

## 7. 支付数据流设计

### 7.1 继续支付入口条件

第一版页面底部按钮策略保持最小可用：

- 对待支付订单显示“继续支付”按钮
- 其它状态仅展示状态，不提供额外操作按钮

是否可支付以订单自身 `status` 与 `payOrderId` 是否有效为基础判断。

### 7.2 继续支付跳转

点击“继续支付”后，直接跳转已有收银台页：

- 目标页面：`/pages/pay-cashier/index`

建议传递参数：

- `payOrderId`
- `tradeOrderId`
- `orderType=product`

示例：

`/pages/pay-cashier/index?payOrderId={payOrderId}&tradeOrderId={orderId}&orderType=product`

这里的 `tradeOrderId` 使用订单主键 `id`，与婚恋端现有支付结果页读取交易订单详情的模式一致。

### 7.3 支付完成后的后续链路

继续支付后的流程不新增分支：

1. 进入 `pay-cashier`
2. 完成支付提交
3. 跳转 `pay-result`
4. `pay-result` 继续通过 `payOrderId` 与 `tradeOrderId` 查询支付单与交易订单状态

这样可以让“从商品详情下单”和“从订单列表继续支付”共用同一套支付后链路。

## 8. 交互与状态设计

### 8.1 登录前置

“我的订单”必须要求用户已登录。

建议双重兜底：

1. `mine/index.uvue` 入口点击时先检查 `isMemberLoggedIn()`，未登录则拉起现有登录逻辑
2. `mine-payment/index.uvue` 页面进入时再次校验登录状态，避免用户通过外部路径直接访问时报错

如果未登录，页面可直接返回并提示用户先登录，不在订单页内重复承载复杂登录逻辑。

### 8.2 初次加载

页面首次进入时：

- 立即展示顶部标题与 tab 区
- 同时请求第一页订单数据
- 可使用简单 loading 文案或轻量占位，不引入复杂 loading 组件体系

### 8.3 切换 tab

切换筛选项时：

- 重置分页数据
- 回到第一页重新加载
- 加载失败时不清空已有数据，只提示失败

### 8.4 分页加载

分页使用婚恋端简单列表模式即可：

- 初次进入加载第一页
- 触底加载更多
- 维护基础状态：`pageNo`、`pageSize`、`loading`、`hasMore`

不依赖商城的 `uni-load-more` 使用方式，也不强制引入额外公共组件。

### 8.5 错误处理

#### 首屏加载失败

- 提示：`订单加载失败`
- 页面保留基础骨架与空内容态
- 用户可以通过重新进入页面或后续刷新重试

#### tab 切换或加载更多失败

- 保留当前已加载列表
- 通过 `uni.showToast` 提示：`加载失败，请稍后重试`

#### 继续支付失败

- 不离开当前页面
- 通过 `resolvePayError` 或通用文案提示用户

## 9. 第一版明确范围

### 9.1 本次实现包含

- mine 页“我的支付”入口接通
- 新增轻量“我的订单”页面
- 支持订单分页列表
- 支持 tab 状态筛选
- 支持订单状态展示
- 支持待支付订单继续支付
- 复用现有 `pay-cashier` 与 `pay-result` 页面

### 9.2 本次实现不包含

以下内容第一版不做：

- 订单详情页
- 取消订单
- 删除订单
- 确认收货
- 售后/退款相关能力
- 拼团、活动等特殊订单分支交互
- 支付记录页或支付单列表页
- 统一抽象商城端所有订单按钮逻辑

该边界用于保证本次改造保持“轻量、低风险、快速接通入口”的目标，不扩大为完整订单中心项目。

## 10. 建议实现顺序

1. 在 `pages.json` 注册 `pages/mine-payment/index`
2. 新增 `utils/order-api.uts`，封装 `/trade/order/page`
3. 在 `pages/mine/index.uvue` 中接通 `payment` 分支跳转
4. 新建 `pages/mine-payment/index.uvue`，完成 tab、列表、分页、空态与继续支付
5. 联通 `pay-cashier` 跳转参数，确保继续支付链路可复用

## 11. 验收标准

满足以下条件即可视为本次设计目标达成：

1. 用户在“我的”页点击“我的支付”后，可进入真实订单列表页
2. 页面能正确加载并展示当前用户的订单列表
3. tab 切换后能按状态重新加载订单
4. 待支付订单能跳转现有收银台页继续支付
5. 空列表、加载失败、未登录等场景有基本可用提示
6. 页面实现不依赖 `uniapp-mall` 的 `sheep` 组件体系
