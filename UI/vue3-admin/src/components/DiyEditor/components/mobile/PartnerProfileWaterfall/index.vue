<template>
  <div class="card">
    <img class="image" :style="{ width: `${property.imageWidthPercent}%` }" :src="sampleImage" alt="匿名人物样例" />
    <div class="content">
      <div class="identity-row">
        <strong>林薇</strong>
        <span class="member-badge">✓ 会员</span>
      </div>
      <div class="age-row"><b>26岁</b><span>女</span></div>
      <div class="detail-row"><img class="detail-icon" :src="workIcon" alt="" /><span class="detail-label">职业</span><strong>产品设计师</strong></div>
      <div class="detail-row"><img class="detail-icon" :src="locationIcon" alt="" /><span class="detail-label">居住地</span><strong>成都</strong></div>
      <div class="actions">
        <button class="contact-button">联系对方</button>
        <button class="profile-button">查看资料</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { PartnerProfileWaterfallProperty } from './config'
import sampleImage from '@/assets/imgs/profile.jpg'
import workIcon from '@/assets/imgs/partner-profile-work.png'
import locationIcon from '@/assets/imgs/partner-profile-location.png'

defineOptions({ name: 'PartnerProfileWaterfall' })
defineProps<{ property: PartnerProfileWaterfallProperty }>()
</script>

<style scoped>
.card {
  display: flex;
  height: 210px;
  overflow: hidden;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 6px 12px rgb(45 31 32 / 12%);
}
.image {
  flex-shrink: 0;
  height: 100%;
  object-fit: cover;
}
.content {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
  padding: 17px 16px 14px;
}
.identity-row, .age-row, .detail-row, .actions { display: flex; align-items: center; }
.identity-row { min-width: 0; gap: 10px; }
.identity-row strong { display: block; flex: 1; min-width: 0; overflow: hidden; color: #241a18; font-size: 23px; line-height: 28px; text-overflow: ellipsis; white-space: nowrap; }
.member-badge { flex-shrink: 0; padding: 3px 8px; color: #e85057; font-size: 11px; font-weight: 700; background: #fff0f2; border-radius: 12px; }
.age-row { gap: 22px; margin: 12px 0 10px; }
.age-row b { color: #e85057; font-size: 20px; }
.age-row span { color: #241a18; font-size: 16px; }
.detail-row { gap: 8px; min-height: 34px; border-top: 1px dashed #e7dfe0; }
.detail-icon { flex-shrink: 0; width: 17px; height: 17px; object-fit: contain; }
.detail-label { width: 45px; color: #8a7d7a; font-size: 12px; }
.detail-row strong { overflow: hidden; color: #302526; font-size: 14px; text-overflow: ellipsis; white-space: nowrap; }
.actions { gap: 8px; margin-top: auto; }
.actions button {
  flex: 1;
  min-width: 0;
  height: 32px;
  padding: 0 8px;
  margin: 0;
  font-size: 13px;
  font-weight: 700;
  border-radius: 17px;
}
.contact-button { color: #e85057; background: #fff; border: 1px solid #e85057; }
.actions .profile-button { color: #fff; background: #e85057; border: 1px solid #e85057; }
</style>
