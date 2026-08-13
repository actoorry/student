<template>
  <ContentWrap>
    <el-row :gutter="16" class="mb-15px">
      <el-col :span="6">
        <el-card shadow="never">
          <div class="text-gray-500 text-13px">当前在聘</div>
          <div class="text-24px font-bold mt-5px">{{ summary.totalActive ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="text-gray-500 text-13px">90天内到期</div>
          <div class="text-24px font-bold mt-5px text-orange-500">{{ summary.expiring90 ?? 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="78px">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入员工姓名" clearable class="!w-200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="岗位类别" prop="postCategory">
        <el-select v-model="queryParams.postCategory" placeholder="请选择" clearable class="!w-160px">
          <el-option v-for="dict in getStrDictOptions(DICT_TYPE.HR_POST_CATEGORY)" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable class="!w-140px">
          <el-option v-for="dict in getStrDictOptions(DICT_TYPE.HR_APPOINTMENT_STATUS)" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="当前聘任" prop="isCurrent">
        <el-select v-model="queryParams.isCurrent" placeholder="请选择" clearable class="!w-120px">
          <el-option label="是" :value="1" />
          <el-option label="否" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['hr:appointment:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button type="warning" plain @click="openBatchForm" v-hasPermi="['hr:appointment:batch']">
          <Icon icon="ep:document-copy" class="mr-5px" /> 批量聘任
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading" v-hasPermi="['hr:appointment:export']">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe show-overflow-tooltip>
      <el-table-column label="姓名" prop="partnerName" min-width="100" />
      <el-table-column label="工号" prop="employeeNo" width="90" />
      <el-table-column label="部门" prop="deptName" min-width="120">
        <template #default="scope">{{ scope.row.deptName || '-' }}</template>
      </el-table-column>
      <el-table-column label="岗位类别" prop="postCategory" width="100">
        <template #default="scope"><dict-tag :type="DICT_TYPE.HR_POST_CATEGORY" :value="scope.row.postCategory" /></template>
      </el-table-column>
      <el-table-column label="岗位等级" prop="postLevel" width="100">
        <template #default="scope"><dict-tag :type="DICT_TYPE.HR_POST_LEVEL" :value="scope.row.postLevel" /></template>
      </el-table-column>
      <el-table-column label="聘任岗位" prop="postName" min-width="120" />
      <el-table-column label="起始日" prop="startDate" width="110" :formatter="dateFormatter2" />
      <el-table-column label="到期日" prop="endDate" width="110" :formatter="dateFormatter2" />
      <el-table-column label="距到期" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.daysToExpire != null && scope.row.daysToExpire <= 90 && scope.row.daysToExpire >= 0" type="warning">
            {{ scope.row.daysToExpire }} 天
          </el-tag>
          <span v-else-if="scope.row.daysToExpire != null && scope.row.daysToExpire < 0" class="text-red-500">已过期</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="90">
        <template #default="scope"><dict-tag :type="DICT_TYPE.HR_APPOINTMENT_STATUS" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="当前" prop="isCurrent" width="70">
        <template #default="scope">
          <el-tag v-if="scope.row.isCurrent === 1" type="success">是</el-tag>
          <span v-else>否</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['hr:appointment:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)" v-hasPermi="['hr:appointment:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <AppointmentForm ref="formRef" @success="getList" />
  <AppointmentBatchForm ref="batchFormRef" @success="getList" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { dateFormatter2 } from '@/utils/formatTime'
import download from '@/utils/download'
import { AppointmentApi, AppointmentVO } from '@/api/hr/appointment'
import AppointmentForm from './AppointmentForm.vue'
import AppointmentBatchForm from './AppointmentBatchForm.vue'

defineOptions({ name: 'HrAppointment' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref<AppointmentVO[]>([])
const summary = ref<{ totalActive?: number; expiring90?: number }>({})
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  postCategory: undefined,
  status: undefined,
  isCurrent: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)

const loadSummary = async () => {
  try {
    summary.value = await AppointmentApi.getExpiringSummary()
  } catch {}
}

const getList = async () => {
  loading.value = true
  try {
    const data = await AppointmentApi.getAppointmentPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
  loadSummary()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, id?: number) => formRef.value.open(type, id)

const batchFormRef = ref()
const openBatchForm = () => batchFormRef.value.open()

const handleDelete = async (row: AppointmentVO) => {
  try {
    await message.delConfirm()
    await AppointmentApi.deleteAppointment(row.id!)
    message.success(t('common.delSuccess'))
    await getList()
    loadSummary()
  } catch {}
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await AppointmentApi.exportAppointment(queryParams)
    download.excel(data, '岗位聘任.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  getList()
  loadSummary()
})
</script>
