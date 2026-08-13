# Local API Base URL Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make development builds send API requests to `http://localhost:1930/app-api`.

**Architecture:** Keep the existing separation between the server origin and API path. Change only the development origin in `.env`; `sheep/config/index.js` and `sheep/request/index.js` continue composing `SHOPRO_DEV_BASE_URL + SHOPRO_API_PATH`.

**Tech Stack:** uni-app, Vue 3, Vite environment variables, PowerShell verification

## Global Constraints

- The final development API prefix must be exactly `http://localhost:1930/app-api`.
- Production, trial, and H5 URLs remain `https://marriage.aderp.cn`.
- `SHOPRO_API_PATH` remains `/app-api`.
- Tenant ID remains `303`.
- Do not modify `D:\develop\front\uniapp-marriage-1`.
- Preserve all unrelated working-tree changes.

---

### Task 1: Point development requests at the local backend

**Files:**
- Modify: `.env:11`
- Verify: `sheep/config/index.js`
- Verify: `sheep/request/index.js`

**Interfaces:**
- Consumes: Vite variables `SHOPRO_DEV_BASE_URL` and `SHOPRO_API_PATH`.
- Produces: Development request base URL `http://localhost:1930/app-api`.

- [ ] **Step 1: Run the pre-change assertion and verify it fails**

Run from `D:\develop\javaproject\suxin1\UI\uniapp-marriage`:

```powershell
$values = @{}
Get-Content .env | Where-Object { $_ -match '^[A-Z0-9_]+=' } | ForEach-Object {
  $key, $value = $_ -split '=', 2
  $values[$key] = $value
}
if (($values['SHOPRO_DEV_BASE_URL'] + $values['SHOPRO_API_PATH']) -ne 'http://localhost:1930/app-api') { exit 1 }
```

Expected: exit code `1`, because the current development URL is the production host.

- [ ] **Step 2: Apply the minimal configuration change**

Change `.env` from:

```dotenv
SHOPRO_DEV_BASE_URL=https://marriage.aderp.cn
```

to:

```dotenv
SHOPRO_DEV_BASE_URL=http://localhost:1930
```

- [ ] **Step 3: Run the post-change assertion**

Run:

```powershell
$values = @{}
Get-Content .env | Where-Object { $_ -match '^[A-Z0-9_]+=' } | ForEach-Object {
  $key, $value = $_ -split '=', 2
  $values[$key] = $value
}
if (($values['SHOPRO_DEV_BASE_URL'] + $values['SHOPRO_API_PATH']) -ne 'http://localhost:1930/app-api') { exit 1 }
if ($values['SHOPRO_BASE_URL'] -ne 'https://marriage.aderp.cn') { exit 1 }
if ($values['SHOPRO_TRIAL_BASE_URL'] -ne 'https://marriage.aderp.cn') { exit 1 }
if ($values['SHOPRO_H5_URL'] -ne 'https://marriage.aderp.cn') { exit 1 }
if ($values['SHOPRO_TENANT_ID'] -ne '303') { exit 1 }
```

Expected: exit code `0`.

- [ ] **Step 4: Review the scoped diff**

Run:

```powershell
git diff -- .env
```

Expected: only `SHOPRO_DEV_BASE_URL` changes from `https://marriage.aderp.cn` to `http://localhost:1930`.

- [ ] **Step 5: Commit only the configuration file if the user requests a commit**

```powershell
git add -- .env
git commit -m "chore: use local API for development"
```
