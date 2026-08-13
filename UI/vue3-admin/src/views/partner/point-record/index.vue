<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="用户编号" prop="userId">
        <el-input v-model="queryParams.userId" class="!w-240px" clearable placeholder="请输入用户编号" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="业务类型" prop="bizType">
        <el-select v-model="queryParams.bizType" class="!w-240px" clearable placeholder="请选择业务类型">
          <el-option v-for="dict in bizTypeOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon class="mr-5px" icon="ep:search" />搜索</el-button>
        <el-button @click="resetQuery"><Icon class="mr-5px" icon="ep:refresh" />重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :show-overflow-tooltip="true" :stripe="true">
      <el-table-column align="center" label="编号" prop="id" width="80px" />
      <el-table-column align="center" label="用户编号" prop="userId" width="100px" />
      <el-table-column align="center" label="业务编码" prop="bizId" width="120px" />
      <el-table-column align="center" label="业务类型" prop="bizType" width="100px">
        <template #default="scope">
          {{ bizTypeOptions.find(item => item.value === scope.row.bizType)?.label || scope.row.bizType }}
        </template>
      </el-table-column>
      <el-table-column align="center" label="标题" prop="title" width="150px" />
      <el-table-column align="center" label="描述" prop="description" width="200px" />
      <el-table-column align="center" label="积分变动" prop="point" width="100px" />
      <el-table-column align="center" label="变动后积分" prop="totalPoint" width="100px" />
      <el-table-column :formatter="dateFormatter" align="center" label="创建时间" prop="createTime" width="180px" />
    </el-table>
    <Pagination v-model:limit="queryParams.pageSize" v-model:page="queryParams.pageNo" :total="total" @pagination="getList" />
  </ContentWrap>
</template>
<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as PartnerPointRecordApi from '@/api/partner/point-record'

defineOptions({ name: 'MemberPointRecord' })

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({ pageNo: 1, pageSize: 10, userId: null, bizType: null })
const queryFormRef = ref()

const bizTypeOptions = [
  { value: 1, label: '签到' },
  { value: 2, label: '管理员修改' },
  { value: 11, label: '订单积分抵扣' },
  { value: 12, label: '订单积分抵扣(整单取消)' },
  { value: 13, label: '订单积分抵扣(单个退款)' },
  { value: 21, label: '订单积分奖励' },
  { value: 22, label: '订单积分奖励(整单取消)' },
  { value: 23, label: '订单积分奖励(单个退款)' }
]

const getList = async () => {
  loading.value = true
  try {
    const data = await PartnerPointRecordApi.getPointRecordPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}
const handleQuery = () => { queryParams.pageNo = 1; getList() }
const resetQuery = () => { queryFormRef.value.resetFields(); handleQuery() }

onMounted(() => { getList() })
</script>
