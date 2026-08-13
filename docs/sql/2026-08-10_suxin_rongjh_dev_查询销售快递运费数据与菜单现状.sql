DESCRIBE system_menu;
DESCRIBE system_role_menu;
SHOW TABLES LIKE 'sales_config';

SELECT tenant_id, COUNT(*) AS express_count
FROM sales_delivery_express
WHERE deleted = b'0'
GROUP BY tenant_id
ORDER BY tenant_id;

SELECT tenant_id, COUNT(*) AS template_count
FROM sales_delivery_express_template
WHERE deleted = b'0'
GROUP BY tenant_id
ORDER BY tenant_id;

SELECT t.tenant_id,
       t.id AS template_id,
       t.name,
       t.charge_mode,
       (SELECT COUNT(*)
          FROM sales_delivery_express_template_charge c
         WHERE c.tenant_id = t.tenant_id
           AND c.template_id = t.id
           AND c.deleted = b'0') AS charge_count,
       (SELECT COUNT(*)
          FROM sales_delivery_express_template_free f
         WHERE f.tenant_id = t.tenant_id
           AND f.template_id = t.id
           AND f.deleted = b'0') AS free_count,
       (SELECT COUNT(*)
          FROM product_spu p
         WHERE p.tenant_id = t.tenant_id
           AND p.delivery_template_id = t.id
           AND p.deleted = b'0') AS product_reference_count
FROM sales_delivery_express_template t
WHERE t.deleted = b'0'
ORDER BY t.tenant_id, t.id;

SELECT p.tenant_id,
       p.delivery_template_id,
       COUNT(*) AS product_count
FROM product_spu p
LEFT JOIN sales_delivery_express_template t
       ON t.tenant_id = p.tenant_id
      AND t.id = p.delivery_template_id
      AND t.deleted = b'0'
WHERE p.deleted = b'0'
  AND p.delivery_template_id IS NOT NULL
  AND t.id IS NULL
GROUP BY p.tenant_id, p.delivery_template_id
ORDER BY p.tenant_id, p.delivery_template_id;

SELECT e.tenant_id,
       e.id AS express_id,
       e.code,
       e.name,
       e.status,
       (SELECT COUNT(*)
          FROM sales_order o
         WHERE o.tenant_id = e.tenant_id
           AND o.logistics_id = e.id
           AND o.deleted = b'0') AS order_reference_count,
       (SELECT COUNT(*)
          FROM sales_after_sale a
         WHERE a.tenant_id = e.tenant_id
           AND a.logistics_id = e.id
           AND a.deleted = b'0') AS after_sale_reference_count
FROM sales_delivery_express e
WHERE e.deleted = b'0'
ORDER BY e.tenant_id, e.id;

SELECT tenant_id, COUNT(*) AS no_delivery_sentinel_count
FROM sales_order
WHERE deleted = b'0'
  AND logistics_id = 0
GROUP BY tenant_id
ORDER BY tenant_id;

SELECT tenant_id,
       delivery_express_free_enabled,
       delivery_express_free_price
FROM sales_config
WHERE deleted = b'0'
ORDER BY tenant_id;

SELECT id,
       parent_id,
       name,
       path,
       component,
       component_name,
       permission,
       status,
       deleted
FROM system_menu
WHERE id BETWEEN 2164 AND 2178
   OR permission LIKE 'trade:delivery:express%'
   OR permission LIKE 'sales:sales_delivery_express%'
ORDER BY id;

SELECT menu_id, COUNT(*) AS role_binding_count
FROM system_role_menu
WHERE menu_id BETWEEN 2164 AND 2178
  AND deleted = b'0'
GROUP BY menu_id
ORDER BY menu_id;
