-- 已废弃：支付应用现统一使用 app_key='mall'，禁止执行本文件中的旧 INSERT。
-- 历史背景：租户 393 下 pay_app 表缺少 app_key='wx_lite_marriage' 的支付应用记录，
-- 导致下单支付时报错 "App 不存在"（PayAppServiceImpl.validatePayApp:158）。
-- 该配置镜像租户 303（id=9）的 wx_lite_marriage 应用，执行前请先确认 id=9 记录仍存在。

-- INSERT INTO pay_app (app_key, name, status, remark, order_notify_url, refund_notify_url, transfer_notify_url, creator, updater, tenant_id)
-- SELECT 'wx_lite_marriage', name, status, '军婚恋支付应用(复制自租户303)', order_notify_url, refund_notify_url, transfer_notify_url, '1', '1', 393
-- FROM pay_app
-- WHERE id = 9 AND deleted = 0;
