# Product Display Config Migration SQL

Manual execution only. Do not execute DDL or DML through MySQL MCP.

```sql
CREATE TABLE IF NOT EXISTS `product_display_config` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `scene_code` varchar(64) NOT NULL COMMENT 'Display scene code',
    `scene_name` varchar(64) NOT NULL COMMENT 'Display scene name',
    `category_ids` varchar(500) NOT NULL COMMENT 'Comma-separated sales category IDs',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT 'Status: 0-enabled, 1-disabled',
    `sort` int NOT NULL DEFAULT 0 COMMENT 'Sort order',
    `remark` varchar(500) DEFAULT NULL COMMENT 'Remark',
    `creator` varchar(64) DEFAULT '' COMMENT 'Creator',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `updater` varchar(64) DEFAULT '' COMMENT 'Updater',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Deleted flag',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT 'Tenant ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_scene_code` (`tenant_id`, `scene_code`, `deleted`),
    KEY `idx_tenant_status` (`tenant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Product display config';

-- Set the target tenant before running the migration.
-- The current database has category 89 under tenant 303, but verify in your target environment first.
SET @tenant_id = 303;

INSERT INTO `product_display_config` (
    `scene_code`, `scene_name`, `category_ids`, `status`, `sort`, `remark`,
    `creator`, `updater`, `tenant_id`
)
SELECT
    'MEMBER_PAGE',
    'Member Package Page',
    c.`value`,
    0,
    0,
    'Migrated from infra_config partner.member.marriage.advanced.category-id',
    '',
    '',
    @tenant_id
FROM `infra_config` c
WHERE c.`config_key` = 'partner.member.marriage.advanced.category-id'
  AND c.`deleted` = b'0'
  AND c.`value` IS NOT NULL
  AND c.`value` <> ''
ON DUPLICATE KEY UPDATE
    `scene_name` = VALUES(`scene_name`),
    `category_ids` = VALUES(`category_ids`),
    `status` = VALUES(`status`),
    `remark` = VALUES(`remark`),
    `updater` = VALUES(`updater`);
```
