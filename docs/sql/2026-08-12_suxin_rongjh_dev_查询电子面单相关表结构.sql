-- 根据项目根目录 .mcp.json 核验 add-tenant-electronic-waybill 目标数据库。
-- 仅允许通过 MySQL MCP 执行以下 SELECT / SHOW，不包含任何 DDL/DML。

SELECT DATABASE() AS `current_database`;

SELECT `TABLE_SCHEMA`, `TABLE_NAME`, `COLUMN_NAME`, `COLUMN_TYPE`,
       `IS_NULLABLE`, `COLUMN_DEFAULT`, `COLUMN_KEY`, `EXTRA`
FROM `information_schema`.`COLUMNS`
WHERE `TABLE_SCHEMA` = 'suxin_rongjh_dev'
  AND `TABLE_NAME` IN ('partner_address', 'sales_electronic_waybill_account',
                       'sales_electronic_waybill', 'sales_order')
ORDER BY `TABLE_NAME`, `ORDINAL_POSITION`;

SELECT `TABLE_SCHEMA`, `TABLE_NAME`
FROM `information_schema`.`TABLES`
WHERE `TABLE_SCHEMA` = 'suxin_rongjh_dev'
  AND `TABLE_NAME` IN ('partner_address', 'sales_electronic_waybill_account',
                       'sales_electronic_waybill', 'sales_order')
ORDER BY `TABLE_NAME`;

SELECT `TABLE_SCHEMA`, `TABLE_NAME`, `INDEX_NAME`, `NON_UNIQUE`,
       `SEQ_IN_INDEX`, `COLUMN_NAME`
FROM `information_schema`.`STATISTICS`
WHERE `TABLE_SCHEMA` = 'suxin_rongjh_dev'
  AND `TABLE_NAME` IN ('partner_address', 'sales_electronic_waybill_account',
                       'sales_electronic_waybill')
ORDER BY `TABLE_NAME`, `INDEX_NAME`, `SEQ_IN_INDEX`;

SELECT `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`,
       `component`, `component_name`, `status`, `visible`, `keep_alive`,
       `always_show`, `deleted`
FROM `suxin_rongjh_dev`.`system_menu`
WHERE `deleted` = 0
  AND (`permission` LIKE 'sales:sales_electronic_waybill%'
       OR `component` LIKE '%waybill%'
       OR `name` LIKE '%电子面单%')
ORDER BY `parent_id`, `sort`, `id`;

SHOW COLUMNS FROM `suxin_rongjh_dev`.`partner_address`;
SHOW INDEX FROM `suxin_rongjh_dev`.`partner_address`;

SHOW TABLES FROM `suxin_rongjh_dev` LIKE 'sales_electronic_waybill_account';
SHOW TABLES FROM `suxin_rongjh_dev` LIKE 'sales_electronic_waybill';

SHOW COLUMNS FROM `suxin_rongjh_dev`.`sales_order`;

SELECT `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`,
       `component`, `component_name`, `status`, `visible`, `keep_alive`,
       `always_show`, `deleted`
FROM `suxin_rongjh_dev`.`system_menu`
WHERE `deleted` = 0
  AND (`name` IN ('销售中心', '订单中心', '订单列表')
       OR `path` IN ('sales', 'trade', 'order')
       OR `component` LIKE 'sales/order%')
ORDER BY `parent_id`, `sort`, `id`;
