# Offline Activity Config SQL

Manual execution only. Do not execute DDL or DML through MySQL MCP.

## 1. Add or update the display scene

```sql
SET @tenant_id = 303;
SET @scene_code = 'OFFLINE_ACTIVITY_PAGE';
SET @scene_name = 'Offline Activity Page';
SET @category_ids = '95';

INSERT INTO `product_display_config` (
    `scene_code`, `scene_name`, `category_ids`, `status`, `sort`, `remark`,
    `creator`, `updater`, `tenant_id`
) VALUES (
    @scene_code, @scene_name, @category_ids, 0, 20,
    'Offline activity display scene',
    '', '', @tenant_id
)
ON DUPLICATE KEY UPDATE
    `scene_name` = VALUES(`scene_name`),
    `category_ids` = VALUES(`category_ids`),
    `status` = VALUES(`status`),
    `sort` = VALUES(`sort`),
    `remark` = VALUES(`remark`),
    `updater` = VALUES(`updater`);
```

## 2. Optional: align activity SPU metadata to service-product semantics

Run this only for offline activity SPUs that were created with incorrect defaults.

Notes:
- `type = 2` aligns to service product semantics.
- `delivery_types = '0'` keeps the first phase on the existing online delivery path.
- `category_sales = 95` places the SPU under the configured activity sales category.
- `stock` continues to represent remaining seats and should be maintained per SKU.

```sql
SET @tenant_id = 303;
SET @activity_spu_id = 0; -- replace with the real activity SPU id

UPDATE `product_spu`
SET `type` = 2,
    `delivery_types` = '0',
    `category_sales` = 95,
    `updater` = 'manual-sql'
WHERE `id` = @activity_spu_id
  AND `tenant_id` = @tenant_id
  AND `deleted` = b'0';
```

## 3. Optional: verify before and after execution

```sql
SELECT `id`, `scene_code`, `scene_name`, `category_ids`, `status`, `sort`, `tenant_id`
FROM `product_display_config`
WHERE `tenant_id` = 303
  AND `scene_code` IN ('EMOTION_COURSE_PAGE', 'OFFLINE_ACTIVITY_PAGE')
  AND `deleted` = b'0'
ORDER BY `scene_code`, `id`;

SELECT `id`, `name`, `category_sales`, `type`, `delivery_types`, `status`, `tenant_id`
FROM `product_spu`
WHERE `tenant_id` = 303
  AND `category_sales` = 95
  AND `deleted` = b'0'
ORDER BY `id`;

SELECT `id`, `spu_id`, `price`, `stock`
FROM `product_sku`
WHERE `spu_id` IN (
    SELECT `id`
    FROM `product_spu`
    WHERE `tenant_id` = 303
      AND `category_sales` = 95
      AND `deleted` = b'0'
)
ORDER BY `spu_id`, `id`;
```
