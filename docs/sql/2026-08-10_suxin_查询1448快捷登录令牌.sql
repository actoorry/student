SELECT id,
       user_id,
       user_type,
       client_id,
       LENGTH(access_token) AS access_token_length,
       LENGTH(refresh_token) AS refresh_token_length,
       expires_time,
       create_time,
       tenant_id
FROM suxin.system_oauth2_access_token
WHERE tenant_id = 393
  AND create_time >= '2026-08-10 14:48:45'
  AND create_time < '2026-08-10 14:49:05'
ORDER BY create_time ASC, id ASC;

SELECT id,
       request_method,
       request_url,
       result_code,
       result_msg,
       begin_time,
       end_time,
       duration,
       tenant_id
FROM suxin.infra_api_access_log
WHERE tenant_id = 393
  AND begin_time >= '2026-08-10 14:48:45'
  AND begin_time < '2026-08-10 14:49:05'
ORDER BY begin_time ASC, id ASC;

SHOW DATABASES;

SHOW TABLES FROM suxin LIKE 'system_oauth2_access_token';

SHOW TABLES FROM suxin LIKE 'infra_api_access_log';

SELECT table_schema,
       table_name
FROM information_schema.tables
WHERE table_name IN ('system_oauth2_access_token', 'infra_api_access_log')
ORDER BY table_schema, table_name;

SELECT 'suxin1' AS database_name,
       id,
       user_id,
       user_type,
       client_id,
       LENGTH(access_token) AS access_token_length,
       LENGTH(refresh_token) AS refresh_token_length,
       create_time,
       tenant_id
FROM suxin1.system_oauth2_access_token
WHERE tenant_id = 393
  AND create_time >= '2026-08-10 14:48:45'
  AND create_time < '2026-08-10 14:49:05'
UNION ALL
SELECT 'suxin1_marriage' AS database_name,
       id,
       user_id,
       user_type,
       client_id,
       LENGTH(access_token) AS access_token_length,
       LENGTH(refresh_token) AS refresh_token_length,
       create_time,
       tenant_id
FROM suxin1_marriage.system_oauth2_access_token
WHERE tenant_id = 393
  AND create_time >= '2026-08-10 14:48:45'
  AND create_time < '2026-08-10 14:49:05'
UNION ALL
SELECT 'suxin1_marriage_dev' AS database_name,
       id,
       user_id,
       user_type,
       client_id,
       LENGTH(access_token) AS access_token_length,
       LENGTH(refresh_token) AS refresh_token_length,
       create_time,
       tenant_id
FROM suxin1_marriage_dev.system_oauth2_access_token
WHERE tenant_id = 393
  AND create_time >= '2026-08-10 14:48:45'
  AND create_time < '2026-08-10 14:49:05'
ORDER BY create_time ASC, id ASC;
