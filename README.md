# 校园管理系统

> 基于 suxin 平台开发的校园管理系统，覆盖学生、教师、课程、考试记录等核心业务。

## 项目介绍

本系统是基于 suxin 快速开发平台构建的校园管理系统，提供学生管理、教师管理、课程管理、考试记录等功能，帮助学校维护基础数据、组织选课并管理成绩信息。

系统采用前后端分离架构，后端基于 Spring Boot 3 与 MyBatis-Plus，前端基于 Vue 3 与 Element Plus，支持多租户、数据权限、定时任务等平台能力，可独立部署或通过 Docker 一键编排。

## 功能特性

| 模块 | 功能 |
| ---- | ---- |
| 学生管理 | 学号、姓名、性别、班级、联系方式，支持当前学期总分统计 |
| 教师管理 | 工号、姓名、性别、职称，支持授课课程关联 |
| 课程管理 | 课程编号、名称、学分、授课教师，支持学生选课 |
| 考试记录 | 成绩录入、学年学期筛选、及格人数统计、学生/课程关联展示 |

## 技术栈

| 端 | 技术 |
| ---- | ---- |
| 后端 | Java 17、Spring Boot 3、MyBatis-Plus、MySQL 8、Redis |
| 前端 | Vue 3、TypeScript、Vite、pnpm、Element Plus |
| 部署 | Docker、Docker Compose、Nginx |

## 快速开始

后端：

```bash
mvn clean package -DskipTests -pl suxin-server -am
java -jar suxin-server/target/suxin-server.jar
```

前端：

```bash
cd UI/vue3-admin
pnpm install
pnpm dev
```

Docker 部署：

```bash
cd docker
docker compose up -d --build
```

## 目录结构

```
suxin-dependencies   依赖版本管理
suxin-framework      框架层（suxin 平台基础模块与 starter）
suxin-module         业务模块（campus 校园管理域）
suxin-server         启动模块
UI/vue3-admin        PC 管理端
docs                 文档与数据库脚本
docker               Docker 部署文件
```

## 版本

当前版本 2026.03。
