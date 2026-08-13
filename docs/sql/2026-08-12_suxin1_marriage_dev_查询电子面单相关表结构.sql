-- add-tenant-electronic-waybill 实施前只读核验 SQL
-- 仅允许通过 MySQL MCP 执行以下 SHOW / SELECT，不包含任何 DDL/DML。

SHOW COLUMNS FROM `partner_address`;
SHOW INDEX FROM `partner_address`;

SHOW TABLES LIKE 'sales_electronic_waybill_account';
SHOW TABLES LIKE 'sales_electronic_waybill';

SHOW COLUMNS FROM `sales_order`;
SHOW INDEX FROM `sales_order`;

SHOW COLUMNS FROM `system_menu`;

SELECT `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`,
       `component`, `component_name`, `status`, `visible`, `keep_alive`,
       `always_show`, `deleted`
FROM `system_menu`
WHERE `deleted` = 0
  AND (`name` IN ('销售中心', '订单中心', '交易订单')
       OR `path` IN ('sales', 'trade', 'order')
       OR `component` LIKE 'sales/order%')
ORDER BY `parent_id`, `sort`, `id`;

SELECT `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`,
       `component`, `component_name`, `status`, `visible`, `keep_alive`,
       `always_show`, `deleted`
FROM `system_menu`
WHERE `deleted` = 0
  AND `permission` LIKE 'sales:sales_order:%'
ORDER BY `parent_id`, `sort`, `id`;
