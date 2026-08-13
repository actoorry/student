<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-form-item label="上级分类" prop="parentId">
        <el-cascader
          v-model="formData.parentId"
          :options="categoryList"
          :props="cascaderProps"
          class="w-80!"
          clearable
          filterable
          placeholder="请选择上级分类"
        />
        <el-button :icon="RefreshRight" class="ml-1" size="small" @click="refreshCategoryList" />
      </el-form-item>
      <el-form-item label="分类名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入分类名称" />
      </el-form-item>
      <el-form-item label="移动端分类图" prop="picUrl">
        <UploadImg v-model="formData.picUrl" :limit="1" :is-show-tip="false" />
        <div class="pl-10px text-10px">推荐 180x180 图片分辨率</div>
      </el-form-item>
      <el-form-item label="分类排序" prop="sort">
        <el-input-number v-model="formData.sort" controls-position="right" :min="0" />
      </el-form-item>
      <el-form-item label="开启状态" prop="status">
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
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'
import * as ProductCategoryApi from '@/api/product/category'
import { defaultProps } from '@/utils/tree'
import { RefreshRight } from '@element-plus/icons-vue'
import {
  disableCategoryAndDescendants,
  loadManagementCategoryTree,
  SALES_CATEGORY_ROOT_ID
} from './categoryDimension'

defineOptions({ name: 'ProductCategoryForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined,
  parentId: SALES_CATEGORY_ROOT_ID as number,
  name: '',
  picUrl: '',
  sort: 0,
  status: CommonStatusEnum.ENABLE
})
const formRules = reactive({
  parentId: [{ required: true, message: '请选择上级分类', trigger: 'change' }],
  name: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
  sort: [{ required: true, message: '分类排序不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '开启状态不能为空', trigger: 'change' }]
})
const formRef = ref()
const categoryList = ref<any[]>([])
const cascaderProps = {
  ...defaultProps,
  checkStrictly: true,
  emitPath: false,
  disabled: 'disabled'
}

const loadCategoryList = async () => {
  categoryList.value = disableCategoryAndDescendants(
    await loadManagementCategoryTree(),
    formType.value === 'update' ? formData.value.id : undefined
  )
}

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ProductCategoryApi.getCategory(id)
    } finally {
      formLoading.value = false
    }
  }
  await loadCategoryList()
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return

  formLoading.value = true
  try {
    const data = formData.value as ProductCategoryApi.CategoryVO
    if (formType.value === 'create') {
      await ProductCategoryApi.createCategory(data)
      message.success(t('common.createSuccess'))
    } else {
      await ProductCategoryApi.updateCategory(data)
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
    parentId: SALES_CATEGORY_ROOT_ID,
    name: '',
    picUrl: '',
    sort: 0,
    status: CommonStatusEnum.ENABLE
  }
  formRef.value?.resetFields()
}

const refreshCategoryList = async () => {
  await loadCategoryList()
}

onMounted(async () => {
  await loadCategoryList()
})
</script>
