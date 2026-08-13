<!--
  互动按钮：分享、打招呼、关注 三个固定动作。
  只消费当前人物推荐作用域的只读上下文；不自行加载人物、
  不修改队列，且模板属性不能改变动作、目标或路由。
-->
<template>
  <view
    v-if="hasValidContext"
    class="p-fib"
    :class="{ 'p-fib--hidden': isHidden }"
    :style="safeAreaStyle"
  >
    <!-- 分享按钮 -->
    <view
      class="p-fib__btn p-fib__btn--share"
      :aria-label="shareLabel"
      @click="handleShareClick"
    >
      <!-- #ifdef MP-WEIXIN -->
      <image
        v-if="shareIconSrc"
        class="p-fib__icon-img"
        :src="shareIconSrc"
        mode="aspectFit"
        @error="handleShareImageError"
      />
      <text v-else class="p-fib__icon-text p-fib__icon-text--share">S</text>
      <!-- 原生分享按钮只负责点击；透明层不能包住可见图标。 -->
      <button
        v-if="canShare"
        class="p-fib__native-share-btn"
        open-type="share"
        @click.stop="prepareProfileShare"
      ></button>
      <!-- #endif -->
      <!-- #ifndef MP-WEIXIN -->
      <image
        v-if="shareIconSrc"
        class="p-fib__icon-img"
        :src="shareIconSrc"
        mode="aspectFit"
        @error="handleShareImageError"
      />
      <text v-else class="p-fib__icon-text p-fib__icon-text--share">S</text>
      <!-- #endif -->
    </view>

    <!-- 打招呼按钮 -->
    <view
      class="p-fib__btn p-fib__btn--greeting"
      :class="{ 'p-fib__btn--pending': greetingPending }"
      :aria-label="greetingLabel"
      @click="handleGreetingClick"
    >
      <image
        v-if="greetingIconSrc"
        class="p-fib__icon-img"
        :src="greetingIconSrc"
        mode="aspectFit"
        @error="handleGreetingImageError"
      />
      <text v-else class="p-fib__icon-text p-fib__icon-text--greeting">Hi</text>
    </view>

    <!-- 关注按钮 -->
    <view
      class="p-fib__btn p-fib__btn--follow"
      :class="{ 'p-fib__btn--pending': followPending }"
      :aria-label="followLabel"
      @click="handleFollowClick"
    >
      <image
        v-if="followIconSrc"
        class="p-fib__icon-img"
        :src="followIconSrc"
        mode="aspectFit"
        @error="handleFollowImageError"
      />
      <text v-else class="p-fib__icon-text p-fib__icon-text--follow">❤</text>
    </view>

    <!-- 关注反馈 -->
    <view v-if="followFeedbackVisible" class="p-fib__feedback">
      <text class="p-fib__feedback-title">{{ followFeedbackTitle }}</text>
      <text v-if="followFeedbackDesc" class="p-fib__feedback-desc">{{ followFeedbackDesc }}</text>
    </view>
  </view>
</template>

<script setup>
import { computed, inject, ref, watch, onBeforeUnmount } from 'vue'
import sheep from '@/sheep'
import InteractionApi from '@/sheep/api/marriage/interaction'
import { getResponseData } from '@/sheep/helper/marriage'
import { showShareModal } from '@/sheep/hooks/useModal'
import { INTERACTION_GUARD_SCENES, requireRealNameInteraction } from '@/sheep/hooks/useRealNameInteractionGuard'
import {
  resolveShareIcon,
  normalizeIconUrl,
  FOLLOW_FEEDBACK_DURATION,
  buildPublicShareInfo,
  hasValidPartnerContext,
} from './partnerFloatingInteractionButtons'

const props = defineProps({
  config: { type: Object, required: true },
})

// ====== 人物上下文 ======
const partnerContext = inject('partnerRecommendationContext', null)
const setPageShareInfo = inject('setPageShareInfo', null)

const hasValidContext = computed(() => hasValidPartnerContext(partnerContext))

const profile = computed(() => partnerContext?.value?.profile || null)
const partnerId = computed(() => partnerContext?.value?.partnerId || null)
const interactionGeneration = computed(() => partnerContext?.value?.interactionGeneration ?? 0)
const floatingVisible = computed(() => {
  const ctx = partnerContext?.value
  if (!ctx) return true
  return ctx.floatingActionsVisible !== false
})

// 隐藏态：上下文无效 或 滚动隐藏
const isHidden = computed(() => !floatingVisible.value)

// ====== 图标状态机 ======

// 分享图标状态
const shareImageFailed = ref(false)
const shareIconSrc = computed(() => {
  const customUrl = normalizeIconUrl(props.config?.shareIconUrl)
  return resolveShareIcon(customUrl, shareImageFailed.value)
})

function handleShareImageError() {
  // 默认资源也失败时进入文字占位
  if (!normalizeIconUrl(props.config?.shareIconUrl) && !shareImageFailed.value) {
    shareImageFailed.value = true
    return
  }
  shareImageFailed.value = true
}

// 打招呼图标状态
const greetingImageFailed = ref(false)
const greetingIconSrc = computed(() => {
  if (greetingImageFailed.value) return ''
  const customUrl = normalizeIconUrl(props.config?.greetingIconUrl)
  return customUrl || '' // 空 → 使用 "Hi" 文字
})

function handleGreetingImageError() {
  greetingImageFailed.value = true
}

// 关注图标状态
const followImageFailed = ref(false)
const followIconSrc = computed(() => {
  if (followImageFailed.value) return ''
  const customUrl = normalizeIconUrl(props.config?.followIconUrl)
  return customUrl || '' // 空 → 使用 "❤" 文字
})

function handleFollowImageError() {
  followImageFailed.value = true
}

// 图标配置或模板变化时重置失败状态
const iconChangeKey = computed(() =>
  [
    props.config?.shareIconUrl,
    props.config?.greetingIconUrl,
    props.config?.followIconUrl,
  ].join('|'),
)

watch(iconChangeKey, () => {
  shareImageFailed.value = false
  greetingImageFailed.value = false
  followImageFailed.value = false
})

const contextKey = computed(() => `${partnerId.value || ''}|${interactionGeneration.value}`)
watch(contextKey, () => {
  // 旧操作与反馈不得越过页面已切换的人物、模板或身份作用域。
  greetingPending.value = false
  followPending.value = false
  followFeedbackSeq.value += 1
  followFeedbackVisible.value = false
  prepareProfileShare()
})

// ====== 动作 guards ======

const shareLabel = '分享当前人物'
const greetingLabel = '向当前人物打招呼'
const followLabel = '关注当前人物'

// 按钮仅在有可见、未隐藏且未 pending 时可用
const canShare = computed(() => hasValidContext.value && !isHidden.value)
const greetingPending = ref(false)
const followPending = ref(false)

// 组件实例是否已卸载
let unloaded = false
onBeforeUnmount(() => { unloaded = true })

function currentBindUserId() {
  return Number(sheep?.$store?.('user')?.userInfo?.id) || 0
}

function prepareProfileShare() {
  const shareInfo = buildPublicShareInfo(profile.value, currentBindUserId())
  if (shareInfo && typeof setPageShareInfo === 'function') setPageShareInfo(shareInfo)
  return shareInfo
}

function isCurrentInteractionContext(targetId, generation) {
  return !unloaded && partnerId.value === targetId && interactionGeneration.value === generation
}

// ====== 分享 ======
function handleShareClick() {
  if (!floatingVisible.value || !hasValidContext.value) return
  // #ifdef MP-WEIXIN
  prepareProfileShare()
  // #endif
  // #ifndef MP-WEIXIN
  // H5/App 使用 Sheep 分享面板
  try {
    const shareInfo = prepareProfileShare()
    if (shareInfo) {
      showShareModal()
    }
  } catch (e) {
    // 分享失败静默
  }
  // #endif
}

// ====== 打招呼 ======
async function handleGreetingClick() {
  if (!floatingVisible.value || !hasValidContext.value) return
  if (greetingPending.value) return

  const targetId = partnerId.value
  const generation = interactionGeneration.value

  // 1) 未登录
  greetingPending.value = true
  try {
    const verified = await requireRealNameInteraction(INTERACTION_GUARD_SCENES.MESSAGE)
    if (!isCurrentInteractionContext(targetId, generation)) return

    if (!verified) return

    // 3) 二次确认目标
    if (!isCurrentInteractionContext(targetId, generation)) return

    // 4) 进入聊天
    const nickname = encodeURIComponent(profile.value?.name || '')
    const avatar = encodeURIComponent(profile.value?.avatarImage || '')
    uni.navigateTo({
      url: `/pages/messages/chat/index?peerId=${targetId}&nickname=${nickname}&avatar=${avatar}`,
      fail: () => {
        uni.showToast({ title: '暂无法发起聊天', icon: 'none' })
      },
    })
  } catch (e) {
    if (!unloaded) {
      uni.showToast({ title: '操作失败，请稍后再试', icon: 'none' })
    }
  } finally {
    if (isCurrentInteractionContext(targetId, generation)) greetingPending.value = false
  }
}

// ====== 关注 ======
const followFeedbackVisible = ref(false)
const followFeedbackTitle = ref('')
const followFeedbackDesc = ref('')
const followFeedbackSeq = ref(0)

async function handleFollowClick() {
  if (!floatingVisible.value || !hasValidContext.value) return
  if (followPending.value) return

  const targetId = partnerId.value
  const targetName = profile.value?.name || ''
  const generation = interactionGeneration.value

  // 1) 未登录
  followPending.value = true
  try {
    const verified = await requireRealNameInteraction(INTERACTION_GUARD_SCENES.FOLLOW)
    if (!isCurrentInteractionContext(targetId, generation)) return

    if (!verified) return

    // 3) 二次确认目标
    if (!isCurrentInteractionContext(targetId, generation)) return

    // 4) 调用关注接口
    const followRes = await InteractionApi.follow(targetId)
    if (!isCurrentInteractionContext(targetId, generation)) return

    if (followRes?.code === 0) {
      const mutual = followRes?.data?.mutual === true
      showFollowFeedback(targetName, mutual)
    } else {
      uni.showToast({ title: followRes?.msg || '关注失败', icon: 'none' })
    }
  } catch (e) {
    if (!unloaded) {
      uni.showToast({ title: '关注失败，请稍后再试', icon: 'none' })
    }
  } finally {
    if (isCurrentInteractionContext(targetId, generation)) followPending.value = false
  }
}

function showFollowFeedback(name, mutual) {
  followFeedbackSeq.value += 1
  const seq = followFeedbackSeq.value
  followFeedbackTitle.value = mutual ? '已互相关注' : '已关注成功'
  followFeedbackDesc.value = mutual
    ? `你和${name || '对方'}已互相关注，可以继续互动`
    : `${name || '对方'}会收到你的关注提醒`
  followFeedbackVisible.value = true
  setTimeout(() => {
    if (followFeedbackSeq.value === seq && !unloaded) {
      followFeedbackVisible.value = false
    }
  }, FOLLOW_FEEDBACK_DURATION)
}

// ====== 安全区 ======
const safeAreaStyle = computed(() => {
  // #ifdef MP-WEIXIN
  const modelInfo = uni.getSystemInfoSync?.()
  const safeBottom = modelInfo?.safeAreaInsets?.bottom || 0
  const safeBottomPx = safeBottom > 0 ? safeBottom : 0
  return { paddingBottom: `${Math.round(safeBottomPx)}px` }
  // #endif
  // #ifndef MP-WEIXIN
  return {}
  // #endif
})
</script>

<style scoped lang="scss">
.p-fib {
  position: fixed;
  left: 50%;
  bottom: 176rpx;
  transform: translate3d(-50%, 0, 0) scale(1);
  display: flex;
  gap: 32rpx;
  align-items: center;
  z-index: 40;
  opacity: 1;
  transition: opacity 220ms, transform 220ms;

  &--hidden {
    opacity: 0;
    transform: translate3d(-50%, 168rpx, 0) scale(0.92);
    pointer-events: none;
  }

  &__btn {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    position: relative;

    &--share {
      background: rgba(248, 242, 238, 0.92);
      border: 1px solid rgba(203, 174, 168, 0.68);
      box-shadow: 0 6px 14px rgba(94, 63, 66, 0.1);
    }

    &--greeting {
      background: rgba(255, 249, 246, 0.92);
      border: 1px solid rgba(206, 175, 168, 0.72);
      box-shadow: 0 6px 14px rgba(94, 63, 66, 0.12);
    }

    &--follow {
      background: #c84449;
      box-shadow: 0 6px 14px rgba(200, 68, 73, 0.28);
    }

    &--pending {
      opacity: 0.6;
      pointer-events: none;
    }
  }

  &__icon-img {
    width: 68rpx;
    height: 68rpx;
  }

  &__icon-text {
    font-size: 36rpx;
    font-weight: 700;

    &--share {
      color: #5d403f;
    }

    &--greeting {
      color: #5d403f;
    }

    &--follow {
      color: #ffffff;
      font-size: 44rpx;
    }
  }

  &__native-share-btn {
    position: absolute;
    inset: 0;
    opacity: 0;
    z-index: 2;
    padding: 0;
    margin: 0;
    border: none;
    background: transparent;
    line-height: 1;

    &::after {
      border: none;
    }
  }

  &__feedback {
    position: fixed;
    left: 32rpx;
    right: 32rpx;
    top: 108rpx;
    z-index: 80;
    min-height: 144rpx;
    border-radius: 36rpx;
    padding: 24rpx 28rpx;
    background: rgba(255, 250, 247, 0.96);
    border: 1px solid rgba(220, 168, 171, 0.42);
    box-shadow: 0 12px 28px rgba(104, 63, 70, 0.18);
    display: flex;
    flex-direction: column;
    justify-content: center;
  }

  &__feedback-title {
    font-size: 32rpx;
    font-weight: 700;
    color: #3f292d;
    margin-bottom: 6rpx;
  }

  &__feedback-desc {
    font-size: 26rpx;
    color: #7c6461;
    line-height: 1.5;
  }
}
</style>
