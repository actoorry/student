<template>
  <!-- 搜索栏 -->
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="90px">
      <el-form-item label="单据ID" prop="orderId">
        <el-input v-model="queryParams.orderId" placeholder="出入库单ID" clearable @keyup.enter="handleQuery"
          class="!w-160px" />
      </el-form-item>
      <el-form-item label="SKU" prop="skuId">
        <el-input v-model="queryParams.skuId" placeholder="SKUID或者产品名称" clearable @keyup.enter="handleQuery"
          class="!w-150px" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" /> 搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" /> 重置
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table row-key="id" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="单据ID" width="80px" align="center" prop="orderId" />
      <el-table-column label="产品" min-width="140px" align="center"><template #default="scope">{{
        formatSkuName(scope.row.skuId) }}</template></el-table-column>
      <el-table-column label="来源库位" width="120px" align="center">
        <template #default="scope">{{ formatLocation(scope.row.fromLocationId) }}</template>
      </el-table-column>
      <el-table-column label="去向库位" width="120px" align="center">
        <template #default="scope">{{ formatLocation(scope.row.toLocationId) }}</template>
      </el-table-column>
      <el-table-column label="数量" width="100px" align="right" prop="quantity" />
      <el-table-column label="批次号" width="140px" prop="batchNo" />
      <el-table-column label="生产日期" width="110px" prop="productionDate" :formatter="dateFormatter" />
      <el-table-column label="有效期至" width="110px" prop="expiryDate" :formatter="dateFormatter" />
      <el-table-column label="单价" width="100px" align="right" prop="unitPrice" />
      <el-table-column label="备注" min-width="160px" prop="remark" show-overflow-tooltip />
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize"
      @pagination="getList" />
  </ContentWrap>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { OrderItemApi } from '@/api/wms/order'
import { WmsProductApi } from '@/api/wms/product'
import { WarehouseApi } from '@/api/wms/warehouse'
import { isVirtualWarehouse, getVirtualWarehouseName } from '../enums'

/** 出入库单据明细查询 */
defineOptions({ name: 'WmsOrderItem' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
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
  orderId: undefined,
  skuId: undefined
})
const queryFormRef = ref()

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
    const data = await OrderItemApi.getOrderItems(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
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

onMounted(() => {
  loadWarehouses()
  getList()
})
</script>