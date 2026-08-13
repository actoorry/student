<template>
  <div class="partner-user-header">
    <div class="partner-user-header__glow partner-user-header__glow--left"></div>
    <div class="partner-user-header__glow partner-user-header__glow--right"></div>
    <div class="partner-user-header__orb partner-user-header__orb--large"></div>
    <div class="partner-user-header__orb partner-user-header__orb--small"></div>
    <div class="partner-user-header__mask"></div>
    <div class="partner-user-header__content">
      <div class="partner-user-header__identity">
        <div class="partner-user-header__avatar">{{ preview.avatarLabel }}</div>
        <span class="partner-user-header__name">{{ preview.nickname }}</span>
      </div>
      <div class="partner-user-header__actions">
        <span class="partner-user-header__edit">编辑资料</span>
        <Icon icon="tdesign:qrcode" :size="22" color="#ffffff" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { PARTNER_USER_HEADER_PREVIEW, type PartnerUserHeaderProperty } from './config'

defineOptions({ name: 'PartnerUserHeader' })
defineProps<{ property: PartnerUserHeaderProperty }>()

// 仅用于管理端视觉预览；不参与属性保存或网络请求。
const preview = PARTNER_USER_HEADER_PREVIEW
</script>

<style scoped lang="scss">
.partner-user-header {
  position: relative;
  min-height: 160px;
  overflow: hidden;
  background: radial-gradient(circle at left top, #ad9cff 0%, #c2baff 28%, #f3ecff 56%, #ffd7e7 100%);

  &__glow,
  &__orb {
    position: absolute;
    border-radius: 999px;
  }

  &__glow {
    background: rgb(255 255 255 / 32%);

    &--left {
      bottom: -68px;
      left: -34px;
      width: 160px;
      height: 160px;
    }

    &--right {
      right: -40px;
      bottom: -72px;
      width: 180px;
      height: 180px;
      background: rgb(255 210 229 / 40%);
    }
  }

  &__orb {
    background: rgb(255 255 255 / 17%);
    box-shadow: 0 0 24px rgb(255 255 255 / 20%);

    &--large {
      top: 42px;
      right: 42px;
      width: 92px;
      height: 92px;
    }

    &--small {
      top: 34px;
      right: 142px;
      width: 54px;
      height: 54px;
    }
  }

  &__mask {
    position: absolute;
    inset: 0;
    background: linear-gradient(90deg, rgb(17 20 30 / 20%), rgb(17 20 30 / 55%));
  }

  &__content {
    position: relative;
    z-index: 1;
    display: flex;
    min-height: 160px;
    align-items: flex-end;
    justify-content: space-between;
    padding: 0 20px 22px;
    color: #fff;
  }

  &__identity {
    display: flex;
    min-width: 0;
    flex: 1;
    align-items: center;
    gap: 12px;
  }

  &__avatar {
    display: flex;
    width: 48px;
    height: 48px;
    flex: none;
    align-items: center;
    justify-content: center;
    border: 2px solid rgb(255 255 255 / 82%);
    border-radius: 50%;
    background: #f7e8e6;
    color: #af6e79;
    font-size: 20px;
    font-weight: 600;
  }

  &__name {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 20px;
    font-weight: 700;
  }

  &__actions {
    display: flex;
    width: 64px;
    flex: none;
    flex-direction: column;
    align-items: center;
    gap: 10px;
  }

  &__edit {
    padding: 4px 8px;
    border: 1px solid rgb(255 255 255 / 68%);
    border-radius: 12px;
    font-size: 11px;
    line-height: 1;
    white-space: nowrap;
  }
}
</style>
