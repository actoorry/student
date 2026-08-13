<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="900px" :close-on-click-modal="false">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px" v-loading="formLoading">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="单据类型" prop="typeId">
            <el-select
              v-model="formData.typeId" placeholder="选择单据类型" class="!w-full"
              @change="onTypeChange" :disabled="formType === 'update'">
              <el-option v-for="type in activeTypes" :key="type.id" :label="type.name" :value="type.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="单据编号" prop="no">
            <el-input v-model="formData.no" disabled placeholder="系统自动生成" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="单据日期" prop="orderTime">
            <el-date-picker v-model="formData.orderTime" value-format="YYYY-MM-DD HH:mm:ss"
              type="datetime" placeholder="选择日期" class="!w-full" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="来源" prop="fromWarehouseId">
            <el-select
              v-model="formData.fromWarehouseId" placeholder="选择来源" class="!w-full" clearable>
              <el-option-group label="虚拟仓">
                <el-option v-for="(name, id) in VIRTUAL_WAREHOUSE" :key="id" :label="name" :value="Number(id)" />
              </el-option-group>
              <el-option-group label="实体仓库">
                <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" :disabled="w.status === 0" />
              </el-option-group>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="去向" prop="toWarehouseId">
            <el-select
              v-model="formData.toWarehouseId" placeholder="选择去向" class="!w-full" clearable>
              <el-option-group label="虚拟仓">
                <el-option v-for="(name, id) in VIRTUAL_WAREHOUSE" :key="id" :label="name" :value="Number(id)" />
              </el-option-group>
              <el-option-group label="实体仓库">
                <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" :disabled="w.status === 0" />
              </el-option-group>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="操作人" prop="operatorId">
            <el-input v-model="formData.operatorId" placeholder="操作人ID" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20" v-if="selectedType?.needSupplier">
        <el-col :span="12">
          <el-form-item label="供应商" prop="supplierId" :required="selectedType?.needSupplier === 1">
            <el-select v-model="formData.supplierId" placeholder="选择供应商" clearable filterable class="!w-full">
              <el-option v-for="s in supplierList" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20" v-if="selectedType?.needCustomer">
        <el-col :span="12">
          <el-form-item label="客户" prop="customerId" :required="selectedType?.needCustomer === 1">
            <el-select v-model="formData.customerId" placeholder="选择客户" clearable filterable class="!w-full">
              <el-option v-for="c in customerList" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" placeholder="备注" type="textarea" :rows="2" maxlength="500" />
      </el-form-item>

      <el-divider content-position="left">单据明细</el-divider>

      <el-table :data="orderItems" row-key="tempId" border style="width: 100%" max-height="400px">
        <el-table-column label="商品名称" min-width="180px">
          <template #default="{ row }">
            <el-select
              v-model="row.skuId"
              filterable
              remote
              reserve-keyword
              clearable
              :remote-method="(q: string) => remoteSkuSearch(row, q)"
              placeholder="搜索商品名称/SKU ID"
              size="small"
              class="!w-full"
            >
              <el-option
                v-for="item in row._skuOptions || []"
                :key="item.id"
                :label="item.displayName"
                :value="item.id"
              >
                <div class="flex flex-col">
                  <span class="text-sm">{{ item.spuName }}</span>
                  <span class="text-xs text-gray-400">SKU#{{ item.id }} {{ item.barCode ? '条码:' + item.barCode : '' }}</span>
                </div>
              </el-option>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="来源库位" width="140px">
          <template #default="{ row }">
            <el-select
                v-model="row.fromLocationId" placeholder="来源" clearable size="small" class="!w-full">
              <el-option v-for="loc in locationList" :key="loc.id" :label="loc.name" :value="loc.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="去向库位" width="140px">
          <template #default="{ row }">
            <el-select
              v-model="row.toLocationId" placeholder="去向" clearable size="small" class="!w-full">
              <el-option v-for="loc in locationList" :key="loc.id" :label="loc.name" :value="loc.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="120px">
          <template #default="{ row }">
            <el-input-number
              v-model="row.quantity" :min="0" :precision="2" :controls="false" size="small" class="!w-full" />
          </template>
        </el-table-column>
        <el-table-column label="批次号" width="140px">
          <template #default="{ row }">
            <el-input v-model="row.batchNo" placeholder="批次号" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="生产日期" width="130px">
          <template #default="{ row }">
            <el-date-picker
              v-model="row.productionDate" type="date" value-format="YYYY-MM-DD" placeholder="生产日期" size="small" class="!w-full" />
          </template>
        </el-table-column>
        <el-table-column label="有效期至" width="130px">
          <template #default="{ row }">
            <el-date-picker
              v-model="row.expiryDate" type="date" value-format="YYYY-MM-DD" placeholder="有效期至" size="small" class="!w-full" />
          </template>
        </el-table-column>
        <el-table-column label="单价" width="100px">
          <template #default="{ row }">
            <el-input-number
              v-model="row.unitPrice" :min="0" :precision="2" :controls="false" size="small" class="!w-full" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80px" align="center">
          <template #default="{ $index }">
            <el-button link type="danger" size="small" @click="removeItem($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-10px">
        <el-button type="primary" plain size="small" @click="addItem">
          <Icon icon="ep:plus" /> 新增行
        </el-button>
      </div>
    </el-form>

    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">保存</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { OrderApi, OrderItemApi } from '@/api/wms/order'
import { OrderTypeApi } from '@/api/wms/ordertype'
import { WarehouseApi } from '@/api/wms/warehouse'
import { VIRTUAL_WAREHOUSE, isVirtualWarehouse } from '../enums'
import { WmsPartnerApi } from '@/api/wms/partner'
import { WmsProductApi } from '@/api/wms/product'
import dayjs from 'dayjs'

defineOptions({ name: 'WmsOrderForm' })
const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const activeTypes = ref<any[]>([])
const warehouseList = ref<any[]>([])
const locationList = ref<any[]>([])
const supplierList = ref<any[]>([])
const customerList = ref<any[]>([])
const orderItems = ref<any[]>([])
let tempIdCounter = 0

const formData = ref({
  id: undefined, no: undefined, typeId: undefined, status: 0,
  supplierId: undefined, customerId: undefined, fromWarehouseId: undefined,
  toWarehouseId: undefined, originId: undefined, operatorId: undefined,
  orderTime: undefined, remark: undefined
})

const formRules = reactive({
  typeId: [{ required: true, message: '请选择单据类型', trigger: 'change' }],
  orderTime: [{ required: true, message: '请选择单据日期', trigger: 'change' }],
  fromWarehouseId: [{ required: true, message: '请选择来源', trigger: 'change' }],
  toWarehouseId: [{ required: true, message: '请选择去向', trigger: 'change' }]
})

const selectedType = computed(() => activeTypes.value.find(t => t.id === formData.value.typeId))
const formRef = ref()

const onTypeChange = (typeId: number) => {
  const type = activeTypes.value.find(t => t.id === typeId)
  if (type) {
    if (type.fromLocationId && type.fromLocationId !== 0) formData.value.fromWarehouseId = type.fromLocationId
    if (type.toLocationId && type.toLocationId !== 0) formData.value.toWarehouseId = type.toLocationId
    if (type.name?.includes('调拨')) {
      if (type.stockImpact === 1) formData.value.fromWarehouseId = 6
      else formData.value.toWarehouseId = 6
    }
  }
}

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增单据' : '编辑单据'
  formType.value = type
  resetForm()
  orderItems.value = []
  // 新增时设置默认单据日期
  if (type === 'create') {
    formData.value.orderTime = dayjs().format('YYYY-MM-DD HH:mm:ss')
  }
  try {
    const [typeRes, whRes, supplierRes, customerRes] = await Promise.all([
      OrderTypeApi.getOrderTypePage({ pageSize: 200 }),
      WarehouseApi.getWarehousePage({ pageSize: 200 }),
      WmsPartnerApi.getSupplierList(),
      WmsPartnerApi.getCustomerList()
    ])
    activeTypes.value = (typeRes.list || []).filter((t: any) => t.active === 1)
    warehouseList.value = (whRes.list || []).filter((w: any) => !isVirtualWarehouse(w.id))
    supplierList.value = supplierRes || []
    customerList.value = customerRes || []
    // 明细行的来源/去向库位：包含虚拟仓 + 实体库位
    locationList.value = [
      ...Object.entries(VIRTUAL_WAREHOUSE).map(([id, name]) => ({ id: Number(id), name, isVirtual: true })),
      ...(whRes.list || []).filter((w: any) => !isVirtualWarehouse(w.id))
    ]
  } catch {}
  if (id) {
    formLoading.value = true
    try {
      const data = await OrderApi.getOrder(id)
      formData.value = { ...formData.value, ...data }
      const itemRes = await OrderItemApi.getOrderItems({ orderId: id })
      const itemList = itemRes.list || (Array.isArray(itemRes) ? itemRes : [])
      orderItems.value = itemList.map((item: any) => ({ ...item, tempId: ++tempIdCounter, _skuOptions: [] }))
      await loadSkuNames(orderItems.value)
    } finally { formLoading.value = false }
  }
}
defineExpose({ open })

const addItem = () => {
  orderItems.value.push({
    tempId: ++tempIdCounter, skuId: undefined, fromLocationId: undefined,
    toLocationId: undefined, quantity: 0, batchNo: undefined,
    productionDate: undefined, expiryDate: undefined, unitPrice: undefined, remark: undefined,
    _skuOptions: []
  })
}
const removeItem = (index: number) => { orderItems.value.splice(index, 1) }

/** 远程搜索 SKU */
const remoteSkuSearch = async (row: any, keyword: string) => {
  if (!keyword || keyword.length < 1) {
    row._skuOptions = []
    return
  }
  try {
    const res = await WmsProductApi.searchSku(keyword, 20)
    row._skuOptions = res || []
  } catch {
    row._skuOptions = []
  }
}

/** 编辑时加载 SKU 名称 */
const loadSkuNames = async (items: any[]) => {
  const skuIds = items.map(i => i.skuId).filter(Boolean)
  if (skuIds.length === 0) return
  try {
    const list = await WmsProductApi.getSkuList(skuIds)
    const map = new Map((list || []).map(i => [i.id, i]))
    items.forEach(item => {
      if (item.skuId && map.has(item.skuId)) {
        item._skuOptions = [map.get(item.skuId)]
      }
    })
  } catch { }
}

const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    const data = { ...formData.value, items: orderItems.value.map(({ tempId, ...rest }) => rest) } as any
    if (formType.value === 'create') { await OrderApi.createOrder(data); message.success(t('common.createSuccess')) }
    else { await OrderApi.updateOrder(data); message.success(t('common.updateSuccess')) }
    dialogVisible.value = false
    emit('success')
  } finally { formLoading.value = false }
}

const resetForm = () => {
  formData.value = {
    id: undefined, no: undefined, typeId: undefined, status: 0,
    supplierId: undefined, customerId: undefined, fromWarehouseId: undefined,
    toWarehouseId: undefined, originId: undefined, operatorId: undefined,
    orderTime: undefined, remark: undefined
  }
  formRef.value?.resetFields()
}
</script>