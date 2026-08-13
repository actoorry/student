<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="用户编号" prop="userId">
        <el-input v-model="queryParams.userId" class="!w-240px" clearable placeholder="请输入用户编号" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="收件人" prop="name">
        <el-input v-model="queryParams.name" class="!w-240px" clearable placeholder="请输入收件人名称" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="queryParams.mobile" class="!w-240px" clearable placeholder="请输入手机号" @keyup.enter="handleQuery" />
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
      <el-table-column align="center" label="收件人" prop="name" width="100px" />
      <el-table-column align="center" label="手机号" prop="mobile" width="120px" />
      <el-table-column align="center" label="详细地址" prop="detailAddress" min-width="200px" />
      <el-table-column align="center" label="邮编" prop="postCode" width="80px" />
      <el-table-column align="center" label="默认地址" prop="defaulted" width="100px">
        <template #default="scope">
          <el-tag :type="scope.row.defaulted ? 'success' : 'info'" size="small">
            {{ scope.row.defaulted ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :formatter="dateFormatter" align="center" label="创建时间" prop="createTime" width="180px" />
    </el-table>
    <Pagination v-model:limit="queryParams.pageSize" v-model:page="queryParams.pageNo" :total="total" @pagination="getList" />
  </ContentWrap>
</template>
<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as PartnerAddressApi from '@/api/partner/address'

defineOptions({ name: 'MemberAddress' })

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({ pageNo: 1, pageSize: 10, userId: null, name: null, mobile: null })
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await PartnerAddressApi.getAddressPage(queryParams)
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
