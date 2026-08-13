# Wallet WeChat Withdraw Confirm Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a "确认收款" action directly to the `uniapp-marriage` wallet page's withdraw tab so users can confirm WeChat balance withdrawals from the page they actually use.

**Architecture:** Keep the existing shared rendering flow for the brokerage tab, but split the withdraw tab into its own `BrokerageWithdrawRecord[]` rendering branch. Reuse the existing `wechat-transfer.uts` helper and `brokerage-api.uts` constants / detail API, then fetch latest withdraw detail before calling `wx.requestMerchantTransfer` and reload the page after success.

**Tech Stack:** uni-app uvue, UTS utilities, existing request wrapper APIs, WeChat Mini Program `wx.requestMerchantTransfer`

## Global Constraints

- Only modify the `uniapp-marriage` frontend unless a missing frontend contract makes that impossible.
- Keep the entry on the wallet page withdraw tab; do not add a withdraw detail page.
- Let the `分佣` tab keep its current shared-list behavior.
- Let the `提现` tab render original `BrokerageWithdrawRecord` data directly.
- Show the button only for WeChat balance withdrawals (`type == 5`) that are still in the pending / processing stage represented by `BROKERAGE_WITHDRAW_STATUS_AUDIT_SUCCESS`.
- Reuse existing APIs: `getBrokerageWithdrawPage(...)` and `getBrokerageWithdraw(id)`.
- Only expose and execute the confirm action inside `MP-WEIXIN`; other platforms must not show a dead-end action.
- After confirm success / cancel / fail, restore button state cleanly; after success, refresh the page.
- Follow existing code style in `UI/uniapp-marriage`: `script setup lang="uts"`, small helper functions, `uni.showToast` for user feedback.
- Do not introduce polling or a new backend API in this task.

---

## File Structure

- `UI/uniapp-marriage/pages/distributor/wallet/index.uvue`
  - Main target page. Split withdraw rendering from brokerage rendering, keep withdraw records in original shape, add confirm-button visibility / state / click flow.
- `UI/uniapp-marriage/utils/wechat-transfer.uts`
  - Already-created helper. Reused as-is unless verification finds a bug.
- `UI/uniapp-marriage/utils/brokerage-api.uts`
  - Already contains the withdraw constants and detail API. Reused as-is unless verification finds a mismatch.

### Task 1: Split wallet page data flow between brokerage and withdraw tabs

**Files:**
- Modify: `UI/uniapp-marriage/pages/distributor/wallet/index.uvue`

**Interfaces:**
- Consumes:
  - `getBrokerageRecordPage(params): Promise<PageResult<BrokerageRecord>>`
  - `getBrokerageWithdrawPage(params): Promise<PageResult<BrokerageWithdrawRecord>>`
- Produces:
  - `recordItems: Ref<WalletListItem[]>`
  - `withdrawItems: Ref<BrokerageWithdrawRecord[]>`
  - `currentTab == 0` renders `recordItems`
  - `currentTab == 1` renders `withdrawItems`

- [ ] **Step 1: Write the failing test notes**

Use this manual failing scenario:

```text
When the user opens the wallet page and switches to 提现, the page only has flattened WalletListItem records.
Because the original BrokerageWithdrawRecord fields are discarded, the page cannot decide whether to show 确认收款.
```

- [ ] **Step 2: Verify the current failure**

Read `UI/uniapp-marriage/pages/distributor/wallet/index.uvue` and confirm:
- both tabs share `items: WalletListItem[]`
- `setWithdrawItems()` flattens withdraw records into generic items
- there is no place to access `type`, `payTransferId`, or transfer package fields during render

Expected: withdraw-specific fields are lost before render.

- [ ] **Step 3: Add separate state for the two tabs**

Replace the page state block with this shape:

```ts
type WalletListItem = {
	key: string
	title: string
	amount: string
	status: string
	time: string
	positive: boolean
}

const PAGE_SIZE = 10
const isLoggedIn = ref(isMemberLoggedIn())
const loading = ref(false)
const errorText = ref('')
const currentTab = ref(0)
const pageNo = ref(1)
const total = ref(0)
const summary = ref<BrokerageSummary>({
	yesterdayPrice: 0,
	withdrawPrice: 0,
	brokeragePrice: 0,
	frozenPrice: 0,
	firstBrokerageUserCount: 0,
	secondBrokerageUserCount: 0
} as BrokerageSummary)
const recordItems = ref<WalletListItem[]>([])
const withdrawItems = ref<BrokerageWithdrawRecord[]>([])
```

- [ ] **Step 4: Update computed values to use the active tab list**

Replace the `hasMore` computed with:

```ts
const currentListCount = computed(() => currentTab.value == 0 ? recordItems.value.length : withdrawItems.value.length)
const hasMore = computed(() => currentListCount.value < total.value)
```

Keep `loadText` and `filterText`, but let them continue to read `hasMore` / `currentTab`.

- [ ] **Step 5: Update reload and page-loading logic**

Replace the reset logic inside `reload()` with:

```ts
pageNo.value = 1
total.value = 0
recordItems.value = []
withdrawItems.value = []
errorText.value = ''
```

Replace `loadPage()` with:

```ts
function loadPage(): Promise<void> {
	if (currentTab.value == 0) {
		return getBrokerageRecordPage({
			pageNo: pageNo.value,
			pageSize: PAGE_SIZE
		}).then((result) => {
			total.value = result.total
			setRecordItems(result.list || [])
		})
	}
	return getBrokerageWithdrawPage({
		pageNo: pageNo.value,
		pageSize: PAGE_SIZE
	}).then((result) => {
		total.value = result.total
		setWithdrawItems(result.list || [])
	})
}
```

- [ ] **Step 6: Update the two mapping functions**

Keep `setRecordItems()` as the flattened mapper:

```ts
function setRecordItems(list: BrokerageRecord[]): void {
	const mapped = list.map((item) => ({
		key: 'record-' + String(item.id),
		title: item.title,
		amount: '+' + formatBrokerageAmount(item.price),
		status: formatBrokerageStatus(item),
		time: formatBrokerageTime(item.createTime),
		positive: true
	} as WalletListItem))
	if (pageNo.value == 1) {
		recordItems.value = mapped
	} else {
		recordItems.value = recordItems.value.concat(mapped)
	}
	errorText.value = ''
}
```

Replace `setWithdrawItems()` with:

```ts
function setWithdrawItems(list: BrokerageWithdrawRecord[]): void {
	if (pageNo.value == 1) {
		withdrawItems.value = list
	} else {
		withdrawItems.value = withdrawItems.value.concat(list)
	}
	errorText.value = ''
}
```

- [ ] **Step 7: Verify the refactor by inspection**

Manual check:
- `分佣` tab still has everything needed from `recordItems`
- `提现` tab now preserves raw withdraw fields for render-time decisions
- pagination still uses the active tab’s count via `currentListCount`

- [ ] **Step 8: Commit**

```bash
git add UI/uniapp-marriage/pages/distributor/wallet/index.uvue
git commit -m "refactor(marriage): split wallet page withdraw data flow"
```

### Task 2: Render withdraw tab directly and add confirm button UI

**Files:**
- Modify: `UI/uniapp-marriage/pages/distributor/wallet/index.uvue`

**Interfaces:**
- Consumes:
  - `recordItems: Ref<WalletListItem[]>`
  - `withdrawItems: Ref<BrokerageWithdrawRecord[]>`
- Produces:
  - `currentTab == 0` → brokerage cards
  - `currentTab == 1` → withdraw cards with optional action row

- [ ] **Step 1: Write the failing UI scenario**

Use this failing scenario:

```text
On the wallet page's 提现 tab, a WeChat withdraw in 审核通过 state should show 确认收款.
Today the tab renders the same generic card template used by 分佣, so no action row can appear.
```

- [ ] **Step 2: Verify the current template fails**

Confirm the template currently uses only:

```vue
<view v-if="items.length == 0" class="empty-wrap">...</view>
<view v-else>
  <view class="record-card" v-for="item in items" ...>
```

Expected: no per-tab rendering branch.

- [ ] **Step 3: Add tab-specific empty-state and list rendering**

Replace the current list section with this structure:

```vue
<view v-if="currentTab == 0 && recordItems.length == 0" class="empty-wrap">
	<text class="empty-text">暂无数据</text>
</view>
<view v-else-if="currentTab == 1 && withdrawItems.length == 0" class="empty-wrap">
	<text class="empty-text">暂无数据</text>
</view>
<view v-else-if="currentTab == 0">
	<view class="record-card" v-for="item in recordItems" :key="item.key">
		<view class="record-head">
			<text class="record-title">{{ item.title }}</text>
			<text class="record-price" :class="item.positive ? 'record-price-positive' : 'record-price-negative'">{{ item.amount }}</text>
		</view>
		<view class="record-foot">
			<text class="record-time">{{ item.time }}</text>
			<text class="record-status">{{ item.status }}</text>
		</view>
	</view>
	<view v-if="errorText != ''" class="inline-error" @tap="loadMore">
		<text class="inline-error-text">{{ errorText }}，点击重试</text>
	</view>
	<view class="load-row">
		<text class="load-text">{{ loadText }}</text>
	</view>
</view>
<view v-else>
	<view class="record-card" v-for="item in withdrawItems" :key="'withdraw-' + String(item.id)">
		<view class="record-head">
			<text class="record-title">{{ formatWithdrawTitle(item) }}</text>
			<text class="record-price record-price-negative">-{{ formatBrokerageAmount(item.price) }}</text>
		</view>
		<view class="record-foot">
			<text class="record-time">{{ formatBrokerageTime(item.createTime) }}</text>
			<text class="record-status">{{ formatWithdrawStatus(item) }}</text>
		</view>
		<view v-if="canConfirmWithdraw(item)" class="record-action-row">
			<view class="record-action-button" :class="isConfirming(item) ? 'record-action-button-disabled' : ''" @tap="handleConfirmReceipt(item)">
				<text class="record-action-button-text">{{ isConfirming(item) ? '确认中...' : '确认收款' }}</text>
			</view>
		</view>
	</view>
	<view v-if="errorText != ''" class="inline-error" @tap="loadMore">
		<text class="inline-error-text">{{ errorText }}，点击重试</text>
	</view>
	<view class="load-row">
		<text class="load-text">{{ loadText }}</text>
	</view>
</view>
```

- [ ] **Step 4: Add helper functions for withdraw labels**

Add these functions below `setWithdrawItems()`:

```ts
function formatWithdrawTitle(item: BrokerageWithdrawRecord): string {
	const typeName = item.typeName != null && item.typeName != '' ? item.typeName : formatBrokerageWithdrawType(item.type)
	return '提现到' + typeName
}

function formatWithdrawStatus(item: BrokerageWithdrawRecord): string {
	return item.statusName != null && item.statusName != '' ? item.statusName : formatBrokerageWithdrawStatus(item.status)
}
```

- [ ] **Step 5: Add styles for the withdraw action row**

Append these style rules near the existing `.record-*` styles:

```css
.record-action-row { flex-direction: row; justify-content: flex-end; margin-top: 12px; }
.record-action-button { min-width: 92px; height: 34px; padding-left: 14px; padding-right: 14px; border-radius: 17px; align-items: center; justify-content: center; background-color: #E93323; }
.record-action-button-disabled { opacity: 0.6; }
.record-action-button-text { font-size: 13px; font-weight: 700; color: #fff; }
```

- [ ] **Step 6: Verify the template split by inspection**

Manual check:
- `分佣` tab still renders generic items only
- `提现` tab now has direct access to `BrokerageWithdrawRecord`
- the `确认收款` button can now be shown without polluting `WalletListItem`

- [ ] **Step 7: Commit**

```bash
git add UI/uniapp-marriage/pages/distributor/wallet/index.uvue
git commit -m "feat(marriage): render wallet withdraw tab directly"
```

### Task 3: Add wallet-page confirm logic using existing helper

**Files:**
- Modify: `UI/uniapp-marriage/pages/distributor/wallet/index.uvue`
- Reference: `UI/uniapp-marriage/utils/wechat-transfer.uts`
- Reference: `UI/uniapp-marriage/utils/brokerage-api.uts`

**Interfaces:**
- Consumes:
  - `getBrokerageWithdraw(id: number): Promise<BrokerageWithdrawRecord>`
  - `requestWechatMerchantTransfer(mchId: string, packageInfo: string): Promise<MerchantTransferConfirmResult>`
  - `BROKERAGE_WITHDRAW_TYPE_WECHAT_API`
  - `BROKERAGE_WITHDRAW_STATUS_AUDIT_SUCCESS`
- Produces:
  - `confirmingId = ref(0)`
  - `canConfirmWithdraw(item: BrokerageWithdrawRecord): boolean`
  - `isConfirming(item: BrokerageWithdrawRecord): boolean`
  - `handleConfirmReceipt(item: BrokerageWithdrawRecord): void`

- [ ] **Step 1: Write the failing behavior scenario**

Use this failing scenario:

```text
A withdraw item on the wallet page may display as 审核通过, but tapping it does nothing because the page has no confirm logic.
The user should be able to tap 确认收款, fetch latest detail, and launch WeChat confirmation.
```

- [ ] **Step 2: Verify the current page lacks confirm logic**

Confirm the current wallet page has:
- no `confirmingId`
- no `canConfirmWithdraw()`
- no `handleConfirmReceipt()`
- no import of `wechat-transfer.uts`

Expected: missing interaction layer.

- [ ] **Step 3: Add imports and state**

Update the imports to include:

```ts
import {
	getBrokerageSummary,
	getBrokerageRecordPage,
	getBrokerageWithdrawPage,
	getBrokerageWithdraw,
	formatBrokerageAmount,
	formatBrokerageTime,
	formatBrokerageStatus,
	formatBrokerageWithdrawStatus,
	formatBrokerageWithdrawType,
	type BrokerageSummary,
	type BrokerageRecord,
	type BrokerageWithdrawRecord,
	BROKERAGE_WITHDRAW_TYPE_WECHAT_API,
	BROKERAGE_WITHDRAW_STATUS_AUDIT_SUCCESS
} from '../../../utils/brokerage-api.uts'
import { canUseWechatMerchantTransfer, requestWechatMerchantTransfer } from '../../../utils/wechat-transfer.uts'
```

Add state near the refs:

```ts
const confirmingId = ref(0)
```

- [ ] **Step 4: Add the eligibility and action handlers**

Add these functions below `formatWithdrawStatus()`:

```ts
function canConfirmWithdraw(item: BrokerageWithdrawRecord): boolean {
	// #ifndef MP-WEIXIN
	return false
	// #endif
	if (item.type != BROKERAGE_WITHDRAW_TYPE_WECHAT_API) {
		return false
	}
	if (item.payTransferId == null || item.payTransferId <= 0) {
		return false
	}
	if (item.status != BROKERAGE_WITHDRAW_STATUS_AUDIT_SUCCESS) {
		return false
	}
	return canUseWechatMerchantTransfer()
}

function isConfirming(item: BrokerageWithdrawRecord): boolean {
	return confirmingId.value == item.id
}

function handleConfirmReceipt(item: BrokerageWithdrawRecord): void {
	if (confirmingId.value > 0) {
		return
	}
	if (!canConfirmWithdraw(item)) {
		uni.showToast({ title: '当前记录暂不能确认收款', icon: 'none' })
		return
	}
	confirmingId.value = item.id
	getBrokerageWithdraw(item.id).then((detail) => {
		if (detail.transferChannelMchId == null || detail.transferChannelMchId == '') {
			return Promise.reject('收款参数未准备完成，请稍后重试')
		}
		if (detail.transferChannelPackageInfo == null || detail.transferChannelPackageInfo == '') {
			return Promise.reject('收款参数未准备完成，请稍后重试')
		}
		return requestWechatMerchantTransfer(detail.transferChannelMchId, detail.transferChannelPackageInfo)
	}).then(() => {
		uni.showToast({ title: '已发起确认收款', icon: 'none' })
		reload()
	}).catch((error) => {
		if (error != null && typeof error == 'object') {
			const obj = error as any
			if (obj.result != null && obj.result == 'cancel') {
				uni.showToast({ title: '已取消确认收款', icon: 'none' })
				return
			}
			if (obj.errMsg != null && typeof obj.errMsg == 'string' && obj.errMsg != '') {
				uni.showToast({ title: obj.errMsg, icon: 'none' })
				return
			}
		}
		uni.showToast({ title: resolveError(error, '确认收款失败，请稍后重试'), icon: 'none' })
	}).finally(() => {
		confirmingId.value = 0
	})
}
```

- [ ] **Step 5: Verify the behavior contract by inspection**

Manual check:
- only wallet withdraw tab items can render the action
- only WeChat balance withdrawals in audit-success state are eligible
- detail is re-fetched before calling WeChat confirm
- success reloads the page; cancel / failure restores the button state

- [ ] **Step 6: Commit**

```bash
git add UI/uniapp-marriage/pages/distributor/wallet/index.uvue
git commit -m "feat(marriage): add wallet withdraw confirm action"
```

### Task 4: Verify the wallet-page user path end to end

**Files:**
- Modify if needed: `UI/uniapp-marriage/pages/distributor/wallet/index.uvue`
- Reuse: `UI/uniapp-marriage/utils/wechat-transfer.uts`
- Reuse: `UI/uniapp-marriage/utils/brokerage-api.uts`

**Interfaces:**
- Consumes: all interfaces from Tasks 1-3
- Produces: final wallet-page behavior matching the updated spec

- [ ] **Step 1: Run the end-to-end manual scenario**

Use this path exactly:

```text
A. Open the 佣金 page.
B. Tap the 提现 tab.
C. Confirm that a 微信零钱 withdrawal in 审核通过 state shows 确认收款.
D. Tap 确认收款.
E. Complete or cancel the WeChat confirm flow.
F. Verify toast feedback is correct.
G. On success, verify reload() runs and the list refreshes.
```

- [ ] **Step 2: Verify non-target cases**

Check these cases manually:

```text
1. 分佣 tab still works exactly as before.
2. 银行卡 / 支付宝 / 收款码提现记录 never show 确认收款.
3. 审核中 / 提现成功 / 提现失败 records never show 确认收款.
4. Double-tap does not trigger duplicate actions because confirmingId locks the request.
5. Empty state and pagination still work on both tabs.
```

- [ ] **Step 3: Apply only minimal fixes discovered during verification**

Allowed minimal fixes:

```text
- Adjusting the eligibility check if live status values differ from the expectation.
- Fixing tab-specific empty-state rendering.
- Fixing load-more count calculations between recordItems and withdrawItems.
- Tweaking toast copy for clarity.
```

Do not add polling, a new page, or backend changes.

- [ ] **Step 4: Final verification pass**

Expected final state:

```text
- Users can complete confirm receipt from the wallet page they actually use.
- The withdraw tab renders original withdraw records, not generic flattened items.
- Eligible WeChat withdraws show 确认收款.
- Clicking the action fetches latest detail first and then launches WeChat confirmation.
- On success, the page reloads and refreshes withdraw status.
```

- [ ] **Step 5: Commit**

```bash
git add UI/uniapp-marriage/pages/distributor/wallet/index.uvue
git commit -m "fix(marriage): finalize wallet withdraw confirm flow"
```

## Self-Review

### Spec coverage
- Real user path moved to `wallet/index.uvue`: covered by Tasks 1-4.
- Withdraw tab rendered directly instead of flattened items: covered by Tasks 1 and 2.
- Confirm button only on eligible WeChat withdraws: covered by Tasks 2 and 3.
- Reuse of `wechat-transfer.uts` and `brokerage-api.uts`: covered by Task 3.
- No new page / no backend changes / no polling: enforced in Global Constraints and Task 4.

### Placeholder scan
- No `TODO` / `TBD` placeholders remain.
- All files use exact paths.
- All code-changing steps include concrete code.

### Type consistency
- `recordItems` remains `WalletListItem[]` only for tab 0.
- `withdrawItems` remains `BrokerageWithdrawRecord[]` only for tab 1.
- `handleConfirmReceipt(item: BrokerageWithdrawRecord)` uses the same withdraw type and status constants defined in `brokerage-api.uts`.
- `requestWechatMerchantTransfer(mchId: string, packageInfo: string)` matches the already-created helper interface.
