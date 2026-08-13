<template>
	<view class="page">
		<CustomNav title="公告通知" background="#fff" color="#111" />
		<scroll-view
			class="notice-scroll"
			scroll-y
			refresher-enabled
			:refresher-triggered="refresherTriggered"
			@refresherrefresh="onRefresh"
			@scrolltolower="loadNotices(false)"
		>
			<view class="notice-content">
				<view v-if="noticeList.length === 0 && !loading" class="empty-wrap">
					<text class="empty-title">暂无公告</text>
					<text class="empty-desc">后续会在这里展示最新通知</text>
				</view>
				<view v-for="item in noticeList" :key="item.id" class="notice-card" @tap="goDetail(item)">
					<image
						v-if="item.cover_image"
						class="notice-cover"
						:src="item.cover_image"
						mode="aspectFill"
					/>
					<view class="notice-head">
						<text class="notice-title">{{ item.title }}</text>
						<text class="notice-time">{{ item.time }}</text>
					</view>
					<text class="notice-summary">{{ item.summary }}</text>
					<view class="notice-foot">
						<text class="notice-tag">{{ item.tag || '公告' }}</text>
						<text class="notice-count">浏览 {{ item.view_count }}</text>
					</view>
				</view>
				<view v-if="loading || loadingMore" class="loading-wrap"><text class="loading-text">加载中...</text></view>
				<view v-else-if="!hasMore && noticeList.length > 0" class="loading-wrap"><text class="loading-text">已显示全部内容</text></view>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getNoticeListApi, resolveRongjhMediaUrl, formatRelativeTime } from '@/sheep/api/rongjh/adapter'

const noticeList = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const refresherTriggered = ref(false)
const page = ref(1)
const pageSize = 10

const formatNoticeItem = (item) => ({
	id: item.id,
	title: item.name || '',
	summary: item.summary || '',
	time: formatRelativeTime(item.publish_date),
	tag: item.announcement_type_name || '',
	is_top: !!item.is_top,
	view_count: item.view_count || 0,
	cover_image: resolveRongjhMediaUrl(item.cover_image || item.cover_url || '')
})

const loadNotices = async (isRefresh = false) => {
	if (isRefresh) {
		if (loading.value) return
		loading.value = true
	} else {
		if (loadingMore.value || !hasMore.value) return
		loadingMore.value = true
	}

	try {
		const currentPage = isRefresh ? 1 : page.value
		const res = await getNoticeListApi({
			page: currentPage,
			limit: pageSize,
		})

		const list = Array.isArray(res?.data?.list) ? res.data.list : []
		const formattedList = list.map(formatNoticeItem)

		if (isRefresh) {
			noticeList.value = formattedList
			page.value = 1
		} else {
			noticeList.value = [...noticeList.value, ...formattedList]
		}

		page.value = currentPage + 1
		const total = res?.total || res?.data?.total || 0
		hasMore.value = noticeList.value.length < total
	} catch (error) {
		console.error('加载公告失败', error)
		uni.showToast({ title: '加载失败', icon: 'none' })
	} finally {
		if (isRefresh) {
			loading.value = false
			if (refresherTriggered.value) {
				refresherTriggered.value = false
			}
		} else {
			loadingMore.value = false
		}
	}
}

const onRefresh = () => {
	refresherTriggered.value = true
	hasMore.value = true
	loadNotices(true)
}

const goDetail = (item) => {
	uni.navigateTo({ url: `/pages/rongjh/notice/detail?id=${item.id}` })
}

onLoad(() => {
	loadNotices(true)
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f7f7fb; }
.notice-scroll { height: 100vh; }
.notice-content { padding: 24rpx; }
.notice-card { background: #fff; border-radius: 24rpx; padding: 24rpx; margin-bottom: 20rpx; box-shadow: 0 12rpx 30rpx rgba(18, 38, 63, 0.06); }
.notice-cover { width: 100%; height: 280rpx; border-radius: 16rpx; margin-bottom: 16rpx; background: #f5f5f5; }
.notice-head { display: flex; justify-content: space-between; gap: 16rpx; }
.notice-title { flex: 1; font-size: 30rpx; font-weight: 600; color: #1f2d3d; }
.notice-time { font-size: 22rpx; color: #829ab1; }
.notice-summary { display: block; margin-top: 14rpx; font-size: 26rpx; line-height: 1.6; color: #52606d; }
.notice-foot { display: flex; justify-content: space-between; margin-top: 18rpx; }
.notice-tag { font-size: 22rpx; color: #e33b3b; }
.notice-count { font-size: 22rpx; color: #829ab1; }
.empty-wrap,.loading-wrap { padding: 80rpx 0; text-align: center; }
.empty-title,.loading-text { font-size: 28rpx; color: #52606d; }
.empty-desc { display: block; margin-top: 12rpx; font-size: 24rpx; color: #829ab1; }
</style>
