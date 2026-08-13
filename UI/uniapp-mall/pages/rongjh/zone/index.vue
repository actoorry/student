<template>
	<view class="page" :class="{ 'page-specialty': zoneType === 'specialty' }">
		<CustomNav :title="zoneName || '专区'" background="url(/static/images/nav-bg.png) center / cover no-repeat" color="#fff" />

		<!-- 轮播图 -->
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
				<view class="banner-item" @tap="onBannerTap(item)">
					<image v-if="item.image" class="banner-img" :src="item.image" mode="aspectFill"></image>
					<view v-else class="banner-placeholder">
						<text class="banner-placeholder-title">{{ bannerPlaceholderTitle }}</text>
						<text class="banner-placeholder-desc">{{ bannerPlaceholderDesc }}</text>
					</view>
				</view>
			</swiper-item>
		</swiper>

		<!-- ===== 全国特产 ===== -->
		<template v-if="zoneType === 'specialty'">
			<view class="province-section">
				<scroll-view class="province-scroll" scroll-x="true" :show-scrollbar="false">
					<view class="province-list">
						<view
							class="province-item"
							v-for="item in provinceList"
							:key="item.id"
							:class="{ active: currentProvinceId === item.id }"
							@tap="onProvince(item)"
						>
							<text class="province-item-text province-item-text-lg">{{ item.displayName }}</text>
						</view>
					</view>
				</scroll-view>
			</view>

			<view class="selected-header">
				<text class="selected-title">{{ currentProvinceName || '精选特产' }}</text>
			</view>

			<scroll-view
				class="product-scroll"
				scroll-y="true"
				@scrolltolower="loadMoreSpecialty"
				:lower-threshold="100"
				:refresher-enabled="true"
				:refresher-triggered="refresherTriggered"
				@refresherrefresh="onSpecialtyRefresh"
				refresher-background="#F7F7F7"
				refresher-default-style="black"
			>
				<RongjhProductGrid
					:products="specialtyProductList"
					:columns="2"
					:gap="16"
					:showDescription="true"
					@itemTap="onProduct"
				/>

				<view v-if="specialtyLoadingMore" class="load-more">
					<text class="load-more-text">加载中...</text>
				</view>
				<view v-else-if="!specialtyHasMore && specialtyProductList.length > 0" class="load-more">
					<text class="load-more-text">— 已加载全部 —</text>
				</view>
				<view v-else-if="!specialtyLoading && specialtyProductList.length === 0" class="empty-container">
					<uni-icons type="empty" size="100" color="#ddd"></uni-icons>
					<text class="empty-text">暂无商品</text>
					<text class="empty-desc">换个省份试试吧~</text>
				</view>
			</scroll-view>
		</template>

		<!-- ===== 通用专区 ===== -->
		<template v-else>
			<view class="search-row">
				<view class="search-bar">
					<uni-icons type="search" size="16" color="#999"></uni-icons>
					<input class="search-input" v-model="keyword" placeholder="搜索" placeholder-class="search-placeholder" />
				</view>
				<text class="search-btn" @tap="onSearch">搜索</text>
			</view>

			<view class="product-grid">
				<view class="product-card" v-for="item in productList" :key="item.id" @tap="onProduct(item)">
					<view class="product-card-img-wrap">
						<image v-if="item.image" class="product-card-img" :src="item.image" mode="aspectFill"></image>
						<view v-else class="product-img-empty">
							<text class="product-img-empty-text">暂无图片</text>
						</view>
					</view>
					<view class="product-card-info">
						<text class="product-card-name">{{ item.name }}</text>
						<view class="product-card-price">
							<text class="price-symbol">¥</text>
							<text class="price-value">{{ item.price }}</text>
						</view>
					</view>
				</view>
			</view>

			<view class="load-more">
				<text class="load-more-text">— 已加载全部 —</text>
			</view>
		</template>
	</view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import {
	getZoneProductListApi,
	getRegionListApi,
	getSpecialtyZoneBannersApi,
	getSpecialtyZoneProductsApi,
} from '@/sheep/api/rongjh/adapter';
import sheep from '@/sheep';

const zoneName = ref('');
const zoneType = ref('default');
const keyword = ref('');
const loading = ref(false);

const bannerList = reactive([]);
const productList = reactive([]);

const bannerPlaceholderTitle = computed(() => {
	if (zoneType.value === 'specialty') return '全国特产';
	if (zoneType.value === 'military') return '军创区';
	return zoneName.value || '专区';
});

const bannerPlaceholderDesc = computed(() => {
	if (zoneType.value === 'specialty') return '精选好物 · 品质保障';
	if (zoneType.value === 'military') return '军创优选 · 品质保障';
	return '精选好物 · 品质保障';
});

const resolveZoneType = (options = {}) => {
	const name = decodeURIComponent(options?.name || '专区');
	if (options?.type) return options.type;
	if (/全国特产/.test(name)) return 'specialty';
	if (/军创/.test(name)) return 'military';
	return 'default';
};

const loadDefaultZoneData = async () => {
	loading.value = true;
	try {
		const res = await getZoneProductListApi({
			zone_name: zoneName.value,
			keyword: keyword.value,
		});
		const data = res?.data || {};
		bannerList.splice(0, bannerList.length, ...(data.banners || [{ image: '' }]));
		productList.splice(0, productList.length, ...(data.list || []));
	} catch (err) {
		console.error('加载专区商品失败', err);
		uni.showToast({ title: '加载失败', icon: 'none' });
	} finally {
		loading.value = false;
	}
};

const onSearch = () => {
	loadDefaultZoneData();
};

const onProduct = (item) => {
	if (item?.id) {
		sheep.$router.go('/pages/goods/index', { id: item.id });
		return;
	}
	uni.showToast({ title: item.name, icon: 'none' });
};

const onBannerTap = (banner) => {
	const productId = banner?.productId || banner?.productTmplId;
	if (!productId) return;
	sheep.$router.go('/pages/goods/index', { id: productId });
};

// ===== 全国特产 =====
const provinceList = reactive([]);
const specialtyProductList = ref([]);
const currentProvinceId = ref(0);
const currentProvinceName = ref('全部');
const specialtyLoading = ref(false);
const specialtyLoadingMore = ref(false);
const specialtyHasMore = ref(true);
const specialtyPage = ref(1);
const specialtyPageSize = 10;
const refresherTriggered = ref(false);

const normalizeProvince = (item = {}) => {
	const name = item.name || '';
	const code = item.code || '';
	const displayName = name.replace(
		/(省|市|自治区|特别行政区|壮族自治区|回族自治区|维吾尔自治区|内蒙古自治区|西藏自治区|宁夏回族自治区|广西壮族自治区)/g,
		'',
	);
	const short = code || displayName.slice(0, 1);
	return {
		id: item.id,
		name,
		code,
		displayName: displayName || name,
		short: short || name.slice(0, 1),
	};
};

const loadSpecialtyBanners = async () => {
	try {
		const res = await getSpecialtyZoneBannersApi();
		const list = Array.isArray(res?.data?.list) ? res.data.list : [];
		bannerList.splice(0, bannerList.length, ...(list.length ? list : [{ image: '' }]));
	} catch (error) {
		console.warn('加载全国特产轮播失败', error);
		bannerList.splice(0, bannerList.length, { image: '' });
	}
};

const loadSpecialtyProvinces = async () => {
	try {
		const res = await getRegionListApi();
		const list = Array.isArray(res?.data?.list) ? res.data.list : [];
		const filteredList = list.filter((item = {}) => {
			const name = String(item.name || '');
			// 全国特产仅过滤港澳台，自治区（新疆、西藏、内蒙古、广西、宁夏）保留
			return !/香港|澳门|台湾/.test(name);
		});
		const normalizedList = filteredList.map(normalizeProvince);
		const priorityMap = { 江苏: 0, 浙江: 1, 上海: 2 };
		normalizedList.sort((a, b) => {
			const pa = priorityMap[a.displayName];
			const pb = priorityMap[b.displayName];
			const hasPa = typeof pa === 'number';
			const hasPb = typeof pb === 'number';
			if (hasPa && hasPb) return pa - pb;
			if (hasPa) return -1;
			if (hasPb) return 1;
			return 0;
		});
		provinceList.splice(
			0,
			provinceList.length,
			{ id: 0, name: '全部', code: '', displayName: '全部', short: '全' },
			...normalizedList,
		);
	} catch (error) {
		console.error('加载省份失败', error);
	}
};

const loadSpecialtyProducts = async (isRefresh = false) => {
	if (specialtyLoading.value || specialtyLoadingMore.value) return;
	if (!isRefresh && !specialtyHasMore.value) return;
	if (isRefresh) specialtyLoading.value = true;
	else specialtyLoadingMore.value = true;

	try {
		const currentPage = isRefresh ? 1 : specialtyPage.value;
		const res = await getSpecialtyZoneProductsApi({
			province_id: currentProvinceId.value,
			page: currentPage,
			limit: specialtyPageSize,
		});
		const list = Array.isArray(res?.data?.list) ? res.data.list : [];
		const pagination = res?.data?.pagination || { page: currentPage, total_pages: 1 };
		const totalPages = Number(pagination.total_pages || 0);

		if (isRefresh) {
			specialtyProductList.value = list;
			specialtyPage.value = currentPage + 1;
			specialtyHasMore.value = totalPages ? currentPage < totalPages : list.length >= specialtyPageSize;
		} else {
			const seen = new Set(specialtyProductList.value.map((item) => item.id));
			const newItems = list.filter((item) => !seen.has(item.id));
			specialtyProductList.value = [...specialtyProductList.value, ...newItems];
			specialtyPage.value = currentPage + 1;
			const reachLast = totalPages ? currentPage >= totalPages : list.length < specialtyPageSize;
			specialtyHasMore.value = newItems.length > 0 && !reachLast;
		}
	} catch (error) {
		uni.showToast({ title: '加载失败', icon: 'none' });
	} finally {
		if (isRefresh) {
			specialtyLoading.value = false;
			refresherTriggered.value = false;
		} else {
			specialtyLoadingMore.value = false;
		}
	}
};

const refreshSpecialtyProducts = async () => {
	specialtyHasMore.value = true;
	specialtyPage.value = 1;
	await loadSpecialtyProducts(true);
};

const loadMoreSpecialty = () => {
	loadSpecialtyProducts(false);
};

const onSpecialtyRefresh = () => {
	refresherTriggered.value = true;
	refreshSpecialtyProducts();
};

const onProvince = async (item) => {
	if (currentProvinceId.value === item.id) return;
	currentProvinceId.value = item.id;
	currentProvinceName.value = item.displayName || '全部';
	await refreshSpecialtyProducts();
};

const initSpecialty = async () => {
	await loadSpecialtyBanners();
	await loadSpecialtyProvinces();
	await refreshSpecialtyProducts();
};

onLoad((options) => {
	zoneName.value = decodeURIComponent(options?.name || '专区');
	zoneType.value = resolveZoneType(options);

	if (zoneType.value === 'specialty') {
		initSpecialty();
		return;
	}
	if (zoneType.value === 'military') {
		// 军创区：直接进入商品二级列表页展示军创区商品
		uni.redirectTo({ url: '/pages/goods/list?isMilitary=true' });
		return;
	}
	loadDefaultZoneData();
});
</script>

<style lang="scss" scoped>
.page {
	background: #f2f2f2;
	min-height: 100vh;
}

.page-specialty {
	background: #f7f7f7;
	height: 100vh;
	display: flex;
	flex-direction: column;
	overflow: hidden;
	min-height: 0;
}

.banner-swiper {
	width: 100%;
	height: 400rpx;
	flex-shrink: 0;
}

.banner-item,
.banner-img {
	width: 100%;
	height: 100%;
}

.banner-placeholder {
	width: 100%;
	height: 100%;
	background: linear-gradient(135deg, #f5e6d0, #e8d5b8);
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 16rpx;
}

.banner-placeholder-title {
	font-size: 40rpx;
	font-weight: bold;
	color: #6b4c2a;
}

.banner-placeholder-desc {
	font-size: 28rpx;
	color: #8b7355;
}

/* ===== 全国特产 ===== */
.province-section {
	background: #fff;
	padding: 30rpx 0 10rpx;
	flex-shrink: 0;
}

.province-scroll {
	width: 100%;
	margin-bottom: 20rpx;
	white-space: nowrap;
}

.province-list {
	display: flex;
	flex-direction: column;
	flex-wrap: wrap;
	gap: 12rpx;
	max-height: 184rpx;
	padding: 0 20rpx;
}

.province-item {
	padding: 12rpx 16rpx;
	border-radius: 18rpx;
	background: #f8fafc;
	border: 1rpx solid #eef2f7;
	text-align: center;
	min-width: 120rpx;
	min-height: 70rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
	box-shadow: 0 4rpx 10rpx rgba(15, 23, 42, 0.03);
}

.province-item.active {
	background: linear-gradient(135deg, #d62d24, #b81d18);
	border-color: transparent;
	box-shadow: 0 10rpx 22rpx rgba(184, 29, 24, 0.18);
}

.province-item-text {
	font-size: 24rpx;
	color: #64748b;
	line-height: 1.2;
	white-space: pre-line;
	font-weight: 600;
}

.province-item-text-lg {
	font-size: 28rpx;
}

.province-item.active .province-item-text {
	color: #fff;
	font-weight: 700;
}

.selected-header {
	padding: 30rpx 0 20rpx;
	text-align: center;
	flex-shrink: 0;
}

.selected-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.product-scroll {
	flex: 1;
	min-height: 0;
}

.empty-container {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 120rpx 0;
}

.empty-text {
	font-size: 32rpx;
	color: #999;
	margin-top: 24rpx;
	margin-bottom: 16rpx;
}

.empty-desc {
	font-size: 28rpx;
	color: #ccc;
}

/* ===== 通用专区 ===== */
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

.product-grid {
	display: flex;
	flex-wrap: wrap;
	justify-content: space-between;
	padding: 16rpx;
	gap: 16rpx 0;
}

.product-card {
	width: 48%;
	background: #fff;
	border-radius: 16rpx;
	overflow: hidden;
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
	background: #f5f5f5;
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

.product-card-price {
	display: flex;
	align-items: baseline;
	gap: 2rpx;
	margin-top: 12rpx;
}

.price-symbol {
	font-size: 26rpx;
	color: #e65c1c;
	font-weight: bold;
}

.price-value {
	font-size: 44rpx;
	color: #e65c1c;
	font-weight: bold;
}

.load-more {
	padding: 40rpx 0 60rpx;
	text-align: center;
}

.load-more-text {
	font-size: 28rpx;
	color: #ccc;
}
</style>
