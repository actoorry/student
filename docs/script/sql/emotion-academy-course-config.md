# Emotion Academy Course Config SQL

Manual execution only. Do not execute DDL or DML through MySQL MCP.

## 1. Add or update the display scene

```sql
SET @tenant_id = 303;
SET @scene_code = 'EMOTION_COURSE_PAGE';
SET @scene_name = 'Emotion Academy Page';
SET @category_ids = '94';

INSERT INTO `product_display_config` (
    `scene_code`, `scene_name`, `category_ids`, `status`, `sort`, `remark`,
    `creator`, `updater`, `tenant_id`
) VALUES (
    @scene_code, @scene_name, @category_ids, 0, 10,
    'Emotion academy course display scene',
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

## 2. Optional: correct existing course SPU to service product semantics

Only run this when the course product was created as an entity product by mistake and you want it aligned with `ProductTypeEnum.SERVICE = 2`.

```sql
SET @tenant_id = 303;
SET @course_spu_id = 654;

UPDATE `product_spu`
SET `type` = 2,
    `updater` = 'manual-sql'
WHERE `id` = @course_spu_id
  AND `tenant_id` = @tenant_id
  AND `deleted` = b'0'
  AND `type` = 1;
```

## 3. Optional: verify before and after execution

```sql
SELECT `id`, `scene_code`, `scene_name`, `category_ids`, `status`, `sort`, `tenant_id`
FROM `product_display_config`
WHERE `tenant_id` = 303
  AND `scene_code` IN ('MEMBER_PAGE', 'EMOTION_COURSE_PAGE')
  AND `deleted` = b'0'
ORDER BY `scene_code`, `id`;

SELECT `id`, `name`, `category_sales`, `type`, `delivery_types`, `tenant_id`
FROM `product_spu`
WHERE `tenant_id` = 303
  AND `id` = 654
  AND `deleted` = b'0';
```
