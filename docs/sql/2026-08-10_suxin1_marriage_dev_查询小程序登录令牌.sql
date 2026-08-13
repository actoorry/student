SELECT id,
       user_id,
       user_type,
       client_id,
       LENGTH(access_token) AS access_token_length,
       LENGTH(refresh_token) AS refresh_token_length,
       expires_time,
       create_time,
       tenant_id
FROM system_oauth2_access_token
WHERE tenant_id = 393
  AND create_time >= '2026-08-10 11:50:30'
  AND create_time < '2026-08-10 11:51:10'
ORDER BY id DESC;

SELECT id,
       client_id,
       status,
       access_token_validity_seconds,
       refresh_token_validity_seconds,
       create_time,
       update_time
FROM system_oauth2_client
WHERE client_id = 'marriage'
  AND deleted = 0;

SELECT NOW() AS database_now,
       @@session.time_zone AS session_time_zone,
       @@global.time_zone AS global_time_zone;

SELECT id,
       user_id,
       user_type,
       client_id,
       LENGTH(access_token) AS access_token_length,
       expires_time,
       create_time,
       tenant_id
FROM system_oauth2_access_token
WHERE create_time >= '2026-08-10 11:50:30'
  AND create_time < '2026-08-10 11:51:10'
ORDER BY id DESC;

SELECT id,
       user_id,
       user_type,
       client_id,
       LENGTH(access_token) AS access_token_length,
       expires_time,
       create_time,
       tenant_id
FROM system_oauth2_access_token
WHERE client_id = 'marriage'
ORDER BY id DESC
LIMIT 10;

SELECT id,
       application_name,
       request_url,
       result_code,
       begin_time,
       end_time,
       create_time,
       tenant_id
FROM infra_api_access_log
WHERE create_time >= '2026-08-10 11:50:30'
  AND create_time < '2026-08-10 11:51:10'
  AND request_url = '/app-api/partner/auth/weixin-mini-login'
ORDER BY id DESC;
