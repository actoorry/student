# WeChat Withdraw Confirm Receipt Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a "确认收款" action on the `uniapp-marriage` withdraw record page for WeChat balance withdrawals that are still awaiting user confirmation, so users can complete confirmation in the mini program and then see refreshed status.

**Architecture:** Reuse the existing withdraw list and withdraw detail APIs. Add a small WeChat mini-program transfer-confirm helper in `utils/`, extend the withdraw record page to detect eligible records, fetch latest detail on click, call `wx.requestMerchantTransfer`, and then reload the page to refresh status.

**Tech Stack:** uni-app uvue, UTS utilities, existing `request` wrapper APIs, WeChat Mini Program `wx.requestMerchantTransfer`

## Global Constraints

- Only modify the `uniapp-marriage` frontend unless a missing frontend contract makes that impossible.
- Keep the entry on the withdraw record page; do not add a withdraw detail page.
- Show the button only for WeChat balance withdrawals (`type == 5`) that are still in the pending / processing stage.
- Reuse existing APIs: `getBrokerageWithdrawPage(...)` and `getBrokerageWithdraw(id)`.
- Only expose and execute the confirm action inside `MP-WEIXIN`; other platforms must not show a dead-end action.
- After confirm success / cancel / fail, restore button state cleanly; after success, refresh the record list.
- Follow existing code style in `UI/uniapp-marriage`: `script setup lang="uts"`, small helper functions, `uni.showToast` for user feedback.
- Do not introduce polling or a new backend API in this task.

---

## File Structure

- `UI/uniapp-marriage/utils/wechat-transfer.uts`
  - New focused utility wrapping `wx.requestMerchantTransfer` and platform checks.
- `UI/uniapp-marriage/pages/distributor/withdraw-record/index.uvue`
  - Existing withdraw record page; add button visibility logic, click handler, loading state, and reload-after-confirm behavior.
- `UI/uniapp-marriage/utils/brokerage-api.uts`
  - Keep existing API shape; only extend types / constants if needed to make page logic explicit and readable.

### Task 1: Add WeChat transfer confirm utility

**Files:**
- Create: `UI/uniapp-marriage/utils/wechat-transfer.uts`
- Reference: `UI/uniapp-marriage/pages/pay-cashier/index.uvue:194-221`

**Interfaces:**
- Consumes: WeChat mini program global `wx`; string inputs `mchId: string`, `packageInfo: string`
- Produces:
  - `export type MerchantTransferConfirmResult = { result: string; errMsg: string }`
  - `export function requestWechatMerchantTransfer(mchId: string, packageInfo: string): Promise<MerchantTransferConfirmResult>`
  - `export function canUseWechatMerchantTransfer(): boolean`

- [ ] **Step 1: Write the failing test notes / acceptance checks**

Use this acceptance checklist for manual-first TDD because this uni-app frontend area has no existing automated test harness in the repo:

```text
1. In MP-WEIXIN, canUseWechatMerchantTransfer() returns true only when wx.requestMerchantTransfer exists.
2. Calling requestWechatMerchantTransfer('', 'x') rejects with "缺少商户号".
3. Calling requestWechatMerchantTransfer('mch', '') rejects with "缺少确认收款参数".
4. On success callback, the Promise resolves with { result: 'success', errMsg }.
5. On fail callback containing cancel, the Promise rejects with { result: 'cancel', errMsg }.
6. On ordinary fail callback, the Promise rejects with { result: 'fail', errMsg }.
```

- [ ] **Step 2: Verify the failure mode before implementation**

Read the current codebase and confirm there is no existing helper in `UI/uniapp-marriage/utils/` for `requestMerchantTransfer`.

Run: search for `requestMerchantTransfer` under `UI/uniapp-marriage`
Expected: no existing helper implementation; only type fields / no consumer

- [ ] **Step 3: Write the minimal utility implementation**

Create `UI/uniapp-marriage/utils/wechat-transfer.uts` with this content:

```ts
export type MerchantTransferConfirmResult = {
	result: string
	errMsg: string
}

export function canUseWechatMerchantTransfer(): boolean {
	// #ifdef MP-WEIXIN
	const wxAny = wx as any
	return wxAny != null && typeof wxAny.requestMerchantTransfer == 'function'
	// #endif
	return false
}

export function requestWechatMerchantTransfer(mchId: string, packageInfo: string): Promise<MerchantTransferConfirmResult> {
	return new Promise((resolve, reject) => {
		if (mchId == null || mchId.trim() == '') {
			reject({ result: 'fail', errMsg: '缺少商户号' } as MerchantTransferConfirmResult)
			return
		}
		if (packageInfo == null || packageInfo.trim() == '') {
			reject({ result: 'fail', errMsg: '缺少确认收款参数' } as MerchantTransferConfirmResult)
			return
		}
		// #ifndef MP-WEIXIN
		reject({ result: 'fail', errMsg: '请在微信小程序中确认收款' } as MerchantTransferConfirmResult)
		return
		// #endif
		// #ifdef MP-WEIXIN
		if (!canUseWechatMerchantTransfer()) {
			reject({ result: 'fail', errMsg: '当前微信版本不支持确认收款，请升级微信后重试' } as MerchantTransferConfirmResult)
			return
		}
		const wxAny = wx as any
		wxAny.requestMerchantTransfer({
			mchId: mchId,
			appId: wxAny.getAccountInfoSync().miniProgram.appId,
			package: packageInfo,
			success: (res: any) => {
				resolve({ result: 'success', errMsg: res != null && res.errMsg != null ? String(res.errMsg) : '' } as MerchantTransferConfirmResult)
			},
			fail: (res: any) => {
				const errMsg = res != null && res.errMsg != null ? String(res.errMsg) : '确认收款失败'
				if (errMsg.indexOf('cancel') >= 0) {
					reject({ result: 'cancel', errMsg } as MerchantTransferConfirmResult)
					return
				}
				reject({ result: 'fail', errMsg } as MerchantTransferConfirmResult)
			}
		})
		// #endif
	})
}
```

- [ ] **Step 4: Validate utility correctness by inspection / build check**

Run the project type-check / build command you normally use for `uniapp-marriage` if available.
Expected: the new file compiles without unresolved `wx` / syntax errors.

If no build command is available in this session, manually verify:
- every code path returns or resolves / rejects
- `MP-WEIXIN` guards wrap all direct `wx` usage

- [ ] **Step 5: Commit**

```bash
git add UI/uniapp-marriage/utils/wechat-transfer.uts
git commit -m "feat(marriage): add wechat transfer confirm helper"
```

### Task 2: Make withdraw record data model explicit for confirm eligibility

**Files:**
- Modify: `UI/uniapp-marriage/utils/brokerage-api.uts`

**Interfaces:**
- Consumes: Existing `BrokerageWithdrawRecord` type and withdraw page / detail API functions
- Produces:
  - `export const BROKERAGE_WITHDRAW_TYPE_WECHAT_API = 5`
  - `export const BROKERAGE_WITHDRAW_STATUS_AUDIT_SUCCESS = 10`
  - `export type BrokerageWithdrawRecord` still includes `id`, `type`, `status`, `payTransferId`, `transferChannelPackageInfo`, `transferChannelMchId`

- [ ] **Step 1: Write the failing readability goal**

Document the current problem:

```text
The withdraw record page currently uses raw literals for withdraw type/status or has no helper constants,
which makes confirm-button rules harder to read and easier to break.
```

- [ ] **Step 2: Verify the current state fails the readability goal**

Read `UI/uniapp-marriage/utils/brokerage-api.uts` and confirm there are no exported constants for WeChat API withdraw type / audit-success state.
Expected: no explicit named constants for this flow

- [ ] **Step 3: Add the minimal constants without changing API behavior**

In `UI/uniapp-marriage/utils/brokerage-api.uts`, add these exports near the existing type / status helpers:

```ts
export const BROKERAGE_WITHDRAW_TYPE_WECHAT_API = 5
export const BROKERAGE_WITHDRAW_STATUS_AUDIT_SUCCESS = 10
```

Do not change request URLs or response parsing.

- [ ] **Step 4: Verify the file still matches existing behavior**

Manual check:
- `getBrokerageWithdrawPage(...)` signature unchanged
- `getBrokerageWithdraw(id)` signature unchanged
- existing pages importing this file would still compile if untouched

- [ ] **Step 5: Commit**

```bash
git add UI/uniapp-marriage/utils/brokerage-api.uts
git commit -m "refactor(marriage): name withdraw confirm constants"
```

### Task 3: Add confirm button UI and click flow on withdraw record page

**Files:**
- Modify: `UI/uniapp-marriage/pages/distributor/withdraw-record/index.uvue`
- Consumes from Task 1: `requestWechatMerchantTransfer(mchId: string, packageInfo: string): Promise<MerchantTransferConfirmResult>`
- Consumes from Task 2: `BROKERAGE_WITHDRAW_TYPE_WECHAT_API`, `BROKERAGE_WITHDRAW_STATUS_AUDIT_SUCCESS`

**Interfaces:**
- Consumes:
  - `getBrokerageWithdrawPage(params: BrokerageWithdrawPageParams): Promise<PageResult<BrokerageWithdrawRecord>>`
  - `getBrokerageWithdraw(id: number): Promise<BrokerageWithdrawRecord>`
  - `requestWechatMerchantTransfer(mchId: string, packageInfo: string): Promise<MerchantTransferConfirmResult>`
- Produces:
  - local helper `canConfirmWithdraw(item: BrokerageWithdrawRecord): boolean`
  - local handler `handleConfirmReceipt(item: BrokerageWithdrawRecord): void`
  - local state `confirmingId = ref(0)`

- [ ] **Step 1: Write the failing manual scenario**

Use this manual scenario as the failing test:

```text
Given a withdraw record of type 5 and status 10 with a payTransferId,
the page should show a "确认收款" button.
Today it shows no action and the user cannot complete receipt.
```

- [ ] **Step 2: Verify the current page fails**

Open `UI/uniapp-marriage/pages/distributor/withdraw-record/index.uvue` and confirm:
- cards show title / amount / time / status only
- there is no confirm button
- there is no handler for fetching detail and calling WeChat confirm

Expected: missing action flow

- [ ] **Step 3: Update imports and state with minimal additions**

Change the script imports and state at the top of `index.uvue` to include:

```ts
import {
	getBrokerageWithdrawPage,
	getBrokerageWithdraw,
	formatBrokerageAmount,
	formatBrokerageTime,
	formatBrokerageWithdrawStatus,
	formatBrokerageWithdrawType,
	type BrokerageWithdrawRecord,
	type BrokerageWithdrawPageParams,
	BROKERAGE_WITHDRAW_TYPE_WECHAT_API,
	BROKERAGE_WITHDRAW_STATUS_AUDIT_SUCCESS
} from '../../../utils/brokerage-api.uts'
import { canUseWechatMerchantTransfer, requestWechatMerchantTransfer } from '../../../utils/wechat-transfer.uts'
```

Add state next to the existing refs:

```ts
const confirmingId = ref(0)
```

- [ ] **Step 4: Add button visibility helpers and action handler**

Add these functions below `formatStatus`:

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

- [ ] **Step 5: Render the button in each eligible card**

Replace the card footer block with this structure so time / status stay visible and the action appears beneath them when allowed:

```vue
<view class="record-card" v-for="item in records" :key="'withdraw-record-' + String(item.id)">
	<view class="record-head">
		<text class="record-card-title">{{ formatWithdrawTitle(item) }}</text>
		<text class="record-card-amount">{{ formatAmount(item.price) }}</text>
	</view>
	<view class="record-foot">
		<text class="record-card-time">{{ formatTime(item.createTime) }}</text>
		<text class="record-card-status">{{ formatStatus(item) }}</text>
	</view>
	<view v-if="canConfirmWithdraw(item)" class="record-action-row">
		<view class="record-action-button" :class="isConfirming(item) ? 'record-action-button-disabled' : ''" @tap="handleConfirmReceipt(item)">
			<text class="record-action-button-text">{{ isConfirming(item) ? '确认中...' : '确认收款' }}</text>
		</view>
	</view>
</view>
```

- [ ] **Step 6: Add the minimal styles for the new action row**

Append these style rules near the existing `.record-*` styles:

```css
.record-action-row {
	flex-direction: row;
	justify-content: flex-end;
	margin-top: 12px;
}
.record-action-button {
	min-width: 92px;
	height: 34px;
	padding-left: 14px;
	padding-right: 14px;
	border-radius: 17px;
	align-items: center;
	justify-content: center;
	background-color: #E93323;
}
.record-action-button-disabled {
	opacity: 0.6;
}
.record-action-button-text {
	font-size: 13px;
	font-weight: 700;
	color: #fff;
}
```

- [ ] **Step 7: Manually verify the page behavior**

Manual checks in WeChat mini program:

```text
1. Type 5 + status 10 + payTransferId present => shows 确认收款.
2. Non-type-5 record => no button.
3. Click button => button changes to 确认中....
4. Missing package info from detail => toast "收款参数未准备完成，请稍后重试".
5. WeChat cancel => toast "已取消确认收款" and button recovers.
6. WeChat success => toast then reload list.
```

- [ ] **Step 8: Commit**

```bash
git add UI/uniapp-marriage/pages/distributor/withdraw-record/index.uvue
git commit -m "feat(marriage): support confirm receipt on withdraw records"
```

### Task 4: Verify end-to-end behavior and clean up integration details

**Files:**
- Modify if needed: `UI/uniapp-marriage/pages/distributor/withdraw-record/index.uvue`
- Modify if needed: `UI/uniapp-marriage/utils/wechat-transfer.uts`
- Modify if needed: `UI/uniapp-marriage/utils/brokerage-api.uts`

**Interfaces:**
- Consumes: all interfaces from Tasks 1-3
- Produces: stable final behavior matching the design spec

- [ ] **Step 1: Run the end-to-end manual scenario in order**

Use one real or staged record matching the target state.

```text
A. Open 提现记录 page in MP-WEIXIN.
B. Find a 微信零钱 record in 待确认/转账中 stage.
C. Tap 确认收款.
D. Complete WeChat confirmation.
E. Return to page and verify list reload runs automatically.
F. Re-open page and confirm the record no longer exposes the action once status advances.
```

- [ ] **Step 2: Verify no regressions on non-target records**

Check these cases manually:

```text
1. 银行卡提现 records still render normally.
2. 支付宝 / 收款码 records still render normally.
3. Empty state, load-more, and reload paths still work.
4. Rapid double-tap on confirm does not start two requests because confirmingId locks the action.
```

- [ ] **Step 3: Apply only minimal fixes discovered during verification**

Allowed minimal fixes include:

```text
- Adjusting canConfirmWithdraw() if the live status code differs from the expected value.
- Tweaking toast text for clarity.
- Resetting confirmingId in any missed failure path.
- Hiding the button if the environment check behaves differently on device.
```

Do not add polling, a new page, or backend changes in this task.

- [ ] **Step 4: Final verification pass**

Expected final state:

```text
- Eligible WeChat withdraw records show 确认收款.
- Clicking the action fetches latest detail first.
- Missing params fail fast with a clear toast.
- WeChat success returns and triggers reload().
- Non-WeChat or finished records never show the action.
```

- [ ] **Step 5: Commit**

```bash
git add UI/uniapp-marriage/pages/distributor/withdraw-record/index.uvue UI/uniapp-marriage/utils/wechat-transfer.uts UI/uniapp-marriage/utils/brokerage-api.uts
git commit -m "fix(marriage): finalize withdraw confirm receipt flow"
```

## Self-Review

### Spec coverage
- Withdraw-record-page entry: covered by Task 3.
- WeChat-only action visibility: covered by Task 1 + Task 3.
- Reuse existing APIs: covered by Task 2 + Task 3.
- Fetch latest detail before confirm: covered by Task 3.
- Reload after success: covered by Task 3 + Task 4.
- No new detail page / no polling: enforced in Global Constraints and Task 4.

### Placeholder scan
- No `TODO` / `TBD` placeholders remain.
- All changed files have exact paths.
- Each code-changing step includes concrete code.

### Type consistency
- `requestWechatMerchantTransfer(mchId: string, packageInfo: string)` is defined in Task 1 and consumed consistently in Task 3.
- `BROKERAGE_WITHDRAW_TYPE_WECHAT_API` and `BROKERAGE_WITHDRAW_STATUS_AUDIT_SUCCESS` are defined in Task 2 and consumed consistently in Task 3.
- `handleConfirmReceipt(item: BrokerageWithdrawRecord): void` only consumes fields present on `BrokerageWithdrawRecord`.
