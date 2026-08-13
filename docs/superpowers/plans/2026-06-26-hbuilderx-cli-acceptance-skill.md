# HBuilderX CLI 验收 Skill Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 创建一个婚恋项目专用验收 skill，基于 `D:\develop\javaproject\hbuilderx-cli`，固化 `UI/uniapp-marriage` 的验收流程，微信小程序优先、H5 兜底，输出统一验收报告。

**Architecture:** 在 `.claude/skills/uniapp-marriage-acceptance/` 下创建一个自包含的 SKILL.md 文件，包含触发条件、执行流程、降级策略、场景验收模板和报告格式。skill 本身是纯文档，不包含可执行代码，但会引用 `hbuilderx-cli` 的实际 CLI 命令作为验收手段。

**Tech Stack:** Markdown skill 文档、`@dcloudio/hbuilderx-cli`、uni-app 项目结构

## Global Constraints

- skill 仅修改 `.claude/skills/uniapp-marriage-acceptance/` 目录
- 不修改 `UI/uniapp-marriage` 业务代码
- 默认验收目标为微信小程序 (`mp-weixin`)，H5 作为兜底预检
- H5 降级时必须明确标注"不等同于微信正式验收"
- 报告必须基于实际运行观察，不能基于代码阅读推测
- skill 不能伪造验证结果

---

## File Structure

- Create: `.claude/skills/uniapp-marriage-acceptance/SKILL.md`
  - 婚恋项目专用验收 skill 的主文档，包含触发条件、流程、模板和报告格式

## Task 1: Create the skill directory and SKILL.md

**Files:**
- Create: `.claude/skills/uniapp-marriage-acceptance/SKILL.md`

**Interfaces:**
- Consumes: spec from `docs/superpowers/specs/2026-06-26-hbuilderx-cli-acceptance-skill-design.md`
- Produces: a routable skill that agents can discover via description matching

- [ ] **Step 1: Create the skill directory**

```bash
mkdir -p .claude/skills/uniapp-marriage-acceptance
```

- [ ] **Step 2: Write the SKILL.md**

```markdown
---
name: uniapp-marriage-acceptance
description: Use when verifying or accepting UI changes in the uniapp-marriage project, including page navigation, component rendering, and route wiring. Triggers on acceptance requests for member pages, product pages, activity pages, matchmaker service pages, and mine-entry redirects.
---

# uniapp-marriage-acceptance

## Overview

婚恋项目专用验收 skill。基于 `D:\develop\javaproject\hbuilderx-cli`，固化 `UI/uniapp-marriage` 的验收流程。默认微信小程序优先，H5 兜底预检，输出统一验收报告。

## When to Use

When the user asks to verify, accept, or validate changes in `UI/uniapp-marriage`. Typical triggers:

- "验收一下 uniapp-marriage"
- "验收这个页面改动"
- "验证红娘服务页"
- "验证会员套餐页"
- "跑一下婚恋项目小程序验收"

Also trigger when the task involves:

- `UI/uniapp-marriage` file modifications
- Page navigation and route changes
- Product showcase pages
- Mini-program page rendering
- Page flow verification

## Core Principle

**WeChat Mini-Program first, H5 fallback.** Never fake a pass. If the environment cannot run the target, say so explicitly.

## Execution Flow

### Step 1: Determine Scope

1. Read the user's request for a target page or flow
2. If no target specified, run `git diff --name-only -- UI/uniapp-marriage/` to find changed files
3. State the verification claim in one sentence

### Step 2: Check Runtime Capability

1. Verify `D:\develop\javaproject\hbuilderx-cli` exists
2. Check if HBuilderX is running: `hbuilderx --version`
3. If WeChat toolchain is available, proceed with `mp-weixin`
4. If not, fall back to H5 with explicit note

### Step 3: Launch Project

**WeChat (preferred):**
```bash
uni-launch mp-weixin --project D:/develop/javaproject/suxin1/UI/uniapp-marriage
```

**H5 fallback:**
```bash
uni-launch web --project D:/develop/javaproject/suxin1/UI/uniapp-marriage
```

If neither works, report BLOCKED with the exact error.

### Step 4: Execute Scene-Based Verification

Pick the appropriate template from the Scene Templates section below based on the changed page type.

### Step 5: Output Report

Use the fixed report format from the Report Format section below.

## Scene Templates

### Template A: Product Showcase Page

For: 情感学院, 线下活动, 红娘服务, 会员套餐

1. Is the page entry reachable?
2. Does the page title and subtitle render correctly?
3. Is the loading state visible?
4. Are empty/error/unconfigured states reasonable?
5. Do product cards display correctly?
6. Does tapping a product navigate to product detail?
7. Does the back button work?

### Template B: Mine Entry Redirect

1. Does the target entry exist on the "我的" page?
2. Does tapping it navigate to the correct page?
3. Does it still show the old placeholder toast?

### Template C: Product Detail Flow

1. Does tapping a product from the showcase page reach product detail?
2. Does the product detail page load correctly?
3. Can the user return to the showcase page?

### Template D: Empty/Error State

1. Does missing config show the correct prompt?
2. Does empty category show the empty state?
3. Does API failure show the error state?
4. Is the retry button functional?

##降级 Strategy

| Condition | Action |
|-----------|--------|
| WeChat toolchain available | Run `mp-weixin` |
| WeChat unavailable, H5 possible | Run H5 with **H5-only** label |
| Neither available | Report **BLOCKED** with reason |

**H5降级 mandatory note:**

> **注意：本次为 H5 预检，不等同于微信正式验收。**

## Report Format

```markdown
## Verification: <one-line description>

**Verdict:** PASS | FAIL | BLOCKED | SKIP | H5-only

**Claim:** <what was supposed to work>

**Method:** <how you launched — mp-weixin or H5 fallback>

### Steps

1. ✅/❌/⚠️/🔍 <what you did> → <what you observed>

### Findings

- <things noticed during verification>
```

## Common Mistakes

- Faking a PASS based on code reading instead of running the app
- Not labeling H5-only runs clearly
- Skipping the scene template and doing ad-hoc checks
- Forgetting to check the mine entry redirect after page changes

## Quick Reference

| Command | Purpose |
|---------|---------|
| `hbuilderx --version` | Check HBuilderX availability |
| `uni-launch mp-weixin --project <path>` | Launch WeChat dev |
| `uni-launch web --project <path>` | Launch H5 fallback |
| `uni-logcat mp-weixin --project <path>` | View WeChat logs |
```

- [ ] **Step 3: Verify the skill is discoverable**

Run: `rg -n "uniapp-marriage-acceptance|description: Use when" .claude/skills/uniapp-marriage-acceptance/SKILL.md`

Expected:
- One match for `uniapp-marriage-acceptance`
- One match for `description: Use when`

- [ ] **Step 4: Commit**

```bash
git add .claude/skills/uniapp-marriage-acceptance/SKILL.md
git commit -m "feat: add uniapp-marriage-acceptance skill"
```

## Self-Review

- **Spec coverage:** skill 触发条件、执行流程、降级策略、场景模板、报告格式均已覆盖
- **Placeholder scan:** 无 TBD/TODO，所有步骤包含具体命令和代码块
- **Type consistency:** skill name、CLI 路径、报告格式在各节保持一致
