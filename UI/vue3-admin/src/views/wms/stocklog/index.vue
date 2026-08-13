<template>
  <!-- 搜索栏 -->
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="SKU" prop="skuId">
        <el-input v-model="queryParams.skuId" placeholder="SKUID或者产品名称" clearable @keyup.enter="handleQuery"
          class="!w-150px" />
      </el-form-item>
      <el-form-item label="仓库" prop="warehouseId">
        <el-select v-model="queryParams.warehouseId" placeholder="全部" clearable class="!w-160px">
          <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="业务类型" prop="bizType">
        <el-select v-model="queryParams.bizType" placeholder="全部" clearable class="!w-140px">
          <el-option label="出入库单" value="order" />
          <el-option label="盘点调整单" value="adjust" />
        </el-select>
      </el-form-item>
      <el-form-item label="单据编号" prop="bizNo">
        <el-input v-model="queryParams.bizNo" placeholder="业务单据编号" clearable @keyup.enter="handleQuery"
          class="!w-160px" />
      </el-form-item>
      <el-form-item label="日期" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" value-format="YYYY-MM-DD HH:mm:ss" type="daterange"
          start-placeholder="开始" end-placeholder="结束" class="!w-220px" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" /> 搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" /> 重置
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['wms:stock-log:export']">
          <Icon icon="ep:download" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表（纯查询，无新增编辑删除按钮） -->
  <ContentWrap>
    <el-table row-key="id" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="时间" width="160px" prop="createTime" :formatter="dateFormatter" sortable="custom" />
      <el-table-column label="产品" min-width="140px" align="center"><template #default="scope">{{
        formatSkuName(scope.row.skuId) }}</template></el-table-column>
      <el-table-column label="仓库" width="120px" align="center">
        <template #default="scope">{{ getWarehouseName(scope.row.warehouseId) }}</template>
      </el-table-column>
      <el-table-column label="来源" width="120px" align="center">
        <template #default="scope">{{ formatLocation(scope.row.fromLocationId) }}</template>
      </el-table-column>
      <el-table-column label="去向" width="120px" align="center">
        <template #default="scope">{{ formatLocation(scope.row.toLocationId) }}</template>
      </el-table-column>
      <el-table-column label="业务类型" width="100px" align="center">
        <template #default="scope">
          <el-tag size="small">{{ scope.row.bizType === 'order' ? '出入库单' : '盘点调整单' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="业务单据编号" width="150px" align="center" prop="bizNo" />
      <el-table-column label="变动前" width="100px" align="right" prop="beforeQuantity" />
      <el-table-column label="变动量" width="100px" align="right">
        <template #default="scope">
          <span :class="scope.row.changeQuantity >= 0 ? 'text-success' : 'text-danger'">
            {{ scope.row.changeQuantity >= 0 ? '+' : '' }}{{ scope.row.changeQuantity }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="变动后" width="100px" align="right" prop="afterQuantity" />
      <el-table-column label="批次号" width="140px" prop="batchNo" />
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize"
      @pagination="getList" />
  </ContentWrap>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { StockLogApi } from '@/api/wms/stocklog'
import { WmsProductApi } from '@/api/wms/product'
import { WarehouseApi } from '@/api/wms/warehouse'
import { isVirtualWarehouse, getVirtualWarehouseName } from '../enums'

/** 库存流水台账（纯查询） */
defineOptions({ name: 'WmsStockLog' })

const message = useMessage()

const loading = ref(true)
const exportLoading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const skuNameMap = ref<Record<number, string>>({})

const formatSkuName = (id: number): string => skuNameMap.value[id] || `SKU(${id})`

const loadSkuNames = async (ids: number[]) => {
  const list = ids.filter(Boolean)
  if (list.length === 0) return
  try {
    const res = await WmsProductApi.getSkuList(list)
      ; (res || []).forEach(item => { skuNameMap.value[item.id] = item.spuName })
  } catch { }
}
const warehouseList = ref<any[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  skuId: undefined,
  warehouseId: undefined,
  bizType: undefined,
  bizNo: undefined,
  createTime: []
})
const queryFormRef = ref()

/** 获取仓库名称 */
const getWarehouseName = (id: number): string => {
  if (isVirtualWarehouse(id)) return getVirtualWarehouseName(id)
  const w = warehouseList.value.find(item => item.id === id)
  return w ? w.name : String(id)
}

/** 格式化位置 */
const formatLocation = (id: number | undefined): string => {
  if (!id) return '-'
  if (isVirtualWarehouse(id)) return getVirtualWarehouseName(id)
  const w = warehouseList.value.find(item => item.id === id)
  return w ? w.name : String(id)
}

/** 加载仓库列表 */
const loadWarehouses = async () => {
  try {
    const res = await WarehouseApi.getWarehousePage({ pageSize: 200 })
    warehouseList.value = res.list || []
  } catch { }
}

const getList = async () => {
  loading.value = true
  try {
    const data = await StockLogApi.getStockLogPage(queryParams)
    list.value = data.list
    total.value = data.total
    const skuIds = (data.list || []).map((i: any) => i.skuId)
    await loadSkuNames(skuIds)
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

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await StockLogApi.exportStockLog(queryParams)
    download.excel(data, '库存流水台账.xls')
  } catch { } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  loadWarehouses()
  getList()
})
</script>

<style scoped>
:deep(.text-success) {
  color: #67c23a;
}

:deep(.text-danger) {
  color: #f56c6c;
}
</style>