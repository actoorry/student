# ============================================================
# suxin 前端多阶段构建：Node 构建 -> Nginx 运行
# build context 为项目根目录（docker-compose 中 context: ..）
# 说明：nginx.conf 位于 docker/frontend/，需与前端源码同属一个 build context，
#       因此此处使用项目根目录作为 context，Dockerfile 内以 UI/vue3-admin/ 前缀引用前端源码。
# ============================================================

# ---------- 构建阶段 ----------
FROM node:20-alpine AS builder

WORKDIR /app

# 1. 启用 corepack 并准备 pnpm 9（pnpm-lock.yaml 为 lockfileVersion 9.0）
RUN corepack enable && corepack prepare pnpm@9.15.4 --activate

# 2. 使用淘宝 npm 镜像，加速依赖下载
RUN pnpm config set registry https://registry.npmmirror.com

# 3. 先复制依赖描述文件，利用 Docker 层缓存
COPY UI/vue3-admin/package.json UI/vue3-admin/pnpm-lock.yaml UI/vue3-admin/pnpm-workspace.yaml UI/vue3-admin/

WORKDIR /app/UI/vue3-admin

# 4. 依据 pnpm-lock.yaml 精确安装依赖
RUN pnpm install --frozen-lockfile

# 5. 复制前端源码
COPY UI/vue3-admin/ ./

# 6. 生产构建（.env.prod 中 VITE_OUT_DIR=dist-prod，产物输出到 dist-prod 目录）
RUN pnpm build:prod

# ---------- 运行阶段 ----------
FROM nginx:alpine

# 复制构建产物（注意：实际输出目录为 dist-prod，由 .env.prod 的 VITE_OUT_DIR 决定）
COPY --from=builder /app/UI/vue3-admin/dist-prod /usr/share/nginx/html

# 复制 nginx 站点配置
COPY docker/frontend/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
