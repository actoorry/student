<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
    <el-descriptions v-loading="loading" :column="2" border>
      <el-descriptions-item label="会员编号">{{ detail.id }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="statusTagType(detail.status)">{{ detail.statusName }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="昵称">{{ detail.nickname || '-' }}</el-descriptions-item>
      <el-descriptions-item label="手机号">{{ detail.mobile || '-' }}</el-descriptions-item>
      <el-descriptions-item label="真实姓名">{{ detail.name || '-' }}</el-descriptions-item>
      <el-descriptions-item label="身份证号">{{ detail.idCard || '-' }}</el-descriptions-item>
      <el-descriptions-item label="人员类别">{{ detail.typeName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="省份">{{ detail.province || '-' }}</el-descriptions-item>
      <el-descriptions-item label="城市">{{ detail.city || '-' }}</el-descriptions-item>
      <el-descriptions-item label="联系电话">{{ detail.phone || '-' }}</el-descriptions-item>
      <template v-if="!isSanshuRecord">
        <el-descriptions-item label="军种">{{ detail.militaryBranch || '-' }}</el-descriptions-item>
        <el-descriptions-item label="服役单位">{{ detail.serviceUnit || '-' }}</el-descriptions-item>
        <el-descriptions-item label="服役年份">{{ detail.serviceYear || '-' }}</el-descriptions-item>
      </template>
      <el-descriptions-item label="申请时间">{{ detail.createTime || '-' }}</el-descriptions-item>
      <el-descriptions-item label="审核备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="descriptionLabel" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
      <el-descriptions-item v-if="detail.certificateImg" label="证件照片" :span="2">
        <el-image :src="detail.certificateImg" style="width: 200px" fit="contain" />
      </el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import * as WarriorApi from '@/api/rongjh/warrior'

defineOptions({ name: 'RongjhWarriorDetail' })

const props = defineProps({
  sanshuMode: {
    type: Boolean,
    default: false
  }
})

const dialogVisible = ref(false)
const loading = ref(false)
const detail = ref<WarriorApi.WarriorVO>({} as WarriorApi.WarriorVO)

const isSanshuRecord = computed(() => {
  const type = detail.value?.type
  return props.sanshuMode || (!!type && type !== 'SELF')
})

const dialogTitle = computed(() => (isSanshuRecord.value ? '三属身份详情' : '战友会身份详情'))
const descriptionLabel = computed(() => (isSanshuRecord.value ? '情况说明' : '入会说明'))

const statusTagType = (status?: number) => {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'warning'
}

const open = async (id: number) => {
  dialogVisible.value = true
  loading.value = true
  try {
    detail.value = await WarriorApi.getWarrior(id)
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>
