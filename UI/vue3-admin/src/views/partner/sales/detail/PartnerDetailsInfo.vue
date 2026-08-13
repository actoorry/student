<template>
  <ContentWrap>
    <el-collapse v-model="activeNames" class="">
      <el-collapse-item name="basicInfo">
        <template #title>
          <span class="text-base font-bold">基本信息</span>
        </template>
        <el-descriptions :column="4">
          <el-descriptions-item label="客户名称">
            {{ partner.name }}
          </el-descriptions-item>
          <el-descriptions-item label="客户来源">
            <dict-tag :type="DICT_TYPE.CRM_CUSTOMER_SOURCE" :value="partner.source" />
          </el-descriptions-item>
          <el-descriptions-item label="手机">{{ partner.mobile }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ partner.telephone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ partner.email }}</el-descriptions-item>
          <el-descriptions-item label="地址">
            {{ partner.areaName }} {{ partner.detailAddress }}
          </el-descriptions-item>
          <el-descriptions-item label="QQ">{{ partner.qq }}</el-descriptions-item>
          <el-descriptions-item label="微信">{{ partner.wechat }}</el-descriptions-item>
          <el-descriptions-item label="客户行业">
            <dict-tag :type="DICT_TYPE.CRM_CUSTOMER_INDUSTRY" :value="partner.industryId" />
          </el-descriptions-item>
          <el-descriptions-item label="客户级别">
            <dict-tag :type="DICT_TYPE.CRM_CUSTOMER_LEVEL" :value="partner.level" />
          </el-descriptions-item>
          <el-descriptions-item label="下次联系时间">
            {{ formatDate(partner.contactNextTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="备注">{{ partner.remark }}</el-descriptions-item>
        </el-descriptions>
      </el-collapse-item>
      <el-collapse-item name="systemInfo">
        <template #title>
          <span class="text-base font-bold">系统信息</span>
        </template>
        <el-descriptions :column="4">
          <el-descriptions-item label="负责人">{{ partner.ownerUserName }}</el-descriptions-item>
          <el-descriptions-item label="最后跟进记录">
            {{ partner.contactLastContent }}
          </el-descriptions-item>
          <el-descriptions-item label="最后跟进时间">
            {{ formatDate(partner.contactLastTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="">&nbsp;</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ partner.creatorName }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(partner.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDate(partner.updateTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-collapse-item>
    </el-collapse>
  </ContentWrap>
</template>
<script lang="ts" setup>
import * as PartnerApi from '@/api/partner/sales'
import { DICT_TYPE } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'PartnerDetailsInfo' })
defineProps<{
  partner: PartnerApi.PartnerVO
}>()

const activeNames = ref(['basicInfo', 'systemInfo'])
</script>
<style lang="scss" scoped></style>
