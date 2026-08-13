SELECT id,
       request_method,
       request_url,
       result_code,
       result_msg,
       begin_time,
       end_time,
       duration,
       tenant_id
FROM infra_api_access_log
WHERE tenant_id = 393
  AND begin_time >= '2026-08-10 13:59:15'
  AND begin_time < '2026-08-10 13:59:25'
ORDER BY begin_time ASC, id ASC;

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
  AND create_time >= '2026-08-10 13:59:15'
  AND create_time < '2026-08-10 13:59:25'
ORDER BY create_time ASC, id ASC;

SELECT NOW() AS database_now,
       @@session.time_zone AS session_time_zone,
       MAX(begin_time) AS latest_api_begin_time
FROM infra_api_access_log
WHERE tenant_id = 393;
