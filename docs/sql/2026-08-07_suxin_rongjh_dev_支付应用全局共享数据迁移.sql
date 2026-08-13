-- 已废弃：该脚本会把 mall 渠道改绑到 wx_lite_marriage，与当前统一使用 mall 的目标相反，禁止执行。
-- 请改为人工执行：
-- docs/sql/2026-08-07_suxin_rongjh_dev_合并wx_lite_marriage到mall并删除旧应用.sql
--
-- 历史背景：PayAppDO 已添加 @TenantIgnore，pay_app 恢复为全局共享（不按租户过滤）。
-- 需要合并存量数据：
--   1) 保留的全局应用 tenant_id 归 0（mall/demo/wallet/wx_lite_marriage）
--   2) 逻辑删除租户 393 的 mall（id=11），避免 app_key='mall' 全局重复
--   3) 渠道改绑：租户 393 的 wx_lite 渠道（app_id=11，将删除）→ app_id=9（wx_lite_marriage 全局应用）
--   4) 逻辑删除指向已删除应用（app_id=10）的残留渠道 id=43：
--      注意：不能把它改绑到 app_id=9，否则租户 303 下 (app_id=9, code=wx_lite) 会有两条记录，
--      导致 selectByAppIdAndCode 的 selectOne 抛 TooManyResultsException
-- 执行前请先备份 pay_app、pay_channel 表。

-- 1. 全局应用 tenant_id 归 0
-- UPDATE pay_app SET tenant_id = 0 WHERE id IN (1, 7, 8, 9) AND deleted = 0;

-- 2. 渠道改绑：租户 393 的 wx_lite 渠道（app_id=11）绑定到全局 wx_lite_marriage 应用（id=9）
-- UPDATE pay_channel SET app_id = 9 WHERE app_id = 11 AND deleted = 0;

-- 3. 逻辑删除租户 303 指向已删除应用（app_id=10）的残留渠道（id=43），避免与 id=42 重复
-- UPDATE pay_channel SET deleted = 1 WHERE app_id = 10 AND deleted = 0;

-- 4. 逻辑删除租户 393 的 mall 应用（app_key 与全局 mall 冲突）
-- UPDATE pay_app SET deleted = 1 WHERE id = 11 AND deleted = 0;

-- 验证（应全部通过）：
-- 1) pay_app 中 app_key 无重复：
--    SELECT app_key, COUNT(*) FROM pay_app WHERE deleted = 0 GROUP BY app_key HAVING COUNT(*) > 1;  -- 应为空
-- 2) pay_channel 的 app_id 全部指向存在且未删除的应用：
--    SELECT id, app_id, tenant_id FROM pay_channel WHERE deleted = 0 AND app_id NOT IN (SELECT id FROM pay_app WHERE deleted = 0);  -- 应为空
-- 3) 同一租户下 (app_id, code) 无重复：
--    SELECT tenant_id, app_id, code, COUNT(*) FROM pay_channel WHERE deleted = 0 GROUP BY tenant_id, app_id, code HAVING COUNT(*) > 1;  -- 应为空
-- 4) 全局应用为：mall(1)、demo(7)、wallet(8)、wx_lite_marriage(9)，tenant_id 均为 0
