-- add-tenant-electronic-waybill：补齐商户寄件地址类型字段
-- 目标数据库：suxin_rongjh_dev
-- 执行方式：仅供 DBA / 用户人工执行；脚本可重复执行，已存在的字段或索引会自动跳过。
-- 背景：应用查询 partner_address.type = 1，但目标库缺少 type 字段，导致 Unknown column 'type'。
--
-- 地址类型：
--   0 = 会员收件地址（所有存量地址默认保持为此类型）
--   1 = 商户寄件地址（租户内共享，供电子面单选择）
--
-- 注意：本脚本不会把任何存量会员地址自动改成商户寄件地址，避免错误共享个人地址。

USE `suxin_rongjh_dev`;

-- 一、前置核验
SELECT COUNT(*) AS `type_column_count`
FROM `information_schema`.`COLUMNS`
WHERE `TABLE_SCHEMA` = 'suxin_rongjh_dev'
  AND `TABLE_NAME` = 'partner_address'
  AND `COLUMN_NAME` = 'type';

SELECT COUNT(*) AS `type_index_count`
FROM `information_schema`.`STATISTICS`
WHERE `TABLE_SCHEMA` = 'suxin_rongjh_dev'
  AND `TABLE_NAME` = 'partner_address'
  AND `INDEX_NAME` = 'idx_partner_address_tenant_type';

-- 二、结构改造：字段和索引分别判断，兼容字段已存在但索引缺失的中间状态
SET @type_column_exists := (
    SELECT COUNT(*)
    FROM `information_schema`.`COLUMNS`
    WHERE `TABLE_SCHEMA` = 'suxin_rongjh_dev'
      AND `TABLE_NAME` = 'partner_address'
      AND `COLUMN_NAME` = 'type'
);
SET @type_column_ddl := IF(
    @type_column_exists = 0,
    'ALTER TABLE `suxin_rongjh_dev`.`partner_address` ADD COLUMN `type` TINYINT NOT NULL DEFAULT 0 COMMENT ''地址类型：0-会员收件地址，1-商户寄件地址'' AFTER `default_status`',
    'SELECT ''partner_address.type 已存在，跳过新增字段'' AS message'
);
PREPARE type_column_stmt FROM @type_column_ddl;
EXECUTE type_column_stmt;
DEALLOCATE PREPARE type_column_stmt;

SET @type_index_exists := (
    SELECT COUNT(*)
    FROM `information_schema`.`STATISTICS`
    WHERE `TABLE_SCHEMA` = 'suxin_rongjh_dev'
      AND `TABLE_NAME` = 'partner_address'
      AND `INDEX_NAME` = 'idx_partner_address_tenant_type'
);
SET @type_index_ddl := IF(
    @type_index_exists = 0,
    'ALTER TABLE `suxin_rongjh_dev`.`partner_address` ADD INDEX `idx_partner_address_tenant_type` (`tenant_id`, `type`, `deleted`, `default_status`, `id`)',
    'SELECT ''idx_partner_address_tenant_type 已存在，跳过新增索引'' AS message'
);
PREPARE type_index_stmt FROM @type_index_ddl;
EXECUTE type_index_stmt;
DEALLOCATE PREPARE type_index_stmt;

-- 三、执行后验证（预期：type 字段存在；所有存量记录 type = 0）
SHOW COLUMNS FROM `suxin_rongjh_dev`.`partner_address`;
SHOW INDEX FROM `suxin_rongjh_dev`.`partner_address`;

SELECT `type`, COUNT(*) AS `address_count`
FROM `suxin_rongjh_dev`.`partner_address`
GROUP BY `type`
ORDER BY `type`;

-- 四、业务数据说明
-- 新增或编辑商户寄件地址时，请通过管理后台把 type 设置为 1。
-- 如需人工指定已有地址，必须先核对 tenant_id、地址所有权及是否允许租户内共享，
-- 再另行执行经审核的 UPDATE；本脚本不自动执行该数据迁移。

-- 五、回滚（仅在确认没有电子面单账户引用商户寄件地址，且应用已回退后人工执行）
-- ALTER TABLE `suxin_rongjh_dev`.`partner_address`
--     DROP INDEX `idx_partner_address_tenant_type`,
--     DROP COLUMN `type`;
