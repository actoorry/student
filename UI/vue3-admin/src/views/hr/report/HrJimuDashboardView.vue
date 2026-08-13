<template>
  <ContentWrap :bodyStyle="{ padding: '0px' }" class="!mb-0 hr-dashboard-wrap">
    <el-empty v-if="!pageId" description="未配置仪表盘 pageId，请在菜单 component 中传入 pageId" />
    <IFrame v-else :src="src" />
  </ContentWrap>
</template>

<script lang="ts" setup>
import { getRefreshToken, getTenantId } from '@/utils/auth'

defineOptions({ name: 'HrJimuDashboard' })

const route = useRoute()

/** 菜单 component 示例：hr/report/HrJimuDashboardView?pageId=1234567890 */
const pageId = computed(() => {
  const id = route.meta.query?.pageId
  return id ? String(id) : ''
})

const src = computed(() => {
  if (!pageId.value) {
    return ''
  }
  const token = getRefreshToken()
  const tenantId = getTenantId() || '1'
  const baseUrl = import.meta.env.VITE_BASE_URL || ''
  return (
    baseUrl +
    '/drag/share/view/' +
    pageId.value +
    '?token=' +
    token +
    '&tenantId=' +
    tenantId
  )
})
</script>

<style scoped>
.hr-dashboard-wrap :deep(.el-card__body) {
  padding: 0 !important;
}
</style>
