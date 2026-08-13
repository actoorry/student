<template>
	<view class="page">
		<CustomNav title="联系客服" background="url(/static/images/nav-bg.png) center / cover no-repeat" color="#fff" />
		<view class="service-header">
			<view class="service-icon">
				<uni-icons type="chat-filled" size="48" color="#B81D18"></uni-icons>
			</view>
			<text class="service-title">{{ pageData.title }}</text>
			<text class="service-desc">{{ pageData.subtitle }}</text>
		</view>

		<view class="contact-card">
			<view class="contact-item" @tap="onCall">
				<view class="contact-icon-wrap" style="background: #FFF0EE;">
					<uni-icons type="phone-filled" size="22" color="#B81D18"></uni-icons>
				</view>
				<view class="contact-info">
					<text class="contact-label">客服热线</text>
					<text class="contact-value">{{ pageData.displayPhone }}</text>
				</view>
				<view class="contact-btn">
					<text class="contact-btn-text">拨打</text>
				</view>
			</view>
			<view class="contact-item">
				<view class="contact-icon-wrap" style="background: #E8F5E9;">
					<uni-icons type="clock" size="22" color="#52A052"></uni-icons>
				</view>
				<view class="contact-info">
					<text class="contact-label">服务时间</text>
					<text class="contact-value">{{ pageData.serviceHours }}</text>
				</view>
			</view>
		</view>

		<view class="faq-card">
			<view class="faq-header">
				<text class="faq-title">常见问题</text>
			</view>
			<view class="faq-list">
				<view
					class="faq-item"
					v-for="(item, index) in faqList"
					:key="index"
					@tap="onToggleFaq(index)"
				>
					<view class="faq-question">
						<text class="faq-q-text">{{ item.question }}</text>
						<uni-icons :type="item.expanded ? 'up' : 'down'" size="14" color="#999"></uni-icons>
					</view>
					<view class="faq-answer" v-if="item.expanded">
						<text class="faq-a-text">{{ item.answer }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { getContactServiceApi } from '@/sheep/api/rongjh/adapter'

const pageData = reactive({
	title: '戎集汇客服中心',
	subtitle: '有问题随时联系我们，竭诚为您服务',
	hotline: '4008888888',
	displayPhone: '18139337035',
	serviceHours: '周一至周日 9:00 - 21:00',
})

const faqList = reactive([])

const loadPage = async () => {
	try {
		const res = await getContactServiceApi()
		const data = res?.data || {}
		pageData.title = data.title || pageData.title
		pageData.subtitle = data.subtitle || pageData.subtitle
		pageData.hotline = data.hotline || pageData.hotline
		pageData.displayPhone = data.displayPhone || pageData.displayPhone
		pageData.serviceHours = data.serviceHours || pageData.serviceHours
		const list = Array.isArray(data.faqList) ? data.faqList : []
		faqList.splice(
			0,
			faqList.length,
			...list.map((item) => ({ ...item, expanded: false })),
		)
	} catch (err) {
		console.error('加载客服页失败', err)
	}
}

const onToggleFaq = (index) => {
	if (faqList[index]) {
		faqList[index].expanded = !faqList[index].expanded
	}
}

const onCall = () => {
	uni.makePhoneCall({
		phoneNumber: pageData.hotline,
		fail: () => {
			uni.setClipboardData({
				data: pageData.displayPhone,
				success: () => uni.showToast({ title: '号码已复制', icon: 'success' }),
			})
		},
	})
}

onMounted(() => {
	loadPage()
})
</script>

<style lang="scss" scoped>
.page {
	min-height: 100vh;
	background: #F5F5F5;
}

.service-header {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 60rpx 0 40rpx;
	background: #fff;
	gap: 12rpx;
}

.service-icon {
	width: 120rpx;
	height: 120rpx;
	border-radius: 50%;
	background: #FFF0EE;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 8rpx;
}

.service-title {
	font-size: 34rpx;
	color: #333;
	font-weight: bold;
}

.service-desc {
	font-size: 30rpx;
	color: #999;
}

.contact-card {
	margin: 20rpx 24rpx 0;
	background: #fff;
	border-radius: 16rpx;
	padding: 0 30rpx;
}

.contact-item {
	display: flex;
	align-items: center;
	padding: 30rpx 0;
	gap: 20rpx;
	border-bottom: 1rpx solid #f5f5f5;
}

.contact-item:last-child {
	border-bottom: none;
}

.contact-icon-wrap {
	width: 72rpx;
	height: 72rpx;
	border-radius: 16rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.contact-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 6rpx;
}

.contact-label {
	font-size: 28rpx;
	color: #999;
}

.contact-value {
	font-size: 34rpx;
	color: #333;
	font-weight: 500;
}

.contact-btn {
	padding: 12rpx 32rpx;
	background: #FFF0EE;
	border-radius: 32rpx;
	flex-shrink: 0;
}

.contact-btn-text {
	font-size: 30rpx;
	color: #B81D18;
	font-weight: 500;
}

.faq-card {
	margin: 20rpx 24rpx 0;
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
}

.faq-header {
	margin-bottom: 16rpx;
}

.faq-title {
	font-size: 30rpx;
	color: #333;
	font-weight: bold;
}

.faq-item {
	border-bottom: 1rpx solid #f5f5f5;
}

.faq-item:last-child {
	border-bottom: none;
}

.faq-question {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 28rpx 0;
}

.faq-q-text {
	font-size: 32rpx;
	color: #333;
	flex: 1;
}

.faq-answer {
	padding: 0 0 24rpx;
}

.faq-a-text {
	font-size: 30rpx;
	color: #666;
	line-height: 1.7;
}
</style>
