<template>

  <div class="profile-block">

    <div class="profile-block__head">

      <div class="profile-block__title-wrap">

        <span class="profile-block__accent profile-block__accent--cert"></span>

        <span class="profile-block__title">执业证书</span>

        <el-tag v-if="certificates?.length" size="small" round type="info">{{ certificates.length }}</el-tag>

      </div>

      <el-link type="primary" :underline="false" @click="emit('navigate', 'HrMyCertificate')">

        维护证书 →

      </el-link>

    </div>

    <el-empty v-if="!certificates?.length" description="暂无执业证书" :image-size="72" />

    <el-row v-else :gutter="14">

      <el-col

        v-for="cert in certificates"

        :key="cert.id"

        :xl="8"

        :lg="12"

        :md="12"

        :sm="24"

        :xs="24"

        class="mb-14px"

      >

        <div

          class="cert-card"

          :class="{ 'cert-card--alert': isAlertStatus(cert.status) }"

          @click="emit('navigate', 'HrMyCertificate')"

        >

          <div class="cert-card__top">

            <div class="cert-card__icon">

              <Icon icon="ep:medal" :size="22" />

            </div>

            <el-tag :type="certStatusTag(cert.status)" size="small">{{ certStatusLabel(cert.status) }}</el-tag>

          </div>

          <div class="cert-card__name">{{ cert.certificateName || '未命名证书' }}</div>

          <div class="cert-card__no">编号 {{ cert.certificateNo || '-' }}</div>

          <div class="cert-card__meta">

            <span>{{ cert.issuingAuthority || '发证机关未填' }}</span>

          </div>

          <div class="cert-card__dates">

            <span>发证 {{ formatDate(cert.issueDate) }}</span>

            <el-divider direction="vertical" />

            <span>到期 {{ formatDate(cert.expireDate) }}</span>

          </div>

        </div>

      </el-col>

    </el-row>

  </div>

</template>



<script lang="ts" setup>

import type { Certificate } from '@/api/hr/certificate'

import {

  formatDate,

  certStatusTag,

  certStatusLabel,

  isAlertStatus

} from '@/views/hr/my/utils/profileUtils'



defineOptions({ name: 'ProfileCertificateList' })



defineProps<{

  certificates?: Certificate[]

}>()



const emit = defineEmits<{

  navigate: [name: string]

}>()

</script>



<style lang="scss" scoped>

.profile-block {

  margin-bottom: 20px;

  padding: 20px 22px;

  background: var(--el-bg-color);

  border: 1px solid var(--el-border-color-lighter);

  border-radius: 12px;

  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);

}



.profile-block__head {

  display: flex;

  justify-content: space-between;

  align-items: center;

  margin-bottom: 16px;

}



.profile-block__title-wrap {

  display: flex;

  align-items: center;

  gap: 10px;

}



.profile-block__accent {

  width: 4px;

  height: 18px;

  border-radius: 2px;



  &--cert {

    background: linear-gradient(180deg, #e6a23c, #f3d19e);

  }

}



.profile-block__title {

  font-size: 16px;

  font-weight: 600;

}



.cert-card {

  height: 100%;

  padding: 16px;

  border: 1px solid var(--el-border-color-lighter);

  border-radius: 10px;

  background: var(--el-fill-color-blank);

  cursor: pointer;

  transition: all 0.22s ease;



  &:hover {

    border-color: rgba(64, 158, 255, 0.35);

    box-shadow: 0 8px 20px rgba(64, 158, 255, 0.1);

    transform: translateY(-2px);

  }



  &--alert {

    border-color: rgba(230, 162, 60, 0.45);

    background: linear-gradient(135deg, rgba(230, 162, 60, 0.06), transparent);

  }

}



.cert-card__top {

  display: flex;

  justify-content: space-between;

  align-items: flex-start;

  margin-bottom: 12px;

}



.cert-card__icon {

  display: flex;

  align-items: center;

  justify-content: center;

  width: 40px;

  height: 40px;

  border-radius: 10px;

  color: #e6a23c;

  background: rgba(230, 162, 60, 0.12);

}



.cert-card__name {

  font-size: 15px;

  font-weight: 600;

  color: var(--el-text-color-primary);

  margin-bottom: 6px;

  line-height: 1.4;

}



.cert-card__no {

  font-size: 12px;

  color: var(--el-text-color-secondary);

  margin-bottom: 8px;

}



.cert-card__meta {

  font-size: 13px;

  color: var(--el-text-color-regular);

  margin-bottom: 10px;

}



.cert-card__dates {

  font-size: 12px;

  color: var(--el-text-color-placeholder);

  padding-top: 10px;

  border-top: 1px dashed var(--el-border-color-lighter);

}

</style>


