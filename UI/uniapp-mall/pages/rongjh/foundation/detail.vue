<template>
	<view class="page">
		<CustomNav title="帮扶记录详情" background="url(/static/images/nav-bg.png) center / cover no-repeat"  color="#fff" />
		
		<!-- 加载中 -->
		<view class="loading-box" v-if="loading">
			<uni-icons type="spinner-cycle" size="32" color="#B81D18"></uni-icons>
			<text class="loading-text">加载中...</text>
		</view>
		
		<template v-else>
			<!-- 顶部信息卡片 -->
			<view class="hero-card">
				<view class="hero-tag" v-if="detail.executionStatus">{{ detail.executionStatus }}</view>
				<text class="hero-title">{{ detail.title || '帮扶记录详情' }}</text>
				<view class="hero-amount" v-if="detail.amount > 0">
					<text class="amount-label">帮扶金额</text>
					<text class="amount-value">¥{{ formatAmount(detail.amount) }}</text>
				</view>
				<view class="hero-meta">
					<text class="meta-item" v-if="detail.date">
						<uni-icons type="calendar" size="14" color="#999"></uni-icons>
						{{ detail.date }}
					</text>
					<text class="meta-item" v-if="detail.publishUser">
						<uni-icons type="person" size="14" color="#999"></uni-icons>
						{{ detail.publishUser }}
					</text>
				</view>
			</view>
			
			<!-- 申请人信息 -->
			<view class="info-section" v-if="detail.applicantName">
				<view class="section-header">
					<uni-icons type="person-filled" size="18" color="#B81D18"></uni-icons>
					<text class="section-title">申请人信息</text>
				</view>
				<view class="info-card">
					<view class="info-item">
						<text class="info-label">姓名</text>
						<text class="info-value">{{ detail.applicantName }}</text>
					</view>
				</view>
			</view>
			
			<!-- 项目摘要 -->
			<view class="info-section" v-if="detail.desc">
				<view class="section-header">
					<uni-icons type="info-filled" size="18" color="#B81D18"></uni-icons>
					<text class="section-title">项目摘要</text>
				</view>
				<view class="info-card">
					<text class="summary-text">{{ detail.desc }}</text>
				</view>
			</view>
			
			<!-- 图片资料 -->
			<view class="info-section" v-if="detail.images.length || detail.videos.length">
				<view class="section-header">
					<uni-icons type="image-filled" size="18" color="#B81D18"></uni-icons>
					<text class="section-title">相关资料</text>
					<text class="image-count" v-if="detail.images.length">{{ detail.images.length }}张</text>
				</view>
				<view class="media-section">
					<view class="media-grid">
						<view class="media-item" v-for="(img, index) in detail.images" :key="`img-${index}`" @tap="img && onPreviewImage(index)">
							<image v-if="img" class="media-image" :src="img" mode="aspectFill"></image>
							<view class="media-overlay" v-if="img">
								<uni-icons type="eye" size="20" color="#fff"></uni-icons>
							</view>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 执行情况 -->
			<view class="info-section" v-if="detail.content">
				<view class="section-header">
					<uni-icons type="compose" size="18" color="#B81D18"></uni-icons>
					<text class="section-title">执行情况</text>
				</view>
				<view class="info-card">
					<text class="content-text">{{ detail.content }}</text>
				</view>
			</view>
		</template>

		<view class="page-space"></view>
	</view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getFoundationRecordDetailApi, addArticleBrowseCountApi, formatRelativeTime, resolveRongjhMediaUrl } from '@/sheep/api/rongjh/adapter'

const detail = reactive({
	title: '帮扶记录详情',
	desc: '',
	date: '',
	amount: 0,
	images: [],
	videos: [],
	poster: '',
	content: '',
	executionStatus: '',
	publishUser: '',
	applicantName: '',
	applicantPhone: '',
	attachments: [],
})
const loading = ref(false)
const recordId = ref(0)

const formatAmount = (num) => Number(num || 0).toLocaleString('zh-CN')

const onPreviewImage = (index) => {
	const urls = detail.images?.length ? detail.images : []
	if (!urls.length) return
	uni.previewImage({
		current: urls[index] || urls[0],
		urls,
	})
}

// 加载详情数据
const loadDetail = async () => {
	if (!recordId.value) return
	loading.value = true
	try {
		const res = await getFoundationRecordDetailApi(recordId.value)
		const data = res?.data
		if (data) {
			detail.title = data.name || ''
			detail.desc = data.summary || ''
			detail.date = formatRelativeTime(data.publish_date)
			detail.amount = Number(data.amount || 0)
			detail.executionStatus = data.execution_status || ''
			detail.publishUser = data.publish_user || ''
			detail.applicantName = data.applicant_name || ''
			detail.applicantPhone = data.applicant_phone || ''
			detail.attachments = Array.isArray(data.attachments) ? data.attachments : []
			const imageUrls = detail.attachments
				.filter((att) => att.file_type === 'image' || att.file_type === 'picture')
				.map((att) => resolveRongjhMediaUrl(att.url || att.file_url || att.datas))
				.filter(Boolean)
			if (!imageUrls.length && data.cover_url) {
				imageUrls.push(resolveRongjhMediaUrl(data.cover_url))
			}
			detail.images = imageUrls
			detail.content = data.content || data.summary || ''
			// 增加浏览量（后端计数）
			addArticleBrowseCountApi(recordId.value).catch((err) => console.error('增加浏览量失败', err))
		}
	} catch (err) {
		console.error('加载详情失败', err)
		uni.showToast({ title: '加载详情失败', icon: 'none' })
	} finally {
		loading.value = false
	}
}

onLoad((query) => {
	recordId.value = Number(query?.id || 0)
	if (recordId.value) {
		loadDetail()
	}
})
</script>

<style lang="scss" scoped>
.page {
	min-height: 100vh;
	background: #F5F5F5;
}

/* 加载状态 */
.loading-box {
	padding: 200rpx 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 20rpx;
}

.loading-text {
	font-size: 28rpx;
	color: #999;
}

/* 顶部卡片 */
.hero-card {
	margin: 24rpx 24rpx 20rpx;
	background: linear-gradient(135deg, #fff 0%, #fafafa 100%);
	border-radius: 24rpx;
	padding: 36rpx 32rpx;
	box-shadow: 0 8rpx 32rpx rgba(184, 29, 24, 0.08);
	border: 1rpx solid rgba(184, 29, 24, 0.06);
	position: relative;
	overflow: hidden;
}

.hero-card::before {
	content: '';
	position: absolute;
	left: 0;
	top: 0;
	bottom: 0;
	width: 8rpx;
	background: linear-gradient(180deg, #D62D24, #B81D18);
	border-radius: 24rpx 0 0 24rpx;
}

.hero-tag {
	display: inline-block;
	padding: 8rpx 20rpx;
	background: linear-gradient(135deg, #D62D24, #B81D18);
	color: #fff;
	font-size: 24rpx;
	border-radius: 24rpx;
	margin-bottom: 20rpx;
}

.hero-title {
	font-size: 38rpx;
	font-weight: 700;
	color: #1a1a1a;
	line-height: 1.4;
	display: block;
}

.hero-amount {
	margin-top: 28rpx;
	padding-top: 28rpx;
	border-top: 1rpx dashed #e5e5e5;
	display: flex;
	align-items: baseline;
	gap: 16rpx;
}

.amount-label {
	font-size: 26rpx;
	color: #666;
}

.amount-value {
	font-size: 48rpx;
	font-weight: 700;
	color: #B81D18;
}

.hero-meta {
	margin-top: 24rpx;
	display: flex;
	flex-wrap: wrap;
	gap: 20rpx;
}

.meta-item {
	display: flex;
	align-items: center;
	gap: 8rpx;
	font-size: 24rpx;
	color: #888;
}

/* 信息区块 */
.info-section {
	margin: 20rpx 24rpx;
}

.section-header {
	display: flex;
	align-items: center;
	gap: 12rpx;
	margin-bottom: 16rpx;
}

.section-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
	flex: 1;
}

.image-count {
	font-size: 24rpx;
	color: #999;
	background: #f0f0f0;
	padding: 6rpx 16rpx;
	border-radius: 20rpx;
}

/* 信息卡片 */
.info-card {
	background: #fff;
	border-radius: 20rpx;
	padding: 28rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.info-item {
	display: flex;
	align-items: center;
	gap: 24rpx;
}

.info-label {
	font-size: 26rpx;
	color: #888;
	min-width: 100rpx;
}

.info-value {
	font-size: 30rpx;
	color: #333;
	font-weight: 500;
}

.summary-text {
	font-size: 28rpx;
	color: #555;
	line-height: 1.8;
}

.content-text {
	font-size: 28rpx;
	color: #444;
	line-height: 1.9;
}

/* 图片网格 */
.media-section {
	background: #fff;
	border-radius: 20rpx;
	padding: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.media-grid {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 16rpx;
}

.media-item {
	aspect-ratio: 1;
	border-radius: 16rpx;
	overflow: hidden;
	position: relative;
	background: #f5f5f5;
}

.media-image {
	width: 100%;
	height: 100%;
}

.media-overlay {
	position: absolute;
	inset: 0;
	background: rgba(0, 0, 0, 0.35);
	display: flex;
	align-items: center;
	justify-content: center;
	opacity: 0;
	transition: opacity 0.2s;
}

.media-item:active .media-overlay {
	opacity: 1;
}

.page-space {
	height: 40rpx;
}
</style>
