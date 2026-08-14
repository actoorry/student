-- ============================================================
-- 校园管理（campus）模块 菜单 + 按钮权限 + 字典 配置脚本
-- 数据库：suxin（dev 环境）
-- 执行方式：docker exec -i mysql8 mysql -uroot -p123456 --default-character-set=utf8mb4 suxin < 本文件
-- 说明：显式指定 id（从 6144 起，避开现有最大 id 6143），幂等可重复执行。
-- ============================================================

SET NAMES utf8mb4;

-- ========== 一级目录：校园管理 ==========
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (6144, '校园管理', '', 1, 45, 0, '/campus', 'ep:school', NULL, NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `name`='校园管理', `path`='/campus', `sort`=45, `icon`='ep:school', `deleted`=b'0';

-- ========== 学生管理（菜单 6145 + 按钮 6146-6149）==========
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (6145, '学生管理', '', 2, 1, 6144, 'student', 'ep:user', 'campus/student/index', 'CampusStudent', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `name`='学生管理', `parent_id`=6144, `path`='student', `component`='campus/student/index', `component_name`='CampusStudent', `deleted`=b'0';

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(6146, '学生查询', 'campus:student:query', 3, 1, 6145, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6147, '学生新增', 'campus:student:create', 3, 2, 6145, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6148, '学生修改', 'campus:student:update', 3, 3, 6145, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6149, '学生删除', 'campus:student:delete', 3, 4, 6145, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `permission`=VALUES(`permission`), `parent_id`=VALUES(`parent_id`), `deleted`=b'0';

-- ========== 教师管理（菜单 6150 + 按钮 6151-6154）==========
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (6150, '教师管理', '', 2, 2, 6144, 'teacher', 'ep:avatar', 'campus/teacher/index', 'CampusTeacher', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `name`='教师管理', `parent_id`=6144, `path`='teacher', `component`='campus/teacher/index', `component_name`='CampusTeacher', `deleted`=b'0';

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(6151, '教师查询', 'campus:teacher:query', 3, 1, 6150, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6152, '教师新增', 'campus:teacher:create', 3, 2, 6150, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6153, '教师修改', 'campus:teacher:update', 3, 3, 6150, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6154, '教师删除', 'campus:teacher:delete', 3, 4, 6150, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `permission`=VALUES(`permission`), `parent_id`=VALUES(`parent_id`), `deleted`=b'0';

-- ========== 课程管理（菜单 6155 + 按钮 6156-6160）==========
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (6155, '课程管理', '', 2, 3, 6144, 'course', 'ep:reading', 'campus/course/index', 'CampusCourse', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `name`='课程管理', `parent_id`=6144, `path`='course', `component`='campus/course/index', `component_name`='CampusCourse', `deleted`=b'0';

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(6156, '课程查询', 'campus:course:query', 3, 1, 6155, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6157, '课程新增', 'campus:course:create', 3, 2, 6155, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6158, '课程修改', 'campus:course:update', 3, 3, 6155, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6159, '课程删除', 'campus:course:delete', 3, 4, 6155, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6160, '学生选课', 'campus:course:select', 3, 5, 6155, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `permission`=VALUES(`permission`), `parent_id`=VALUES(`parent_id`), `deleted`=b'0';

-- ========== 考试记录（菜单 6161 + 按钮 6162-6165）==========
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (6161, '考试记录', '', 2, 4, 6144, 'exam-record', 'ep:document', 'campus/examRecord/index', 'CampusExamRecord', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `name`='考试记录', `parent_id`=6144, `path`='exam-record', `component`='campus/examRecord/index', `component_name`='CampusExamRecord', `deleted`=b'0';

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(6162, '成绩查询', 'campus:exam-record:query', 3, 1, 6161, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6163, '成绩录入', 'campus:exam-record:create', 3, 2, 6161, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6164, '成绩修改', 'campus:exam-record:update', 3, 3, 6161, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6165, '成绩删除', 'campus:exam-record:delete', 3, 4, 6161, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `permission`=VALUES(`permission`), `parent_id`=VALUES(`parent_id`), `deleted`=b'0';

-- ========== 数据字典：性别、学期 ==========
INSERT INTO `system_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES ('校园-性别', 'campus_gender', 0, '校园管理性别字典', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `name`='校园-性别', `deleted`=b'0';

INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(1, '男', '1', 'campus_gender', 0, '1', NOW(), '1', NOW(), b'0'),
(2, '女', '2', 'campus_gender', 0, '1', NOW(), '1', NOW(), b'0');

INSERT INTO `system_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES ('校园-学期', 'campus_semester', 0, '校园管理学期字典', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `name`='校园-学期', `deleted`=b'0';

INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(1, '第一学期', '1', 'campus_semester', 0, '1', NOW(), '1', NOW(), b'0'),
(2, '第二学期', '2', 'campus_semester', 0, '1', NOW(), '1', NOW(), b'0');
