# 项目指令

## 项目事实基线

| 项 | 值 |
|---|---|
| **GroupId** | `vip.appap.suxin` |
| **包名前缀** | `vip.appap.suxin.module.{模块名}` |
| **技术栈** | Java 17, Spring Boot 3.5, MyBatis-Plus, MySQL 8, Vue 3 + Element Plus |
| **项目名** | 书心 (suxin) |

## OpenSpec 工作流（优先）

本项目以 `openspec/` 作为需求、设计、实施和验收的协作基线。开始非微小改动前，先阅读 `openspec/project.md`、相关的当前规格和活动提案，并遵循 `openspec/AGENTS.md`：

- `openspec/specs/` 是当前已实现行为的唯一事实来源；不得将未实现的计划直接写入这里。
- 功能、接口、权限、数据模型、跨端行为和重构类改动，先在 `openspec/changes/<change-name>/` 创建提案、任务和 delta specs，再开始编码。
- 完成验证后，把最终行为同步至 `openspec/specs/`，再归档提案至 `openspec/changes/archive/`。
- 微小且不改变可观察行为的修复可不建提案；判断不确定时，默认创建提案。

## 模块结构

Maven 模块分为五类，只有 `suxin-module` 和 `suxin-module-base` 写业务代码：

```
suxin-dependencies/           — BOM 依赖管理
suxin-framework/
  ├── suxin-common/           — 通用基类、工具类
  ├── suxin-spring-boot-starter-*/  — 各种 starter
  ├── suxin-module-base/      — ★ 系统基础模块（位于 framework 下，无租户隔离）
  └── suxin-module-report/    — 报表
suxin-module/                 — ★ 拓展业务模块（租户隔离）
suxin-server/                 — 主启动模块
```

**suxin-module-base** 内部包含 4 个公共业务域（包路径 `vip.appap.suxin.module.*`）：

```
suxin-module-base/src/main/java/vip/appap/suxin/module/
├── system/    — 系统管理（用户/角色/菜单/租户/字典/通知/短信/邮件/OAuth2）
├── partner/   — 客商管理（客户/供应商/会员/等级/积分/签到/地址）
├── product/   — 产品中心（SPU/SKU/分类/品牌/属性/评价/收藏）
└── infra/     — 基础设施（配置/文件/日志/代码生成/定时任务/WebSocket）
```

**suxin-module** 内部包含 8 个业务子包：

```
suxin-module/src/main/java/vip/appap/suxin/module/
├── crm/         — CRM（线索/客户/商机/合同/回款/跟进）
├── sales/       — 交易/营销（订单/售后/优惠券/秒杀/拼团/分销/物流）
├── hr/          — 人力资源（员工/简历/合同/薪资/考勤）
├── im/          — 即时通讯（私聊/群聊/好友/音视频）
├── accountant/  — 会计中心（支付单/退款/转账/充值/账户流水）
├── marriage/    — 婚恋交友（用户/照片/动态/评论/点赞）
├── purchase/    — 采购管理
└── rongjh/      — 融计划
```

**架构原则**：
- `suxin-module-base` 是框架层模块，被 `suxin-server` 和 `suxin-module` 依赖，不依赖 `suxin-module`
- 公共业务（system/partner/product/infra）全部集中在 `suxin-module-base`，拓展业务（crm/sales/hr 等）集中在 `suxin-module`
- **所有业务代码集中在这两个模块中，不新建独立 Maven 模块**

## 命名规范

### 核心原则
**业务模块名 + 表名** — 所有命名（类名、方法名、权限注解）都以业务实体为准。比如 `partner` 前缀代表 partner 模块下的 partner 表的增删改查；不是 `partner/user`。

### 完整类名体系

| 角色 | 命名规则 | 示例 |
|---|---|---|
| DO 实体 | `{模块名}DO` | `PartnerDO`、`ProductSpuDO` |
| Mapper | `{模块名}Mapper` | `PartnerMapper` |
| Service 接口 | `{模块名}Service` | `PartnerService` |
| ServiceImpl | `{模块名}ServiceImpl` | `PartnerServiceImpl` |
| Admin Controller | `{模块名}Controller` | `PartnerController` |
| App Controller | `App{模块名}Controller` | `AppPartnerController` |
| API 接口 | `{模块名}Api` | `PartnerApi` |
| API 实现 | `{模块名}ApiImpl` | `PartnerApiImpl` |
| Convert | `{模块名}Convert` | `PartnerConvert` |

**Java 文件名必须与公共类名一致！**

### VO 命名（细分）

| 用途 | 命名 | 示例 |
|---|---|---|
| 新增/编辑入参 | `{Entity}SaveReqVO` | `PartnerSaveReqVO` |
| 分页查询入参 | `{Entity}PageReqVO` | `PartnerPageReqVO` |
| 列表查询入参 | `{Entity}ListReqVO` | `PartnerListReqVO` |
| 响应出参 | `{Entity}RespVO` | `PartnerRespVO` |
| 导入 Excel | `{Entity}ImportExcelVO` | `PartnerImportExcelVO` |
| 导入结果 | `{Entity}ImportRespVO` | `PartnerImportRespVO` |

### 方法名规范
方法名中的实体名必须与业务模块对齐，统一使用 `{模块名}` 而非其他名称：

- `create{Entity}` — 创建
- `update{Entity}` — 更新
- `delete{Entity}` — 删除
- `get{Entity}` — 查询单个
- `get{Entity}List` — 查询列表
- `get{Entity}Page` — 分页查询
- `get{Entity}By{Field}` — 按字段查询
- `validate{Entity}Exists` — 校验存在性
- `validate{Field}Unique` — 校验字段唯一性

### Controller 路由规范

| 端 | URL 前缀 | 示例 |
|---|---|---|
| 管理后台 | `/admin-api/{模块}/{资源}` | `/admin-api/partner/partner/page` |
| 用户 App | `/app-api/{模块}/{资源}` | `/app-api/partner/partner/get` |

### 权限注解规范
`@PreAuthorize("@ss.hasPermission('{模块}:{表名}:{操作}')")`
示例：`partner:partner:create`、`partner:partner:update`、`partner:partner:delete`、`partner:partner:query`

### 包结构规范
扁平化，不要冗余子包层级：
- ✅ `partner.dal.dataobject.PartnerDO`
- ❌ `partner.dal.dataobject.user.PartnerUserDO`（去掉 `user` 子包）

每业务模块标准包结构：
```
partner/
├── api/              — API 接口 + DTO（供其他模块调用）
├── controller/
│   ├── admin/        — 管理后台 Controller + vo/
│   └── app/          — 用户 App Controller + vo/
├── convert/          — MapStruct 转换器（DO ↔ VO / DTO）
├── dal/
│   ├── dataobject/   — DO 实体类
│   └── mysql/        — Mapper 接口
├── enums/            — 枚举 + 错误码常量（ErrorCodeConstants.java）
└── service/          — Service 接口 + ServiceImpl 实现
```

### 表名 & 字段命名
- 表名用下划线：`partner`、`partner_level`、`product_spu`、`product_sku`
- MES 表统一前缀 `mes_`，如 `mes_md_item`、`mes_wm_batch`
- 布尔字段 `is_` 前缀：`is_customer`、`is_member`
- 外键 `{表}_id`：`category_id`、`spu_id`
- 不使用复数形式

### 错误码规范
统一定义在 `enums/ErrorCodeConstants.java`，格式 `{模块编号}_{业务编号}_{序号}`：
```java
ErrorCode PARTNER_NOT_EXISTS = new ErrorCode(1_040_100_000, "合作伙伴不存在");
```

## 核心业务模型

### partner — 统一人员主数据
`partner` 表表达所有"人"的概念（客户、供应商、会员、员工）。通过布尔字段区分身份，同一人可同时拥有多种身份：

| 字段 | 说明 |
|---|---|
| `is_customer` | 是否客户 |
| `is_supplier` | 是否供应商 |
| `is_member` | 是否会员 |
| `is_company` | 是否公司 |

> 任何拓展业务涉及"人"时，必须先插入 `partner` 获取 ID，再插入其他业务表。

### product — 统一产品主数据
`product_spu`（产品主表）+ `product_sku`（产品明细表），通过 `type` 字段区分实体产品/服务产品/组合产品/会员产品。

### 模块间关系
所有业务单据围绕 **partner**（人）和 **product**（物）两个核心主数据运转：单据中的人员字段引用 `partner.id`，产品字段引用 `product_sku.id` 或 `product_spu.id`。

## 参数分层体系

系统参数根据业务范围存储在不同位置：

| 层级 | 存储位置 | 适用场景 |
|---|---|---|
| 应用级 | `application-{env}.yaml` | DB连接、Redis、框架开关 |
| 系统级 | `infra_config` 表 | 功能开关、URL配置、JSON业务配置 |
| 租户级 | `system_tenant` 表 | 租户名、套餐、到期时间 |
| 菜单级 | `system_menu.query_params` | 页面默认过滤参数（JSON） |
| 用户级 | `partner` 表字段 | 角色标记、会员等级、积分 |
| 产品级 | `product_spu` / `product_sku` 表 | 分类、价格、库存 |

> **参数速查：** 全局开关用 `infra_config`，单个用户/产品的参数直接加表字段，不要用 key-value。

## 数据字典

- 数据字典是系统级翻译表（`system_dict_type` + `system_dict_data`），**没有 `tenant_id`**，所有租户共享
- 命名格式：`{模块}_{对象}_{字段}`，如 `mes_wm_sales_notice_status`
- 后端用 `DictTypeConstants` 定义常量 key，用 `@DictFormat` 注解做 Excel 自动翻译
- 前端用 `<dict-tag :type="DICT_TYPE.XXX" :value="..." />` 展示
- **判断标准：** 全局通用值（性别、状态）用数据字典；各租户不同的值（自定义分类）建业务表加 `tenant_id`

## BaseService 功能清单

`BaseService` 位于 `suxin-module-base` 的 `system/util/BaseService.java`，提供单号生成、当前用户、redis 缓存、权限校验、字典翻译、文件上传等通用能力。

**继承规则：**

| 模块 | 是否继承 | 原因 |
|---|---|---|
| 业务模块（crm/sales/hr/mes/accountant...） | ✅ 继承 | 消费基础能力 |
| system/partner/product 模块 | ❌ 不继承 | 是基础能力的提供者 |

- 代码生成器已默认让生成的 Service 继承 BaseService
- 继承后所有依赖用 `@Lazy` 延迟注入，不用的不初始化
- 方法用 `protected` 修饰，只给子类使用
- 业务模块独有的功能放在本模块 `util/` 目录，不进 BaseService

## 编码规范

- 新增接口使用 `jakarta.validation.Valid`（非 `javax.validation`）
- Controller 使用 `@PermitAll` 或 `@PreAuthorize` 权限注解控制访问
- DO 实体不直接作为 Controller 出入参，必须通过 VO 转换
- 错误码统一定义在 `enums/ErrorCodeConstants.java`
- 前端使用动态路由（从 `system_menu` 表加载），静态路由在 `router/modules/remaining.ts` 作为补充

## 数据库访问规范

**使用 MCP 连接 MySQL 数据库进行查询，但不能执行增删改操作（SELECT only）。DDL/DML 操作需要输出 SQL 脚本由用户手动执行。**

- 本项目已配置 MySQL MCP：根目录 `.mcp.json` 中的 `mysql` 服务，连接 `suxin1_marriage_dev` 数据库
- 需要核对表结构、数据存在性、菜单权限、字典配置等数据库状态时，优先使用 MySQL MCP 执行只读 `SELECT` / `SHOW` 查询
- 禁止通过 MCP 执行 `INSERT`、`UPDATE`、`DELETE`、`ALTER`、`DROP`、`TRUNCATE` 等写入或结构变更操作

### SQL 文件留痕规范
- 每次准备对数据库执行 SQL 时，必须先在项目根目录 `docs/sql/` 下新增 `.sql` 文件并保存
- 文件名格式：`yyyy-MM-dd_数据库名_SQL作用的中文说明.sql`
- 示例：`2026-08-02_suxin1_查询签到积分配置.sql`

## 重构检查清单

进行大规模重命名时，按以下顺序执行：
1. 先创建新文件（不要直接改旧文件）
2. 更新所有 import 引用
3. 更新所有方法调用
4. 删除旧文件
5. 如用户明确要求，再执行编译验证
6. 检查是否有遗漏的引用（grep 搜索旧名称）

## 常见易错点
1. **文件名与类名不一致** — Java 要求公共类名必须与文件名相同
2. **重复 import** — 批量替换时可能产生重复 import，需要清理
3. **全限定名引用** — 有些地方使用全限定名（如 `vip.appap.suxin.module.partner.xxx`），需要特别注意
4. **前端 API 路径** — 后端 Controller 路径修改后，前端 API 文件也需要同步更新
5. **包前缀固定** — 所有 Java 代码包名均以 `vip.appap.suxin` 开头，不是 `cn.iocoder.yudao`
