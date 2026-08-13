<template>
  <!-- 搜索栏 -->
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="SKU" prop="skuId">
        <el-input v-model="queryParams.skuId" placeholder="SKUID或者产品名称" clearable @keyup.enter="handleQuery"
          class="!w-160px" />
      </el-form-item>
      <el-form-item label="仓库" prop="warehouseId">
        <el-select v-model="queryParams.warehouseId" placeholder="全部" clearable class="!w-160px">
          <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="库位" prop="locationId">
        <el-select v-model="queryParams.locationId" placeholder="全部" clearable class="!w-160px" filterable>
          <el-option v-for="w in locationList" :key="w.id" :label="w.name" :value="w.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="批次" prop="batchNo">
        <el-input v-model="queryParams.batchNo" placeholder="批次号" clearable @keyup.enter="handleQuery"
          class="!w-160px" />
      </el-form-item>
      <el-form-item label="有效期" prop="expiryDate">
        <el-date-picker v-model="queryParams.expiryDate" value-format="YYYY-MM-DD" type="daterange"
          start-placeholder="开始" end-placeholder="结束" class="!w-220px" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" /> 搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" /> 重置
        </el-button>
        <!-- 加大左右外边距 -->
        <el-tooltip content="零库存记录默认保留，由定时任务自动清理；勾选可临时隐藏" placement="top">
          <el-checkbox v-model="hideZero" style="margin: 0 20px;">隐藏零库存</el-checkbox>
        </el-tooltip>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['wms:stock:export']">
          <Icon icon="ep:download" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table row-key="id" v-loading="loading" :data="filteredList" :stripe="true" :show-overflow-tooltip="true"
      :row-class-name="getRowClassName" :default-sort="{ prop: 'createTime', order: 'descending' }">
      <el-table-column label="产品" min-width="240px" align="left" :show-overflow-tooltip="false">
        <template #default="scope">
          <!-- 库存预警通知：低于最小库存/高于最大库存时常驻显示，回到安全范围内自动消失 -->
          <div v-if="getStockWarning(scope.row).type !== 'normal'" class="stock-warn-tip"
            :class="getStockWarning(scope.row).type === 'low' ? 'stock-warn-tip--low' : 'stock-warn-tip--high'">
            <Icon icon="ep:warning-filled" :size="12" />
            <span>{{ getWarningText(scope.row) }}</span>
          </div>
          <div class="stock-sku-name">{{ formatSkuName(scope.row.skuId) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="仓库" width="120px" align="center">
        <template #default="scope">{{ getWarehouseName(scope.row.warehouseId) }}</template>
      </el-table-column>
      <el-table-column label="库位" width="120px" align="center">
        <template #default="scope">{{ getLocationName(scope.row.locationId) }}</template>
      </el-table-column>
      <el-table-column label="库存管控" width="100px" align="center">
        <template #default="scope">
          <el-tag size="small">{{ getStockModeLabel(scope.row.stockMode) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="批次号" width="140px" prop="batchNo" />
      <el-table-column label="生产日期" width="110px" prop="productionDate" :formatter="dateFormatter" />
      <el-table-column label="有效期至" width="110px" prop="expiryDate" :formatter="dateFormatter"
        :class-name="getExpiryWarningClass" />
      <el-table-column label="库存数量" width="110px" align="right" prop="quantity">
        <template #default="scope">
          <span :class="getStockQtyClass(scope.row)">
            {{ scope.row.quantity }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="可用数量" width="90px" align="right">
        <template #default="scope">
          <span :class="getAvailableClass(scope.row)">
            {{ getAvailable(scope.row) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="安全库存" width="120px" align="center">
        <template #default="scope">
          <span class="text-info">{{ getStockRangeText(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="均价" width="100px" align="right" prop="unitPrice" />
      <el-table-column label="库存价值" width="120px" align="right">
        <template #default="scope">
          {{ formatStockValue(scope.row) }}
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="160px" prop="createTime" :formatter="dateFormatter" sortable="custom" />
      <el-table-column label="操作" width="80px" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="warning" size="small" @click="openAdjustForRow(scope.row)"
            v-hasPermi="['wms:stock:adjust']">盘点</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize"
      @pagination="getList" />
  </ContentWrap>

  <!-- 盘点弹窗 -->
  <Dialog title="库存盘点" v-model="adjustDialogVisible" width="500px" :close-on-click-modal="false">
    <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustRules" label-width="100px">
      <el-alert title="选中库存记录" type="info" :closable="false" class="mb-15px">
        <template #default>
          <div>产品：{{ adjustForm.skuName }}</div>
          <div>库位：{{ adjustForm.locationName }}</div>
          <div>当前库存：<strong>{{ adjustForm.bookQuantity }}</strong></div>
        </template>
      </el-alert>
      <el-form-item label="实盘数量" prop="actualQuantity">
        <el-input-number v-model="adjustForm.actualQuantity" :min="0" :precision="2" class="!w-full" />
      </el-form-item>
      <el-form-item label="盘点类型" prop="checkType">
        <el-select v-model="adjustForm.checkType" placeholder="选择盘点类型" class="!w-full">
          <el-option label="全盘" :value="1" />
          <el-option label="抽盘" :value="2" />
          <el-option label="手动调整" :value="3" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" :loading="adjustSubmitting" @click="submitSaveDraft"
        :disabled="!!adjustForm.adjustId">保存草稿</el-button>
      <el-button @click="adjustDialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { StockApi } from '@/api/wms/stock'
import { InventoryAdjustApi } from '@/api/wms/inventoryadjust'
import { WmsProductApi } from '@/api/wms/product'
import { WarehouseApi } from '@/api/wms/warehouse'
import { getStockModeLabel, isVirtualWarehouse, getVirtualWarehouseName } from '../enums'

/** 库存快照管理 */
defineOptions({ name: 'WmsStock' })

const message = useMessage()

const loading = ref(true)
const exportLoading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
/** 将安全库存值统一转为数字，未配置（null/空串）返回 null */
const toNum = (v: any): number | null => {
  if (v === null || v === undefined || v === '') return null
  const n = Number(v)
  return isNaN(n) ? null : n
}

const skuNameMap = ref<Record<number, string>>({})
/** SKU 安全库存上下限缓存：{ [skuId]: { minStock, maxStock } } */
const skuStockRangeMap = ref<Record<number, { minStock: number | null; maxStock: number | null }>>({})

const formatSkuName = (id: number): string => skuNameMap.value[id] || `SKU(${id})`

const loadSkuNames = async (ids: number[]) => {
  const list = ids.filter(Boolean)
  if (list.length === 0) return
  try {
    const res = await WmsProductApi.getSkuList(list)
      ; (res || []).forEach(item => {
        skuNameMap.value[item.id] = item.spuName
        // 缓存安全库存上下限，用于库存变化后判定是否触发预警
        skuStockRangeMap.value[item.id] = { minStock: toNum(item.minStock), maxStock: toNum(item.maxStock) }
      })
  } catch { }
}

// ==================== 安全库存预警（库存变化后按当前库存判定，回到安全范围内自动消失） ====================

/**
 * 判定当前库存是否触发安全库存预警
 * low = 低于最小库存（缺货，建议采购补货）；high = 高于最大库存（积压，建议尽快销售）；normal = 安全范围内
 */
const getStockWarning = (row: any): { type: 'low' | 'high' | 'normal'; min: number | null; max: number | null } => {
  const range = skuStockRangeMap.value[row.skuId]
  const min = range ? range.minStock : null
  const max = range ? range.maxStock : null
  const qty = Number(row.quantity || 0)
  if (min !== null && qty < min) return { type: 'low', min, max }
  if (max !== null && qty > max) return { type: 'high', min, max }
  return { type: 'normal', min, max }
}

/** 预警通知文案 */
const getWarningText = (row: any): string => {
  const w = getStockWarning(row)
  const qty = Number(row.quantity || 0)
  if (w.type === 'low') return `缺货预警：当前 ${qty} 低于最小库存 ${w.min}，建议及时采购补货`
  if (w.type === 'high') return `积压预警：当前 ${qty} 高于最大库存 ${w.max}，建议尽快安排销售出库`
  return ''
}

/** 库存数量样式：缺货标红、积压标橙 */
const getStockQtyClass = (row: any): string => {
  const w = getStockWarning(row)
  if (w.type === 'low') return 'text-danger font-bold'
  if (w.type === 'high') return 'text-warning font-bold'
  return Number(row.quantity || 0) <= 0 ? 'text-danger font-bold' : ''
}

/** 安全库存范围展示：最小 ~ 最大，未配置时提示未配置 */
const getStockRangeText = (row: any): string => {
  const w = getStockWarning(row)
  if (w.min === null && w.max === null) return '未配置'
  return `${w.min ?? '-'} ~ ${w.max ?? '-'}`
}

/** 预警行整行底色，方便快速定位 */
const getRowClassName = ({ row }: { row: any }): string => {
  const w = getStockWarning(row)
  if (w.type === 'low') return 'stock-warn-row--low'
  if (w.type === 'high') return 'stock-warn-row--high'
  return ''
}

const hideZero = ref(false)

const warehouseList = ref<any[]>([])
const locationList = ref<any[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  skuId: undefined,
  warehouseId: undefined,
  locationId: undefined,
  batchNo: undefined,
  expiryDate: []
})
const queryFormRef = ref()

/** 按隐藏零库存过滤（命中缺货预警的记录始终保留，否则零库存的缺货预警会被隐藏） */
const filteredList = computed(() => {
  if (hideZero.value) {
    return list.value.filter(item =>
      (item.quantity && Number(item.quantity) > 0) || getStockWarning(item).type === 'low')
  }
  return list.value
})

/** 获取仓库名称 */
const getWarehouseName = (id: number): string => {
  if (isVirtualWarehouse(id)) return getVirtualWarehouseName(id)
  const w = warehouseList.value.find(item => item.id === id)
  return w ? w.name : String(id)
}

/** 获取库位名称 */
const getLocationName = (id: number): string => {
  const w = locationList.value.find(item => item.id === id)
  return w ? w.name : String(id)
}

/** 有效期预警样式 */
const getExpiryWarningClass = (row: any): string => {
  if (!row.expiryDate) return ''
  const now = new Date()
  const expiry = new Date(row.expiryDate)
  const days = (expiry.getTime() - now.getTime()) / (1000 * 60 * 60 * 24)
  if (days < 0) return 'text-danger font-bold'       // 已过期
  if (days < 30) return 'text-warning'                // 30天内过期
  return ''
}

/** 计算可用数量 = 库存量 - 预占量 */
const getAvailable = (row: any): number => {
  return (row.quantity || 0) - (row.reservedQuantity || 0)
}

/** 可用数量样式 */
const getAvailableClass = (row: any): string => {
  const available = getAvailable(row)
  if (available <= 0) return 'text-danger font-bold'
  if ((row.reservedQuantity || 0) > 0) return 'text-warning'
  return ''
}

/** 库存价值 = 数量 × 均价 */
const formatStockValue = (row: any): string => {
  const qty = row.quantity || 0
  const price = row.unitPrice || 0
  const value = qty * price
  if (value === 0) return '-'
  return value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/** 加载仓库列表 */
const loadWarehouses = async () => {
  try {
    const res = await WarehouseApi.getWarehousePage({ pageSize: 200 })
    const all = res.list || []
    warehouseList.value = all.filter((w: any) => w.parentId === 0 && !isVirtualWarehouse(w.id))
    locationList.value = all.filter((w: any) => !isVirtualWarehouse(w.id))
  } catch { }
}

const getList = async () => {
  loading.value = true
  try {
    const data = await StockApi.getStockPage(queryParams)
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
    const data = await StockApi.exportStock(queryParams)
    download.excel(data, '库存快照.xls')
  } catch { } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  loadWarehouses()
  getList()
})

// keep-alive 缓存下切回本页时重新加载，确保其他页面（出入库/调拨/盘点审核）引起的库存变化能及时判定预警
let skipFirstActivated = true
onActivated(() => {
  if (skipFirstActivated) {
    skipFirstActivated = false
    return
  }
  getList()
})

/** ========== 盘点功能重构 ========== */
const adjustDialogVisible = ref(false)
const adjustSubmitting = ref(false)
const adjustFormRef = ref()
const adjustForm = reactive({
  stockId: undefined as number | undefined,
  adjustId: undefined as number | undefined,
  skuName: '',
  locationName: '',
  bookQuantity: 0,
  actualQuantity: 0,
  checkType: 1
})
const adjustRules = reactive({
  actualQuantity: [{ required: true, message: '请输入实盘数量', trigger: 'blur' }],
  checkType: [{ required: true, message: '请选择盘点类型', trigger: 'change' }]
})

/** 打开盘点弹窗（从工具栏按钮进入，需手动选择记录） */
const openAdjustDialog = () => {
  adjustForm.stockId = undefined
  adjustForm.adjustId = undefined
  adjustForm.skuName = ''
  adjustForm.locationName = ''
  adjustForm.bookQuantity = 0
  adjustForm.actualQuantity = 0
  adjustForm.checkType = 1
  adjustDialogVisible.value = true
}

/** 打开盘点弹窗（从行操作按钮进入，自动填充数据） */
const openAdjustForRow = (row: any) => {
  adjustForm.stockId = row.id
  adjustForm.adjustId = undefined
  adjustForm.skuName = formatSkuName(row.skuId)
  adjustForm.locationName = getLocationName(row.locationId)
  adjustForm.bookQuantity = row.quantity || 0
  adjustForm.actualQuantity = row.quantity || 0
  adjustForm.checkType = 1
  adjustDialogVisible.value = true
}

/** 保存草稿：创建盘点调整记录（status=0） */
const submitSaveDraft = async () => {
  await adjustFormRef.value.validate()
  if (!adjustForm.stockId) {
    message.error('请先选择要盘点的库存记录')
    return
  }
  adjustSubmitting.value = true
  try {
    const id = await InventoryAdjustApi.createInventoryAdjust({
      stockId: adjustForm.stockId,
      actualQuantity: adjustForm.actualQuantity,
      checkType: adjustForm.checkType
    })
    adjustForm.adjustId = id
    message.success('草稿保存成功')
  } catch { } finally {
    adjustSubmitting.value = false
  }
}

/** 审核：提交盘点调整审核 */
const submitApprove = async () => {
  if (!adjustForm.adjustId) {
    message.error('请先保存草稿')
    return
  }
  adjustSubmitting.value = true
  try {
    await InventoryAdjustApi.approveInventoryAdjust(adjustForm.adjustId)
    message.success('盘点审核成功，库存已更新')
    adjustDialogVisible.value = false
    await getList()
  } catch { } finally {
    adjustSubmitting.value = false
  }
}
</script>

<style scoped>
:deep(.text-danger) {
  color: #f56c6c;
}

:deep(.text-warning) {
  color: #e6a23c;
}

:deep(.font-bold) {
  font-weight: bold;
}

:deep(.text-info) {
  color: #909399;
}

/* 库存预警通知条：显示在本行数据上方，库存回到安全范围内即消失 */
.stock-warn-tip {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 1px 6px;
  margin-bottom: 3px;
  font-size: 12px;
  line-height: 18px;
  border-radius: 3px;
}

.stock-warn-tip--low {
  color: #f56c6c;
  background-color: #fde2e2;
}

.stock-warn-tip--high {
  color: #e6a23c;
  background-color: #faecd8;
}

.stock-sku-name {
  font-size: 13px;
}

/* 预警行整行底色（覆盖斑马纹） */
:deep(.el-table__row.stock-warn-row--low > td.el-table__cell) {
  background-color: #fef0f0 !important;
}

:deep(.el-table__row.stock-warn-row--high > td.el-table__cell) {
  background-color: #fdf6ec !important;
}
</style>