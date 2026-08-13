-- 作用：
--   1. 将 wx_lite_marriage 支付应用的全部关联数据迁移到 mall；
--   2. 迁移完成后逻辑删除 wx_lite_marriage 支付应用。
-- 说明：
--   - 本脚本仅供人工执行，执行前请备份相关表。
--   - 已通过只读查询确认 app_key='mall' 与 app_key='wx_lite_marriage' 各只有一条有效记录。
--   - 已确认合并后不会产生同租户渠道、商户订单号或商户转账号冲突。

START TRANSACTION;

SET @mall_app_id = (
    SELECT id
    FROM pay_app
    WHERE app_key = 'mall' AND deleted = 0
    LIMIT 1
);
SET @wx_lite_marriage_app_id = (
    SELECT id
    FROM pay_app
    WHERE app_key = 'wx_lite_marriage' AND deleted = 0
    LIMIT 1
);

-- 执行前确认：预期 mall_app_id=1、wx_lite_marriage_app_id=9，任一值为 NULL 时不要继续。
SELECT @mall_app_id AS mall_app_id,
       @wx_lite_marriage_app_id AS wx_lite_marriage_app_id;

-- 迁移支付渠道及全部带 app_id 的支付业务记录，包含历史、逻辑删除和通知记录。
UPDATE pay_channel
SET app_id = @mall_app_id
WHERE app_id = @wx_lite_marriage_app_id
  AND @mall_app_id IS NOT NULL
  AND @wx_lite_marriage_app_id IS NOT NULL
  AND @mall_app_id <> @wx_lite_marriage_app_id;

UPDATE pay_order
SET app_id = @mall_app_id
WHERE app_id = @wx_lite_marriage_app_id
  AND @mall_app_id IS NOT NULL
  AND @wx_lite_marriage_app_id IS NOT NULL
  AND @mall_app_id <> @wx_lite_marriage_app_id;

UPDATE account_refund
SET app_id = @mall_app_id
WHERE app_id = @wx_lite_marriage_app_id
  AND @mall_app_id IS NOT NULL
  AND @wx_lite_marriage_app_id IS NOT NULL
  AND @mall_app_id <> @wx_lite_marriage_app_id;

UPDATE pay_notify_task
SET app_id = @mall_app_id
WHERE app_id = @wx_lite_marriage_app_id
  AND @mall_app_id IS NOT NULL
  AND @wx_lite_marriage_app_id IS NOT NULL
  AND @mall_app_id <> @wx_lite_marriage_app_id;

UPDATE pay_transfer
SET app_id = @mall_app_id
WHERE app_id = @wx_lite_marriage_app_id
  AND @mall_app_id IS NOT NULL
  AND @wx_lite_marriage_app_id IS NOT NULL
  AND @mall_app_id <> @wx_lite_marriage_app_id;

UPDATE pay_demo_transfer
SET app_id = @mall_app_id
WHERE app_id = @wx_lite_marriage_app_id
  AND @mall_app_id IS NOT NULL
  AND @wx_lite_marriage_app_id IS NOT NULL
  AND @mall_app_id <> @wx_lite_marriage_app_id;

-- 关联清零后逻辑删除旧支付应用。
UPDATE pay_app
SET deleted = 1,
    updater = '1',
    update_time = NOW()
WHERE id = @wx_lite_marriage_app_id
  AND app_key = 'wx_lite_marriage'
  AND deleted = 0
  AND @mall_app_id IS NOT NULL
  AND @wx_lite_marriage_app_id IS NOT NULL
  AND @mall_app_id <> @wx_lite_marriage_app_id;

COMMIT;

-- 执行后验证：以下残留引用查询均应返回 0，旧应用查询应返回 0 行。
SELECT 'pay_channel' AS table_name, COUNT(*) AS remaining_count
FROM pay_channel WHERE app_id = @wx_lite_marriage_app_id
UNION ALL
SELECT 'pay_order', COUNT(*) FROM pay_order WHERE app_id = @wx_lite_marriage_app_id
UNION ALL
SELECT 'account_refund', COUNT(*) FROM account_refund WHERE app_id = @wx_lite_marriage_app_id
UNION ALL
SELECT 'pay_notify_task', COUNT(*) FROM pay_notify_task WHERE app_id = @wx_lite_marriage_app_id
UNION ALL
SELECT 'pay_transfer', COUNT(*) FROM pay_transfer WHERE app_id = @wx_lite_marriage_app_id
UNION ALL
SELECT 'pay_demo_transfer', COUNT(*) FROM pay_demo_transfer WHERE app_id = @wx_lite_marriage_app_id;

SELECT id, app_key, name, status, deleted, tenant_id
FROM pay_app
WHERE app_key = 'wx_lite_marriage' AND deleted = 0;

SELECT id, app_key, name, status, deleted, tenant_id
FROM pay_app
WHERE app_key = 'mall' AND deleted = 0;
