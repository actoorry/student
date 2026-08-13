<template>
	<view class="page">
		<CustomNav title="关于我们" background="url(/static/images/nav-bg.png) center / cover no-repeat" color="#fff" />
		<!-- 应用信息 -->
		<view class="app-info">
			<view class="app-logo">
				<image class="logo-img" :src="logo" mode="aspectFill" />
			</view>
			<text class="app-name">{{ appName }}</text>
			<text class="app-version">Version {{ version }}</text>
		</view>

		<!-- 公司介绍 -->
		<view class="section">
			<view class="section-header">
				<text class="section-title">关于我们</text>
			</view>
			<view class="section-body">
				<text class="section-text">{{ description }}</text>
			</view>
		</view>

		<!-- 功能列表 -->
		<view class="func-card">
			<view class="func-item" @tap="onCall">
				<view class="func-item-left">
					<uni-icons type="phone" size="18" color="#B81D18"></uni-icons>
					<text class="func-item-label">客服电话</text>
				</view>
				<view class="func-item-right">
					<text class="func-item-value">{{ servicePhone }}</text>
					<uni-icons type="right" size="14" color="#ccc"></uni-icons>
				</view>
			</view>
			<view class="func-item" @tap="onProtocol('user')">
				<view class="func-item-left">
					<uni-icons type="flag" size="18" color="#1890FF"></uni-icons>
					<text class="func-item-label">用户协议</text>
				</view>
				<uni-icons type="right" size="14" color="#ccc"></uni-icons>
			</view>
			<view class="func-item" @tap="onProtocol('privacy')">
				<view class="func-item-left">
					<uni-icons type="locked" size="18" color="#52A052"></uni-icons>
					<text class="func-item-label">隐私政策</text>
				</view>
				<uni-icons type="right" size="14" color="#ccc"></uni-icons>
			</view>
		</view>

		<!-- 底部版权 -->
		<view class="footer">
			<text class="footer-text">{{ copyright }}</text>
			<text class="footer-text">All Rights Reserved</text>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAboutPageApi } from '@/sheep/api/rongjh/adapter'
import RONGJH_ROUTES from '@/sheep/helper/rongjh-routes'

const appName = ref('戎集汇')
const version = ref('1.0.0')
const logo = ref('/static/images/login-icon.png')
const description = ref('')
const hotline = ref('4008888888')
const servicePhone = ref('18139337035')
const copyright = ref('Copyright © 2026 戎集汇')

const loadAbout = async () => {
	try {
		const res = await getAboutPageApi()
		const data = res?.data || {}
		appName.value = data.appName || appName.value
		version.value = data.version || version.value
		logo.value = data.logo || logo.value
		description.value = data.description || description.value
		hotline.value = data.hotline || hotline.value
		servicePhone.value = data.servicePhone || servicePhone.value
		copyright.value = data.copyright || copyright.value
	} catch (err) {
		console.error('加载关于我们失败', err)
	}
}

const onCall = () => {
	uni.makePhoneCall({
		phoneNumber: hotline.value,
		fail: () => {
			uni.setClipboardData({
				data: servicePhone.value,
				success: () => uni.showToast({ title: '号码已复制', icon: 'success' }),
			})
		},
	})
}

const onProtocol = (type) => {
	uni.navigateTo({ url: RONGJH_ROUTES.protocolIndex(type) })
}

onMounted(() => {
	loadAbout()
})
</script>

<style lang="scss" scoped>
.page {
	min-height: 100vh;
	background: #F5F5F5;
}

/* ===== 应用信息 ===== */
.app-info {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 80rpx 0 50rpx;
	background: #fff;
}

.app-logo {
	width: 200rpx;
	height: 200rpx;
	border-radius: 16rpx;
	background: transparent;
	display: flex;
	align-items: center;
	justify-content: center;
	overflow: hidden;
	margin-bottom: 24rpx;
}

.logo-img {
	width: 100%;
	height: 100%;
}

.app-name {
	font-size: 40rpx;
	color: #333;
	font-weight: bold;
	margin-bottom: 8rpx;
}

.app-version {
	font-size: 28rpx;
	color: #999;
}

/* ===== 公司介绍 ===== */
.section {
	margin: 20rpx 24rpx 0;
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
}

.section-header {
	margin-bottom: 20rpx;
}

.section-title {
	font-size: 30rpx;
	color: #333;
	font-weight: bold;
}

.section-text {
	font-size: 30rpx;
	color: #666;
	line-height: 1.8;
}

/* ===== 功能列表 ===== */
.func-card {
	margin: 20rpx 24rpx 0;
	background: #fff;
	border-radius: 16rpx;
	padding: 0 30rpx;
}

.func-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 32rpx 0;
	border-bottom: 1rpx solid #f5f5f5;
}

.func-item:last-child {
	border-bottom: none;
}

.func-item-left {
	display: flex;
	align-items: center;
	gap: 16rpx;
}

.func-item-label {
	font-size: 32rpx;
	color: #333;
}

.func-item-right {
	display: flex;
	align-items: center;
	gap: 8rpx;
}

.func-item-value {
	font-size: 32rpx;
	color: #666;
}

/* ===== 底部 ===== */
.footer {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 60rpx 0 40rpx;
	gap: 8rpx;
}

.footer-text {
	font-size: 26rpx;
	color: #ccc;
}
</style>
