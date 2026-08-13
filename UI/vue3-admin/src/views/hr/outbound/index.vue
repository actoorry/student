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
          placeholder="请输入员工姓名"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="外出类型" prop="recordType">
        <el-select
          v-model="queryParams.recordType"
          placeholder="请选择外出类型"
          clearable
          class="!w-200px"
        >
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
          v-hasPermi="['hr:outbound:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['hr:outbound:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
    >
      <el-table-column label="员工姓名" align="center" prop="partnerName" min-width="100px" />
      <el-table-column label="工号" align="center" prop="employeeNo" width="100px" />
      <el-table-column label="部门" align="center" prop="deptName" min-width="120px">
        <template #default="scope">{{ scope.row.deptName || '-' }}</template>
      </el-table-column>
      <el-table-column label="外出类型" align="center" prop="recordType" min-width="100px">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.HR_OUTBOUND_TYPE" :value="scope.row.recordType" />
        </template>
      </el-table-column>
      <el-table-column label="单位/地点" align="center" min-width="180px">
        <template #default="scope">
          {{ formatOutboundPlace(scope.row) }}
        </template>
      </el-table-column>
      <el-table-column label="开始日期" align="center" prop="startDate" width="110px" :formatter="dateFormatter2" />
      <el-table-column label="结束日期" align="center" prop="endDate" width="110px" :formatter="dateFormatter2" />
      <el-table-column label="天数" align="center" prop="durationDays" width="70px" />
      <el-table-column label="服务年限" align="center" prop="supportYears" width="90px">
        <template #default="scope">
          <span v-if="scope.row.recordType === 'rural_support' && scope.row.supportYears != null">
            {{ scope.row.supportYears }} 年
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="继教学分" align="center" prop="continuingEducationCredit" width="90px">
        <template #default="scope">
          <span v-if="scope.row.recordType !== 'rural_support' && scope.row.continuingEducationCredit != null">
            {{ scope.row.continuingEducationCredit }}
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="是否计入" align="center" prop="effective" width="90px">
        <template #default="scope">
          <el-tag v-if="scope.row.effective === 1" type="success">计入</el-tag>
          <el-tag v-else type="info">不计入</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" width="140px" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['hr:outbound:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row)"
            v-hasPermi="['hr:outbound:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <OutboundForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { dateFormatter, dateFormatter2 } from '@/utils/formatTime'
import download from '@/utils/download'
import { OutboundApi, OutboundVO } from '@/api/hr/outbound'
import OutboundForm from './OutboundForm.vue'

defineOptions({ name: 'HrOutbound' })

const message = useMessage()
const { t } = useI18n()
const { query } = useRoute()

const loading = ref(true)
const total = ref(0)
const list = ref<OutboundVO[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  recordType: undefined,
  effective: undefined,
  startDate: [],
  createTime: []
})
const queryFormRef = ref()
const exportLoading = ref(false)

/** 单位 + 地点（省/市/区）展示 */
const formatOutboundPlace = (row: OutboundVO) => {
  const place = [row.province, row.city, row.county].filter(Boolean).join(' / ')
  const parts = [row.organization, place].filter(Boolean)
  return parts.length ? parts.join(' · ') : '-'
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await OutboundApi.getOutboundPage(queryParams)
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
const openForm = (type: string, id?: number, presetPartnerId?: number) => {
  formRef.value.open(type, id, presetPartnerId)
}

/** 删除 */
const handleDelete = async (row: OutboundVO) => {
  try {
    await message.delConfirm()
    await OutboundApi.deleteOutbound(row.id!)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 导出 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await OutboundApi.exportOutbound(queryParams)
    download.excel(data, '外出记录.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  getList()
  // 支持从员工档案带 partnerId 跳转：自动打开新增并预填员工
  if (query.partnerId) {
    nextTick(() => {
      openForm('create', undefined, Number(query.partnerId))
    })
  }
})
</script>
