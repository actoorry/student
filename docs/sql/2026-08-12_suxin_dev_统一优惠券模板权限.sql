-- 优惠券模板权限与后端 SalesCouponTemplateController 对齐
-- 数据库：suxin_dev
-- 仅供人工执行；执行前请确认当前数据库为 suxin_dev

START TRANSACTION;

UPDATE system_menu
SET permission = CASE id
    WHEN 2033 THEN 'sales:sales_coupon_template:query'
    WHEN 2034 THEN 'sales:sales_coupon_template:create'
    WHEN 2035 THEN 'sales:sales_coupon_template:update'
    WHEN 2036 THEN 'sales:sales_coupon_template:delete'
END
WHERE id IN (2033, 2034, 2035, 2036)
  AND deleted = 0;

COMMIT;

-- 验证
SELECT id, name, permission
FROM system_menu
WHERE id IN (2033, 2034, 2035, 2036)
  AND deleted = 0
ORDER BY id;
