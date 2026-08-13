<template>
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="78px">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="queryParams.name" placeholder="推送目标姓名" clearable class="!w-200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="通知类型" prop="notifyType">
        <el-select v-model="queryParams.notifyType" placeholder="请选择" clearable class="!w-160px">
          <el-option label="到期前90天" value="expire_90" />
        </el-select>
      </el-form-item>
      <el-form-item label="推送状态" prop="pushStatus">
        <el-select v-model="queryParams.pushStatus" placeholder="请选择" clearable class="!w-140px">
          <el-option label="成功" value="success" />
          <el-option label="失败" value="failed" />
          <el-option label="跳过" value="skipped" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe show-overflow-tooltip>
      <el-table-column label="推送时间" prop="notifyTime" min-width="160" :formatter="dateFormatter" />
      <el-table-column label="目标" min-width="120">
        <template #default="scope">{{ scope.row.targetName || '-' }}</template>
      </el-table-column>
      <el-table-column label="员工" prop="partnerName" min-width="100" />
      <el-table-column label="聘任岗位" prop="postName" min-width="120" />
      <el-table-column label="通知类型" width="110">
        <template #default="scope">{{ scope.row.notifyType === 'expire_90' ? '到期前90天' : scope.row.notifyType }}</template>
      </el-table-column>
      <el-table-column label="参考到期日" prop="referenceDate" width="110" />
      <el-table-column label="状态" prop="pushStatus" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.pushStatus === 'success' ? 'success' : scope.row.pushStatus === 'failed' ? 'danger' : 'info'">
            {{ scope.row.pushStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="快照" prop="contentSnapshot" min-width="200" />
      <el-table-column label="失败原因" prop="errorMsg" min-width="120" />
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { AppointmentApi, AppointmentNotifyLogVO } from '@/api/hr/appointment'

defineOptions({ name: 'HrAppointmentNotifyLog' })

const loading = ref(true)
const total = ref(0)
const list = ref<AppointmentNotifyLogVO[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  notifyType: undefined,
  pushStatus: undefined,
  notifyTime: []
})
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await AppointmentApi.getNotifyLogPage(queryParams)
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

onMounted(() => getList())
</script>
