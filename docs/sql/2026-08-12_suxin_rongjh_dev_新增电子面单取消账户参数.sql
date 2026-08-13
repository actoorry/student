-- add-tenant-electronic-waybill：补齐官方取消电子面单账户参数
-- 目标数据库：suxin_rongjh_dev
-- 执行方式：仅供 DBA / 用户人工执行；请先执行前置核验，并仅执行一次 ALTER TABLE。

USE `suxin_rongjh_dev`;

-- 一、前置核验（已存在的字段不要重复新增）
SELECT `COLUMN_NAME`, `COLUMN_TYPE`, `IS_NULLABLE`, `COLUMN_COMMENT`
FROM `information_schema`.`COLUMNS`
WHERE `TABLE_SCHEMA` = 'suxin_rongjh_dev'
  AND `TABLE_NAME` = 'sales_electronic_waybill_account'
  AND `COLUMN_NAME` IN ('check_man', 'exp_type')
ORDER BY `ORDINAL_POSITION`;

-- 二、结构改造（仅当上述两个字段均不存在时执行）
ALTER TABLE `suxin_rongjh_dev`.`sales_electronic_waybill_account`
    ADD COLUMN `check_man` VARCHAR(64) NULL COMMENT '取消电子面单操作人' AFTER `partner_name`,
    ADD COLUMN `exp_type` VARCHAR(64) NULL COMMENT '快递产品类型' AFTER `check_man`;

-- 三、执行后验证（预期返回 check_man、exp_type 两行）
SELECT `COLUMN_NAME`, `COLUMN_TYPE`, `IS_NULLABLE`, `COLUMN_COMMENT`
FROM `information_schema`.`COLUMNS`
WHERE `TABLE_SCHEMA` = 'suxin_rongjh_dev'
  AND `TABLE_NAME` = 'sales_electronic_waybill_account'
  AND `COLUMN_NAME` IN ('check_man', 'exp_type')
ORDER BY `ORDINAL_POSITION`;

-- 四、回滚（应用已回退且确认字段数据无需保留时人工执行）
-- ALTER TABLE `suxin_rongjh_dev`.`sales_electronic_waybill_account`
--     DROP COLUMN `exp_type`,
--     DROP COLUMN `check_man`;
