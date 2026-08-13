<template>
	<view class="page">
		<CustomNav :title="title || '协议'" background="url(/static/images/nav-bg.png) center / cover no-repeat" color="#fff" show-home-when-no-back />
		<view class="content-card">
			<text class="content-title">{{ title }}</text>
			<text class="update-time">更新日期：{{ updateDate }}</text>
			<view class="content-body">
				<view class="section" v-for="(section, index) in sections" :key="index">
					<text class="section-title">{{ section.title }}</text>
					<text class="section-text">{{ section.content }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getProtocolContentApi } from '@/sheep/api/rongjh/adapter'

const title = ref('')
const updateDate = ref('2026年1月1日')
const sections = reactive([])

const loadProtocol = async (type) => {
	try {
		const res = await getProtocolContentApi(type)
		const data = res?.data || {}
		title.value = data.title || (type === 'privacy' ? '隐私政策' : '用户协议')
		updateDate.value = data.updateDate || updateDate.value
		sections.splice(0, sections.length, ...(data.sections || []))
	} catch (err) {
		console.error('加载协议失败', err)
		uni.showToast({ title: '加载失败', icon: 'none' })
	}
}

onLoad((options) => {
	const type = options?.type || 'user'
	loadProtocol(type)
})
</script>

<style lang="scss" scoped>
.page {
	min-height: 100vh;
	background: #F5F5F5;
}

.content-card {
	margin: 20rpx 24rpx;
	background: #fff;
	border-radius: 16rpx;
	padding: 36rpx 30rpx;
}

.content-title {
	font-size: 36rpx;
	color: #333;
	font-weight: bold;
	display: block;
	text-align: center;
	margin-bottom: 12rpx;
}

.update-time {
	font-size: 28rpx;
	color: #999;
	display: block;
	text-align: center;
	margin-bottom: 36rpx;
}

.section {
	margin-bottom: 36rpx;
}

.section:last-child {
	margin-bottom: 0;
}

.section-title {
	font-size: 28rpx;
	color: #333;
	font-weight: bold;
	display: block;
	margin-bottom: 16rpx;
}

.section-text {
	font-size: 30rpx;
	color: #666;
	line-height: 1.8;
	white-space: pre-wrap;
}
</style>
