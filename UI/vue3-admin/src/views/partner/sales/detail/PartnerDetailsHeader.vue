<template>
  <div v-loading="loading">
    <div class="flex items-start justify-between">
      <div>
        <el-col>
          <el-row>
            <span class="text-xl font-bold">{{ partner.name }}</span>
          </el-row>
        </el-col>
      </div>
      <div>
        <slot></slot>
      </div>
    </div>
  </div>
  <ContentWrap class="mt-10px">
    <el-descriptions :column="5" direction="vertical">
      <el-descriptions-item label="客户级别">
        <dict-tag :type="DICT_TYPE.CRM_CUSTOMER_LEVEL" :value="partner.level" />
      </el-descriptions-item>
      <el-descriptions-item label="成交状态">
        {{ partner.dealStatus ? '已成交' : '未成交' }}
      </el-descriptions-item>
      <el-descriptions-item label="负责人">{{ partner.ownerUserName }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">
        {{ formatDate(partner.createTime) }}
      </el-descriptions-item>
    </el-descriptions>
  </ContentWrap>
</template>
<script lang="ts" setup>
import { DICT_TYPE } from '@/utils/dict'
import * as PartnerApi from '@/api/partner/sales'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'PartnerDetailsHeader' })
defineProps<{
  partner: PartnerApi.PartnerVO
  loading: boolean
}>()
</script>
