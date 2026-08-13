# uniapp-mall 微信小程序配置迁移设计

- 日期：2026-07-21
- 范围：`UI/uniapp-mall`
- 参考项目：`UI/uniapp-marriage_backup1_7-16`

## 目标

将 `uniapp-mall` 完整复用婚恋项目的微信应用标识，统一使用微信 AppID `wxc6e16a1b05f5ddaa`，同时保留商城自身的平台能力和业务配置。

## 方案

采用“直接修改现有配置”方案，不整体覆盖商城配置文件：

1. 修改 `UI/uniapp-mall/manifest.json` 中所有微信相关 AppID：
   - `mp-weixin.appid`
   - `app-plus.distribute.sdkConfigs.oauth.weixin.appid`
   - `app-plus.distribute.sdkConfigs.payment.weixin.appid`
   - `app-plus.distribute.sdkConfigs.share.weixin.appid`
2. 保留上述节点中的其他商城配置，包括 Universal Links、分包、组件、私有接口、H5 和其他平台设置。
3. 不覆盖商城现有 `manifest.json`，也不复制婚恋项目中与本次需求无关的配置。
4. 对目标项目中的微信配置进行残留检查，确认旧 AppID 不再被使用。

## 数据流

构建工具从 `manifest.json` 读取各平台配置：微信小程序构建读取 `mp-weixin.appid`，App 端微信 OAuth、支付和分享分别读取对应 `sdkConfigs` 节点。四处配置统一指向同一微信应用，避免登录、支付、分享使用不同应用标识。

## 校验与错误处理

- 使用文本搜索确认旧 AppID `wx32b92b22d15a2584` 和 `wxae7a0c156da9383b` 在 `UI/uniapp-mall` 配置中不再出现。
- 通过 JSON/JSONC 解析或格式检查确认 `manifest.json` 结构未被破坏。
- 不运行 Maven 编译；本次只涉及前端配置，按项目规范进行配置级验证。
- 不修改微信开发者平台、后端密钥或支付商户配置；这些需要在对应平台/服务端另行配置。

## 验收标准

- `manifest.json` 的四个微信 AppID 均为 `wxc6e16a1b05f5ddaa`。
- 商城现有其他配置保持不变。
- 目标目录配置中无旧微信 AppID 残留。
- Git diff 仅包含本次配置迁移相关修改。
