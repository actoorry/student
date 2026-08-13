# Distributor Center Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rework the uni-app distributor center and related distributor pages to match the provided reference layouts while keeping existing brokerage APIs wired up.

**Architecture:** Keep all business logic in `UI/uniapp-marriage` and treat the reference pages in `UI/uniapp-mall` as visual/interaction templates. The distributor center becomes the entry hub, with feature pages split by responsibility: summary/home, order list, withdrawal flow, withdraw history, team list, commission detail, rankings, and product promotion. Existing API wrappers in `utils/brokerage-api.uts`, the poster component, and the login sheet remain the integration points; page work is mostly layout, navigation, and data binding.

**Tech Stack:** uni-app (`<script setup lang="uts">` / `uvue`), existing `brokerage-api.uts`, existing `member-login-sheet.uvue`, existing `brokerage-poster.uvue`, `pages.json` routes, `uni.navigateTo` / `uni.navigateBack`, existing `request` wrapper.

## Global Constraints

- Keep all business code under `UI/uniapp-marriage`.
- Do not add a new Maven module.
- Do not change Java backend behavior in this task.
- Do not run Maven compile unless explicitly requested later.
- Use existing brokerage APIs where available; do not invent new backend endpoints.
- Preserve login gating and invitation binding behavior.
- Prefer reference-driven UI parity with the supplied images and `UI/uniapp-mall` commission pages.

---

### Task 1: Map mall reference pages to marriage routes

**Files:**
- Modify: `UI/uniapp-marriage/pages.json`
- Review only: `UI/uniapp-mall/pages/commission/index.vue`
- Review only: `UI/uniapp-mall/pages/commission/order.vue`
- Review only: `UI/uniapp-mall/pages/commission/wallet.vue`
- Review only: `UI/uniapp-mall/pages/commission/withdraw.vue`
- Review only: `UI/uniapp-mall/pages/commission/team.vue`
- Review only: `UI/uniapp-mall/pages/commission/promoter.vue`
- Review only: `UI/uniapp-mall/pages/commission/commission-ranking.vue`
- Review only: `UI/uniapp-mall/pages/commission/goods.vue`

**Interfaces:**
- Consumes: existing distributor routes and mall reference page layouts
- Produces: finalized route list for all distributor feature pages

- [ ] **Step 1: Add missing distributor routes**

  Ensure `pages.json` includes routes for the distributor hub and every feature page used by the new function grid:

  ```json
  {
    "path": "pages/distributor/index",
    "style": { "navigationStyle": "custom" }
  },
  {
    "path": "pages/distributor/order/index",
    "style": { "navigationStyle": "custom" }
  },
  {
    "path": "pages/distributor/withdraw/index",
    "style": { "navigationStyle": "custom" }
  },
  {
    "path": "pages/distributor/withdraw-record/index",
    "style": { "navigationStyle": "custom" }
  },
  {
    "path": "pages/distributor/team/index",
    "style": { "navigationStyle": "custom" }
  },
  {
    "path": "pages/distributor/wallet/index",
    "style": { "navigationStyle": "custom" }
  },
  {
    "path": "pages/distributor/promoter-rank/index",
    "style": { "navigationStyle": "custom" }
  },
  {
    "path": "pages/distributor/commission-rank/index",
    "style": { "navigationStyle": "custom" }
  },
  {
    "path": "pages/distributor/goods/index",
    "style": { "navigationStyle": "custom" }
  }
  ```

- [ ] **Step 2: Verify mall page responsibilities**

  Use the mall pages to map the feature responsibilities:
  - `commission/index.vue` → distributor center home
  - `commission/order.vue` → order list
  - `commission/wallet.vue` → commission detail / wallet
  - `commission/withdraw.vue` → withdrawal application
  - `commission/team.vue` → my team
  - `commission/promoter.vue` → promoter rank
  - `commission/commission-ranking.vue` → commission rank
  - `commission/goods.vue` → promotion goods

- [ ] **Step 3: Keep navigation assumptions consistent**

  Use `uni.navigateTo({ url: '/pages/distributor/.../index' })` for feature tiles so the grid can stay dumb and reusable.

---

### Task 2: Rebuild the distributor hub to match the reference home page

**Files:**
- Modify: `UI/uniapp-marriage/pages/distributor/index.uvue`
- Modify: `UI/uniapp-marriage/utils/brokerage-api.uts` (only if a small helper is needed for display formatting)
- Modify: `UI/uniapp-marriage/components/brokerage-poster.uvue` (only if the poster entry flow needs a new trigger style)

**Interfaces:**
- Consumes: `getBrokerageUser()`, `getBrokerageSummary()`, `getOrderBrokerageRecordPage()`, `getTradeConfig()`, `formatBrokerageAmount()`, `formatBrokerageTime()`, `formatBrokerageStatus()`
- Produces: redesigned distributor hub with a function grid and recent activity section

- [ ] **Step 1: Replace the current layout with the reference structure**

  Restructure the page into:
  1. custom top bar
  2. hero/banner section with user name and decorative background
  3. account info card
  4. function grid
  5. realtime activity card

- [ ] **Step 2: Bind the account summary to existing API data**

  Map the summary card to the current API fields:
  - 当前佣金 → `summary.brokeragePrice`
  - 昨天的佣金 → `summary.yesterdayPrice`
  - 累计已提 → `summary.withdrawPrice`

- [ ] **Step 3: Turn the function area into a real launcher grid**

  Add tiles for:
  - 我的团队
  - 佣金明细
  - 分销订单
  - 推广商品
  - 邀请海报
  - 推广排行
  - 佣金排行

  Wire each tile to a real route when the page exists; use the poster component for the poster tile.

- [ ] **Step 4: Keep realtime activity based on order brokerage records**

  Continue using `getOrderBrokerageRecordPage(1, N, null)` for the lower activity section, but style it as a “实时动态” feed instead of a separate order preview block.

- [ ] **Step 5: Preserve login and permission gating**

  Keep the existing login sheet and `brokerageEnabled` state. If the user is not logged in or lacks brokerage permission, show the same blocked-state cards instead of the normal content.

- [ ] **Step 6: Keep poster generation wired up**

  Preserve the existing poster flow:
  - `getTradeConfig()` supplies background images
  - `generateQrcode` remains the QR source
  - saving and sharing behavior stays intact

---

### Task 3: Restyle the distributor order page to match the red banner design

**Files:**
- Modify: `UI/uniapp-marriage/pages/distributor/order/index.uvue`

**Interfaces:**
- Consumes: `getBrokerageUser()`, `getOrderBrokerageRecordPage()`, `formatBrokerageAmount()`, `formatBrokerageTime()`, `formatBrokerageStatus()`
- Produces: tabbed order page with a red hero banner and order list

- [ ] **Step 1: Replace the current order page shell with the reference banner + tabs layout**

  Match the provided “分销订单” screenshot:
  - red hero header
  - total order count card
  - tab row for 全部 / 待结算 / 已结算
  - infinite scroll list
  - empty state

- [ ] **Step 2: Keep the existing filter behavior**

  Preserve the current tab-to-status mapping:
  - 全部 → `null`
  - 待结算 → `0`
  - 已结算 → `1`

- [ ] **Step 3: Keep the existing API binding**

  Continue loading with `getOrderBrokerageRecordPage(pageNo, pageSize, tab.status)` and render the returned records in the new card layout.

- [ ] **Step 4: Add back navigation parity**

  Keep the existing `goBack()` fallback so the page can return to `/pages/distributor/index`.

---

### Task 4: Restyle the withdraw and withdraw-record pages to match the佣金/提现 reference

**Files:**
- Modify: `UI/uniapp-marriage/pages/distributor/withdraw/index.uvue`
- Modify: `UI/uniapp-marriage/pages/distributor/withdraw-record/index.uvue`
- Modify: `UI/uniapp-marriage/utils/brokerage-api.uts` (only if shared withdraw display helpers are needed)

**Interfaces:**
- Consumes: `getBrokerageSummary()`, `getTradeConfig()`, `createBrokerageWithdraw()`, `getBrokerageWithdrawPage()`, `formatBrokerageAmount()`, `formatBrokerageTime()`, `formatBrokerageWithdrawStatus()`, `formatBrokerageWithdrawType()`
- Produces: a commission detail / withdraw pair that follows the supplied commission and withdraw visual language

- [ ] **Step 1: Rework the withdraw page into a commission-style detail screen**

  Use the supplied佣金 screenshot as the visual target:
  - red commission card at top
  - visible current/frozen/withdrawable amounts
  - select withdraw type
  - amount input
  - submit action
  - withdrawal guidance text

- [ ] **Step 2: Keep the withdraw submission behavior intact**

  Preserve the current validation and `createBrokerageWithdraw()` payload mapping for wallet / bank / QR / API transfers.

- [ ] **Step 3: Restyle the withdraw record page as a clean history list**

  Keep the current pagination and loading behavior, but make the list match the app’s commission history styling rather than the current bare card list.

- [ ] **Step 4: Keep back navigation and empty states consistent**

  Return to `/pages/distributor/index` when stack back is unavailable; preserve the current empty-state behavior.

---

### Task 5: Add the missing distributor feature pages

**Files:**
- Create: `UI/uniapp-marriage/pages/distributor/team/index.uvue`
- Create: `UI/uniapp-marriage/pages/distributor/wallet/index.uvue`
- Create: `UI/uniapp-marriage/pages/distributor/promoter-rank/index.uvue`
- Create: `UI/uniapp-marriage/pages/distributor/commission-rank/index.uvue`
- Create: `UI/uniapp-marriage/pages/distributor/goods/index.uvue`
- Modify: `UI/uniapp-marriage/pages.json`

**Interfaces:**
- Consumes: `getBrokerageUser()`, `getBrokerageSummary()`, `getBrokerageRecordPage()`, `getBrokerageWithdrawPage()`, `getProductBrokeragePrice()`, `getBrokerageUserChildSummaryPage()`, `getBrokerageUserChildSummaryPageByPrice()`, `getBrokerageUserRankPageByUserCount()`, `getRankByPrice()`
- Produces: feature pages reachable from the function grid

- [ ] **Step 1: Build the my team page skeleton and wire the team API**

  Use the mall `team.vue` page as the model for the layout:
  - red banner
  - team count summary
  - 一级 / 二级 tabs
  - search box
  - sort row
  - list / empty state

- [ ] **Step 2: Build the wallet / commission detail page skeleton**

  Use the mall `wallet.vue` page as the model for the layout:
  - current commission block
  - frozen / available summary
  - 分佣 / 提现 tabs
  - list / empty state

- [ ] **Step 3: Build the promoter rank page**

  Use the mall `promoter.vue` page as the model:
  - red header
  - 周排行 / 月排行 tabs
  - rank list / empty state

- [ ] **Step 4: Build the commission rank page**

  Use the mall `commission-ranking.vue` page as the model:
  - red trophy banner
  - current rank display
  - 周排行 / 月排行 tabs
  - rank list / empty state

- [ ] **Step 5: Build the promotion goods page**

  Use the mall `goods.vue` page as the model:
  - product list
  - estimated brokerage display
  - poster / share action where possible
  - empty state if there are no goods to show

- [ ] **Step 6: Add all new routes to `pages.json`**

  Make sure the new pages are declared in the router so the function grid links resolve.

---

### Task 6: Hook the distributor hub buttons to the new and existing pages

**Files:**
- Modify: `UI/uniapp-marriage/pages/distributor/index.uvue`
- Modify: `UI/uniapp-marriage/pages/distributor/order/index.uvue`
- Modify: `UI/uniapp-marriage/pages/distributor/withdraw/index.uvue`

**Interfaces:**
- Consumes: the new routes from Task 5 and the existing withdraw/order routes
- Produces: a fully navigable distributor feature hub

- [ ] **Step 1: Wire every function tile to a navigation target**

  Expected destinations:
  - 我的团队 → `/pages/distributor/team/index`
  - 佣金明细 → `/pages/distributor/wallet/index`
  - 分销订单 → `/pages/distributor/order/index`
  - 推广商品 → `/pages/distributor/goods/index`
  - 邀请海报 → existing poster UI
  - 推广排行 → `/pages/distributor/promoter-rank/index`
  - 佣金排行 → `/pages/distributor/commission-rank/index`

- [ ] **Step 2: Keep withdraw entry points discoverable**

  If withdraw is not part of the function grid, keep it reachable from the wallet/commission detail page and from any current action buttons.

- [ ] **Step 3: Preserve the old entry behavior where needed**

  Do not break current navigation from the old distributor home into order / withdraw pages while the UI is being replaced.

---

### Task 7: Validate the refactor against the supplied reference layouts

**Files:**
- Review only: `UI/uniapp-marriage/pages/distributor/**/*.uvue`
- Review only: `UI/uniapp-marriage/pages.json`
- Review only: `UI/uniapp-marriage/utils/brokerage-api.uts`

**Interfaces:**
- Consumes: all distributor pages and shared brokerage helpers
- Produces: verified distributor UI parity and route integrity

- [ ] **Step 1: Manually inspect each page for the reference structure**

  Confirm:
  - distributor hub matches the first reference image
  - order page matches the order screenshot
  - team / wallet / rank pages match the corresponding screenshots

- [ ] **Step 2: Verify API wiring paths**

  Check that the following still resolve correctly:
  - summary data
  - order list data
  - withdraw creation
  - withdraw history
  - poster generation
  - team/rank/product endpoints where available

- [ ] **Step 3: Verify fallback behavior for missing data**

  Every page must render a usable empty state when its API returns no rows or when the user lacks brokerage permission.

- [ ] **Step 4: Commit the finished UI refactor**

  Use a focused commit after the pages are all wired and visually aligned.
