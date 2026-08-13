<template>
  <ContentWrap>
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="场景名称" prop="sceneName">
        <el-input
          v-model="queryParams.sceneName"
          placeholder="请输入场景名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="filteredList" row-key="sceneCode">
      <el-table-column label="页面" min-width="140" prop="sceneName" />
      <el-table-column label="场景编码" min-width="180" prop="sceneCode" />
      <el-table-column label="分类编号" align="center" min-width="100" prop="categoryId">
        <template #default="scope">
          <span v-if="scope.row.categoryId">{{ scope.row.categoryId }}</span>
          <el-tag v-else type="info">未配置</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="分类名称" min-width="140" prop="categoryName">
        <template #default="scope">
          <span v-if="scope.row.categoryName">{{ scope.row.categoryName }}</span>
          <span v-else class="text-gray-400">-</span>
        </template>
      </el-table-column>
      <el-table-column label="配置状态" align="center" min-width="140">
        <template #default="scope">
          <el-tag :type="getConfigStateType(scope.row.configState)">
            {{ getConfigStateLabel(scope.row.configState) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" min-width="100" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" min-width="80" prop="sort" />
      <el-table-column label="备注" min-width="160" prop="remark" show-overflow-tooltip />
      <el-table-column
        label="更新时间"
        align="center"
        prop="updateTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column label="操作" align="center" min-width="180" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm(scope.row)"
            v-hasPermi="['product:display-config:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="primary"
            v-if="scope.row.categoryId"
            @click="handleViewSpu(scope.row.categoryId)"
            v-hasPermi="['product:spu:query']"
          >
            查看产品
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <Dialog title="编辑页面展示配置" v-model="dialogVisible" width="600px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="页面">
        <el-input v-model="formData.sceneName" disabled />
      </el-form-item>
      <el-form-item label="场景编码">
        <el-input v-model="formData.sceneCode" disabled />
      </el-form-item>
      <el-form-item label="销售分类" prop="categoryId">
        <el-tree-select
          v-model="formData.categoryId"
          :data="categoryTree"
          :props="{ label: 'name', value: 'id', children: 'children' }"
          :render-after-expand="false"
          placeholder="请选择销售分类"
          check-strictly
          filterable
          class="w-1/1"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" controls-position="right" :min="0" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading" :loading="formLoading">
        确 定
      </el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'
import { dateFormatter } from '@/utils/formatTime'
import { handleTree } from '@/utils/tree'
import * as ProductCategoryApi from '@/api/product/category'
import * as ProductDisplayConfigApi from '@/api/product/displayConfig'
import { ProductDisplayConfigVO } from '@/api/product/displayConfig'

defineOptions({ name: 'ProductPageDisplay' })

const message = useMessage()
const { t } = useI18n()
const router = useRouter()

const loading = ref(true)
const list = ref<ProductDisplayConfigVO[]>([])
const queryParams = reactive({
  sceneName: undefined
})
const queryFormRef = ref()

const dialogVisible = ref(false)
const formLoading = ref(false)
const formData = ref({
  sceneCode: '',
  sceneName: '',
  categoryId: undefined as number | undefined,
  status: CommonStatusEnum.ENABLE,
  sort: 0,
  remark: '',
  updateTime: undefined as string | undefined
})
const formRules = reactive({
  categoryId: [{ required: true, message: '请选择销售分类', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }]
})
const formRef = ref()

const categoryTree = ref<any[]>([])

const filteredList = computed(() => {
  if (!queryParams.sceneName) {
    return list.value
  }
  const keyword = String(queryParams.sceneName).trim()
  return list.value.filter(
    (item) =>
      item.sceneName.includes(keyword) ||
      item.sceneCode.toLowerCase().includes(keyword.toLowerCase())
  )
})

const configStateMap: Record<string, { label: string; type: any }> = {
  UNCONFIGURED: { label: '未配置', type: 'info' },
  ACTIVE: { label: '生效中', type: 'success' },
  DISABLED: { label: '已禁用', type: 'warning' },
  INVALID_CATEGORY: { label: '分类无效', type: 'danger' },
  MULTIPLE_CATEGORIES: { label: '多分类不兼容', type: 'danger' }
}

const getConfigStateLabel = (state: string) => configStateMap[state]?.label || state
const getConfigStateType = (state: string) => configStateMap[state]?.type || 'info'

const getList = async () => {
  loading.value = true
  try {
    list.value = await ProductDisplayConfigApi.getDisplayConfigList()
  } finally {
    loading.value = false
  }
}

const loadCategoryTree = async () => {
  try {
    const data = await ProductCategoryApi.getCategoryList({ parentId: 1, status: 0 })
    categoryTree.value = handleTree(data, 'id', 'parentId')
  } catch {
    message.error('加载销售分类失败')
  }
}

const handleQuery = () => {
  // 仅前端过滤
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
}

const openForm = (row: ProductDisplayConfigVO) => {
  dialogVisible.value = true
  formData.value = {
    sceneCode: row.sceneCode,
    sceneName: row.sceneName,
    categoryId: row.categoryId,
    status: row.status,
    sort: row.sort,
    remark: row.remark || '',
    updateTime: row.updateTime
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (formData.value.status === CommonStatusEnum.DISABLE) {
    try {
      await message.confirm('禁用后该页面将无法获取展示分类，是否继续？')
    } catch {
      return
    }
  }

  formLoading.value = true
  try {
    const req = {
      sceneCode: formData.value.sceneCode,
      categoryId: formData.value.categoryId!,
      status: formData.value.status,
      sort: formData.value.sort,
      remark: formData.value.remark,
      updateTime: formData.value.updateTime
    }
    await ProductDisplayConfigApi.updateDisplayConfig(req)
    message.success(t('common.updateSuccess'))
    dialogVisible.value = false
    await getList()
  } catch (error: any) {
    // 冲突类错误统一刷新列表，便于用户看到最新状态
    if (error?.code === 1_008_010_002 || error?.code === 1_008_010_003) {
      message.error(error?.msg || '配置已过期，请刷新后重试')
      await getList()
    }
  } finally {
    formLoading.value = false
  }
}

const handleViewSpu = (categoryId?: number) => {
  if (!categoryId) return
  router.push({
    name: 'ProductSpu',
    query: { categorySales: categoryId }
  })
}

onMounted(() => {
  getList()
  loadCategoryTree()
})
</script>
