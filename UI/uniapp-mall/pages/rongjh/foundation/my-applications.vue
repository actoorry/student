<template>
	<view class="page">
		<CustomNav title="我的申请" background="url(/static/images/nav-bg.png) center / cover no-repeat" color="#fff" />

		<view v-if="loading" class="loading-box">
			<uni-icons type="spinner-cycle" size="32" color="#B81D18"></uni-icons>
			<text class="loading-text">加载中...</text>
		</view>

		<view v-else-if="!applications.length" class="empty-box">
			<uni-icons type="info" size="48" color="#ccc"></uni-icons>
			<text class="empty-text">暂无帮扶申请</text>
			<view class="empty-btn" @tap="onGoApply">去申请</view>
		</view>

		<view v-else class="list">
			<view
				v-for="item in applications"
				:key="item.id"
				class="card"
				@tap="openDetail(item)"
			>
				<view class="card-header">
					<text class="card-title">{{ item.name }}</text>
					<text class="status-tag" :class="`status-${item.status}`">{{ item.status_text }}</text>
				</view>
				<text class="card-reason">{{ item.reason }}</text>
				<view class="card-meta">
					<text class="meta-text">{{ item.create_time }}</text>
					<text v-if="item.apply_amount > 0" class="meta-amount">申请 ¥{{ formatAmount(item.apply_amount) }}</text>
				</view>
				<view v-if="item.status === 0" class="card-actions" @tap.stop>
					<view class="cancel-btn" @tap="onCancel(item)">撤销申请</view>
				</view>
			</view>
		</view>

		<view v-if="showDetail" class="detail-mask" @tap="closeDetail">
			<view class="detail-panel" @tap.stop>
				<view class="detail-header">
					<text class="detail-title">申请详情</text>
					<uni-icons type="closeempty" size="22" color="#999" @tap="closeDetail"></uni-icons>
				</view>
				<scroll-view scroll-y class="detail-body">
					<view class="detail-row">
						<text class="detail-label">状态</text>
						<text class="status-tag" :class="`status-${detail.status}`">{{ detail.status_text }}</text>
					</view>
					<view class="detail-row">
						<text class="detail-label">姓名</text>
						<text class="detail-value">{{ detail.name }}</text>
					</view>
					<view class="detail-row">
						<text class="detail-label">电话</text>
						<text class="detail-value">{{ detail.phone }}</text>
					</view>
					<view class="detail-row">
						<text class="detail-label">申请时间</text>
						<text class="detail-value">{{ detail.create_time }}</text>
					</view>
					<view v-if="detail.apply_amount > 0" class="detail-row">
						<text class="detail-label">申请金额</text>
						<text class="detail-value">¥{{ formatAmount(detail.apply_amount) }}</text>
					</view>
					<view v-if="detail.actual_amount > 0" class="detail-row">
						<text class="detail-label">批准金额</text>
						<text class="detail-value accent">¥{{ formatAmount(detail.actual_amount) }}</text>
					</view>
					<view class="detail-block">
						<text class="detail-label">求助说明</text>
						<text class="detail-desc">{{ detail.reason }}</text>
					</view>
					<view v-if="detail.remark" class="detail-block">
						<text class="detail-label">审核备注</text>
						<text class="detail-desc">{{ detail.remark }}</text>
					</view>
					<view v-if="detail.materials.length" class="detail-block">
						<text class="detail-label">证明材料</text>
						<view class="media-grid">
							<image
								v-for="(file, idx) in detail.materials"
								:key="idx"
								class="media-image"
								:src="file.url"
								mode="aspectFill"
								@tap="onPreviewImage(idx)"
							></image>
						</view>
					</view>
				</scroll-view>
				<view v-if="detail.status === 0" class="detail-footer">
					<view class="cancel-btn full" @tap="onCancel(detail)">撤销申请</view>
				</view>
			</view>
		</view>

		<view class="page-space"></view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import sheep from '@/sheep'
import { showAuthModal } from '@/sheep/hooks/useModal'
import { getMyHelpApplicationsApi, cancelHelpApplicationApi } from '@/sheep/api/rongjh/adapter'
import RONGJH_ROUTES from '@/sheep/helper/rongjh-routes'

const loading = ref(false)
const applications = ref([])
const showDetail = ref(false)
const detail = ref({})

const formatAmount = (num) => Number(num || 0).toLocaleString('zh-CN')

const loadApplications = async () => {
	if (!sheep.$store('user').isLogin) {
		showAuthModal('accountLogin')
		return
	}
	loading.value = true
	try {
		const res = await getMyHelpApplicationsApi()
		applications.value = Array.isArray(res?.data) ? res.data : []
	} catch (err) {
		console.error('加载申请列表失败', err)
		uni.showToast({ title: '加载失败', icon: 'none' })
	} finally {
		loading.value = false
	}
}

const openDetail = (item) => {
	detail.value = { ...item }
	showDetail.value = true
}

const closeDetail = () => {
	showDetail.value = false
}

const onPreviewImage = (index) => {
	const urls = (detail.value.materials || []).map((item) => item.url).filter(Boolean)
	if (!urls.length) return
	uni.previewImage({ current: urls[index] || urls[0], urls })
}

const onCancel = (item) => {
	uni.showModal({
		title: '撤销申请',
		content: '确定要撤销该帮扶申请吗？',
		success: async (res) => {
			if (!res.confirm) return
			try {
				const cancelRes = await cancelHelpApplicationApi(item.id)
				if (cancelRes?.code === 200) {
					uni.showToast({ title: '已撤销', icon: 'success' })
					closeDetail()
					loadApplications()
				} else {
					uni.showToast({ title: cancelRes?.msg || '撤销失败', icon: 'none' })
				}
			} catch (err) {
				uni.showToast({ title: '撤销失败', icon: 'none' })
			}
		},
	})
}

const onGoApply = () => {
	uni.navigateTo({ url: RONGJH_ROUTES.foundationApply })
}

onShow(() => {
	loadApplications()
})
</script>

<style lang="scss" scoped>
.page {
	min-height: 100vh;
	background: #f5f5f5;
}

.loading-box,
.empty-box {
	padding: 180rpx 40rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 20rpx;
}

.loading-text,
.empty-text {
	font-size: 28rpx;
	color: #999;
}

.empty-btn {
	margin-top: 20rpx;
	padding: 16rpx 48rpx;
	background: linear-gradient(135deg, #d62d24, #b81d18);
	color: #fff;
	font-size: 28rpx;
	border-radius: 40rpx;
}

.list {
	padding: 24rpx;
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.card {
	background: #fff;
	border-radius: 20rpx;
	padding: 28rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.card-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16rpx;
	margin-bottom: 12rpx;
}

.card-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
}

.status-tag {
	font-size: 22rpx;
	padding: 6rpx 16rpx;
	border-radius: 20rpx;
	flex-shrink: 0;
}

.status-0 {
	background: #fff7e6;
	color: #d48806;
}

.status-1 {
	background: #f6ffed;
	color: #389e0d;
}

.status-2 {
	background: #fff1f0;
	color: #cf1322;
}

.status-3 {
	background: #f5f5f5;
	color: #8c8c8c;
}

.card-reason {
	font-size: 26rpx;
	color: #666;
	line-height: 1.6;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
}

.card-meta {
	margin-top: 16rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.meta-text {
	font-size: 24rpx;
	color: #999;
}

.meta-amount {
	font-size: 24rpx;
	color: #b81d18;
}

.card-actions {
	margin-top: 20rpx;
	display: flex;
	justify-content: flex-end;
}

.cancel-btn {
	padding: 12rpx 28rpx;
	border: 1rpx solid #d9d9d9;
	border-radius: 32rpx;
	font-size: 24rpx;
	color: #666;
}

.cancel-btn.full {
	width: 100%;
	text-align: center;
}

.detail-mask {
	position: fixed;
	inset: 0;
	background: rgba(0, 0, 0, 0.45);
	display: flex;
	align-items: flex-end;
	z-index: 100;
}

.detail-panel {
	width: 100%;
	max-height: 80vh;
	background: #fff;
	border-radius: 24rpx 24rpx 0 0;
	display: flex;
	flex-direction: column;
}

.detail-header {
	padding: 28rpx 32rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	border-bottom: 1rpx solid #f0f0f0;
}

.detail-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
}

.detail-body {
	max-height: 60vh;
	padding: 24rpx 32rpx;
}

.detail-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 16rpx 0;
	border-bottom: 1rpx solid #f5f5f5;
}

.detail-block {
	padding: 20rpx 0;
}

.detail-label {
	font-size: 26rpx;
	color: #888;
}

.detail-value {
	font-size: 28rpx;
	color: #333;
}

.detail-value.accent {
	color: #b81d18;
	font-weight: 600;
}

.detail-desc {
	display: block;
	margin-top: 12rpx;
	font-size: 28rpx;
	color: #555;
	line-height: 1.7;
}

.media-grid {
	margin-top: 16rpx;
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 16rpx;
}

.media-image {
	width: 100%;
	aspect-ratio: 1;
	border-radius: 12rpx;
	background: #f5f5f5;
}

.detail-footer {
	padding: 20rpx 32rpx 40rpx;
	border-top: 1rpx solid #f0f0f0;
}

.page-space {
	height: 40rpx;
}
</style>
