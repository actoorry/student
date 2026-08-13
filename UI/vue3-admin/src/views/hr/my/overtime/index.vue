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
      <el-form-item label="加班类型" prop="overtimeType">
        <el-select v-model="queryParams.overtimeType" placeholder="请选择加班类型" clearable class="!w-200px">
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.HR_OVERTIME_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="加班原因" prop="overtimeReason">
        <el-select v-model="queryParams.overtimeReason" placeholder="请选择加班原因" clearable class="!w-200px">
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.HR_OVERTIME_REASON)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="加班日期" prop="overtimeDate">
        <el-date-picker
          v-model="queryParams.overtimeDate"
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
          v-hasPermi="['hr:my:overtime:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增加班
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="加班类型" align="center" prop="overtimeType" min-width="110px">
        <template #default="scope">
          <DictTag :type="DICT_TYPE.HR_OVERTIME_TYPE" :value="scope.row.overtimeType" />
        </template>
      </el-table-column>
      <el-table-column label="加班日期" align="center" prop="overtimeDate" min-width="110px" :formatter="dateFormatter2" />
      <el-table-column label="加班原因" align="center" prop="overtimeReason" min-width="130px">
        <template #default="scope">
          <DictTag :type="DICT_TYPE.HR_OVERTIME_REASON" :value="scope.row.overtimeReason" />
        </template>
      </el-table-column>
      <el-table-column label="开始时间" align="center" prop="startTime" min-width="160px" :formatter="dateFormatter" />
      <el-table-column label="结束时间" align="center" prop="endTime" min-width="160px" :formatter="dateFormatter" />
      <el-table-column label="时长(小时)" align="center" prop="durationHours" min-width="100px" />
      <el-table-column label="值班科室" align="center" prop="workDeptName" min-width="110px">
        <template #default="scope">{{ scope.row.workDeptName || '-' }}</template>
      </el-table-column>
      <el-table-column label="值班地点" align="center" prop="workLocation" min-width="110px">
        <template #default="scope">{{ scope.row.workLocation || '-' }}</template>
      </el-table-column>
      <el-table-column label="节假日" align="center" prop="holidayName" min-width="110px">
        <template #default="scope">{{ scope.row.holidayName || '-' }}</template>
      </el-table-column>
      <el-table-column label="工作内容" align="center" prop="workSummary" min-width="160px" :show-overflow-tooltip="true" />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" min-width="120px" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['hr:my:overtime:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['hr:my:overtime:delete']">删除</el-button>
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
  <HrMyOvertimeForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { dateFormatter, dateFormatter2 } from '@/utils/formatTime'
import { MyOvertimeApi } from '@/api/hr/my/overtime'
import type { Overtime } from '@/api/hr/overtime'
import HrMyOvertimeForm from './HrMyOvertimeForm.vue'

/** 职工我的加班登记 列表 */
defineOptions({ name: 'HrMyOvertime' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<Overtime[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  overtimeType: undefined,
  overtimeReason: undefined,
  overtimeDate: []
})
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await MyOvertimeApi.getMyOvertimePage(queryParams)
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
    await MyOvertimeApi.deleteMyOvertime(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

onMounted(getList)
</script>
