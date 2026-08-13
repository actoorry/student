<template>
	<view class="page">
		<CustomNav title="戎集汇调试" background="url(/static/images/nav-bg.png) center / cover no-repeat" color="#fff" />
		<view class="banner">
			<text class="banner-title">戎集汇页面调试入口</text>
			<text class="banner-desc">套壳阶段 QA 用，点击下方链接逐页验收</text>
			<view class="mock-tag" :class="{ off: !mockEnabled }">
				<text>Mock：{{ mockEnabled ? '开启' : '关闭' }}</text>
			</view>
		</view>

		<view class="group" v-for="(group, gIndex) in linkGroups" :key="gIndex">
			<text class="group-title">{{ group.name }}</text>
			<view class="link-list">
				<view
					class="link-item"
					v-for="(item, index) in group.links"
					:key="index"
					@tap="onNavigate(item.path)"
				>
					<view class="link-main">
						<text class="link-label">{{ item.label }}</text>
						<text class="link-path">{{ item.path }}</text>
					</view>
					<uni-icons type="right" size="14" color="#ccc"></uni-icons>
				</view>
			</view>
		</view>

		<view class="footer">
			<text class="footer-text">共 {{ totalLinks }} 个入口 · Phase C/E/F 调试页</text>
		</view>
	</view>
</template>

<script setup>
import { computed } from 'vue'
import { RONGJH_USE_MOCK } from '@/sheep/config/rongjh'
import RONGJH_ROUTES from '@/sheep/helper/rongjh-routes'

const mockEnabled = RONGJH_USE_MOCK

/** Mock 样例 ID，与 sheep/mock/rongjh/* 中数据一致 */
const MOCK = {
	warriorArticleId: 101,
	foundationRecordId: 201,
	noticeId: 301,
}

const linkGroups = [
	{
		name: '身份认证',
		links: [
			{ label: '身份认证申请', path: RONGJH_ROUTES.identityApply },
			{ label: '身份认证 · 烈士遗属', path: `${RONGJH_ROUTES.identityApply}?type=MARTYR` },
		],
	},
	{
		name: '战友会',
		links: [
			{ label: '战友会首页', path: RONGJH_ROUTES.warriorIndex },
			{ label: '入会申请', path: RONGJH_ROUTES.warriorApply },
			{ label: '动态详情（Mock #101）', path: RONGJH_ROUTES.warriorDetail(MOCK.warriorArticleId) },
		],
	},
	{
		name: '爱心帮扶',
		links: [
			{ label: '爱心帮扶首页', path: RONGJH_ROUTES.foundationIndex },
			{ label: '帮扶申请', path: RONGJH_ROUTES.foundationApply },
			{ label: '我的申请', path: RONGJH_ROUTES.foundationMyApplications },
			{ label: '帮扶详情（Mock #201）', path: RONGJH_ROUTES.foundationDetail(MOCK.foundationRecordId) },
		],
	},
	{
		name: '专区',
		links: [
			{ label: '专区（默认）', path: RONGJH_ROUTES.zoneIndex },
			{ label: '专区 · 礼品专区', path: RONGJH_ROUTES.zoneNamed('礼品专区') },
			{ label: '专区 · 爱心基金', path: RONGJH_ROUTES.zoneNamed('爱心基金') },
			{ label: '全国特产（Phase F1）', path: RONGJH_ROUTES.zoneSpecialty },
			{ label: '军创区', path: RONGJH_ROUTES.zoneMilitary() },
		],
	},
	{
		name: '公告',
		links: [
			{ label: '戎公告列表', path: RONGJH_ROUTES.noticeIndex },
			{ label: '公告详情（Mock #301）', path: RONGJH_ROUTES.noticeDetail(MOCK.noticeId, '平台服务升级公告') },
		],
	},
	{
		name: '静态页',
		links: [
			{ label: '关于我们', path: RONGJH_ROUTES.aboutIndex },
			{ label: '用户协议', path: RONGJH_ROUTES.protocolIndex('user') },
			{ label: '隐私政策', path: RONGJH_ROUTES.protocolIndex('privacy') },
		],
	},
	{
		name: 'Phase E 扩展',
		links: [
			{ label: '联系我们', path: RONGJH_ROUTES.serviceIndex },
			{ label: '修改密码', path: RONGJH_ROUTES.changePasswordIndex },
			{ label: '同城服务', path: RONGJH_ROUTES.localServiceIndex },
			{ label: '同城服务（带参）', path: RONGJH_ROUTES.localServiceNamed('同城服务') },
		],
	},
]

const totalLinks = computed(() =>
	linkGroups.reduce((sum, group) => sum + group.links.length, 0),
)

const onNavigate = (url) => {
	if (!url) return
	uni.navigateTo({ url })
}
</script>

<style lang="scss" scoped>
.page {
	min-height: 100vh;
	background: #f5f5f5;
	padding-bottom: 40rpx;
}

.banner {
	margin: 20rpx 24rpx 0;
	padding: 32rpx 28rpx;
	background: linear-gradient(135deg, #b81d18, #d4382a);
	border-radius: 16rpx;
	color: #fff;
}

.banner-title {
	display: block;
	font-size: 34rpx;
	font-weight: bold;
	margin-bottom: 12rpx;
}

.banner-desc {
	display: block;
	font-size: 26rpx;
	opacity: 0.9;
	line-height: 1.5;
}

.mock-tag {
	display: inline-flex;
	margin-top: 20rpx;
	padding: 8rpx 20rpx;
	background: rgba(255, 255, 255, 0.2);
	border-radius: 100rpx;
	font-size: 24rpx;
}

.mock-tag.off {
	background: rgba(0, 0, 0, 0.2);
}

.group {
	margin: 24rpx 24rpx 0;
}

.group-title {
	display: block;
	font-size: 28rpx;
	color: #999;
	margin-bottom: 12rpx;
	padding-left: 8rpx;
}

.link-list {
	background: #fff;
	border-radius: 16rpx;
	overflow: hidden;
}

.link-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 28rpx 24rpx;
	border-bottom: 1rpx solid #f5f5f5;
}

.link-item:last-child {
	border-bottom: none;
}

.link-main {
	flex: 1;
	min-width: 0;
	padding-right: 16rpx;
}

.link-label {
	display: block;
	font-size: 30rpx;
	color: #333;
	margin-bottom: 8rpx;
}

.link-path {
	display: block;
	font-size: 22rpx;
	color: #999;
	word-break: break-all;
	line-height: 1.4;
}

.footer {
	padding: 48rpx 0 32rpx;
	text-align: center;
}

.footer-text {
	font-size: 24rpx;
	color: #ccc;
}
</style>
