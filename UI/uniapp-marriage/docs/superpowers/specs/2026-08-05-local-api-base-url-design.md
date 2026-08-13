# 本地 API 请求地址配置设计

## 目标

让当前 uni-app 项目在开发模式下请求本机后端，最终 API 前缀为 `http://localhost:1930/app-api`，并与参考项目 `D:\develop\front\uniapp-marriage-1` 的本地接口配置保持一致。

## 变更范围

- 将 `.env` 中的 `SHOPRO_DEV_BASE_URL` 从线上地址改为 `http://localhost:1930`。
- 保留 `SHOPRO_API_PATH=/app-api`，由现有请求配置将二者拼接为 `http://localhost:1930/app-api`。
- 保持生产地址、体验版地址、H5 地址、租户 `303`、微信 AppID 和 UniApp AppID 不变。
- 不修改参考项目。

## 数据流

开发构建读取 `SHOPRO_DEV_BASE_URL`，`sheep/config/index.js` 将其作为 `baseUrl`；`sheep/request/index.js` 使用 `baseUrl + apiPath` 生成请求基址。

## 风险与处理

- 不把 `/app-api` 写入 `SHOPRO_DEV_BASE_URL`，避免与 `SHOPRO_API_PATH` 重复拼接。
- `localhost` 只指向运行客户端的设备；本方案面向用户已确认的本机调试场景。
- 当前工作树包含其他未提交修改，本次只修改 `.env` 和本设计记录，不触碰其他文件。

## 验证

- 检查环境变量组合后的地址严格等于 `http://localhost:1930/app-api`。
- 运行项目现有可用的配置检查或构建命令；若项目未提供完整 CLI 构建脚本，则至少执行静态配置检查并报告限制。
