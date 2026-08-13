<template>
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="单据编号" prop="no">
        <el-input v-model="queryParams.no" placeholder="单据编号" clearable @keyup.enter="handleQuery" class="!w-180px" />
      </el-form-item>
      <el-form-item label="单据类型" prop="typeId">
        <el-select v-model="queryParams.typeId" placeholder="全部" clearable class="!w-160px">
          <el-option v-for="t in orderTypeList" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable class="!w-120px">
          <el-option v-for="dict in getOrderStatusOptions()" :key="dict.value" :label="dict.label"
            :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="单据日期" prop="orderTime">
        <el-date-picker v-model="queryParams.orderTime" value-format="YYYY-MM-DD HH:mm:ss" type="daterange"
          start-placeholder="开始" end-placeholder="结束" class="!w-220px" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" /> 搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" /> 重置
        </el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['wms:order:create']">
          <Icon icon="ep:plus" /> 新增
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['wms:order:export']">
          <Icon icon="ep:download" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table row-key="id" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="单据编号" width="180px" align="center" prop="no" />
      <el-table-column label="单据类型" width="120px" align="center">
        <template #default="scope">{{ getOrderTypeName(scope.row.typeId) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100px" align="center">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.WMS_ORDER_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="来源" width="120px" align="center">
        <template #default="scope">{{ formatWarehouse(scope.row.fromWarehouseId) }}</template>
      </el-table-column>
      <el-table-column label="去向" width="120px" align="center">
        <template #default="scope">{{ formatWarehouse(scope.row.toWarehouseId) }}</template>
      </el-table-column>
      <el-table-column label="供应商" width="120px" align="center">
        <template #default="scope">{{ getPartnerName(scope.row.supplierId) }}</template>
      </el-table-column>
      <el-table-column label="客户" width="120px" align="center">
        <template #default="scope">{{ getPartnerName(scope.row.customerId) }}</template>
      </el-table-column>
      <el-table-column label="单据日期" width="160px" prop="orderTime" :formatter="dateFormatter" />
      <el-table-column label="备注" min-width="160px" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" width="280px" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openDetail(scope.row)">详情</el-button>
          <el-button v-if="scope.row.status === 0 || scope.row.status === 1" link type="primary"
            @click="openForm('update', scope.row.id)" v-hasPermi="['wms:order:update']">编辑</el-button>
          <el-button v-if="scope.row.status === 0" link type="primary" @click="handleSubmit(scope.row.id)"
            v-hasPermi="['wms:order:finish']">接收</el-button>
          <el-button v-if="scope.row.status === 1" link type="primary" @click="handleApprove(scope.row.id)"
            v-hasPermi="['wms:order:finish']">审核</el-button>
          <el-button v-if="scope.row.status === 2" link type="primary" @click="handleFinish(scope.row.id)"
            v-hasPermi="['wms:order:finish']">完成</el-button>
          <el-button v-if="scope.row.status === 2" link type="primary" @click="handleReverse(scope.row.id)"
            v-hasPermi="['wms:order:create']">退货</el-button>
          <el-button v-if="scope.row.status === 0 || scope.row.status === 1" link type="danger"
            @click="handleCancel(scope.row.id)" v-hasPermi="['wms:order:finish']">取消</el-button>
          <el-button v-if="scope.row.status === 0 || scope.row.status === -1" link type="danger"
            @click="handleDelete(scope.row.id)" v-hasPermi="['wms:order:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize"
      @pagination="getList" />
  </ContentWrap>

  <OrderForm ref="formRef" @success="getList" />
  <OrderDetail ref="detailRef" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { OrderApi } from '@/api/wms/order'
import { OrderTypeApi } from '@/api/wms/ordertype'
import { WarehouseApi } from '@/api/wms/warehouse'
import OrderForm from './OrderForm.vue'
import OrderDetail from './OrderDetail.vue'
import { WmsPartnerApi } from '@/api/wms/partner'
import { ORDER_STATUS, getOrderStatusOptions, getVirtualWarehouseName, isVirtualWarehouse } from '../enums'
import { DICT_TYPE } from '@/utils/dict'

defineOptions({ name: 'WmsOrder' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const exportLoading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const orderTypeList = ref<any[]>([])
const warehouseList = ref<any[]>([])
const partnerMap = ref<Record<number, string>>({})
const queryParams = reactive({
  pageNo: 1, pageSize: 20,
  no: undefined, typeId: undefined, status: undefined, orderTime: []
})
const queryFormRef = ref()

const getOrderTypeName = (typeId: number) => {
  const t = orderTypeList.value.find(i => i.id === typeId)
  return t ? t.name : String(typeId)
}

const formatWarehouse = (id: number | undefined) => {
  if (!id) return '-'
  if (isVirtualWarehouse(id)) return getVirtualWarehouseName(id)
  const w = warehouseList.value.find((i: any) => i.id === id)
  return w ? w.name : `仓库(${id})`
}

const loadOrderTypes = async () => {
  try {
    const res = await OrderTypeApi.getOrderTypePage({ pageSize: 200 })
    orderTypeList.value = res.list || []
  } catch { }
}

const loadWarehouses = async () => {
  try {
    const res = await WarehouseApi.getWarehousePage({ pageSize: 200 })
    warehouseList.value = res.list || []
  } catch { }
}

const getPartnerName = (id: number | undefined) => {
  if (!id) return '-'
  return partnerMap.value[id] || `客商(${id})`
}

const loadPartners = async () => {
  try {
    const [suppliers, customers] = await Promise.all([
      WmsPartnerApi.getSupplierList(),
      WmsPartnerApi.getCustomerList()
    ])
    const map: Record<number, string> = {}
      ;[...(suppliers || []), ...(customers || [])].forEach(p => { map[p.id] = p.name })
    partnerMap.value = map
  } catch { }
}

const getList = async () => {
  loading.value = true
  try {
    const data = await OrderApi.getOrderPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.pageNo = 1; getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery() }

const formRef = ref()
const openForm = (type: string, id?: number) => { formRef.value.open(type, id) }

const detailRef = ref()
const openDetail = (row: any) => { detailRef.value.open(row.id) }

const handleSubmit = async (id: number) => {
  try {
    await message.confirm('确认提交该单据？')
    await OrderApi.submitOrder(id)
    message.success('提交成功')
    await getList()
  } catch { }
}

const handleApprove = async (id: number) => {
  try {
    await message.confirm('确认审核通过？审核后将更新库存。')
    await OrderApi.approveOrder(id)
    message.success('审核成功')
    // 安全库存上下限预警不再弹窗，改由「实时库存查询」页按当前库存持续展示
    await getList()
  } catch { }
}

const handleFinish = async (id: number) => {
  try {
    await message.confirm('确认完成该单据？')
    await OrderApi.finishOrder(id)
    message.success('已完成')
    await getList()
  } catch { }
}

const handleCancel = async (id: number) => {
  try {
    await message.confirm('确认取消该单据？')
    await OrderApi.cancelOrder(id)
    message.success('已取消')
    await getList()
  } catch { }
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await OrderApi.deleteOrder(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch { }
}

const handleReverse = async (id: number) => {
  try {
    await message.confirm('确认生成退货单？将创建反向单据。')
    await OrderApi.reverseOrder(id)
    message.success('退货单已生成')
    await getList()
  } catch { }
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await OrderApi.exportOrder(queryParams)
    download.excel(data, '出入库单据.xls')
  } catch { } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  loadOrderTypes()
  loadWarehouses()
  loadPartners()
  getList()
})
</script>