<template>
  <Dialog title="单据详情" v-model="dialogVisible" width="800px" :close-on-click-modal="false">
    <div v-loading="loading">
      <el-descriptions :column="3" border size="small">
        <el-descriptions-item label="单据编号">{{ detail.no }}</el-descriptions-item>
        <el-descriptions-item label="单据类型">{{ getOrderTypeName(detail.typeId) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <dict-tag :type="DICT_TYPE.WMS_ORDER_STATUS" :value="detail.status" />
        </el-descriptions-item>
        <el-descriptions-item label="来源">{{ formatWarehouse(detail.fromWarehouseId) }}</el-descriptions-item>
        <el-descriptions-item label="去向">{{ formatWarehouse(detail.toWarehouseId) }}</el-descriptions-item>
        <el-descriptions-item label="单据日期">{{ detail.orderTime }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.supplierId" label="供应商">{{ detail.supplierId }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.customerId" label="客户">{{ detail.customerId }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.operatorId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="3">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">明细清单</el-divider>
      <el-table :data="items" border size="small" max-height="300px" v-if="items.length > 0">
        <el-table-column label="商品" min-width="140px">
          <template #default="{ row }">{{ formatSkuName(row.skuId) }}</template>
        </el-table-column>
        <el-table-column label="来源库位" width="120px">
          <template #default="{ row }">{{ formatWarehouse(row.fromLocationId) }}</template>
        </el-table-column>
        <el-table-column label="去向库位" width="120px">
          <template #default="{ row }">{{ formatWarehouse(row.toLocationId) }}</template>
        </el-table-column>
        <el-table-column label="数量" prop="quantity" width="100px" align="right" />
        <el-table-column label="批次号" prop="batchNo" width="120px" />
        <el-table-column label="单价" prop="unitPrice" width="100px" align="right" />
        <el-table-column label="备注" prop="remark" min-width="140px" />
      </el-table>
      <el-empty v-else description="无明细数据" />
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { OrderApi, OrderItemApi } from '@/api/wms/order'
import { OrderTypeApi } from '@/api/wms/ordertype'
import { WmsProductApi } from '@/api/wms/product'
import { getVirtualWarehouseName, isVirtualWarehouse } from '../enums'
import { DICT_TYPE } from '@/utils/dict'

defineOptions({ name: 'WmsOrderDetail' })
const dialogVisible = ref(false)
const loading = ref(false)
const detail = ref<any>({})
const items = ref<any[]>([])
const orderTypeList = ref<any[]>([])
const skuNameMap = ref<Record<number, string>>({})

const formatSkuName = (id: number): string => skuNameMap.value[id] || `SKU(${id})`

const loadSkuNames = async (ids: number[]) => {
  const list = ids.filter(Boolean)
  if (list.length === 0) return
  try {
    const res = await WmsProductApi.getSkuList(list)
    ;(res || []).forEach(item => { skuNameMap.value[item.id] = item.spuName })
  } catch { }
}

const getOrderTypeName = (typeId: number) => { const t = orderTypeList.value.find(i => i.id === typeId); return t ? t.name : String(typeId) }
const formatWarehouse = (id: number | undefined) => { if (!id) return '-'; if (isVirtualWarehouse(id)) return getVirtualWarehouseName(id); return `仓库(${id})` }

const open = async (id: number) => {
  dialogVisible.value = true; loading.value = true
  try {
    if (orderTypeList.value.length === 0) { const res = await OrderTypeApi.getOrderTypePage({ pageSize: 200 }); orderTypeList.value = res.list || [] }
    detail.value = await OrderApi.getOrder(id)
    const itemRes = await OrderItemApi.getOrderItems({ orderId: id })
    items.value = itemRes.list || (Array.isArray(itemRes) ? itemRes : [])
    const skuIds = items.value.map((i: any) => i.skuId)
    await loadSkuNames(skuIds)
  } catch {} finally { loading.value = false }
}
defineExpose({ open })
</script>