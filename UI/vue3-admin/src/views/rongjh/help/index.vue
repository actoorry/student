<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="90px">
      <el-form-item label="困难人" prop="name">
        <el-input v-model="queryParams.name" class="!w-180px" clearable placeholder="困难人姓名" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="联系电话" prop="phone">
        <el-input v-model="queryParams.phone" class="!w-180px" clearable placeholder="困难人电话" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="申请人" prop="mobile">
        <el-input v-model="queryParams.mobile" class="!w-180px" clearable placeholder="会员手机号" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item v-if="isAllPage" label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-150px" clearable placeholder="全部">
          <el-option v-for="item in HELP_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon class="mr-5px" icon="ep:search" />搜索</el-button>
        <el-button @click="resetQuery"><Icon class="mr-5px" icon="ep:refresh" />重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-tabs v-if="!isAllPage" v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="待审核" name="0" />
      <el-tab-pane label="已通过" name="1" />
      <el-tab-pane label="已驳回" name="2" />
      <el-tab-pane label="已撤销" name="3" />
    </el-tabs>

    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="编号" prop="id" width="80" />
      <el-table-column align="center" label="申请人" prop="nickname" min-width="100" />
      <el-table-column align="center" label="申请人手机" prop="mobile" width="120" />
      <el-table-column align="center" label="困难人" prop="name" min-width="100" />
      <el-table-column align="center" label="联系电话" prop="phone" width="120" />
      <el-table-column align="center" label="困难原因" prop="reason" min-width="180" show-overflow-tooltip />
      <el-table-column align="center" label="申请金额" prop="applyAmount" width="100">
        <template #default="{ row }">{{ formatAmount(row.applyAmount) }}</template>
      </el-table-column>
      <el-table-column align="center" label="批准金额" prop="actualAmount" width="100">
        <template #default="{ row }">{{ formatAmount(row.actualAmount) }}</template>
      </el-table-column>
      <el-table-column align="center" label="状态" prop="status" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :formatter="dateFormatter" align="center" label="申请时间" prop="createTime" width="170" />
      <el-table-column align="center" label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-hasPermi="['rongjh:help:query']" link type="primary" @click="openDetail(row.id)">详情</el-button>
          <el-button
            v-if="row.status === 0"
            v-hasPermi="['rongjh:help:audit']"
            link
            type="success"
            @click="openAudit('approve', row.id)"
          >
            通过
          </el-button>
          <el-button
            v-if="row.status === 0"
            v-hasPermi="['rongjh:help:audit']"
            link
            type="warning"
            @click="openAudit('reject', row.id)"
          >
            驳回
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination v-model:limit="queryParams.pageSize" v-model:page="queryParams.pageNo" :total="total" @pagination="getList" />
  </ContentWrap>

  <HelpDetail ref="detailRef" />
  <HelpAuditForm ref="auditRef" @success="getList" />
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as HelpApi from '@/api/rongjh/help'
import { HELP_STATUS_OPTIONS } from '@/api/rongjh/help'
import HelpDetail from './HelpDetail.vue'
import HelpAuditForm from './HelpAuditForm.vue'
import { useRoute } from 'vue-router'

defineOptions({ name: 'RongjhHelp' })

const route = useRoute()
const isAllPage = computed(() => route.name === 'RongjhHelpAll')
const loading = ref(true)
const total = ref(0)
const list = ref<HelpApi.HelpVO[]>([])
const activeTab = ref('0')
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined as string | undefined,
  phone: undefined as string | undefined,
  mobile: undefined as string | undefined,
  status: 0 as number | undefined
})
const queryFormRef = ref()
const detailRef = ref()
const auditRef = ref()

const statusTagType = (status?: number) => {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  if (status === 3) return 'info'
  return 'warning'
}

const formatAmount = (amount?: number) => {
  if (amount === undefined || amount === null) return '-'
  return `¥${Number(amount).toFixed(2)}`
}

const getList = async () => {
  loading.value = true
  try {
    const data = await HelpApi.getHelpPage(queryParams)
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
  queryFormRef.value.resetFields()
  if (!isAllPage.value) {
    queryParams.status = Number(activeTab.value)
  } else {
    queryParams.status = undefined
  }
  handleQuery()
}

const handleTabChange = (name: string | number) => {
  queryParams.status = Number(name)
  queryParams.pageNo = 1
  getList()
}

const openDetail = (id: number) => {
  detailRef.value.open(id)
}

const openAudit = (type: 'approve' | 'reject', id: number) => {
  auditRef.value.open(type, id)
}

onMounted(() => {
  if (route.meta.query) {
    if (route.meta.query.status !== undefined && !isAllPage.value) {
      activeTab.value = String(route.meta.query.status)
      queryParams.status = Number(route.meta.query.status)
    }
  }
  if (isAllPage.value) {
    queryParams.status = undefined
  }
  getList()
})
</script>
