<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="78px"
    >
      <el-form-item label="姓名" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="推送目标姓名"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="通知类型" prop="notifyType">
        <el-select v-model="queryParams.notifyType" placeholder="请选择" clearable class="!w-180px">
          <el-option v-for="opt in notifyTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="推送状态" prop="pushStatus">
        <el-select v-model="queryParams.pushStatus" placeholder="请选择" clearable class="!w-160px">
          <el-option v-for="opt in pushStatusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="目标类型" prop="targetType">
        <el-select v-model="queryParams.targetType" placeholder="请选择" clearable class="!w-160px">
          <el-option v-for="opt in targetTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="推送时间" prop="notifyTime">
        <el-date-picker
          v-model="queryParams.notifyTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-220px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
    >
      <el-table-column label="推送时间" align="center" prop="notifyTime" min-width="160px" :formatter="dateFormatter" />
      <el-table-column label="目标" align="center" min-width="120px">
        <template #default="scope">
          <div>{{ scope.row.targetName || '-' }}</div>
          <el-tag size="small" class="mt-1" :type="targetTypeTag(scope.row.targetType)">
            {{ targetTypeLabel(scope.row.targetType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="证书名称" align="center" prop="certificateName" min-width="120px" />
      <el-table-column label="证书编号" align="center" prop="certificateNo" min-width="110px" />
      <el-table-column label="通知类型" align="center" prop="notifyType" min-width="110px">
        <template #default="scope">{{ notifyTypeLabel(scope.row.notifyType) }}</template>
      </el-table-column>
      <el-table-column label="参考日" align="center" prop="referenceDate" min-width="110px" />
      <el-table-column label="渠道" align="center" prop="notifyChannel" min-width="80px">
        <template #default="scope">{{ channelLabel(scope.row.notifyChannel) }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="pushStatus" min-width="90px">
        <template #default="scope">
          <el-tag :type="pushStatusTag(scope.row.pushStatus)">{{ pushStatusLabel(scope.row.pushStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="消息摘要/原因" align="center" min-width="220px" show-overflow-tooltip>
        <template #default="scope">
          <span v-if="scope.row.pushStatus === 'failed'" class="text-red-500">{{ scope.row.errorMsg }}</span>
          <span v-else-if="scope.row.pushStatus === 'skipped'">{{ scope.row.errorMsg }}</span>
          <span v-else>{{ scope.row.contentSnapshot }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" min-width="90px" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.pushStatus === 'failed'"
            link
            type="primary"
            @click="handleRetry(scope.row.id)"
            v-hasPermi="['hr:certificate:notifylog:retry']"
          >补推</el-button>
          <span v-else class="text-gray-400">-</span>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { CertificateApi, CertificateNotifyLog } from '@/api/hr/certificate'

/** 证书推送日志 */
defineOptions({ name: 'HrCertificateNotifyLog' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<CertificateNotifyLog[]>([])
const total = ref(0)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  notifyType: undefined,
  pushStatus: undefined,
  targetType: undefined,
  notifyTime: []
})
const queryFormRef = ref()

const notifyTypeOptions = [
  { label: '已过期', value: 'expired' },
  { label: '到期前90天', value: 'expire_90' },
  { label: '到期前60天', value: 'expire_60' },
  { label: '到期前30天', value: 'expire_30' },
  { label: '考核前60天', value: 'assessment_60' }
]
const pushStatusOptions = [
  { label: '成功', value: 'success' },
  { label: '失败', value: 'failed' },
  { label: '跳过', value: 'skipped' }
]
const targetTypeOptions = [
  { label: '本人', value: 'self' },
  { label: '科室主任', value: 'dept_leader' },
  { label: '人事科', value: 'hr' }
]

const notifyTypeLabel = (v?: string) => notifyTypeOptions.find((o) => o.value === v)?.label || v || ''
const pushStatusLabel = (v?: string) => pushStatusOptions.find((o) => o.value === v)?.label || v || ''
const pushStatusTag = (v?: string) => {
  if (v === 'success') return 'success'
  if (v === 'failed') return 'danger'
  return 'info'
}
const targetTypeLabel = (v?: string) => targetTypeOptions.find((o) => o.value === v)?.label || v || ''
const targetTypeTag = (v?: string) => (v === 'self' ? 'success' : v === 'dept_leader' ? 'warning' : 'info')
const channelLabel = (v?: string) => (v === 'internal' ? '站内信' : v === 'zhiye' ? '智业' : v || '')

const getList = async () => {
  loading.value = true
  try {
    const data = await CertificateApi.getCertificateNotifyLogPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const handleRetry = async (id: number) => {
  try {
    await message.confirm('确定对该条失败记录进行补推吗？')
    await CertificateApi.retryCertificateNotifyLog(id)
    message.success('补推完成')
    await getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>
