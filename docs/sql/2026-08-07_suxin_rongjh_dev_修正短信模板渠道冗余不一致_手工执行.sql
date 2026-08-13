-- ============================================================================
-- 恢复原版短信端到端流程：修正短信模板 channel_id/channel_code 不一致（最小手工 DML）
-- 数据库：suxin_rongjh_dev
-- 日期：2026-08-07
--
-- 【执行方式】本脚本为 DML，禁止通过 MySQL MCP 执行；请由用户审核后在受控环境手工执行。
--
-- 【影响范围】
--   仅影响 system_sms_template 中 id=7（test-04）这一条模板的 channel_code 冗余字段：
--   该模板 channel_id=7（指向 TENCENT 渠道），但冗余 channel_code 仍为 DEBUG_DING_TALK，
--   与本条记录实际绑定的渠道不一致。修正原则：channel_code 是 channel_id 引用渠道编码的
--   冗余副本，应与其保持一致。
--
-- 【注意事项】
--   1. 本脚本不包含任何密钥（SecretId/SecretKey/SDK AppId 不写入 SQL）。
--   2. 6 个业务模板（user-sms-login / user-update-mobile / user-update-password /
--      user-reset-password / admin-sms-login / admin-reset-password）当前仍绑定
--      DEBUG_DING_TALK 调试渠道；正式腾讯云签名、模板编号、SDK AppId、回调域名确认后，
--      再在管理端或另行脚本绑定腾讯云渠道（本变更范围不直接改绑定，留待用户确认）。
--   3. 腾讯云渠道（id=7）当前 api_key 为单段 SecretId，不符合 TencentSmsClient 要求的
--      "secretId sdkAppId" 两段格式，需由管理员在管理端渠道配置中录入真实凭据。
--   4. 执行前建议先执行只读复核脚本
--      docs/sql/2026-08-07_suxin_rongjh_dev_短信模板渠道关联只读复核.sql 核对现状。
--
-- 【执行顺序】单条 UPDATE，可独立执行。
-- ============================================================================

-- 1. 修正模板 7（test-04）的 channel_code 冗余值与 channel_id 指向的渠道一致
UPDATE system_sms_template
SET channel_code = 'TENCENT',
    updater = 'restore-original-sms-end-to-end-flow',
    update_time = CURRENT_TIMESTAMP
WHERE id = 7
  AND channel_id = 7
  AND channel_code = 'DEBUG_DING_TALK'
  AND deleted = b'0';

-- 2. 执行后核对（只读）
SELECT id, code, channel_id, channel_code
FROM system_sms_template
WHERE id = 7 AND deleted = b'0';

-- ============================================================================
-- 【回滚 SQL】如需回滚本条修正，执行以下语句：
-- UPDATE system_sms_template
-- SET channel_code = 'DEBUG_DING_TALK',
--     updater = 'rollback-restore-original-sms-end-to-end-flow',
--     update_time = CURRENT_TIMESTAMP
-- WHERE id = 7
--   AND channel_id = 7
--   AND channel_code = 'TENCENT'
--   AND deleted = b'0';
-- ============================================================================
