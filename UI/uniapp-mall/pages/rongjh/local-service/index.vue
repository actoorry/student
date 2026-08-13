<template>
	<view class="page">
		<CustomNav :title="pageTitle" background="url(/static/images/nav-bg.png) center / cover no-repeat" color="#fff" />

		<view class="location-row" @tap="onLocationTap">
			<view class="location-info">
				<uni-icons type="location" size="16" color="#D62D24"></uni-icons>
				<text class="location-text">{{ currentLocation.name || currentLocation.address || '选择位置' }}</text>
			</view>
			<view class="location-switch">
				<text class="location-switch-text">切换地址</text>
				<uni-icons type="right" size="14" color="#999"></uni-icons>
			</view>
		</view>

		<swiper
			class="banner-swiper"
			:indicator-dots="true"
			:autoplay="true"
			:interval="4000"
			:circular="true"
			indicator-color="rgba(255,255,255,0.4)"
			indicator-active-color="#fff"
		>
			<swiper-item v-for="(item, index) in bannerList" :key="index">
				<view class="banner-item">
					<image v-if="item.image" class="banner-img" :src="item.image" mode="aspectFill"></image>
					<view v-else class="banner-placeholder">
						<text class="banner-placeholder-title">{{ pageTitle }}</text>
						<text class="banner-placeholder-desc">同城优选 · 快速送达</text>
					</view>
				</view>
			</swiper-item>
		</swiper>

		<view class="search-row">
			<view class="search-bar">
				<uni-icons type="search" size="16" color="#999"></uni-icons>
				<input
					class="search-input"
					v-model="keyword"
					placeholder="搜索同城服务"
					placeholder-class="search-placeholder"
					@confirm="onSearch"
				/>
			</view>
			<text class="search-btn" @tap="onSearch">搜索</text>
		</view>

		<scroll-view
			class="product-scroll"
			scroll-y
			@scrolltolower="loadMore"
			:lower-threshold="100"
		>
			<view class="product-grid">
				<view class="product-card" v-for="item in displayList" :key="item.id" @tap="onProduct(item)">
					<view class="product-card-img-wrap">
						<image v-if="item.image" class="product-card-img" :src="item.image" mode="aspectFill"></image>
						<view v-else class="product-img-empty">
							<text class="product-img-empty-text">暂无图片</text>
						</view>
					</view>
					<view class="product-card-info">
						<text class="product-card-name">{{ item.name }}</text>
						<text class="product-card-desc" v-if="item.description">{{ item.description }}</text>
						<view class="product-card-price">
							<text class="price-symbol">¥</text>
							<text class="price-value">{{ item.price }}</text>
						</view>
					</view>
				</view>
			</view>

			<view class="load-more" v-if="loading">
				<text class="load-more-text">加载中...</text>
			</view>
			<view class="load-more" v-else-if="!hasMore && displayList.length">
				<text class="load-more-text">— 已加载全部 —</text>
			</view>
			<view class="empty-box" v-else-if="!loading && !displayList.length">
				<text class="empty-text">暂无商品</text>
				<text class="empty-desc">换个城市试试吧~</text>
			</view>
		</scroll-view>

		<!-- 省市选择弹窗 -->
		<view v-if="locationPickerVisible" class="location-mask" @tap="closeLocationPicker">
			<view class="location-picker" @tap.stop>
				<view class="picker-header">
					<text class="picker-action" @tap="closeLocationPicker">取消</text>
					<text class="picker-title">选择位置</text>
					<text class="picker-action picker-confirm" @tap="confirmLocationPicker">确定</text>
				</view>
				<picker-view
					class="picker-body"
					:value="[provinceIndex, cityIndex]"
					@change="onPickerChange"
				>
					<picker-view-column>
						<view class="picker-item" v-for="(province, pIdx) in pickerProvinces" :key="province.id">
							<text class="picker-item-text">{{ province.name }}</text>
						</view>
					</picker-view-column>
					<picker-view-column>
						<view class="picker-item" v-for="(city, cIdx) in pickerCities" :key="city.id">
							<text class="picker-item-text">{{ city.name }}</text>
						</view>
					</picker-view-column>
				</picker-view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import {
	getCityProductsApi,
	getLocalServiceBannersApi,
} from '@/sheep/api/rongjh/adapter'
import {
	resolveCityLocation,
	getStoredLocation,
	setStoredLocation,
	buildCityLocation,
	fetchAreaTree,
} from '@/sheep/helper/rongjh-location'
import sheep from '@/sheep'

const pageTitle = ref('同城服务')
const keyword = ref('')
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const hasMore = ref(true)

const currentLocation = ref({ cityId: 0, name: '定位中...' })
const areaTree = ref([])

// 省市选择弹窗状态
const locationPickerVisible = ref(false)
const provinceIndex = ref(0)
const cityIndex = ref(0)
const pickerProvinces = ref([])
const pickerCities = ref([])
const allProducts = ref([])
const bannerList = reactive([{ image: '' }])

const filteredProducts = computed(() => {
	const kw = keyword.value.trim()
	if (!kw) return allProducts.value
	return allProducts.value.filter(
		(item) =>
			String(item.name || '').includes(kw) || String(item.description || '').includes(kw),
	)
})

const displayList = computed(() => {
	const end = page.value * pageSize
	return filteredProducts.value.slice(0, end)
})

const loadBanners = async () => {
	try {
		const res = await getLocalServiceBannersApi()
		const list = res?.data?.list || [{ image: '' }]
		bannerList.splice(0, bannerList.length, ...list)
	} catch (err) {
		console.error('加载轮播失败', err)
	}
}

const loadProducts = async () => {
	const cityId = Number(currentLocation.value?.cityId || 0)
	if (!cityId) {
		allProducts.value = []
		return
	}
	loading.value = true
	try {
		const res = await getCityProductsApi({
			cityId,
			fetchAll: true,
			keyword: '',
		})
		allProducts.value = Array.isArray(res?.data?.list) ? res.data.list : []
		page.value = 1
		hasMore.value = allProducts.value.length > pageSize
	} catch (err) {
		console.error('加载同城服务失败', err)
		allProducts.value = []
		uni.showToast({ title: '加载失败', icon: 'none' })
	} finally {
		loading.value = false
	}
}

const onSearch = () => {
	page.value = 1
	hasMore.value = filteredProducts.value.length > pageSize
}

const loadMore = () => {
	if (loading.value || !hasMore.value) return
	if (displayList.value.length >= filteredProducts.value.length) {
		hasMore.value = false
		return
	}
	page.value += 1
	hasMore.value = displayList.value.length < filteredProducts.value.length
}

/** 取某省份下的市级节点 */
const getProvinceCities = (province = {}) =>
	Array.isArray(province?.children) ? province.children : []

const onLocationTap = async () => {
	if (!areaTree.value.length) {
		try {
			areaTree.value = await fetchAreaTree()
		} catch (err) {
			console.error('加载地区失败', err)
			uni.showToast({ title: '地区加载失败，请重试', icon: 'none' })
			return
		}
	}
	const provinces = areaTree.value
	if (!provinces.length) {
		uni.showToast({ title: '地区加载失败，请重试', icon: 'none' })
		return
	}
	pickerProvinces.value = provinces
	// 定位当前省份
	const curProvinceId = Number(currentLocation.value?.provinceId || 0)
	let pIdx = provinces.findIndex((item) => Number(item.id) === curProvinceId)
	if (pIdx < 0) pIdx = 0
	provinceIndex.value = pIdx
	// 当前省份无城市时给出提示且不打开弹窗
	const cities = getProvinceCities(provinces[pIdx])
	if (!cities.length) {
		uni.showToast({ title: '该省暂无可选城市', icon: 'none' })
		return
	}
	pickerCities.value = cities
	// 定位当前城市
	const curCityId = Number(currentLocation.value?.cityId || 0)
	let cIdx = cities.findIndex((item) => Number(item.id) === curCityId)
	if (cIdx < 0) cIdx = 0
	cityIndex.value = cIdx
	locationPickerVisible.value = true
}

/** picker 滚动变化：切换省份时重置到该省首个城市 */
const onPickerChange = (e) => {
	const [pIdx, cIdx] = e?.detail?.value || [0, 0]
	const province = pickerProvinces.value[pIdx]
	if (pIdx !== provinceIndex.value) {
		provinceIndex.value = pIdx
		pickerCities.value = getProvinceCities(province)
		cityIndex.value = 0
	} else {
		provinceIndex.value = pIdx
		cityIndex.value = cIdx
	}
}

/** 取消/遮罩关闭：不改变当前地址与缓存 */
const closeLocationPicker = () => {
	locationPickerVisible.value = false
}

/** 确认：仅此时写入缓存并刷新商品 */
const confirmLocationPicker = () => {
	const province = pickerProvinces.value[provinceIndex.value]
	const cities = getProvinceCities(province)
	const city = cities[cityIndex.value]
	if (!province || !city) {
		uni.showToast({ title: '该省暂无可选城市', icon: 'none' })
		return
	}
	const location = buildCityLocation(province, city)
	if (!location?.cityId) {
		uni.showToast({ title: '位置无效，请重试', icon: 'none' })
		return
	}
	locationPickerVisible.value = false
	setStoredLocation(location)
	currentLocation.value = location
	keyword.value = ''
	loadProducts()
}

const onProduct = (item) => {
	if (item?.id) {
		sheep.$router.go('/pages/goods/index', { id: item.id })
		return
	}
	uni.showToast({ title: item.name, icon: 'none' })
}

onLoad(async (options) => {
	const name = decodeURIComponent(options?.categoryName || '')
	if (name) {
		pageTitle.value = name.includes('同城') ? '同城服务' : name
	}
	loadBanners()
	try {
		areaTree.value = await fetchAreaTree()
	} catch (err) {
		console.error('加载地区失败', err)
		uni.showToast({ title: '地区加载失败，请重试', icon: 'none' })
	}
	currentLocation.value = await resolveCityLocation()
	await loadProducts()
})

onShow(async () => {
	const stored = getStoredLocation()
	if (stored?.cityId && stored.cityId !== currentLocation.value.cityId) {
		currentLocation.value = stored
		await loadProducts()
	}
})
</script>

<style lang="scss" scoped>
.page {
	background: #F2F2F2;
	min-height: 100vh;
	display: flex;
	flex-direction: column;
}

.location-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20rpx 24rpx;
	background: #fff;
	border-bottom: 1rpx solid #f0f0f0;
}

.location-info {
	display: flex;
	align-items: center;
	gap: 8rpx;
	flex: 1;
	min-width: 0;
}

.location-text {
	font-size: 28rpx;
	color: #333;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.location-switch {
	display: flex;
	align-items: center;
	gap: 4rpx;
	flex-shrink: 0;
}

.location-switch-text {
	font-size: 26rpx;
	color: #999;
}

.banner-swiper {
	width: 100%;
	height: 320rpx;
}

.banner-item {
	width: 100%;
	height: 100%;
}

.banner-img {
	width: 100%;
	height: 100%;
}

.banner-placeholder {
	width: 100%;
	height: 100%;
	background: linear-gradient(135deg, #F5E6D0, #E8D5B8);
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 12rpx;
}

.banner-placeholder-title {
	font-size: 36rpx;
	font-weight: bold;
	color: #6B4C2A;
}

.banner-placeholder-desc {
	font-size: 26rpx;
	color: #8B7355;
}

.search-row {
	display: flex;
	align-items: center;
	padding: 20rpx 24rpx;
	background: #fff;
	gap: 16rpx;
}

.search-bar {
	flex: 1;
	display: flex;
	align-items: center;
	background: #f5f5f5;
	border-radius: 100rpx;
	padding: 16rpx 24rpx;
	gap: 10rpx;
}

.search-input {
	flex: 1;
	font-size: 30rpx;
	color: #333;
}

.search-placeholder {
	color: #bbb;
	font-size: 30rpx;
}

.search-btn {
	font-size: 32rpx;
	font-weight: 500;
	color: #333;
}

.product-scroll {
	flex: 1;
	height: calc(100vh - 520rpx);
}

.product-grid {
	display: flex;
	flex-wrap: wrap;
	justify-content: space-between;
	padding: 16rpx;
}

.product-card {
	width: 48%;
	background: #fff;
	border-radius: 16rpx;
	overflow: hidden;
	margin-bottom: 16rpx;
}

.product-card-img-wrap {
	width: 100%;
	padding-bottom: 100%;
	position: relative;
	overflow: hidden;
	background: #f5f5f5;
}

.product-card-img {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
}

.product-img-empty {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	display: flex;
	align-items: center;
	justify-content: center;
}

.product-img-empty-text {
	font-size: 28rpx;
	color: #ccc;
}

.product-card-info {
	padding: 16rpx 20rpx 24rpx;
}

.product-card-name {
	font-size: 30rpx;
	color: #333;
	line-height: 1.5;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
}

.product-card-desc {
	display: block;
	font-size: 24rpx;
	color: #999;
	margin-top: 8rpx;
}

.product-card-price {
	display: flex;
	align-items: baseline;
	gap: 2rpx;
	margin-top: 12rpx;
}

.price-symbol {
	font-size: 26rpx;
	color: #E65C1C;
	font-weight: bold;
}

.price-value {
	font-size: 44rpx;
	color: #E65C1C;
	font-weight: bold;
}

.load-more {
	padding: 32rpx 0 48rpx;
	text-align: center;
}

.load-more-text {
	font-size: 28rpx;
	color: #ccc;
}

.empty-box {
	padding: 120rpx 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 16rpx;
}

.empty-text {
	font-size: 32rpx;
	color: #999;
}

.empty-desc {
	font-size: 28rpx;
	color: #ccc;
}

/* 省市选择弹窗 */
.location-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.5);
	z-index: 1000;
	display: flex;
	align-items: flex-end;
}

.location-picker {
	width: 100%;
	background: #fff;
	border-radius: 24rpx 24rpx 0 0;
	overflow: hidden;
}

.picker-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 28rpx 32rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.picker-action {
	font-size: 30rpx;
	color: #999;
}

.picker-confirm {
	color: #D62D24;
	font-weight: 500;
}

.picker-title {
	font-size: 32rpx;
	font-weight: 500;
	color: #333;
}

.picker-body {
	height: 480rpx;
}

.picker-item {
	display: flex;
	align-items: center;
	justify-content: center;
	height: 88rpx;
}

.picker-item-text {
	font-size: 30rpx;
	color: #333;
}
</style>
