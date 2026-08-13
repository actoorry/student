-- ============================================================================
-- 2026-08-10 实施前只读预检 SQL 留痕（repair-sales-express-freight-flow）
-- 数据库：suxin_rongjh_dev（项目 .mcp.json 的 stdio MySQL MCP）
-- 说明：以下全部为 SELECT / SHOW / DESCRIBE / EXPLAIN 只读查询，不包含任何 DDL/DML。
-- 来源：openspec/changes/repair-sales-express-freight-flow/docs/working-state.md
-- ============================================================================

-- 1. 连接与版本
SELECT 1 AS connection_check, DATABASE() AS current_db, VERSION() AS version;
-- => 1, suxin_rongjh_dev, 8.0.34

-- 2. 快递公司（启用列表按 sort 稳定排序；当前全部 status=0）
SELECT id, code, name, status, sort, deleted FROM suxin_rongjh_dev.sales_delivery_express ORDER BY sort, id;
-- => 10 条；1..9 租户 1，10 租户 393

-- 3. 运费模板（模板 1 有 1 条计费规则；模板 4 无计费规则、2 条包邮规则）
SELECT t.id, t.name, t.charge_mode, t.sort, t.deleted,
  (SELECT COUNT(*) FROM suxin_rongjh_dev.sales_delivery_express_template_charge c WHERE c.template_id = t.id AND c.deleted = 0) AS charge_cnt,
  (SELECT COUNT(*) FROM suxin_rongjh_dev.sales_delivery_express_template_free f WHERE f.template_id = t.id AND f.deleted = 0) AS free_cnt
FROM suxin_rongjh_dev.sales_delivery_express_template t WHERE t.deleted = 0 ORDER BY t.id;
-- => 模板 1：charge_cnt=1 free_cnt=0；模板 4：charge_cnt=0 free_cnt=2

-- 4. 菜单 2168–2178 权限现状（仍为旧 trade:delivery:express*）
SELECT id, name, permission, type, parent_id, status, deleted FROM suxin_rongjh_dev.system_menu WHERE id BETWEEN 2168 AND 2178 ORDER BY id;
-- => 2168..2172 快递公司（type=3 按钮）；2173 运费模版目录(type=2)；2174..2178 模板按钮
--    permission 均为 trade:delivery:express* 或 trade:delivery:express-template*

-- 5. 商品引用模板（模板 1：3 个商品；模板 4：7 个商品；47 个为空）
SELECT delivery_template_id, COUNT(*) AS cnt FROM suxin_rongjh_dev.product_spu WHERE deleted = 0 GROUP BY delivery_template_id ORDER BY delivery_template_id;
-- => null:47, 1:3, 4:7

-- 6. 订单引用快递公司（logistics_id 0 为“无需发货”哨兵 9 条；1..5 被引用）
SELECT logistics_id, COUNT(*) AS cnt, SUM(CASE WHEN logistics_id = 0 THEN 1 ELSE 0 END) AS zero_cnt FROM suxin_rongjh_dev.sales_order WHERE deleted = 0 GROUP BY logistics_id ORDER BY logistics_id;
-- => null:234, 0:9(zero), 1:6, 2:1, 3:1, 4:1, 5:2

-- 7. 售后引用快递公司（公司 1 被 2 个售后引用）
SELECT logistics_id, COUNT(*) AS cnt FROM suxin_rongjh_dev.sales_after_sale WHERE deleted = 0 GROUP BY logistics_id ORDER BY logistics_id;
-- => null:18, 1:2

-- 8. 现有索引（引用/唯一性查询均无专用索引）
SELECT table_name, index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS columns
FROM information_schema.statistics
WHERE table_schema = 'suxin_rongjh_dev'
  AND table_name IN ('sales_delivery_express','sales_delivery_express_template',
                     'sales_delivery_express_template_charge','sales_delivery_express_template_free',
                     'product_spu','sales_order','sales_after_sale')
GROUP BY table_name, index_name ORDER BY table_name, index_name;
-- => 仅主键与既有 partner_id / salesperson_id 索引

-- 9. 七类引用/唯一性查询 EXPLAIN（全部 ALL）
EXPLAIN SELECT * FROM suxin_rongjh_dev.sales_delivery_express WHERE tenant_id = 1 AND deleted = 0 AND code = 'shentong';          -- ALL rows=10
EXPLAIN SELECT * FROM suxin_rongjh_dev.sales_delivery_express_template WHERE tenant_id = 1 AND deleted = 0 AND name = '按照体积配送'; -- ALL rows=2
EXPLAIN SELECT * FROM suxin_rongjh_dev.sales_delivery_express_template_charge WHERE tenant_id = 1 AND deleted = 0 AND template_id = 1; -- ALL rows=14
EXPLAIN SELECT * FROM suxin_rongjh_dev.sales_delivery_express_template_free WHERE tenant_id = 1 AND deleted = 0 AND template_id = 1;  -- ALL rows=23
EXPLAIN SELECT id FROM suxin_rongjh_dev.product_spu WHERE tenant_id = 1 AND deleted = 0 AND delivery_template_id = 1;                 -- ALL rows=64
EXPLAIN SELECT id FROM suxin_rongjh_dev.sales_order WHERE tenant_id = 1 AND deleted = 0 AND logistics_id = 1;                       -- ALL rows=267
EXPLAIN SELECT id FROM suxin_rongjh_dev.sales_after_sale WHERE tenant_id = 1 AND deleted = 0 AND logistics_id = 1;                  -- ALL rows=20
