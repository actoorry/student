# uniapp-mall 微信小程序配置迁移实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `UI/uniapp-mall` 所有微信相关配置统一切换为婚恋项目使用的微信 AppID `wxc6e16a1b05f5ddaa`，并验证配置无旧 AppID 残留。

**Architecture:** 仅修改商城现有 `manifest.json`，不整体覆盖配置文件。分别更新微信小程序 AppID，以及 App 端微信 OAuth、支付、分享配置中的 AppID，保留各节点其他商城配置不变。

**Tech Stack:** uni-app、Vue 3、JSON/JSONC、微信小程序构建配置、Git。

## Global Constraints

- 目标文件：`UI/uniapp-mall/manifest.json`。
- 参考微信 AppID：`wxc6e16a1b05f5ddaa`。
- 旧 AppID `wx32b92b22d15a2584` 和 `wxae7a0c156da9383b` 不得继续出现在目标项目配置中。
- 不覆盖商城现有 `manifest.json`，不得删除或改动与 AppID 无关的平台配置。
- 不修改微信开发者平台、后端密钥、支付商户配置或数据库。
- 按项目规范不运行 Maven 编译；使用前端配置级检查。

---

### Task 1: 统一商城微信 AppID 配置

**Files:**
- Modify: `UI/uniapp-mall/manifest.json:96-120`（App 端 OAuth、支付、分享配置）
- Modify: `UI/uniapp-mall/manifest.json:183-185`（微信小程序配置）

**Interfaces:**
- Consumes: 当前商城 `manifest.json` 中的四个微信 AppID 字段。
- Produces: 四个字段统一返回/使用 `wxc6e16a1b05f5ddaa`，其余配置保持原值。

- [ ] **Step 1: 确认待修改字段和值**

在 `UI/uniapp-mall/manifest.json` 中确认以下四个字段存在：

```text
app-plus.distribute.sdkConfigs.oauth.weixin.appid
app-plus.distribute.sdkConfigs.payment.weixin.appid
app-plus.distribute.sdkConfigs.share.weixin.appid
mp-weixin.appid
```

当前它们分别使用商城旧值，修改目标均为：

```text
wxc6e16a1b05f5ddaa
```

- [ ] **Step 2: 替换四个微信 AppID**

将以下配置统一修改为：

```jsonc
"oauth": {
  "apple": {},
  "weixin": {
    "appid": "wxc6e16a1b05f5ddaa",
    "UniversalLinks": "https://shopro.sheepjs.com/uni-universallinks/__UNI__082C0BA/"
  }
},
"payment": {
  "weixin": {
    "__platform__": [
      "ios",
      "android"
    ],
    "appid": "wxc6e16a1b05f5ddaa",
    "UniversalLinks": "https://shopro.sheepjs.com/uni-universallinks/__UNI__082C0BA/"
  }
},
"share": {
  "weixin": {
    "appid": "wxc6e16a1b05f5ddaa",
    "UniversalLinks": "https://shopro.sheepjs.com/uni-universallinks/__UNI__082C0BA/"
  }
}
```

并将小程序节点保留其他商城配置，仅修改 AppID：

```jsonc
"mp-weixin": {
  "appid": "wxc6e16a1b05f5ddaa",
  "setting": {
    "urlCheck": false,
    "minified": true,
    "postcss": true
  },
  "optimization": {
    "subPackages": true
  },
  "plugins": {},
  "lazyCodeLoading": "requiredComponents",
  "usingComponents": {},
  "permission": {},
  "requiredPrivateInfos": [
    "chooseAddress"
  ]
}
```

- [ ] **Step 3: 检查修改范围**

运行：

```bash
git diff -- UI/uniapp-mall/manifest.json
```

预期：diff 只显示微信 AppID 字符串由旧值改为 `wxc6e16a1b05f5ddaa`，不出现其他配置删除或重排造成的大范围变更。

- [ ] **Step 4: 检查旧值和新值残留**

运行：

```bash
rg -n 'wx32b92b22d15a2584|wxae7a0c156da9383b|wxc6e16a1b05f5ddaa' UI/uniapp-mall --glob '!node_modules/**' --glob '!unpackage/**'
```

预期：

- 旧值 `wx32b92b22d15a2584` 无匹配。
- 旧值 `wxae7a0c156da9383b` 无匹配。
- 新值 `wxc6e16a1b05f5ddaa` 出现 4 处，分别对应 OAuth、支付、分享和小程序配置。

- [ ] **Step 5: 验证 JSON/JSONC 配置结构**

由于 `manifest.json` 可能包含 uni-app 支持的注释，先使用 Node 清除 JSONC 注释后解析：

```bash
node -e "const fs=require('fs'); const p='UI/uniapp-mall/manifest.json'; const s=fs.readFileSync(p,'utf8').replace(/\/\/.*$/gm,'').replace(/\/\*[\s\S]*?\*\//g,''); JSON.parse(s); console.log('manifest.json structure: OK')"
```

预期输出：

```text
manifest.json structure: OK
```

- [ ] **Step 6: 检查 Git 状态**

运行：

```bash
git status --short -- UI/uniapp-mall/manifest.json
```

预期只显示：

```text
 M UI/uniapp-mall/manifest.json
```

不要提交或覆盖用户在其他文件中的既有修改。

- [ ] **Step 7: 提交配置修改**

运行：

```bash
git add -- UI/uniapp-mall/manifest.json
git commit -m "chore: unify uniapp mall wechat appid"
```

预期：创建一个仅包含 `UI/uniapp-mall/manifest.json` 的提交。

---

## 最终验证清单

- [ ] `manifest.json` 中 App 端微信 OAuth AppID 为 `wxc6e16a1b05f5ddaa`。
- [ ] `manifest.json` 中 App 端微信支付 AppID 为 `wxc6e16a1b05f5ddaa`。
- [ ] `manifest.json` 中 App 端微信分享 AppID 为 `wxc6e16a1b05f5ddaa`。
- [ ] `manifest.json` 中微信小程序 AppID 为 `wxc6e16a1b05f5ddaa`。
- [ ] 旧 AppID 在 `UI/uniapp-mall` 配置中无残留。
- [ ] 配置结构解析成功。
- [ ] 没有运行 Maven 编译，也没有修改无关文件。
