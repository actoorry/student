-- 注册「清理零库存记录」定时任务
-- ============================================================
-- 重要：yudao 定时任务由两张表协作
--   1) infra_job                      —— 任务配置表（本脚本写入）
--   2) qrtz_job_details / qrtz_triggers —— Quartz 运行表（真正驱动调度）
-- 仅插入 infra_job 不会自动同步到 Quartz，任务不会执行！
-- 必须再把本任务同步到 Quartz（见文末「同步步骤」）。
-- ============================================================

-- 1) 写入任务配置（monitor_timeout=0 表示不监控超时，该列为 NOT NULL）
INSERT INTO infra_job
    (name, status, handler_name, handler_param, cron_expression,
     retry_count, retry_interval, monitor_timeout,
     create_time, update_time, creator, updater, deleted)
VALUES
    ('清理零库存记录', 1, 'wmsStockCleanupJob', '',
     '0 0 3 * * ?',
     0, 0, 0,
     NOW(), NOW(), '1', '1', 0);

-- ============================================================
-- 同步步骤（二选一，否则任务不会运行）
-- ============================================================
-- 方式 A（推荐，最稳妥）：不依赖本 SQL，直接在后台创建
--   路径：基础设施 → 定时任务 → 创建
--     任务名称    ：清理零库存记录
--     处理器的名字：wmsStockCleanupJob   （即 WmsStockCleanupJob 的 Spring Bean 名）
--     处理器的参数：（空）
--     CRON 表达式 ：0 0 3 * * ?          （每天凌晨 3 点，可按需调整）
--   创建后框架自动写入 qrtz_ 表，无需手动执行本 INSERT。
--   若已执行上方 INSERT，请先删除重复记录再后台创建（避免 handler_name 唯一约束冲突）：
--     DELETE FROM infra_job WHERE handler_name = 'wmsStockCleanupJob';

-- 方式 B（已执行上方 INSERT 后）：把 infra_job 同步到 Quartz
--   路径：基础设施 → 定时任务 → 点「同步」按钮
--   或调用接口：POST /admin-api/infra/job/sync
--   说明：syncJob 会遍历全部 infra_job 并重新注册到 qrtz_ 表（status=1 正常调度，2 暂停）。
-- ============================================================
