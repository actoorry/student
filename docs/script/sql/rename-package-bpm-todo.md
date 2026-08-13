# 改包后启用 BPM 模块 — `rename-package.sql` 待办

> 适用场景：项目已从 `cn.iocoder.yudao` 改包为 `vip.appap.suxin`，当前 **BPM Maven 模块未启用**，未来解开 `suxin-module-bpm` 时使用。  
> 关联脚本：[`rename-package.sql`](rename-package.sql)

---

## 背景

改包后，Java 类路径变为 `vip.appap.suxin.module.bpm...`，但数据库里可能仍保存 **旧全限定类名**。  
[`rename-package.sql`](rename-package.sql) 已包含 `bpm_process_expression` 的 UPDATE；**尚未覆盖**监听器表、Flowable 已部署流程等。

若改包当天已完整执行过脚本，启用 BPM 前以 **验证 + 补漏** 为主，不必重复全表 UPDATE。

---

## TODO 清单

### 1. 改包 SQL（必做 / 验证）

- [ ] **确认是否已执行** [`rename-package.sql`](rename-package.sql) 中的 BPM 段（`bpm_process_expression`）
- [ ] 若 **从未执行** 或 **不确定**：在目标库执行整份脚本（或至少 BPM 段 + `infra_file_config` 等）
- [ ] 若执行报 **1267 排序规则** 错误：在脚本开头临时加一行后再跑  
  `SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;`
- [ ] **验证**（结果应为 `0`）：

```sql
SELECT COUNT(*) AS remain_old
FROM bpm_process_expression
WHERE expression LIKE '%cn.iocoder.yudao%';
```

- [ ] **验证新包名已写入**（有数据时 `> 0`）：

```sql
SELECT id, expression
FROM bpm_process_expression
WHERE expression LIKE '%vip.appap.suxin%';
```

---

### 2. 需在 `rename-package.sql` 中补上的 SQL（启用 BPM 后按需追加）

当前脚本 **没有** 下面这段。若库中存在 Java 类类型的流程监听器，解开 BPM 后应 **追加到脚本并执行**：

```sql
-- BPM 流程监听器（value_type = class 时，value 存全限定类名）
UPDATE bpm_process_listener
SET value = REPLACE(value, @old_package, @new_package)
WHERE value_type = 'class'
  AND value LIKE CONCAT('%', @old_package, '%');

-- 验证
-- SELECT id, name, value FROM bpm_process_listener
-- WHERE value LIKE '%cn.iocoder.yudao%';
```

**TODO（维护脚本）**：

- [ ] 启用 BPM 并确认 `bpm_process_listener` 有 `class` 类型数据后，将上述 UPDATE **合并进** [`rename-package.sql`](rename-package.sql)
- [ ] 同步更新 [`docs/改包检查清单.md`](../../改包检查清单.md) 中的 BPM 说明（可选）

---

### 3. 重新导入 BPM 相关 SQL 时（必做）

若解开模块时又执行了 **旧版 SQL dump**（INSERT 里仍是 `cn.iocoder.yudao`）：

- [ ] 导入完成后 **再跑一遍** [`rename-package.sql`](rename-package.sql)（至少 BPM 段 + 上节 `bpm_process_listener` 补段）
- [ ] 不要全量重导业务库覆盖已有数据；仅执行 **增量菜单 / BPM 初始化 SQL** 时尤其注意

---

### 4. 已部署流程 / Flowable（按需）

- [ ] 若已有 **自定义流程** 且 BPMN 里配置了 Java 类监听器，在管理端 **重新发布** 流程，或检查是否仍含 `cn.iocoder.yudao`
- [ ] 流程报 `ClassNotFoundException`：先查表达式/监听器表，再查流程定义是否需重新部署

---

### 5. 启用模块与运行（非 SQL，配套）

- [ ] 解开 `suxin-module-bpm` Maven 依赖
- [ ] 执行 BPM 菜单 SQL（若有）
- [ ] `mvn compile` → **清空 Redis** → 重启 → 前端重新登录

---

## 推荐执行顺序

```text
1. 解开 BPM 模块 → compile
2. 执行/补跑 rename-package.sql（bpm_process_expression；按需 bpm_process_listener）
3. 若有新导入旧 SQL → 再跑 BPM 相关 UPDATE
4. Flush Redis → 重启
5. 验证 SQL → 抽测流程
6. 将 bpm_process_listener 段合并进 rename-package.sql
```

---

## 快速自检

```sql
SELECT 'bpm_process_expression' AS tbl, COUNT(*) AS old_cnt
FROM bpm_process_expression WHERE expression LIKE '%cn.iocoder.yudao%'
UNION ALL
SELECT 'bpm_process_listener', COUNT(*)
FROM bpm_process_listener
WHERE value_type = 'class' AND value LIKE '%cn.iocoder.yudao%';
```

---

## 备注

- 表名仍为 `bpm_*`，rename 脚本 **不改表名**。
- 配置键仍为 `yudao.info.*`，仅包名 **值** 变为 `vip.appap.suxin`。
