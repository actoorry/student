<template>
  <!-- 搜索栏 -->
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="单据编号" prop="no">
        <el-input v-model="queryParams.no" placeholder="单据编号" clearable @keyup.enter="handleQuery" class="!w-180px" />
      </el-form-item>
      <el-form-item label="盘点仓库" prop="warehouseId">
        <el-select v-model="queryParams.warehouseId" placeholder="全部" clearable class="!w-160px">
          <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="盘点类型" prop="checkType">
        <el-select v-model="queryParams.checkType" placeholder="全部" clearable class="!w-130px">
          <el-option label="全盘" :value="1" />
          <el-option label="抽盘" :value="2" />
          <el-option label="手动调整" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable class="!w-100px">
          <el-option label="草稿" :value="0" />
          <el-option label="已审核" :value="1" />
        </el-select>
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
      <el-table-column label="单据编号" width="180px" align="center" prop="no" />
      <el-table-column label="盘点仓库" width="120px" align="center">
        <template #default="scope">{{ getWarehouseName(scope.row.warehouseId) }}</template>
      </el-table-column>
      <el-table-column label="产品" min-width="140px" align="center">
        <template #default="scope">{{ formatSkuName(scope.row.skuId) }}</template>
      </el-table-column>
      <el-table-column label="库位" width="120px" align="center">
        <template #default="scope">{{ getLocationName(scope.row.locationId) }}</template>
      </el-table-column>
      <el-table-column label="盘点类型" width="100px" align="center">
        <template #default="scope">
          <el-tag size="small">{{ getCheckTypeLabel(scope.row.checkType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="账面数量" width="100px" align="right" prop="bookQuantity" />
      <el-table-column label="实盘数量" width="100px" align="right" prop="actualQuantity" />
      <el-table-column label="盈亏" width="100px" align="right">
        <template #default="scope">
          <span v-if="scope.row.status === 1" :class="getDiffClass(scope.row)">
            {{ formatDiff(scope.row) }}
          </span>
          <span v-else class="text-gray-400">—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80px" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 0 ? 'warning' : 'success'" size="small">
            {{ scope.row.status === 0 ? '草稿' : '已审核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="盘点时间" width="160px" prop="checkTime" :formatter="dateFormatter" />
      <el-table-column label="备注" min-width="140px" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" width="220px" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openDetail(scope.row)">详情</el-button>
          <el-button v-if="scope.row.status === 0" link type="primary" @click="handleApprove(scope.row)"
            v-hasPermi="['wms:inventory-adjust:finish']">审核</el-button>
          <el-button v-if="scope.row.status === 0" link type="danger" @click="handleDelete(scope.row.id)"
            v-hasPermi="['wms:inventory-adjust:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize"
      @pagination="getList" />
  </ContentWrap>

  <!-- 审核差异确认弹窗 -->
  <Dialog title="盘点差异确认" v-model="diffDialogVisible" width="500px" @close="onDialogClose">
    <div v-if="diffData">
      <el-alert :title="diffData.title" :type="diffData.type" :description="diffData.description" show-icon
        :closable="false" class="mb-15px" />
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="账面数量">{{ diffData.bookQuantity }}</el-descriptions-item>
        <el-descriptions-item label="实盘数量">{{ diffData.actualQuantity }}</el-descriptions-item>
        <el-descriptions-item label="差异数量" :span="2">
          <span :class="diffData.diff >= 0 ? 'text-success' : 'text-danger'">
            {{ diffData.diff >= 0 ? '+' : '' }}{{ diffData.diff }}
            （{{ diffData.diff >= 0 ? '盘盈' : '盘亏' }}）
          </span>
        </el-descriptions-item>
      </el-descriptions>
      <div class="text-gray-400 text-sm mt-10px">
        审核后库存将被自动调整，差异数量将从盘点差异虚拟仓(3)调整到对应库位。
      </div>
    </div>
    <template #footer>
      <!-- 关键改动：只有审核入口打开弹窗，才显示确认按钮 -->
      <el-button v-if="isFromApprove" type="primary" :loading="approving" @click="confirmApprove">确认审核</el-button>
      <el-button @click="diffDialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { InventoryAdjustApi } from '@/api/wms/inventoryadjust'
import { WmsProductApi } from '@/api/wms/product'
import { WarehouseApi } from '@/api/wms/warehouse'
import { isVirtualWarehouse, getVirtualWarehouseName } from '../enums'

/** 盘点调整记录管理 */
defineOptions({ name: 'WmsInventoryAdjust' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<any[]>([])
const total = ref(0)
const warehouseList = ref<any[]>([])
const locationList = ref<any[]>([])
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

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  no: undefined,
  warehouseId: undefined,
  checkType: undefined,
  status: undefined
})
const queryFormRef = ref()

/** 审核差异弹窗 */
const diffDialogVisible = ref(false)
const diffData = ref<any>(null)
const approving = ref(false)
const currentApproveId = ref<number | null>(null)
// 新增：标记弹窗来源 true=审核按钮打开 false=详情打开
const isFromApprove = ref(false)

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

/** 盘点类型标签 */
const getCheckTypeLabel = (type: number): string => {
  const map: Record<number, string> = { 1: '全盘', 2: '抽盘', 3: '手动调整' }
  return map[type] || `未知(${type})`
}

/** 计算差异 */
const formatDiff = (row: any): string => {
  const diff = (row.actualQuantity || 0) - (row.bookQuantity || 0)
  return diff >= 0 ? `+${diff}` : String(diff)
}

const getDiffClass = (row: any): string => {
  const diff = (row.actualQuantity || 0) - (row.bookQuantity || 0)
  return diff >= 0 ? 'text-success' : 'text-danger'
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
    const data = await InventoryAdjustApi.getInventoryAdjustPage(queryParams)
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

/** 弹窗关闭统一重置状态 */
const onDialogClose = () => {
  diffData.value = null
  currentApproveId.value = null
  isFromApprove.value = false
}

/** 打开详情（仅查看，不可审核） */
const openDetail = (row: any) => {
  const diff = (row.actualQuantity || 0) - (row.bookQuantity || 0)
  diffData.value = {
    bookQuantity: row.bookQuantity,
    actualQuantity: row.actualQuantity,
    diff: diff,
    title: diff >= 0 ? '盘盈' : '盘亏',
    type: diff >= 0 ? 'success' : 'warning',
    description: diff >= 0
      ? `实际库存比账面多 ${diff}，将从盘点差异虚拟仓(3)调整到对应库位。`
      : `实际库存比账面少 ${Math.abs(diff)}，将从盘点差异虚拟仓(3)调整到对应库位。`
  }
  currentApproveId.value = null
  isFromApprove.value = false
  diffDialogVisible.value = true
}

/** 审核：弹出差异确认（可执行审核） */
const handleApprove = async (row: any) => {
  const diff = (row.actualQuantity || 0) - (row.bookQuantity || 0)
  diffData.value = {
    bookQuantity: row.bookQuantity,
    actualQuantity: row.actualQuantity,
    diff: diff,
    title: diff >= 0 ? '盘盈' : '盘亏',
    type: diff >= 0 ? 'success' : 'warning',
    description: diff >= 0
      ? `实际库存比账面多 ${diff}，将从盘点差异虚拟仓(3)调整到对应库位。`
      : `实际库存比账面少 ${Math.abs(diff)}，将从盘点差异虚拟仓(3)调整到对应库位。`
  }
  currentApproveId.value = row.id
  isFromApprove.value = true
  diffDialogVisible.value = true
}

/** 确认审核 */
const confirmApprove = async () => {
  if (!currentApproveId.value) return
  approving.value = true
  try {
    await InventoryAdjustApi.approveInventoryAdjust(currentApproveId.value)
    message.success('审核成功，库存已更新')
    diffDialogVisible.value = false
    await getList()
  } catch { } finally {
    approving.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await InventoryAdjustApi.deleteInventoryAdjust(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch { }
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