# 互相关注联系人页改造 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将婚恋端消息页的“互相关注”入口改造成新的聊天发起入口，移除首页红色悬浮按钮，并将 `pages/messages/mutual/index` 复用为只展示互相关注联系人的纯联系人页。

**Architecture:** 保持现有 `UI/uniapp-marriage` 页面路由不变，继续使用 `pages/messages/mutual/index` 作为“互相关注”入口页，但将其数据源从互动通知切换为互动关系接口。消息首页继续负责快捷入口与已有会话列表，联系人页负责展示 `mutual = true` 的联系人并复用现有聊天页跳转参数。

**Tech Stack:** uni-app uvue、UTS、现有 `request` HTTP 封装、`member-interaction.uts` 互动关系接口、现有聊天页 `pages/messages/chat/index.uvue`

## Global Constraints

- 仅修改 `UI/uniapp-marriage` 前端页面与 UTS 接口封装，不新增后端接口。
- “互相关注”入口路由必须继续使用 `/pages/messages/mutual/index`。
- 联系人页必须只展示互相关注联系人，不保留提醒流、左滑删除、未读态、提醒时间。
- 联系人卡片采用简洁版：头像、昵称、城市/年龄，点击整行直接进入聊天页。
- 消息首页必须移除右下角红色悬浮按钮及“私信和新对话功能后续接入”占位提示。
- 聊天页继续复用 `UI/uniapp-marriage/pages/messages/chat/index.uvue`，不新增“创建会话”接口。
- 页面状态至少覆盖加载中、空态、错误态；空态文案为“暂无互相关注联系人”。
- 保持现有消息模块 UI 风格与 `navigationStyle: custom` 页面模式。
- 默认不主动运行 Maven 编译；本次仅涉及 uni-app 前端代码。

---

## File Structure

- Modify: `UI/uniapp-marriage/utils/member-interaction.uts`
  - 增加更明确的互相关注联系人查询方法，避免页面层重复分页+过滤逻辑。
- Modify: `UI/uniapp-marriage/pages/messages/mutual/index.uvue`
  - 从提醒消息页改造成纯联系人页，接入互相关注数据、状态展示与聊天跳转。
- Modify: `UI/uniapp-marriage/pages/messages/index.uvue`
  - 移除红色悬浮按钮和相关占位文案，保留现有快捷入口与会话列表逻辑。

## Task 1: Add a mutual-contact data method

**Files:**
- Modify: `UI/uniapp-marriage/utils/member-interaction.uts`
- Test: `UI/uniapp-marriage/utils/member-interaction.uts`（通过静态检查 + 页面调用接入验证）

**Interfaces:**
- Consumes:
  - `request<CommonResult<PageResult<MemberRelationItem>>>(options)` from `UI/uniapp-marriage/utils/http.uts`
  - Existing `export type MemberRelationItem`
- Produces:
  - `export function getMutualFollowPage(req: PageReq = {}): Promise<PageResult<MemberRelationItem>>`

- [ ] **Step 1: Write the failing test**

Add the exported function with a placeholder rejection so the page task can import a stable interface before the real request is implemented.

```ts
export function getMutualFollowPage(req: PageReq = {}): Promise<PageResult<MemberRelationItem>> {
	return Promise.reject('not implemented')
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `rg -n "getMutualFollowPage|not implemented" UI/uniapp-marriage/utils/member-interaction.uts`

Expected:
- One hit for `export function getMutualFollowPage`
- One hit for `Promise.reject('not implemented')`

- [ ] **Step 3: Write minimal implementation**

Replace the placeholder with a real request that reuses the existing “我关注的列表”接口 and filters to only mutual contacts.

```ts
export function getMutualFollowPage(req: PageReq = {}): Promise<PageResult<MemberRelationItem>> {
	return request<CommonResult<PageResult<MemberRelationItem>>>({
		url: '/marriage/interaction/my-follow-page',
		method: 'GET',
		data: {
			pageNo: req.pageNo != null ? req.pageNo : 1,
			pageSize: req.pageSize != null ? req.pageSize : 100
		}
	}).then((response) => {
		if (response.code !== 0 || response.data == null) {
			return Promise.reject(response.msg != '' ? response.msg : '获取互相关注联系人失败')
		}
		const list = (response.data.list || []).filter((item) => item.mutual)
		return {
			list,
			total: list.length
		} as PageResult<MemberRelationItem>
	})
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `rg -n "getMutualFollowPage|获取互相关注联系人失败|item\.mutual" UI/uniapp-marriage/utils/member-interaction.uts`

Expected:
- A hit for `export function getMutualFollowPage`
- A hit for `获取互相关注联系人失败`
- A hit for `.filter((item) => item.mutual)`
- No hit for `not implemented`

- [ ] **Step 5: Commit**

```bash
git add UI/uniapp-marriage/utils/member-interaction.uts
git commit -m "feat: add mutual follow contact query"
```

## Task 2: Convert the mutual page into a pure contact list

**Files:**
- Modify: `UI/uniapp-marriage/pages/messages/mutual/index.uvue`
- Test: `UI/uniapp-marriage/pages/messages/mutual/index.uvue`（通过静态检查 + 路由拼接检查）

**Interfaces:**
- Consumes:
  - `getMutualFollowPage(req?: PageReq): Promise<PageResult<MemberRelationItem>>` from `UI/uniapp-marriage/utils/member-interaction.uts`
  - `resolveMemberAvatar(avatar: string): string` from `UI/uniapp-marriage/utils/member-auth.uts`
- Produces:
  - `function loadContacts(): void`
  - `function openChat(item: MemberRelationItem): void`
  - `function resolveContactMeta(item: MemberRelationItem): string`

- [ ] **Step 1: Write the failing test**

Replace the old reminder-specific imports and state with a contact-page skeleton. Keep the page intentionally incomplete by rendering an empty list and a placeholder loader message.

```ts
<script setup lang="uts">
import { getMutualFollowPage, type MemberRelationItem } from '../../../utils/member-interaction.uts'
import { resolveMemberAvatar } from '../../../utils/member-auth.uts'

const loading = ref(false)
const loadError = ref('')
const contacts = ref<MemberRelationItem[]>([])

onShow(() => {
	loadContacts()
})

function loadContacts(): void {
	loading.value = true
	loadError.value = ''
	contacts.value = []
	getMutualFollowPage({ pageNo: 1, pageSize: 100 }).finally(() => {
		loading.value = false
	})
}
</script>
```

Run a template placeholder with the new header and empty-state copy:

```vue
<text class="header-title">互相关注</text>
<text class="intro-title">可以开始聊天的人</text>
<text class="empty-text">暂无互相关注联系人</text>
```

- [ ] **Step 2: Run test to verify it fails**

Run: `rg -n "getInteractionNotificationPage|readInteractionNotificationsByScene|message-swipe-row|去聊天|左滑|暂无互相关注联系人" UI/uniapp-marriage/pages/messages/mutual/index.uvue`

Expected before finishing the task:
- Old reminder hits still exist for `getInteractionNotificationPage` or `message-swipe-row`
- New copy hit exists for `暂无互相关注联系人`

- [ ] **Step 3: Write minimal implementation**

Replace the entire page logic with a contact list implementation. Use the following script section.

```ts
<script setup lang="uts">
import { resolveMemberAvatar } from '../../../utils/member-auth.uts'
import { getMutualFollowPage, type MemberRelationItem } from '../../../utils/member-interaction.uts'

const loading = ref(false)
const loadError = ref('')
const contacts = ref<MemberRelationItem[]>([])

onShow(() => {
	loadContacts()
})

function loadContacts(): void {
	loading.value = true
	loadError.value = ''
	getMutualFollowPage({ pageNo: 1, pageSize: 100 }).then((page) => {
		contacts.value = page.list || []
	}).catch((error) => {
		loadError.value = typeof error == 'string' ? error : '加载失败，请稍后重试'
		contacts.value = []
	}).finally(() => {
		loading.value = false
	})
}

function openChat(item: MemberRelationItem): void {
	if (item.partnerId <= 0) {
		uni.showToast({ title: '缺少聊天对象', icon: 'none' })
		return
	}
	uni.navigateTo({
		url: '/pages/messages/chat/index?peerId=' + String(item.partnerId)
			+ '&nickname=' + encodeURIComponent(resolveNickname(item))
			+ '&avatar=' + encodeURIComponent(item.avatar || '')
	})
}

function resolveNickname(item: MemberRelationItem): string {
	return item.nickname != '' ? item.nickname : '互相关注用户'
}

function resolveAvatar(item: MemberRelationItem): string {
	return resolveMemberAvatar(item.avatar || '')
}

function resolveAvatarText(item: MemberRelationItem): string {
	const nickname = resolveNickname(item)
	return nickname.length > 0 ? nickname.substring(0, 1) : '互'
}

function resolveContactMeta(item: MemberRelationItem): string {
	const parts: string[] = []
	if (item.age > 0) {
		parts.push(String(item.age) + '岁')
	}
	if (item.city != '') {
		parts.push(item.city)
	}
	return parts.join(' · ')
}

function goBack(): void {
	uni.navigateBack()
}
</script>
```

Use the following template shape.

```vue
<template>
	<view class="page">
		<view class="header">
			<view class="header-bar">
				<view class="header-action" @tap="goBack">
					<view class="header-back-arrow"></view>
				</view>
				<text class="header-title">互相关注</text>
				<view class="header-action"></view>
			</view>
		</view>

		<scroll-view class="scroll" :scroll-y="true">
			<view class="content">
				<view class="intro-card">
					<text class="intro-title">可以开始聊天的人</text>
					<text class="intro-desc">这里展示所有互相关注联系人，点击任意联系人即可直接发起聊天。</text>
				</view>

				<view v-if="loading" class="empty-card">
					<text class="empty-text">加载联系人中...</text>
				</view>
				<view v-else-if="loadError != ''" class="empty-card" @tap="loadContacts">
					<text class="empty-text">{{ loadError }}</text>
				</view>
				<view v-else-if="contacts.length == 0" class="empty-card">
					<text class="empty-text">暂无互相关注联系人</text>
				</view>
				<view v-else class="contact-list">
					<view class="contact-card" v-for="item in contacts" :key="'mutual-' + String(item.partnerId)" @tap="openChat(item)">
						<view class="contact-avatar">
							<image v-if="resolveAvatar(item) != ''" class="contact-avatar-image" :src="resolveAvatar(item)" mode="aspectFill"></image>
							<text v-else class="contact-avatar-text">{{ resolveAvatarText(item) }}</text>
						</view>
						<view class="contact-main">
							<text class="contact-name">{{ resolveNickname(item) }}</text>
							<text v-if="resolveContactMeta(item) != ''" class="contact-meta">{{ resolveContactMeta(item) }}</text>
						</view>
						<text class="contact-action">聊天</text>
					</view>
				</view>
			</view>
		</scroll-view>
	</view>
</template>
```

Replace the old reminder/list CSS with focused contact styles.

```css
.contact-list {
	gap: 12px;
}

.contact-card {
	background-color: #fffdfa;
	border-radius: 24px;
	border-width: 1px;
	border-color: rgba(202, 173, 164, 0.26);
	box-shadow: 0 12px 28px rgba(205, 175, 160, 0.12);
	padding: 16px;
	flex-direction: row;
	align-items: center;
	margin-bottom: 12px;
}

.contact-avatar {
	width: 52px;
	height: 52px;
	border-radius: 26px;
	background-color: #89a88d;
	justify-content: center;
	align-items: center;
	margin-right: 12px;
	overflow: hidden;
}

.contact-avatar-image {
	width: 52px;
	height: 52px;
	border-radius: 26px;
}

.contact-avatar-text {
	font-size: 20px;
	line-height: 24px;
	font-weight: 800;
	color: #ffffff;
}

.contact-main {
	flex: 1;
}

.contact-name {
	font-size: 17px;
	line-height: 23px;
	font-weight: 800;
	color: #1c1c19;
	margin-bottom: 4px;
}

.contact-meta {
	font-size: 14px;
	line-height: 20px;
	color: #866c67;
}

.contact-action {
	font-size: 14px;
	line-height: 20px;
	font-weight: 800;
	color: #C94A4D;
	margin-left: 12px;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `rg -n "getMutualFollowPage|加载联系人中|暂无互相关注联系人|openChat\(|contact-card|message-swipe-row|getInteractionNotificationPage" UI/uniapp-marriage/pages/messages/mutual/index.uvue`

Expected:
- Hits for `getMutualFollowPage`, `加载联系人中`, `暂无互相关注联系人`, `openChat(`, `contact-card`
- No hits for `message-swipe-row`
- No hits for `getInteractionNotificationPage`

- [ ] **Step 5: Commit**

```bash
git add UI/uniapp-marriage/pages/messages/mutual/index.uvue
git commit -m "feat: convert mutual page to contacts"
```

## Task 3: Remove the compose FAB from the messages home page

**Files:**
- Modify: `UI/uniapp-marriage/pages/messages/index.uvue`
- Test: `UI/uniapp-marriage/pages/messages/index.uvue`（通过静态检查）

**Interfaces:**
- Consumes:
  - Existing `quickEntries`, `conversationItems`, `openQuickEntry`, `openConversation`
- Produces:
  - Messages home page with no compose-button block and no `composeHint` dependency

- [ ] **Step 1: Write the failing test**

Keep the existing page behavior, but first mark the red FAB and its copy as the removal target.

Run: `rg -n "compose-button|composeHint|私信和新对话功能后续接入" UI/uniapp-marriage/pages/messages/index.uvue`

Expected:
- A hit for the floating button container
- A hit for `composeHint`
- A hit for `私信和新对话功能后续接入`

- [ ] **Step 2: Run test to verify it fails**

This step fails by definition because the old compose entry still exists.

Run: `rg -n "compose-button|composeHint" UI/uniapp-marriage/pages/messages/index.uvue`

Expected:
- At least one hit for both patterns

- [ ] **Step 3: Write minimal implementation**

Delete the floating button block from the template.

```vue
<view class="compose-button" @tap="showLaterHint(ui.composeHint)">
	<image class="compose-button-image" src="/static/icons/messages/compose.svg" mode="aspectFit"></image>
</view>
```

Delete the placeholder copy from the `ui` object.

```ts
const ui = {
	title: '消息',
	bannerTitle: '有人关注你时会第一时间提醒',
	bannerDesc: '互相关注后也会同步通知双方，方便继续互动。',
	bannerAction: '知道了',
	bannerHint: '当前展示的是站内信提醒',
	listTitle: '最新消息',
	listAction: '全部已读'
}
```

Delete the unused floating-button styles.

```css
.compose-button {
	/* remove this whole block */
}

.compose-button-image {
	/* remove this whole block */
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `rg -n "compose-button|composeHint|私信和新对话功能后续接入" UI/uniapp-marriage/pages/messages/index.uvue`

Expected:
- No hits

Run: `rg -n "label: '互相关注'|route: '/pages/messages/mutual/index'" UI/uniapp-marriage/pages/messages/index.uvue`

Expected:
- One hit for the `互相关注` label
- One hit for the route `/pages/messages/mutual/index`

- [ ] **Step 5: Commit**

```bash
git add UI/uniapp-marriage/pages/messages/index.uvue
git commit -m "feat: remove messages compose shortcut"
```

## Task 4: Verify the mutual-contact flow end to end

**Files:**
- Modify: none
- Test: `UI/uniapp-marriage/pages/messages/index.uvue`
- Test: `UI/uniapp-marriage/pages/messages/mutual/index.uvue`
- Test: `UI/uniapp-marriage/pages/messages/chat/index.uvue`

**Interfaces:**
- Consumes:
  - `openQuickEntry(item)` from messages home page
  - `openChat(item)` from mutual contacts page
  - Existing chat page query parameters `peerId`, `nickname`, `avatar`
- Produces:
  - Verified UI flow from messages home → mutual contacts → chat page

- [ ] **Step 1: Write the failing test**

Capture the exact strings and route wiring that define the feature.

Run: `rg -n "互相关注|/pages/messages/mutual/index|/pages/messages/chat/index|peerId=|nickname=|avatar=" UI/uniapp-marriage/pages/messages/index.uvue UI/uniapp-marriage/pages/messages/mutual/index.uvue UI/uniapp-marriage/pages/messages/chat/index.uvue`

Expected before the final check:
- Hits for all three page paths/parameters
- If any query parameter string is missing from the mutual page, treat that as a failure

- [ ] **Step 2: Run test to verify it fails**

Run: `git diff -- UI/uniapp-marriage/pages/messages/index.uvue UI/uniapp-marriage/pages/messages/mutual/index.uvue UI/uniapp-marriage/utils/member-interaction.uts`

Expected:
- Shows exactly the three touched files for this feature
- If unrelated changes appear in these files, stop and separate them before continuing

- [ ] **Step 3: Write minimal implementation**

No new code in this task. Instead, manually inspect the three files and confirm the final behavior checklist:

```text
1. 消息首页没有红色悬浮按钮
2. “互相关注”入口仍跳 /pages/messages/mutual/index
3. mutual 页面只使用 getMutualFollowPage，不再使用提醒通知接口
4. mutual 页面空态文案为“暂无互相关注联系人”
5. 点击联系人跳 /pages/messages/chat/index，并带 peerId、nickname、avatar 参数
6. chat 页面继续从 onLoad 读取 peerId / nickname / avatar
```

- [ ] **Step 4: Run test to verify it passes**

Run: `rg -n "getMutualFollowPage|暂无互相关注联系人|peerId =|options\['peerId'\]|decodeURIComponent\(rawName\)|compose-button" UI/uniapp-marriage/pages/messages/mutual/index.uvue UI/uniapp-marriage/pages/messages/chat/index.uvue UI/uniapp-marriage/pages/messages/index.uvue`

Expected:
- A hit for `getMutualFollowPage`
- A hit for `暂无互相关注联系人`
- A hit in chat page for `options['peerId']`
- A hit in chat page for `decodeURIComponent(rawName)`
- No hit for `compose-button` in messages home page

- [ ] **Step 5: Commit**

```bash
git add UI/uniapp-marriage/utils/member-interaction.uts UI/uniapp-marriage/pages/messages/mutual/index.uvue UI/uniapp-marriage/pages/messages/index.uvue
git commit -m "feat: add mutual follow contacts entry"
```
