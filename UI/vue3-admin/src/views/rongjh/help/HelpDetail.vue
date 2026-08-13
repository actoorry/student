<template>
  <Dialog v-model="dialogVisible" title="帮扶申请详情" width="760px">
    <el-descriptions v-loading="loading" :column="2" border>
      <el-descriptions-item label="申请编号">{{ detail.id }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="statusTagType(detail.status)">{{ detail.statusName }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="申请人昵称">{{ detail.nickname || '-' }}</el-descriptions-item>
      <el-descriptions-item label="申请人手机">{{ detail.mobile || '-' }}</el-descriptions-item>
      <el-descriptions-item label="困难人姓名">{{ detail.name || '-' }}</el-descriptions-item>
      <el-descriptions-item label="困难人电话">{{ detail.phone || '-' }}</el-descriptions-item>
      <el-descriptions-item label="身份证号">{{ detail.idCard || '-' }}</el-descriptions-item>
      <el-descriptions-item label="申请金额">{{ formatAmount(detail.applyAmount) }}</el-descriptions-item>
      <el-descriptions-item label="批准金额">{{ formatAmount(detail.actualAmount) }}</el-descriptions-item>
      <el-descriptions-item label="申请时间">{{ detail.createTime || '-' }}</el-descriptions-item>
      <el-descriptions-item label="审核备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      <el-descriptions-item label="困难原因" :span="2">{{ detail.reason || '-' }}</el-descriptions-item>
      <el-descriptions-item v-if="detail.materialUrls?.length" label="证明材料" :span="2">
        <div class="material-list">
          <el-image
            v-for="(url, index) in detail.materialUrls"
            :key="index"
            :src="url"
            :preview-src-list="detail.materialUrls"
            :initial-index="index"
            fit="cover"
            class="material-image"
          />
        </div>
      </el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import * as HelpApi from '@/api/rongjh/help'

defineOptions({ name: 'RongjhHelpDetail' })

const dialogVisible = ref(false)
const loading = ref(false)
const detail = ref<HelpApi.HelpVO>({} as HelpApi.HelpVO)

const statusTagType = (status?: number) => {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  if (status === 3) return 'info'
  return 'warning'
}

const formatAmount = (amount?: number) => {
  if (amount === undefined || amount === null) return '-'
  return `¥${Number(amount).toFixed(2)}`
}

const open = async (id: number) => {
  dialogVisible.value = true
  loading.value = true
  try {
    detail.value = await HelpApi.getHelp(id)
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>

<style scoped>
.material-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.material-image {
  width: 120px;
  height: 120px;
  border-radius: 8px;
}
</style>
