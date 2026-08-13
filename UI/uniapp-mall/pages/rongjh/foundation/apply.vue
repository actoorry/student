<template>
	<view class="page">
		<CustomNav title="帮扶求助申请" background="url(/static/images/nav-bg.png) center / cover no-repeat"  color="#fff" />
		<view class="header">
			<text class="header-title">帮扶求助资料申请</text>
			<text class="header-desc">填写完整资料，工作人员将尽快联系您</text>
		</view>

		<view class="form-card">
			<view class="form-item">
				<text class="form-label">申请对象</text>
				<picker mode="selector" :range="applyTargets" @change="onTargetChange">
					<view class="form-picker">{{ applyForm.target }}</view>
				</picker>
			</view>
			<view class="form-item">
				<text class="form-label">姓名</text>
				<input class="form-input" placeholder="请输入姓名" v-model="applyForm.name" />
			</view>
			<view class="form-item">
				<text class="form-label">联系电话</text>
				<input
					class="form-input"
					type="number"
					maxlength="11"
					placeholder="请输入11位手机号"
					v-model="applyForm.phone"
				/>
			</view>
			<view class="form-item">
				<text class="form-label">所在省份</text>
				<picker mode="selector" :range="states" range-key="name" @change="onProvinceChange">
					<view class="form-picker">{{ applyForm.provinceName || '请选择省份' }}</view>
				</picker>
			</view>
			<view class="form-item">
				<text class="form-label">所在地区</text>
				<input class="form-input" placeholder="请输入地区" v-model="applyForm.area" />
			</view>
			<view class="form-item">
				<text class="form-label">求助说明</text>
				<textarea class="form-textarea" placeholder="请简要描述求助情况" v-model="applyForm.reason"></textarea>
			</view>
			<view class="form-item">
				<text class="form-label">相关资料</text>
				<view class="attachment-grid" v-if="attachments.length > 0">
					<view class="attachment-thumb" v-for="(file, idx) in attachments" :key="idx">
						<image
							class="thumb-image"
							:src="file.preview || file.url"
							mode="aspectFill"
							@tap="onPreviewAttachment(idx)"
						></image>
						<view class="thumb-remove" @tap.stop="removeAttachment(idx)">
							<uni-icons type="closeempty" size="14" color="#fff"></uni-icons>
						</view>
					</view>
				</view>
				<view class="upload-box" @tap="onUploadTap" v-if="attachments.length < 9">
					<uni-icons type="cloud-upload" size="20" color="#B81D18"></uni-icons>
					<text class="upload-text">上传证明材料（{{ attachments.length }}/9）</text>
				</view>
			</view>
		</view>

		<view class="form-actions">
			<view class="primary-btn" @tap="onSubmitApply">提交申请</view>
			<view class="ghost-btn" @tap="onResetApply">重置</view>
		</view>

		<view class="page-space"></view>
	</view>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import sheep from '@/sheep'
import { showAuthModal } from '@/sheep/hooks/useModal'
import { getComradeStatesApi, submitFoundationAssistanceApi, resolveRongjhMediaUrl } from '@/sheep/api/rongjh/adapter'
import RONGJH_ROUTES from '@/sheep/helper/rongjh-routes'
import { chooseAndUploadFile } from '@/sheep/components/s-uploader/choose-and-upload-file'

const applyTargets = ['本人', '家属', '战友', '其他']
const states = ref([]) // 省份列表
const attachments = ref([]) // 附件列表
const isSubmitting = ref(false)

const applyForm = reactive({
	target: '本人',
	name: '',
	phone: '',
	provinceId: null,
	provinceName: '',
	area: '',
	reason: '',
})

// 加载省份列表（与战友会申请一致：公开模型接口返回 data.list）
const loadStates = async () => {
	try {
		const res = await getComradeStatesApi()
		let list = Array.isArray(res?.data?.list) ? res.data.list : []
		if (!list.length && Array.isArray(res?.data?.states)) {
			list = res.data.states
		}
		if (!list.length && Array.isArray(res?.data)) {
			list = res.data
		}
		states.value = list.filter((item = {}) => {
			const name = String(item.name || '')
			return !/香港|澳门|台湾/.test(name)
		})
	} catch (err) {
		console.error('加载省份失败', err)
		states.value = []
		uni.showToast({ title: '省份列表加载失败', icon: 'none' })
	}
}

const onTargetChange = (e) => {
	const index = e?.detail?.value || 0
	applyForm.target = applyTargets[index]
}

const onProvinceChange = (e) => {
	const index = e?.detail?.value || 0
	const province = states.value[index]
	if (province) {
		applyForm.provinceId = province.id
		applyForm.provinceName = province.name
	}
}

// 上传图片（走文件服务，与商城其他上传一致）
const onUploadTap = async () => {
	if (!sheep.$store('user').isLogin) {
		showAuthModal('accountLogin')
		return
	}
	const remain = 9 - attachments.value.length
	if (remain <= 0) return
	try {
		const files = await chooseAndUploadFile({
			type: 'image',
			count: remain,
			sizeType: ['compressed'],
			sourceType: ['album', 'camera'],
			directory: 'rongjh/help',
		})
		if (!files?.length) return
		const uploaded = files
			.filter((file) => file?.url)
			.map((file) => {
				const url = resolveRongjhMediaUrl(file.url)
				return {
					filename: file.name || 'image.jpg',
					url,
					preview: url,
					file_type: 'image',
				}
			})
		if (!uploaded.length) {
			uni.showToast({ title: '上传失败', icon: 'none' })
			return
		}
		attachments.value.push(...uploaded)
		uni.showToast({ title: '上传成功', icon: 'success' })
	} catch (err) {
		const msg = String(err?.errMsg || '')
		if (msg && !/cancel/i.test(msg)) {
			uni.showToast({ title: '上传失败', icon: 'none' })
		}
	}
}

const onPreviewAttachment = (index) => {
	const urls = attachments.value.map((item) => item.preview || item.url).filter(Boolean)
	if (!urls.length) return
	uni.previewImage({
		current: urls[index] || urls[0],
		urls,
	})
}

// 删除附件
const removeAttachment = (index) => {
	attachments.value.splice(index, 1)
}

const validateForm = () => {
	if (!applyForm.name.trim()) return '请输入姓名'
	if (!/^1\d{10}$/.test(String(applyForm.phone || '').trim())) return '请输入正确的手机号'
	if (!applyForm.reason.trim()) return '请输入求助说明'
	return ''
}

// 提交申请
const onSubmitApply = async () => {
	if (!sheep.$store('user').isLogin) {
		showAuthModal('accountLogin')
		return
	}
	const errorMsg = validateForm()
	if (errorMsg) {
		uni.showToast({ title: errorMsg, icon: 'none' })
		return
	}

	isSubmitting.value = true
	uni.showLoading({ title: '提交中...' })

	try {
		const res = await submitFoundationAssistanceApi({
			name: applyForm.name.trim(),
			phone: applyForm.phone.trim(),
			province_id: applyForm.provinceId,
			city: applyForm.area,
			description: applyForm.reason,
			attachments: attachments.value.map(({ filename, url, file_type }) => ({
				filename,
				url,
				file_type,
			})),
		})

		uni.hideLoading()
		if (res?.code === 200) {
			uni.showModal({
				title: '申请已提交',
				content: '平台将在3个工作日内与您联系，请保持电话畅通。',
				cancelText: '返回首页',
				confirmText: '我的申请',
				success: (modalRes) => {
					if (modalRes.confirm) {
						uni.redirectTo({ url: RONGJH_ROUTES.foundationMyApplications })
					} else {
						uni.redirectTo({ url: RONGJH_ROUTES.foundationIndex })
					}
				},
			})
		} else {
			uni.showToast({ title: res?.msg || '提交失败', icon: 'none' })
		}
	} catch (err) {
		uni.hideLoading()
		uni.showToast({ title: '提交失败，请重试', icon: 'none' })
		console.error('提交申请失败', err)
	} finally {
		isSubmitting.value = false
	}
}

const onResetApply = () => {
	applyForm.target = '本人'
	applyForm.name = ''
	applyForm.phone = ''
	applyForm.provinceId = null
	applyForm.provinceName = ''
	applyForm.area = ''
	applyForm.reason = ''
	attachments.value = []
}

onMounted(() => {
	loadStates()
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
.form-picker {
	background: #F7F7F7;
	border-radius: 16rpx;
	padding: 20rpx;
	font-size: 28rpx;
	color: #333;
	height: 80rpx;
}

.form-textarea {
	height: auto;
	min-height: 160rpx;
}

.upload-box {
	height: 88rpx;
	border-radius: 12rpx;
	border: 1rpx dashed #e4b6b6;
	background: #fff7f7;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 10rpx;
}

.upload-text {
	font-size: 26rpx;
	color: #b81d18;
}

.attachment-grid {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 16rpx;
	margin-bottom: 16rpx;
}

.attachment-thumb {
	position: relative;
	aspect-ratio: 1;
	border-radius: 12rpx;
	overflow: hidden;
	background: #f5f5f5;
}

.thumb-image {
	width: 100%;
	height: 100%;
}

.thumb-remove {
	position: absolute;
	top: 8rpx;
	right: 8rpx;
	width: 36rpx;
	height: 36rpx;
	border-radius: 50%;
	background: rgba(0, 0, 0, 0.45);
	display: flex;
	align-items: center;
	justify-content: center;
}

.attachment-list {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
	margin-bottom: 16rpx;
}

.attachment-item {
	display: flex;
	align-items: center;
	gap: 12rpx;
	padding: 16rpx;
	background: #F7F7F7;
	border-radius: 12rpx;
}

.attachment-name {
	flex: 1;
	font-size: 26rpx;
	color: #333;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
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
