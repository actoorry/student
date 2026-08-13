<template>
  <view class="yd-page-container">
    <!-- 顶部导航栏 -->
    <wd-navbar
      :title="getTitle"
      left-arrow placeholder safe-area-inset-top fixed
      @click-left="handleBack"
    />

    <!-- 表单区域 -->
    <view>
      <wd-form ref="formRef" :model="formData" :schema="formSchema">
        <wd-cell-group border>
          <wd-form-item
            title="字典类型"
            title-width="200rpx"
            prop="dictType"
            :is-link="!formData.id"
            :value="getWotPickerFormValue(dictTypeOptions, formData.dictType)"
            placeholder="请选择字典类型"
            @click="!formData.id && (pickerVisible.dictType = true)"
          />
          <wd-picker
            v-model:visible="pickerVisible.dictType"
            :model-value="formData.dictType"
            :columns="dictTypeOptions"
            @confirm="({ value }) => formData.dictType = value[0]"
          />
          <wd-form-item title="数据标签" title-width="200rpx" prop="label">
            <wd-input
              v-model="formData.label"
              clearable
              placeholder="请输入数据标签"
            />
          </wd-form-item>
          <wd-form-item title="数据键值" title-width="200rpx" prop="value">
            <wd-input
              v-model="formData.value"
              clearable
              placeholder="请输入数据键值"
            />
          </wd-form-item>
          <wd-form-item title="显示排序" title-width="200rpx" prop="sort">
            <wd-input
              v-model.number="formData.sort"
              type="number"
              clearable
              placeholder="请输入显示排序"
            />
          </wd-form-item>
          <wd-form-item title="状态" title-width="200rpx" prop="status" center>
            <wd-radio-group v-model="formData.status" type="button">
              <wd-radio
                v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
                :key="dict.value"
                :value="dict.value"
              >
                {{ dict.label }}
              </wd-radio>
            </wd-radio-group>
          </wd-form-item>
          <wd-form-item
            title="颜色类型"
            title-width="200rpx"
            prop="colorType"
            is-link
            :value="getWotPickerFormValue(getStrDictOptions(DICT_TYPE.SYSTEM_DICT_COLOR_TYPE), formData.colorType)"
            placeholder="请选择颜色类型"
            @click="pickerVisible.colorType = true"
          />
          <wd-picker
            v-model:visible="pickerVisible.colorType"
            :model-value="formData.colorType"
            :columns="getStrDictOptions(DICT_TYPE.SYSTEM_DICT_COLOR_TYPE)"
            @confirm="({ value }) => formData.colorType = value[0]"
          />
          <wd-form-item title="CSS Class" title-width="200rpx" prop="cssClass">
            <wd-input
              v-model="formData.cssClass"
              clearable
              placeholder="请输入 CSS Class，如 #108ee9"
            />
          </wd-form-item>
          <wd-form-item title="备注" title-width="200rpx" prop="remark">
            <wd-textarea
              v-model="formData.remark"
              clearable
              placeholder="请输入备注"
            />
          </wd-form-item>
        </wd-cell-group>
      </wd-form>
    </view>

    <!-- 底部保存按钮 -->
    <view class="yd-detail-footer">
      <wd-button
        type="primary"
        block
        :loading="formLoading"
        @click="handleSubmit"
      >
        保存
      </wd-button>
    </view>
  </view>
</template>

<script lang="ts" setup>
import type { FormInstance } from '@wot-ui/ui/components/wd-form/types'
import type { DictData } from '@/api/system/dict/data'
import { useToast } from '@wot-ui/ui/components/wd-toast'
import { computed, onMounted, ref } from 'vue'
import { createDictData, getDictData, updateDictData } from '@/api/system/dict/data'
import { getSimpleDictTypeList } from '@/api/system/dict/type'
import { getIntDictOptions, getStrDictOptions } from '@/hooks/useDict'
import { navigateBackPlus } from '@/utils'
import { CommonStatusEnum, DICT_TYPE } from '@/utils/constants'
import { createFormSchema, getWotPickerFormValue } from '@/utils/wot'

const props = defineProps<{
  id?: number | any
  dictType?: string | any
}>()

definePage({
  style: {
    navigationBarTitleText: '',
    navigationStyle: 'custom',
  },
})

const toast = useToast()
const getTitle = computed(() => props.id ? '编辑字典数据' : '新增字典数据')
const formLoading = ref(false) // 表单提交状态
const formData = ref<DictData>({
  id: undefined,
  dictType: props.dictType || '',
  label: '',
  value: '',
  sort: 0,
  status: CommonStatusEnum.ENABLE,
  colorType: '',
  cssClass: '',
  remark: '',
}) // 表单数据
const formSchema = createFormSchema({
  dictType: [{ required: true, message: '字典类型不能为空' }],
  label: [{ required: true, message: '数据标签不能为空' }],
  value: [{ required: true, message: '数据键值不能为空' }],
  sort: [{ required: true, message: '显示排序不能为空' }],
  status: [{ required: true, message: '状态不能为空' }],
})
const formRef = ref<FormInstance>() // 表单组件引用
const pickerVisible = ref<Record<string, boolean>>({})

/** 字典类型选项 */
const dictTypeOptions = ref<{ label: string, value: string }[]>([])

/** 返回上一页 */
function handleBack() {
  navigateBackPlus('/pages-system/dict/index')
}

/** 加载字典类型列表 */
async function loadDictTypeList() {
  const list = await getSimpleDictTypeList()
  dictTypeOptions.value = list.map(item => ({
    label: item.name,
    value: item.type,
  }))
}

/** 加载详情 */
async function getDetail() {
  if (!props.id) {
    return
  }
  formData.value = await getDictData(props.id)
}

/** 提交表单 */
async function handleSubmit() {
  const { valid } = await formRef.value.validate()
  if (!valid) {
    return
  }

  formLoading.value = true
  try {
    if (props.id) {
      await updateDictData(formData.value)
      toast.success('修改成功')
    } else {
      await createDictData(formData.value)
      toast.success('新增成功')
    }
    setTimeout(() => {
      handleBack()
    }, 500)
  } finally {
    formLoading.value = false
  }
}

/** 初始化 */
onMounted(async () => {
  await loadDictTypeList()
  await getDetail()
})
</script>

<style lang="scss" scoped>
</style>
