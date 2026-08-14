# suxin ERP Docker 部署

本目录提供 suxin ERP 的一键 Docker 部署文件，启动后包含 MySQL、Redis、后端、前端四个服务。

## 前置要求

- Docker 20.10+
- Docker Compose v2
- 开放端口：80（前端）、1930（后端）、3306（MySQL）、6379（Redis）

## 一键启动

```bash
cd docker
docker compose up -d --build
```

首次启动会自动完成三件事：构建后端镜像、构建前端镜像、启动 MySQL 并导入 `mysql/init/suxin.sql`（建库建表 + 初始数据）。

## 访问地址

| 服务 | 地址 |
| ---- | ---- |
| 前端 | http://服务器IP |
| 后端 API | http://服务器IP:1930/admin-api/ |
| 接口文档 | http://服务器IP:1930/doc.html |
| Druid 监控 | http://服务器IP:1930/druid/index.html |

## 默认账号

`admin / admin123`，如登录失败请查询数据库 `system_users` 表。

## 停止与数据

```bash
docker compose down        # 停止，保留数据
docker compose down -v     # 停止并删除数据卷（慎用）
```

MySQL 数据保存在命名卷 `mysql_data`，删除容器后数据仍保留；初始化 SQL 仅在数据卷为空（首次启动）时执行。

## 常用命令

```bash
docker compose logs -f backend   # 查看后端日志
docker compose ps                # 查看服务状态
docker compose restart backend   # 重启后端
docker compose up -d --build     # 代码变更后重建并启动
```

## 服务与端口

| 服务 | 端口 | 说明 |
| ---- | ---- | ---- |
| frontend | 80 | Nginx 前端 + 反向代理 |
| backend | 1930 | Spring Boot 后端 |
| mysql | 3306 | MySQL 8，库 suxin |
| redis | 6379 | Redis，database 3，无密码 |

## 配置说明

- 后端通过容器名 `mysql` / `redis` 连接中间件，无需改 IP（见 `application-docker.yaml`）。
- 后端激活 `dev,docker` 双 profile：`dev` 提供完整功能配置（Quartz、Druid、验证码、日志等），`docker` 覆盖中间件连接地址为容器名。
- 生产部署建议修改 MySQL root 密码，并同步修改 `docker-compose.yml` 的 `MYSQL_ROOT_PASSWORD` 与 `application-docker.yaml` 中的数据源密码。
