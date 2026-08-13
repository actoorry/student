<template>
  <ContentWrap>
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-lg font-bold">签到配置</span>
          <div class="flex gap-2">
            <el-button @click="fillDefaultRows">
              <Icon class="mr-5px" icon="ep:plus" />补齐 7 天
            </el-button>
            <el-button
              type="primary"
              :loading="saving"
              @click="submitForm"
              v-hasPermi="['partner:sign-in-config:save']"
            >
              <Icon class="mr-5px" icon="ep:check" />保存
            </el-button>
          </div>
        </div>
      </template>
      <el-table v-loading="loading" :data="formData" :stripe="true">
        <el-table-column align="center" label="第几天" prop="day" width="120px" />
        <el-table-column align="center" label="奖励积分" width="220px">
          <template #default="scope">
            <el-input-number v-model="scope.row.point" :min="0" size="small" />
          </template>
        </el-table-column>
        <el-table-column align="center" label="奖励经验" width="220px">
          <template #default="scope">
            <el-input-number v-model="scope.row.experience" :min="0" size="small" />
          </template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="140px">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="0"
              :inactive-value="1"
              active-text="启用"
              inactive-text="停用"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </ContentWrap>
</template>

<script lang="ts" setup>
import * as PartnerSignInConfigApi from '@/api/partner/sign-in-config'
import { useMessage } from '@/hooks/web/useMessage'

const message = useMessage()
const loading = ref(false)
const saving = ref(false)
const formData = ref<PartnerSignInConfigApi.MemberSignInConfigVO[]>([])

onMounted(() => {
  getList()
})

const getList = async () => {
  loading.value = true
  try {
    const data = await PartnerSignInConfigApi.getSignInConfigList()
    formData.value = data && data.length > 0 ? data : buildDefaultRows()
    fillDefaultRows()
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  saving.value = true
  try {
    await PartnerSignInConfigApi.saveSignInConfig(formData.value)
    message.success('保存成功')
    await getList()
  } finally {
    saving.value = false
  }
}

const fillDefaultRows = () => {
  const existingDays = new Set(formData.value.map((item) => item.day))
  for (let day = 1; day <= 7; day++) {
    if (!existingDays.has(day)) {
      formData.value.push({
        day,
        point: day * 10,
        experience: 0,
        status: 0
      })
    }
  }
  formData.value = formData.value.sort((a, b) => a.day - b.day)
}

const buildDefaultRows = () => {
  const rows: PartnerSignInConfigApi.MemberSignInConfigVO[] = []
  for (let day = 1; day <= 7; day++) {
    rows.push({
      day,
      point: day * 10,
      experience: 0,
      status: 0
    })
  }
  return rows
}
</script>
