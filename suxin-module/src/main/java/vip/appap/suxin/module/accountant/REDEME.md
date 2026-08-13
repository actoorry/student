# 商城财务模块设计文档

> 基于现有系统分析与《中国企业会计准则》、金税四期等最新规范的设计建议。

---

## 一、现有系统分析

### 1.1 模块全景

项目已有三大与财务相关的模块：

```
suxin-module/
├── accountant/          ← 支付网关 + 钱包系统（12 张表）
├── sales/trade/         ← 商城交易系统（15+ 张表）
└── sales/statistics/    ← 交易统计

suxin-module-erp/
└── finance/             ← ERP 收款单 / 付款单（6 张表）
```

### 1.2 Accountant 模块（支付 + 钱包）

当前 `accountant` 模块本质上是一个**支付网关 + 会员钱包**系统，不是完整会计模块。

#### 核心数据表

| 表名 | DO 类 | 用途 |
|---|---|---|
| `account` | `AccountDO` | 会员钱包 — id（关联 partner）、userType、balance、freezePrice、totalExpense、totalRecharge |
| `account_recharge` | `AccountRechargeDO` | 充值记录 — accountId、payPrice、bonusPrice、totalPrice、packageId、payOrderId、payChannelCode、payTime，支持退款 |
| `account_recharge_package` | `AccountRechargePackageDO` | 充值套餐 — name、payPrice、bonusPrice、status |
| `account_transaction` | `AccountTransactionDO` | 钱包流水 — accountId、no、bizType、bizId、title、price（正增负减）、balance |
| `pay_app` | `PayAppDO` | 支付应用（多商户）— appKey、name、status、各类回调 URL |
| `pay_channel` | `PayChannelDO` | 支付渠道 — code（wx_pub/alipay_pc/...）、status、feeRate、appId、config(JSON) |
| `pay_order` | `PayOrderDO` | 支付订单 — appId、channelId、merchantOrderId、price、status、channelFeeRate、channelFeePrice、refundPrice、expireTime |
| `pay_order_extension` | `PayOrderExtensionDO` | 支付扩展（渠道级重试）— orderId、channelId、no、status、channelExtras、channelNotifyData |
| `pay_refund` | `PayRefundDO` | 退款单 — appId、channelId、orderId、merchantRefundId、refundPrice、status、channelRefundNo |
| `pay_transfer` | `PayTransferDO` | 转账单 — appId、channelId、merchantTransferId、price、userAccount、userName、status |
| `pay_notify_task` | `PayNotifyTaskDO` | 异步通知任务 — 最多 9 次重试（频率: 15s, 15s, 30s, 3m, 30m×3, 1h） |
| `pay_notify_log` | `PayNotifyLogDO` | 通知日志 — taskId、notifyTimes、response、status |

#### 业务枚举

| 枚举 | 值 | 说明 |
|---|---|---|
| `AccountBizTypeEnum` | RECHARGE(1) / RECHARGE_REFUND(2) / PAYMENT(3) / PAYMENT_REFUND(4) / UPDATE_BALANCE(5) / TRANSFER(6) | 钱包交易类型 |
| `PayOrderStatusEnum` | WAITING(0) / SUCCESS(10) / REFUND(20) / CLOSED(30) | 支付状态 |
| `PayRefundStatusEnum` | WAITING(0) / SUCCESS(10) / FAILURE(20) | 退款状态 |
| `PayChannelEnum` | WX_PUB / WX_LITE / WX_APP / WX_NATIVE / WX_WAP / WX_BAR / ALIPAY_PC / ALIPAY_WAP / ALIPAY_APP / ALIPAY_QR / ALIPAY_BAR / MOCK / ACCOUNT | 支付渠道 |

#### 关键服务

| 服务 | 职责 |
|---|---|
| `AccountServiceImpl` | 钱包余额管理：Redis 分布式锁 + CAS 更新（`updateWhenConsumption` / `updateWhenRecharge` 等），支持冻结/解冻 |
| `AccountRechargeServiceImpl` | 充值生命周期：创建 → 支付 → 到账 → 退款 |
| `PayOrderServiceImpl` | 支付订单编排：创建 → 提交渠道 → 同步 → 过期 |
| `PayRefundServiceImpl` | 退款处理：创建 → 通知 → 同步 |
| `PayNotifyServiceImpl` | 通知回调：创建任务 → 线程池批量执行 → 退避重试 |

#### 支付客户端框架

基于 `PayClient` 接口的插件化架构，`PayClientFactoryImpl` 管理所有渠道客户端：

- **微信支付** — WxPubPayClient、WxLitePayClient、WxAppPayClient、WxNativePayClient、WxWapPayClient、WxBarPayClient
- **支付宝** — AlipayPcPayClient、AlipayWapPayClient、AlipayAppPayClient、AlipayQrPayClient、AlipayBarPayClient
- **钱包余额** — `AccountPayClient`（直接操作 AccountDO，无外部渠道）
- **Mock** — MockPayClient（测试用）

#### 对外 API（供其他模块调用）

| API | 方法 |
|---|---|
| `AccountApi` | `addAccountBalance()` / `getOrCreateAccount()` |
| `PayOrderApi` | `createOrder()` / `getOrder()` / `updatePayOrderPrice()` |
| `PayRefundApi` | `createRefund()` / `getRefund()` |
| `PayTransferApi` | `createTransfer()` / `getTransfer()` |

### 1.3 ERP 模块（基础财务）

位于 `suxin-module-erp/src/main/java/cn/iocoder/suxin/module/erp/`

#### 财务相关表

| 表名 | DO 类 | 关键字段 |
|---|---|---|
| `erp_account` | `ErpAccountDO` | id、name、no、remark、status、sort、defaultStatus — **结算账户** |
| `erp_finance_payment` | `ErpFinancePaymentDO` | id、no、status、paymentTime、financeUserId、supplierId、accountId、totalPrice、discountPrice、**paymentPrice** |
| `erp_finance_payment_item` | `ErpFinancePaymentItemDO` | id、paymentId、bizType、bizId、bizNo、totalPrice(应付)、paidPrice(已付)、**paymentPrice(本次付款)** |
| `erp_finance_receipt` | `ErpFinanceReceiptDO` | id、no、status、receiptTime、financeUserId、customerId、accountId、totalPrice、discountPrice、**receiptPrice** |
| `erp_finance_receipt_item` | `ErpFinanceReceiptItemDO` | id、receiptId、bizType、bizId、bizNo、totalPrice(应收)、receiptedPrice(已收)、**receiptPrice(本次收款)** |

#### 业务类型枚举

`ErpBizTypeEnum`: PURCHASE_ORDER(10)、PURCHASE_IN(11)、PURCHASE_RETURN(12)、SALE_ORDER(20)、SALE_OUT(21)、SALE_RETURN(22)

**设计亮点：** 收/付款单的 item 通过 `bizType + bizId` 关联业务单据，支持**多次收/付款**（应付-已付-本次付款），这是一个标准的"核销"模式。

### 1.4 交易模块（Trade）

位于 `suxin-module/.../sales/trade/`

| 表名 | 关键财务字段 |
|---|---|
| `trade_order` | payOrderId、payStatus、payTime、payChannelCode、totalPrice、discountPrice、deliveryPrice、adjustPrice、payPrice、couponPrice、pointPrice、vipPrice、refundPrice、refundStatus、brokerageUserId |
| `trade_after_sale` | payRefundId — 售后关联退款 |
| `trade_brokerage_record` | bizId、price、totalPrice、frozenDays、unfreezeTime — 佣金记录 |
| `trade_brokerage_withdraw` | payTransferId — 佣金提现关联转账 |

### 1.5 现有能力总结与缺口

| 能力域 | 现状 | 缺口 |
|---|---|---|
| 支付网关 | ✅ 微信/支付宝/钱包，完整订单/退款/转账 | — |
| 钱包系统 | ✅ 余额、流水、冻结、充值套餐 | — |
| 收付款单 | ✅ ERP 有收款单/付款单（含明细） | 缺少与支付网关的自动对账 |
| **会计科目表** | ❌ | **完全缺失** |
| **记账凭证** | ❌ | **完全缺失** — 任何交易都未生成借贷分录 |
| **科目余额/总账** | ❌ | **完全缺失** |
| **电子发票** | ❌ | **完全缺失** — 金税四期要求 |
| **税务管理** | ❌ | **完全缺失** — 无税率配置、无增值税管理 |
| **渠道对账** | ❌ | **完全缺失** — 无支付宝/微信账单下载比对 |
| **商户结算** | ❌ | 钱包有余额，但无系统化结算批次 |
| **财务报表** | ❌ | **完全缺失** — 无资产负债表/利润表/现金流量表 |
| **会计期间/期末结转** | ❌ | **完全缺失** |

---

## 二、中国财务管理规范要求

### 2.1 企业会计准则（CAS）

中国采用**企业会计准则**（与 IFRS 实质性趋同），核心要求：

#### 会计科目编码体系（4 级数字编码）

| 首位 | 类别 | 示例 |
|---|---|---|
| 1 | 资产类 | 1001 库存现金、1002 银行存款、1122 应收账款、1405 库存商品 |
| 2 | 负债类 | 2001 短期借款、2202 应付账款、2221 应交税费 |
| 3 | 共同类 | （金融企业专用） |
| 4 | 所有者权益类 | 4001 实收资本、4103 本年利润、4104 利润分配 |
| 5 | 成本类 | 5001 生产成本、5101 制造费用 |
| 6 | 损益类 | 6001 主营业务收入、6401 主营业务成本、6601 销售费用、6602 管理费用 |

#### 记账凭证规范

依据《会计基础工作规范》，每张凭证必须包含：

| 字段 | 说明 |
|---|---|
| 凭证日期 | 编制日期 |
| 凭证编号 | 连续、不重复 |
| 经济业务摘要 | 交易描述 |
| 会计科目 | 借方/贷方科目 + 金额 |
| 附件张数 | 原始凭证数量 |
| 制单人 / 复核人 / 记账人 / 主管 | 财务人员签章 |

**借贷规则：**
- 允许一借多贷、多借一贷
- 一般禁止多借多贷（常规业务）
- 每张凭证借贷必须平衡：Σ借方 = Σ贷方

#### 凭证类型

| 类型 | 说明 |
|---|---|
| 收款凭证 | 借方固定为现金/银行存款 |
| 付款凭证 | 贷方固定为现金/银行存款 |
| 转账凭证 | 不涉及现金/银行存款的转账业务 |

### 2.2 金税四期 + 数电票（GB/T 44554.5-2025）

**财会〔2025〕9号**要求：

- 全面数字化电子发票（数电票），XML 结构化数据格式
- XML 含数字签名，纸质打印件不再具有法律效力
- 接收方必须解析 XML 数据进行报销入账
- 发票需存储 XML 原文 + 关键解析字段

**数电票必要数据项：**
- 购销双方统一社会信用代码、名称
- 发票类型代码、发票号码、开票日期
- 商品/服务明细：名称、规格、单位、数量、单价、金额、税率、税额
- 合计金额（不含税、税额、含税）
- 数字签名块
- 验证二维码 URL

**发票生命周期：** 开具 → 红冲 → 查验 → 入账（同一份 XML 贯穿全流程）

### 2.3 电商特有规范

#### 收入确认（CAS 14 ≈ IFRS 15）

电商平台需区分**主要责任人 vs 代理人**：

| 模式 | 角色 | 收入确认法 |
|---|---|---|
| 自营（京东直营） | 主要责任人 Principal | **总额法** — 全额确认收入，成本结转对应 |
| 第三方平台（淘宝/天猫） | 代理人 Agent | **净额法** — 仅佣金/服务费确认收入 |

**五项判断指标：** ① 谁承担履约责任 ② 谁承担库存风险 ③ 谁有定价权 ④ 谁承担信用风险 ⑤ 对价形式是否为佣金

#### 电商核算分录参考

**自营模式（总额法）：**
```
借: 其他货币资金—支付宝            （实际到账 = 商品售价 - 平台费率）
    销售费用—平台佣金
  贷: 主营业务收入                  （商品售价 / 1.13）
      应交税费—应交增值税(销项税额)
```

**平台模式（净额法/佣金）：**
```
借: 其他货币资金—平台账户
  贷: 其他应付款—商家待结算          （应付给商家的货款）
      主营业务收入—平台服务费        （佣金收入 / 1.06）
      应交税费—应交增值税(销项税额)
```

#### 增值税税率

| 场景 | 税率 | 适用 |
|---|---|---|
| 货物销售（一般纳税人） | **13%** | 实物商品 |
| 服务 | **6%** | 平台佣金、技术服务、广告、会员费 |
| 交通运输 | **9%** | 物流服务 |
| 小规模纳税人 | **1%** | 至 2027 年底（原 3% 减按 1%） |

#### 企业所得税

| 类型 | 税率 |
|---|---|
| 标准 | **25%** |
| 小微企业（应纳税所得 ≤ 300 万） | 实际 **5%** |
| 跨境电商综试区 | 核定利润率 **4%** |

### 2.4 电商结算体系

标准的三阶段模型：

```
交易阶段 → 清算阶段 → 结算阶段
(Transaction)  (Clearing)    (Settlement)
 下单+支付      数据交换+       实际资金划拨
               费用计算       到商户账户
```

**结算周期：**
- T+1：标准 B2C（工作日次日）
- D+1：自然日（含节假日，微信/支付宝常用）
- D+0/S0：实时结算（高频商户）

**结算账户体系：**

| 账户 | 用途 |
|---|---|
| 清算往来户 | 待从支付渠道归集的在途资金 |
| 商户待结算户 | 已收款但未到结算周期的资金 |
| 商户结算户 | 已完成清算待提现 |
| 平台收入户 | 平台自有收入（佣金、服务费） |
| 银行账户 | 实际银行账户 |

### 2.5 渠道对账

核心对账流程：

```
支付宝/微信账单下载 → 解析 → 与系统记录匹配
    ├── 匹配成功（金额 + 时间 + 单号）→ 自动对账
    └── 未匹配 → 异常队列
            ├── 长款（渠道多）→ 待查存款
            └── 短款（渠道少）→ 待查手续费/拒付/延迟
```

**三维匹配算法：** ① 金额匹配（容忍手续费四舍五入差异）② 时间接近 ③ 单号匹配

### 2.6 法定财务报表

| 报表 | 内容 | 报送要求 |
|---|---|---|
| 资产负债表 | 资产/负债/所有者权益（时点） | 年报（年度终了 4 个月内，审计后） |
| 利润表 | 收入/费用/利润（期间） | 年报 + 季报（上市公司） |
| 现金流量表 | 现金流入流出（直接法+间接法） | 年报 |
| 所有者权益变动表 | 权益各项目变动 | 年报 |
| 附注 | 补充披露 | 年报 |

---

## 三、建议扩展的数据库设计

以下新表建议放在 `suxin-module-erp` 下，遵循现有 ERP 模块的命名范式 `erp_*`。

### 3.1 会计科目表 `erp_accounting_subject`

```sql
CREATE TABLE erp_accounting_subject (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '科目编号',
    tenant_id       BIGINT        NOT NULL                COMMENT '租户编号',
    code            VARCHAR(20)   NOT NULL                COMMENT '科目编码，层级用.分隔，如 1001, 1002.01',
    name            VARCHAR(100)  NOT NULL                COMMENT '科目名称',
    parent_id       BIGINT        DEFAULT 0               COMMENT '父级科目 ID，0 为顶级',
    level           TINYINT       NOT NULL                COMMENT '层级，1-4',
    category        VARCHAR(20)   NOT NULL                COMMENT '科目类别: ASSET/LIABILITY/EQUITY/COST/PROFIT_LOSS',
    direction       VARCHAR(20)   NOT NULL DEFAULT 'DEBIT' COMMENT '余额方向: DEBIT(借方)/CREDIT(贷方)',
    is_cash_flow    BIT           DEFAULT 0               COMMENT '是否现金流量科目',
    is_enabled      BIT           DEFAULT 1               COMMENT '是否启用',
    is_system       BIT           DEFAULT 0               COMMENT '是否系统预设（不可删除）',
    sort            INT           DEFAULT 0               COMMENT '排序',
    remark          VARCHAR(500)                          COMMENT '备注',
    creator         VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         BIT           NOT NULL DEFAULT 0       COMMENT '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_code (tenant_id, code)
) COMMENT='会计科目表';
```

**预设科目初始化数据（商城核心科目）：**

```
资产类:
  1001  库存现金
  1002  银行存款
  1002.01  银行存款—基本户
  1002.02  银行存款—支付宝
  1002.03  银行存款—微信
  1012  其他货币资金
  1012.01  其他货币资金—支付宝待清算
  1012.02  其他货币资金—微信待清算
  1122  应收账款
  1122.01  应收账款—商家
  1221  其他应收款
  1405  库存商品

负债类:
  2001  短期借款
  2202  应付账款
  2202.01  应付账款—供应商
  2203  预收账款
  2221  应交税费
  2221.01  应交增值税（销项税额）
  2221.02  应交增值税（进项税额）
  2221.03  应交企业所得税
  2241  其他应付款
  2241.01  其他应付款—商家待结算
  2241.02  其他应付款—用户退款

所有者权益类:
  4001  实收资本
  4103  本年利润
  4104  利润分配

损益类:
  6001  主营业务收入
  6001.01  主营业务收入—商品销售
  6001.02  主营业务收入—平台服务费
  6051  其他业务收入
  6401  主营业务成本
  6601  销售费用
  6601.01  销售费用—平台佣金
  6601.02  销售费用—推广费
  6601.03  销售费用—物流费
  6602  管理费用
  6801  所得税费用
```

### 3.2 记账凭证 `erp_accounting_voucher` + `erp_accounting_voucher_item`

```sql
CREATE TABLE erp_accounting_voucher (
    id                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '凭证编号',
    tenant_id         BIGINT        NOT NULL                COMMENT '租户编号',
    voucher_no        VARCHAR(32)   NOT NULL                COMMENT '凭证号（如 PZ-202506-0001）',
    voucher_date      DATE          NOT NULL                COMMENT '凭证日期',
    voucher_type      VARCHAR(20)   NOT NULL                COMMENT '凭证类型: RECEIPT(收款)/PAYMENT(付款)/TRANSFER(转账)',
    accounting_period VARCHAR(7)    NOT NULL                COMMENT '会计期间，如 2025-06',
    summary           VARCHAR(500)  NOT NULL                COMMENT '摘要',
    attachment_count  INT           DEFAULT 0               COMMENT '附件张数',
    total_debit       DECIMAL(18,2) NOT NULL DEFAULT 0.00   COMMENT '借方合计',
    total_credit      DECIMAL(18,2) NOT NULL DEFAULT 0.00   COMMENT '贷方合计',
    status            VARCHAR(20)   NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT(草稿)/REVIEWED(已复核)/BOOKED(已记账)/REVERSED(已冲销)',

    -- 关联业务单据（可选，实现原始凭证追溯）
    source_type       VARCHAR(30)                           COMMENT '来源类型: PAY_ORDER/REFUND/SETTLEMENT/RECEIPT/PAYMENT/MANUAL',
    source_id         BIGINT                                COMMENT '来源单据 ID',
    source_no         VARCHAR(64)                           COMMENT '来源单号',

    -- 签章
    prepared_by       VARCHAR(64)                           COMMENT '制单人',
    reviewed_by       VARCHAR(64)                           COMMENT '复核人',
    booked_by         VARCHAR(64)                           COMMENT '记账人',
    approved_by       VARCHAR(64)                           COMMENT '主管',

    creator           VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
    create_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater           VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
    update_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted           BIT           NOT NULL DEFAULT 0       COMMENT '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_voucher_no (tenant_id, voucher_no),
    KEY idx_period (tenant_id, accounting_period),
    KEY idx_status (tenant_id, status)
) COMMENT='记账凭证';

CREATE TABLE erp_accounting_voucher_item (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '分录编号',
    voucher_id      BIGINT        NOT NULL                COMMENT '凭证 ID',
    line_no         INT           NOT NULL                COMMENT '行号',
    subject_code    VARCHAR(20)   NOT NULL                COMMENT '科目编码（关联 accounting_subject.code）',
    subject_name    VARCHAR(100)  NOT NULL                COMMENT '科目名称（冗余）',
    direction       VARCHAR(10)   NOT NULL                COMMENT '借贷方向: DEBIT(借)/CREDIT(贷)',
    amount          DECIMAL(18,2) NOT NULL                COMMENT '金额',
    summary         VARCHAR(500)                          COMMENT '分录摘要',

    -- 辅助核算（可选扩展）
    customer_id     BIGINT                                COMMENT '客户/商家 ID',
    supplier_id     BIGINT                                COMMENT '供应商 ID',
    account_id      BIGINT                                COMMENT '结算账户 ID',
    product_id      BIGINT                                COMMENT '商品 ID',

    creator         VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_voucher (voucher_id),
    KEY idx_subject (subject_code)
) COMMENT='记账凭证分录';
```

**凭证状态流转：**

```
DRAFT(草稿) → REVIEWED(已复核) → BOOKED(已记账)
                                      ↓
                                 REVERSED(已冲销)
                                      ↑
                              （通过红字冲销凭证）
```

**关键约束：**
- `BOOKED` 状态不可修改/删除，只能通过红字冲销更正
- 同凭证 `SUM(debit) = SUM(credit)`，不平衡不允许保存
- 凭证号按月连续编号

### 3.3 科目余额表 `erp_account_balance`

```sql
CREATE TABLE erp_account_balance (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id       BIGINT        NOT NULL                COMMENT '租户编号',
    subject_code    VARCHAR(20)   NOT NULL                COMMENT '科目编码',
    accounting_period VARCHAR(7)  NOT NULL                COMMENT '会计期间',

    -- 期初余额
    opening_debit   DECIMAL(18,2) DEFAULT 0.00            COMMENT '期初借方余额',
    opening_credit  DECIMAL(18,2) DEFAULT 0.00            COMMENT '期初贷方余额',

    -- 本期发生额
    current_debit   DECIMAL(18,2) DEFAULT 0.00            COMMENT '本期借方发生额',
    current_credit  DECIMAL(18,2) DEFAULT 0.00            COMMENT '本期贷方发生额',

    -- 期末余额
    closing_debit   DECIMAL(18,2) DEFAULT 0.00            COMMENT '期末借方余额',
    closing_credit  DECIMAL(18,2) DEFAULT 0.00            COMMENT '期末贷方余额',

    creator         VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_subject_period (tenant_id, subject_code, accounting_period)
) COMMENT='科目余额表';
```

### 3.4 会计期间 `erp_financial_period`

```sql
CREATE TABLE erp_financial_period (
    id              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id       BIGINT      NOT NULL                COMMENT '租户编号',
    period          VARCHAR(7)  NOT NULL                COMMENT '会计期间，如 2025-06',
    start_date      DATE        NOT NULL                COMMENT '期间开始日期',
    end_date        DATE        NOT NULL                COMMENT '期间结束日期',
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN'  COMMENT '状态: OPEN(打开)/CLOSING(结转中)/CLOSED(已关闭)',
    closed_at       DATETIME                            COMMENT '关闭时间',
    closed_by       VARCHAR(64)                         COMMENT '关闭人',
    creator         VARCHAR(64) DEFAULT ''              COMMENT '创建者',
    create_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_period (tenant_id, period)
) COMMENT='会计期间';
```

### 3.5 电子发票 `erp_einvoice`

```sql
CREATE TABLE erp_einvoice (
    id                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id           BIGINT       NOT NULL                COMMENT '租户编号',

    -- 发票基本信息
    invoice_code        VARCHAR(20)                         COMMENT '发票代码',
    invoice_no          VARCHAR(20)  NOT NULL                COMMENT '发票号码（数电票统一赋码）',
    invoice_date        DATE         NOT NULL                COMMENT '开票日期',
    invoice_type        VARCHAR(20)  NOT NULL                COMMENT '发票类型: NORMAL(普票)/SPECIAL(专票)/E_NORMAL(数电普票)/E_SPECIAL(数电专票)',

    -- 销方信息
    seller_name         VARCHAR(200) NOT NULL                COMMENT '销方名称',
    seller_tax_id       VARCHAR(20)  NOT NULL                COMMENT '销方统一社会信用代码',
    seller_address      VARCHAR(500)                        COMMENT '销方地址',
    seller_bank_account VARCHAR(200)                        COMMENT '销方银行账号',

    -- 购方信息
    buyer_name          VARCHAR(200) NOT NULL                COMMENT '购方名称',
    buyer_tax_id        VARCHAR(20)                         COMMENT '购方统一社会信用代码',
    buyer_address       VARCHAR(500)                        COMMENT '购方地址',
    buyer_bank_account  VARCHAR(200)                        COMMENT '购方银行账号',

    -- 金额
    amount_excluding_tax DECIMAL(18,2) NOT NULL             COMMENT '不含税金额合计',
    tax_amount           DECIMAL(18,2) NOT NULL             COMMENT '税额合计',
    amount_including_tax DECIMAL(18,2) NOT NULL             COMMENT '含税金额合计',

    -- 关联
    voucher_id          BIGINT                              COMMENT '关联记账凭证 ID',
    source_type         VARCHAR(30)                         COMMENT '来源: TRADE_ORDER / ERP_SALE_ORDER',
    source_id           BIGINT                              COMMENT '来源单据 ID',

    -- 状态
    status              VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT(草稿)/ISSUED(已开具)/VOIDED(已红冲)/VERIFIED(已查验)/BOOKED(已入账)',
    verification_time   DATETIME                            COMMENT '查验时间',

    -- 数电票 XML
    xml_data            MEDIUMTEXT                          COMMENT '数电票 XML 原文（含数字签名）',
    xml_hash            VARCHAR(64)                         COMMENT 'XML 文件 SHA256 Hash',

    -- 二维码
    qr_code_url         VARCHAR(500)                        COMMENT '查验二维码 URL',

    creator             VARCHAR(64)  DEFAULT ''             COMMENT '创建者',
    create_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater             VARCHAR(64)  DEFAULT ''             COMMENT '更新者',
    update_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             BIT          NOT NULL DEFAULT 0      COMMENT '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_invoice_no (tenant_id, invoice_no),
    KEY idx_buyer (tenant_id, buyer_tax_id),
    KEY idx_voucher (voucher_id)
) COMMENT='电子发票';
```

### 3.6 税率配置 `erp_tax_config`

```sql
CREATE TABLE erp_tax_config (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id       BIGINT        NOT NULL                COMMENT '租户编号',
    tax_type        VARCHAR(20)   NOT NULL                COMMENT '税种: VAT(增值税)/CIT(企业所得税)/SURCHARGE(附加税)',
    tax_item        VARCHAR(50)   NOT NULL                COMMENT '税目: GOODS_13/SERVICE_6/LOGISTICS_9/SMALL_1',
    tax_name        VARCHAR(100)  NOT NULL                COMMENT '税目名称: 货物销售13%/服务6%/交通运输9%/小规模1%',
    tax_rate        DECIMAL(5,4)  NOT NULL                COMMENT '税率, 如 0.1300=13%',
    effective_from  DATE          NOT NULL                COMMENT '生效日期',
    effective_to    DATE                                  COMMENT '失效日期（NULL 表示有效中）',
    is_default      BIT           DEFAULT 0               COMMENT '是否默认税率',
    remark          VARCHAR(500)                          COMMENT '备注',
    creator         VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_tenant_type (tenant_id, tax_type)
) COMMENT='税率配置';
```

### 3.7 对账单 `erp_reconciliation_bill` + `erp_reconciliation_detail`

```sql
CREATE TABLE erp_reconciliation_bill (
    id                      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id               BIGINT        NOT NULL                COMMENT '租户编号',
    bill_no                 VARCHAR(32)   NOT NULL                COMMENT '对账单号',
    bill_date               DATE          NOT NULL                COMMENT '对账日期',
    channel                 VARCHAR(20)   NOT NULL                COMMENT '渠道: ALIPAY/WECHAT/BANK_ACCOUNT',
    channel_bill_file       VARCHAR(500)                          COMMENT '渠道账单文件路径',

    -- 汇总
    total_system_count      INT           DEFAULT 0               COMMENT '系统记录笔数',
    total_channel_count     INT           DEFAULT 0               COMMENT '渠道记录笔数',
    total_system_amount     DECIMAL(18,2) DEFAULT 0.00            COMMENT '系统金额合计',
    total_channel_amount    DECIMAL(18,2) DEFAULT 0.00            COMMENT '渠道金额合计',

    -- 匹配结果
    matched_count           INT           DEFAULT 0               COMMENT '匹配成功笔数',
    unmatched_count         INT           DEFAULT 0               COMMENT '未匹配笔数',
    long_count              INT           DEFAULT 0               COMMENT '长款笔数（渠道有，系统无）',
    short_count             INT           DEFAULT 0               COMMENT '短款笔数（系统有，渠道无）',
    difference_amount       DECIMAL(18,2) DEFAULT 0.00            COMMENT '差异金额（渠道 - 系统）',

    status                  VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING(待对账)/MATCHED(完全匹配)/PARTIAL(部分匹配)/EXCEPTION(异常)/RESOLVED(已处理)',
    resolved_by             VARCHAR(64)                           COMMENT '处理人',
    resolved_time           DATETIME                              COMMENT '处理时间',
    remark                  VARCHAR(500)                          COMMENT '备注',

    creator                 VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
    create_time             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater                 VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
    update_time             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_bill (tenant_id, bill_date, channel)
) COMMENT='渠道对账单';

CREATE TABLE erp_reconciliation_detail (
    id                  BIGINT        NOT NULL AUTO_INCREMENT COMMENT '编号',
    bill_id             BIGINT        NOT NULL                COMMENT '对账单 ID',
    transaction_date    DATE          NOT NULL                COMMENT '交易日期',
    channel_ref_no      VARCHAR(64)                          COMMENT '渠道交易流水号',
    system_ref_no       VARCHAR(64)                          COMMENT '系统交易单号（pay_order.no / pay_refund.no）',
    channel_amount      DECIMAL(18,2) NOT NULL               COMMENT '渠道金额',
    system_amount       DECIMAL(18,2) NOT NULL               COMMENT '系统金额',
    match_status        VARCHAR(20)   NOT NULL                COMMENT '匹配状态: MATCHED(匹配)/LONG(长款)/SHORT(短款)/MANUAL(人工处理)',
    difference_reason   VARCHAR(500)                          COMMENT '差异原因',
    adjustment_entry    VARCHAR(20)                           COMMENT '调整分录方式',

    creator             VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
    create_time         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_bill (bill_id),
    KEY idx_match (match_status)
) COMMENT='对账明细';
```

### 3.8 结算批次 `erp_settlement_batch` + `erp_settlement_detail`

```sql
CREATE TABLE erp_settlement_batch (
    id                  BIGINT        NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id           BIGINT        NOT NULL                COMMENT '租户编号',
    batch_no            VARCHAR(32)   NOT NULL                COMMENT '结算批次号',
    period_start        DATE          NOT NULL                COMMENT '结算周期开始',
    period_end          DATE          NOT NULL                COMMENT '结算周期结束',
    settlement_type     VARCHAR(20)   NOT NULL                COMMENT '结算类型: MERCHANT(商家)/BROKERAGE(佣金)/SUPPLIER(供应商)',

    -- 金额汇总
    total_order_count   INT           DEFAULT 0               COMMENT '订单总数',
    total_order_amount  DECIMAL(18,2) DEFAULT 0.00            COMMENT '订单总金额',
    total_fee           DECIMAL(18,2) DEFAULT 0.00            COMMENT '手续费/佣金合计',
    total_settle_amount DECIMAL(18,2) DEFAULT 0.00            COMMENT '应付结算金额',

    status              VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING(待结算)/SETTLING(结算中)/SETTLED(已结算)/CANCELLED(已取消)',
    settled_at          DATETIME                              COMMENT '结算完成时间',

    -- 关联凭证
    voucher_id          BIGINT                                COMMENT '关联记账凭证 ID',

    creator             VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
    create_time         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater             VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
    update_time         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_batch (tenant_id, batch_no)
) COMMENT='结算批次';

CREATE TABLE erp_settlement_detail (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '编号',
    batch_id        BIGINT        NOT NULL                COMMENT '结算批次 ID',
    merchant_id     BIGINT        NOT NULL                COMMENT '商户/商家 ID',
    order_id        BIGINT        NOT NULL                COMMENT '订单 ID（TradeOrderDO.id）',
    order_amount    DECIMAL(18,2) NOT NULL               COMMENT '订单金额',
    fee_rate        DECIMAL(5,4)  NOT NULL               COMMENT '费率',
    fee_amount      DECIMAL(18,2) NOT NULL               COMMENT '费用金额',
    settle_amount   DECIMAL(18,2) NOT NULL               COMMENT '结算金额',
    creator         VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_batch (batch_id),
    KEY idx_merchant (merchant_id)
) COMMENT='结算明细';
```

### 3.9 现有表扩展建议

#### `erp_account` 扩展

```sql
ALTER TABLE erp_account ADD COLUMN account_type VARCHAR(20) DEFAULT 'BANK' COMMENT '账户类型: BANK(银行)/ALIPAY(支付宝)/WECHAT(微信)/INTERNAL_CLEARING(内部清算)/VIRTUAL(虚拟户)';
ALTER TABLE erp_account ADD COLUMN currency VARCHAR(3) DEFAULT 'CNY' COMMENT '币种';
ALTER TABLE erp_account ADD COLUMN opening_balance DECIMAL(18,2) DEFAULT 0.00 COMMENT '期初余额';
ALTER TABLE erp_account ADD COLUMN current_balance DECIMAL(18,2) DEFAULT 0.00 COMMENT '当前余额';
```

#### `erp_finance_receipt` / `erp_finance_payment` 扩展

```sql
ALTER TABLE erp_finance_receipt ADD COLUMN reconciled_status VARCHAR(20) DEFAULT 'UNRECONCILED' COMMENT '对账状态: UNRECONCILED(未对账)/RECONCILED(已对账)/DIFFERENCE(有差异)';
ALTER TABLE erp_finance_receipt ADD COLUMN reconciliation_bill_id BIGINT COMMENT '对账单 ID';
ALTER TABLE erp_finance_payment ADD COLUMN reconciled_status VARCHAR(20) DEFAULT 'UNRECONCILED' COMMENT '对账状态';
ALTER TABLE erp_finance_payment ADD COLUMN reconciliation_bill_id BIGINT COMMENT '对账单 ID';
```

---

## 四、系统架构建议

### 4.1 推荐模块结构

建议在 `suxin-module-erp` 下扩展财务子模块：

```
suxin-module-erp/
└── src/main/java/cn/iocoder/suxin/module/erp/
    ├── dal/dataobject/finance/
    │   ├── ErpAccountDO.java                        (已有 — 扩展)
    │   ├── ErpFinancePaymentDO.java                 (已有 — 扩展)
    │   ├── ErpFinancePaymentItemDO.java             (已有)
    │   ├── ErpFinanceReceiptDO.java                 (已有 — 扩展)
    │   ├── ErpFinanceReceiptItemDO.java             (已有)
    │   ├── ErpAccountingSubjectDO.java              (新增)
    │   ├── ErpAccountingVoucherDO.java              (新增)
    │   ├── ErpAccountingVoucherItemDO.java          (新增)
    │   ├── ErpAccountBalanceDO.java                 (新增)
    │   ├── ErpFinancialPeriodDO.java                (新增)
    │   ├── ErpEinvoiceDO.java                       (新增)
    │   ├── ErpTaxConfigDO.java                      (新增)
    │   ├── ErpReconciliationBillDO.java             (新增)
    │   ├── ErpReconciliationDetailDO.java           (新增)
    │   ├── ErpSettlementBatchDO.java                (新增)
    │   └── ErpSettlementDetailDO.java               (新增)
    │
    ├── dal/mysql/finance/
    │   └── (对应的 Mapper 接口)
    │
    ├── service/finance/
    │   ├── ErpAccountingSubjectService.java         (新增)
    │   ├── ErpAccountingVoucherService.java         (新增 — 核心)
    │   ├── ErpAccountBalanceService.java            (新增)
    │   ├── ErpFinancialPeriodService.java           (新增)
    │   ├── ErpEinvoiceService.java                  (新增)
    │   ├── ErpTaxService.java                       (新增)
    │   ├── ErpReconciliationService.java            (新增)
    │   └── ErpSettlementService.java                (新增)
    │
    ├── controller/admin/finance/
    │   ├── ErpAccountingSubjectController.java      (新增)
    │   ├── ErpAccountingVoucherController.java      (新增)
    │   ├── ErpAccountBalanceController.java         (新增)
    │   ├── ErpFinancialPeriodController.java        (新增)
    │   ├── ErpEinvoiceController.java               (新增)
    │   ├── ErpReconciliationController.java         (新增)
    │   └── ErpSettlementController.java             (新增)
    │
    └── job/
        ├── ErpReconciliationJob.java                (新增 — T+1 自动对账)
        ├── ErpSettlementJob.java                    (新增 — 结算批次生成)
        └── ErpPeriodCloseJob.java                   (新增 — 月末结转)
```

### 4.2 核心业务流程

#### 4.2.1 支付 → 凭证自动生成

```
TradeOrder 支付成功
    │
    ├── PayOrder.status = SUCCESS
    │
    └── [自动记账引擎] AccountingVoucherAutoGenerator
            │
            ├── 判断业务类型 → 选择分录模板
            │   ├── 自营商品销售 → 总额法分录
            │   ├── 平台佣金收入 → 净额法分录
            │   └── 充值到账 → 资金转入分录
            │
            ├── 生成 ErpAccountingVoucherDO + Items
            │   └── 自动平衡校验: SUM(借方) == SUM(贷方)
            │
            ├── 状态: DRAFT → 自动复核 → BOOKED
            │
            └── 更新 ErpAccountBalanceDO（当期发生额）
```

#### 4.2.2 渠道对账流程

```
[定时任务] ErpReconciliationJob (T+1 每天 9:00)
    │
    ├── 下载支付宝/微信账单文件
    │
    ├── 解析 → ErpReconciliationBillDO + Details
    │
    ├── 自动匹配引擎
    │   ├── 规则1: channel_ref_no ↔ pay_order.no (精确匹配)
    │   ├── 规则2: amount + 时间窗口 (模糊匹配)
    │   └── 规则3: 金额差异 < 0.01 (容忍四舍五入)
    │
    ├── 结果分类
    │   ├── MATCHED → 自动对账完成
    │   ├── LONG → 待查（渠道多款）
    │   └── SHORT → 待查（系统多款/渠道扣费）
    │
    └── 生成对账报告，异常发送通知
```

#### 4.2.3 商户结算流程

```
[定时/手动触发] ErpSettlementJob
    │
    ├── 查询结算周期内已支付且未结算的订单
    │
    ├── 生成 ErpSettlementBatchDO
    │   ├── 按商户分组
    │   ├── 计算佣金 = 订单金额 × 费率
    │   └── 结算金额 = 订单金额 - 佣金 - 手续费
    │
    ├── 生成 ErpSettlementDetailDO
    │
    ├── 生成记账凭证（转账凭证）
    │   ├── 借: 主营业务成本 / 其他应付款—商家待结算
    │   └── 贷: 主营业务收入—平台服务费 / 其他货币资金
    │
    └── 通知商家结算结果
```

#### 4.2.4 月末结转流程

```
[定时任务] ErpPeriodCloseJob (每月 1 日 0:00)
    │
    ├── 校验当期凭证全部 BOOKED
    │
    ├── 结转损益类科目到本年利润
    │   ├── 借: 主营业务收入 → 贷: 本年利润
    │   ├── 借: 本年利润 → 贷: 主营业务成本
    │   └── 借: 本年利润 → 贷: 各项费用
    │
    ├── 生成科目余额表（当期快照）
    │
    ├── 关闭当前期间: OPEN → CLOSED
    │
    └── 生成下一期间: OPEN
```

### 4.3 关键设计原则

| 原则 | 说明 |
|---|---|
| **双记账** | 所有财务交易必须生成借贷平衡的凭证，不平衡不允许保存 |
| **不可篡改** | BOOKED 状态的凭证不可修改/删除，错误只能通过红字冲销更正 |
| **多租户隔离** | 所有财务表含 `tenant_id`，各租户独立的科目表、凭证、会计期间 |
| **可追溯** | 凭证关联原始业务单据（source_type + source_id），实现"凭证 → 原始凭证"的追溯链 |
| **幂等性** | 同一笔业务交易不会生成重复凭证（通过 source_type + source_id 唯一约束） |
| **异步处理** | 对账、结算、结转等批量操作使用 Quartz 定时任务 + 线程池，避免阻塞业务 |

### 4.4 与现有模块的集成

```
┌────────────────────────────────────────────────────────────────┐
│                        Trade 交易模块                           │
│  TradeOrderDO ──── payOrderId ────→ PayOrderDO (支付模块)      │
│  AfterSaleDO  ──── payRefundId ───→ PayRefundDO (支付模块)     │
└──────────────────────────┬─────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Accountant 支付模块                            │
│  PayOrderDO / PayRefundDO / PayTransferDO                       │
│  AccountDO / AccountTransactionDO (钱包)                         │
└──────────────────────────┬──────────────────────────────────────┘
                           │ 自动记账
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                   ERP 财务模块 (新增)                             │
│                                                                  │
│  ┌─────────────────┐  ┌─────────────────┐  ┌────────────────┐  │
│  │ 会计科目表       │  │ 记账凭证         │  │ 科目余额表     │  │
│  │ (科目编码体系)   │→ │ (借贷双分录)     │→ │ (期间快照)     │  │
│  └─────────────────┘  └─────────────────┘  └────────┬───────┘  │
│                                                      │          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌───────▼────────┐  │
│  │ 结算批次         │  │ 渠道对账单       │  │ 财务报表       │  │
│  │ (商户/佣金结算)  │  │ (支付宝/微信)    │  │ (BS/IS/CF)     │  │
│  └─────────────────┘  └─────────────────┘  └────────────────┘  │
│                                                                  │
│  ┌─────────────────┐  ┌─────────────────┐                       │
│  │ 电子发票         │  │ 税务配置         │                       │
│  │ (数电票/金税)    │  │ (税率/税种)      │                       │
│  └─────────────────┘  └─────────────────┘                       │
└─────────────────────────────────────────────────────────────────┘
```

---

## 五、实施路线图

### 第一阶段：会计基础（P0 — 必须）

| 任务 | 说明 | 预估 |
|---|---|---|
| 会计科目表 CRUD | `erp_accounting_subject` 表 + 预设数据 + 管理界面 | 3天 |
| 记账凭证 CRUD | `erp_accounting_voucher` + `item` 表 + 手动录入 + 审核流程 | 5天 |
| 科目余额表 | `erp_account_balance` + 余额查询 + 总账 / 明细账 | 3天 |
| 会计期间管理 | `erp_financial_period` + 期间开关 + 月末结转 | 3天 |
| 凭证自动生成引擎 | 基于业务类型 + 分录模板的自动记账 | 5天 |

### 第二阶段：业务集成（P1 — 核心）

| 任务 | 说明 | 预估 |
|---|---|---|
| 支付到凭证集成 | PayOrder SUCCESS → 自动生成销售/佣金凭证 | 3天 |
| 退款到凭证集成 | PayRefund SUCCESS → 自动生成退款冲销凭证 | 2天 |
| 充值到凭证集成 | AccountRecharge → 资金转入凭证 | 1天 |
| 收/付款单到凭证集成 | ERP 收/付款单 → 凭证 | 2天 |

### 第三阶段：对账与结算（P2 — 重要）

| 任务 | 说明 | 预估 |
|---|---|---|
| 渠道对账引擎 | 支付宝/微信账单下载 → 解析 → 匹配 | 5天 |
| 商户结算系统 | 结算批次生成 + 佣金计算 + 结算凭证 | 5天 |
| 对账定时任务 | 每日自动对账 + 异常通知 | 2天 |
| 结算定时任务 | 按周期自动结算 | 2天 |

### 第四阶段：税务与报表（P3 — 增强）

| 任务 | 说明 | 预估 |
|---|---|---|
| 税率配置 | 多税种/多税率配置 | 1天 |
| 增值税管理 | 销项/进项发票登记 + 税金计算 | 3天 |
| 电子发票管理 | 数电票开具/红冲/查验 | 5天 |
| 资产负债表 | 基于科目余额快照生成 | 3天 |
| 利润表 | 基于损益类科目发生额生成 | 2天 |
| 现金流量表 | 基于现金科目 + 主表的间接法/直接法 | 3天 |

---

## 六、参考资料

- 《企业会计准则 — 基本准则》（财政部令第 33 号）
- 《企业会计准则第 14 号 — 收入》（CAS 14，IFRS 15 趋同）
- 《企业会计准则第 30 号 — 财务报表列报》
- 《会计基础工作规范》（财政部 2019 年修订）
- GB/T 44554.5-2025《全面数字化的电子发票》
- 财会〔2025〕9号《财政部关于规范电子会计凭证报销入账归档的通知》
- 国家税务总局关于增值税税率的相关公告（2025 年）
- [芋道 suxin/ruoyi-vue-pro](https://github.com/YunaiV/ruoyi-vue-pro) 开发平台

---

> **文档维护**: 此文档为商城财务模块设计蓝图，随着开发进展持续更新。设计决策请在此文档中记录。
>
> **作者**: 书心软件 / Claude Code
> **日期**: 2025-06-10
