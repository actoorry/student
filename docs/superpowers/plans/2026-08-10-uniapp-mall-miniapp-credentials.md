# uniapp-mall Miniapp Credentials Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore one consistent WeChat mini program identity for the `uniapp-mall` client and the active `dev` backend.

**Architecture:** The client contains only the public mini program AppID in `mp-weixin.appid`. The active backend `wx.miniapp` configuration contains the matching AppID and the user-provided server-side secret; unrelated App OAuth, payment, and sharing identities remain unchanged.

**Tech Stack:** uni-app manifest JSON, Spring Boot YAML, Node.js configuration checks, Git.

## Global Constraints

- Modify only the mini program identity fields approved by the user.
- Never place the mini program secret in `UI/uniapp-mall`.
- Do not modify App OAuth, payment, or sharing AppIDs.
- Do not modify production or personal backend profiles.
- This is a configuration-only change; the user approved configuration-level verification instead of TDD.
- Do not commit because the tracked development YAML will contain a real secret.

---

### Task 1: Restore the mini program client identity

**Files:**
- Modify: `UI/uniapp-mall/manifest.json:185`

**Interfaces:**
- Consumes: HBuilderX `mp-weixin` manifest configuration.
- Produces: A generated WeChat project associated with the approved mini program AppID.

- [ ] **Step 1: Replace only `mp-weixin.appid`**

Set the field to the user-approved AppID while leaving `app-plus.distribute.sdkConfigs` unchanged.

- [ ] **Step 2: Parse the manifest**

Run:

```powershell
node -e "const fs=require('fs'); JSON.parse(fs.readFileSync('UI/uniapp-mall/manifest.json','utf8')); console.log('manifest.json: OK')"
```

Expected: `manifest.json: OK`.

### Task 2: Restore the active development backend credentials

**Files:**
- Modify: `suxin-server/src/main/resources/application-dev.yaml:198`
- Modify: `suxin-server/src/main/resources/application-dev.yaml:199`

**Interfaces:**
- Consumes: The mini program AppID and secret used by WxJava.
- Produces: Matching credentials for the backend WeChat session exchange.

- [ ] **Step 1: Replace the `wx.miniapp` pair**

Set `wx.miniapp.appid` to the same AppID as the client and set `wx.miniapp.secret` to the exact value supplied by the user. Do not change the `wx.mp` official-account pair.

- [ ] **Step 2: Parse the YAML**

Run the repository's available YAML parser against `application-dev.yaml` and require a zero exit code.

### Task 3: Verify scope and credential placement

**Files:**
- Verify: `UI/uniapp-mall/manifest.json`
- Verify: `suxin-server/src/main/resources/application-dev.yaml`

**Interfaces:**
- Consumes: Tasks 1 and 2 configuration output.
- Produces: Evidence that client/backend AppIDs match and the secret remains server-side.

- [ ] **Step 1: Search the approved AppID**

Run a targeted `rg` search in the two modified configuration files. Expected: one `mp-weixin` client occurrence and one `wx.miniapp` backend occurrence.

- [ ] **Step 2: Check the secret boundary**

Search `UI/uniapp-mall` for the supplied secret while excluding generated and dependency directories. Expected: no matches.

- [ ] **Step 3: Review Git diff**

Run:

```powershell
git diff -- UI/uniapp-mall/manifest.json suxin-server/src/main/resources/application-dev.yaml
```

Expected: exactly three value changes: client AppID, backend AppID, and backend secret.

- [ ] **Step 4: Check repository status**

Run `git status --short` and preserve all unrelated user changes. Do not stage or commit the credential-bearing YAML.

