# 项目指令

# 命名规范

## 核心原则
**业务模块名+表名** — 所有命名（类名、方法名、权限注解）都以业务实体为准。

比如 `partner/partner` 前缀代表 partner 模块下的 partner 表的增删改查。

## 类名规范
- DO 实体：`{模块名}DO`（如 `PartnerDO`，不是 `PartnerUserDO`）
- Mapper：`{模块名}Mapper`（如 `PartnerMapper`）
- Service 接口：`{模块名}Service`（如 `PartnerService`）
- Service 实现：`{模块名}ServiceImpl`（如 `PartnerServiceImpl`）
- Controller：`{模块名}Controller`（如 `PartnerController`）
- API 接口：`{模块名}Api`（如 `PartnerApi`）
- VO 类：`{模块名}{用途}VO`（如 `PartnerCreateReqVO`、`PartnerRespVO`）
- DTO 类：`{模块名}RespDTO`（如 `PartnerRespDTO`）
- Convert：`{模块名}Convert`（如 `PartnerConvert`）

**注意：Java 文件名必须与公共类名一致！**

## 方法名规范
方法名中的实体名必须与业务模块对齐，统一使用 `{模块名}` 而非其他名称：
- ✅ `createPartner`、`getPartner`、`updatePartner`、`deletePartner`
- ❌ `createUser`、`getUser`、`updateUser`、`deleteUser`（partner 模块中不应出现 User）

常见方法命名：
- `create{Partner}` — 创建
- `update{Partner}` — 更新
- `delete{Partner}` — 删除
- `get{Partner}` — 查询单个
- `get{Partner}List` — 查询列表
- `get{Partner}Page` — 分页查询
- `get{Partner}By{Field}` — 按字段查询
- `validate{Partner}Exists` — 校验存在性
- `validate{Field}Unique` — 校验字段唯一性

## 权限注解规范
`@PreAuthorize` 权限命名：`{模块名}:{表名}:{操作}`

示例：
- `partner:partner:create`
- `partner:partner:update`
- `partner:partner:delete`
- `partner:partner:query`

## 包结构规范
包结构扁平化，不要有冗余的子包层级：
- ✅ `partner.dal.dataobject.PartnerDO`
- ❌ `partner.dal.dataobject.user.PartnerUserDO`（去掉 `user` 子包）

标准包结构：
```
vip.appap.suxin.module.{模块名}
├── api/              # API 接口
│   └── dto/          # DTO
├── controller/
│   └── admin/
│       └── vo/       # VO
├── convert/          # MapStruct 转换
├── dal/
│   ├── dataobject/   # DO 实体
│   └── mysql/        # Mapper
├── enums/            # 枚举、错误码
└── service/          # Service
```

## System 模块包装类规范
当 system 模块需要调用其他模块时，通过反射包装类避免编译期依赖：
- 包装类命名：`{模块名}UserService`（如 `PartnerUserService`）
- 包装类位置：`system.service.{模块名}/`
- 包装类职责：仅提供 `get{Entity}Mobile`、`get{Entity}Email` 等基础查询

## 项目概述

基于 suxin-pro (ruoyi-vue-pro) 的 ERP 系统，Spring Boot 3.5 + MyBatis-Plus + Vue 3。

## 模块结构

- `suxin-module` — 核心业务模块（partner 客商管理、product 产品中心等）
- `suxin-framework/suxin-module-infra` — 基础设施模块（仍使用 `suxin-module-infra` 坐标，由 `suxin-framework` 聚合）
- `suxin-server` — 主启动模块
- `suxin-framework` — 框架层
- `UI/vue3-admin` — 前端 Vue 3 项目

# 工作规范

## 模块归属
后续所有改动迁移模块把业务模块都集中到 system 模块中，不新建独立 Maven 模块。

## 编译验证
默认不主动运行 Maven 编译；仅在用户明确要求时再执行：
```bash
mvn compile -pl suxin-module -am -DskipTests
```


## 重构检查清单
进行大规模重命名时，按以下顺序执行：
1. 先创建新文件（不要直接改旧文件）
2. 更新所有 import 引用
3. 更新所有方法调用
4. 删除旧文件
5. 如用户明确要求，再执行编译验证
6. 检查是否有遗漏的引用（grep 搜索旧名称）

## 数据库访问规范
**使用 MCP 连接 MySQL 数据库进行查询，但不能执行增删改操作（SELECT only）。DDL/DML 操作需要输出 SQL 脚本由用户手动执行。**

## 常见问题
1. **文件名与类名不一致**：Java 要求公共类名必须与文件名相同
2. **重复 import**：批量替换时可能产生重复 import，需要清理
3. **全限定名引用**：有些地方使用了全限定名（如 `vip.appap.suxin.module.partner.xxx`），需要特别注意
4. **前端 API 路径**：后端 Controller 路径修改后，前端 API 文件也需要同步更新


## 技术栈

- Java 17, Spring Boot 3.5, MyBatis-Plus
- 包名前缀: `vip.appap.suxin.module.*`
- DO 实体在 `dal/dataobject/`, Mapper 在 `dal/mysql/`
- MapStruct 用于 DO ↔ VO/DTO 转换
- 前端: Vue 3 + Element Plus + TypeScript

## 编码规范

- 新增接口使用 `jakarta.validation.Valid`（非 `javax.validation`）
- Controller 使用 `@PermitAll` 或权限注解控制访问
- 错误码统一定义在 `enums/ErrorCodeConstants.java`
- 所有业务代码放在 `suxin-module` 下，不新建独立 Maven 模块

## 前端路由
前端使用动态路由（从数据库 `system_menu` 表加载），静态路由在 `router/modules/remaining.ts` 中作为补充。

## 文档
项目文档统一放在 `D:\笔记仓库\工作` 目录下。

