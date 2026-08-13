<template>
	<view class="page">
		<view class="hero-top-bg">
			<image class="hero-bg-image" src="@/static/images/index-bg.jpg" mode="widthFix"></image>
			<view class="hero-top-content">
				<CustomNav title="战友会" background="transparent" color="#fff" />
				<view class="hero">
					<view class="hero-top">
						<view>
							<text class="hero-title">全国战友会</text>
							<text class="hero-desc">连接各省市战友会，记录聚会风采，服务退役军人</text>
							<text v-if="stats.totalCount > 0" class="hero-stats">平台已经加入全部的战友 {{ stats.totalCount }} 名</text>
							<!-- <text v-if="stats.provinceCount > 0" class="hero-stats province-stats">{{ currentProvinceName }} 战友 {{ stats.provinceCount }} 名</text> -->
							<text class="hero-stats welcome-stats">让我们再次欢聚一堂!</text>
						</view>
						<!-- 已入会标签暂时隐藏
						<view v-if="memberInfo.is_member" class="member-badge">已入会</view>
						-->
					</view>
					<!-- member-card 暂时隐藏
					<view v-if="memberInfo.is_member" class="member-card">
						<text class="member-name">{{ memberInfo.name || '战友' }}</text>
						<text class="member-meta">{{ memberInfo.service_unit || '暂无服役单位' }}</text>
						<text class="member-meta">服役年份：{{ memberInfo.service_year || '--' }}</text>
						<text class="member-meta">入会时间：{{ memberInfo.member_time || '--' }}</text>
					</view>
					-->
				</view>

				<!-- 战友人数统计卡片 -->
				<view class="stats-cards">
					<view class="stats-card">
						<view class="stats-card-header">
							<view class="stats-card-icon">
								<uni-icons type="personadd" size="20" color="#fff"></uni-icons>
							</view>
							<text class="stats-card-badge">全部</text>
						</view>
						<text class="stats-card-label">全国战友总数</text>
						<view class="stats-card-value">
							<text class="stats-card-number">{{ formatMemberCount(stats.totalCount) }}</text>
							<text class="stats-card-unit">人</text>
						</view>
					</view>
					<view class="stats-card accent">
						<view class="stats-card-header">
							<view class="stats-card-icon light">
								<uni-icons type="location" size="20" color="#fff"></uni-icons>
							</view>
							<text class="stats-card-badge light">{{ currentProvinceName }}</text>
						</view>
						<text class="stats-card-label">{{ currentProvinceId === 0 ? '全国战友' : '该省战友' }}</text>
						<view class="stats-card-value">
							<text class="stats-card-number">{{ formatMemberCount(currentProvinceMemberCount) }}</text>
							<text class="stats-card-unit">人</text>
						</view>
					</view>
				</view>
			</view>
		</view>

		<view class="section-card" v-if="showPendingStatus && !loadingStatus">
			<view class="section-header">
				<text class="section-title">入会申请</text>
				<text class="section-subtitle">您的申请正在审核中</text>
			</view>
			<view class="status-entry pending">
				<text class="status-entry-title">{{ applicationStatus.state_display || '待审核' }}</text>
				<text class="status-entry-desc">管理员将在 1-3 个工作日内完成审核，请耐心等待</text>
			</view>
		</view>

		<view class="section-card" v-if="showRejectedStatus && !loadingStatus">
			<view class="section-header">
				<text class="section-title">入会申请</text>
				<text class="section-subtitle">审核未通过，可修改资料后重新提交</text>
			</view>
			<view class="apply-entry">
				<view class="apply-entry-text">
					<text class="apply-entry-title">{{ applicationStatus.state_display || '已驳回' }}</text>
					<text class="apply-entry-desc">请核对资料后重新申请</text>
				</view>
				<button class="apply-btn" @tap="onGoApply">重新申请</button>
			</view>
		</view>

		<view class="section-card" v-if="showApplyForm && !loadingStatus">
			<view class="section-header">
				<text class="section-title">入会申请</text>
				<text class="section-subtitle">提交资料后将由管理员审核</text>
			</view>
			<view class="apply-entry">
				<view class="apply-entry-text">
					<text class="apply-entry-title">尚未提交申请</text>
					<text class="apply-entry-desc">点击进入申请页面，填写入会资料</text>
				</view>
				<button class="apply-btn" @tap="onGoApply">立即申请</button>
			</view>
		</view>

		<view class="section-card">
			<view class="section-header">
				<text class="section-title">战友之家</text>
				<text class="section-subtitle">发布图文，记录各地聚会情况</text>
			</view>
			<scroll-view class="province-scroll" scroll-x :show-scrollbar="false" enable-flex>
				<view class="province-list">
					<view
						v-for="item in provinceList"
						:key="item.id"
						class="province-item"
						:class="{ active: currentProvinceId === item.id }"
						@tap="onChangeProvince(item)"
					>
						<text class="province-item-text">{{ formatProvinceName(item.name) }}</text>
					</view>
				</view>
			</scroll-view>

			<scroll-view
				class="feed-scroll"
				scroll-y
				refresher-enabled
				:refresher-triggered="refresherTriggered"
				@refresherrefresh="onRefresh"
				@scrolltolower="onReachBottom"
			>
				<view v-if="feedList.length === 0 && !loadingArticles" class="empty-state">
					<text class="empty-title">暂无战友动态</text>
					<text class="empty-desc">后续会展示各省战友会发布的图文动态与活动记录</text>
				</view>

				<view v-for="item in feedList" :key="item.id" class="feed-card" @tap="onFeedDetail(item)">
					<view class="feed-card-header">
						<text class="feed-title">{{ item.title }}</text>
						<text class="feed-date">{{ item.publish_date }}</text>
					</view>
					<text class="feed-summary">{{ item.summary }}</text>
					<view v-if="item.cover" class="feed-cover-wrap">
						<image class="feed-cover" :src="item.cover" mode="aspectFill"></image>
					</view>
					<view v-if="item.videos && item.videos.length" class="feed-video-wrap">
						<video class="feed-video" :src="item.videos[0].url" controls object-fit="cover"></video>
					</view>
				</view>

				<view v-if="loadingArticles" class="loading-wrap">
					<text class="loading-text">加载中...</text>
				</view>
				<view v-else-if="!hasMore && feedList.length > 0" class="loading-wrap">
					<text class="loading-text">已显示全部内容</text>
				</view>
			</scroll-view>
		</view>
	</view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad, onReachBottom, onShow } from '@dcloudio/uni-app'
import {
	getComradeStatesApi,
	getComradeApplyStatusApi,
	getComradeArticleListApi,
	formatRelativeTime,
} from '@/sheep/api/rongjh/adapter'

const pageSize = 10
const page = ref(1)
const hasMore = ref(true)
const loadingArticles = ref(false)
const refresherTriggered = ref(false)
const loadingStatus = ref(false)

const feedList = ref([])
const currentProvinceId = ref(0)
const currentProvinceName = ref('全部')

const provinceList = ref([{ id: 0, name: '全部' }])
const stats = reactive({
	totalCount: 0,
	provinceCount: 0,
})

const memberInfo = reactive({
	is_member: false,
	name: '',
	service_unit: '',
	service_year: '',
	member_time: '',
})

const applicationStatus = reactive({
	has_application: false,
	state: '',
	state_display: '',
})

const showApplyForm = computed(() => {
	return !memberInfo.is_member && !applicationStatus.has_application
})

const showPendingStatus = computed(() => {
	return !memberInfo.is_member && applicationStatus.has_application && applicationStatus.state === 'pending'
})

const showRejectedStatus = computed(() => {
	return !memberInfo.is_member && applicationStatus.has_application && applicationStatus.state === 'rejected'
})

const currentProvinceMemberCount = computed(() => {
	return currentProvinceId.value === 0 ? stats.totalCount : stats.provinceCount
})

const formatMemberCount = (count) => {
	return Number(count || 0).toLocaleString()
}

const formatProvinceName = (name = '') => {
	const value = String(name || '').trim()
	if (!value || value === '全部') return '全部'
	return value.length > 4 ? value.slice(0, 4) : value
}

const formatArticleItem = (item) => {
	const cover = item.cover_url || item.cover_image || ''
	const videoIds = Array.isArray(item.video_ids)
		? item.video_ids
		: typeof item.video_ids === 'string' && item.video_ids
			? item.video_ids.split(',').map((id) => Number(id)).filter(Boolean)
			: []
	const videos = Array.isArray(item.videos)
		? item.videos.map((video) => ({
			id: video.id,
			url: video.url || video.video_url || '',
			title: video.title || item.name || '',
		}))
		: []
	return {
		id: item.id,
		title: item.name || item.title || '',
		summary: item.summary || item.introduction || '',
		publish_date: formatRelativeTime(item.publish_date),
		cover,
		video_ids: videoIds,
		videos,
	}
}

const loadBaseInfo = async () => {
	loadingStatus.value = true
	try {
		const [provinceRes, statusRes] = await Promise.allSettled([
			getComradeStatesApi(),
			getComradeApplyStatusApi({
				state_id: currentProvinceId.value || undefined,
			}),
		])

		if (provinceRes.status === 'fulfilled') {
			const list = provinceRes.value?.data?.list
			if (Array.isArray(list)) {
				provinceList.value = [{ id: 0, name: '全部' }, ...list]
			}
		}

		if (statusRes.status === 'fulfilled' && statusRes.value?.data) {
			const data = statusRes.value.data
			Object.assign(memberInfo, {
				is_member: !!data.is_member,
				name: data.name || '',
				service_unit: data.service_unit || '',
				service_year: data.service_year || '',
				member_time: data.approve_date || data.create_date || '',
			})
			Object.assign(applicationStatus, {
				has_application: !!data.has_application,
				state: data.state || '',
				state_display: data.state_display || '',
			})
			if (data.total_count !== undefined) {
				stats.totalCount = Number(data.total_count) || 0
			}
			if (data.member_count !== undefined) {
				stats.provinceCount = Number(data.member_count) || 0
			}
		}
	} finally {
		loadingStatus.value = false
	}
}

const fetchArticles = async (isRefresh = false) => {
	if (loadingArticles.value) return
	if (!isRefresh && !hasMore.value) return
	loadingArticles.value = true
	try {
		const nextPage = isRefresh ? 1 : page.value
		const params = {
			page: nextPage,
			limit: pageSize,
		}
		if (currentProvinceId.value) {
			params.state_id = currentProvinceId.value
		}
		const res = await getComradeArticleListApi(params)
		const list = Array.isArray(res?.data?.list) ? res.data.list.map(formatArticleItem) : []
		const pagination = res?.data?.pagination || {}
		feedList.value = isRefresh ? list : [...feedList.value, ...list]

		if (pagination.comrade_users_count !== undefined) {
			stats.totalCount = Number(pagination.comrade_users_count) || 0
		}
		if (pagination.state_users_count !== undefined) {
			stats.provinceCount = Number(pagination.state_users_count) || 0
		}
		page.value = nextPage + 1
		const totalPages = Number(pagination.total_pages || 0)
		if (totalPages) {
			hasMore.value = nextPage < totalPages
		} else {
			hasMore.value = list.length >= pageSize
		}
	} catch (error) {
		if (isRefresh) {
			feedList.value = []
		}
		hasMore.value = false
	} finally {
		loadingArticles.value = false
		if (refresherTriggered.value) {
			refresherTriggered.value = false
		}
	}
}

const onChangeProvince = (item) => {
	const nextProvinceId = item.id || 0
	if (loadingArticles.value || currentProvinceId.value === nextProvinceId) return
	currentProvinceId.value = nextProvinceId
	currentProvinceName.value = item.name || '全部'
	uni.setStorageSync('warriorProvinceId', nextProvinceId)
	hasMore.value = true
	page.value = 1
	fetchArticles(true)
}

const onGoApply = () => {
	uni.navigateTo({ url: '/pages/rongjh/warrior/apply' })
}

const onFeedDetail = (item) => {
	const videosParam = item.videos?.length
		? encodeURIComponent(JSON.stringify(item.videos))
		: ''
	let url = `/pages/rongjh/warrior/detail?id=${item.id}`
	if (videosParam) {
		url += `&videos=${videosParam}`
	}
	uni.navigateTo({ url })
}

const onRefresh = () => {
	refresherTriggered.value = true
	hasMore.value = true
	page.value = 1
	fetchArticles(true)
}

onReachBottom(() => {
	fetchArticles(false)
})

onLoad(() => {
	const cachedProvinceId = Number(uni.getStorageSync('warriorProvinceId') || 0)
	currentProvinceId.value = cachedProvinceId
})

onShow(async () => {
	await loadBaseInfo()
	const province = provinceList.value.find((item) => item.id === currentProvinceId.value)
	if (province) {
		currentProvinceName.value = province.name
	}
	hasMore.value = true
	page.value = 1
	await fetchArticles(true)
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f6f7fb; }
.hero-top-bg { position: relative; overflow: hidden; }
.hero-bg-image { width: 100%; }
.hero-top-content { position: absolute; inset: 0; display: flex; flex-direction: column; }
.hero { padding: 32rpx; color: #fff; }
.hero-title { display: block; font-size: 44rpx; font-weight: 700; }
.hero-desc,.hero-stats { display: block; margin-top: 12rpx; font-size: 26rpx; }
.stats-cards { display: flex; gap: 20rpx; padding: 0 32rpx 24rpx; }
.stats-card { flex: 1; background: rgba(255,255,255,0.15); border-radius: 24rpx; padding: 24rpx; color: #fff; }
.stats-card.accent { background: rgba(255,255,255,0.22); }
.stats-card-header { display: flex; justify-content: space-between; align-items: center; }
.stats-card-icon { width: 56rpx; height: 56rpx; border-radius: 28rpx; background: rgba(255,255,255,0.2); display: flex; align-items: center; justify-content: center; }
.stats-card-icon.light,.stats-card-badge.light { background: rgba(255,255,255,0.25); }
.stats-card-badge { padding: 6rpx 16rpx; border-radius: 999rpx; background: rgba(255,255,255,0.18); font-size: 22rpx; }
.stats-card-label { display: block; margin-top: 20rpx; font-size: 24rpx; }
.stats-card-value { margin-top: 20rpx; display: flex; align-items: baseline; gap: 8rpx; }
.stats-card-number { font-size: 48rpx; font-weight: 700; }
.stats-card-unit { font-size: 24rpx; }
.section-card { margin: 24rpx; background: #fff; border-radius: 24rpx; padding: 24rpx; box-shadow: 0 12rpx 30rpx rgba(18, 38, 63, 0.06); }
.section-header { display: flex; justify-content: space-between; align-items: center; gap: 20rpx; }
.section-title { display: block; font-size: 34rpx; font-weight: 700; color: #1f2d3d; }
.section-subtitle { display: block; margin-top: 8rpx; font-size: 24rpx; color: #7a869a; }
.apply-entry { margin-top: 24rpx; display: flex; justify-content: space-between; align-items: center; gap: 20rpx; }
.status-entry { margin-top: 24rpx; padding: 24rpx; border-radius: 20rpx; background: #fff8e6; }
.status-entry-title { display: block; font-size: 28rpx; font-weight: 600; color: #b45309; }
.status-entry-desc { display: block; margin-top: 8rpx; font-size: 24rpx; color: #92400e; }
.apply-entry-title { display: block; font-size: 28rpx; font-weight: 600; color: #334e68; }
.apply-entry-desc { display: block; margin-top: 8rpx; font-size: 24rpx; color: #829ab1; }
.apply-btn { min-width: 180rpx; height: 72rpx; border-radius: 999rpx; background: linear-gradient(135deg, #ff6b6b, #ff8e53); color: #fff; font-size: 28rpx; border: none; }
.province-scroll {
	width: 100%;
	margin-top: 20rpx;
	white-space: nowrap;
}
.province-list {
	display: flex;
	flex-direction: row;
	flex-wrap: nowrap;
	gap: 12rpx;
	padding: 0 4rpx 8rpx;
}
.province-item {
	flex-shrink: 0;
	padding: 12rpx 24rpx;
	border-radius: 18rpx;
	background: #f8fafc;
	border: 1rpx solid #eef2f7;
	min-height: 70rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 4rpx 10rpx rgba(15, 23, 42, 0.03);
}
.province-item.active {
	background: linear-gradient(135deg, #d62d24, #b81d18);
	border-color: transparent;
	box-shadow: 0 10rpx 22rpx rgba(184, 29, 24, 0.18);
}
.province-item-text {
	font-size: 26rpx;
	color: #64748b;
	line-height: 1.2;
	white-space: nowrap;
	font-weight: 600;
}
.province-item.active .province-item-text {
	color: #fff;
	font-weight: 700;
}
.feed-scroll { max-height: 1200rpx; margin-top: 24rpx; }
.feed-card { padding: 24rpx 0; border-bottom: 1rpx solid #eef2f7; }
.feed-card-header { display: flex; justify-content: space-between; gap: 16rpx; }
.feed-title { flex: 1; font-size: 30rpx; font-weight: 600; color: #102a43; }
.feed-date { font-size: 22rpx; color: #829ab1; }
.feed-summary { display: block; margin-top: 16rpx; font-size: 26rpx; line-height: 1.6; color: #52606d; }
.feed-cover-wrap,.feed-video-wrap { margin-top: 20rpx; }
.feed-cover,.feed-video { width: 100%; border-radius: 20rpx; }
.empty-state,.loading-wrap { padding: 64rpx 0; text-align: center; }
.empty-title,.loading-text { font-size: 28rpx; color: #52606d; }
.empty-desc { display: block; margin-top: 12rpx; font-size: 24rpx; color: #829ab1; }
</style>
