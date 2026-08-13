# Miniapp Marriage Withdraw Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix the marriage miniapp withdraw page so Wechat balance withdraw (type=5) correctly uses openid as `userAccount` and a platform-derived `transferChannelCode`, while preserving all other withdrawal methods.

**Architecture:** The marriage app already has `getWechatMiniSocialUser()` (returns `{ openid }` via `/partner/social-user/get?type=34`) and `PAY_CHANNEL_WX_LITE` constant in `pay-api.uts`. The withdraw page just needs to: (1) fetch the social user before submitting type=5, (2) use the returned openid as `userAccount`, (3) use the platform constant as `transferChannelCode`, and (4) remove the user-facing "提现账号" input for type=5. No new backend endpoints, no framework changes, no database changes.

**Tech Stack:** uni-app (`<script setup lang="uts">` / `uvue`), existing `brokerage-api.uts`, existing `pay-api.uts`

## Global Constraints

- Keep all business code under `UI/uniapp-marriage`.
- Do not add a new Maven module.
- Do not change Java backend behavior.
- Do not modify `suxin-framework`.
- Use existing brokerage APIs; do not invent new backend endpoints.
- Database changes require SQL output only.
- Do not run Maven compile unless explicitly requested.

---

### Task 1: Fetch social user in withdraw page

**Files:**
- Modify: `UI/uniapp-marriage/pages/distributor/withdraw/index.uvue`

**Interfaces:**
- Consumes: `getWechatMiniSocialUser()` from `pay-api.uts` (returns `{ openid: string }`)
- Produces: withdraw page loads social user openid on mount for type=5 use

- [ ] **Step 1: Import getWechatMiniSocialUser**

In `UI/uniapp-marriage/pages/distributor/withdraw/index.uvue`, add the import:

```uts
import { getBrokerageSummary, getTradeConfig, createBrokerageWithdraw, formatBrokerageAmount, type BrokerageSummary, type TradeConfig, type BrokerageWithdrawCreateReq } from '../../../utils/brokerage-api.uts'
import { getWechatMiniSocialUser, PAY_CHANNEL_WX_LITE } from '../../../utils/pay-api.uts'
```

- [ ] **Step 2: Add social user state variable**

Add a reactive ref to hold the openid:

```uts
const wechatOpenid = ref('')
```

- [ ] **Step 3: Fetch social user during loadData**

In the `loadData()` function, add a third API call to fetch the social user. After the existing `Promise.all`, add:

```uts
function loadData(): void {
	loading.value = true
	errorText.value = ''
	Promise.all([
		getBrokerageSummary(),
		getTradeConfig()
	]).then((result) => {
		const summary = result[0] as BrokerageSummary
		const config = result[1] as TradeConfig
		withdrawablePrice.value = summary.brokeragePrice
		balanceValueText.value = (summary.brokeragePrice / 100).toFixed(2)
		frozenPriceText.value = formatBrokerageAmount(summary.frozenPrice)
		minPrice.value = config.brokerageWithdrawMinPrice || 0
		if (config.brokerageWithdrawMinPrice > 0) {
			minPriceText.value = formatBrokerageAmount(config.brokerageWithdrawMinPrice)
		} else {
			minPriceText.value = ''
		}
		const types = config.brokerageWithdrawTypes || []
		methodOptions.value = types.map((t) => ({
			type: t,
			title: METHOD_LABELS[t] || '未知方式',
			desc: METHOD_DESCS[t] || ''
		} as MethodOption))
		if (methodOptions.value.length > 0) {
			selectMethod(methodOptions.value[0].type)
		}
		// Fetch social user for Wechat withdraw openid
		getWechatMiniSocialUser().then((socialUser) => {
			if (socialUser != null && socialUser.openid != null && socialUser.openid != '') {
				wechatOpenid.value = socialUser.openid
			}
		}).catch(() => {
			// Social user fetch failed — type=5 will be blocked at submit time
		})
	}).catch((error) => {
		errorText.value = resolveError(error, '账户信息加载失败')
	}).finally(() => {
		loading.value = false
	})
}
```

- [ ] **Step 4: Commit**

```bash
git add UI/uniapp-marriage/pages/distributor/withdraw/index.uvue
git commit -m "feat: fetch social user openid in marriage withdraw page"
```

---

### Task 2: Remove user-facing "提现账号" input for type=5

**Files:**
- Modify: `UI/uniapp-marriage/pages/distributor/withdraw/index.uvue`

**Interfaces:**
- Consumes: `wechatOpenid` ref from Task 1
- Produces: type=5 no longer shows a manual "提现账号" input; user cannot manually enter openid

- [ ] **Step 1: Hide the "提现账号" field for type=5**

Change the `needAccountNo` computed property to exclude `WITHDRAW_TYPE_WECHAT_API`:

Current code (line 194-198):
```uts
const needAccountNo = computed(() => {
	return selectedType.value == WITHDRAW_TYPE_BANK ||
		selectedType.value == WITHDRAW_TYPE_WECHAT_API ||
		selectedType.value == WITHDRAW_TYPE_ALIPAY_API
})
```

Replace with:
```uts
const needAccountNo = computed(() => {
	return selectedType.value == WITHDRAW_TYPE_BANK ||
		selectedType.value == WITHDRAW_TYPE_ALIPAY_API
})
```

- [ ] **Step 2: Add a read-only openid display for type=5**

After the `needQrCode` block (around line 101-103 in template), add a new block that shows the bound openid when type=5 is selected:

```html
<view v-if="selectedType == WITHDRAW_TYPE_WECHAT_API" class="field-group">
	<text class="form-title">绑定微信账号</text>
	<view class="readonly-box">
		<text class="readonly-text" v-if="wechatOpenid != ''">{{ wechatOpenid }}</text>
		<text class="readonly-text" v-else>未绑定微信，无法使用微信零钱提现</text>
	</view>
</view>
```

Remove the old `needTransferChannel` block (lines 104-109) that showed `wx_lite（系统自动设置）`:

Delete this block entirely:
```html
<view v-if="needTransferChannel" class="field-group">
	<text class="form-title">转账渠道</text>
	<view class="readonly-box">
		<text class="readonly-text">wx_lite（系统自动设置）</text>
	</view>
</view>
```

- [ ] **Step 3: Remove the needTransferChannel computed property**

Delete the line:
```uts
const needTransferChannel = computed(() => selectedType.value == WITHDRAW_TYPE_WECHAT_API)
```

- [ ] **Step 4: Commit**

```bash
git add UI/uniapp-marriage/pages/distributor/withdraw/index.uvue
git commit -m "feat: hide manual account input and show bound openid for Wechat withdraw"
```

---

### Task 3: Fix submit payload for all withdrawal types

**Files:**
- Modify: `UI/uniapp-marriage/pages/distributor/withdraw/index.uvue`

**Interfaces:**
- Consumes: `wechatOpenid`, `PAY_CHANNEL_WX_LITE` from Task 1
- Produces: submit payload correctly maps `userAccount`/`transferChannelCode` per type, matching `AppBrokerageWithdrawCreateReqVO` contract

- [ ] **Step 1: Rewrite handleSubmit to build payload per type**

Replace the current `handleSubmit()` function (lines 273-328) with type-aware payload construction:

```uts
function handleSubmit(): void {
	submitError.value = ''
	validateAmount()
	if (selectedType.value == 0) {
		uni.showToast({ title: '请选择提现方式', icon: 'none' })
		return
	}
	if (amountText.value == '') {
		uni.showToast({ title: '请输入提现金额', icon: 'none' })
		return
	}
	if (amountError.value != '') {
		uni.showToast({ title: amountError.value, icon: 'none' })
		return
	}
	const value = parseFloat(amountText.value)
	const price = Math.round(value * 100)

	// Type-specific field validation
	if (selectedType.value == WITHDRAW_TYPE_BANK) {
		if (userName.value == '') {
			submitError.value = '请填写收款真名'
			return
		}
		if (userAccount.value == '') {
			submitError.value = '请填写提现账号'
			return
		}
		if (bankName.value == '') {
			submitError.value = '请填写开户银行'
			return
		}
	}
	if (selectedType.value == WITHDRAW_TYPE_WECHAT_QR || selectedType.value == WITHDRAW_TYPE_ALIPAY_QR) {
		if (qrCodeUrl.value == '') {
			submitError.value = '请上传收款码'
			return
		}
	}
	if (selectedType.value == WITHDRAW_TYPE_WECHAT_API) {
		if (wechatOpenid.value == '') {
			uni.showToast({ title: '未绑定微信，无法使用微信零钱提现', icon: 'none' })
			return
		}
		if (userName.value == '') {
			submitError.value = '请填写收款真名'
			return
		}
	}
	if (selectedType.value == WITHDRAW_TYPE_ALIPAY_API) {
		if (userAccount.value == '') {
			submitError.value = '请填写提现账号'
			return
		}
		if (userName.value == '') {
			submitError.value = '请填写收款真名'
			return
		}
	}

	// Build payload per type
	const req = {
		type: selectedType.value,
		price: price
	} as BrokerageWithdrawCreateReq

	if (selectedType.value == WITHDRAW_TYPE_WECHAT_API) {
		// Wechat balance: userAccount = openid, transferChannelCode from platform
		req.userAccount = wechatOpenid.value
		req.userName = userName.value != '' ? userName.value : null
		req.transferChannelCode = PAY_CHANNEL_WX_LITE
	} else if (selectedType.value == WITHDRAW_TYPE_BANK) {
		req.userAccount = userAccount.value
		req.userName = userName.value != '' ? userName.value : null
		req.bankName = bankName.value
		req.bankAddress = bankAddress.value != '' ? bankAddress.value : null
	} else if (selectedType.value == WITHDRAW_TYPE_ALIPAY_API) {
		req.userAccount = userAccount.value
		req.userName = userName.value != '' ? userName.value : null
	} else if (selectedType.value == WITHDRAW_TYPE_WECHAT_QR || selectedType.value == WITHDRAW_TYPE_ALIPAY_QR) {
		req.qrCodeUrl = qrCodeUrl.value
	}
	// WALLET: only type + price, no extra fields

	submitting.value = true
	createBrokerageWithdraw(req).then(() => {
		uni.showToast({ title: '提现申请已提交', icon: 'success' })
		setTimeout(() => {
			uni.navigateTo({ url: '/pages/distributor/withdraw-record/index' })
		}, 800)
	}).catch((error) => {
		uni.showToast({ title: resolveError(error, '提交失败'), icon: 'none' })
	}).finally(() => {
		submitting.value = false
	})
}
```

- [ ] **Step 2: Commit**

```bash
git add UI/uniapp-marriage/pages/distributor/withdraw/index.uvue
git commit -m "feat: fix withdraw submit payload per type, Wechat uses openid"
```

---

### Task 4: Update selectMethod to reset wechatOpenid display state

**Files:**
- Modify: `UI/uniapp-marriage/pages/distributor/withdraw/index.uvue`

**Interfaces:**
- Consumes: none
- Produces: `selectMethod` clears user inputs but preserves `wechatOpenid`

- [ ] **Step 1: Verify selectMethod is correct**

The existing `selectMethod` function (lines 245-253) already clears `userName`, `userAccount`, `bankName`, `bankAddress`, `qrCodeUrl` on method switch. It does NOT clear `wechatOpenid`, which is correct — the openid is system-derived and should persist across method switches. No change needed here.

- [ ] **Step 2: Commit (no-op verification)**

This is a verification-only task. If no code was changed, skip the commit. If any cleanup was done:

```bash
git add UI/uniapp-marriage/pages/distributor/withdraw/index.uvue
git commit -m "chore: verify selectMethod preserves wechatOpenid"
```

---

### Task 5: End-to-end verification

**Files:**
- Read: `UI/uniapp-marriage/pages/distributor/withdraw/index.uvue` (final state)
- Read: `UI/uniapp-marriage/utils/brokerage-api.uts`
- Read: `UI/uniapp-marriage/utils/pay-api.uts`

**Interfaces:**
- Consumes: all previous tasks
- Produces: verified alignment between frontend submit contract and `AppBrokerageWithdrawCreateReqVO`

- [ ] **Step 1: Verify type=5 payload matches backend contract**

Read the final `handleSubmit()` and confirm:
- `type` = 5 (int)
- `price` = amount * 100 (int, fen)
- `userAccount` = `wechatOpenid.value` (string, openid)
- `userName` = user-entered name (string, nullable)
- `transferChannelCode` = `PAY_CHANNEL_WX_LITE` which is `'wx_lite'` (string)

Cross-reference with `AppBrokerageWithdrawCreateReqVO.java` lines 20-51:
- `type` must pass `@InEnum(BrokerageWithdrawTypeEnum.class)` — value 5 is `WECHAT_API` ✓
- `price` must pass `@PositiveOrZero` and `@Min(30)` for WechatApi group ✓
- `userAccount` must pass `@NotBlank` for WechatApi group ✓
- `userName` must pass `@NotBlank` for WechatApi group ✓
- `transferChannelCode` must pass `@NotNull` for WechatApi group and `@InEnum(PayChannelEnum.class)` — `'wx_lite'` is in `PayChannelEnum` ✓

- [ ] **Step 2: Verify type=2 (bank) payload**

Confirm bank submit includes: `type=2`, `price`, `userAccount`, `userName`, `bankName`, `bankAddress?`. No `transferChannelCode`. Matches backend Bank validation group. ✓

- [ ] **Step 3: Verify type=3/4 (QR code) payload**

Confirm QR submit includes: `type=3|4`, `price`, `qrCodeUrl`. No `userAccount`, no `transferChannelCode`. Matches backend WechatQR/AlipayQR validation group. ✓

- [ ] **Step 4: Verify type=1 (wallet) payload**

Confirm wallet submit includes only `type=1` and `price`. No extra fields. Matches backend Wallet validation group. ✓

- [ ] **Step 5: Verify type=6 (Alipay API) payload**

Confirm Alipay submit includes: `type=6`, `price`, `userAccount`, `userName`. No `transferChannelCode`. Matches backend AlipayApi validation group. ✓

- [ ] **Step 6: Verify no backend or framework files were modified**

Run: `git diff --name-only`
Expected: only files under `UI/uniapp-marriage/` appear.

- [ ] **Step 7: Final commit if any cleanup was done**

```bash
git add -A
git commit -m "chore: verify marriage withdraw alignment with backend contract"
```
