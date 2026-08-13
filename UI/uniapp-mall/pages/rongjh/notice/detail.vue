<template>
	<view class="page">
		<CustomNav :title="notice.title || '公告详情'" background="url(/static/images/nav-bg.png) center / cover no-repeat" 
			color="#fff" />

		<scroll-view class="detail-scroll" scroll-y>
			<view class="notice-detail" v-if="notice.id">
				<!-- 标题区域 -->
				<view class="detail-header">
					<text class="detail-title">{{ notice.title }}</text>
					<view class="detail-meta">
						<view class="detail-tag" v-if="notice.tag">
							<text class="detail-tag-text">{{ notice.tag }}</text>
						</view>
						<text class="detail-time">{{ notice.time }}</text>
						<text class="detail-views">浏览 {{ notice.view_count }}</text>
					</view>
				</view>

				<!-- 封面图 -->
				<view class="detail-images" v-if="notice.images && notice.images.length > 0">
					<image
						v-for="(img, index) in notice.images"
						:key="index"
						class="detail-image"
						:src="img"
						mode="widthFix"
						@tap="previewImage(index)"
					/>
				</view>

				<!-- 内容区域 -->
				<view class="detail-content">
					<mp-html v-if="notice.content" class="detail-rich-text" :content="notice.content"></mp-html>
					<text v-else class="detail-empty">暂无内容</text>
				</view>

				<!-- 底部信息 -->
				<view class="detail-footer">
					<text class="footer-text">戎集汇平台</text>
					<text class="footer-text">{{ notice.time }}</text>
				</view>
			</view>

			<!-- 加载状态 -->
			<view class="loading-container" v-else>
				<uni-icons type="spinner-cycle" size="40" color="#ddd"></uni-icons>
				<text class="loading-text">加载中...</text>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
import { reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
	getNoticeDetailApi,
	addNoticeBrowseCountApi,
	resolveRongjhMediaUrl,
	fixRongjhContentHtml,
	formatRelativeTime,
} from '@/sheep/api/rongjh/adapter'

const notice = reactive({
	id: 0,
	title: '',
	summary: '',
	content: '',
	time: '',
	tag: '',
	view_count: 0,
	images: [],
})

const collectNoticeImages = (data = {}) => {
	const fromList = (data.images || [])
		.map((img) => resolveRongjhMediaUrl(img))
		.filter(Boolean)
	if (fromList.length) return fromList
	const cover = data.cover_image || data.cover_url || data.picUrl || ''
	const resolved = resolveRongjhMediaUrl(cover)
	return resolved ? [resolved] : []
}

// 格式化公告详情数据
const formatNoticeDetail = (data) => ({
	id: data.id,
	title: data.name || '',
	summary: data.summary || '',
	content: fixRongjhContentHtml(data.content || ''),
	time: formatRelativeTime(data.publish_date),
	tag: data.announcement_type_name || '',
	is_top: !!data.is_top,
	view_count: data.view_count || 0,
	publish_user: data.publish_user || '',
	images: collectNoticeImages(data),
})

// 加载公告详情
const loadNoticeDetail = async (id) => {
	try {
		uni.showLoading({ title: '加载中...' })
		
		const res = await getNoticeDetailApi(id)
		const data = res?.data
		
		if (data) {
			const formatted = formatNoticeDetail(data)
			Object.assign(notice, formatted)
			// 增加浏览量：后端计数 + 本地 +1 即时反馈
			addNoticeBrowseCountApi(id)
				.then(() => {
					notice.view_count = (notice.view_count || 0) + 1
				})
				.catch((err) => console.error('增加浏览量失败', err))
		} else {
			uni.showToast({ title: '公告不存在', icon: 'none' })
			setTimeout(() => uni.navigateBack(), 1500)
		}
	} catch (error) {
		console.error('加载公告详情失败', error)
		uni.showToast({ title: error?.message || '加载失败', icon: 'none' })
	} finally {
		uni.hideLoading()
	}
}

// 预览图片
const previewImage = (index) => {
	if (!notice.images || notice.images.length === 0) return
	uni.previewImage({
		current: notice.images[index],
		urls: notice.images
	})
}

onLoad((options) => {
	const id = options?.id
	if (id) {
		loadNoticeDetail(id)
	} else {
		uni.showToast({ title: '参数错误', icon: 'none' })
		setTimeout(() => uni.navigateBack(), 1500)
	}
})
</script>

<style lang="scss" scoped>
.page {
	background: #F5F5F5;
	min-height: 100vh;
	display: flex;
	flex-direction: column;
}

.detail-scroll {
	flex: 1;
	height: calc(100vh - 44px - env(safe-area-inset-top));
}

.notice-detail {
	background: #fff;
	min-height: 100%;
}

.detail-header {
	padding: 40rpx 30rpx;
	border-bottom: 1rpx solid #f5f5f5;
}

.detail-title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
	line-height: 1.5;
	display: block;
	margin-bottom: 20rpx;
}

.detail-meta {
	display: flex;
	align-items: center;
	gap: 20rpx;
}

.detail-tag {
	background: linear-gradient(135deg, #FDF2E9, #FDEBD0);
	padding: 6rpx 16rpx;
	border-radius: 8rpx;
}

.detail-tag-text {
	font-size: 24rpx;
	color: #E65C1C;
}

.detail-time {
	font-size: 26rpx;
	color: #999;
}

.detail-views {
	font-size: 26rpx;
	color: #999;
}

/* 图片区域样式 */
.detail-images {
	padding: 20rpx 30rpx;
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.detail-image {
	width: 100%;
	border-radius: 12rpx;
	background: #f5f5f5;
}

.detail-content {
	padding: 40rpx 30rpx;
	min-height: 400rpx;
}

.detail-rich-text {
	font-size: 30rpx;
	color: #333;
	line-height: 1.8;
}

.detail-empty {
	font-size: 28rpx;
	color: #999;
	text-align: center;
	padding: 60rpx 0;
}

.detail-footer {
	padding: 40rpx 30rpx 60rpx;
	border-top: 1rpx solid #f5f5f5;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 12rpx;
}

.footer-text {
	font-size: 26rpx;
	color: #999;
}

.loading-container {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 200rpx 0;
	gap: 20rpx;
}

.loading-text {
	font-size: 28rpx;
	color: #999;
}
</style>
