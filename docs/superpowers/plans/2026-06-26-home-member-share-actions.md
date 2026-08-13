# Home/Member Share Actions Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将婚恋端首页推荐页与个人主页详情页统一为同一套 `分享 / Hi / 喜欢` 圆形悬浮操作，并让详情页头部主视觉贴齐首页推荐卡的无滑动版本。

**Architecture:** 直接在两个现有页面内收敛模板结构与样式命名，不新增业务接口、不改分享链路。首页以“迁移分享入口 + 统一底部按钮组”为主，详情页以“复用首页推荐卡视觉层次 + 统一按钮组”为主，优先实现可见一致性，暂不抽公共组件。

**Tech Stack:** uni-app UTS、`.uvue` 页面模板/样式、微信分享 `open-type="share"`、现有页面 computed/ref/onShare 生命周期。

## Global Constraints

- 仅修改 `UI/uniapp-marriage` 相关页面，不扩展到其他婚恋端页面。
- 首页与个人主页详情页都统一为三枚操作：`分享 / Hi / 喜欢`。
- 详情页只复用首页推荐卡视觉，不引入左右切换资料能力。
- 保持现有分享文案、分享参数、分享路径策略不变，除非当前样式改造必须调整。
- 保持现有登录拦截、实名认证拦截、关注能力、打招呼能力不变。
- 不强行抽离完整公共推荐卡组件，本轮优先直接对齐和复用已有结构/样式语义。
- 不主动运行 Maven 编译；前端验证以页面级检查为主。

---

## File Structure Map

- `UI/uniapp-marriage/pages/index/index.uvue`
  - 首页推荐页。
  - 负责推荐卡头部视觉、底部悬浮操作组、滚动显隐、分享回调。
  - 本次要移除头部 `stage-share-btn` 展示，并新增底部分享圆形按钮。

- `UI/uniapp-marriage/pages/member-detail/index.uvue`
  - 个人主页详情页。
  - 负责详情资料页头图、资料内容、底部互动按钮、分享回调。
  - 本次要让头部视觉向首页推荐卡对齐，并把三枚悬浮按钮统一到首页同款样式和布局。

## Task 1: Migrate the home-page share entry into the floating action rail

**Files:**
- Modify: `UI/uniapp-marriage/pages/index/index.uvue`

**Interfaces:**
- Consumes:
  - Existing `actionBarVisible: Ref<boolean>` for button show/hide.
  - Existing `onShareAppMessage` / `onShareTimeline` definitions already in `index.uvue`.
- Produces:
  - A third floating action share button using `open-type="share"` in the same action rail as `Hi` and `喜欢`.
  - Removal of top-right `stage-share-btn` from the hero card.

- [ ] **Step 1: Read the current home-page share and floating-action markup**

Read these sections in `UI/uniapp-marriage/pages/index/index.uvue` before editing:

```vue
<view class="profile-stage-topbar">
  <view class="stage-progress-chip">
    <text class="stage-progress-text">{{ recommendIndex + 1 }}/{{ recommendMembers.length }}</text>
  </view>
  <view class="stage-action-group">
    <button class="stage-share-btn" open-type="share">
      <text class="stage-share-btn-text">分享</text>
    </button>
    <view v-if="hasHeroPhoto" class="profile-preview-chip">
      <text class="profile-preview-chip-text">查看大图</text>
    </view>
  </view>
</view>
```

and

```vue
<view
  v-if="currentRecommendMember.id > 0"
  class="floating-action floating-action-hi"
  :class="{ 'floating-action-hidden': !actionBarVisible }"
  @tap="sendGreeting"
>
  <text class="action-button-icon action-button-icon-hi">Hi</text>
</view>
<view
  v-if="currentRecommendMember.id > 0"
  class="floating-action floating-action-love"
  :class="{ 'floating-action-hidden': !actionBarVisible }"
  @tap="sendFollowNotice"
>
  <text class="action-button-icon action-button-icon-love">❤</text>
</view>
```

Expected: you can point to the exact top share button and the exact bottom action rail that will receive the new share button.

- [ ] **Step 2: Remove the top share button but keep the preview chip**

Change the hero-card topbar so it no longer renders the top-right share button.

Use this target structure:

```vue
<view class="profile-stage-topbar">
  <view class="stage-progress-chip">
    <text class="stage-progress-text">{{ recommendIndex + 1 }}/{{ recommendMembers.length }}</text>
  </view>
  <view v-if="hasHeroPhoto" class="profile-preview-chip">
    <text class="profile-preview-chip-text">查看大图</text>
  </view>
</view>
```

Expected diff shape:
- remove `.stage-action-group` wrapper usage from template
- remove `button.stage-share-btn`
- keep `profile-preview-chip` visible when `hasHeroPhoto`

- [ ] **Step 3: Add the new home-page floating share button in the same rail**

Insert a new share button before the `Hi` button so the visual order is `分享 / Hi / 喜欢`.

Use this exact template shape:

```vue
<view
  v-if="currentRecommendMember.id > 0"
  class="floating-action floating-action-share"
  :class="{ 'floating-action-hidden': !actionBarVisible }"
>
  <button class="floating-action-btn floating-action-btn-share" open-type="share">
    <text class="action-button-icon action-button-icon-share">↗</text>
  </button>
</view>
<view
  v-if="currentRecommendMember.id > 0"
  class="floating-action floating-action-hi"
  :class="{ 'floating-action-hidden': !actionBarVisible }"
  @tap="sendGreeting"
>
  <text class="action-button-icon action-button-icon-hi">Hi</text>
</view>
<view
  v-if="currentRecommendMember.id > 0"
  class="floating-action floating-action-love"
  :class="{ 'floating-action-hidden': !actionBarVisible }"
  @tap="sendFollowNotice"
>
  <text class="action-button-icon action-button-icon-love">❤</text>
</view>
```

Expected: the new share button inherits the same show/hide timing via `actionBarVisible` and still uses native share behavior via `open-type="share"`.

- [ ] **Step 4: Replace the home-page floating-action CSS with a 3-button rail**

In `UI/uniapp-marriage/pages/index/index.uvue`, replace the old two-button positioning with a three-button layout that matches the detail-page share button structure.

Use this CSS block as the target for the floating action region:

```css
.floating-action {
  position: fixed;
  bottom: 88px;
  width: 60px;
  height: 60px;
  border-radius: 30px;
  align-items: center;
  justify-content: center;
  z-index: 40;
  opacity: 1;
  transform: translate3d(0, 0, 0);
  transition-property: opacity, transform;
  transition-duration: 220ms;
}

.floating-action-hidden {
  opacity: 0;
  transform: translate3d(0, 84px, 0) scale(0.92);
  pointer-events: none;
}

.floating-action-share {
  left: 50%;
  margin-left: -144px;
}

.floating-action-btn {
  width: 60px;
  height: 60px;
  border-radius: 30px;
  justify-content: center;
  align-items: center;
  background-color: rgba(255, 249, 245, 0.96);
  border-width: 1px;
  border-color: rgba(206, 175, 168, 0.8);
  box-shadow: 0 8px 20px rgba(106, 73, 67, 0.12);
}

.floating-action-btn::after {
  border: none;
}

.floating-action-hi {
  left: 50%;
  margin-left: -68px;
  background-color: rgba(255, 249, 245, 0.96);
  border-width: 1px;
  border-color: rgba(206, 175, 168, 0.8);
  box-shadow: 0 8px 20px rgba(106, 73, 67, 0.12);
}

.floating-action-love {
  left: 50%;
  margin-left: 8px;
  background-color: #C84449;
  box-shadow: 0 10px 22px rgba(200, 68, 73, 0.24);
}

.action-button-icon {
  font-size: 20px;
  line-height: 20px;
  color: #6b5550;
  font-weight: 700;
}

.action-button-icon-share,
.action-button-icon-hi {
  font-size: 18px;
  line-height: 18px;
}

.action-button-icon-love {
  color: #ffffff;
}
```

Expected: homepage buttons are centered as a three-button rail, and the share button no longer uses the old pill style from `.stage-share-btn`.

- [ ] **Step 5: Remove obsolete top-share CSS from the home page**

Delete CSS that only existed for the removed top share button.

Remove these now-unused rules if they are no longer referenced:

```css
.stage-action-group {
  flex-direction: row;
  align-items: center;
}

.stage-share-btn {
  height: 32px;
  padding: 0 12px;
  border-radius: 16px;
  margin-right: 8px;
  background-color: rgba(255, 249, 245, 0.88);
  border-width: 1px;
  border-color: rgba(255, 255, 255, 0.36);
  justify-content: center;
  align-items: center;
}

.stage-share-btn::after {
  border: none;
}

.stage-share-btn-text {
  font-size: 12px;
  line-height: 16px;
  font-weight: 700;
  color: #734b46;
}
```

Expected: the home page has no dead CSS for the removed top share button.

- [ ] **Step 6: Manually review the home-page diff before moving on**

Check that the post-edit template still preserves these unchanged behaviors:

```ts
onShareAppMessage(() => ({
  title: shareTitle.value,
  path: buildSharePath(currentUserId.value, currentRecommendMember.value.id),
  imageUrl: shareImageUrl.value
}))
```

and

```ts
onShareTimeline(() => ({
  title: shareTitle.value,
  query: buildShareQuery(currentUserId.value, currentRecommendMember.value.id),
  imageUrl: shareImageUrl.value
}))
```

Expected: only the share entry point moved; no share payload logic changed.

- [ ] **Step 7: Commit Task 1**

Run:

```bash
git add UI/uniapp-marriage/pages/index/index.uvue
git commit -m "feat: move home share action into floating rail"
```

Expected: one focused commit containing only homepage share-entry migration and style unification.

## Task 2: Make the member-detail hero visually match the home recommendation card

**Files:**
- Modify: `UI/uniapp-marriage/pages/member-detail/index.uvue`

**Interfaces:**
- Consumes:
  - Existing `member`, `displayHeroImage`, `displayAvatarImage`, `displayAge`, `displayBioText`, and `showInteractionButtons` in `member-detail/index.uvue`.
  - Existing share callback and interaction handlers already defined in `member-detail/index.uvue`.
- Produces:
  - A detail-page hero that uses the same visual layers as the home recommendation card but without swipe UI.
  - A three-button action rail whose share/Hi/love visuals match the home page.

- [ ] **Step 1: Read the current detail-page hero and floating-action blocks**

Read these sections in `UI/uniapp-marriage/pages/member-detail/index.uvue` before editing:

```vue
<view class="profile-card">
  <view class="profile-stage" @tap="openProfileImagePreview">
    <image class="profile-stage-image" :src="displayHeroImage" mode="aspectFill"></image>
    <view class="profile-stage-scrim"></view>
    <view class="profile-stage-top">
      <view class="profile-preview-chip">
        <text class="profile-preview-chip-text">点击查看大图</text>
      </view>
    </view>

    <view class="profile-stage-bottom">
      <view class="profile-identity">
        <image class="profile-avatar" :src="displayAvatarImage" mode="aspectFill"></image>
        <view class="profile-meta">
          <view class="profile-name-row">
            <text class="profile-name">{{ member.name }}</text>
            <text v-if="displayAge != ''" class="profile-age">{{ displayAge }}</text>
            <view v-if="member.verifiedLabel != ''" class="verified-pill">
              <text class="verified-pill-text">{{ member.verifiedLabel }}</text>
            </view>
          </view>
          <text class="profile-bio">“{{ displayBioText }}”</text>
        </view>
      </view>
    </view>
  </view>
</view>
```

and

```vue
<view v-if="showInteractionButtons" class="floating-action floating-action-share">
  <button class="floating-action-btn" open-type="share">
    <text class="action-button-icon action-button-icon-share">↗</text>
  </button>
</view>
<view v-if="showInteractionButtons" class="floating-action floating-action-hi" @tap="sendGreeting">
  <text class="action-button-icon action-button-icon-hi">Hi</text>
</view>
<view v-if="showInteractionButtons" class="floating-action floating-action-love" @tap="sendFollowNotice">
  <text class="action-button-icon action-button-icon-love">❤</text>
</view>
```

Expected: you understand which detail-page hero sections will be replaced and which handlers stay untouched.

- [ ] **Step 2: Add homepage-style computed data needed for the hero card**

Before editing the detail-page template, add the extra computed values needed to mirror the home recommendation card.

Add these computed blocks near the existing `displayBioText` / `displayBasicInfoGrid` declarations:

```ts
const hasHeroPhoto = computed(() => member.value.mainImage != '' || member.value.albumImages.length > 0)

const displayHeroSummary = computed(() => {
  const parts: string[] = []
  const current = member.value
  if (current.city != null && current.city.length > 0) parts.push(current.city)
  if (current.job != null && current.job.length > 0) parts.push(current.job)
  if (current.education != null && current.education.length > 0) parts.push(current.education)
  if (parts.length > 0) return parts.join(' / ')
  return hasHeroPhoto.value ? '资料已开放查看' : '资料待完善'
})

const displayQuickFacts = computed((): string[] => {
  const facts: string[] = []
  const current = member.value
  if (current.city != null && current.city.length > 0) facts.push(current.city)
  if (current.job != null && current.job.length > 0) facts.push(current.job)
  if (current.education != null && current.education.length > 0) facts.push(current.education)
  if (current.income != null && current.income.length > 0) facts.push(current.income)
  return facts
})
```

Expected: the detail page can render the same summary row and quick-fact chips as the home page without changing API shape.

- [ ] **Step 3: Replace the detail-page hero template with a no-swipe version of the home card**

Rewrite the detail-page hero so it matches the home recommendation card structure while omitting swipe-only elements.

Use this template shape as the target:

```vue
<view class="profile-card">
  <view class="profile-stage" @tap="openProfileImagePreview">
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
              <text class="profile-name">{{ member.name }}</text>
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
```

Expected: the detail-page hero now reads like the home recommendation card’s static variant instead of the old simpler hero.

- [ ] **Step 4: Replace the detail-page hero CSS with the homepage visual language**

Update the detail-page style block to match the homepage card values for the shared hero pieces.

Copy or align these selectors from `UI/uniapp-marriage/pages/index/index.uvue` into `UI/uniapp-marriage/pages/member-detail/index.uvue`, adjusting only the detail-only chip label class where needed:

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

.profile-name-row {
  flex-direction: row;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 6px;
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

.profile-quick-facts {
  flex-direction: row;
  flex-wrap: wrap;
  margin-top: 10px;
}

.quick-fact-chip {
  height: 28px;
  padding: 0 10px;
  border-radius: 14px;
  margin-right: 8px;
  margin-bottom: 6px;
  background-color: rgba(245, 235, 230, 0.96);
}
```

Add one detail-only helper if needed:

```css
.stage-progress-chip-detail {
  background-color: rgba(255, 249, 245, 0.92);
}
```

Expected: the detail-page hero matches the home recommendation card’s proportions, overlay, and identity-card rhythm.

- [ ] **Step 5: Normalize the detail-page floating share button to the same class structure as the home page**

Update the detail-page floating share button to use the same class structure as the home-page share button.

Use this exact template shape:

```vue
<view v-if="showInteractionButtons" class="floating-action floating-action-share">
  <button class="floating-action-btn floating-action-btn-share" open-type="share">
    <text class="action-button-icon action-button-icon-share">↗</text>
  </button>
</view>
```

Then align the button CSS to match the home page exactly:

```css
.floating-action {
  position: fixed;
  bottom: 88px;
  width: 60px;
  height: 60px;
  border-radius: 30px;
  align-items: center;
  justify-content: center;
  z-index: 40;
}

.floating-action-share {
  left: 50%;
  margin-left: -144px;
}

.floating-action-btn {
  width: 60px;
  height: 60px;
  border-radius: 30px;
  justify-content: center;
  align-items: center;
  background-color: rgba(255, 249, 245, 0.96);
  border-width: 1px;
  border-color: rgba(206, 175, 168, 0.8);
  box-shadow: 0 8px 20px rgba(106, 73, 67, 0.12);
}

.floating-action-btn::after {
  border: none;
}

.floating-action-hi {
  left: 50%;
  margin-left: -68px;
  background-color: rgba(255, 249, 245, 0.96);
  border-width: 1px;
  border-color: rgba(206, 175, 168, 0.8);
  box-shadow: 0 8px 20px rgba(106, 73, 67, 0.12);
}

.floating-action-love {
  left: 50%;
  margin-left: 8px;
  background-color: #C84449;
  box-shadow: 0 10px 22px rgba(200, 68, 73, 0.24);
}
```

Expected: the share button is the same size, spacing, and rail alignment as the home page instead of sitting farther right.

- [ ] **Step 6: Keep the detail-page business handlers unchanged**

Before committing, verify that these handlers remain intact apart from surrounding template/style edits:

```ts
onShareAppMessage(() => ({
  title: shareTitle.value,
  path: buildSharePath(currentUserId.value, member.value.id),
  imageUrl: shareImageUrl.value
}))
```

```ts
function sendGreeting(): void {
  if (requireLoginForInteraction()) return
  checkRealVerified().then((result) => {
    if (result.needLogin) {
      loginSheetVisible.value = true
      return
    }
    if (!result.isVerified) {
      verifiedSheetVisible.value = true
      return
    }
    openCurrentMemberChat()
  }).catch(() => {
    openCurrentMemberChat()
  })
}
```

```ts
function sendFollowNotice(): void {
  if (requireLoginForInteraction()) return
  checkRealVerified().then((result) => {
    if (result.needLogin) {
      loginSheetVisible.value = true
      return
    }
    if (!result.isVerified) {
      verifiedSheetVisible.value = true
      return
    }
    doFollowPartner()
  }).catch(() => {
    doFollowPartner()
  })
}
```

Expected: the detail page has only visual/structural changes; share, greeting, and follow behaviors still call the same logic.

- [ ] **Step 7: Commit Task 2**

Run:

```bash
git add UI/uniapp-marriage/pages/member-detail/index.uvue
git commit -m "feat: align member detail hero with home card"
```

Expected: one focused commit containing detail-page hero alignment and button-rail normalization.

## Task 3: Verify cross-page visual parity and interaction behavior

**Files:**
- Modify: `UI/uniapp-marriage/pages/index/index.uvue` (only if verification finds a parity issue)
- Modify: `UI/uniapp-marriage/pages/member-detail/index.uvue` (only if verification finds a parity issue)

**Interfaces:**
- Consumes:
  - The updated home-page rail from Task 1.
  - The updated detail-page hero and rail from Task 2.
- Produces:
  - Final parity polish ensuring both pages share the same button sizes, spacing, and visual language.

- [ ] **Step 1: Launch the uni-app target you normally use for marriage-page visual checks**

From the project’s normal UI workflow, start the app target you already use for `UI/uniapp-marriage` manual inspection.

Run one of the project’s existing local workflows; do not invent a new toolchain. Example placeholder structure only—replace with the project’s actual established command before execution:

```bash
# use the project's existing uni-app dev command for UI/uniapp-marriage
```

Expected: homepage and member-detail page can be opened locally for manual comparison.

- [ ] **Step 2: Verify homepage acceptance points**

Open the homepage and confirm all of the following in one pass:

```text
1. 顶部右上角不再出现独立分享按钮
2. 底部右侧为三枚操作：分享 / Hi / 喜欢
3. 三枚按钮都在同一视觉轴线上
4. 向下滚动时三枚按钮一起隐藏；回到可见区时一起出现
5. 点击分享仍走系统分享能力
```

Expected: all five checks pass with no button-size mismatch and no leftover top-share pill.

- [ ] **Step 3: Verify member-detail acceptance points**

Open a member-detail page and confirm all of the following in one pass:

```text
1. 头部主视觉看起来是首页推荐卡的静态版
2. 顶部为左侧信息胶囊 + 右侧查看大图胶囊
3. 底部为身份信息卡 + quick fact chips
4. 悬浮按钮为分享 / Hi / 喜欢三枚，位置与首页一致
5. 点击分享、Hi、喜欢分别还能正常触发原有能力
```

Expected: the hero no longer looks like the old standalone profile header; it matches the home card language without swipe affordances.

- [ ] **Step 4: Fix any parity drift immediately if found**

If verification finds a mismatch, apply only the smallest necessary edit. Common acceptable fixes include these exact categories:

```text
- 调整 floating-action-share 的 margin-left
- 统一 floating-action-btn 与 floating-action-hi 的 border / shadow
- 收敛 profile-stage-topbar 或 profile-stage-bottom 的 inset 值
- 调整 quick-fact-chip 的 spacing 以贴齐首页
```

Expected: do not widen scope beyond parity polish; keep fixes strictly tied to the approved spec.

- [ ] **Step 5: Record the final verification result in the commit message scope**

If Step 4 required code changes, commit them with:

```bash
git add UI/uniapp-marriage/pages/index/index.uvue UI/uniapp-marriage/pages/member-detail/index.uvue
git commit -m "fix: polish shared marriage action visuals"
```

If Step 4 required no code changes, do not create an extra commit.

Expected: final history is either two clean feature commits or two feature commits plus one tiny parity-polish fix commit.

## Spec Coverage Check

- 首页右上角分享按钮移到底部悬浮操作组：Task 1
- 首页与详情页统一为 `分享 / Hi / 喜欢` 三枚圆形按钮：Task 1, Task 2, Task 3
- 详情页头部主视觉向首页推荐卡靠齐：Task 2
- 详情页不引入滑动切换：Task 2 explicitly uses a static card variant only
- 保持分享/关注/打招呼业务能力不变：Task 1 Step 6, Task 2 Step 6, Task 3 verification

## Self-Review Notes

- No placeholders like `TODO` / `TBD` remain in tasks.
- Later tasks reuse the same class names introduced earlier: `floating-action-share`, `floating-action-btn`, `profile-stage-topbar`, `profile-identity-card`, `displayHeroSummary`, `displayQuickFacts`.
- The plan stays within the approved scope: two page files only, no component extraction, no swipe feature work.
