# Preview Home Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让“我的资料 > 预览主页”tab 的展示效果完全对齐当前个人主页详情页，同时保持为只读预览且不显示互动按钮。

**Architecture:** 保持 `mine-profile/basic` 中 `ProfilePreviewCard` 的挂载入口不变，直接升级 `components/profile-preview-card.uvue` 为详情页同款静态展示，并在 `mine-profile/basic` 中补齐 `previewMember` 的认证字段透传。这样不改 tab 切换和编辑流程，只收敛展示结构与数据映射。

**Tech Stack:** uni-app UTS、`.uvue` 组件模板/样式、现有 `ref`/`computed`/`defineProps` 数据流。

## Global Constraints

- 仅修改 `UI/uniapp-marriage/components/profile-preview-card.uvue` 与 `UI/uniapp-marriage/pages/mine-profile/basic/index.uvue`。
- `预览主页` 需要完全对齐当前个人主页详情页。
- 顶部文案保持 `个人主页`，预览时直接看成成品效果。
- 不需要预览页中的互动按钮。
- 不改 `UI/uniapp-marriage/pages/member-detail/index.uvue`。
- 不改 `UI/uniapp-marriage/pages/index/index.uvue`。
- 不抽公共展示组件。
- 不改变 `我的资料` 页面的 tab 结构和编辑交互。
- 不主动运行 Maven 编译；前端验证以页面展示一致性检查为主。

---

## File Structure Map

- `UI/uniapp-marriage/components/profile-preview-card.uvue`
  - 预览主页展示组件。
  - 本次要升级为当前 `member-detail` 页的静态只读镜像：头图主视觉、认证区、资料概览、择偶宣言、基本资料、择偶条件、相册。

- `UI/uniapp-marriage/pages/mine-profile/basic/index.uvue`
  - 编辑页与预览 tab 的宿主页面。
  - 本次只补齐 `previewMember` 类型与组装字段映射，不改变 tab 切换和表单交互。

## Task 1: Upgrade ProfilePreviewCard to match the detail-page static layout

**Files:**
- Modify: `UI/uniapp-marriage/components/profile-preview-card.uvue`

**Interfaces:**
- Consumes:
  - `props.member: PreviewMember`
  - Existing preview fields: `name`, `age`, `city`, `education`, `job`, `income`, `bio`, `interestTags`, `albumImages`, `verifiedLabel`
- Produces:
  - Detail-page-aligned static preview layout with:
    - hero card
    - certification row
    - profile overview title
    - declaration card
    - basic info / preference / album blocks
  - New preview-only computed helpers mirroring detail-page read-only display.

- [ ] **Step 1: Read the current preview component and identify missing detail-page sections**

Open `UI/uniapp-marriage/components/profile-preview-card.uvue` and confirm the current template only contains:

```vue
<view class="profile-card">...</view>
<view v-if="basicInfoGrid.length > 0" class="content-section">...</view>
<view v-if="member.interestTags.length > 0" class="content-section">...</view>
<view v-if="displayAlbumPreview.length > 0" class="content-section">...</view>
```

Expected: verify the current component lacks all of these detail-page sections:
- certification row
- `资料概览`
- `择偶宣言`
- detail-page style hero topbar + identity card layout

- [ ] **Step 2: Expand the PreviewMember type for certification rendering**

In `UI/uniapp-marriage/components/profile-preview-card.uvue`, extend `PreviewMember` with the fields used by the detail-page certification block.

Use this exact type shape:

```ts
type PreviewMember = {
	id: number
	name: string
	sex: number
	age: number
	city: string
	education: string
	job: string
	height: number
	weight: number
	income: string
	maritalStatus: string
	houseStatus: string
	carStatus: string
	realVerified: number
	singleVerified: number
	maskedIdCard?: string | null
	verifiedLabel: string
	mainImage: string
	avatarImage: string
	onlineLabel: string
	viewerLabel: string
	bio: string
	tags: string[]
	interestTags: string[]
	albumImages: string[]
}
```

Expected: the preview component can render认证信息 without pulling data from any external page state.

- [ ] **Step 3: Add detail-page-aligned computed helpers to the preview component**

Insert these helpers in `UI/uniapp-marriage/components/profile-preview-card.uvue` near the existing `displayAge`, `displayHeroImage`, and `basicInfoGrid` computed values:

```ts
function resolveIdCardPrefix(maskedIdCard: string | null | undefined): string {
	if (maskedIdCard == null || maskedIdCard == '') return ''
	const match = maskedIdCard.match(/\d{4}/)
	return match != null ? match[0] : ''
}

const hasHeroPhoto = computed(() => props.member.mainImage != '' || props.member.albumImages.length > 0)

const displayHeroSummary = computed(() => {
	const parts: string[] = []
	const current = props.member
	if (current.city != null && current.city.length > 0) parts.push(current.city)
	if (current.job != null && current.job.length > 0) parts.push(current.job)
	if (current.education != null && current.education.length > 0) parts.push(current.education)
	if (parts.length > 0) return parts.join(' / ')
	return hasHeroPhoto.value ? '资料已开放查看' : '资料待完善'
})

const displayQuickFacts = computed((): string[] => {
	const facts: string[] = []
	const current = props.member
	if (current.city != null && current.city.length > 0) facts.push(current.city)
	if (current.job != null && current.job.length > 0) facts.push(current.job)
	if (current.education != null && current.education.length > 0) facts.push(current.education)
	if (current.income != null && current.income.length > 0) facts.push(current.income)
	return facts
})

const displayCertificationBadges = computed((): { key: string, title: string, desc: string }[] => {
	const badges: { key: string, title: string, desc: string }[] = []
	const current = props.member
	if (current.realVerified == 1) {
		const idCardPrefix = resolveIdCardPrefix(current.maskedIdCard)
		badges.push({
			key: 'real',
			title: '实名认证',
			desc: idCardPrefix != '' ? '身份证前四位：' + idCardPrefix : '身份已核验'
		})
	}
	if (current.singleVerified == 1) {
		badges.push({
			key: 'marriage',
			title: '婚姻认证',
			desc: '状态已核验'
		})
	}
	return badges
})
```

Expected: the preview card now has the same read-only data shapers the detail page relies on.

- [ ] **Step 4: Replace the preview component template with the detail-page-aligned static structure**

Rewrite the template in `UI/uniapp-marriage/components/profile-preview-card.uvue` so it mirrors the current detail page, while omitting all interaction buttons.

Use this target structure:

```vue
<template>
	<view class="preview-card">
		<view class="profile-card">
			<view class="profile-stage">
				<image class="profile-stage-image" :src="displayHeroImage" mode="aspectFill"></image>
				<view class="profile-stage-scrim"></view>
				<view class="profile-stage-topbar">
					<view class="stage-progress-chip stage-progress-chip-detail">
						<text class="stage-progress-text">个人主页</text>
					</view>
					<view class="profile-preview-chip">
						<text class="profile-preview-chip-text">查看大图</text>
					</view>
				</view>

				<view class="profile-stage-bottom">
					<view class="profile-identity-card">
						<view class="profile-identity">
							<image class="profile-avatar" :src="displayAvatarImage" mode="aspectFill"></image>
							<view class="profile-meta">
								<view class="profile-name-row">
									<text class="profile-name">{{ displayName }}</text>
									<text v-if="displayAge != ''" class="profile-age">{{ displayAge }}</text>
									<view v-if="member.verifiedLabel != ''" class="verified-pill">
										<text class="verified-pill-text">{{ member.verifiedLabel }}</text>
									</view>
								</view>
								<text class="profile-bio">{{ displayHeroSummary }}</text>
							</view>
						</view>
						<view v-if="displayQuickFacts.length > 0" class="profile-quick-facts">
							<view class="quick-fact-chip" v-for="(item, index) in displayQuickFacts" :key="'fact-' + index">
								<text class="quick-fact-chip-text">{{ item }}</text>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>

		<view v-if="displayCertificationBadges.length > 0" class="certification-row">
			<view
				v-for="(item, index) in displayCertificationBadges"
				:key="'cert-badge-' + item.key + '-' + index"
				class="certification-card"
				:class="{ 'certification-card-marriage': item.key == 'marriage' }"
			>
				<view class="certification-icon-wrap" :class="{ 'certification-icon-wrap-marriage': item.key == 'marriage' }">
					<text class="certification-icon">✓</text>
				</view>
				<view class="certification-copy">
					<text class="certification-title">{{ item.title }}</text>
					<text class="certification-desc">{{ item.desc }}</text>
				</view>
			</view>
		</view>

		<view class="profile-info-head">
			<text class="profile-info-title">资料概览</text>
		</view>
		<text class="profile-info-desc">择偶宣言</text>

		<view class="profile-story">
			<text class="profile-story-text">{{ displayBioText }}</text>
		</view>

		<view v-if="basicInfoGrid.length > 0" class="content-section">...</view>
		<view v-if="member.interestTags.length > 0" class="content-section">...</view>
		<view v-if="displayAlbumPreview.length > 0" class="content-section">...</view>
	</view>
</template>
```

Expected: preview tab visually reads like the current detail page and still has no bottom action rail.

- [ ] **Step 5: Replace the preview component CSS with detail-page-aligned static styles**

In `UI/uniapp-marriage/components/profile-preview-card.uvue`, replace the old hero/card styles with the current detail-page-aligned values and add the new certification/story blocks.

Use these style groups from the detail page as the target values:

```css
.profile-stage {
	position: relative;
	height: 438px;
	border-radius: 34px;
	overflow: hidden;
	background-color: #eadccf;
	border-width: 1px;
	border-color: rgba(228, 220, 214, 0.9);
}

.profile-stage-scrim {
	position: absolute;
	left: 0;
	right: 0;
	top: 0;
	bottom: 0;
	background-image: linear-gradient(180deg, rgba(49, 33, 27, 0.08) 0%, rgba(49, 33, 27, 0.02) 26%, rgba(32, 20, 16, 0.14) 56%, rgba(28, 18, 16, 0.72) 100%);
}

.profile-stage-topbar {
	position: absolute;
	left: 16px;
	right: 16px;
	top: 14px;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
}

.stage-progress-chip {
	height: 32px;
	padding: 0 12px;
	border-radius: 16px;
	background-color: rgba(255, 249, 245, 0.9);
	justify-content: center;
	align-items: center;
}

.stage-progress-chip-detail {
	background-color: rgba(255, 249, 245, 0.92);
}

.profile-stage-bottom {
	position: absolute;
	left: 14px;
	right: 14px;
	bottom: 16px;
}

.profile-identity-card {
	padding: 14px 14px 12px;
	border-radius: 22px;
	background-color: rgba(255, 248, 243, 0.9);
	border-width: 1px;
	border-color: rgba(255, 255, 255, 0.58);
}

.profile-identity {
	flex-direction: row;
	align-items: center;
}

.profile-avatar {
	width: 46px;
	height: 46px;
	border-radius: 23px;
	margin-right: 12px;
	background-color: rgba(255, 255, 255, 0.7);
}

.profile-name {
	font-size: 28px;
	line-height: 32px;
	font-weight: 700;
	color: #201917;
	margin-right: 8px;
}

.profile-age {
	font-size: 15px;
	line-height: 20px;
	font-weight: 600;
	color: #6f5a55;
	margin-right: 8px;
}

.profile-bio {
	font-size: 13px;
	line-height: 18px;
	color: #6d5a56;
}

.profile-quick-facts { ... }
.quick-fact-chip { ... }
.profile-info-head { ... }
.profile-info-title { ... }
.profile-info-desc { ... }
.profile-story { ... }
.profile-story-text { ... }
.certification-row { ... }
.certification-card { ... }
.certification-card-marriage { ... }
.certification-icon-wrap { ... }
.certification-icon-wrap-marriage { ... }
.certification-title { ... }
.certification-desc { ... }
```

Also keep the existing lower-section styles (`basic-info-grid`, `interest-row`, `album-grid`) unless a small alignment adjustment is required.

Expected: `ProfilePreviewCard` no longer looks like the old preview-only variant; it matches the updated detail page styling language.

- [ ] **Step 6: Do a static parity pass against the detail page**

Before editing any other file, compare the upgraded preview component against the detail page requirement list:

```text
- 头图卡片与详情页主视觉一致
- 认证区出现
- 资料概览出现
- 择偶宣言出现
- 左侧胶囊保持“个人主页”
- 没有底部互动按钮
```

Expected: all six checks are satisfied from code inspection alone before data plumbing.

## Task 2: Extend previewMember data assembly with certification fields

**Files:**
- Modify: `UI/uniapp-marriage/pages/mine-profile/basic/index.uvue`

**Interfaces:**
- Consumes:
  - Existing `previewMember.value = { ... } as PreviewMember`
  - Existing refs/computed values: `realVerified`, `verifiedLabel`, `profileForm`, `albumImages`, `avatarUrl`
- Produces:
  - `previewMember` objects that contain the certification fields required by the upgraded `ProfilePreviewCard`.

- [ ] **Step 1: Read the current PreviewMember type and previewMember assembly**

Open `UI/uniapp-marriage/pages/mine-profile/basic/index.uvue` and verify these current sections exist:

```ts
type PreviewMember = {
	id: number
	name: string
	sex: number
	age: number
	city: string
	education: string
	job: string
	height: number
	weight: number
	income: string
	maritalStatus: string
	houseStatus: string
	carStatus: string
	verifiedLabel: string
	mainImage: string
	avatarImage: string
	onlineLabel: string
	viewerLabel: string
	bio: string
	tags: string[]
	interestTags: string[]
	albumImages: string[]
}
```

and

```ts
previewMember.value = {
	id: 1,
	name: form.nickname != '' ? form.nickname : '微信用户',
	...
	verifiedLabel: realVerified.value ? (verifiedLabel.value != '' ? verifiedLabel.value : '已实名') : '',
	mainImage: ...,
	avatarImage: avatarUrl.value,
	...
	albumImages: albumImages.value.map((item) => item.url)
} as PreviewMember
```

Expected: confirm the missing certification fields are absent from both type and assignment.

- [ ] **Step 2: Extend the local PreviewMember type in mine-profile/basic**

Change the type in `UI/uniapp-marriage/pages/mine-profile/basic/index.uvue` to include the same certification fields expected by the upgraded preview card.

Use this exact insertion between `carStatus` and `verifiedLabel`:

```ts
	realVerified: number
	singleVerified: number
	maskedIdCard?: string | null
```

Expected: `ProfilePreviewCard :member="previewMember"` becomes type-compatible with the upgraded component.

- [ ] **Step 3: Identify the data source for each new preview field**

Use the existing page state already loaded in `mine-profile/basic/index.uvue`:

```ts
realVerified.value = profile.realVerified === 1
verifiedLabel.value = profile.verifiedLabel || ''
```

For this task, wire the new preview fields as follows:

```text
realVerified  -> realVerified.value ? 1 : 0
singleVerified -> 0
maskedIdCard -> ''
```

Expected: even if the edit page does not currently expose marriage-certification or masked-id-card values, the preview card can still render the real-name block consistently and safely skip unavailable items.

- [ ] **Step 4: Update previewMember.value assembly with the new fields**

In the `previewMember.value = { ... } as PreviewMember` assignment, insert the new fields exactly here:

```ts
previewMember.value = {
	id: 1,
	name: form.nickname != '' ? form.nickname : '微信用户',
	sex: sexValue,
	age: age,
	city: form.liveAreaText,
	education: form.education,
	job: form.jobTitle,
	height: parseInt(form.heightCm) || 0,
	weight: parseInt(form.weightKg) || 0,
	income: form.incomeLevel,
	maritalStatus: form.maritalStatus,
	houseStatus: form.houseStatus,
	carStatus: form.carStatus,
	realVerified: realVerified.value ? 1 : 0,
	singleVerified: 0,
	maskedIdCard: '',
	verifiedLabel: realVerified.value ? (verifiedLabel.value != '' ? verifiedLabel.value : '已实名') : '',
	mainImage: form.backgroundImage != '' ? form.backgroundImage : (albumImages.value.length > 0 ? albumImages.value[0].url : ''),
	avatarImage: avatarUrl.value,
	onlineLabel: '',
	viewerLabel: '',
	bio: form.bio,
	tags: tags,
	interestTags: interestTags,
	albumImages: albumImages.value.map((item) => item.url)
} as PreviewMember
```

Expected: preview tab receives all fields needed for the aligned certification block.

- [ ] **Step 5: Do a static verification pass for preview tab wiring**

From code inspection, confirm all three of these are true:

```text
1. activeTab == 'preview' still renders <ProfilePreviewCard :member="previewMember" />
2. previewMember now includes realVerified / singleVerified / maskedIdCard
3. no preview-only interaction handlers were introduced into mine-profile/basic
```

Expected: preview alignment stays purely presentational and does not alter form-editing behavior.

## Task 3: Final static verification for preview/detail parity

**Files:**
- Modify: `UI/uniapp-marriage/components/profile-preview-card.uvue` (only if parity issues are found)
- Modify: `UI/uniapp-marriage/pages/mine-profile/basic/index.uvue` (only if parity issues are found)

**Interfaces:**
- Consumes:
  - The upgraded `ProfilePreviewCard`
  - The updated `previewMember` data assembly
- Produces:
  - Final alignment polish ensuring the preview tab and current detail page stay visually and structurally in sync.

- [ ] **Step 1: Compare preview-card sections against the approved spec checklist**

Use this checklist exactly:

```text
1. 头图卡片与详情页主视觉一致
2. 认证区在预览页中出现
3. 资料概览 / 择偶宣言在预览页中出现
4. 基本资料 / 择偶条件 / 相册顺序与详情页一致
5. 左侧胶囊保持“个人主页”
6. 不显示底部互动按钮
7. 我的资料页 tab 切换和保存编辑能力不受影响（代码层面未改）
```

Expected: every approved requirement maps to concrete code in either `profile-preview-card.uvue` or `mine-profile/basic/index.uvue`.

- [ ] **Step 2: Run diff hygiene checks on the two touched files**

Run:

```bash
git diff --check -- UI/uniapp-marriage/components/profile-preview-card.uvue UI/uniapp-marriage/pages/mine-profile/basic/index.uvue
```

Expected: no whitespace errors. CRLF warnings are acceptable; trailing-space or conflict-marker issues are not.

- [ ] **Step 3: If needed, make one tiny parity-only correction**

If you find a small mismatch during code review, restrict the correction to one of these categories only:

```text
- 胶囊文案或位置与详情页不一致
- 认证区/宣言区顺序不一致
- quick facts 或 hero summary 的映射遗漏
- PreviewMember 字段名与组件读取字段不一致
```

Expected: no scope creep beyond parity polish.

## Spec Coverage Check

- 预览主页完全对齐当前详情页：Task 1 + Task 3
- 顶部文案保持“个人主页”：Task 1 Step 4 + Task 3
- 不显示互动按钮：Task 1 Step 4 + Task 3
- 保留 tab 切换与编辑流程：Task 2 + Task 3
- 补齐认证字段透传：Task 2

## Self-Review Notes

- No placeholders (`TODO`, `TBD`, “implement later”) remain.
- Type names and fields are consistent across the plan: `PreviewMember`, `realVerified`, `singleVerified`, `maskedIdCard`, `displayCertificationBadges`, `displayHeroSummary`, `displayQuickFacts`.
- The plan stays within the approved scope: preview component + preview data assembly only, no interaction buttons, no shared-component extraction.
