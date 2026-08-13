<template>
  <ContentWrap>
    <!-- 搜索工作栏：职工端无姓名搜索 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="78px"
    >
      <el-form-item label="外出类型" prop="recordType">
        <el-select v-model="queryParams.recordType" placeholder="请选择外出类型" clearable class="!w-200px">
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.HR_OUTBOUND_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="是否计入" prop="effective">
        <el-select v-model="queryParams.effective" placeholder="请选择" clearable class="!w-160px">
          <el-option label="计入" :value="1" />
          <el-option label="不计入" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始日期" prop="startDate">
        <el-date-picker
          v-model="queryParams.startDate"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['hr:my:outbound:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增外出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="外出类型" align="center" prop="recordType" min-width="110px">
        <template #default="scope">
          <DictTag :type="DICT_TYPE.HR_OUTBOUND_TYPE" :value="scope.row.recordType" />
        </template>
      </el-table-column>
      <el-table-column label="部门" align="center" prop="deptName" min-width="110px">
        <template #default="scope">{{ scope.row.deptName || '-' }}</template>
      </el-table-column>
      <el-table-column label="单位" align="center" prop="organization" min-width="140px" />
      <el-table-column label="开始日期" align="center" prop="startDate" min-width="110px" :formatter="dateFormatter" />
      <el-table-column label="结束日期" align="center" prop="endDate" min-width="110px" :formatter="dateFormatter" />
      <el-table-column label="天数" align="center" prop="durationDays" min-width="80px" />
      <el-table-column label="继教学分" align="center" prop="continuingEducationCredit" min-width="90px" />
      <el-table-column label="是否计入" align="center" prop="effective" min-width="90px">
        <template #default="scope">
          <el-tag :type="scope.row.effective === 1 ? 'success' : 'info'">
            {{ scope.row.effective === 1 ? '计入' : '不计入' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" min-width="120px" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['hr:my:outbound:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['hr:my:outbound:delete']">删除</el-button>
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

  <!-- 表单弹窗 -->
  <HrMyOutboundForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import { MyOutboundApi } from '@/api/hr/my/outbound'
import type { OutboundVO } from '@/api/hr/outbound'
import HrMyOutboundForm from './HrMyOutboundForm.vue'

/** 职工我的外出申请 列表 */
defineOptions({ name: 'HrMyOutbound' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<OutboundVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  recordType: undefined,
  effective: undefined,
  startDate: []
})
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await MyOutboundApi.getMyOutboundPage(queryParams)
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
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await MyOutboundApi.deleteMyOutbound(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

onMounted(getList)
</script>
