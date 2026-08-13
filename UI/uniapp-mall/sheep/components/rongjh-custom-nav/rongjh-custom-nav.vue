<template>
	<view class="custom-nav" :style="navBackgroundStyle">
		<image v-if="bgImageSrc" class="nav-bg-image" :src="bgImageSrc" mode="aspectFill"></image>
		<view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
		<view class="nav-bar" :style="{ height: navBarHeight + 'px' }">
			<view class="nav-left" v-if="showBack" @tap="onBack">
				<uni-icons v-if="leftIcon === 'arrow-left'" type="back" size="19" :color="titleColor"></uni-icons>
				<text v-else class="back-icon" :style="{ color: titleColor }">〈</text>
			</view>
			<view class="nav-left" v-else-if="showHomeFallback" @tap="onGoHome">
				<uni-icons type="home" size="19" :color="titleColor"></uni-icons>
			</view>
			<text class="nav-title" :style="{ color: titleColor }" v-if="!showBackArrowOnly && displayTitle">{{ displayTitle }}</text>
			<view class="nav-right"></view>
		</view>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
	title: { type: String, default: '' },
	background: { type: String, default: '#B81D18' },
	color: { type: String, default: '#fff' },
	showBack: { type: Boolean, default: undefined },
	leftIcon: { type: String, default: 'arrow-left' },
	emptyBackMode: { type: String, default: 'switchTab' },
	emptyBackUrl: { type: String, default: '/pages/index/index' },
	showHomeWhenNoBack: { type: Boolean, default: false },
})

const systemInfo = uni.getSystemInfoSync() || {}
const statusBarHeight = ref(Number(systemInfo.statusBarHeight || 0))

const resolveNavBarHeight = () => {
	const platform = String(systemInfo.platform || '').toLowerCase()
	const osName = String(systemInfo.osName || '').toLowerCase()
	const system = String(systemInfo.system || '').toLowerCase()
	const uniPlatform = String(systemInfo.uniPlatform || '').toLowerCase()
	const isMiniProgram = uniPlatform.startsWith('mp-')
	const isApp = uniPlatform === 'app' || uniPlatform === 'app-plus'

	if (isApp) {
		return 44
	}

	if (isMiniProgram && typeof uni.getMenuButtonBoundingClientRect === 'function') {
		const menuButton = uni.getMenuButtonBoundingClientRect()
		if (menuButton && menuButton.height && menuButton.top) {
			return menuButton.height + (menuButton.top - statusBarHeight.value) * 2
		}
	}

	const isIOS = platform === 'ios' || osName === 'ios'
	const isHarmony = osName.includes('harmony') || system.includes('harmony')
	if (isIOS) return 44
	if (isHarmony) return 46
	return 48
}

const navBarHeight = ref(resolveNavBarHeight())

const parseBackground = computed(() => {
	const bg = String(props.background || '').trim()
	if (!bg) return { imageSrc: '', style: { backgroundColor: '#B81D18' } }

	if (!bg.includes('url(')) {
		return { imageSrc: '', style: { background: bg } }
	}

	const match = bg.match(/url\(([^)]+)\)/i)
	const rawUrl = match?.[1]?.trim()?.replace(/^['"]|['"]$/g, '') || ''
	return {
		imageSrc: rawUrl,
		style: { backgroundColor: 'transparent' },
	}
})

const bgImageSrc = computed(() => parseBackground.value.imageSrc)
const navBackgroundStyle = computed(() => parseBackground.value.style)
const titleColor = computed(() => props.color)
const showBack = computed(() => {
	if (props.showBack !== undefined) return props.showBack
	const pages = getCurrentPages()
	return pages.length > 1
})

const leftIcon = computed(() => props.leftIcon)

const showHomeFallback = computed(() => props.showHomeWhenNoBack && !showBack.value)

const displayTitle = computed(() => {
	const info = uni.getSystemInfoSync() || {}
	const uniPlatform = String(info.uniPlatform || '').toLowerCase()
	const isMiniProgram = uniPlatform.startsWith('mp-')
	return isMiniProgram ? props.title : ''
})

const showBackArrowOnly = computed(() => {
	const info = uni.getSystemInfoSync() || {}
	const uniPlatform = String(info.uniPlatform || '').toLowerCase()
	return uniPlatform === 'app' || uniPlatform === 'app-plus'
})

const goHomePage = () => {
	if (props.emptyBackMode === 'reLaunch') {
		uni.reLaunch({ url: props.emptyBackUrl || '/pages/index/index' })
		return
	}
	uni.switchTab({ url: props.emptyBackUrl || '/pages/index/index' })
}

const onBack = () => {
	const pages = getCurrentPages()
	if (pages.length > 1) {
		uni.navigateBack()
		return
	}
	goHomePage()
}

const onGoHome = () => {
	goHomePage()
}
</script>

<style lang="scss" scoped>
.custom-nav {
	width: 100%;
	position: sticky;
	top: 0;
	z-index: 999;
	overflow: hidden;
}

.nav-bg-image {
	position: absolute;
	left: 0;
	top: 0;
	width: 100%;
	height: 100%;
	z-index: 0;
	pointer-events: none;
}

.status-bar {
	width: 100%;
	position: relative;
	z-index: 1;
}

.nav-bar {
	position: relative;
	z-index: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 0 24rpx;
	border-bottom: 0;
	box-shadow: none;
}

.nav-title {
	font-size: 36rpx;
	font-weight: 600;
}

/* #ifdef APP-PLUS */
.nav-title {
	font-size: 32rpx;
	font-weight: 500;
}
/* #endif */

.nav-left,
.nav-right {
	position: absolute;
	top: 0;
	bottom: 0;
	display: flex;
	align-items: center;
}

.nav-left {
	left: 20rpx;
}

/* #ifdef APP-PLUS */
.nav-left {
	left: 16rpx;
}
/* #endif */

.nav-right {
	right: 20rpx;
}

.back-icon {
	font-size: 36rpx;
	color: #fff;
	font-weight: 600;
}

/* #ifdef APP-PLUS */
.back-icon {
	font-size: 32rpx;
}
/* #endif */
</style>
