# suxin ERP Docker 部署说明

本目录包含 suxin ERP 项目的完整 Docker 部署文件，可一键拉起 MySQL、Redis、后端、前端四个服务。

## 前置要求

- Docker 20.10+
- Docker Compose v2（支持 `docker compose` 子命令）
- 服务器开放端口：`80`（前端）、`1930`（后端）、`3306`（MySQL）、`6379`（Redis）

## 一键启动

```bash
cd docker
docker compose up -d --build
```

首次启动会自动完成：

1. 构建后端镜像（Maven 多阶段构建，阿里云 Maven 镜像加速）
2. 构建前端镜像（Node 构建 + Nginx，淘宝 npm 镜像加速）
3. 启动 MySQL，并自动执行 `mysql/init/suxin.sql` 初始化数据库（含建库、建表、初始数据，约 4.3MB）

## 访问地址

| 服务 | 地址 |
| ---- | ---- |
| 前端 | `http://<服务器IP>` |
| 后端 API | `http://<服务器IP>:1930/admin-api/` |
| Swagger 文档 | `http://<服务器IP>:1930/swagger-ui/index.html` |
| knife4j 文档 | `http://<服务器IP>:1930/doc.html` |
| Druid 监控 | `http://<服务器IP>:1930/druid/index.html`（docker 环境默认未开启，见下方说明） |

## 默认账号

yudao-pro 默认管理员账号为 `admin / admin123`；如登录失败，请查询数据库 `system_users` 表。

## 停止与清理

```bash
# 停止并移除容器（保留数据卷 mysql_data）
docker compose down

# 停止并同时删除数据卷（清空数据库数据，慎用）
docker compose down -v
```

## 数据持久化

MySQL 数据保存在命名卷 `mysql_data` 中，即使容器删除，数据仍保留。
初始化 SQL 仅在数据卷为空（首次启动）时执行。

## 常用命令

```bash
# 查看后端日志
docker compose logs -f backend

# 查看前端日志
docker compose logs -f frontend

# 查看所有容器状态
docker compose ps

# 重启后端
docker compose restart backend

# 重启全部服务
docker compose restart

# 代码变更后重新构建并启动
docker compose up -d --build
```

## 端口与中间件

| 服务 | 端口 | 说明 |
| ---- | ---- | ---- |
| frontend | 80 | Nginx 前端 + 反向代理 |
| backend | 1930 | Spring Boot 后端 |
| mysql | 3306 | MySQL 8，数据库 suxin |
| redis | 6379 | Redis，database 3，无密码 |

## 注意事项

1. 后端通过容器名 `mysql` / `redis` 连接中间件（见 `suxin-server/src/main/resources/application-docker.yaml`），无需修改 IP。
2. 生产环境建议修改 MySQL root 密码：同步修改 `docker-compose.yml` 的 `MYSQL_ROOT_PASSWORD` 与 `application-docker.yaml` 中的数据源密码。
3. 数据库初始化脚本为 `docker/mysql/init/suxin.sql`，请勿随意修改。
4. 前端构建产物输出目录为 `dist-prod`（由 `UI/vue3-admin/.env.prod` 的 `VITE_OUT_DIR` 决定），已在前端 Dockerfile 中对应处理。
5. Druid 监控、Quartz 的 JDBC 存储等配置位于 `application-dev.yaml`，docker profile 默认不加载；如需在容器内启用，请补充到 `application-docker.yaml`。
