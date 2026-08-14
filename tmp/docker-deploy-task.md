# suxin 项目 Docker 化部署文件开发任务

你是部署工程师。请为 suxin ERP 项目（yudao-pro 二开，Spring Boot 3 + Vue3）编写完整的 Docker 部署文件。项目根目录就是当前 workdir。

## 项目背景（已侦察确认，直接照用）

- **后端**：可运行模块 `suxin-server`（主类 `SuxinServerApplication`），Java 17，端口 **1930**，API 前缀 `/admin-api`，websocket 路径 `/infra/ws`
- **后端依赖中间件**：MySQL 8（库 `suxin`，root/123456）+ Redis（无密码，database 3）。RocketMQ/Kafka 可选（websocket 是 local 模式，不装）
- **前端**：`UI/vue3-admin`，Vue3 + Vite + pnpm（有 `pnpm-lock.yaml`），history 路由模式（nginx 必须 try_files）
- **数据库初始化脚本已就绪**：`docker/mysql/init/suxin.sql`（含 CREATE DATABASE + USE + 全部表 + 数据，4.31MB，已导出，不要动它）
- **前端生产构建**：`pnpm build:prod`（.env.prod 里 VITE_BASE_URL 为空、VITE_API_URL=/admin-api，走 nginx 反代）

## 重要约定

1. **只新建/修改下列文件，不要动其他任何文件**（尤其不要改 `docker/mysql/init/suxin.sql` 和任何业务代码）。
2. **不要运行 docker / mvn / pnpm**（组长统一验证）。
3. **不要 git commit**。
4. 用中文写注释和文档。

## 交付文件清单

```
docker/
├── mysql/init/suxin.sql        # 已存在，不动
├── backend/Dockerfile          # 新建
├── frontend/Dockerfile         # 新建
├── frontend/nginx.conf         # 新建
├── docker-compose.yml          # 新建
├── .dockerignore               # 新建（放 docker/ 目录，供两个 build context 用）
└── README.md                   # 新建（部署说明）
suxin-server/src/main/resources/application-docker.yaml   # 新建（Docker 环境配置）
```

## 文件 1：application-docker.yaml（后端 Docker 环境配置）

路径：`suxin-server/src/main/resources/application-docker.yaml`

内容：基于 dev 配置，只覆盖「连接地址」相关项，让后端在容器内连到 `mysql`/`redis` 容器名。具体：

```yaml
server:
  port: 1930

spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:mysql://mysql:3306/suxin?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true&rewriteBatchedStatements=true
          username: root
          password: 123456
        slave:
          lazy: true
          url: jdbc:mysql://mysql:3306/suxin?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true&rewriteBatchedStatements=true
          username: root
          password: 123456
  data:
    redis:
      host: redis
      port: 6379
      database: 3
      # 无密码
```

注意：`application.yaml` 里默认 `profiles.active: dev`，Docker 启动时用 `--spring.profiles.active=docker` 覆盖。application-docker.yaml 不需要重复 dev 里的其他配置（Spring 会先加载 application.yaml 基础配置，再叠加 docker profile）。

## 文件 2：backend/Dockerfile（后端多阶段构建）

路径：`docker/backend/Dockerfile`，**build context 是项目根目录**（compose 里用 `context: ..`）

要点：
- 多阶段：`maven:3.9-eclipse-temurin-17` 构建 → `eclipse-temurin:17-jre` 运行
- **阿里云 Maven 镜像**：在构建阶段写一个 settings.xml（或 COPY 进去），mirror 指向 `https://maven.aliyun.com/repository/public`，加速国内依赖下载
- **多模块 pom 层缓存**：项目是多模块聚合（根 pom + suxin-dependencies + suxin-framework + suxin-module + suxin-server），先 COPY 根 pom.xml + 各模块 pom.xml，跑 `mvn dependency:go-offline` 利用 Docker 层缓存
- COPY 全部源码
- `RUN mvn clean package -DskipTests -pl suxin-server -am`
- 运行阶段：`COPY --from=build /app/suxin-server/target/*.jar app.jar`（用通配符，避免版本号硬编码）
- 非 root 用户（useradd）
- **healthcheck 需要 curl**：`eclipse-temurin:17-jre` 无 curl，先 `apt-get update && apt-get install -y curl`（放 USER 之前，root 权限），再切 USER
- `EXPOSE 1930`
- `ENTRYPOINT ["java","-Xmx512m","-jar","app.jar","--spring.profiles.active=docker"]`
- HEALTHCHECK：`curl -f http://localhost:1930/actuator/health || exit 1`（dev 环境 actuator 已开 * 端点）

## 文件 3：frontend/Dockerfile（前端多阶段构建）

路径：`docker/frontend/Dockerfile`，**build context 是 `UI/vue3-admin`**（compose 里用 `context: ../UI/vue3-admin`）

要点：
- 多阶段：`node:20-alpine` 构建 → `nginx:alpine` 运行
- pnpm：用 `corepack enable && corepack prepare pnpm@9 --activate`（或 `npm install -g pnpm`，优先 corepack）
- **淘宝 npm 镜像**：`pnpm config set registry https://registry.npmmirror.com`（在 pnpm install 之前）
- `RUN pnpm install --frozen-lockfile`（用 pnpm-lock.yaml 精确安装）
- `RUN pnpm build:prod`
- 运行阶段：`COPY --from=build /app/dist /usr/share/nginx/html`，`COPY nginx.conf /etc/nginx/conf.d/default.conf`
- `EXPOSE 80`

## 文件 4：frontend/nginx.conf

路径：`docker/frontend/nginx.conf`

要点：
- `location /admin-api/` 反代 `http://backend:1930`（注意保留路径，proxy_pass 末尾不带斜杠或用正确写法）
- **websocket 升级**：`location /infra/ws` 必须带 `proxy_http_version 1.1; proxy_set_header Upgrade $http_upgrade; proxy_set_header Connection "upgrade";` + `proxy_read_timeout 3600s;` + `proxy_buffering off;`
- `location /` 用 `try_files $uri $uri/ /index.html;`（history 路由模式必须）
- gzip 开启
- 上传大小限制 `client_max_body_size 32m;`（后端 multipart 32MB）

## 文件 5：docker-compose.yml

路径：`docker/docker-compose.yml`

要点：
- **删除 `version:` 行**（compose v2 废弃）
- 4 个服务：`mysql`、`redis`、`backend`、`frontend`
- `mysql`：镜像 `mysql:8.0`，`MYSQL_ROOT_PASSWORD=123456`，挂载 `./mysql/init:/docker-entrypoint-initdb.d:ro`（初始化自动导入 suxin.sql），数据卷 `mysql_data:/var/lib/mysql`，healthcheck `mysqladmin ping -h localhost -p123456`，端口 `3306:3306`
- `redis`：镜像 `redis:7`，healthcheck `redis-cli ping`，端口 `6379:6379`
- `backend`：build `./backend`（context 指项目根），`depends_on` mysql + redis 且 `condition: service_healthy`，端口 `1930:1930`
- `frontend`：build `./frontend`，`depends_on` backend，端口 `80:80`
- 默认网络，服务名互访（backend 用 `mysql`/`redis`，nginx 用 `backend`）
- 中文注释说明每个服务的用途

## 文件 6：docker/.dockerignore

排除 node_modules、dist、target、.git、.idea、tmp、logs 等，减小 build context。

## 文件 7：docker/README.md（部署说明）

内容：
- 前置要求：Docker + Docker Compose v2
- 一键启动：`cd docker && docker compose up -d --build`
- 访问地址：前端 `http://<服务器IP>`、后端 API `http://<服务器IP>:1930`、Druid 监控、Swagger
- 默认账号密码（yudao-pro 默认 admin/admin123，如不确定写「见数据库 system_users 表」）
- 停止：`docker compose down`
- 数据持久化说明（mysql_data 卷）
- 常用命令：查看日志 `docker compose logs -f backend`、重启等

## 完成后

用中文列出：创建了哪些文件、每个文件的关键点。不要运行构建。
