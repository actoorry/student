# uniapp-mall 微信小程序凭据恢复设计

- 日期：2026-08-10
- 范围：`UI/uniapp-mall` 与当前启用的后端 `dev` 配置

## 目标

恢复 `uniapp-mall` 使用的微信小程序身份，使微信开发者工具、客户端登录请求和后端 `code2Session` 使用同一个小程序 AppID。

## 配置边界

1. `UI/uniapp-mall/manifest.json` 只修改 `mp-weixin.appid`。
2. `suxin-server/src/main/resources/application-dev.yaml` 同步修改 `wx.miniapp.appid` 和 `wx.miniapp.secret`。
3. `app-plus.distribute.sdkConfigs` 下的 OAuth、支付和分享属于 App 端微信能力，不随小程序 AppID 一起修改。
4. 小程序密钥只写入服务端配置，不进入 uni-app 客户端。用户已明确要求在本地服务端配置文件中保存明文密钥。
5. 不修改 `application-prod.yaml`、`application-zjx.yaml` 或用户未跟踪的 `application-rongjih.yaml`。

## 数据流

HBuilderX 从 `manifest.json` 的 `mp-weixin.appid` 生成微信小程序项目。客户端调用微信登录取得临时 code 后，当前 `dev` 后端通过 `wx.miniapp.appid` 和 `wx.miniapp.secret` 向微信换取会话。两端 AppID 必须一致。

## 验证

- 解析 `manifest.json`，确认 JSON 结构有效。
- 解析 `application-dev.yaml`，确认 YAML 结构有效。
- 检查小程序客户端与 `dev` 后端 AppID 一致。
- 检查真实密钥未出现在 `UI/uniapp-mall`。
- 检查 Git diff 只包含批准的配置与本次设计/计划文档；不运行 Maven。

