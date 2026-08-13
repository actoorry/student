<template>
  <!-- 搜索栏 -->
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="90px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="单据类型名称" clearable @keyup.enter="handleQuery"
          class="!w-200px" />
      </el-form-item>
      <el-form-item label="库存影响" prop="stockImpact">
        <el-select v-model="queryParams.stockImpact" placeholder="全部" clearable class="!w-140px">
          <el-option v-for="dict in getStockImpactOptions()" :key="dict.value" :label="dict.label"
            :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="active">
        <el-select v-model="queryParams.active" placeholder="全部" clearable class="!w-120px">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" /> 搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" /> 重置
        </el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['wms:order-type:create']">
          <Icon icon="ep:plus" /> 新增
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['wms:order-type:export']">
          <Icon icon="ep:download" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table row-key="id" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="编号" width="70px" align="center" prop="id" />
      <el-table-column label="名称" min-width="160px" prop="name">
        <template #default="scope">
          <span class="font-medium">{{ scope.row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="库存影响" width="150px" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.stockImpact === 1 ? 'success' : 'danger'" size="small">
            {{ getStockImpactLabel(scope.row.stockImpact) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="默认来源" width="140px" align="center">
        <template #default="scope">
          {{ formatLocation(scope.row.fromLocationId) }}
        </template>
      </el-table-column>
      <el-table-column label="默认去向" width="140px" align="center">
        <template #default="scope">
          {{ formatLocation(scope.row.toLocationId) }}
        </template>
      </el-table-column>
      <el-table-column label="强制供应商" width="100px" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.needSupplier === 1 ? 'warning' : 'info'" size="small">
            {{ scope.row.needSupplier === 1 ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="强制客户" width="100px" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.needCustomer === 1 ? 'warning' : 'info'" size="small">
            {{ scope.row.needCustomer === 1 ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80px" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.active === 1 ? 'success' : 'danger'" size="small">
            {{ scope.row.active === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" min-width="160px" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" width="180px" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)"
            v-hasPermi="['wms:order-type:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)"
            v-hasPermi="['wms:order-type:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize"
      @pagination="getList" />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <OrderTypeForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { OrderTypeApi, OrderType } from '@/api/wms/ordertype'
import OrderTypeForm from './OrderTypeForm.vue'
import { getVirtualWarehouseName, getStockImpactLabel, getStockImpactOptions } from '../enums'

/** 出入库单据类型配置 */
defineOptions({ name: 'WmsOrderType' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const exportLoading = ref(false)
const list = ref<OrderType[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  name: undefined,
  stockImpact: undefined,
  active: undefined
})
const queryFormRef = ref()

/** 格式化位置（虚拟仓名称 / 用户自选） */
const formatLocation = (id: number | undefined): string => {
  if (id === undefined || id === null) return '-'
  if (id === 0) return '用户自选'
  return getVirtualWarehouseName(id)
}

const getList = async () => {
  loading.value = true
  try {
    const data = await OrderTypeApi.getOrderTypePage(queryParams)
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
    await OrderTypeApi.deleteOrderType(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch { }
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await OrderTypeApi.exportOrderType(queryParams)
    download.excel(data, '出入库单据类型配置.xls')
  } catch { } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>