<template>
	<view class="page">
		<CustomNav title="身份认证申请" background="url(/static/images/nav-bg.png) center / cover no-repeat" color="#fff" />
		<view class="header">
			<text class="header-title">退役军人及三属人员认证</text>
			<text class="header-desc">请如实填写资料，审核通过后可享专属优惠</text>
		</view>

		<view class="form-card">
			<view class="form-item">
				<text class="form-label">人员类别</text>
				<picker mode="selector" :range="typeOptions" range-key="label" :value="selectedTypeIndex" @change="onTypeChange">
					<view class="picker-value">{{ currentTypeLabel }}</view>
				</picker>
			</view>
			<view class="form-item">
				<text class="form-label">姓名</text>
				<input class="form-input" placeholder="请输入姓名" v-model="applyForm.name" />
			</view>
			<view class="form-item">
				<text class="form-label">联系电话</text>
				<input class="form-input" type="number" maxlength="11" placeholder="请输入11位手机号" v-model="applyForm.phone" />
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
				<input class="form-input" placeholder="请输入城市" v-model="applyForm.city" />
			</view>
			<template v-if="isSelfType">
				<view class="form-item">
					<text class="form-label">服役单位</text>
					<input class="form-input" placeholder="请输入服役单位" v-model="applyForm.service_unit" />
				</view>
				<view class="form-item">
					<text class="form-label">服役年份</text>
					<input class="form-input" placeholder="如 2010 或 2008-2012" v-model="applyForm.service_year" />
				</view>
			</template>
			<view class="form-item">
				<text class="form-label">{{ isSelfType ? '入会说明' : '情况说明' }}</text>
				<textarea
					class="form-textarea"
					:placeholder="isSelfType ? '可补充服役经历、入会理由' : '请说明与军人关系及认证情况'"
					v-model="applyForm.description"
				/>
			</view>
		</view>

		<view class="form-actions">
			<view class="primary-btn" :class="{ disabled: submitting }" @tap="onSubmitApply">
				{{ submitting ? '提交中...' : '提交认证' }}
			</view>
			<view class="ghost-btn" @tap="onResetApply">重置</view>
		</view>

		<view class="page-space"></view>
	</view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import sheep from '@/sheep'
import { showAuthModal } from '@/sheep/hooks/useModal'
import {
	getComradeStatesApi,
	getIdentityCertStatusApi,
	submitIdentityCertApi,
} from '@/sheep/api/rongjh/adapter'

const typeOptions = [
	{ value: 'SELF', label: '退役军人（本人）' },
	{ value: 'MARTYR', label: '烈士遗属' },
	{ value: 'SACRIFICE', label: '因公牺牲军人遗属' },
	{ value: 'ILLNESS', label: '病故军人遗属' },
	{ value: 'FAMILY', label: '军人家属' },
]

const stateOptions = ref([])
const submitting = ref(false)
const applyForm = reactive({
	type: 'SELF',
	name: '',
	phone: '',
	state_id: null,
	state_name: '',
	city: '',
	service_unit: '',
	service_year: '',
	description: '',
})

const isSelfType = computed(() => applyForm.type === 'SELF')
const selectedTypeIndex = computed(() => {
	const index = typeOptions.findIndex((item) => item.value === applyForm.type)
	return index >= 0 ? index : 0
})
const currentTypeLabel = computed(() => typeOptions[selectedTypeIndex.value]?.label || '请选择')
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
		const res = await getIdentityCertStatusApi()
		const data = res?.data || {}
		if (data.is_member || data.state === 'approved') {
			uni.showModal({
				title: '已通过认证',
				content: '您已通过军人及三属人员认证，无需重复提交。',
				showCancel: false,
				success: () => uni.navigateBack(),
			})
			return
		}
		if (data.has_application) {
			fillFormByStatus(data)
			if (data.state === 'pending') {
				uni.showModal({
					title: '提示',
					content: '您已有认证申请正在审核中，请耐心等待。',
					showCancel: false,
					success: () => uni.navigateBack(),
				})
			}
		}
	} catch (error) {
		console.error('获取认证状态失败', error)
	}
}

const onTypeChange = (event) => {
	const index = Number(event?.detail?.value || 0)
	const selected = typeOptions[index]
	if (!selected) return
	applyForm.type = selected.value
	if (!isSelfType.value) {
		applyForm.service_unit = ''
		applyForm.service_year = ''
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
	if (!/^1\d{10}$/.test(String(applyForm.phone || '').trim())) return '请输入正确的手机号'
	if (!applyForm.state_id) return '请选择所在省份'
	if (!applyForm.city.trim()) return '请输入所在城市'
	if (isSelfType.value) {
		if (!applyForm.service_unit.trim()) return '请输入服役单位'
		if (!applyForm.service_year.trim()) return '请输入服役年份'
	}
	return ''
}

const onSubmitApply = async () => {
	if (!sheep.$store('user').isLogin) {
		showAuthModal('accountLogin')
		return
	}
	if (submitting.value) return
	const errorMsg = validateForm()
	if (errorMsg) {
		uni.showToast({ title: errorMsg, icon: 'none' })
		return
	}
	submitting.value = true
	try {
		await submitIdentityCertApi({
			type: applyForm.type,
			name: applyForm.name.trim(),
			phone: applyForm.phone.trim(),
			state_id: Number(applyForm.state_id),
			state_name: applyForm.state_name,
			city: applyForm.city.trim(),
			service_unit: applyForm.service_unit.trim(),
			service_year: applyForm.service_year.trim(),
			description: applyForm.description.trim(),
		})
		uni.showModal({
			title: '提交成功',
			content: '您的认证申请已提交，请等待管理员审核。审核结果可在「我的」页面查看。',
			showCancel: false,
			confirmText: '我知道了',
			success: () => uni.navigateBack(),
		})
	} catch (err) {
		uni.showToast({ title: err?.message || '提交失败', icon: 'none' })
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

onLoad(async (query) => {
	const type = String(query?.type || '').toUpperCase()
	if (typeOptions.some((item) => item.value === type)) {
		applyForm.type = type
	}
	if (!sheep.$store('user').isLogin) {
		showAuthModal('accountLogin')
	}
	await fetchStates()
	await fetchApplyStatus()
})
</script>

<style lang="scss" scoped>
.page {
	min-height: 100vh;
	background: #f7f7f7;
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
	background: #f7f7f7;
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
	background: linear-gradient(135deg, #d62d24, #b81d18);
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
	border: 1rpx solid #e4b6b6;
	color: #b81d18;
	font-size: 30rpx;
	font-weight: 600;
	background: #fff;
}

.page-space {
	height: 40rpx;
}
</style>
