<template>
	<view class="page">
		<CustomNav title="战友会入会申请" background="url(/static/images/nav-bg.png) center / cover no-repeat"  color="#fff" />
		<view class="header">
			<text class="header-title">战友会入会申请</text>
			<text class="header-desc">请如实填写资料，提交后等待管理员审核</text>
		</view>

		<view class="form-card">
			<view class="form-item">
				<text class="form-label">姓名</text>
				<input class="form-input" placeholder="请输入姓名" v-model="applyForm.name" />
			</view>
			<view class="form-item">
				<text class="form-label">联系电话</text>
				<input class="form-input" type="number" maxlength="11" placeholder="请输入手机号" v-model="applyForm.phone" />
			</view>
			<view class="form-item">
				<text class="form-label">所在省份</text>
				<picker class="picker-wrap" mode="selector" :range="stateOptions" range-key="name" :value="selectedStateIndex" @change="onStateChange">
					<view class="picker-value" :class="{ placeholder: !applyForm.state_name }">
						{{ applyForm.state_name || '请选择省份' }}
					</view>
				</picker>
			</view>
			<view class="form-item">
				<text class="form-label">所在城市</text>
				<input class="form-input" placeholder="请输入城市，如合肥市" v-model="applyForm.city" />
			</view>
			<view class="form-item">
				<text class="form-label">服役单位</text>
				<input class="form-input" placeholder="请输入服役单位" v-model="applyForm.service_unit" />
			</view>
			<view class="form-item">
				<text class="form-label">服役年份</text>
				<input class="form-input" placeholder="如 2010 或 2008-2012" v-model="applyForm.service_year" />
			</view>
			<view class="form-item">
				<text class="form-label">入会说明</text>
				<textarea class="form-textarea" placeholder="可补充服役经历、入会理由" v-model="applyForm.description" />
			</view>
		</view>

		<view class="form-actions">
			<view class="primary-btn" :class="{ disabled: submitting }" @tap="onSubmitApply">{{ submitting ? '提交中...' : '提交申请' }}</view>
			<view class="ghost-btn" @tap="onResetApply">重置</view>
		</view>

		<view class="page-space"></view>
	</view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
	getComradeStatesApi,
	getComradeApplyStatusApi,
	submitComradeApplyApi,
} from '@/sheep/api/rongjh/adapter'

const stateOptions = ref([])
const submitting = ref(false)

const applyForm = reactive({
	name: '',
	phone: '',
	state_id: null,
	state_name: '',
	city: '',
	service_unit: '',
	service_year: '',
	description: '',
})

const selectedStateIndex = computed(() => {
	if (!stateOptions.value.length) return 0
	const index = stateOptions.value.findIndex((item) => item.id === applyForm.state_id)
	return index >= 0 ? index : 0
})

const fillFormByStatus = (data = {}) => {
	applyForm.name = data.name || ''
	applyForm.phone = data.phone || ''
	applyForm.state_id = data.state_id || null
	applyForm.state_name = data.state_name || ''
	applyForm.city = data.city || ''
	applyForm.service_unit = data.service_unit || ''
	applyForm.service_year = data.service_year || ''
	applyForm.description = data.description || ''
}

const fetchStates = async () => {
	try {
		const res = await getComradeStatesApi()
		// 与战友会首页、商家入驻一致：公开模型接口返回 data.list
		let states = Array.isArray(res?.data?.list) ? res.data.list : []
		if (!states.length && Array.isArray(res?.data)) {
			states = res.data
		}
		stateOptions.value = states.filter((item = {}) => {
			const name = String(item.name || '')
			return !/香港|澳门|台湾/.test(name)
		})
	} catch (error) {
		stateOptions.value = []
		uni.showToast({ title: '省份列表加载失败', icon: 'none' })
	}
}

const fetchApplyStatus = async () => {
	try {
		const res = await getComradeApplyStatusApi()
		const data = res?.data || {}
		if (data.has_application) {
			fillFormByStatus(data)
			if (data.state && data.state !== 'rejected') {
				uni.showModal({
					title: '提示',
					content: `您当前已有${data.state_display || '入会'}申请记录，请在列表页查看审核状态。`,
					showCancel: false,
					success: () => {
						uni.navigateBack()
					}
				})
			}
		}
	} catch (error) {
		console.error('获取申请状态失败', error)
	}
}

const onStateChange = (event) => {
	const index = Number(event?.detail?.value || 0)
	const selected = stateOptions.value[index]
	if (!selected) return
	applyForm.state_id = selected.id
	applyForm.state_name = selected.name
}

const validateForm = () => {
	if (!applyForm.name.trim()) return '请输入姓名'
	if (!/^1\d{10}$/.test(applyForm.phone)) return '请输入正确的手机号'
	if (!applyForm.state_id) return '请选择所在省份'
	if (!applyForm.city.trim()) return '请输入所在城市'
	if (!applyForm.service_unit.trim()) return '请输入服役单位'
	if (!applyForm.service_year.trim()) return '请输入服役年份'
	return ''
}

const onSubmitApply = async () => {
	if (submitting.value) return
	const errorMsg = validateForm()
	if (errorMsg) {
		uni.showToast({ title: errorMsg, icon: 'none' })
		return
	}
	submitting.value = true
	try {
		await submitComradeApplyApi({
			name: applyForm.name.trim(),
			phone: applyForm.phone.trim(),
			state_id: Number(applyForm.state_id),
			city: applyForm.city.trim(),
			service_unit: applyForm.service_unit.trim(),
			service_year: applyForm.service_year.trim(),
			description: applyForm.description.trim(),
		})
		uni.showModal({
			title: '提交成功',
			content: '您的入会申请已提交，请等待管理员审核。审核结果可在战友会首页查看。',
			showCancel: false,
			confirmText: '我知道了',
			success: () => {
				uni.navigateBack()
			}
		})
	} finally {
		submitting.value = false
	}
}

const onResetApply = () => {
	applyForm.name = ''
	applyForm.phone = ''
	applyForm.state_id = null
	applyForm.state_name = ''
	applyForm.city = ''
	applyForm.service_unit = ''
	applyForm.service_year = ''
	applyForm.description = ''
}

onLoad(async () => {
	await fetchStates()
	await fetchApplyStatus()
})
</script>

<style lang="scss" scoped>
.page {
	min-height: 100vh;
	background: #F7F7F7;
}

.header {
	padding: 40rpx 28rpx 20rpx;
}

.header-title {
	font-size: 36rpx;
	font-weight: 700;
	color: #333;
}

.header-desc {
	margin-top: 10rpx;
	font-size: 26rpx;
	color: #999;
}

.form-card {
	margin: 0 24rpx;
	background: #fff;
	border-radius: 20rpx;
	padding: 28rpx;
	box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.04);
}

.form-item {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
	margin-bottom: 20rpx;
}

.form-label {
	font-size: 26rpx;
	color: #666;
}

.form-input,
.form-textarea,
.picker-value {
	background: #F7F7F7;
	border-radius: 16rpx;
	padding: 20rpx;
	font-size: 28rpx;
	color: #333;
	min-height: 80rpx;
	box-sizing: border-box;
}

.picker-wrap {
	width: 100%;
}

.picker-value {
	display: flex;
	align-items: center;
}

.picker-value.placeholder {
	color: #999;
}

.form-textarea {
	height: 160rpx;
	min-height: 160rpx;
	max-height: 240rpx;
	width: 100%;
	box-sizing: border-box;
}

.form-actions {
	margin: 24rpx 24rpx 0;
	display: flex;
	gap: 16rpx;
}

.primary-btn {
	flex: 1;
	text-align: center;
	padding: 20rpx 0;
	background: linear-gradient(135deg, #D62D24, #B81D18);
	color: #fff;
	border-radius: 40rpx;
	font-size: 30rpx;
	font-weight: 600;
}

.primary-btn.disabled {
	opacity: 0.7;
}

.ghost-btn {
	flex: 1;
	text-align: center;
	padding: 20rpx 0;
	border-radius: 40rpx;
	border: 1rpx solid #E4B6B6;
	color: #B81D18;
	font-size: 30rpx;
	font-weight: 600;
	background: #fff;
}

.page-space {
	height: 40rpx;
}
</style>
