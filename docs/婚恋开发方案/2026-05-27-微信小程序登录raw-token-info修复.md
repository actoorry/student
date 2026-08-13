# 微信小程序登录 raw_token_info 报错修复

## 问题现象

微信小程序调用 `/app-api/marriage/auth/wechat-mini-login` 时，后端写入 `system_social_user` 失败：

`Field 'raw_token_info' doesn't have a default value`

## 原因分析

婚恋模块的微信小程序登录在 `MarriageAuthServiceImpl` 中自行构造 `SocialUserDO` 并插入 `system_social_user`。

当前实现只写入了：

- `type`
- `openid`
- `nickname`
- `avatar`
- `raw_user_info`
- `code`
- `state`

但数据库表 `system_social_user` 中 `raw_token_info` 为非空且无默认值，因此插入时报错。

另外，这条链路没有复用系统 `SocialUserServiceImpl` 的社交登录落库方式，导致 `token/rawTokenInfo` 字段缺失。

## 修复内容

修改文件：

- `suxin-module-system/src/main/java/cn/iocoder/suxin/module/marriage/service/MarriageAuthServiceImpl.java`

修复点：

1. `wxMiniLogin` 将 `WxMaJscode2SessionResult` 传入 `upsertSocialUser`
2. 新增 `rawTokenInfo` 持久化，内容为 `sessionResult` 的 JSON
3. 将 `token` 赋值为 `sessionKey`
4. `rawUserInfo` 改为统一 JSON 序列化，不再手写拼接字符串
5. 更新已有社交用户时，同步刷新 `token/rawTokenInfo/rawUserInfo`

## 验证结果

执行编译命令：

```bash
mvn compile -pl suxin-module-system -am -DskipTests
```

结果：

- 编译通过
- 本次修改未引入新的编译错误

## 结论

本次修复后，微信小程序首次登录写入 `system_social_user` 时会补齐 `raw_token_info`，可以消除当前数据库插入异常。
