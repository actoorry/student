# 红娘服务专属页 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新增一个“红娘服务”专属页，先展示平台官方风服务说明，再通过展示配置读取“红娘服务”分类并展示该分类下全部商品，商品继续复用通用商品详情页。

**Architecture:** 保持 `UI/uniapp-marriage` 现有专题页模式：新增一个页面文件承载专题内容与状态，一个页面级 API 文件负责展示配置与商品查询，再在 `pages.json` 和 `pages/mine/index.uvue` 中接入路由与入口。商品卡片复用 `components/product-showcase-card.uvue`，商品详情继续走 `pages/product-detail/index.uvue`，不改交易链路。

**Tech Stack:** uni-app uvue、UTS、现有 `request` HTTP 封装、`ProductShowcaseCard` 组件、`pages.json` 自定义导航路由

## Global Constraints

- 仅修改 `UI/uniapp-marriage` 前端专题页相关代码，不新增后端链路。
- 页面整体风格必须保持“平台官方风”，先讲服务，再上商品。
- 商品来源必须通过 `/product/display-config/get?sceneCode=...` 读取配置，固定场景码使用 `matchmaker_service`。
- 商品列表必须展示“红娘服务”分类下全部商品，不做二次分组、排序器或复杂筛选。
- 商品卡片复用 `UI/uniapp-marriage/components/product-showcase-card.uvue`。
- 商品详情跳转必须继续使用 `UI/uniapp-marriage/pages/product-detail/index.uvue`。
- 页面必须覆盖加载态、未配置、无商品、加载失败四类状态，并提供重试入口。
- 保持现有 uni-app 页面模式：`navigationStyle: custom`，返回逻辑与专题页一致。
- 默认不主动运行 Maven 编译；本次工作仅涉及 uni-app 前端，不新增 Java 改动。

---

## File Structure

- Create: `UI/uniapp-marriage/utils/matchmaker-service-api.uts`
  - 页面级 API：定义红娘服务页复用的类型、场景码、展示配置请求、商品列表查询与详情补全。
- Create: `UI/uniapp-marriage/pages/matchmaker-service/index.uvue`
  - 红娘服务专题页：静态官方风内容、商品列表、加载/空/错误状态、重试与详情跳转。
- Modify: `UI/uniapp-marriage/pages.json`
  - 注册 `pages/matchmaker-service/index` 页面，保持 `navigationStyle: custom`。
- Modify: `UI/uniapp-marriage/pages/mine/index.uvue`
  - 将现有 `matchmaker` 入口从占位 toast 改成真实页面跳转。

## Task 1: Build the matchmaker service data module

**Files:**
- Create: `UI/uniapp-marriage/utils/matchmaker-service-api.uts`
- Modify: `UI/uniapp-marriage/utils/course-api.uts:93-177`（仅阅读参考，不在本任务中修改）
- Test: `UI/uniapp-marriage/utils/matchmaker-service-api.uts`（通过静态结构检查与调用点接入验证）

**Interfaces:**
- Consumes: `request<T>(options)` from `UI/uniapp-marriage/utils/http.uts`
- Produces:
  - `export const MATCHMAKER_SERVICE_SCENE_CODE: string`
  - `export type MatchmakerServiceSpu = { id: number; name: string; introduction: string; picUrl: string; price: number; marketPrice: number; stock: number; salesCount: number; skus: MatchmakerServiceSku[] }`
  - `export function getMatchmakerServiceDisplayConfig(sceneCode: string): Promise<ProductDisplayConfig>`
  - `export function getMatchmakerServicePage(categorySales: number): Promise<MatchmakerServiceSpu[]>`

- [ ] **Step 1: Write the failing test**

Create the new file with only the shared types and the exported function signatures that intentionally return rejected promises. This gives the page task something concrete to import while making data loading fail until the implementation is added.

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

export type ProductDisplayConfig = {
	sceneCode: string
	categoryIds: number[]
}

export type MatchmakerServiceSkuProperty = {
	propertyId: number
	propertyName: string
	valueId: number
	valueName: string
}

export type MatchmakerServiceSku = {
	id: number
	properties: MatchmakerServiceSkuProperty[]
	price: number
	marketPrice: number
	stock: number
}

export type MatchmakerServiceSpu = {
	id: number
	name: string
	introduction: string
	picUrl: string
	price: number
	marketPrice: number
	stock: number
	salesCount: number
	skus: MatchmakerServiceSku[]
}

export const MATCHMAKER_SERVICE_SCENE_CODE = 'matchmaker_service'

export function getMatchmakerServiceDisplayConfig(sceneCode: string): Promise<ProductDisplayConfig> {
	return Promise.reject('not implemented')
}

export function getMatchmakerServicePage(categorySales: number): Promise<MatchmakerServiceSpu[]> {
	return Promise.reject('not implemented')
}
```

- [ ] **Step 2: Run test to verify it fails**

Run a static grep to confirm the placeholder implementation is still present.

Run: `rg -n "not implemented|MATCHMAKER_SERVICE_SCENE_CODE" UI/uniapp-marriage/utils/matchmaker-service-api.uts`

Expected:
- A hit for `MATCHMAKER_SERVICE_SCENE_CODE = 'matchmaker_service'`
- A hit for `Promise.reject('not implemented')`

- [ ] **Step 3: Write minimal implementation**

Replace the placeholder bodies with the real page-level API implementation. Follow the same sequential detail-fetch pattern used by `course-api.uts` so the card receives the full `salesCount` and `skus` data it expects.

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

export type ProductDisplayConfig = {
	sceneCode: string
	categoryIds: number[]
}

export type MatchmakerServiceSkuProperty = {
	propertyId: number
	propertyName: string
	valueId: number
	valueName: string
}

export type MatchmakerServiceSku = {
	id: number
	properties: MatchmakerServiceSkuProperty[]
	price: number
	marketPrice: number
	stock: number
}

export type MatchmakerServiceSpu = {
	id: number
	name: string
	introduction: string
	picUrl: string
	price: number
	marketPrice: number
	stock: number
	salesCount: number
	skus: MatchmakerServiceSku[]
}

type ProductSpuPageItem = {
	id: number
	name: string
	introduction: string
	picUrl: string
	price: number
	marketPrice: number
	stock: number
	salesCount: number
}

export const MATCHMAKER_SERVICE_SCENE_CODE = 'matchmaker_service'

export function getMatchmakerServiceDisplayConfig(sceneCode: string): Promise<ProductDisplayConfig> {
	return request<CommonResult<ProductDisplayConfig>>({
		url: '/product/display-config/get?sceneCode=' + encodeURIComponent(sceneCode),
		method: 'GET',
		withAuth: false
	}).then((response) => {
		if (response.code !== 0 || response.data == null) {
			return Promise.reject(response.msg != '' ? response.msg : '获取红娘服务展示配置失败')
		}
		return response.data
	})
}

export function getMatchmakerServicePage(categorySales: number): Promise<MatchmakerServiceSpu[]> {
	return request<CommonResult<PageResult<ProductSpuPageItem>>>({
		url: '/product/spu/page?pageNo=1&pageSize=20&categorySales=' + String(categorySales),
		method: 'GET',
		withAuth: false
	}).then((response) => {
		if (response.code !== 0 || response.data == null) {
			return Promise.reject(response.msg != '' ? response.msg : '获取红娘服务商品失败')
		}
		const items = response.data.list
		const result = [] as MatchmakerServiceSpu[]
		let chain = Promise.resolve()
		items.forEach((item) => {
			chain = chain.then(() => {
				return getMatchmakerServiceDetail(item.id).then((detail) => {
					result.push(detail)
				})
			})
		})
		return chain.then(() => result)
	})
}

function getMatchmakerServiceDetail(id: number): Promise<MatchmakerServiceSpu> {
	return request<CommonResult<MatchmakerServiceSpu>>({
		url: '/product/spu/get-detail?id=' + String(id),
		method: 'GET',
		withAuth: false
	}).then((response) => {
		if (response.code !== 0 || response.data == null) {
			return Promise.reject(response.msg != '' ? response.msg : '获取红娘服务详情失败')
		}
		return response.data
	})
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `rg -n "获取红娘服务展示配置失败|获取红娘服务商品失败|getMatchmakerServicePage|getMatchmakerServiceDetail" UI/uniapp-marriage/utils/matchmaker-service-api.uts`

Expected:
- One match for each failure message
- One match for `getMatchmakerServicePage`
- One match for `getMatchmakerServiceDetail`
- No matches for `not implemented`

- [ ] **Step 5: Commit**

```bash
git add UI/uniapp-marriage/utils/matchmaker-service-api.uts
git commit -m "feat: add matchmaker service data module"
```

## Task 2: Build the dedicated matchmaker service page and register its route

**Files:**
- Create: `UI/uniapp-marriage/pages/matchmaker-service/index.uvue`
- Modify: `UI/uniapp-marriage/pages.json:44-79`
- Modify: `UI/uniapp-marriage/pages/activity/index.uvue:1-237`（仅阅读参考，不在本任务中修改）
- Modify: `UI/uniapp-marriage/pages/emotion-academy/index.uvue:1-189`（仅阅读参考，不在本任务中修改）
- Test: `UI/uniapp-marriage/pages/matchmaker-service/index.uvue`

**Interfaces:**
- Consumes:
  - `MATCHMAKER_SERVICE_SCENE_CODE: string`
  - `getMatchmakerServiceDisplayConfig(sceneCode: string): Promise<ProductDisplayConfig>`
  - `getMatchmakerServicePage(categorySales: number): Promise<MatchmakerServiceSpu[]>`
  - `ProductShowcaseCard`
- Produces:
  - A routable page at `/pages/matchmaker-service/index`
  - `function loadServices(): void`
  - `function openDetail(spuId: number): void`
  - `function retryLoad(): void`
  - `function goBack(): void`

- [ ] **Step 1: Write the failing test**

Create the page and route entry with minimal placeholders so route wiring can fail loudly before the real UI is implemented.

`UI/uniapp-marriage/pages/matchmaker-service/index.uvue`

```vue
<template>
	<view class="matchmaker-page">
		<text class="matchmaker-title">红娘服务</text>
		<text class="matchmaker-placeholder">页面建设中</text>
	</view>
</template>

<script setup lang="uts">
</script>

<style>
.matchmaker-page {
	flex: 1;
}

.matchmaker-title {
	font-size: 24px;
}
</style>
```

`UI/uniapp-marriage/pages.json`

```json
{
	"path": "pages/matchmaker-service/index",
	"style": {
		"navigationStyle": "custom"
	}
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `rg -n "页面建设中|pages/matchmaker-service/index" UI/uniapp-marriage/pages/matchmaker-service/index.uvue UI/uniapp-marriage/pages.json`

Expected:
- One match for `页面建设中`
- One match for `pages/matchmaker-service/index`
- No matches yet for `getMatchmakerServiceDisplayConfig` or `ProductShowcaseCard`

- [ ] **Step 3: Write minimal implementation**

Replace the placeholder page with the full dedicated page. Keep the layout self-contained: back button, official intro card, three service info blocks, product section title, loading/empty/error card with retry button, product list, and a bottom note.

```vue
<template>
	<view class="matchmaker-page">
		<view class="matchmaker-topbar">
			<view class="topbar-back" @tap="goBack">
				<view class="topbar-back-arrow"></view>
				<text class="topbar-back-text">返回</text>
			</view>
		</view>

		<view class="hero-card">
			<text class="hero-title">红娘服务</text>
			<text class="hero-subtitle">平台提供规范化婚恋顾问与牵线服务，帮助用户在真实、安心的前提下提升匹配效率。</text>
			<view class="hero-tags">
				<view v-for="(item, index) in trustTags" :key="'trust-' + index" class="hero-tag">
					<text class="hero-tag-text">{{ item }}</text>
				</view>
			</view>
		</view>

		<view class="info-card">
			<text class="info-card-title">服务保障</text>
			<text class="info-card-desc">平台围绕实名认证、资料审核、顾问跟进与服务记录建立规范化服务流程，提升沟通效率与服务可靠性。</text>
		</view>
		<view class="info-card">
			<text class="info-card-title">服务流程</text>
			<text class="info-card-desc">从需求沟通、顾问评估、匹配推荐到后续跟进，平台按照既定流程推进服务安排，帮助用户更清晰地理解服务节奏。</text>
		</view>
		<view class="info-card">
			<text class="info-card-title">适合人群</text>
			<text class="info-card-desc">适合希望获得更明确婚恋建议、提升匹配效率、期待平台顾问提供阶段性协助与牵线服务的用户。</text>
		</view>

		<view class="section-header">
			<text class="section-title">可选服务</text>
			<text class="section-subtitle">以下商品均来自“红娘服务”分类配置</text>
		</view>

		<view v-if="loading" class="state-card">
			<text class="state-title">红娘服务加载中</text>
			<text class="state-desc">正在读取红娘服务展示配置与商品列表。</text>
		</view>

		<view v-else-if="services.length == 0" class="state-card">
			<text class="state-title">{{ stateTitle }}</text>
			<text class="state-desc">{{ stateDesc }}</text>
			<view v-if="showRetry" class="retry-button" @tap="retryLoad">
				<text class="retry-button-text">重新加载</text>
			</view>
		</view>

		<ProductShowcaseCard
			v-for="(spu, spuIndex) in services"
			:key="'matchmaker-' + spuIndex"
			:product="spu"
			badgeText="红娘服务"
			detailText="查看服务"
			salesPrefix="已服务 "
			:benefitTags="serviceBenefitTags"
			@detail="openDetail"
		/>

		<view class="bottom-note">
			<text class="bottom-note-text">具体服务内容、权益范围与适用条件以商品详情说明为准。</text>
		</view>
	</view>
</template>

<script setup lang="uts">
import ProductShowcaseCard from '../../components/product-showcase-card.uvue'
import {
	MATCHMAKER_SERVICE_SCENE_CODE,
	getMatchmakerServiceDisplayConfig,
	getMatchmakerServicePage,
	type MatchmakerServiceSpu
} from '../../utils/matchmaker-service-api.uts'

const trustTags = ['实名审核', '顾问跟进', '流程规范'] as string[]
const serviceBenefitTags = ['官方服务流程', '顾问协助跟进', '统一商品支付'] as string[]
const services = ref<MatchmakerServiceSpu[]>([])
const loading = ref(true)
const stateTitle = ref('暂无可选红娘服务')
const stateDesc = ref('当前分类下暂无已上架商品，请稍后再来查看')
const showRetry = ref(false)

onLoad(() => {
	loadServices()
})

function loadServices(): void {
	loading.value = true
	showRetry.value = false
	uni.showLoading({ title: '加载中...', mask: true })
	getMatchmakerServiceDisplayConfig(MATCHMAKER_SERVICE_SCENE_CODE).then((config) => {
		if (config.categoryIds.length == 0) {
			stateTitle.value = '暂未配置红娘服务'
			stateDesc.value = '请在商品展示配置中绑定“红娘服务”分类后再试'
			return [] as MatchmakerServiceSpu[]
		}
		return getMatchmakerServicePage(config.categoryIds[0])
	}).then((list) => {
		uni.hideLoading()
		loading.value = false
		services.value = list
		if (list.length == 0) {
			stateTitle.value = '暂无可选红娘服务'
			stateDesc.value = '当前分类下暂无已上架商品，请稍后再来查看'
		}
	}).catch(() => {
		uni.hideLoading()
		loading.value = false
		services.value = []
		stateTitle.value = '加载失败'
		stateDesc.value = '网络异常或服务暂不可用，请稍后重试'
		showRetry.value = true
		uni.showToast({
			title: '红娘服务加载失败',
			icon: 'none'
		})
	})
}

function retryLoad(): void {
	loadServices()
}

function openDetail(spuId: number): void {
	uni.navigateTo({
		url: '/pages/product-detail/index?id=' + String(spuId),
		fail: () => {
			uni.redirectTo({
				url: '/pages/product-detail/index?id=' + String(spuId)
			})
		}
	})
}

function goBack(): void {
	uni.navigateBack({
		delta: 1,
		fail: () => {
			uni.switchTab({
				url: '/pages/mine/index',
				fail: () => {
					uni.reLaunch({
						url: '/pages/index/index'
					})
				}
			})
		}
	})
}
</script>

<style>
.matchmaker-page {
	flex: 1;
	background-color: #FCF7F3;
	padding: calc(var(--status-bar-height) + 18px) 16px 28px;
}

.matchmaker-topbar {
	min-height: 38px;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 12px;
}

.topbar-back {
	height: 34px;
	border-radius: 17px;
	background-color: #FFFBF8;
	border-width: 1px;
	border-color: rgba(217, 203, 195, 0.72);
	padding-left: 12px;
	padding-right: 14px;
	flex-direction: row;
	align-items: center;
}

.topbar-back-arrow {
	width: 9px;
	height: 9px;
	border-left-width: 2px;
	border-bottom-width: 2px;
	border-left-color: #342321;
	border-bottom-color: #342321;
	border-style: solid;
	transform: rotate(45deg);
	margin-right: 6px;
}

.topbar-back-text {
	font-size: 13px;
	font-weight: 700;
	color: #342321;
}

.hero-card,
.info-card,
.state-card,
.bottom-note {
	border-radius: 16px;
	background-color: #FFFBF8;
	border-width: 1px;
	border-color: rgba(217, 203, 195, 0.72);
	margin-bottom: 14px;
	padding: 18px;
}

.hero-title {
	font-size: 26px;
	line-height: 34px;
	font-weight: 800;
	color: #1F1A17;
	margin-bottom: 8px;
}

.hero-subtitle,
.info-card-desc,
.state-desc,
.bottom-note-text,
.section-subtitle {
	font-size: 14px;
	line-height: 22px;
	color: #7F6C66;
}

.hero-tags {
	flex-direction: row;
	flex-wrap: wrap;
	margin-top: 12px;
}

.hero-tag {
	height: 28px;
	border-radius: 14px;
	background-color: #F5E9E4;
	padding-left: 10px;
	padding-right: 10px;
	justify-content: center;
	align-items: center;
	margin-right: 8px;
	margin-bottom: 8px;
}

.hero-tag-text {
	font-size: 12px;
	font-weight: 700;
	color: #8A514D;
}

.info-card-title,
.state-title,
.section-title {
	font-size: 17px;
	line-height: 24px;
	font-weight: 700;
	color: #1F1A17;
	margin-bottom: 6px;
}

.section-header {
	margin-bottom: 14px;
}

.retry-button {
	height: 34px;
	border-radius: 17px;
	background-color: #C84449;
	padding-left: 16px;
	padding-right: 16px;
	justify-content: center;
	align-items: center;
	margin-top: 14px;
	align-self: flex-start;
}

.retry-button-text {
	font-size: 13px;
	font-weight: 700;
	color: #FFFFFF;
}
</style>
```

Also register the route near the other专题页 entries in `pages.json`.

```json
{
	"path": "pages/matchmaker-service/index",
	"style": {
		"navigationStyle": "custom"
	}
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `rg -n "MATCHMAKER_SERVICE_SCENE_CODE|ProductShowcaseCard|重新加载|pages/matchmaker-service/index" UI/uniapp-marriage/pages/matchmaker-service/index.uvue UI/uniapp-marriage/pages.json`

Expected:
- One import of `MATCHMAKER_SERVICE_SCENE_CODE`
- One import/use of `ProductShowcaseCard`
- One `重新加载` retry button
- One route entry for `pages/matchmaker-service/index`
- No match for `页面建设中`

- [ ] **Step 5: Commit**

```bash
git add UI/uniapp-marriage/pages/matchmaker-service/index.uvue UI/uniapp-marriage/pages.json
git commit -m "feat: add matchmaker service page"
```

## Task 3: Wire the mine entry to the real matchmaker service page

**Files:**
- Modify: `UI/uniapp-marriage/pages/mine/index.uvue:362-450`
- Modify: `UI/uniapp-marriage/components/mine-profile-actions.uvue:94-123`（仅阅读确认入口 key 为 `matchmaker`，不在本任务中修改）
- Test: `UI/uniapp-marriage/pages/mine/index.uvue`

**Interfaces:**
- Consumes:
  - `emitAction('matchmaker')` from `mine-profile-actions.uvue`
  - Route path `/pages/matchmaker-service/index`
- Produces:
  - `function openMatchmakerServicePage(): void`
  - Updated action branch so `key == 'matchmaker'` opens the page instead of showing `showVipHint()`

- [ ] **Step 1: Write the failing test**

Keep the current failing behavior visible by locating the placeholder branch before editing.

Relevant current code:

```ts
if (key == 'wallet' || key == 'matchmaker') {
	showVipHint()
	return
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `rg -n "wallet' \|\| key == 'matchmaker'|showVipHint\(|goToMemberPackages\(|openOfflineActivityPage\(" UI/uniapp-marriage/pages/mine/index.uvue`

Expected:
- A match showing `key == 'wallet' || key == 'matchmaker'`
- A match showing `showVipHint()`
- No match yet for `openMatchmakerServicePage`

- [ ] **Step 3: Write minimal implementation**

Split the placeholder branch so wallet keeps the toast, while matchmaker gets a real navigation function.

```ts
if (key == 'wallet') {
	showVipHint()
	return
}

if (key == 'matchmaker') {
	openMatchmakerServicePage()
	return
}
```

Add the new function near the other page-open helpers:

```ts
function openMatchmakerServicePage(): void {
	uni.navigateTo({
		url: '/pages/matchmaker-service/index'
	})
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `rg -n "openMatchmakerServicePage|pages/matchmaker-service/index|key == 'matchmaker'" UI/uniapp-marriage/pages/mine/index.uvue`

Expected:
- One `if (key == 'matchmaker')` branch
- One `openMatchmakerServicePage()` call
- One `url: '/pages/matchmaker-service/index'`
- No remaining `key == 'wallet' || key == 'matchmaker'` combined branch

- [ ] **Step 5: Commit**

```bash
git add UI/uniapp-marriage/pages/mine/index.uvue
git commit -m "feat: connect mine matchmaker entry"
```

## Task 4: Verify the integrated user flow manually

**Files:**
- Modify: none
- Test: `UI/uniapp-marriage/pages/matchmaker-service/index.uvue`, `UI/uniapp-marriage/pages/mine/index.uvue`, `UI/uniapp-marriage/pages.json`, `UI/uniapp-marriage/utils/matchmaker-service-api.uts`

**Interfaces:**
- Consumes:
  - Mine action key `matchmaker`
  - Route `/pages/matchmaker-service/index`
  - Scene code `matchmaker_service`
  - Product detail route `/pages/product-detail/index?id={spuId}`
- Produces:
  - A verified navigation flow from mine page → matchmaker page → product detail
  - Verified copy/state behavior for loading, unconfigured, empty, and error cases

- [ ] **Step 1: Write the failing test**

Prepare a manual verification checklist in your working notes before launching the app so failure is obvious if any step is missing.

```md
- Mine page “红娘服务”入口仍然弹 toast：FAIL
- 红娘服务页没有官方风介绍卡：FAIL
- 配置为空时没有显示“暂未配置红娘服务”：FAIL
- 请求失败时没有“重新加载”按钮：FAIL
- 商品点击不能进入 /pages/product-detail/index?id=...：FAIL
```

- [ ] **Step 2: Run test to verify it fails**

Use the app runner for a real check.

Run: `cd UI/uniapp-marriage && npm run dev:h5`

Expected before the implementation is complete:
- App cannot satisfy the checklist above
- At least one of the listed failure conditions is observable in the browser or simulator

- [ ] **Step 3: Write minimal implementation**

No new code in this task. Instead, complete the real-user verification loop after Tasks 1-3 are merged:

```md
1. Open the mine page and tap “红娘服务”.
2. Confirm the app navigates to `/pages/matchmaker-service/index`.
3. Confirm the first screen shows the official intro card and three service info blocks.
4. With a valid `matchmaker_service` config, confirm the page renders one or more `ProductShowcaseCard` items.
5. Tap a product card and confirm navigation to `/pages/product-detail/index?id=<spuId>`.
6. Temporarily test a missing-config environment and confirm the page shows “暂未配置红娘服务”.
7. Temporarily test an empty-category environment and confirm the page shows “暂无可选红娘服务”.
8. Temporarily simulate request failure and confirm the page shows “加载失败” plus the “重新加载” button.
```

- [ ] **Step 4: Run test to verify it passes**

Run: `rg -n "matchmaker_service|重新加载|暂未配置红娘服务|暂无可选红娘服务|/pages/product-detail/index\?id=" UI/uniapp-marriage/pages/matchmaker-service/index.uvue UI/uniapp-marriage/utils/matchmaker-service-api.uts`

Expected:
- One `matchmaker_service` constant
- One `重新加载` button label
- One `暂未配置红娘服务` empty-state string
- One `暂无可选红娘服务` empty-state string
- One product detail route string

Then confirm the manual checklist is fully green in the running app.

- [ ] **Step 5: Commit**

```bash
git add UI/uniapp-marriage/utils/matchmaker-service-api.uts UI/uniapp-marriage/pages/matchmaker-service/index.uvue UI/uniapp-marriage/pages.json UI/uniapp-marriage/pages/mine/index.uvue
git commit -m "feat: launch matchmaker service flow"
```

## Self-Review

- **Spec coverage:**
  - 专属页面：Task 2
  - 配置表读取分类：Task 1 + Task 2
  - 展示全部商品：Task 1 + Task 2
  - 平台官方风、先讲服务再上商品：Task 2
  - 复用通用商品详情页：Task 2 + Task 4
  - mine 入口可达：Task 3
  - 加载态、未配置、无商品、失败与重试：Task 2 + Task 4
- **Placeholder scan:** 已避免 `TODO`、`TBD`、"similar to" 之类占位表述；每个任务都给出实际文件、代码块与命令。
- **Type consistency:** `MATCHMAKER_SERVICE_SCENE_CODE`、`MatchmakerServiceSpu`、`getMatchmakerServiceDisplayConfig`、`getMatchmakerServicePage`、`openMatchmakerServicePage` 在各任务中名称保持一致。
