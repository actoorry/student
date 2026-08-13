<template>
  <ContentWrap>
    <!-- 搜索工作栏：职工端仅按合同编号/甲方/合同状态筛选，无姓名搜索 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
      <el-form-item label="合同编号" prop="contractNo">
        <el-input
          v-model="queryParams.contractNo"
          placeholder="请输入合同编号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="甲方" prop="partyA">
        <el-input
          v-model="queryParams.partyA"
          placeholder="请输入甲方"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="合同状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable class="!w-160px">
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.HR_WORKING_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="合同编号" align="center" prop="contractNo" min-width="130px" />
      <el-table-column label="甲方" align="center" prop="partyA" min-width="120px" />
      <el-table-column label="部门" align="center" prop="deptName" min-width="110px">
        <template #default="scope">{{ scope.row.deptName || '-' }}</template>
      </el-table-column>
      <el-table-column label="合同开始" align="center" prop="startTime" min-width="160px" :formatter="dateFormatter" />
      <el-table-column label="合同结束" align="center" prop="endTime" min-width="160px" :formatter="dateFormatter" />
      <el-table-column label="合同状态" align="center" prop="status" min-width="100px">
        <template #default="scope">
          <DictTag :type="DICT_TYPE.HR_WORKING_TYPE" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" min-width="160px" :formatter="dateFormatter" />
      <el-table-column label="操作" align="center" min-width="120px" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['hr:my:contract:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['hr:my:contract:delete']">删除</el-button>
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

  <!-- 表单弹窗：添加/修改 -->
  <HrMyContractForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import { MyContractApi } from '@/api/hr/my/contract'
import type { Contract } from '@/api/hr/contract'
import HrMyContractForm from './HrMyContractForm.vue'

/** 职工我的合同 列表 */
defineOptions({ name: 'HrMyContract' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<Contract[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  contractNo: undefined,
  partyA: undefined,
  status: undefined
})
const queryFormRef = ref()

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await MyContractApi.getMyContractPage(queryParams)
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

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await MyContractApi.deleteMyContract(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

onMounted(getList)
</script>
