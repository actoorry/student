<template>
  <el-card shadow="never">
    <template #header>
      <slot name="header"></slot>
    </template>
    <el-row>
      <el-col :span="4">
        <ElAvatar :size="140" :src="user.avatar || undefined" shape="square" />
      </el-col>
      <el-col :span="20">
        <el-descriptions :column="2">
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="ep:user" label="昵称" />
            </template>
            {{ user.nickname }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="ep:phone" label="手机号" />
            </template>
            {{ user.mobile }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="ep:message" label="邮箱" />
            </template>
            {{ user.email || '空' }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="fa:mars-double" label="性别" />
            </template>
            <dict-tag :type="DICT_TYPE.SYSTEM_USER_SEX" :value="user.sex" />
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="ep:location" label="所在地" />
            </template>
            {{ user.areaName || '空' }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="fa:birthday-cake" label="生日" />
            </template>
            {{ user.birthday ? formatDate(user.birthday as any) : '空' }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="ep:calendar" label="注册时间" />
            </template>
            {{ user.createTime ? formatDate(user.createTime as any) : '空' }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="ep:office-building" label="客户" />
            </template>
            <el-tag v-if="user.isCustomer" type="success">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="ep:van" label="供应商" />
            </template>
            <el-tag v-if="user.isSupplier" type="success">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="ep:office-building" label="公司" />
            </template>
            <el-tag v-if="user.isCompany" type="success">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="ep:edit" label="备注" />
            </template>
            {{ user.remark || '空' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-col>
    </el-row>
  </el-card>
</template>
<script lang="ts" setup>
import { DICT_TYPE } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import * as PartnerApi from '@/api/partner/partner'
import { DescriptionsItemLabel } from '@/components/Descriptions/index'

defineProps<{ user: PartnerApi.PartnerVO }>()
</script>
<style lang="scss" scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
