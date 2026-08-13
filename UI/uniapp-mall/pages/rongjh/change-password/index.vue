<template>
	<view class="page">
		<CustomNav title="修改密码" background="url(/static/images/nav-bg.png) center / cover no-repeat" color="#fff" />
		<view class="form-card">
			<view class="form-item">
				<text class="form-label">验证码</text>
				<view class="form-input-wrapper" :class="{ focused: codeFocused }">
					<input
						type="number"
						class="form-input"
						v-model="form.code"
						placeholder="请输入验证码"
						maxlength="6"
						@focus="codeFocused = true"
						@blur="codeFocused = false"
					/>
					<text class="code-btn" :class="{ disabled: !canSendCode }" @tap="sendCode">
						{{ codeTimer > 0 ? codeTimer + 's' : '获取验证码' }}
					</text>
				</view>
			</view>
			<view class="form-item">
				<text class="form-label">新密码</text>
				<view class="form-input-wrapper" :class="{ focused: newPwdFocused }">
					<input
						:type="showNewPwd ? 'text' : 'password'"
						class="form-input"
						v-model="form.newPassword"
						placeholder="请输入新密码（6-20位）"
						maxlength="20"
						@focus="newPwdFocused = true"
						@blur="newPwdFocused = false"
					/>
					<uni-icons
						:type="showNewPwd ? 'eye' : 'eye-slash'"
						size="18"
						color="#999"
						@tap="showNewPwd = !showNewPwd"
					></uni-icons>
				</view>
			</view>
			<view class="form-item">
				<text class="form-label">确认密码</text>
				<view class="form-input-wrapper" :class="{ focused: confirmPwdFocused }">
					<input
						:type="showConfirmPwd ? 'text' : 'password'"
						class="form-input"
						v-model="form.confirmPassword"
						placeholder="请再次输入新密码"
						maxlength="20"
						@focus="confirmPwdFocused = true"
						@blur="confirmPwdFocused = false"
					/>
					<uni-icons
						:type="showConfirmPwd ? 'eye' : 'eye-slash'"
						size="18"
						color="#999"
						@tap="showConfirmPwd = !showConfirmPwd"
					></uni-icons>
				</view>
			</view>
		</view>

		<view class="pwd-tips" v-if="form.newPassword">
			<view class="pwd-tip-item" :class="{ pass: form.newPassword.length >= 6 }">
				<uni-icons
					:type="form.newPassword.length >= 6 ? 'checkmarkempty' : 'circle'"
					size="12"
					:color="form.newPassword.length >= 6 ? '#52A052' : '#ccc'"
				></uni-icons>
				<text class="pwd-tip-text">至少6个字符</text>
			</view>
			<view class="pwd-tip-item" :class="{ pass: hasLetterAndNumber }">
				<uni-icons
					:type="hasLetterAndNumber ? 'checkmarkempty' : 'circle'"
					size="12"
					:color="hasLetterAndNumber ? '#52A052' : '#ccc'"
				></uni-icons>
				<text class="pwd-tip-text">包含字母和数字</text>
			</view>
			<view class="pwd-tip-item" :class="{ pass: passwordsMatch }">
				<uni-icons
					:type="passwordsMatch ? 'checkmarkempty' : 'circle'"
					size="12"
					:color="passwordsMatch ? '#52A052' : '#ccc'"
				></uni-icons>
				<text class="pwd-tip-text">两次密码一致</text>
			</view>
		</view>

		<view class="forgot-tip">
			<text class="forgot-tip-text" @tap="goResetPassword">忘记密码？通过手机号重置</text>
		</view>

		<view class="btn-wrapper">
			<view class="submit-btn" :class="{ loading: isLoading }" @tap="onSubmit">
				<text class="submit-btn-text">{{ isLoading ? '提交中...' : '确认修改' }}</text>
			</view>
		</view>

		<!-- 戎集汇页未走 s-layout，需挂载授权弹窗 -->
		<s-auth-modal />
	</view>
</template>

<script setup>
import { ref, reactive, computed, onUnmounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { changePasswordApi } from '@/sheep/api/rongjh/adapter'
import { showAuthModal, getSmsCode } from '@/sheep/hooks/useModal'
import sheep from '@/sheep'

const isLoading = ref(false)
const showNewPwd = ref(false)
const showConfirmPwd = ref(false)
const codeFocused = ref(false)
const newPwdFocused = ref(false)
const confirmPwdFocused = ref(false)
const codeTimer = ref(0)
let codeTimerInterval = null

const form = reactive({
	code: '',
	newPassword: '',
	confirmPassword: '',
})

const canSendCode = computed(() => codeTimer.value <= 0)

const hasLetterAndNumber = computed(
	() => /[a-zA-Z]/.test(form.newPassword) && /\d/.test(form.newPassword),
)

const passwordsMatch = computed(
	() => form.newPassword && form.confirmPassword && form.newPassword === form.confirmPassword,
)

// 发送验证码：场景 3 短信修改密码，以当前登录手机号为准
const sendCode = () => {
	if (!canSendCode.value) return
	const mobile = sheep.$store('user').userInfo.mobile
	if (!mobile) {
		return uni.showToast({ title: '请先登录', icon: 'none' })
	}
	getSmsCode('changePassword', mobile)
	codeTimer.value = 60
	codeTimerInterval = setInterval(() => {
		codeTimer.value -= 1
		if (codeTimer.value <= 0) {
			clearInterval(codeTimerInterval)
			codeTimerInterval = null
		}
	}, 1000)
}

onUnmounted(() => {
	if (codeTimerInterval) {
		clearInterval(codeTimerInterval)
		codeTimerInterval = null
	}
})

const onSubmit = async () => {
	if (!form.code || !/^\d{6}$/.test(form.code)) {
		return uni.showToast({ title: '请输入6位验证码', icon: 'none' })
	}
	if (!form.newPassword || form.newPassword.length < 6) {
		return uni.showToast({ title: '新密码至少6个字符', icon: 'none' })
	}
	if (form.newPassword.length > 20) {
		return uni.showToast({ title: '密码不能超过20个字符', icon: 'none' })
	}
	if (form.newPassword !== form.confirmPassword) {
		return uni.showToast({ title: '两次输入的密码不一致', icon: 'none' })
	}
	if (isLoading.value) return
	isLoading.value = true
	try {
		const res = await changePasswordApi({
			code: form.code,
			password: form.newPassword,
		})
		if (res && res.code === 200) {
			uni.showModal({
				title: '修改成功',
				content: '密码已修改，请重新登录',
				showCancel: false,
				confirmText: '去登录',
				success: async () => {
					await sheep.$store('user').logout()
					uni.reLaunch({ url: '/pages/index/login' })
				},
			})
		} else {
			uni.showToast({ title: res?.msg || '修改失败', icon: 'none' })
		}
	} catch (err) {
		console.error('修改密码失败', err)
		uni.showToast({ title: '修改失败，请重试', icon: 'none' })
	} finally {
		isLoading.value = false
	}
}

const goResetPassword = () => {
	showAuthModal('resetPassword')
}

onLoad(() => {
	if (!sheep.$store('user').isLogin) {
		uni.showToast({ title: '请先登录', icon: 'none' })
		setTimeout(() => uni.navigateBack(), 1500)
	}
})
</script>

<style lang="scss" scoped>
.page {
	min-height: 100vh;
	background: #F5F5F5;
}

.form-card {
	margin: 20rpx 24rpx;
	background: #fff;
	border-radius: 16rpx;
	padding: 0 30rpx;
}

.form-item {
	padding: 28rpx 0;
	border-bottom: 1rpx solid #f5f5f5;
}

.form-item:last-child {
	border-bottom: none;
}

.form-label {
	font-size: 30rpx;
	color: #999;
	margin-bottom: 16rpx;
	display: block;
}

.form-input-wrapper {
	display: flex;
	align-items: center;
	background: #F8F8F8;
	border-radius: 12rpx;
	padding: 0 24rpx;
	border: 2rpx solid transparent;
}

.form-input-wrapper.focused {
	border-color: #B81D18;
	background: #fff;
}

.form-input {
	flex: 1;
	height: 88rpx;
	font-size: 34rpx;
	color: #333;
}

.code-btn {
	font-size: 28rpx;
	color: #B81D18;
	padding-left: 20rpx;
	white-space: nowrap;
}

.code-btn.disabled {
	color: #999;
}

.pwd-tips {
	margin: 0 24rpx;
	padding: 24rpx 30rpx;
	background: #fff;
	border-radius: 16rpx;
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.pwd-tip-item {
	display: flex;
	align-items: center;
	gap: 10rpx;
}

.pwd-tip-text {
	font-size: 28rpx;
	color: #999;
}

.pwd-tip-item.pass .pwd-tip-text {
	color: #52A052;
}

.forgot-tip {
	margin: 24rpx 24rpx 0;
	padding: 0 30rpx;
}

.forgot-tip-text {
	font-size: 30rpx;
	color: #B81D18;
}

.btn-wrapper {
	margin: 48rpx 24rpx 0;
	padding: 0 6rpx;
}

.submit-btn {
	width: 100%;
	height: 96rpx;
	background: linear-gradient(135deg, #D62D24 0%, #B81D18 100%);
	border-radius: 48rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 8rpx 24rpx rgba(184, 29, 24, 0.3);
}

.submit-btn.loading {
	opacity: 0.7;
}

.submit-btn-text {
	font-size: 36rpx;
	color: #fff;
	font-weight: bold;
}
</style>
