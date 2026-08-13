-- 2026-08-07 suxin_rongjh_dev 分销链路数据库基线核对（只读）
-- 用途：restore-original-sales-brokerage-flow 实施前，核对 Sales 分销表与配置现状
-- 执行人：opencode（通过 MySQL MCP 执行只读查询）

-- 1. 确认 Sales 分销相关表存在
SELECT TABLE_NAME, TABLE_COMMENT
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'suxin_rongjh_dev'
  AND (TABLE_NAME LIKE 'sales_%' OR TABLE_NAME LIKE 'partner%' OR TABLE_NAME LIKE 'rongjh%')
ORDER BY TABLE_NAME;

-- 2. 确认 sales_brokerage_user 表结构（推广关系）
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'suxin_rongjh_dev' AND TABLE_NAME = 'sales_brokerage_user'
ORDER BY ORDINAL_POSITION;

-- 3. 确认 sales_brokerage_record 表结构（佣金记录）
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'suxin_rongjh_dev' AND TABLE_NAME = 'sales_brokerage_record'
ORDER BY ORDINAL_POSITION;

-- 4. 确认 sales_brokerage_withdraw 表结构（提现）
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'suxin_rongjh_dev' AND TABLE_NAME = 'sales_brokerage_withdraw'
ORDER BY ORDINAL_POSITION;

-- 5. 确认 sales_order 是否含 brokerage_user_id
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'suxin_rongjh_dev' AND TABLE_NAME = 'sales_order'
  AND (COLUMN_NAME LIKE '%brokerage%' OR COLUMN_NAME LIKE '%promotion%' OR COLUMN_NAME LIKE '%referrer%')
ORDER BY ORDINAL_POSITION;

-- 6. 各租户 sales_config 分销配置概览
SELECT tenant_id, enabled, bind_mode, first_level_rate, second_level_rate, frozen_days
FROM suxin_rongjh_dev.sales_config
ORDER BY tenant_id;

-- 7. 推广关系与佣金/提现数据量
SELECT COUNT(*) AS brokerage_user_cnt FROM suxin_rongjh_dev.sales_brokerage_user;
SELECT COUNT(*) AS brokerage_record_cnt FROM suxin_rongjh_dev.sales_brokerage_record;
SELECT COUNT(*) AS withdraw_cnt FROM suxin_rongjh_dev.sales_brokerage_withdraw;
SELECT COUNT(*) AS sales_order_cnt FROM suxin_rongjh_dev.sales_order;
