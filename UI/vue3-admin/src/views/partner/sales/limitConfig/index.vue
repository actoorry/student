<template>
  <doc-alert title="【客户】客户管理、公海客户" url="https://doc.appap.vip/partner/sales/" />
  <doc-alert title="【通用】数据权限" url="https://doc.appap.vip/crm/permission/" />

  <!-- 列表 -->
  <ContentWrap>
    <el-tabs>
      <el-tab-pane label="拥有客户数限制">
        <PartnerLimitConfigList :confType="LimitConfType.PARTNER_QUANTITY_LIMIT" />
      </el-tab-pane>
      <el-tab-pane label="锁定客户数限制">
        <PartnerLimitConfigList :confType="LimitConfType.PARTNER_LOCK_LIMIT" />
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>
</template>
<script setup lang="ts">
import PartnerLimitConfigList from './PartnerLimitConfigList.vue'
import { LimitConfType } from '@/api/partner/sales/limitConfig'

defineOptions({ name: 'PartnerLimitConfig' })
</script>
