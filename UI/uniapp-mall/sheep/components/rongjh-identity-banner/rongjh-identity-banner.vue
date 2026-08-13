<template>
	<view class="identity-banner-wrap" v-if="visible">
		<view
			class="identity-banner"
			:class="[`is-${certStatus}`]"
			@tap="onTap"
		>
			<view class="banner-left">
				<uni-icons
					:type="certStatus === 'approved' ? 'checkbox-filled' : 'star-filled'"
					size="18"
					:color="certStatus === 'approved' ? '#52A052' : '#F97316'"
				></uni-icons>
				<text class="banner-text">{{ bannerText }}</text>
			</view>
			<view v-if="showAction" class="banner-action">
				<text class="action-text">{{ actionLabel }}</text>
				<uni-icons type="right" size="12" color="#F97316"></uni-icons>
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import sheep from '@/sheep'
import { showAuthModal } from '@/sheep/hooks/useModal'
import { getIdentityCertStatusApi } from '@/sheep/api/rongjh/adapter'

const DEFAULT_CONFIG = {
	applyUrl: '/pages/rongjh/identity/apply',
	textNone: '退役军人及三属人员认证可享更多优惠',
	textPending: '您的认证申请审核中，请耐心等待',
	textApproved: '您已通过军人及三属人员认证',
	actionText: '立即认证',
	actionTextRejected: '重新认证',
}

const props = defineProps({
	/** 装修配置（来自 DIY property） */
	data: {
		type: Object,
		default: () => ({}),
	},
	/** 未登录时是否隐藏整条横幅 */
	hideWhenGuest: {
		type: Boolean,
		default: false,
	},
})

const loading = ref(false)
const certStatus = ref('none') // none | pending | approved | rejected

const config = computed(() => ({
	applyUrl: props.data?.applyUrl || DEFAULT_CONFIG.applyUrl,
	textNone: props.data?.textNone || DEFAULT_CONFIG.textNone,
	textPending: props.data?.textPending || DEFAULT_CONFIG.textPending,
	textApproved: props.data?.textApproved || DEFAULT_CONFIG.textApproved,
	actionText: props.data?.actionText || DEFAULT_CONFIG.actionText,
	actionTextRejected: props.data?.actionTextRejected || DEFAULT_CONFIG.actionTextRejected,
}))

const isLogin = computed(() => sheep.$store('user').isLogin)
const visible = computed(() => {
	if (props.hideWhenGuest && !isLogin.value) return false
	return true
})

const bannerText = computed(() => {
	if (certStatus.value === 'approved') {
		return config.value.textApproved
	}
	if (certStatus.value === 'pending') {
		return config.value.textPending
	}
	return config.value.textNone
})

const showAction = computed(() => certStatus.value === 'none' || certStatus.value === 'rejected')
const actionLabel = computed(() =>
	certStatus.value === 'rejected' ? config.value.actionTextRejected : config.value.actionText,
)

const loadStatus = async () => {
	if (!isLogin.value) {
		certStatus.value = 'none'
		return
	}
	loading.value = true
	try {
		const res = await getIdentityCertStatusApi()
		const data = res?.data || {}
		if (data.is_member || data.state === 'approved') {
			certStatus.value = 'approved'
		} else if (data.state === 'rejected') {
			certStatus.value = 'rejected'
		} else if (data.has_application || data.state === 'pending') {
			certStatus.value = 'pending'
		} else {
			certStatus.value = 'none'
		}
	} catch (err) {
		console.error('加载身份认证状态失败', err)
		certStatus.value = 'none'
	} finally {
		loading.value = false
	}
}

const onTap = () => {
	if (certStatus.value === 'approved') return
	if (certStatus.value === 'pending') return
	if (!isLogin.value) {
		showAuthModal('accountLogin')
		return
	}
	const url = config.value.applyUrl
	if (url) {
		sheep.$router.go(url)
	}
}

defineExpose({ refresh: loadStatus })

onShow(() => {
	loadStatus()
})
</script>

<style lang="scss" scoped>
.identity-banner-wrap {
	padding: 0 24rpx 20rpx;
}

.identity-banner {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16rpx;
	padding: 22rpx 24rpx;
	border-radius: 20rpx;
	background: linear-gradient(90deg, #fff8f2 0%, #fff3ea 100%);
	box-shadow: 0 4rpx 16rpx rgba(249, 115, 22, 0.08);
}

.identity-banner.is-approved {
	background: linear-gradient(90deg, #f6fff6 0%, #f0faf0 100%);
	box-shadow: 0 4rpx 16rpx rgba(82, 160, 82, 0.08);
}

.identity-banner.is-pending {
	background: linear-gradient(90deg, #fffaf5 0%, #fff5eb 100%);
}

.banner-left {
	flex: 1;
	display: flex;
	align-items: center;
	gap: 12rpx;
	min-width: 0;
}

.banner-text {
	flex: 1;
	font-size: 26rpx;
	line-height: 1.45;
	color: #9a4b1f;
}

.is-approved .banner-text {
	color: #3d7a3d;
}

.banner-action {
	display: flex;
	align-items: center;
	gap: 4rpx;
	flex-shrink: 0;
}

.action-text {
	font-size: 26rpx;
	font-weight: 600;
	color: #f97316;
}
</style>
