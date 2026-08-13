<template>
	<view class="page">
		<CustomNav title="战友会动态" background="url(/static/images/nav-bg.png) center / cover no-repeat" color="#fff" />

		<view v-if="loading" class="loading-wrap">
			<uni-icons type="spinner-cycle" size="28" color="#B81D18"></uni-icons>
			<text class="loading-text">正在加载文章详情...</text>
		</view>

		<view v-else class="content-card">
			<view class="detail-hero">
				<view class="detail-hero-top">
					<view class="detail-hero-copy">
						<text class="detail-kicker">战友会文章</text>
						<text class="detail-title">{{ detail.title }}</text>
					</view>
					<view v-if="detail.is_top" class="top-badge">置顶</view>
				</view>
				<text class="detail-meta">{{ detail.meta }}</text>
				<view class="stats-row">
					<text class="stats-text">浏览 {{ detail.view_count }}</text>
					<view class="like-btn" :class="{ liked: hasLiked }" @tap="onLikeArticle">
						<uni-icons type="heart-filled" size="14" color="#fff"></uni-icons>
						<text class="like-btn-text">{{ hasLiked ? '已点赞' : '点赞' }} {{ detail.like_count }}</text>
					</view>
				</view>
			</view>

			<!-- 媒体区域 -->
			<view v-if="detail.images.length || detail.videos.length" class="media-section">
				<view class="media-card" v-for="(img, index) in detail.images" :key="`img-${index}`" @tap="onPreviewImage(index)">
					<image class="media-image" :src="img" mode="aspectFill"></image>
					<view class="media-tip">点击预览</view>
				</view>
				<view class="media-card" v-for="(vid, index) in detail.videos" :key="`vid-${index}`">
					<video class="media-video" :src="vid" controls></video>
				</view>
			</view>

			<!-- 活动详情 -->
			<view class="detail-section">
				<text class="detail-section-title">活动详情</text>
				<rich-text v-if="detail.content" class="detail-rich" :nodes="detail.content"></rich-text>
				<text v-else class="detail-empty">暂无详情内容</text>
			</view>
		</view>

		<view class="page-space"></view>
	</view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getComradeArticleDetailApi, likeComradeArticleApi, addArticleBrowseCountApi, formatRelativeTime } from '@/sheep/api/rongjh/adapter'
import BASE_URL from '@/utils/env'

const loading = ref(true)
const likeSubmitting = ref(false)
const hasLiked = ref(false)
const articleId = ref(0)
const passedVideos = ref([]) // 从列表页传入的视频数据

const detail = reactive({
	title: '战友会动态',
	summary: '',
	content: '',
	meta: '',
	images: [],
	videos: [],
	poster: '',
	view_count: 0,
	like_count: 0,
	is_top: false,
})

// 检查是否已点赞
const checkHasLiked = () => {
	if (!articleId.value) return
	const likedArticles = uni.getStorageSync('likedArticles') || []
	hasLiked.value = likedArticles.includes(articleId.value)
}

// 保存点赞记录
const saveLiked = () => {
	if (!articleId.value) return
	const likedArticles = uni.getStorageSync('likedArticles') || []
	if (!likedArticles.includes(articleId.value)) {
		likedArticles.push(articleId.value)
		uni.setStorageSync('likedArticles', likedArticles)
	}
	hasLiked.value = true
}

const normalizeUrl = (url = '') => {
	const value = String(url || '').trim()
	if (!value) return ''
	if (/^https?:\/\//i.test(value)) return value
	if (value.startsWith('/')) return `${BASE_URL}${value}`
	return `${BASE_URL}/${value}`
}

const onPreviewImage = (index) => {
	if (!detail.images.length) return
	uni.previewImage({
		current: detail.images[index] || detail.images[0],
		urls: detail.images,
	})
}

const mapDetailData = (data = {}) => {
	// 调试输出原始数据
	console.log('详情页原始数据:', {
		image_ids: data.image_ids,
		video_ids: data.video_ids
	})
	
	// 处理图片列表：从 image_ids 中提取 datas 字段
	const imageIds = Array.isArray(data.image_ids) ? data.image_ids : []
	const images = imageIds
		.map((item) => {
			if (typeof item === 'string') return normalizeUrl(item)
			if (item?.datas) return normalizeUrl(item.datas)
			return ''
		})
		.filter(Boolean)
	
	// 处理视频列表：优先使用接口返回的 video_ids，如果没有则使用传入的
	let videoIds = Array.isArray(data.video_ids) ? data.video_ids : []
	// 如果接口没有返回视频，使用从列表页传入的视频数据
	if (videoIds.length === 0 && passedVideos.value.length > 0) {
		videoIds = passedVideos.value
		console.log('使用列表传入的视频:', videoIds)
	}
	const videos = videoIds
		.map((item) => {
			if (typeof item === 'string') return normalizeUrl(item)
			if (item?.datas) return normalizeUrl(item.datas)
			return ''
		})
		.filter(Boolean)
	
	// 调试输出处理后的数据
	console.log('详情页处理后:', { images, videos })

	detail.title = data.name || '战友会动态'
	detail.summary = data.summary || ''
	detail.content = data.content || ''
	detail.meta = `${data.state_name || '全国'} · ${formatRelativeTime(data.publish_date)} · 发布人 ${data.publish_user_name || '--'}`
	detail.images = images
	detail.videos = videos
	detail.poster = images[0] || ''
	detail.view_count = Number(data.view_count || 0)
	detail.like_count = Number(data.like_count || 0)
	detail.is_top = !!data.is_top
}

const fetchArticleDetail = async () => {
	if (!articleId.value) {
		loading.value = false
		uni.showToast({ title: '缺少文章ID', icon: 'none' })
		return
	}
	loading.value = true
	try {
		const res = await getComradeArticleDetailApi(articleId.value)
		mapDetailData(res?.data || {})
		checkHasLiked()
		// 增加浏览量：后端计数 + 本地 +1 即时反馈
		addArticleBrowseCountApi(articleId.value)
			.then(() => {
				detail.view_count = (detail.view_count || 0) + 1
			})
			.catch((err) => console.error('增加浏览量失败', err))
	} finally {
		loading.value = false
	}
}

const onLikeArticle = async () => {
	if (!articleId.value || likeSubmitting.value || hasLiked.value) {
		if (hasLiked.value) {
			uni.showToast({ title: '您已点赞过', icon: 'none' })
		}
		return
	}
	likeSubmitting.value = true
	try {
		const res = await likeComradeArticleApi(articleId.value)
		detail.like_count = Number(res?.data?.like_count || detail.like_count + 1)
		saveLiked()
		uni.showToast({ title: '点赞成功', icon: 'success' })
	} finally {
		likeSubmitting.value = false
	}
}

onLoad((query) => {
	articleId.value = Number(query?.id || 0)
	// 解析从列表页传入的视频数据
	if (query?.videos) {
		try {
			passedVideos.value = JSON.parse(decodeURIComponent(query.videos))
			console.log('从列表传入的视频:', passedVideos.value)
		} catch (e) {
			console.error('解析传入视频失败:', e)
			passedVideos.value = []
		}
	}
	fetchArticleDetail()
})
</script>

<style lang="scss" scoped>
.page {
	min-height: 100vh;
	background:
		linear-gradient(180deg, rgba(184, 29, 24, 0.18) 0%, rgba(184, 29, 24, 0.04) 20%, #f7f7f7 20%, #f7f7f7 100%);
}

.loading-wrap {
	padding: 132rpx 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 20rpx;
}

.loading-text {
	font-size: 28rpx;
	color: #94a3b8;
}

.content-card {
	margin: 18rpx 24rpx;
	background: rgba(255, 255, 255, 0.98);
	border-radius: 28rpx;
	padding: 28rpx;
	box-shadow: 0 12rpx 34rpx rgba(15, 23, 42, 0.08);
	border: 1rpx solid rgba(255, 255, 255, 0.92);
}

.detail-hero {
	padding: 6rpx 2rpx 20rpx;
	background: linear-gradient(180deg, rgba(255, 243, 243, 0.9) 0%, rgba(255, 255, 255, 0) 100%);
	border-bottom: 1rpx solid #fee2e2;
	margin-bottom: 22rpx;
}

.detail-hero-top {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	gap: 16rpx;
	margin-bottom: 12rpx;
}

.detail-hero-copy {
	flex: 1;
	min-width: 0;
}

.detail-kicker {
	display: block;
	font-size: 22rpx;
	color: #b91c1c;
	font-weight: 800;
	letter-spacing: 2rpx;
	margin-bottom: 10rpx;
}

.detail-title {
	font-size: 38rpx;
	font-weight: 900;
	color: #111827;
	line-height: 1.38;
	display: block;
}

.top-badge {
	padding: 8rpx 18rpx;
	font-size: 22rpx;
	color: #fff;
	background: linear-gradient(135deg, #d62d24, #a81612);
	border-radius: 999rpx;
	flex-shrink: 0;
	box-shadow: 0 8rpx 16rpx rgba(184, 29, 24, 0.16);
}

.detail-meta {
	font-size: 24rpx;
	color: #94a3b8;
	line-height: 1.6;
	display: block;
	margin-bottom: 18rpx;
}

.stats-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	gap: 16rpx;
}

.stats-text {
	font-size: 24rpx;
	color: #94a3b8;
}

.like-btn {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding: 14rpx 24rpx;
	border-radius: 999rpx;
	background: linear-gradient(135deg, #d62d24, #a81612);
	box-shadow: 0 10rpx 20rpx rgba(184, 29, 24, 0.18);
}

.like-btn.liked {
	background: linear-gradient(135deg, #c81e1e, #b81d18);
}

.like-btn-text {
	font-size: 24rpx;
	color: #fff;
	font-weight: 700;
}

.media-section {
	margin-bottom: 24rpx;
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.media-card {
	width: 100%;
	height: 420rpx;
	border-radius: 20rpx;
	overflow: hidden;
	background: #f5f5f5;
	position: relative;
	border: 1rpx solid #f1f5f9;
}

.media-image,
.media-video {
	width: 100%;
	height: 100%;
}

.media-tip {
	position: absolute;
	right: 16rpx;
	bottom: 16rpx;
	background: rgba(17, 24, 39, 0.56);
	color: #fff;
	font-size: 22rpx;
	padding: 6rpx 14rpx;
	border-radius: 20rpx;
}

.detail-section {
	padding-top: 24rpx;
	border-top: 1rpx solid #f1f5f9;
}

.detail-section-title {
	font-size: 30rpx;
	font-weight: 900;
	color: #111827;
	margin-bottom: 16rpx;
	display: block;
}

.detail-rich {
	font-size: 28rpx;
	color: #4b5563;
	line-height: 1.9;
}

.detail-empty {
	font-size: 28rpx;
	color: #9ca3af;
	padding: 40rpx 0;
	text-align: center;
}

.page-space {
	height: 40rpx;
}
</style>
