<template>
	<view class="product-grid-wrap">
		<view v-if="products.length > 0" class="product-grid" :style="gridStyle">
			<view
				v-for="(item, index) in products"
				:key="item.id || index"
				class="product-card"
				:class="[`product-card--cols-${safeColumns}`]"
				:style="cardStyle"
				@tap="onItemTap(item)"
			>
				<view class="product-card-img-wrap" :class="`product-card-img-wrap--cols-${safeColumns}`">
					<image v-if="item.image" class="product-card-img" :src="item.image" mode="aspectFill"></image>
					<view v-else class="product-img-empty">
						<text class="product-img-empty-text">暂无图片</text>
					</view>
				</view>
				<view class="product-card-info">
					<text class="product-card-name">{{ item.name }}</text>
					<text v-if="showDescription && item.description" class="product-card-desc">{{ item.description }}</text>
					<view class="product-card-bottom">
						<view class="product-card-price">
							<text class="price-symbol">¥</text>
							<text class="price-value">{{ item.price }}</text>
						</view>
						<text v-if="showSales" class="product-card-sales">已售{{ item.sales || item.sold_qty || 0 }}+</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
	products: {
		type: Array,
		default: () => [],
	},
	columns: {
		type: Number,
		default: 2,
	},
	gap: {
		type: [Number, String],
		default: 16,
	},
	showDescription: {
		type: Boolean,
		default: true,
	},
	showSales: {
		type: Boolean,
		default: true,
	},
});

const emit = defineEmits(['itemTap']);

const safeColumns = computed(() => Math.max(1, Number(props.columns) || 2));

const gapValue = computed(() =>
	typeof props.gap === 'number' ? props.gap : Number(String(props.gap).replace('rpx', '')) || 16,
);

const gridStyle = computed(() => ({
	'--columns': String(safeColumns.value),
	'--item-gap': `${gapValue.value}rpx`,
}));

const cardStyle = computed(() => ({
	width: `calc((100% - (var(--columns) - 1) * var(--item-gap)) / var(--columns))`,
	flex: `0 0 calc((100% - (var(--columns) - 1) * var(--item-gap)) / var(--columns))`,
	maxWidth: `calc((100% - (var(--columns) - 1) * var(--item-gap)) / var(--columns))`,
}));

const onItemTap = (item) => {
	emit('itemTap', item);
};
</script>

<style lang="scss" scoped>
.product-grid-wrap {
	width: 100%;
}

.product-grid {
	display: flex;
	flex-wrap: wrap;
	gap: var(--item-gap);
	align-items: stretch;
	padding: 0 16rpx;
}

.product-card {
	background: #fff;
	border-radius: 16rpx;
	overflow: hidden;
	box-shadow: 0 10rpx 28rpx rgba(0, 0, 0, 0.04);
	display: flex;
	flex-direction: column;
	border: 1rpx solid rgba(0, 0, 0, 0.03);
	box-sizing: border-box;
}

.product-card--cols-2 .product-card-img-wrap,
.product-card--cols-3 .product-card-img-wrap {
	aspect-ratio: 1 / 1;
	height: auto;
	min-height: 0;
}

@supports not (aspect-ratio: 1 / 1) {
	.product-card--cols-2 .product-card-img-wrap,
	.product-card--cols-3 .product-card-img-wrap {
		height: 0;
		padding-top: 100%;
	}

	.product-card--cols-2 .product-card-img-wrap .product-card-img,
	.product-card--cols-3 .product-card-img-wrap .product-card-img,
	.product-card--cols-2 .product-card-img-wrap .product-img-empty,
	.product-card--cols-3 .product-card-img-wrap .product-img-empty {
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
	}
}

.product-card-img-wrap {
	width: 100%;
	position: relative;
	background: #f5f5f5;
	overflow: hidden;
}

.product-card-img {
	display: block;
	width: 100%;
	height: 100%;
	object-fit: cover;
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
	font-size: 26rpx;
	color: #ccc;
}

.product-card-info {
	padding: 16rpx 20rpx 20rpx;
	display: flex;
	flex-direction: column;
	flex: 1;
}

.product-card-name {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
	line-height: 1.4;
	overflow: hidden;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	word-break: break-all;
}

.product-card-desc {
	font-size: 24rpx;
	color: #999;
	line-height: 1.4;
	margin-top: 8rpx;
	display: -webkit-box;
	-webkit-line-clamp: 1;
	-webkit-box-orient: vertical;
	overflow: hidden;
}

.product-card-bottom {
	display: flex;
	align-items: center;
	justify-content: flex-start;
	margin-top: auto;
	padding-top: 16rpx;
}

.product-card-price {
	display: flex;
	align-items: baseline;
	gap: 2rpx;
	flex-shrink: 0;
}

.price-symbol {
	font-size: 26rpx;
	color: #e02e24;
	font-weight: bold;
}

.price-value {
	font-size: 40rpx;
	color: #e02e24;
	font-weight: bold;
}

.product-card-sales {
	font-size: 24rpx;
	color: #999;
	margin-left: auto;
	padding-left: 8rpx;
	white-space: nowrap;
}
</style>
