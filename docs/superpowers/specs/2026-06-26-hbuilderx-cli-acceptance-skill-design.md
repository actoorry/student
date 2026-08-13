# HBuilderX CLI 验收 Skill 设计说明

**日期：** 2026-06-26  
**范围：** `.claude/skills/` 新增项目专用验收 skill  
**目标：** 创建一个基于 `D:\develop\javaproject\hbuilderx-cli` 的婚恋项目专用验收 skill，固化 `UI/uniapp-marriage` 的验收流程，默认微信小程序优先、H5 兜底预检，输出统一验收报告。

## 1. 背景与动机

当前 `UI/uniapp-marriage` 项目的验收存在以下问题：

- 环境中没有全局可用的 `hbuilderx` / `uni` CLI
- 项目没有 `package.json`，无法直接用 Vite/npm 启动
- 验收流程依赖人工判断启动方式和验收范围
- 每次页面改动后需要手动重复相同的验证步骤

用户已拉取 `D:\develop\javaproject\hbuilderx-cli`，该 CLI 包含以下可用命令：

- `hbuilderx`
- `uni-launch`
- `uni-logcat`
- `uni-test`

本次设计目标是将这些 CLI 能力封装成一个可复用的 skill，使后续验收工作标准化、可重复。

## 2. 设计决策

### 2.1 skill 定位

这是一个 **项目专用验收 skill**，不是通用 uni-app 开发 skill。

它的职责是：

- 固定服务 `UI/uniapp-marriage`
- 固定使用 `D:\develop\javaproject\hbuilderx-cli`
- 固化验收流程和输出格式
- 不修改业务代码

### 2.2 运行目标策略

采用 **微信小程序优先、H5 兜底** 的策略：

- 默认尝试微信小程序 (`mp-weixin`) 验收
- 如果当前环境不方便拉起微信或不适合快速验证，降级到 H5 预检
- 降级时必须明确标注：**本次为 H5 预检，不等同于微信正式验收**

### 2.3 不采用的方案

- **只做 H5 验收**：不符合项目主运行环境
- **微信 + H5 双通道都跑**：效率低，且微信验证失败时再跑微信没意义
- **只做启动不做场景检查**：验收价值不足

## 3. skill 职责边界

### 3.1 负责

- 识别 `UI/uniapp-marriage` 项目上下文
- 查找并调用 `D:\develop\javaproject\hbuilderx-cli`
- 判定当前应该走微信还是 H5
- 驱动最小可观察验收路径
- 记录证据并输出统一报告
- 在无法正式验收时说明降级原因

### 3.2 不负责

- 修改业务代码
- 替代 code review
- 伪造"通过"
- 在运行链路缺失时猜测命令
- 执行单元测试或类型检查（CI 的职责）

## 4. skill 元信息

### 4.1 目录结构

```
.claude/skills/uniapp-marriage-acceptance/
  SKILL.md
```

### 4.2 name

`uniapp-marriage-acceptance`

### 4.3 description

Use when verifying or accepting UI changes in the uniapp-marriage project, including page navigation, component rendering, and route wiring. Triggers on acceptance requests for member pages, product pages, activity pages, matchmaker service pages, and mine-entry redirects.

## 5. 触发条件

skill 应在以下场景触发：

### 5.1 用户直接请求验收

- "验收一下 uniapp-marriage"
- "验收这个页面改动"
- "验证红娘服务页"
- "验证会员套餐页"
- "跑一下婚恋项目小程序验收"
- "accept the changes"

### 5.2 任务明确涉及 uniapp-marriage

当任务涉及以下内容时也应触发：

- `UI/uniapp-marriage` 中的文件修改
- 页面跳转和路由变更
- 商品专题页改动
- 小程序端页面展示
- 页面链路验证

## 6. 输入格式

skill 支持两种输入层级：

### 6.1 简短输入

例如：

- `红娘服务页面改动验收`
- `会员套餐页验收`
- `我的页面入口跳转验收`

skill 需要从 diff 和文件上下文中推断主要改动和验收链路。

### 6.2 明确输入

例如：

- `验证 UI/uniapp-marriage/pages/matchmaker-service/index.uvue`
- `验收"我的"页到红娘服务页到商品详情页链路`

skill 直接围绕指定目标执行。

## 7. 默认执行流程

### 7.1 确认验收范围

1. 看用户指定目标
2. 如果未指定，检查 git diff
3. 输出本次验收 claim（一句描述）

### 7.2 确认运行能力

1. 检查 `D:\develop\javaproject\hbuilderx-cli` 是否可用
2. 判断是否能走微信小程序链路
3. 若不能，降级到 H5 预检
4. 降级时明确标注

### 7.3 启动项目

1. 优先尝试 `mp-weixin`
2. 不可用时走 H5
3. 记录启动命令、结果和 blocker

### 7.4 执行场景化验收

根据页面类型选择检查模板（见第 8 节）

### 7.5 输出统一报告

固定格式：

```
## Verification: <一句话描述>

**Verdict:** PASS | FAIL | BLOCKED | SKIP | H5-only

**Claim:** <验收声明>

**Method:** <启动方式，是否走了 H5 降级>

### Steps
<逐步观察记录>

### Findings
<发现和注意事项>
```

## 8. 页面场景验收模板

### 8.1 专题商品页模板

适用页面：情感学院、线下活动、红娘服务、会员套餐

验收步骤：

1. 页面入口是否可达
2. 页面标题和副标题是否正确显示
3. 加载态是否可见
4. 空态/失败态/未配置态是否合理
5. 商品卡片是否正确展示
6. 点击商品是否进入商品详情页
7. 返回按钮行为是否正常

### 8.2 mine 入口跳转模板

验收步骤：

1. "我的"页面对应入口是否存在
2. 点击入口跳转是否正确
3. 是否弹出旧的占位 toast

### 8.3 商品详情链路模板

验收步骤：

1. 从专题页点击商品是否能跳转到商品详情
2. 商品详情页是否正确加载
3. 返回是否能回到专题页

### 8.4 空态/失败态/未配置态模板

验收步骤：

1. 配置未设置时是否显示对应提示
2. 分类为空时是否显示空态
3. 接口失败时是否显示失败态
4. 重试按钮是否可用

## 9. 降级策略

### 9.1 微信小程序优先

默认先尝试微信小程序验收。

### 9.2 H5 兜底

如果微信链路无法完成，降级到 H5 预检。

降级条件：

- HBuilderX CLI 无法拉起微信开发者工具
- 当前环境不支持小程序运行
- 用户明确允许走 H5 预检

### 9.3 降级标注

每次走 H5 降级时，报告中必须包含：

> **注意：本次为 H5 预检，不等同于微信正式验收。**

### 9.4 BLOCKED

当 H5 和微信都无法执行时，报告为 BLOCKED，并说明阻塞原因。

## 10. 约束与注意事项

- 仅修改 `.claude/skills/uniapp-marriage-acceptance/` 下的文件
- 不修改 `UI/uniapp-marriage` 业务代码
- 不伪造验证结果
- 不在环境无法支持时猜测命令
- 报告必须基于实际运行观察，不能基于代码阅读推测
