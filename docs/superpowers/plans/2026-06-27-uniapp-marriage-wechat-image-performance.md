# Uniapp Marriage WeChat Image Performance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reduce first-open jank for `UI/uniapp-marriage` on WeChat Mini Program by adding WeChat lazy code-loading config, moving non-tab pages into subpackages, and deferring non-critical image-heavy sections on the heaviest pages.

**Architecture:** Keep the existing `uvue + uts` structure and fix the problem at three layers: WeChat runtime config, route/package structure, and per-page render timing. Prioritize first-screen content on the home page, then apply the same "critical first, image-heavy later" rule to one profile page and one product/detail page.

**Tech Stack:** uni-app x (`.uvue`, `lang="uts"`), WeChat Mini Program config in `manifest.json` / `pages.json`, existing uni APIs such as `setTimeout`, `nextTick`, `onLoad`, `onShow`

## Global Constraints

- WeChat Mini Program is the priority platform.
- Keep business code inside `UI/uniapp-marriage`; do not introduce a new module or framework.
- Preserve existing route paths and `navigationStyle: custom` for existing pages.
- Keep all tabBar pages in the main package.
- Add `lazyCodeLoading: "requiredComponents"` under `mp-weixin` in `UI/uniapp-marriage/manifest.json`.
- Use subpackages in `UI/uniapp-marriage/pages.json` only for non-tab pages.
- Optimize for perceived smoothness, even if some non-first-screen image blocks appear slightly later.
- Prefer `v-if` delayed mount over mounting hidden nodes.
- Keep existing core interactions, share, login, and real-name verification flows working.
- Do not change backend APIs or response shapes.
- Do not proactively run Maven; this task is frontend-only.
- This codebase currently has no established automated unit-test harness for `.uvue` pages, so verification is static diff review plus manual WeChat Mini Program smoke testing.

---

## File Structure Map

- `UI/uniapp-marriage/manifest.json`
  - WeChat Mini Program runtime config; add lazy code-loading without disturbing existing app IDs or other platform settings.
- `UI/uniapp-marriage/pages.json`
  - Route registry and package layout; keep tabBar routes in main package and move secondary pages into subpackages.
- `UI/uniapp-marriage/pages/index/index.uvue`
  - Main recommendation/home page; defer non-critical image-heavy sections and keep only the hero card path on first render.
- `UI/uniapp-marriage/pages/mine/index.uvue`
  - Personal center; make heavy background image rendering more conservative while keeping avatar/profile info immediately available.
- `UI/uniapp-marriage/pages/product-detail/index.uvue`
  - Product detail page; keep banner/summary/buy path immediate, delay long description images until the page is stable.
- `docs/superpowers/specs/2026-06-27-uniapp-marriage-wechat-image-performance-design.md`
  - Approved design reference; do not modify unless an implementation discovery contradicts it.

## Task 1: Add WeChat lazy loading config and subpackage layout

**Files:**
- Modify: `UI/uniapp-marriage/manifest.json`
- Modify: `UI/uniapp-marriage/pages.json`

**Interfaces:**
- Consumes: Existing `mp-weixin` config and current flat `pages` array.
- Produces:
  - `mp-weixin.lazyCodeLoading = "requiredComponents"`
  - New `subPackages` array in `pages.json`
  - Main package retains tabBar pages and only the minimal always-hot pages

- [ ] **Step 1: Review the existing route list and freeze the package split**

Group pages before editing so the split is deterministic:

```text
Main package
- pages/index/index
- pages/messages/index
- pages/dynamics/index
- pages/mine/index

Subpackage: pages/messages
- follow/index
- mutual/index
- chat/index

Subpackage: pages/member
- member-detail/index
- member-packages/index
- partner-dynamics/index
- mine-view/index

Subpackage: pages/product
- product-detail/index
- pay-cashier/index
- pay-result/index
- mine-payment/index

Subpackage: pages/profile
- mine-checkin/index
- mine-points/index
- mine-dynamics/index
- mine-follow/index
- mine-certifications/index
- mine-profile/basic/index
- mine-profile/preference/index
- mine-profile/album/index
- mine-profile/nameCheck/index
- mine-profile/marriageCheck/index

Subpackage: pages/distributor
- index
- order/index
- withdraw/index
- withdraw-record/index
- team/index
- wallet/index
- promoter-rank/index
- commission-rank/index
- goods/index

Subpackage: pages/marketing
- activity/index
- join-us/index
- license-publicity/index
- my-activities/index
- emotion-academy/index
- matchmaker-service/index
```

- [ ] **Step 2: Add WeChat lazy code-loading config in `manifest.json`**

Update the `mp-weixin` block to this shape:

```jsonc
"mp-weixin" : {
    "appid" : "wxc6e16a1b05f5ddaa",
    "setting" : {
        "urlCheck" : true
    },
    "usingComponents" : true,
    "lazyCodeLoading" : "requiredComponents"
}
```

- [ ] **Step 3: Convert `pages.json` from flat-only pages to main pages + subPackages**

Keep the top-level `pages` array with only the tabBar routes:

```jsonc
"pages": [
  {
    "path": "pages/index/index",
    "style": { "navigationStyle": "custom" }
  },
  {
    "path": "pages/messages/index",
    "style": { "navigationStyle": "custom" }
  },
  {
    "path": "pages/dynamics/index",
    "style": { "navigationStyle": "custom" }
  },
  {
    "path": "pages/mine/index",
    "style": { "navigationStyle": "custom" }
  }
],
"subPackages": [
  {
    "root": "pages/messages",
    "pages": [
      { "path": "follow/index", "style": { "navigationStyle": "custom" } },
      { "path": "mutual/index", "style": { "navigationStyle": "custom" } },
      { "path": "chat/index", "style": { "navigationStyle": "custom" } }
    ]
  }
]
```

Then continue the same pattern for `member`, `product`, `profile`, `distributor`, and `marketing` roots.

- [ ] **Step 4: Verify no tabBar page moved into a subpackage**

Check the file manually against this invariant:

```text
Must remain in top-level pages:
- pages/index/index
- pages/messages/index
- pages/dynamics/index
- pages/mine/index
```

And ensure these exact `tabBar.list[*].pagePath` values still point to those same top-level pages.

- [ ] **Step 5: Review the config diff before moving on**

Run:

```bash
git diff -- UI/uniapp-marriage/manifest.json UI/uniapp-marriage/pages.json
```

Expected:

```text
- manifest.json shows lazyCodeLoading under mp-weixin
- pages.json shows top-level pages reduced to 4 tabBar pages
- pages.json shows a new subPackages array
- tabBar block remains intact
```

- [ ] **Step 6: Commit**

```bash
git add UI/uniapp-marriage/manifest.json UI/uniapp-marriage/pages.json
git commit -m "perf(marriage): optimize wechat package loading"
```

## Task 2: Reduce first-render image work on the home page

**Files:**
- Modify: `UI/uniapp-marriage/pages/index/index.uvue`

**Interfaces:**
- Consumes:
  - Existing computed values such as `displayAlbumPreview`, `displayMomentImages`, `swipePreviewImage`, `hasLatestMoment`
  - Existing state such as `previewVisible`, `previewImage`, `currentRecommendMember`
- Produces:
  - New delayed-render booleans for home-page image-heavy sections
  - New scheduling helpers to turn those booleans on after the first screen is stable
  - Template guards that prevent non-critical image sections from mounting immediately

- [ ] **Step 1: Add explicit first-screen render flags near the existing page state**

Add state close to the other `ref(...)` declarations:

```ts
const homeHeroReady = ref(false)
const homeExtraSectionsReady = ref(false)
const homeMomentReady = ref(false)
const homeShadowReady = ref(false)
```

Use these responsibilities:

```text
homeHeroReady          -> first screen is ready to show the active card
homeExtraSectionsReady -> album and other lower non-critical sections may mount
homeMomentReady        -> latest-moment image block may mount
homeShadowReady        -> swipe preview shadow image may mount
```

- [ ] **Step 2: Add scheduling helpers that stage rendering instead of turning everything on at once**

Add these functions in the script section:

```ts
function resetHomeDeferredSections(): void {
	homeHeroReady.value = false
	homeExtraSectionsReady.value = false
	homeMomentReady.value = false
	homeShadowReady.value = false
}

function scheduleHomeDeferredSections(): void {
	homeHeroReady.value = true
	setTimeout(() => {
		homeShadowReady.value = true
	}, 80)
	setTimeout(() => {
		homeExtraSectionsReady.value = true
	}, 160)
	setTimeout(() => {
		homeMomentReady.value = true
	}, 240)
}
```

Reset before a full reload of recommendation content and re-schedule after first usable data lands.

- [ ] **Step 3: Wire the scheduling into the recommendation load flow**

Find the recommendation success path and apply this rule:

```ts
if (pageNo == 1) {
	recommendMembers.value = nextList
	recommendIndex.value = 0
	resetHomeDeferredSections()
	scheduleHomeDeferredSections()
}
```

Also call `resetHomeDeferredSections()` before a hard refresh, for example in the entry path used by initial load and pull-to-refresh.

- [ ] **Step 4: Prevent the swipe-preview shadow image from mounting on the first frame**

Change the preview card condition from:

```vue
<view v-if="swipePreviewMember.id > 0" class="profile-card profile-card-shadow" :style="previewCardStyle">
```

to:

```vue
<view v-if="homeShadowReady && swipePreviewMember.id > 0" class="profile-card profile-card-shadow" :style="previewCardStyle">
```

This keeps the extra card image off the initial render path.

- [ ] **Step 5: Gate album and latest-moment image sections behind delayed booleans**

Change the album section to:

```vue
<view v-if="homeExtraSectionsReady && displayAlbumPreview.length > 0" class="content-section album-section">
```

Change the moment section to:

```vue
<view v-if="homeMomentReady && hasLatestMoment" class="content-section moment-section">
```

And inside the moment section, keep image creation gated as well:

```vue
<view v-if="homeMomentReady && displayMomentImages.length > 0" class="moment-images">
```

- [ ] **Step 6: Keep the preview overlay strict-on-demand only**

Leave the overlay guard in the strict shape below; do not weaken it during refactor:

```vue
<view v-if="previewVisible && previewImage != ''" class="image-preview-overlay" @tap="closeProfileImagePreview">
```

The goal is to preserve true lazy mounting for the large preview image.

- [ ] **Step 7: Re-check that critical first-screen content still renders immediately**

Confirm the following nodes remain independent of delayed flags:

```text
- active profile card container
- main hero image or fallback avatar
- profile avatar
- name, age, verified pill, summary text
- floating action buttons
```

Do not put these behind `homeExtraSectionsReady`, `homeMomentReady`, or `homeShadowReady`.

- [ ] **Step 8: Review the page diff**

Run:

```bash
git diff -- UI/uniapp-marriage/pages/index/index.uvue
```

Expected:

```text
- new home deferred-render refs
- new scheduling/reset helpers
- shadow card gated behind homeShadowReady
- album section gated behind homeExtraSectionsReady
- moment section gated behind homeMomentReady
- no changes to share/login/verified interaction wiring
```

- [ ] **Step 9: Commit**

```bash
git add UI/uniapp-marriage/pages/index/index.uvue
git commit -m "perf(marriage): defer home image-heavy sections"
```

## Task 3: Make the mine page background rendering more conservative

**Files:**
- Modify: `UI/uniapp-marriage/pages/mine/index.uvue`

**Interfaces:**
- Consumes:
  - Existing computed values `hasCustomCover`, `coverImage`, `displayProfileAvatar`, `backgroundPreviewVisible`
- Produces:
  - A delayed-mount flag for the custom background cover
  - Scheduling logic that keeps avatar/profile info immediate while background image work starts slightly later

- [ ] **Step 1: Add a delayed cover flag near the existing refs**

Add:

```ts
const heroCoverReady = ref(false)
```

- [ ] **Step 2: Add cover scheduling helpers**

Add:

```ts
function resetMineDeferredSections(): void {
	heroCoverReady.value = false
}

function scheduleMineDeferredSections(): void {
	setTimeout(() => {
		heroCoverReady.value = true
	}, 80)
}
```

- [ ] **Step 3: Trigger scheduling during page activation and profile sync**

Use this pattern in `onShow` before or alongside the data sync:

```ts
onShow(() => {
	resetMineDeferredSections()
	loginSheetVisible.value = !isMemberLoggedIn()
	if (!loginSheetVisible.value) {
		scheduleMineDeferredSections()
		syncMemberProfile()
	}
})
```

And after a successful `getMyProfile()` response, ensure the flag is enabled again in case the page loaded with a custom background:

```ts
scheduleMineDeferredSections()
```

- [ ] **Step 4: Gate the custom cover image but keep the fallback decorative cover immediate**

Change:

```vue
<image v-if="hasCustomCover" class="hero-background-image" :src="coverImage" mode="aspectFill"></image>
```

to:

```vue
<image v-if="heroCoverReady && hasCustomCover" class="hero-background-image" :src="coverImage" mode="aspectFill"></image>
```

Do not change the default cover branch:

```vue
<view v-else class="hero-default-cover">
```

This preserves an immediate visual shell even when the remote background image is deferred.

- [ ] **Step 5: Keep the background preview overlay on strict on-demand mount**

Do not relax this guard:

```vue
<view v-if="backgroundPreviewVisible" class="background-preview-mask" @tap="closeBackgroundPreview">
```

The heavy preview image should continue to exist only while the preview is open.

- [ ] **Step 6: Review the diff**

Run:

```bash
git diff -- UI/uniapp-marriage/pages/mine/index.uvue
```

Expected:

```text
- one new delayed-render ref
- reset/schedule helpers
- custom hero background image gated behind heroCoverReady
- avatar/profile card remains immediate
```

- [ ] **Step 7: Commit**

```bash
git add UI/uniapp-marriage/pages/mine/index.uvue
git commit -m "perf(marriage): defer mine cover image mount"
```

## Task 4: Delay long detail images on the product-detail page

**Files:**
- Modify: `UI/uniapp-marriage/pages/product-detail/index.uvue`

**Interfaces:**
- Consumes:
  - Existing computed values `detailImageUrls`, `displayBannerUrls`, `displayDescription`, `loading`, `loadFailed`
- Produces:
  - A delayed flag for detail images only
  - A helper array used by the template instead of mounting all extracted description images immediately

- [ ] **Step 1: Add delayed image state**

Add near the page refs:

```ts
const detailImagesReady = ref(false)
const visibleDetailImageUrls = computed((): string[] => {
	if (!detailImagesReady.value) {
		return []
	}
	return detailImageUrls.value
})
```

- [ ] **Step 2: Add reset/schedule helpers for the long images**

Add:

```ts
function resetDetailDeferredSections(): void {
	detailImagesReady.value = false
}

function scheduleDetailDeferredSections(): void {
	setTimeout(() => {
		detailImagesReady.value = true
	}, 180)
}
```

- [ ] **Step 3: Wire the helpers into the page load lifecycle**

At the top of `loadDetail()` reset the deferred section state:

```ts
resetDetailDeferredSections()
```

In the success branch after `detail.value = nextDetail` and `selectedSku.value = findDefaultSku(nextDetail)`, schedule the detail images:

```ts
scheduleDetailDeferredSections()
```

If the request fails, leave `detailImagesReady` false.

- [ ] **Step 4: Mount long images from the delayed computed array, not the raw array**

Change the template block from:

```vue
<image
	v-for="(imageUrl, imageIndex) in detailImageUrls"
	:key="'detail-image-' + imageIndex"
	class="detail-image"
	:src="imageUrl"
	mode="widthFix"
></image>
```

to:

```vue
<image
	v-for="(imageUrl, imageIndex) in visibleDetailImageUrls"
	:key="'detail-image-' + imageIndex"
	class="detail-image"
	:src="imageUrl"
	mode="widthFix"
></image>
```

This keeps banner, summary, sku selection, and pay path immediate while moving long images off the first render.

- [ ] **Step 5: Keep the rest of the purchase flow untouched**

Do not change these existing interfaces:

```ts
openSkuSheet()
closeSkuSheet()
selectSku(...)
buySelected()
onShareAppMessage(...)
onShareTimeline(...)
```

The only intended behavior change in this task is when detail images mount.

- [ ] **Step 6: Review the diff**

Run:

```bash
git diff -- UI/uniapp-marriage/pages/product-detail/index.uvue
```

Expected:

```text
- new detailImagesReady ref
- new visibleDetailImageUrls computed
- loadDetail resets then schedules delayed detail images
- template renders long images from visibleDetailImageUrls
```

- [ ] **Step 7: Commit**

```bash
git add UI/uniapp-marriage/pages/product-detail/index.uvue
git commit -m "perf(marriage): defer product detail long images"
```

## Task 5: Manual WeChat Mini Program smoke verification

**Files:**
- Verify: `UI/uniapp-marriage/manifest.json`
- Verify: `UI/uniapp-marriage/pages.json`
- Verify: `UI/uniapp-marriage/pages/index/index.uvue`
- Verify: `UI/uniapp-marriage/pages/mine/index.uvue`
- Verify: `UI/uniapp-marriage/pages/product-detail/index.uvue`

**Interfaces:**
- Consumes: All code/config changes from Tasks 1-4.
- Produces: A clear go/no-go result and a list of any regressions that must be fixed before merge.

- [ ] **Step 1: Review the final diff for scope control**

Run:

```bash
git diff -- UI/uniapp-marriage/manifest.json UI/uniapp-marriage/pages.json UI/uniapp-marriage/pages/index/index.uvue UI/uniapp-marriage/pages/mine/index.uvue UI/uniapp-marriage/pages/product-detail/index.uvue
```

Expected:

```text
Only the five planned files changed for this optimization pass.
```

- [ ] **Step 2: Build/check in WeChat Mini Program tooling**

Open the project in HBuilderX or WeChat DevTools and verify the app still compiles after subpackage changes.

Manual check list:

```text
- pages.json parses successfully
- no route-not-found errors for moved subpackage pages
- no tabBar warnings
- home page opens successfully
```

- [ ] **Step 3: Smoke-test the home page**

Manual expected behavior:

```text
- home page first frame shows active card shell quickly
- main hero image / fallback avatar appears before album and latest-moment image sections
- swipe shadow preview card can still appear after the page settles
- tapping hero image still opens the large preview overlay
- login and real-name sheets still behave as before
```

- [ ] **Step 4: Smoke-test the mine page**

Manual expected behavior:

```text
- mine page shell, avatar, and profile card appear immediately
- custom background cover, if any, appears shortly after page entry instead of blocking the first frame
- previewing background image still works
- edit profile / member package actions still work
```

- [ ] **Step 5: Smoke-test the product-detail page**

Manual expected behavior:

```text
- banner, summary, and sku selection appear first
- long detail images appear after the initial content settles
- sku sheet still opens
- buy path still navigates correctly
- share still uses the existing title/path/image logic
```

- [ ] **Step 6: Record any regressions immediately if found**

If any of these failures occur, stop and fix before merge:

```text
- moved page cannot open after subpackage conversion
- tabBar navigation breaks
- home page loses hero image or action buttons on first load
- mine page never shows custom background
- product detail long images never appear
```

- [ ] **Step 7: Commit the verified final state**

If additional verification fixes were needed:

```bash
git add UI/uniapp-marriage/manifest.json UI/uniapp-marriage/pages.json UI/uniapp-marriage/pages/index/index.uvue UI/uniapp-marriage/pages/mine/index.uvue UI/uniapp-marriage/pages/product-detail/index.uvue
git commit -m "fix(marriage): finalize wechat image performance tuning"
```

If no post-verification fixes were needed, skip this commit.

## Spec Coverage Check

- WeChat `lazyCodeLoading` config: covered by Task 1.
- `pages.json` subpackage split: covered by Task 1.
- Home page first-screen reduction and delayed non-critical image blocks: covered by Task 2.
- Additional image-heavy page light optimization: covered by Task 3 (`mine`) and Task 4 (`product-detail`).
- Manual WeChat Mini Program verification: covered by Task 5.

## Placeholder Scan

- No `TODO` / `TBD` markers remain.
- Each task lists exact files.
- Each verification step states expected behavior.
- Later tasks use only names defined in the same task or already existing file APIs.

## Type Consistency Check

- Home page delayed flags: `homeHeroReady`, `homeExtraSectionsReady`, `homeMomentReady`, `homeShadowReady` are introduced only in Task 2 and used there consistently.
- Mine page delayed flag: `heroCoverReady` is introduced and used consistently in Task 3.
- Product detail delayed state: `detailImagesReady` and `visibleDetailImageUrls` are introduced and used consistently in Task 4.
- No new backend interfaces or renamed existing public functions are introduced.
