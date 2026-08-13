<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px" v-loading="formLoading">
      <el-form-item label="上级节点" prop="parentId">
        <el-tree-select
          v-model="formData.parentId"
          :data="parentTreeData"
          :props="{ label: 'name', value: 'id', children: 'children' }"
          :disabled="formType === 'update'"
          placeholder="选择上级节点（空=根节点）"
          clearable check-strictly class="!w-full" filterable
        />
        <div class="text-gray-400 text-xs mt-1">
          <template v-if="formData.parentId === 0 || !formData.parentId">将创建为【实体仓库】（根节点）</template>
          <template v-else-if="getSelectedParentLevel() === 0">将创建为【库区】</template>
          <template v-else>将创建为【库位】</template>
        </div>
      </el-form-item>

      <el-form-item label="位置类型" prop="usage">
        <el-select v-model="formData.locType" placeholder="选择位置类型" class="!w-full">
          <el-option label="实体仓库" :value="1" />
          <el-option label="库区" :value="2" />
          <el-option label="库位" :value="3" />
          <el-option label="供应商虚拟" :value="4" />
          <el-option label="客户虚拟" :value="5" />
          <el-option label="盘点差异" :value="6" />
          <el-option label="报废" :value="7" />
          <el-option label="生产" :value="8" />
          <el-option label="在途" :value="9" />
          <el-option label="虚拟节点" :value="0" />
        </el-select>
      </el-form-item>

      <el-form-item label="名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入名称" maxlength="100" show-word-limit />
      </el-form-item>

      <el-form-item label="编码" prop="code">
        <el-input v-model="formData.code" placeholder="编码（非必填）" maxlength="50" />
      </el-form-item>

      <template v-if="formData.parentId === 0 || !formData.parentId">
        <el-form-item label="地区" prop="areaId">
          <el-cascader
            v-model="formData.areaId"
            :options="areaList"
            :props="defaultProps"
            class="!w-full"
            clearable
            filterable
            placeholder="选择省市区"
          />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="formData.address" placeholder="详细地址" maxlength="255" />
        </el-form-item>
        <el-form-item label="负责人" prop="partnerId">
          <el-select v-model="formData.partnerId" placeholder="选择公司" clearable filterable class="!w-full">
            <el-option v-for="item in companyList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
      </template>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" :max="9999" />
      </el-form-item>

      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" placeholder="备注" type="textarea" :rows="2" maxlength="500" />
      </el-form-item>

      <el-form-item label="出库策略" prop="removalStrategy" v-if="formData.locType === 3 || formData.locType === undefined">
        <el-select v-model="formData.removalStrategy" placeholder="默认 FIFO" clearable class="!w-full">
          <el-option v-for="dict in getRemovalStrategyOptions()" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { WarehouseApi, Warehouse } from '@/api/wms/warehouse'
import { WmsPartnerApi } from '@/api/wms/partner'
import { isVirtualWarehouse, getRemovalStrategyOptions } from '../enums'
import * as AreaApi from '@/api/system/area'
import { defaultProps } from '@/utils/tree'

defineOptions({ name: 'WmsWarehouseForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const parentTreeData = ref<Warehouse[]>([])
const areaList = ref([]) // 地区列表
const companyList = ref<any[]>([]) // 公司列表

const formData = ref({
  id: undefined,
  parentId: 0,
  usage: 1,
  name: undefined,
  code: undefined,
  areaId: undefined,
  address: undefined,
  partnerId: undefined,
  status: 1,
  sort: 0,
  remark: undefined,
  removalStrategy: undefined
})

const formRules = reactive({
  parentId: [{ required: false }],
  usage: [{ required: true, message: '请选择位置类型', trigger: 'change' }],
  name: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }],
  sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }]
})

const formRef = ref()

const getSelectedParentLevel = (): number => {
  if (!formData.value.parentId || formData.value.parentId === 0) return -1
  const findLevel = (id: number, list: Warehouse[], depth = 0): number => {
    for (const item of list) {
      if (item.id === id) return depth
      if (item.children) {
        const found = findLevel(id, item.children, depth + 1)
        if (found >= 0) return found
      }
    }
    return -1
  }
  return findLevel(formData.value.parentId, parentTreeData.value)
}

const open = async (type: string, id?: number, parentId?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增节点' : '编辑节点'
  formType.value = type
  resetForm()

  try {
    const res = await WarehouseApi.getWarehousePage({ pageSize: 200 })
    const allList = res.list || []
    const filtered = allList.filter((item: Warehouse) => !isVirtualWarehouse(item.id))
    parentTreeData.value = buildTree(filtered)
  } catch { }

  // 加载地区列表
  try {
    areaList.value = await AreaApi.getAreaTree()
  } catch { }

  // 加载公司列表（仓库负责人下拉选择）
  try {
    companyList.value = await WmsPartnerApi.getCompanyList()
  } catch { }

  if (type === 'create' && parentId !== undefined) {
    formData.value.parentId = parentId
    const parent = findNodeById(parentTreeData.value, parentId)
    if (parent) {
      if (parent.locType === 1 || parent.locType === undefined) formData.value.locType = 2
      else if (parent.locType === 2) formData.value.locType = 3
    }
  }

  if (id) {
    formLoading.value = true
    try {
      const data = await WarehouseApi.getWarehouse(id)
      formData.value = { ...formData.value, ...data }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

const buildTree = (list: Warehouse[]): Warehouse[] => {
  const map = new Map<number, Warehouse>()
  const tree: Warehouse[] = []
  list.sort((a, b) => (a.sort || 0) - (b.sort || 0))
  list.forEach(item => map.set(item.id, { ...item, children: [] }))
  list.forEach(item => {
    const node = map.get(item.id)!
    if (item.parentId === 0) { tree.push(node) }
    else {
      const parent = map.get(item.parentId!)
      if (parent) { parent.children = parent.children || []; parent.children.push(node) }
      else { tree.push(node) }
    }
  })
  return tree
}

const findNodeById = (list: Warehouse[], id: number): Warehouse | null => {
  for (const item of list) {
    if (item.id === id) return item
    if (item.children) {
      const found = findNodeById(item.children, id)
      if (found) return found
    }
  }
  return null
}

const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    const data = formData.value as unknown as Warehouse
    if (formType.value === 'create') {
      await WarehouseApi.createWarehouse(data)
      message.success(t('common.createSuccess'))
    } else {
      await WarehouseApi.updateWarehouse(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    parentId: 0,
    usage: 1,
    name: undefined,
    code: undefined,
    areaId: undefined,
    address: undefined,
    partnerId: undefined,
    status: 1,
    sort: 0,
    remark: undefined,
    removalStrategy: undefined
  }
  formRef.value?.resetFields()
}
</script>