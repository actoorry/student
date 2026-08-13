<template>
  <view class="partner-user-header">
    <view class="partner-user-header__frame">
      <image
        v-if="backgroundUrl"
        :key="backgroundKey"
        class="partner-user-header__background"
        :src="backgroundUrl"
        mode="aspectFill"
        @error="backgroundFailed = true"
      />
      <view v-else class="partner-user-header__background partner-user-header__placeholder">
        <view class="partner-user-header__placeholder-gradient" />
        <view class="partner-user-header__placeholder-glow partner-user-header__placeholder-glow--left" />
        <view class="partner-user-header__placeholder-glow partner-user-header__placeholder-glow--right" />
        <view class="partner-user-header__placeholder-orb partner-user-header__placeholder-orb--large" />
        <view class="partner-user-header__placeholder-orb partner-user-header__placeholder-orb--small" />
        <view class="partner-user-header__placeholder-spark partner-user-header__placeholder-spark--left" />
        <view class="partner-user-header__placeholder-spark partner-user-header__placeholder-spark--right" />
      </view>
      <view class="partner-user-header__mask" />
      <view class="partner-user-header__cover-stage" @tap="openBackgroundActions" />
      <view class="partner-user-header__content">
        <view class="partner-user-header__identity">
          <image
            :key="avatarKey"
            class="partner-user-header__avatar"
            :src="avatarUrl"
            mode="aspectFill"
            @error="avatarFailed = true"
          />
          <text class="partner-user-header__nickname">{{ nickname }}</text>
        </view>
        <view class="partner-user-header__actions">
          <button
            v-if="authenticated"
            class="partner-user-header__edit ss-reset-button"
            @tap.stop="goProfileEditor"
          >
            编辑资料
          </button>
          <button class="partner-user-header__qrcode ss-reset-button" @tap.stop="showShareModal">
            <text class="sicon-qrcode" />
          </button>
        </view>
      </view>
    </view>

    <view v-if="previewVisible" class="partner-user-header__preview-mask" @tap="closeBackgroundPreview">
      <view class="partner-user-header__preview-panel" @tap.stop>
        <view class="partner-user-header__preview-canvas">
          <image
            v-if="backgroundUrl"
            class="partner-user-header__preview-image"
            :src="backgroundUrl"
            mode="aspectFit"
            @error="backgroundFailed = true"
          />
          <view v-else class="partner-user-header__preview-image partner-user-header__placeholder">
            <view class="partner-user-header__placeholder-gradient" />
            <view class="partner-user-header__placeholder-glow partner-user-header__placeholder-glow--left" />
            <view class="partner-user-header__placeholder-glow partner-user-header__placeholder-glow--right" />
            <view class="partner-user-header__placeholder-orb partner-user-header__placeholder-orb--large" />
            <view class="partner-user-header__placeholder-orb partner-user-header__placeholder-orb--small" />
            <view class="partner-user-header__placeholder-spark partner-user-header__placeholder-spark--left" />
            <view class="partner-user-header__placeholder-spark partner-user-header__placeholder-spark--right" />
          </view>
        </view>
        <view class="partner-user-header__preview-actions">
          <button class="partner-user-header__preview-button partner-user-header__preview-button--secondary ss-reset-button" @tap.stop="closeBackgroundPreview">关闭</button>
          <button class="partner-user-header__preview-button partner-user-header__preview-button--primary ss-reset-button" :disabled="uploading" @tap.stop="replaceBackgroundImage">
            {{ uploading ? '上传中...' : '更换背景图' }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
  import { computed, inject, ref, watch } from 'vue';
  import sheep from '@/sheep';
  import FileApi from '@/sheep/api/infra/file';
  import MarriageProfileApi from '@/sheep/api/marriage/profile';
  import { showShareModal } from '@/sheep/hooks/useModal';

  defineOptions({ name: 'PartnerUserHeader' });
  defineProps({
    data: { type: Object, default: () => ({}) },
    styles: { type: Object, default: () => ({}) },
  });

  const runtimeContext = inject('partnerUserProfileRuntimeContext', null);
  const profile = computed(() => runtimeContext?.state?.profile || null);
  const authenticated = computed(() => runtimeContext?.state?.authenticated === true);
  const avatarFailed = ref(false);
  const backgroundFailed = ref(false);
  const previewVisible = ref(false);
  const uploading = ref(false);
  const defaultAvatar = computed(() => sheep.$url.static('/static/img/shop/default_avatar.png'));

  const nickname = computed(() => {
    const value = typeof profile.value?.nickname === 'string' ? profile.value.nickname.trim() : '';
    if (value) return value;
    return authenticated.value ? '用户' : '请先登录';
  });
  const avatarUrl = computed(() => {
    const value = typeof profile.value?.avatar === 'string' ? profile.value.avatar.trim() : '';
    return avatarFailed.value || !value ? defaultAvatar.value : sheep.$url.cdn(value);
  });
  const backgroundUrl = computed(() => {
    const value = typeof profile.value?.backgroundImage === 'string'
      ? profile.value.backgroundImage.trim()
      : '';
    return backgroundFailed.value || !value ? '' : sheep.$url.cdn(value);
  });
  const avatarKey = computed(() => `${profile.value?.userId || 'anonymous'}:${avatarFailed.value}`);
  const backgroundKey = computed(() => `${profile.value?.userId || 'anonymous'}:${backgroundFailed.value}`);

  watch(
    () => [profile.value?.userId, profile.value?.avatar, profile.value?.backgroundImage].join('|'),
    () => {
      avatarFailed.value = false;
      backgroundFailed.value = false;
    },
  );

  function goProfileEditor() {
    if (!authenticated.value) return;
    sheep.$router.go('/pages/mine-profile/basic/index');
  }

  function openBackgroundActions() {
    if (!authenticated.value) {
      uni.showToast({ title: '请先登录后管理背景图', icon: 'none' });
      return;
    }
    uni.showActionSheet({
      itemList: ['查看背景图', '更换背景图'],
      success: ({ tapIndex }) => {
        if (tapIndex === 0) previewVisible.value = true;
        if (tapIndex === 1) replaceBackgroundImage();
      },
    });
  }

  function closeBackgroundPreview() {
    if (!uploading.value) previewVisible.value = false;
  }

  async function replaceBackgroundImage() {
    if (!authenticated.value || uploading.value) return;
    previewVisible.value = false;
    const filePath = await chooseBackgroundImage();
    if (!filePath) return;
    uploading.value = true;
    try {
      const uploadResult = await FileApi.uploadFile(filePath, 'marriage/background');
      const imageUrl = typeof uploadResult?.data === 'string' ? uploadResult.data.trim() : '';
      if (uploadResult?.code !== 0 || !imageUrl) throw new Error('图片上传失败');
      const updateResult = await MarriageProfileApi.updateBackgroundImage(imageUrl);
      if (updateResult?.code !== 0) throw new Error(updateResult?.msg || '背景图更新失败');
      if (typeof runtimeContext?.refresh === 'function') await runtimeContext.refresh();
      uni.showToast({ title: '背景图已更新', icon: 'success' });
    } catch (error) {
      uni.showToast({ title: error?.message || '上传失败，请稍后重试', icon: 'none' });
    } finally {
      uploading.value = false;
    }
  }

  function chooseBackgroundImage() {
    return new Promise((resolve) => {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album'],
        success: (result) => resolve(result.tempFilePaths?.[0] || ''),
        fail: () => resolve(''),
      });
    });
  }
</script>

<style scoped lang="scss">
  .partner-user-header {
    position: relative;
    height: 360rpx;

    &__frame {
      position: relative;
      width: 100%;
      height: 100%;
      overflow: hidden;
    }

    &__background {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
    }

    &__placeholder {
      overflow: hidden;
      background: #e8dff9;
    }

    &__placeholder-gradient {
      position: absolute;
      inset: 0;
      background: radial-gradient(circle at left top, rgba(173, 156, 255, 0.96) 0%, rgba(194, 186, 255, 0.58) 28%, rgba(243, 236, 255, 0.52) 56%, rgba(255, 215, 231, 0.95) 100%);
    }

    &__placeholder-glow,
    &__placeholder-orb,
    &__placeholder-spark {
      position: absolute;
      border-radius: 999rpx;
    }

    &__placeholder-glow {
      background: rgba(255, 255, 255, 0.32);

      &--left { left: -70rpx; bottom: -110rpx; width: 320rpx; height: 320rpx; }
      &--right { right: -80rpx; bottom: -120rpx; width: 360rpx; height: 360rpx; background: rgba(255, 210, 229, 0.4); }
    }

    &__placeholder-orb {
      background: rgba(255, 255, 255, 0.17);
      box-shadow: 0 0 48rpx rgba(255, 255, 255, 0.2);

      &--large { right: 88rpx; top: 86rpx; width: 184rpx; height: 184rpx; }
      &--small { right: 286rpx; top: 70rpx; width: 106rpx; height: 106rpx; }
    }

    &__placeholder-spark {
      width: 12rpx;
      height: 12rpx;
      background: rgba(255, 255, 255, 0.98);
      box-shadow: 0 0 28rpx rgba(255, 255, 255, 0.9);

      &--left { left: 128rpx; top: 92rpx; }
      &--right { right: 168rpx; top: 58rpx; }
    }

    &__mask {
      position: absolute;
      inset: 0;
      background: linear-gradient(90deg, rgb(25 20 31 / 22%), rgb(25 20 31 / 56%));
    }

    &__cover-stage {
      position: absolute;
      z-index: 1;
      inset: 0;
    }

    &__content {
      position: relative;
      z-index: 2;
      display: flex;
      height: 100%;
      align-items: flex-end;
      justify-content: space-between;
      padding: 0 32rpx 42rpx;
      box-sizing: border-box;
      pointer-events: none;
    }

    &__identity { display: flex; min-width: 0; flex: 1; align-items: center; }
    &__avatar { width: 96rpx; height: 96rpx; flex: none; margin-right: 20rpx; border: 3rpx solid rgb(255 255 255 / 82%); border-radius: 50%; background: #f7e8e6; }
    &__nickname { overflow: hidden; color: #fff; font-size: 36rpx; font-weight: 700; text-overflow: ellipsis; white-space: nowrap; }
    &__actions { display: flex; width: 112rpx; flex: none; flex-direction: column; align-items: center; gap: 18rpx; pointer-events: auto; }
    &__edit { padding: 8rpx 12rpx; border: 1rpx solid rgb(255 255 255 / 72%); border-radius: 18rpx; color: #fff; font-size: 22rpx; line-height: 1; white-space: nowrap; }
    &__qrcode { color: #fff; .sicon-qrcode { font-size: 46rpx; } }

    &__preview-mask {
      position: fixed;
      z-index: 999;
      inset: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 40rpx;
      background: rgba(0, 0, 0, 0.72);
      box-sizing: border-box;
    }

    &__preview-panel { width: 100%; overflow: hidden; border-radius: 28rpx; background: #fffdfa; }
    &__preview-canvas { position: relative; width: 100%; height: 720rpx; overflow: hidden; background: #201614; }
    &__preview-image { position: absolute; inset: 0; width: 100%; height: 100%; }
    &__preview-actions { display: flex; padding: 24rpx; gap: 16rpx; }
    &__preview-button { display: flex; height: 84rpx; flex: 1; align-items: center; justify-content: center; border-radius: 42rpx; font-size: 28rpx; font-weight: 700; }
    &__preview-button--secondary { background: #f4efeb; color: #5f4744; }
    &__preview-button--primary { background: #ef655f; color: #fff; }
  }
</style>
