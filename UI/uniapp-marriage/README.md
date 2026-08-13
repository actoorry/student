# 原始需求

1、这是项目的手机端，使用 uniapp 开发
2、最近项目把会员 member 模块修改成了 partner 模块
3、pay 模块的账户管理修改成了 accountant 模块
请把后端修改同步到小程序，罗列改造方案

# 修改方案

## 1. 后端变更映射

### 1.1 member → partner（会员→客商）

后端 partner 模块现有 app 端点：

| 旧 URL（member） | 新 URL（partner） |
|---|---|
| `/member/auth/login` | `/partner/auth/login` |
| `/member/auth/sms-login` | `/partner/auth/sms-login` |
| `/member/auth/send-sms-code` | `/partner/auth/send-sms-code` |
| `/member/auth/logout` | `/partner/auth/logout` |
| `/member/auth/refresh-token` | `/partner/auth/refresh-token` |
| `/member/auth/social-auth-redirect` | `/partner/auth/social-auth-redirect` |
| `/member/auth/social-login` | `/partner/auth/social-login` |
| `/member/auth/weixin-mini-app-login` | `/partner/auth/weixin-mini-login` |
| `/member/auth/create-weixin-jsapi-signature` | `/partner/auth/create-weixin-jsapi-signature` |

> 注：partner 模块目前无 app 端的 user/address/point/sign-in/social 控制器。如小程序仍需这些接口，需在 partner 模块新增 App 控制器或确认 admin 端接口可公用。

### 1.2 pay → accountant（支付→会计中心）

| 旧 URL（pay） | 新 URL（accountant） |
|---|---|
| `/pay/wallet/get` | `/accountant/account/get` |
| `/pay/wallet-transaction/page` | `/accountant/account-transaction/page` |
| `/pay/wallet-transaction/get-summary` | `/accountant/account-transaction/get-summary` |
| `/pay/wallet-recharge-package/list` | `/accountant/account-recharge-package/list` |
| `/pay/wallet-recharge/create` | `/accountant/account-recharge/create` |
| `/pay/wallet-recharge/page` | `/accountant/account-recharge/page` |
| `/pay/order/get` | `/accountant/order/get` |
| `/pay/order/submit` | `/accountant/order/submit` |
| `/pay/channel/get-enable-code-list` | `/accountant/channel/get-enable-code-list` |
| `/pay/transfer/sync` | `/accountant/transfer/sync` |

## 2. 前端改动清单

### 2.1 目录重命名

| 当前 | 改为 | 说明 |
|---|---|---|
| `sheep/api/member/` | `sheep/api/partner/` | member→partner |
| `sheep/api/pay/` | `sheep/api/accountant/` | pay→accountant |

### 2.2 API 文件 URL 修改

**`sheep/api/partner/auth.js`**（9 处）：
```
/member/auth/login              → /partner/auth/login
/member/auth/sms-login          → /partner/auth/sms-login
/member/auth/send-sms-code      → /partner/auth/send-sms-code
/member/auth/logout             → /partner/auth/logout
/member/auth/refresh-token      → /partner/auth/refresh-token
/member/auth/social-auth-redirect → /partner/auth/social-auth-redirect
/member/auth/social-login       → /partner/auth/social-login
/member/auth/weixin-mini-app-login → /partner/auth/weixin-mini-login
/member/auth/create-weixin-jsapi-signature → /partner/auth/create-weixin-jsapi-signature
```

**`sheep/api/partner/user.js`**（6 处）：
```
/member/user/get                → /partner/user/get（待确认后端接口）
/member/user/update             → /partner/user/update（待确认）
/member/user/update-mobile      → /partner/user/update-mobile（待确认）
/member/user/update-password    → /partner/user/update-password（待确认）
/member/user/reset-password     → /partner/user/reset-password（待确认）
```

**`sheep/api/partner/address.js`**（5 处）：
```
/member/address/* → /partner/address/*（待确认后端接口）
```

**`sheep/api/partner/point.js`**（1处）：
```
/member/point/record/page → /partner/point/record/page（待确认后端接口）
```

**`sheep/api/partner/signin.js`**（4处）：
```
/member/sign-in/* → /partner/sign-in/*（待确认后端接口）
```

**`sheep/api/partner/social.js`**（1处）：
```
/member/social-user/get → /partner/social-user/get（待确认后端接口）
```

**`sheep/api/accountant/wallet.js`**（6 处）：
```
/pay/wallet/get                  → /accountant/account/get
/pay/wallet-transaction/page     → /accountant/account-transaction/page
/pay/wallet-transaction/get-summary → /accountant/account-transaction/get-summary
/pay/wallet-recharge-package/list → /accountant/account-recharge-package/list
/pay/wallet-recharge/create      → /accountant/account-recharge/create
/pay/wallet-recharge/page        → /accountant/account-recharge/page
```

**`sheep/api/accountant/order.js`**（2 处）：
```
/pay/order/get   → /accountant/order/get
/pay/order/submit → /accountant/order/submit
```

**`sheep/api/accountant/channel.js`**（1 处）：
```
/pay/channel/get-enable-code-list → /accountant/channel/get-enable-code-list
```

**`sheep/api/accountant/transfer.js`**（1 处）：
```
/pay/transfer/sync → /accountant/transfer/sync
```

### 2.3 拦截器硬编码修复

**`sheep/request/index.js`**（2 处）：
```js
// 自动登录检测（约第115行）
response.config.url.indexOf('/member/auth/')  → '/partner/auth/'

// 刷新token排除（约第227行）
config.url.indexOf('/member/auth/refresh-token')  → '/partner/auth/refresh-token'
```

### 2.4 import 路径更新（21 个文件）

`@/sheep/api/member/*` → `@/sheep/api/partner/*`，`@/sheep/api/pay/*` → `@/sheep/api/accountant/*`：

| 文件 | 旧 import | 新 import |
|---|---|---|
| `sheep/store/user.js` | `member/user`，`pay/wallet` | `partner/user`，`accountant/wallet` |
| `sheep/request/index.js` | `member/auth` | `partner/auth` |
| `sheep/hooks/useModal.js` | `member/auth` | `partner/auth` |
| `sheep/libs/sdk-h5-weixin.js` | `member/auth` | `partner/auth` |
| `sheep/platform/pay.js` | `pay/order` | `accountant/order` |
| `sheep/platform/provider/wechat/openPlatform.js` | `member/social` | `partner/social` |
| `sheep/platform/provider/wechat/officialAccount.js` | `member/auth`, `social` | `partner/auth`, `social` |
| `sheep/platform/provider/wechat/miniProgram.js` | `member/auth`, `social`, `user` | `partner/auth`, `social`, `user` |
| `sheep/platform/provider/alipay/miniProgram.js` | `member/auth`, `social`, `user` | `partner/auth`, `social`, `user` |
| `pages/user/info.vue` | `member/user` | `partner/user` |
| `pages/user/address/list.vue` | `member/address` | `partner/address` |
| `pages/user/address/edit.vue` | `member/address` | `partner/address` |
| `pages/user/account/score.vue` | `member/point` | `partner/point` |
| `pages/public/setting.vue` | `member/auth` | `partner/auth` |
| `pages/app/sign.vue` | `member/signin` | `partner/signin` |
| `pages/order/detail.vue` | `pay/order` | `accountant/order` |
| `pages/commission/wallet.vue` | `pay/transfer` | `accountant/transfer` |
| `sheep/components/s-auth-modal/components/*.vue`（6个） | `member/*` | `partner/*` |

### 2.5 pages.json 修复

```json
// "root": "pages/pay"  → "root": "pages/accountant"
```

### 2.6 页面路由引用更新

`pages/pay/` → `pages/accountant/` 出现在以下文件：
- `pages/accountant/index.vue`
- `pages/accountant/recharge.vue`
- `pages/accountant/result.vue`
- `pages/order/list.vue`
- `pages/order/detail.vue`
- `pages/order/confirm.vue`
- `sheep/platform/pay.js`

## 3. 执行顺序

1. 重命名目录 `sheep/api/member/` → `sheep/api/partner/`，`sheep/api/pay/` → `sheep/api/accountant/`
2. 修改 `pages.json` 子包 root
3. 修改 10 个 API 文件的 URL 字符串
4. 修改 21 个文件的 import 路径
5. 修改 `sheep/request/index.js` 拦截器
6. 修改 7 个文件的路由引用
7. 确认 partner 模块是否需要补充 app 控制器（user/address/point/sign-in/social）
8. 小程序真机测试登录、支付、充值流程

## 4. 待确认项

| 问题 | 状态 |
|---|---|
| partner 模块是否需要新增 `/partner/user/*` app 控制器 | 需确认 |
| partner 模块是否需要新增 `/partner/address/*` app 控制器 | 需确认 |
| partner 模块是否需要新增 `/partner/point/*` app 控制器 | 需确认 |
| partner 模块是否需要新增 `/partner/sign-in/*` app 控制器 | 需确认 |
| partner 模块是否需要新增 `/partner/social-user/*` app 控制器 | 需确认 |
