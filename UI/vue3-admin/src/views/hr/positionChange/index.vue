<template>
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="78px">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入员工姓名" clearable class="!w-200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="异动类型" prop="changeType">
        <el-select v-model="queryParams.changeType" placeholder="请选择" clearable class="!w-160px">
          <el-option v-for="dict in getStrDictOptions(DICT_TYPE.HR_POSITION_CHANGE_TYPE)" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['hr:position-change:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading" v-hasPermi="['hr:position-change:export']">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe show-overflow-tooltip>
      <el-table-column label="姓名" prop="partnerName" min-width="100" />
      <el-table-column label="工号" prop="employeeNo" width="90" />
      <el-table-column label="异动类型" prop="changeType" width="100">
        <template #default="scope"><dict-tag :type="DICT_TYPE.HR_POSITION_CHANGE_TYPE" :value="scope.row.changeType" /></template>
      </el-table-column>
      <el-table-column label="原部门" prop="fromDeptName" min-width="110">
        <template #default="scope">{{ scope.row.fromDeptName || '-' }}</template>
      </el-table-column>
      <el-table-column label="新部门" prop="toDeptName" min-width="110">
        <template #default="scope">{{ scope.row.toDeptName || '-' }}</template>
      </el-table-column>
      <el-table-column label="原岗位" prop="fromPost" min-width="100" />
      <el-table-column label="新岗位" prop="toPost" min-width="100" />
      <el-table-column label="开始日期" prop="startDate" width="110" :formatter="dateFormatter2" />
      <el-table-column label="结束日期" prop="endDate" width="110" :formatter="dateFormatter2" />
      <el-table-column label="同步档案" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.syncEmployee === 1" type="success">是</el-tag>
          <span v-else>否</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['hr:position-change:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)" v-hasPermi="['hr:position-change:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <PositionChangeForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { dateFormatter2 } from '@/utils/formatTime'
import download from '@/utils/download'
import { PositionChangeApi, PositionChangeVO } from '@/api/hr/positionChange'
import PositionChangeForm from './PositionChangeForm.vue'

defineOptions({ name: 'HrPositionChange' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref<PositionChangeVO[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  changeType: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)

const getList = async () => {
  loading.value = true
  try {
    const data = await PositionChangeApi.getPositionChangePage(queryParams)
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

const formRef = ref()
const openForm = (type: string, id?: number) => formRef.value.open(type, id)

const handleDelete = async (row: PositionChangeVO) => {
  try {
    await message.delConfirm()
    await PositionChangeApi.deletePositionChange(row.id!)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await PositionChangeApi.exportPositionChange(queryParams)
    download.excel(data, '岗位异动.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => getList())
</script>
