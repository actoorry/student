<template>
	<view class="page">
		<view class="hero-top-bg">
			<image class="hero-bg-image" src="@/static/images/index-bg.jpg" mode="aspectFill"></image>
			<view class="hero-top-content">
				<CustomNav title="戎爱心" background="transparent" color="#fff" />
				<view class="hero">
					<view class="hero-content">
						<view class="hero-top">
							<view class="hero-title">
								<text class="hero-name">戎爱心</text>
							</view>
							<text class="hero-desc">用于展示平台帮扶爱心与爱心贡献情况</text>
						</view>
						<view class="hero-cards">
							<view class="hero-card summary-card">
								<view class="card-topline">
									<text class="card-badge">总爱心</text>
									<text class="card-subtle">截至{{ foundationData.updatedAt }}</text>
								</view>
								<text class="card-label">平台的贡献值</text>
								<view class="card-value">
									<text class="card-number">{{ formatPlatformContribution(foundationData.totalFund) }}</text>
								</view>
							</view>
							<view class="hero-card accent" @tap="onMyContributionTap">
								<view class="card-topline">
									<text class="card-badge light">我的爱心</text>
									<text class="card-subtle light">{{ isLogin ? '持续累计中' : '登录后查看' }}</text>
								</view>
								<text class="card-label">我的贡献值</text>
								<view class="card-value">
									<text class="card-number">{{ formatAmount(foundationData.myContribution) }}</text>
									<text class="card-unit">爱心</text>
								</view>
							</view>
						</view>
						<view class="hero-links">
							<text class="hero-link" @tap="onMyApplications">我的申请</text>
						</view>
					</view>
				</view>

				<view class="section tip-card">
					<view class="tip-left">
						<uni-icons type="info" size="16" color="#D62D24"></uni-icons>
						<text class="tip-title">爱心规则</text>
					</view>
					<text class="tip-text">平台消费自动累计爱心贡献值，可用于帮扶公示。</text>
				</view>
			</view>
		</view>

		<view class="section">
			<view class="section-header">
				<view>
					<text class="section-title">帮扶记录</text>
					<text class="section-subtitle small">爱心公示与帮扶动态</text>
				</view>
				<view class="section-more" @tap="onMoreRecords">
					<text class="section-more-text">更多</text>
					<uni-icons type="right" size="12" color="#999"></uni-icons>
				</view>
			</view>
			<view class="record-list">
				<view
					v-for="item in foundationData.records"
					:key="item.id"
					class="record-card"
					@tap="onRecordDetail(item)"
				>
					<image
						v-if="item.image"
						class="record-image"
						:src="item.image"
						mode="aspectFill"
					></image>
					<view v-else class="record-image-empty">
						<text class="record-image-empty-text">暂无图片</text>
					</view>
					<view class="record-content">
						<view class="record-title">{{ item.title }}</view>
						<view class="record-desc">{{ item.desc }}</view>
						<view class="record-meta">
							<text class="record-date">{{ item.date }}</text>
							<text v-if="item.amount > 0" class="record-amount">¥{{ formatAmount(item.amount) }}</text>
						</view>
					</view>
				</view>

				<view v-if="foundationData.loading && foundationData.records.length > 0" class="loading-more">
					<uni-icons type="spinner-cycle" size="20" color="#999"></uni-icons>
					<text class="loading-more-text">加载中...</text>
				</view>
				<view v-else-if="!hasMore && foundationData.records.length > 0" class="load-finish">
					<text class="loading-more-text">— 已加载全部 —</text>
				</view>
				<view v-if="!foundationData.loading && foundationData.records.length === 0" class="record-empty">
					<uni-icons type="info" size="48" color="#ccc"></uni-icons>
					<text class="record-empty-text">暂无帮扶记录</text>
				</view>
			</view>
		</view>

		<view class="section">
			<view class="section-header">
				<view>
					<text class="section-title">帮扶求助资料申请</text>
					<text class="section-subtitle small">完善资料后可提交帮扶申请</text>
				</view>
			</view>
			<view class="apply-entry">
				<view class="apply-entry-text">
					<text class="apply-entry-title">提交帮扶求助资料</text>
					<text class="apply-entry-desc">完善资料后提交，工作人员将在 3 个工作日内与您联系</text>
				</view>
				<view class="apply-entry-btn" @tap="onGoApply">去填写</view>
			</view>
		</view>

		<view class="page-space"></view>
	</view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import sheep from '@/sheep'
import { showAuthModal } from '@/sheep/hooks/useModal'
import {
	getFoundationRecordListApi,
	getFoundationRecordTotalAmountApi,
	getPartnerLoveValueApi,
	formatRelativeTime,
} from '@/sheep/api/rongjh/adapter'
import { RONGJH_ROUTES } from '@/sheep/helper/rongjh-routes'

const isLogin = computed(() => sheep.$store('user').isLogin)

const foundationData = reactive({
	totalFund: 0,
	myContribution: 0,
	updatedAt: '--',
	records: [],
	loading: false,
})
const page = ref(1)
const pageSize = 10
const hasMore = ref(true)
const totalRecords = ref(0)

const onGoApply = () => {
	if (!isLogin.value) {
		showAuthModal('accountLogin')
		return
	}
	uni.navigateTo({ url: RONGJH_ROUTES.foundationApply })
}

const onMyContributionTap = () => {
	if (!isLogin.value) {
		showAuthModal('accountLogin')
	}
}

const onMyApplications = () => {
	if (!isLogin.value) {
		showAuthModal('accountLogin')
		return
	}
	uni.navigateTo({ url: RONGJH_ROUTES.foundationMyApplications })
}

const loadFoundationSummary = async () => {
	try {
		const loveRes = await getPartnerLoveValueApi()
		if (loveRes?.code === 200 && loveRes?.data) {
			foundationData.totalFund = Number(loveRes.data.total_love_values || 0)
		}
	} catch (err) {
		console.error('加载平台贡献值失败', err)
	}

	if (!isLogin.value) {
		foundationData.myContribution = 0
		foundationData.updatedAt = '--'
		return
	}

	try {
		const res = await getFoundationRecordTotalAmountApi()
		const data = res?.data || {}
		foundationData.myContribution = Number(data.contribution_amount || 0)
		foundationData.updatedAt = data.deadline || '--'
	} catch (err) {
		console.error('加载我的爱心贡献失败', err)
		foundationData.myContribution = 0
	}
}

const formatPlatformContribution = (num) => Number(num || 0).toFixed(2)
const formatAmount = (num) => Number(num || 0).toLocaleString('zh-CN')

const loadFoundationRecords = async (isRefresh = false) => {
	if (foundationData.loading) return
	if (isRefresh) {
		page.value = 1
		hasMore.value = true
	}
	if (!isRefresh && !hasMore.value) return

	foundationData.loading = true
	try {
		const res = await getFoundationRecordListApi({
			page: page.value,
			limit: pageSize,
		})
		const list = Array.isArray(res?.data?.list)
			? res.data.list
			: Array.isArray(res?.data)
				? res.data
				: []
		totalRecords.value = res?.total || res?.data?.total || list.length
		const formattedList = list.map((item) => ({
			id: item.id,
			title: item.name || item.title,
			desc: item.summary,
			date: formatRelativeTime(item.publish_date),
			amount: Number(item.amount || 0),
			image: item.cover_url || '',
			applicantName: item.applicant_name,
			content: item.content || item.summary,
		}))

		if (isRefresh) {
			foundationData.records = formattedList
		} else {
			foundationData.records.push(...formattedList)
		}

		const loadedCount = foundationData.records.length
		hasMore.value = loadedCount < totalRecords.value
		if (list.length > 0) {
			page.value += 1
		}
	} catch (error) {
		console.error('加载帮扶记录失败', error)
		if (isRefresh) {
			foundationData.records = []
		}
		hasMore.value = false
	} finally {
		foundationData.loading = false
	}
}

const onRecordDetail = (item) => {
	uni.navigateTo({ url: RONGJH_ROUTES.foundationDetail(item.id) })
}

const onMoreRecords = () => {
	uni.showToast({ title: '继续上滑加载更多记录', icon: 'none' })
}

onShow(async () => {
	await loadFoundationSummary()
	await loadFoundationRecords(true)
})

onPullDownRefresh(() => {
	Promise.all([loadFoundationSummary(), loadFoundationRecords(true)]).finally(() => {
		uni.stopPullDownRefresh()
	})
})

onReachBottom(() => {
	loadFoundationRecords(false)
})
</script>

<style lang="scss" scoped>
.page {
	background: #f7f7f7;
	min-height: 100vh;
	padding-bottom: constant(safe-area-inset-bottom);
	padding-bottom: env(safe-area-inset-bottom);
}

.hero-top-bg {
	position: relative;
	overflow: hidden;
	padding-bottom: 16rpx;
	min-height: 520rpx;
	background: #b81d18;
}

.hero-bg-image {
	position: absolute;
	left: 0;
	top: 0;
	width: 100%;
	height: 100%;
	min-height: 100%;
	z-index: 0;
	pointer-events: none;
	object-position: center top;
}

.hero-top-content {
	position: relative;
	z-index: 1;
}

.hero {
	padding: 64rpx 36rpx 48rpx;
	color: #fff;
	position: relative;
	overflow: hidden;
}

.hero-content {
	position: relative;
	z-index: 1;
}

.hero-top {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	gap: 20rpx;
	flex-wrap: wrap;
	margin-bottom: 24rpx;
}

.hero-title {
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.hero-name {
	font-size: 34rpx;
	font-weight: 800;
	letter-spacing: 1rpx;
}

.hero-desc {
	margin-top: 10rpx;
	font-size: 28rpx;
	line-height: 1.45;
	opacity: 0.95;
	display: block;
}

.hero-cards {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 16rpx;
}

.hero-links {
	margin-top: 20rpx;
	display: flex;
	justify-content: flex-end;
}

.hero-link {
	font-size: 26rpx;
	color: rgba(255, 255, 255, 0.92);
	text-decoration: underline;
}

.hero-card {
	background: rgba(255, 255, 255, 0.14);
	border: 1rpx solid rgba(255, 255, 255, 0.18);
	border-radius: 22rpx;
	padding: 22rpx 20rpx;
	backdrop-filter: blur(10rpx);
	box-shadow: 0 10rpx 28rpx rgba(0, 0, 0, 0.08);
}

.hero-card.accent {
	background: linear-gradient(180deg, rgba(255, 255, 255, 0.2) 0%, rgba(255, 255, 255, 0.14) 100%);
}

.card-topline {
	display: flex;
	justify-content: space-between;
	align-items: center;
	gap: 12rpx;
	margin-bottom: 14rpx;
}

.card-badge {
	padding: 6rpx 12rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.16);
	font-size: 20rpx;
	font-weight: 600;
}

.card-badge.light {
	background: rgba(255, 255, 255, 0.22);
}

.card-subtle {
	font-size: 20rpx;
	opacity: 0.82;
}

.card-subtle.light {
	opacity: 0.9;
}

.card-label {
	font-size: 24rpx;
	opacity: 0.9;
	font-weight: 600;
}

.card-value {
	display: flex;
	align-items: baseline;
	gap: 8rpx;
	margin-top: 12rpx;
	flex-wrap: wrap;
}

.card-number {
	font-size: 48rpx;
	font-weight: 800;
	line-height: 1;
}

.card-unit {
	font-size: 24rpx;
	opacity: 0.95;
}

.section {
	margin: 22rpx 24rpx 0;
}

.tip-card {
	background: linear-gradient(135deg, #fff7f7 0%, #fff 100%);
	border: 1rpx solid #ffd7d7;
	border-radius: 20rpx;
	padding: 20rpx 22rpx;
	display: flex;
	align-items: flex-start;
	gap: 14rpx;
	box-shadow: 0 8rpx 22rpx rgba(216, 45, 36, 0.06);
}

.tip-left {
	display: flex;
	align-items: center;
	gap: 6rpx;
	flex-shrink: 0;
}

.tip-title {
	font-size: 26rpx;
	color: #d62d24;
	font-weight: 600;
}

.tip-text {
	font-size: 24rpx;
	color: #9f6a6a;
	flex: 1;
	line-height: 1.6;
}

.section-header {
	display: flex;
	align-items: flex-end;
	justify-content: space-between;
	gap: 16rpx;
	margin-bottom: 18rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: 800;
	color: #222;
	line-height: 1.2;
}

.section-subtitle.small {
	display: block;
	font-size: 22rpx;
	color: #a3a3a3;
	margin-top: 6rpx;
}

.section-more {
	display: flex;
	align-items: center;
	gap: 4rpx;
}

.section-more-text {
	font-size: 24rpx;
	color: #999;
}

.record-list {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.record-card {
	background: #fff;
	border-radius: 20rpx;
	overflow: hidden;
	box-shadow: 0 10rpx 26rpx rgba(0, 0, 0, 0.05);
	display: flex;
	border: 1rpx solid #f3f3f3;
}

.record-image {
	width: 220rpx;
	height: 180rpx;
	flex-shrink: 0;
}

.record-image-empty {
	width: 220rpx;
	height: 180rpx;
	flex-shrink: 0;
	display: flex;
	align-items: center;
	justify-content: center;
	background: #f3f4f6;
}

.record-image-empty-text {
	font-size: 24rpx;
	color: #9ca3af;
}

.record-content {
	padding: 20rpx 18rpx;
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 10rpx;
	min-width: 0;
}

.record-title {
	font-size: 28rpx;
	font-weight: 700;
	color: #222;
	line-height: 1.4;
}

.record-desc {
	font-size: 24rpx;
	color: #666;
	line-height: 1.55;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
}

.record-meta {
	margin-top: auto;
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 12rpx;
	font-size: 22rpx;
	color: #999;
}

.record-amount {
	color: #b81d18;
	font-weight: 600;
}

.record-empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 60rpx 0;
	gap: 16rpx;
}

.record-empty-text {
	font-size: 28rpx;
	color: #999;
}

.loading-more,
.load-finish {
	padding: 40rpx 0;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 12rpx;
}

.loading-more-text {
	font-size: 26rpx;
	color: #999;
}

.apply-entry {
	background: linear-gradient(135deg, #fff 0%, #fff7f7 100%);
	border-radius: 20rpx;
	padding: 24rpx;
	box-shadow: 0 10rpx 26rpx rgba(0, 0, 0, 0.05);
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16rpx;
	border: 1rpx solid #f5e3e3;
}

.apply-entry-text {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
	flex: 1;
}

.apply-entry-title {
	font-size: 28rpx;
	font-weight: 600;
	color: #333;
}

.apply-entry-desc {
	font-size: 24rpx;
	color: #999;
}

.apply-entry-btn {
	padding: 18rpx 32rpx;
	border-radius: 999rpx;
	background: linear-gradient(135deg, #d62d24, #b81d18);
	color: #fff;
	font-size: 28rpx;
	font-weight: 700;
	box-shadow: 0 10rpx 18rpx rgba(184, 29, 24, 0.22);
	flex-shrink: 0;
}

.page-space {
	height: 30rpx;
}
</style>
