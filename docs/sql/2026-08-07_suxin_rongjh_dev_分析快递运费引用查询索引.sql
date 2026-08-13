SHOW INDEX FROM product_spu;
SHOW INDEX FROM sales_order;
SHOW INDEX FROM sales_after_sale;

EXPLAIN SELECT id
FROM product_spu
WHERE tenant_id = 1 AND deleted = b'0' AND delivery_template_id = 1
LIMIT 1;

EXPLAIN SELECT id
FROM sales_order
WHERE tenant_id = 1 AND deleted = b'0' AND logistics_id = 1
LIMIT 1;

EXPLAIN SELECT id
FROM sales_after_sale
WHERE tenant_id = 1 AND deleted = b'0' AND logistics_id = 1
LIMIT 1;

EXPLAIN SELECT id
FROM sales_delivery_express
WHERE tenant_id = 1 AND deleted = b'0' AND code = 'shentong'
LIMIT 1;

EXPLAIN SELECT id
FROM sales_delivery_express_template
WHERE tenant_id = 1 AND deleted = b'0' AND name = '按照体积配送'
LIMIT 1;

EXPLAIN SELECT id
FROM sales_delivery_express_template_charge
WHERE tenant_id = 1 AND deleted = b'0' AND template_id = 1;

EXPLAIN SELECT id
FROM sales_delivery_express_template_free
WHERE tenant_id = 1 AND deleted = b'0' AND template_id = 1;
