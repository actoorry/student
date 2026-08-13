<template>
  <div class="h-full flex flex-col gap-10px overflow-hidden p-10px">
    <!-- 搜索栏 -->
    <ContentWrap>
      <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="queryParams.name" placeholder="仓库/库区/库位名称" clearable @keyup.enter="handleQuery"
            class="!w-200px" />
        </el-form-item>
        <el-form-item label="编码" prop="code">
          <el-input v-model="queryParams.code" placeholder="编码" clearable @keyup.enter="handleQuery" class="!w-160px" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="状态" clearable class="!w-120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery">
            <Icon icon="ep:search" /> 搜索
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" /> 重置
          </el-button>
          <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['wms:warehouse:create']">
            <Icon icon="ep:plus" /> 新增仓库
          </el-button>
          <el-button type="success" plain @click="handleExport" :loading="exportLoading"
            v-hasPermi="['wms:warehouse:export']">
            <Icon icon="ep:download" /> 导出
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <!-- 树形表格（自适应全屏区域） -->
    <ContentWrap class="flex-1 flex flex-col overflow-hidden">
      <el-table :data="treeData" row-key="id" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        v-loading="loading" :stripe="true" default-expand-all height="100%">
        <el-table-column label="名称" min-width="240px" prop="name">
          <template #default="scope">
            <div class="flex items-center">
              <Icon :icon="getNodeIcon(scope.row)" class="mr-5px" :style="{ color: getNodeColor(scope.row) }" />
              <span :class="{ 'font-bold': scope.row.parentId === 0 }">{{ scope.row.name }}</span>
              <el-tag v-if="isVirtualWarehouse(scope.row.id)" size="small" type="warning" class="ml-5px">
                {{ getVirtualWarehouseName(scope.row.id) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="编码" width="120px" prop="code" />
        <el-table-column label="类型" width="100px" align="center">
          <template #default="scope">
            <el-tag :type="getLevelTagType(scope.row)" size="small">
              {{ getLevelName(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80px" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="负责人" width="120px" align="center">
          <template #default="scope">
            {{ partnerMap[scope.row.partnerId] || scope.row.partnerId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="所属区域" width="180px" prop="areaName" show-overflow-tooltip />
        <el-table-column label="地址" min-width="200px" prop="address" show-overflow-tooltip />
        <el-table-column label="排序" width="70px" prop="sort" align="center" />
        <el-table-column label="出库策略" width="100px" align="center">
          <template #default="scope">
            <dict-tag :type="DICT_TYPE.WMS_REMOVAL_STRATEGY" :value="scope.row.removalStrategy" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220px" align="center" fixed="right">
          <template #default="scope">
            <template v-if="!isVirtualWarehouse(scope.row.id)">
              <el-button link type="primary" @click="openForm('update', scope.row.id)"
                v-hasPermi="['wms:warehouse:update']">编辑</el-button>
              <el-button v-if="getLevel(scope.row) < 2" link type="primary"
                @click="openForm('create', undefined, scope.row.id)"
                v-hasPermi="['wms:warehouse:create']">新增子节点</el-button>
              <el-button link type="danger" @click="handleDelete(scope.row)"
                v-hasPermi="['wms:warehouse:delete']">删除</el-button>
            </template>
            <span v-else class="text-gray-400 text-sm">— 虚拟仓不可操作 —</span>
          </template>
        </el-table-column>
      </el-table>
    </ContentWrap>

    <WarehouseForm ref="formRef" @success="getList" />
  </div>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { WarehouseApi, Warehouse } from '@/api/wms/warehouse'
import WarehouseForm from './WarehouseForm.vue'
import {
  VIRTUAL_WAREHOUSE,
  isVirtualWarehouse,
  getVirtualWarehouseName,
  getUsageLabel,
  WAREHOUSE_STATUS_MAP
} from '../enums'
import { DICT_TYPE } from '@/utils/dict'
import { WmsPartnerApi } from '@/api/wms/partner'

defineOptions({ name: 'WmsWarehouse' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const exportLoading = ref(false)
const treeData = ref<Warehouse[]>([])
const flatList = ref<Warehouse[]>([])
const partnerMap = ref<Record<number, string>>({})

const queryParams = reactive({
  name: undefined,
  code: undefined,
  status: undefined
})
const queryFormRef = ref()

/** 获取层级 */
const getLevel = (row: Warehouse): number => {
  if (row.locType !== undefined && row.locType !== 1) {
    if (row.locType === 1) return 0
    if (row.locType === 2) return 1
    if (row.locType === 3) return 2
    return 0
  }
  if (row.parentId === 0) return 0
  if (row.parentId && row.parentId > 0 && !flatList.value.find(n => n.id === row.parentId)?.parentId) return 1
  const parent = flatList.value.find(n => n.id === row.parentId)
  if (!parent) return 0
  if (parent.parentId === 0) return 1
  return 2
}

/** 获取层级/类型名称 */
const getLevelName = (row: Warehouse): string => {
  if (row.locType !== undefined && row.locType !== 1) return getUsageLabel(row.locType)
  // locType=1 或未设置时按 parentId 推算
  if (row.parentId === 0) return '实体仓库'
  const parent = flatList.value.find(n => n.id === row.parentId)
  if (!parent || parent.parentId === 0) return '库区'
  return '库位'
}

/** 获取层级标签样式 */
const getLevelTagType = (row: Warehouse): string => {
  if (row.locType !== undefined) {
    if (row.locType >= 4) return 'warning'
    if (row.locType === 1) return 'primary'
    if (row.locType === 2) return 'warning'
    return 'info'
  }
  if (row.parentId === 0) return 'primary'
  const parent = flatList.value.find(n => n.id === row.parentId)
  if (!parent || parent.parentId === 0) return 'warning'
  return 'info'
}

/** 获取节点图标 */
const getNodeIcon = (row: Warehouse): string => {
  if (isVirtualWarehouse(row.id)) return 'ep:goods'
  if (row.locType !== undefined) {
    if (row.locType >= 4) return 'ep:cloudy'
    if (row.locType === 1) return 'ep:office-building'
    if (row.locType === 2) return 'ep:folder-opened'
    return 'ep:map-location'
  }
  if (row.parentId === 0) return 'ep:office-building'
  const parent = flatList.value.find(n => n.id === row.parentId)
  if (!parent || parent.parentId === 0) return 'ep:folder-opened'
  return 'ep:map-location'
}

/** 获取节点颜色 */
const getNodeColor = (row: Warehouse): string => {
  if (isVirtualWarehouse(row.id)) return '#e6a23c'
  if (row.locType !== undefined) {
    if (row.locType >= 4) return '#e6a23c'
    if (row.locType === 1) return '#409eff'
    if (row.locType === 2) return '#67c23a'
    return '#909399'
  }
  if (row.parentId === 0) return '#409eff'
  const parent = flatList.value.find(n => n.id === row.parentId)
  if (!parent || parent.parentId === 0) return '#67c23a'
  return '#909399'
}

/** 构建树形结构 */
const buildTree = (list: Warehouse[]): Warehouse[] => {
  const map = new Map<number, Warehouse>()
  const tree: Warehouse[] = []
  list.sort((a, b) => (a.sort || 0) - (b.sort || 0))
  list.forEach(item => { map.set(item.id, { ...item, children: [] }) })
  list.forEach(item => {
    const node = map.get(item.id)!
    if (item.parentId === 0) {
      tree.push(node)
    } else {
      const parent = map.get(item.parentId!)
      if (parent) {
        parent.children = parent.children || []
        parent.children.push(node)
      } else {
        tree.push(node)
      }
    }
  })
  return tree
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const res = await WarehouseApi.getWarehousePage({ ...queryParams, pageSize: 200 })
    flatList.value = res.list || []
    const filteredList = flatList.value.filter(item => !isVirtualWarehouse(item.id))
    treeData.value = buildTree(filteredList)
    // 加载负责人名称
    await loadPartnerNames()
  } finally {
    loading.value = false
  }
}

/** 加载负责人名称 */
const loadPartnerNames = async () => {
  const ids = flatList.value.map(i => i.partnerId).filter(Boolean) as number[]
  if (ids.length === 0) return
  try {
    const list = await WmsPartnerApi.getCompanyList()
    const map: Record<number, string> = {}
    list.forEach(item => { map[item.id] = item.name })
    partnerMap.value = map
  } catch { }
}

const handleQuery = () => { getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); getList() }

const formRef = ref()
const openForm = (type: string, id?: number, parentId?: number) => {
  formRef.value.open(type, id, parentId)
}

const handleDelete = async (row: Warehouse) => {
  const hasChildren = flatList.value.some(item => item.parentId === row.id)
  if (hasChildren) {
    message.error('该节点存在子节点，无法删除，请先删除子节点')
    return
  }
  try {
    await message.delConfirm()
    await WarehouseApi.deleteWarehouse(row.id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch { }
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await WarehouseApi.exportWarehouse(queryParams)
    download.excel(data, '仓库位置.xls')
  } catch { } finally {
    exportLoading.value = false
  }
}

onMounted(() => { getList() })
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
</style>