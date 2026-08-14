# ============================================================
# suxin 后端多阶段构建：Maven 构建 -> JRE 运行
# build context 为项目根目录（docker-compose 中 context: ..）
# ============================================================

# ---------- 构建阶段 ----------
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 1. 配置阿里云 Maven 镜像，加速国内依赖下载
RUN mkdir -p /root/.m2 && printf '%s\n' \
    '<?xml version="1.0" encoding="UTF-8"?>' \
    '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"' \
    '          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' \
    '          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">' \
    '    <mirrors>' \
    '        <mirror>' \
    '            <id>aliyunmaven</id>' \
    '            <name>阿里云公共仓库</name>' \
    '            <url>https://maven.aliyun.com/repository/public</url>' \
    '            <mirrorOf>central</mirrorOf>' \
    '        </mirror>' \
    '    </mirrors>' \
    '</settings>' > /root/.m2/settings.xml

# 2. 先复制各模块 pom.xml，利用 Docker 层缓存（pom 不变时无需重新下载依赖）
COPY pom.xml ./
COPY suxin-dependencies/pom.xml ./suxin-dependencies/pom.xml
COPY suxin-framework/pom.xml ./suxin-framework/pom.xml
COPY suxin-framework/suxin-common/pom.xml ./suxin-framework/suxin-common/pom.xml
COPY suxin-framework/suxin-module-ai/pom.xml ./suxin-framework/suxin-module-ai/pom.xml
COPY suxin-framework/suxin-module-base/pom.xml ./suxin-framework/suxin-module-base/pom.xml
COPY suxin-framework/suxin-module-bpm/pom.xml ./suxin-framework/suxin-module-bpm/pom.xml
COPY suxin-framework/suxin-module-iot/pom.xml ./suxin-framework/suxin-module-iot/pom.xml
COPY suxin-framework/suxin-module-iot/suxin-module-iot-biz/pom.xml ./suxin-framework/suxin-module-iot/suxin-module-iot-biz/pom.xml
COPY suxin-framework/suxin-module-iot/suxin-module-iot-core/pom.xml ./suxin-framework/suxin-module-iot/suxin-module-iot-core/pom.xml
COPY suxin-framework/suxin-module-iot/suxin-module-iot-gateway/pom.xml ./suxin-framework/suxin-module-iot/suxin-module-iot-gateway/pom.xml
COPY suxin-framework/suxin-module-mp/pom.xml ./suxin-framework/suxin-module-mp/pom.xml
COPY suxin-framework/suxin-module-report/pom.xml ./suxin-framework/suxin-module-report/pom.xml
COPY suxin-framework/suxin-spring-boot-starter-biz-data-permission/pom.xml ./suxin-framework/suxin-spring-boot-starter-biz-data-permission/pom.xml
COPY suxin-framework/suxin-spring-boot-starter-biz-ip/pom.xml ./suxin-framework/suxin-spring-boot-starter-biz-ip/pom.xml
COPY suxin-framework/suxin-spring-boot-starter-biz-tenant/pom.xml ./suxin-framework/suxin-spring-boot-starter-biz-tenant/pom.xml
COPY suxin-framework/suxin-spring-boot-starter-excel/pom.xml ./suxin-framework/suxin-spring-boot-starter-excel/pom.xml
COPY suxin-framework/suxin-spring-boot-starter-job/pom.xml ./suxin-framework/suxin-spring-boot-starter-job/pom.xml
COPY suxin-framework/suxin-spring-boot-starter-monitor/pom.xml ./suxin-framework/suxin-spring-boot-starter-monitor/pom.xml
COPY suxin-framework/suxin-spring-boot-starter-mq/pom.xml ./suxin-framework/suxin-spring-boot-starter-mq/pom.xml
COPY suxin-framework/suxin-spring-boot-starter-mybatis/pom.xml ./suxin-framework/suxin-spring-boot-starter-mybatis/pom.xml
COPY suxin-framework/suxin-spring-boot-starter-protection/pom.xml ./suxin-framework/suxin-spring-boot-starter-protection/pom.xml
COPY suxin-framework/suxin-spring-boot-starter-redis/pom.xml ./suxin-framework/suxin-spring-boot-starter-redis/pom.xml
COPY suxin-framework/suxin-spring-boot-starter-security/pom.xml ./suxin-framework/suxin-spring-boot-starter-security/pom.xml
COPY suxin-framework/suxin-spring-boot-starter-test/pom.xml ./suxin-framework/suxin-spring-boot-starter-test/pom.xml
COPY suxin-framework/suxin-spring-boot-starter-web/pom.xml ./suxin-framework/suxin-spring-boot-starter-web/pom.xml
COPY suxin-framework/suxin-spring-boot-starter-websocket/pom.xml ./suxin-framework/suxin-spring-boot-starter-websocket/pom.xml
COPY suxin-module/pom.xml ./suxin-module/pom.xml
COPY suxin-server/pom.xml ./suxin-server/pom.xml

# 3. 预下载依赖（离线缓存到本地 Maven 仓库，加速后续构建）
RUN mvn -B dependency:go-offline -pl suxin-server -am -DskipTests

# 4. 复制全部源码
COPY . .

# 5. 打包 suxin-server 及其依赖模块
RUN mvn clean package -DskipTests -pl suxin-server -am

# ---------- 运行阶段 ----------
FROM eclipse-temurin:17-jre

# 安装 curl 用于 HEALTHCHECK（必须在切换 USER 之前执行，需要 root 权限）
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# 创建非 root 运行用户
RUN useradd -m suxin

WORKDIR /app

# 复制构建产物（finalName 为 suxin-server，通配符避免版本号硬编码）
COPY --from=builder /app/suxin-server/target/*.jar app.jar

# 切换非 root 用户运行
USER suxin

EXPOSE 1930

# 使用 dev,docker 双 profile：dev 提供完整功能配置（Quartz JDBC、Druid、验证码、日志等），
# docker 覆盖中间件连接地址为容器名（mysql / redis）
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar", "--spring.profiles.active=dev,docker"]

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:1930/actuator/health || exit 1
