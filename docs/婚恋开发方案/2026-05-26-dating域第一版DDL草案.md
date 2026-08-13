# dating 域第一版 DDL 草案

## 1. 文档说明

本文档给出婚恋交友平台 `dating` 域第一版 MySQL DDL 草案，用于后续：

- 评审数据模型
- 拆分后端 DO/Mapper/Service
- 生成后台菜单与权限
- 拆解接口与页面开发任务

设计原则：

- 继续复用 `partner`、`member`、`system_users`、`trade_brokerage_*`、`pay_*`、`im_*`
- 婚恋专属能力全部收敛到 `dating_*` 表
- 所有新表遵循当前项目通用字段规范
- 默认兼容 MySQL 8.x

通用字段规范：

- `creator`
- `create_time`
- `updater`
- `update_time`
- `deleted`
- `tenant_id`

默认建表规范：

- `ENGINE=InnoDB`
- `DEFAULT CHARSET=utf8mb4`
- `ROW_FORMAT=DYNAMIC`

## 2. 复用现有主表说明

以下表不在本次 DDL 重建：

- `partner`
- `member`
- `system_users`
- `im_private_message`
- `im_friend`
- `trade_brokerage_user`
- `trade_brokerage_record`
- `trade_brokerage_withdraw`
- `pay_wallet`
- `pay_wallet_transaction`
- `pay_order`
- `pay_refund`

## 3. 状态与枚举建议

在建表前先统一几组枚举口径，后端枚举与字典按此落地：

### 3.1 婚恋接入状态

- `0` 游客
- `10` 基础注册
- `20` 实名通过
- `30` 风控冻结

### 3.2 实名状态

- `0` 未提交
- `10` 审核中
- `20` 审核通过
- `30` 审核拒绝
- `40` 风控冻结

### 3.3 档案状态

- `0` 未完善
- `10` 待审核
- `20` 已上架
- `30` 已下架

### 3.4 点赞匹配状态

- `0` 单向点赞
- `10` 双向互赞
- `20` 已取消

### 3.5 聊天授权状态

- `0` 不可私聊
- `10` 可发破冰
- `20` 已自由聊

### 3.6 活动类型

- `1` 线上
- `2` 线下

## 4. DDL 草案

## 4.1 婚恋档案主表

```sql
CREATE TABLE `dating_profile` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` bigint NOT NULL COMMENT '用户编号',
    `display_name` varchar(64) NOT NULL DEFAULT '' COMMENT '展示昵称',
    `marital_status` tinyint NOT NULL DEFAULT 0 COMMENT '婚姻状态',
    `height_cm` int NOT NULL DEFAULT 0 COMMENT '身高(cm)',
    `weight_kg` int NOT NULL DEFAULT 0 COMMENT '体重(kg)',
    `education` tinyint NOT NULL DEFAULT 0 COMMENT '学历',
    `industry` varchar(64) NOT NULL DEFAULT '' COMMENT '行业',
    `income_level` tinyint NOT NULL DEFAULT 0 COMMENT '收入档位',
    `house_status` tinyint NOT NULL DEFAULT 0 COMMENT '房产情况',
    `car_status` tinyint NOT NULL DEFAULT 0 COMMENT '车辆情况',
    `children_status` tinyint NOT NULL DEFAULT 0 COMMENT '子女情况',
    `hukou_area_id` int DEFAULT NULL COMMENT '户籍地区编号',
    `live_area_id` int DEFAULT NULL COMMENT '常住地区编号',
    `job_title` varchar(64) NOT NULL DEFAULT '' COMMENT '职业/职位',
    `bio` varchar(500) NOT NULL DEFAULT '' COMMENT '自我介绍',
    `profile_status` tinyint NOT NULL DEFAULT 0 COMMENT '档案状态',
    `on_shelf_time` datetime DEFAULT NULL COMMENT '上架时间',
    `off_shelf_reason` varchar(255) NOT NULL DEFAULT '' COMMENT '下架原因',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_profile_status` (`profile_status`),
    KEY `idx_live_area_id` (`live_area_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='婚恋档案主表';
```

## 4.2 婚恋档案敏感扩展表

```sql
CREATE TABLE `dating_profile_detail` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` bigint NOT NULL COMMENT '用户编号',
    `real_name_masked` varchar(64) NOT NULL DEFAULT '' COMMENT '脱敏真实姓名',
    `mobile_masked` varchar(32) NOT NULL DEFAULT '' COMMENT '脱敏手机号',
    `wechat_masked` varchar(64) NOT NULL DEFAULT '' COMMENT '脱敏微信号',
    `id_card_cipher` varchar(512) NOT NULL DEFAULT '' COMMENT '身份证密文',
    `id_card_hash` varchar(128) NOT NULL DEFAULT '' COMMENT '身份证检索哈希',
    `occupation_company` varchar(128) NOT NULL DEFAULT '' COMMENT '工作单位',
    `graduate_school` varchar(128) NOT NULL DEFAULT '' COMMENT '毕业院校',
    `family_intro` varchar(500) NOT NULL DEFAULT '' COMMENT '家庭介绍',
    `mate_requirement_remark` varchar(500) NOT NULL DEFAULT '' COMMENT '择偶补充要求',
    `remark` varchar(500) NOT NULL DEFAULT '' COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_id_card_hash` (`id_card_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='婚恋档案敏感扩展表';
```

## 4.3 婚恋档案标签表

```sql
CREATE TABLE `dating_profile_tag` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` bigint NOT NULL COMMENT '用户编号',
    `tag_code` varchar(64) NOT NULL DEFAULT '' COMMENT '标签编码',
    `tag_name` varchar(64) NOT NULL DEFAULT '' COMMENT '标签名称',
    `tag_source` tinyint NOT NULL DEFAULT 0 COMMENT '标签来源',
    `weight` int NOT NULL DEFAULT 0 COMMENT '标签权重',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_tag` (`user_id`, `tag_code`),
    KEY `idx_tag_code` (`tag_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='婚恋档案标签表';
```

## 4.4 择偶条件表

```sql
CREATE TABLE `dating_match_preference` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` bigint NOT NULL COMMENT '用户编号',
    `age_min` tinyint NOT NULL DEFAULT 0 COMMENT '最小年龄',
    `age_max` tinyint NOT NULL DEFAULT 0 COMMENT '最大年龄',
    `height_min` int NOT NULL DEFAULT 0 COMMENT '最小身高(cm)',
    `height_max` int NOT NULL DEFAULT 0 COMMENT '最大身高(cm)',
    `education_min` tinyint NOT NULL DEFAULT 0 COMMENT '最低学历',
    `income_min` tinyint NOT NULL DEFAULT 0 COMMENT '最低收入档位',
    `marital_status_set` varchar(64) NOT NULL DEFAULT '' COMMENT '接受婚姻状态集合',
    `house_required` tinyint NOT NULL DEFAULT 0 COMMENT '是否要求有房',
    `car_required` tinyint NOT NULL DEFAULT 0 COMMENT '是否要求有车',
    `live_area_ids` varchar(512) NOT NULL DEFAULT '' COMMENT '接受地区编号集合',
    `distance_km` int NOT NULL DEFAULT 0 COMMENT '距离限制(公里)',
    `remark` varchar(500) NOT NULL DEFAULT '' COMMENT '补充要求',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='择偶条件表';
```

## 4.5 实名认证主表

```sql
CREATE TABLE `dating_realname_verify` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` bigint NOT NULL COMMENT '用户编号',
    `verify_status` tinyint NOT NULL DEFAULT 0 COMMENT '实名状态',
    `real_name` varchar(64) NOT NULL DEFAULT '' COMMENT '真实姓名',
    `id_card_cipher` varchar(512) NOT NULL DEFAULT '' COMMENT '身份证密文',
    `id_card_hash` varchar(128) NOT NULL DEFAULT '' COMMENT '身份证检索哈希',
    `ocr_result` varchar(2000) NOT NULL DEFAULT '' COMMENT 'OCR结果JSON',
    `face_check_result` varchar(2000) NOT NULL DEFAULT '' COMMENT '活体结果JSON',
    `third_serial_no` varchar(128) NOT NULL DEFAULT '' COMMENT '三方流水号',
    `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
    `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
    `approve_user_id` bigint DEFAULT NULL COMMENT '审核人编号',
    `reject_reason` varchar(255) NOT NULL DEFAULT '' COMMENT '驳回原因',
    `risk_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否命中风控',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_verify_status` (`verify_status`),
    KEY `idx_id_card_hash` (`id_card_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='实名认证主表';
```

## 4.6 实名认证日志表

```sql
CREATE TABLE `dating_realname_verify_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `verify_id` bigint NOT NULL COMMENT '实名认证编号',
    `before_status` tinyint NOT NULL DEFAULT 0 COMMENT '变更前状态',
    `after_status` tinyint NOT NULL DEFAULT 0 COMMENT '变更后状态',
    `action_type` tinyint NOT NULL DEFAULT 0 COMMENT '操作类型',
    `operator_id` bigint DEFAULT NULL COMMENT '操作人编号',
    `remark` varchar(500) NOT NULL DEFAULT '' COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_verify_id` (`verify_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='实名认证日志表';
```

## 4.7 相册表

```sql
CREATE TABLE `dating_album` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` bigint NOT NULL COMMENT '用户编号',
    `file_id` bigint NOT NULL COMMENT '文件编号',
    `media_type` tinyint NOT NULL DEFAULT 1 COMMENT '媒体类型',
    `is_avatar` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否头像',
    `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序号',
    `audit_status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态',
    `audit_reason` varchar(255) NOT NULL DEFAULT '' COMMENT '审核原因',
    `visible_scope` tinyint NOT NULL DEFAULT 0 COMMENT '可见范围',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='婚恋相册表';
```

## 4.8 点赞关系表

```sql
CREATE TABLE `dating_like` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `from_user_id` bigint NOT NULL COMMENT '发起用户编号',
    `to_user_id` bigint NOT NULL COMMENT '目标用户编号',
    `like_type` tinyint NOT NULL DEFAULT 1 COMMENT '点赞类型',
    `match_status` tinyint NOT NULL DEFAULT 0 COMMENT '匹配状态',
    `source` tinyint NOT NULL DEFAULT 0 COMMENT '来源场景',
    `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_like_user` (`from_user_id`, `to_user_id`),
    KEY `idx_to_user_id` (`to_user_id`),
    KEY `idx_match_status` (`match_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='点赞关系表';
```

## 4.9 拉黑表

```sql
CREATE TABLE `dating_blacklist` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` bigint NOT NULL COMMENT '用户编号',
    `blocked_user_id` bigint NOT NULL COMMENT '被拉黑用户编号',
    `reason` varchar(255) NOT NULL DEFAULT '' COMMENT '拉黑原因',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_black_user` (`user_id`, `blocked_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='拉黑表';
```

## 4.10 举报表

```sql
CREATE TABLE `dating_report` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `report_user_id` bigint NOT NULL COMMENT '举报用户编号',
    `target_user_id` bigint DEFAULT NULL COMMENT '目标用户编号',
    `target_type` tinyint NOT NULL DEFAULT 0 COMMENT '目标类型',
    `target_id` bigint DEFAULT NULL COMMENT '目标业务编号',
    `reason_type` tinyint NOT NULL DEFAULT 0 COMMENT '举报原因类型',
    `content` varchar(500) NOT NULL DEFAULT '' COMMENT '举报内容',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '处理状态',
    `handle_result` varchar(500) NOT NULL DEFAULT '' COMMENT '处理结果',
    `handle_user_id` bigint DEFAULT NULL COMMENT '处理人编号',
    `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_target_type_target_id` (`target_type`, `target_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='举报表';
```

## 4.11 聊天券账户表

```sql
CREATE TABLE `dating_chat_ticket` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` bigint NOT NULL COMMENT '用户编号',
    `available_count` int NOT NULL DEFAULT 0 COMMENT '可用张数',
    `used_count` int NOT NULL DEFAULT 0 COMMENT '已使用张数',
    `expired_count` int NOT NULL DEFAULT 0 COMMENT '已过期张数',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='聊天券账户表';
```

## 4.12 聊天券流水表

```sql
CREATE TABLE `dating_chat_ticket_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` bigint NOT NULL COMMENT '用户编号',
    `biz_type` tinyint NOT NULL DEFAULT 0 COMMENT '业务类型',
    `biz_id` varchar(64) NOT NULL DEFAULT '' COMMENT '业务编号',
    `change_count` int NOT NULL DEFAULT 0 COMMENT '变动数量',
    `balance_count` int NOT NULL DEFAULT 0 COMMENT '变动后余额',
    `remark` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
    `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_biz_type_biz_id` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='聊天券流水表';
```

## 4.13 会话解锁表

```sql
CREATE TABLE `dating_chat_unlock` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_a_id` bigint NOT NULL COMMENT '用户A编号',
    `user_b_id` bigint NOT NULL COMMENT '用户B编号',
    `unlock_type` tinyint NOT NULL DEFAULT 0 COMMENT '解锁类型',
    `unlock_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_unlock_user_pair` (`user_a_id`, `user_b_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='会话解锁表';
```

## 4.14 访客记录表

```sql
CREATE TABLE `dating_browse_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `viewer_user_id` bigint NOT NULL COMMENT '访问者用户编号',
    `target_user_id` bigint NOT NULL COMMENT '被访问用户编号',
    `browse_source` tinyint NOT NULL DEFAULT 0 COMMENT '访问来源',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_target_user_id` (`target_user_id`),
    KEY `idx_viewer_user_id` (`viewer_user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='访客记录表';
```

## 4.15 推荐曝光日志表

```sql
CREATE TABLE `dating_recommend_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` bigint NOT NULL COMMENT '推荐接收用户编号',
    `target_user_id` bigint NOT NULL COMMENT '被推荐用户编号',
    `recommend_scene` tinyint NOT NULL DEFAULT 0 COMMENT '推荐场景',
    `score` decimal(10,2) NOT NULL DEFAULT 0 COMMENT '推荐分值',
    `exposed_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '曝光时间',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_exposed_time` (`exposed_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='推荐曝光日志表';
```

## 4.16 活动主表

```sql
CREATE TABLE `dating_activity` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `merchant_id` bigint NOT NULL COMMENT '商家编号',
    `title` varchar(128) NOT NULL DEFAULT '' COMMENT '活动标题',
    `activity_type` tinyint NOT NULL DEFAULT 1 COMMENT '活动类型',
    `city_id` int DEFAULT NULL COMMENT '城市编号',
    `address` varchar(255) NOT NULL DEFAULT '' COMMENT '活动地址',
    `start_time` datetime NOT NULL COMMENT '开始时间',
    `end_time` datetime NOT NULL COMMENT '结束时间',
    `signup_deadline` datetime NOT NULL COMMENT '报名截止时间',
    `capacity` int NOT NULL DEFAULT 0 COMMENT '名额',
    `signed_count` int NOT NULL DEFAULT 0 COMMENT '已报名人数',
    `price` int NOT NULL DEFAULT 0 COMMENT '报名费用(分)',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
    `cover_url` varchar(255) NOT NULL DEFAULT '' COMMENT '封面图',
    `content` text COMMENT '活动详情',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='活动主表';
```

## 4.17 活动报名表

```sql
CREATE TABLE `dating_activity_signup` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `activity_id` bigint NOT NULL COMMENT '活动编号',
    `user_id` bigint NOT NULL COMMENT '用户编号',
    `signup_status` tinyint NOT NULL DEFAULT 0 COMMENT '报名状态',
    `pay_order_id` bigint DEFAULT NULL COMMENT '支付订单编号',
    `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态',
    `checkin_code` varchar(64) NOT NULL DEFAULT '' COMMENT '签到核销码',
    `checkin_status` tinyint NOT NULL DEFAULT 0 COMMENT '签到状态',
    `checkin_time` datetime DEFAULT NULL COMMENT '签到时间',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
    KEY `idx_pay_order_id` (`pay_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='活动报名表';
```

## 4.18 商家表

```sql
CREATE TABLE `dating_merchant` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `partner_id` bigint NOT NULL COMMENT '关联客商编号',
    `name` varchar(128) NOT NULL DEFAULT '' COMMENT '商家名称',
    `license_no` varchar(128) NOT NULL DEFAULT '' COMMENT '营业执照号',
    `contact_name` varchar(64) NOT NULL DEFAULT '' COMMENT '联系人',
    `contact_mobile_cipher` varchar(255) NOT NULL DEFAULT '' COMMENT '联系人手机号密文',
    `contact_mobile_hash` varchar(128) NOT NULL DEFAULT '' COMMENT '联系人手机号哈希',
    `merchant_status` tinyint NOT NULL DEFAULT 0 COMMENT '商家状态',
    `settle_mode` tinyint NOT NULL DEFAULT 0 COMMENT '结算模式',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_partner_id` (`partner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='婚恋商家表';
```

## 4.19 红娘表

```sql
CREATE TABLE `dating_matchmaker` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `merchant_id` bigint NOT NULL COMMENT '商家编号',
    `user_id` bigint NOT NULL COMMENT '用户编号',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
    `data_scope_type` tinyint NOT NULL DEFAULT 0 COMMENT '数据权限范围',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_merchant_user` (`merchant_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='红娘表';
```

## 4.20 线索表

```sql
CREATE TABLE `dating_lead` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` bigint NOT NULL COMMENT '用户编号',
    `source_type` tinyint NOT NULL DEFAULT 0 COMMENT '线索来源',
    `merchant_id` bigint DEFAULT NULL COMMENT '归属商家编号',
    `matchmaker_user_id` bigint DEFAULT NULL COMMENT '归属红娘用户编号',
    `lead_status` tinyint NOT NULL DEFAULT 0 COMMENT '线索状态',
    `realname_status` tinyint NOT NULL DEFAULT 0 COMMENT '实名状态快照',
    `intention_level` tinyint NOT NULL DEFAULT 0 COMMENT '意向等级',
    `next_follow_time` datetime DEFAULT NULL COMMENT '下次跟进时间',
    `last_follow_time` datetime DEFAULT NULL COMMENT '最近跟进时间',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_matchmaker_user_id` (`matchmaker_user_id`),
    KEY `idx_lead_status` (`lead_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='线索表';
```

## 4.21 跟进记录表

```sql
CREATE TABLE `dating_follow_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `lead_id` bigint NOT NULL COMMENT '线索编号',
    `merchant_id` bigint NOT NULL COMMENT '商家编号',
    `matchmaker_user_id` bigint NOT NULL COMMENT '红娘用户编号',
    `follow_type` tinyint NOT NULL DEFAULT 0 COMMENT '跟进类型',
    `content` varchar(1000) NOT NULL DEFAULT '' COMMENT '跟进内容',
    `next_follow_time` datetime DEFAULT NULL COMMENT '下次跟进时间',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_lead_id` (`lead_id`),
    KEY `idx_matchmaker_user_id` (`matchmaker_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='跟进记录表';
```

## 4.22 商家佣金规则表

```sql
CREATE TABLE `dating_commission_rule` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `merchant_id` bigint NOT NULL COMMENT '商家编号',
    `biz_type` tinyint NOT NULL DEFAULT 0 COMMENT '业务类型',
    `biz_id` bigint DEFAULT NULL COMMENT '业务编号',
    `brokerage_percent` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '返佣比例',
    `settle_delay_days` int NOT NULL DEFAULT 7 COMMENT '延迟结算天数',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
    `effective_time` datetime DEFAULT NULL COMMENT '生效时间',
    `expire_time` datetime DEFAULT NULL COMMENT '失效时间',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_merchant_biz` (`merchant_id`, `biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商家佣金规则表';
```

## 4.23 锁客绑定关系表

```sql
CREATE TABLE `dating_promoter_bind` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` bigint NOT NULL COMMENT '被绑定用户编号',
    `promoter_user_id` bigint NOT NULL COMMENT '推广员用户编号',
    `merchant_id` bigint DEFAULT NULL COMMENT '关联商家编号',
    `bind_time` datetime NOT NULL COMMENT '绑定时间',
    `protect_expire_time` datetime NOT NULL COMMENT '保护期到期时间',
    `last_paid_time` datetime DEFAULT NULL COMMENT '最近支付时间',
    `bind_status` tinyint NOT NULL DEFAULT 0 COMMENT '绑定状态',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_promoter_user_id` (`promoter_user_id`),
    KEY `idx_protect_expire_time` (`protect_expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='锁客绑定关系表';
```

## 4.24 风控事件表

```sql
CREATE TABLE `dating_risk_event` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` bigint NOT NULL COMMENT '用户编号',
    `risk_type` tinyint NOT NULL DEFAULT 0 COMMENT '风控类型',
    `risk_level` tinyint NOT NULL DEFAULT 0 COMMENT '风险等级',
    `source` tinyint NOT NULL DEFAULT 0 COMMENT '触发来源',
    `evidence_json` varchar(4000) NOT NULL DEFAULT '' COMMENT '证据JSON',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '处理状态',
    `handle_result` varchar(500) NOT NULL DEFAULT '' COMMENT '处理结果',
    `handle_user_id` bigint DEFAULT NULL COMMENT '处理人编号',
    `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_risk_type` (`risk_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='风控事件表';
```

## 5. 第一阶段必建表建议

如果按 V1 分阶段推进，建议先建以下 12 张：

- `dating_profile`
- `dating_profile_detail`
- `dating_profile_tag`
- `dating_match_preference`
- `dating_realname_verify`
- `dating_realname_verify_log`
- `dating_album`
- `dating_like`
- `dating_chat_ticket`
- `dating_chat_ticket_record`
- `dating_chat_unlock`
- `dating_promoter_bind`

第二阶段补充：

- `dating_browse_record`
- `dating_report`
- `dating_blacklist`
- `dating_risk_event`

第三阶段补充：

- `dating_activity`
- `dating_activity_signup`
- `dating_merchant`
- `dating_matchmaker`
- `dating_lead`
- `dating_follow_record`
- `dating_commission_rule`

## 6. 待确认项

在正式执行 DDL 前，需要先确认以下事项：

1. 是否允许字段名使用中文
建议正式库不要使用中文字段名。
本文档已统一采用英文数据库字段名，中文仅保留在注释中。

2. 手机号、身份证号密文字段长度
需要根据最终加密方案确定，一般建议：
- AES/Base64：`varchar(255)` 或 `varchar(512)`

3. 是否启用强租户
若 V1 仍采用“平台单租户 + 商家数据权限”，所有新表仍保留 `tenant_id`，统一兼容框架规范。

4. `dating_profile` 是否继续复用 `partner` 的头像、昵称
建议：
- `partner` 保存通用身份
- `dating_profile` 保存婚恋展示态快照

5. 活动支付是否完全复用现有订单中心
建议：
- V1 报名支付先直接挂 `pay_order_id`
- 后续如活动订单复杂，再单独补 `dating_activity_order`
