# Mine Payment Orders Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a lightweight “我的订单” page in `UI/uniapp-marriage`, connect the mine-page `payment` entry to it, and let users continue payment through the existing cashier/result flow.

**Architecture:** Keep the change local to `UI/uniapp-marriage` by adding one focused `order-api.uts` data-access file and one focused `mine-payment/index.uvue` page. Reuse the existing request stack, login checks, `pay-cashier`, and `pay-result` pages instead of importing `uniapp-mall` UI or button logic.

**Tech Stack:** uni-app `uvue`, UTS, existing `request()` wrapper in `utils/http.uts`, existing pay flow in `utils/pay-api.uts`, custom page registration in `pages.json`

## Global Constraints

- Do not import or depend on `UI/uniapp-mall` `sheep` components, hooks, or router helpers.
- Keep all new business code inside `UI/uniapp-marriage`.
- Follow existing `uvue + uts` patterns already used by `member-packages`, `pay-cashier`, and `pay-result`.
- Reuse existing unified order and payment endpoints: `/trade/order/page`, `/accountant/order/get`, `/trade/order/get-detail`.
- First version only includes: mine-page entry wiring, order list tabs, order list display, empty/error states, and continue-payment.
- First version explicitly excludes: order detail page, cancel order, delete order, confirm receipt, after-sale/refund flows, and mall-style full button matrix.
- Keep page visuals aligned with the current marriage app warm-tone style and `navigationStyle: custom` pages.
- Default verification in this repository does not proactively run Maven compile; this feature is front-end only.

---

## File Structure

- Create: `UI/uniapp-marriage/utils/order-api.uts`
  - Owns order-list request types, response unwrapping, and status-filter request construction.
- Create: `UI/uniapp-marriage/pages/mine-payment/index.uvue`
  - Owns mine-payment page UI, tab state, pagination, login guard, empty/error/loading behavior, and continue-payment navigation.
- Modify: `UI/uniapp-marriage/pages/mine/index.uvue`
  - Owns `payment` action handling and mine-page navigation into the new order list.
- Modify: `UI/uniapp-marriage/pages.json`
  - Registers the new page route.

## Task 1: Add focused order list API support

**Files:**
- Create: `UI/uniapp-marriage/utils/order-api.uts`
- Test: none (manual verification through page integration in later tasks)

**Interfaces:**
- Consumes: `request<T>(options: RequestOptions): Promise<T>` from `UI/uniapp-marriage/utils/http.uts`
- Produces:
  - `export type OrderProperty = { valueName: string }`
  - `export type OrderItem = { id: number; picUrl: string; spuName: string; price: number; count: number; properties: OrderProperty[] }`
  - `export type OrderPageItem = { id: number; no: string; status: number; payPrice: number; payOrderId: number; items: OrderItem[] }`
  - `export type OrderPageResult = { list: OrderPageItem[]; total: number }`
  - `export function getOrderPage(pageNo: number, pageSize: number, status: number | null): Promise<OrderPageResult>`

- [ ] **Step 1: Create `UI/uniapp-marriage/utils/order-api.uts` with the complete minimal implementation**

```ts
import { request } from './http.uts'

type CommonResult<T> = {
	code: number
	msg: string
	data: T | null
}

type PageResult<T> = {
	list: T[]
	total: number
}

export type OrderProperty = {
	valueName: string
}

export type OrderItem = {
	id: number
	picUrl: string
	spuName: string
	price: number
	count: number
	properties: OrderProperty[]
}

export type OrderPageItem = {
	id: number
	no: string
	status: number
	payPrice: number
	payOrderId: number
	items: OrderItem[]
}

export type OrderPageResult = {
	list: OrderPageItem[]
	total: number
}

export function getOrderPage(pageNo: number, pageSize: number, status: number | null): Promise<OrderPageResult> {
	let url = '/trade/order/page?pageNo=' + String(pageNo) + '&pageSize=' + String(pageSize)
	if (status != null) {
		url += '&status=' + String(status)
	}
	return request<CommonResult<PageResult<OrderPageItem>>>({
		url,
		method: 'GET'
	}).then((response) => {
		if (response.code != 0 || response.data == null) {
			return Promise.reject(response.msg != '' ? response.msg : '订单加载失败')
		}
		return {
			list: response.data.list != null ? response.data.list : [],
			total: response.data.total || 0
		} as OrderPageResult
	})
}
```

- [ ] **Step 2: Manually inspect the new API file for interface alignment**

Check that the file exports exactly these names for later tasks:
- `OrderProperty`
- `OrderItem`
- `OrderPageItem`
- `OrderPageResult`
- `getOrderPage(pageNo, pageSize, status)`

Expected: all names exist exactly once and `getOrderPage` returns `Promise<OrderPageResult>`.

- [ ] **Step 3: Commit the API scaffold**

```bash
git add UI/uniapp-marriage/utils/order-api.uts
git commit -m "feat(marriage): add mine payment order api

Co-Authored-By: Claude <noreply@anthropic.com>"
```

Expected: one new file committed.

## Task 2: Register the mine-payment page route

**Files:**
- Modify: `UI/uniapp-marriage/pages.json`
- Consumes: new page path `pages/mine-payment/index`
- Produces: routable page entry for `uni.navigateTo({ url: '/pages/mine-payment/index' })`

**Interfaces:**
- Consumes: page-routing conventions already used in `pages/member-packages/index`, `pages/pay-cashier/index`, `pages/pay-result/index`
- Produces: `pages/mine-payment/index` route with `navigationStyle: custom`

- [ ] **Step 1: Add the page registration near the existing mine-related pages**

Insert this page object into the `pages` array in `UI/uniapp-marriage/pages.json` near other `pages/mine-*` entries:

```json
		{
			"path": "pages/mine-payment/index",
			"style": {
				"navigationStyle": "custom"
			}
		},
```

- [ ] **Step 2: Verify the route path matches the planned navigation target**

Check that all three strings are identical after the edit:
- `pages/mine-payment/index` in `pages.json`
- `/pages/mine-payment/index` in future navigation from `mine/index.uvue`
- `/pages/mine-payment/index` in any direct fallback navigation in the new page itself

Expected: no naming mismatch such as `mine-pay`, `my-payment`, or missing `index`.

- [ ] **Step 3: Commit the route registration**

```bash
git add UI/uniapp-marriage/pages.json
git commit -m "feat(marriage): register mine payment page

Co-Authored-By: Claude <noreply@anthropic.com>"
```

Expected: one modified file committed.

## Task 3: Connect the mine-page payment action

**Files:**
- Modify: `UI/uniapp-marriage/pages/mine/index.uvue`
- Test: manual verification from the mine page UI

**Interfaces:**
- Consumes:
  - `isMemberLoggedIn(): boolean` from existing imports in `pages/mine/index.uvue`
  - `loginSheetVisible` state already used by other guarded actions
- Produces:
  - `payment` action handling inside `handleAction(key: string): void`
  - `openMinePaymentPage(): void`

- [ ] **Step 1: Add `payment` handling inside `handleAction`**

In `UI/uniapp-marriage/pages/mine/index.uvue`, insert this block before the generic fallback toast:

```ts
	if (key == 'payment') {
		openMinePaymentPage()
		return
	}
```

- [ ] **Step 2: Add the navigation helper function next to the other `open*Page` helpers**

```ts
function openMinePaymentPage(): void {
	if (!isMemberLoggedIn()) {
		loginSheetVisible.value = true
		return
	}
	uni.navigateTo({
		url: '/pages/mine-payment/index'
	})
}
```

- [ ] **Step 3: Manually verify control flow consistency in the file**

Check that:
- `payment` no longer falls through to the generic “该功能页面待补充” toast
- the new helper follows the same login-guard pattern as `openCheckinPage()` and `openPointsPage()`
- no existing branch for `wallet`, `member-packages`, or `matchmaker` is changed accidentally

Expected: only the `payment` branch behavior changes.

- [ ] **Step 4: Commit the mine entry wiring**

```bash
git add UI/uniapp-marriage/pages/mine/index.uvue
git commit -m "feat(marriage): connect mine payment entry

Co-Authored-By: Claude <noreply@anthropic.com>"
```

Expected: one modified file committed.

## Task 4: Build the lightweight mine-payment page UI and logic

**Files:**
- Create: `UI/uniapp-marriage/pages/mine-payment/index.uvue`
- Consumes:
  - `getOrderPage(pageNo: number, pageSize: number, status: number | null): Promise<OrderPageResult>` from `UI/uniapp-marriage/utils/order-api.uts`
  - `isMemberLoggedIn(): boolean` from `UI/uniapp-marriage/utils/member-auth.uts`
  - `formatPayAmount(price: number): string` and `resolvePayError(error: any, fallback: string): string` from `UI/uniapp-marriage/utils/pay-api.uts`
- Produces:
  - route-backed page at `/pages/mine-payment/index`
  - internal functions `loadOrders(reset: boolean): void`, `changeTab(index: number): void`, `loadMore(): void`, `continuePay(order: OrderPageItem): void`

**Interfaces:**
- Consumes:
  - `OrderPageItem.status` values `0 | 10 | 20 | 30 | other`
  - `OrderPageItem.payOrderId` and `OrderPageItem.id`
- Produces:
  - continue-pay navigation URL `/pages/pay-cashier/index?payOrderId={payOrderId}&tradeOrderId={id}&orderType=product`

- [ ] **Step 1: Create `UI/uniapp-marriage/pages/mine-payment/index.uvue` with the full page implementation**

```vue
<template>
	<view class="mine-payment-page">
		<view class="payment-header">
			<view class="payment-title-row">
				<view class="back-button" @tap="goBack">
					<view class="back-button-arrow"></view>
				</view>
				<text class="payment-title">我的订单</text>
			</view>
			<text class="payment-subtitle">查看订单状态并继续支付</text>
		</view>

		<scroll-view class="tab-scroll" :scroll-x="true" show-scrollbar="false">
			<view class="tab-row">
				<view
					v-for="(tab, index) in tabs"
					:key="'tab-' + index"
					class="tab-item"
					:class="currentTab == index ? 'tab-item-active' : ''"
					@tap="changeTab(index)"
				>
					<text class="tab-text" :class="currentTab == index ? 'tab-text-active' : ''">{{ tab.label }}</text>
				</view>
			</view>
		</scroll-view>

		<scroll-view class="payment-scroll" :scroll-y="true" @scrolltolower="loadMore">
			<view class="payment-content">
				<view v-if="loading && orders.length == 0" class="state-card">
					<text class="state-title">订单加载中</text>
					<text class="state-desc">正在获取你的订单信息</text>
				</view>

				<view v-else-if="orders.length == 0" class="state-card">
					<text class="state-title">暂无订单</text>
					<text class="state-desc">当前筛选条件下还没有订单记录</text>
				</view>

				<view v-for="(order, orderIndex) in orders" :key="'order-' + orderIndex" class="order-card">
					<view class="order-card-header">
						<text class="order-no">订单号：{{ order.no }}</text>
						<text class="order-status" :class="statusClass(order.status)">{{ statusText(order.status) }}</text>
					</view>

					<view v-for="(item, itemIndex) in order.items" :key="'item-' + orderIndex + '-' + itemIndex" class="goods-row">
						<image class="goods-image" :src="item.picUrl" mode="aspectFill"></image>
						<view class="goods-main">
							<text class="goods-name">{{ item.spuName }}</text>
							<text class="goods-sku">{{ skuText(item.properties) }}</text>
							<view class="goods-bottom-row">
								<text class="goods-price">{{ formatPayAmount(item.price) }}</text>
								<text class="goods-count">x{{ item.count }}</text>
							</view>
						</view>
					</view>

					<view class="order-summary-row">
						<text class="summary-text">合计</text>
						<text class="summary-amount">{{ formatPayAmount(order.payPrice) }}</text>
					</view>

					<view class="order-action-row" v-if="canContinuePay(order)">
						<view class="primary-action-button" @tap="continuePay(order)">
							<text class="primary-action-button-text">继续支付</text>
						</view>
					</view>
				</view>

				<view v-if="orders.length > 0" class="load-more-row">
					<text class="load-more-text">{{ loadMoreText }}</text>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script setup lang="uts">
import { isMemberLoggedIn } from '../../utils/member-auth.uts'
import {
	getOrderPage,
	type OrderPageItem,
	type OrderProperty
} from '../../utils/order-api.uts'
import {
	formatPayAmount,
	resolvePayError
} from '../../utils/pay-api.uts'

type PaymentTab = {
	label: string
	status: number | null
}

const tabs: PaymentTab[] = [
	{ label: '全部', status: null },
	{ label: '待付款', status: 0 },
	{ label: '待发货', status: 10 },
	{ label: '待收货', status: 20 },
	{ label: '待评价', status: 30 }
]

const currentTab = ref(0)
const orders = ref<OrderPageItem[]>([])
const loading = ref(false)
const pageNo = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)

const loadMoreText = computed(() => {
	if (loading.value && orders.value.length > 0) {
		return '加载中...'
	}
	if (!hasMore.value) {
		return '没有更多订单了'
	}
	return '上拉加载更多'
})

onLoad(() => {
	if (!isMemberLoggedIn()) {
		uni.showToast({
			title: '请先登录',
			icon: 'none'
		})
		setTimeout(() => {
			goMine()
		}, 600)
		return
	}
	loadOrders(true)
})

function loadOrders(reset: boolean): void {
	if (loading.value) {
		return
	}
	if (reset) {
		pageNo.value = 1
		hasMore.value = true
	}
	if (!hasMore.value) {
		return
	}
	loading.value = true
	const currentStatus = tabs[currentTab.value].status
	getOrderPage(pageNo.value, pageSize.value, currentStatus).then((result) => {
		const nextList = result.list
		if (reset) {
			orders.value = nextList
		} else {
			orders.value = orders.value.concat(nextList)
		}
		const loadedCount = orders.value.length
		hasMore.value = loadedCount < result.total && nextList.length >= pageSize.value
		if (hasMore.value) {
			pageNo.value = pageNo.value + 1
		}
	}).catch((error) => {
		uni.showToast({
			title: resolvePayError(error, reset ? '订单加载失败' : '加载失败，请稍后重试'),
			icon: 'none'
		})
	}).finally(() => {
		loading.value = false
	})
}

function changeTab(index: number): void {
	if (currentTab.value == index) {
		return
	}
	currentTab.value = index
	loadOrders(true)
}

function loadMore(): void {
	if (loading.value || !hasMore.value) {
		return
	}
	loadOrders(false)
}

function canContinuePay(order: OrderPageItem): boolean {
	return order.status == 0 && order.payOrderId > 0
}

function continuePay(order: OrderPageItem): void {
	if (!canContinuePay(order)) {
		return
	}
	const url = '/pages/pay-cashier/index?payOrderId=' + String(order.payOrderId)
		+ '&tradeOrderId=' + String(order.id)
		+ '&orderType=product'
	uni.navigateTo({
		url,
		fail: () => {
			uni.redirectTo({
				url,
				fail: () => {
					uni.showToast({ title: '页面跳转失败，请重试', icon: 'none' })
				}
			})
		}
	})
}

function skuText(properties: OrderProperty[]): string {
	if (properties.length == 0) {
		return '默认规格'
	}
	return properties.map((property) => property.valueName).join(' ')
}

function statusText(status: number): string {
	if (status == 0) return '待付款'
	if (status == 10) return '待发货'
	if (status == 20) return '待收货'
	if (status == 30) return '待评价'
	if (status == 40) return '已完成'
	return '订单处理中'
}

function statusClass(status: number): string {
	if (status == 0) return 'order-status-waiting'
	if (status == 10 || status == 20) return 'order-status-progress'
	if (status == 30 || status == 40) return 'order-status-done'
	return 'order-status-default'
}

function goBack(): void {
	uni.navigateBack({
		delta: 1,
		fail: () => {
			goMine()
		}
	})
}

function goMine(): void {
	uni.switchTab({
		url: '/pages/mine/index',
		fail: () => {
			uni.reLaunch({
				url: '/pages/mine/index',
				fail: () => {
					uni.showToast({ title: '页面跳转失败，请重试', icon: 'none' })
				}
			})
		}
	})
}
</script>

<style>
.mine-payment-page {
	flex: 1;
	background-color: #fcf7f3;
	padding-top: calc(var(--status-bar-height) + 18px);
}

.payment-header {
	padding-left: 16px;
	padding-right: 16px;
	margin-bottom: 16px;
}

.payment-title-row {
	min-height: 38px;
	flex-direction: row;
	align-items: center;
	margin-bottom: 4px;
}

.back-button {
	width: 34px;
	height: 34px;
	border-radius: 17px;
	background-color: #f6ebe6;
	border-width: 1px;
	border-color: rgba(214, 198, 190, 0.72);
	justify-content: center;
	align-items: center;
	margin-right: 10px;
}

.back-button-arrow {
	width: 10px;
	height: 10px;
	border-left-width: 2px;
	border-bottom-width: 2px;
	border-left-color: #342321;
	border-bottom-color: #342321;
	border-style: solid;
	transform: rotate(45deg);
	margin-left: 4px;
}

.payment-title {
	font-size: 24px;
	line-height: 32px;
	font-weight: 800;
	color: #241818;
	margin-bottom: 8px;
}

.payment-subtitle {
	font-size: 14px;
	line-height: 22px;
	color: #705c58;
}

.tab-scroll {
	white-space: nowrap;
	margin-bottom: 12px;
}

.tab-row {
	flex-direction: row;
	padding-left: 16px;
	padding-right: 16px;
}

.tab-item {
	padding-left: 14px;
	padding-right: 14px;
	height: 34px;
	border-radius: 17px;
	background-color: #f6ebe6;
	justify-content: center;
	align-items: center;
	margin-right: 10px;
	border-width: 1px;
	border-color: rgba(214, 198, 190, 0.5);
}

.tab-item-active {
	background-color: #ef655f;
	border-color: #ef655f;
}

.tab-text {
	font-size: 13px;
	color: #6f5d58;
}

.tab-text-active {
	color: #ffffff;
	font-weight: 700;
}

.payment-scroll {
	flex: 1;
}

.payment-content {
	padding-left: 16px;
	padding-right: 16px;
	padding-bottom: 28px;
}

.state-card,
.order-card {
	border-radius: 16px;
	background-color: #FFFBF8;
	border-width: 1px;
	border-color: rgba(214, 198, 190, 0.62);
	padding: 16px;
	margin-bottom: 14px;
}

.state-title {
	font-size: 17px;
	line-height: 24px;
	font-weight: 700;
	color: #241818;
	margin-bottom: 6px;
}

.state-desc {
	font-size: 13px;
	line-height: 20px;
	color: #705c58;
}

.order-card-header {
	flex-direction: row;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 14px;
}

.order-no {
	flex: 1;
	font-size: 13px;
	line-height: 20px;
	color: #705c58;
	margin-right: 10px;
}

.order-status {
	font-size: 13px;
	font-weight: 700;
}

.order-status-waiting {
	color: #ef655f;
}

.order-status-progress {
	color: #d48d23;
}

.order-status-done {
	color: #4f8a5b;
}

.order-status-default {
	color: #705c58;
}

.goods-row {
	flex-direction: row;
	padding-top: 12px;
	padding-bottom: 12px;
	border-top-width: 1px;
	border-top-color: rgba(125, 95, 88, 0.08);
}

.goods-image {
	width: 76px;
	height: 76px;
	border-radius: 12px;
	background-color: #f2ebe6;
	margin-right: 12px;
}

.goods-main {
	flex: 1;
	justify-content: space-between;
}

.goods-name {
	font-size: 15px;
	line-height: 22px;
	font-weight: 700;
	color: #241818;
}

.goods-sku {
	font-size: 12px;
	line-height: 18px;
	color: #8c7a75;
	margin-top: 4px;
}

.goods-bottom-row {
	flex-direction: row;
	justify-content: space-between;
	align-items: center;
	margin-top: 10px;
}

.goods-price {
	font-size: 15px;
	font-weight: 700;
	color: #ef655f;
}

.goods-count {
	font-size: 13px;
	color: #705c58;
}

.order-summary-row {
	flex-direction: row;
	justify-content: flex-end;
	align-items: center;
	margin-top: 8px;
}

.summary-text {
	font-size: 13px;
	color: #705c58;
	margin-right: 6px;
}

.summary-amount {
	font-size: 18px;
	font-weight: 800;
	color: #241818;
}

.order-action-row {
	flex-direction: row;
	justify-content: flex-end;
	margin-top: 14px;
}

.primary-action-button {
	min-width: 96px;
	height: 38px;
	padding-left: 18px;
	padding-right: 18px;
	border-radius: 19px;
	background-color: #ef655f;
	justify-content: center;
	align-items: center;
}

.primary-action-button-text {
	font-size: 14px;
	font-weight: 700;
	color: #ffffff;
}

.load-more-row {
	align-items: center;
	padding-top: 2px;
	padding-bottom: 10px;
}

.load-more-text {
	font-size: 12px;
	line-height: 18px;
	color: #8c7a75;
}
</style>
```

- [ ] **Step 2: Verify the page uses only local marriage-app dependencies**

Check imports in `UI/uniapp-marriage/pages/mine-payment/index.uvue`.

Expected imports are limited to:
- `../../utils/member-auth.uts`
- `../../utils/order-api.uts`
- `../../utils/pay-api.uts`

Expected: no `@/sheep`, no mall hooks, no mall components, no mall router helpers.

- [ ] **Step 3: Manually verify page logic against the spec**

Check that the page includes all first-version requirements:
- custom header with back button
- tabs: 全部 / 待付款 / 待发货 / 待收货 / 待评价
- order list card fields: order no, status, items, total amount
- empty state
- load-more state
- login guard
- continue-payment only for payable orders

Expected: all listed requirements appear directly in this one page file.

- [ ] **Step 4: Commit the page implementation**

```bash
git add UI/uniapp-marriage/pages/mine-payment/index.uvue
git commit -m "feat(marriage): add mine payment orders page

Co-Authored-By: Claude <noreply@anthropic.com>"
```

Expected: one new page file committed.

## Task 5: Verify the end-to-end order-entry flow manually

**Files:**
- Modify: none unless issues are found
- Test: manual app verification for `mine -> payment -> order list -> continue pay`

**Interfaces:**
- Consumes:
  - mine-page `payment` action
  - route `/pages/mine-payment/index`
  - `continuePay(order)` URL contract
- Produces:
  - validated user flow across mine page, order list, and cashier handoff

- [ ] **Step 1: Launch the marriage app in the usual local way for this project**

Use the project’s normal uni-app run flow that you already use for `UI/uniapp-marriage`.

Expected: app opens to a state where you can sign in and reach the “我的” tab.

- [ ] **Step 2: Verify the mine-page entry behavior**

Manual checks:
- Open the “我的” tab
- Tap “我的支付” while logged out
- Confirm the existing login sheet appears instead of the generic placeholder toast
- Log in, tap “我的支付” again

Expected: navigation enters `/pages/mine-payment/index` successfully.

- [ ] **Step 3: Verify the order list states**

Manual checks on the new page:
- Confirm header and tab strip render
- Confirm tab switching reloads data without crashing
- Confirm empty state says `暂无订单` when no records exist for a filter
- Confirm at least one order card, if available, shows order no, goods info, and total amount

Expected: page remains usable for both empty and populated tabs.

- [ ] **Step 4: Verify continue-payment handoff**

If a `status == 0` order exists:
- Tap `继续支付`
- Confirm the existing cashier page opens
- Confirm the cashier page shows the order amount and can continue the existing payment flow
- Confirm any return path still lands on existing `pay-result` behavior

If no pending order exists:
- Record that continue-payment UI could not be exercised with current local data
- Still verify that non-payable orders do not incorrectly show the button

Expected: no custom cashier logic was needed; the existing page handles the route.

- [ ] **Step 5: Commit only if verification required fixes**

If you changed code during verification:

```bash
git add UI/uniapp-marriage/pages/mine/index.uvue UI/uniapp-marriage/pages/mine-payment/index.uvue UI/uniapp-marriage/pages.json UI/uniapp-marriage/utils/order-api.uts
git commit -m "fix(marriage): polish mine payment orders flow

Co-Authored-By: Claude <noreply@anthropic.com>"
```

If no code changed, do not create an extra commit.

## Spec Coverage Check

- Mine-page `payment` entry becomes a real route: Task 2 + Task 3
- Lightweight marriage-only order API wrapper: Task 1
- Lightweight order-list page with warm-tone custom UI: Task 4
- Tabs, empty state, load-more, login guard, and continue payment: Task 4
- Reuse of existing cashier/result flow instead of mall UI: Task 4 + Task 5
- Explicit first-version scope without detail/cancel/delete/after-sale: enforced by Task 4 implementation boundaries

## Placeholder Scan

- No `TODO`, `TBD`, or “similar to Task N” placeholders remain.
- All produced names and routes are spelled out exactly.
- All code-changing tasks include concrete code blocks.

## Type Consistency Check

- `getOrderPage(pageNo, pageSize, status)` is introduced in Task 1 and consumed with the same signature in Task 4.
- `OrderPageItem` from Task 1 is consumed consistently in Task 4.
- Route path `pages/mine-payment/index` is used consistently across Task 2, Task 3, and Task 4.
- Continue-payment route parameters `payOrderId`, `tradeOrderId`, and `orderType=product` are consistent with the existing cashier/result page expectations.
