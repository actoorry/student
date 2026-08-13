<template>
  <!-- 搜索框入口 -->
  <view @click="visible = true">
    <wd-search :placeholder="placeholder" hide-cancel disabled />
  </view>

  <!-- 搜索弹窗 -->
  <wd-popup
    v-model="visible"
    position="top"
    :custom-style="getTopPopupStyle()"
    :modal-style="getTopPopupModalStyle()"
    @close="visible = false"
  >
    <view class="yd-search-form-container">
      <view class="yd-search-form-item">
        <view class="yd-search-form-label">
          任务名称
        </view>
        <wd-input
          v-model="formData.name"
          placeholder="请输入任务名称"
          clearable
        />
      </view>
      <view v-if="processDefinitionList.length > 0" class="yd-search-form-item">
        <view class="yd-search-form-label">
          所属流程
        </view>
        <view
          class="flex items-center justify-between rounded-12rpx bg-[#f7f8fa] p-24rpx"
          @click="pickerVisible.processDefinitionKey = true"
        >
          <text class="text-28rpx text-[#333]">
            {{ getWotPickerDisplay(processDefinitionList, formData.processDefinitionKey, { valueKey: 'key', labelKey: 'name', placeholder: '请选择' }) }}
          </text>
          <wd-icon name="arrow-down" size="32rpx" color="#666" />
        </view>
        <wd-picker
          v-model:visible="pickerVisible.processDefinitionKey"
          :model-value="formData.processDefinitionKey"
          :columns="processDefinitionList"
          label-key="name"
          value-key="key"
          @confirm="({ value }) => formData.processDefinitionKey = value[0]"
        />
      </view>
      <view v-if="categoryList.length > 0" class="yd-search-form-item">
        <view class="yd-search-form-label">
          流程分类
        </view>
        <view
          class="flex items-center justify-between rounded-12rpx bg-[#f7f8fa] p-24rpx"
          @click="pickerVisible.category = true"
        >
          <text class="text-28rpx text-[#333]">
            {{ getWotPickerDisplay(categoryList, formData.category, { valueKey: 'code', labelKey: 'name', placeholder: '请选择' }) }}
          </text>
          <wd-icon name="arrow-down" size="32rpx" color="#666" />
        </view>
        <wd-picker
          v-model:visible="pickerVisible.category"
          :model-value="formData.category"
          :columns="categoryList"
          label-key="name"
          value-key="code"
          @confirm="({ value }) => formData.category = value[0]"
        />
      </view>
      <view class="yd-search-form-item">
        <view class="yd-search-form-label">
          流程状态
        </view>
        <wd-radio-group v-model="formData.status" type="button">
          <wd-radio :value="-1">
            全部
          </wd-radio>
          <wd-radio v-for="dict in getIntDictOptions(DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS)" :key="dict.value" :value="dict.value">
            {{ dict.label }}
          </wd-radio>
        </wd-radio-group>
      </view>
      <view class="yd-search-form-item">
        <view class="yd-search-form-label">
          发起时间
        </view>
        <view class="yd-search-form-date-range-container">
          <view class="flex-1" @click="visibleCreateTime[0] = true">
            <view class="yd-search-form-date-range-picker">
              {{ formatDate(formData.createTime?.[0]) || '开始日期' }}
            </view>
          </view>
          -
          <view class="flex-1" @click="visibleCreateTime[1] = true">
            <view class="yd-search-form-date-range-picker">
              {{ formatDate(formData.createTime?.[1]) || '结束日期' }}
            </view>
          </view>
        </view>
        <wd-datetime-picker-view v-if="visibleCreateTime[0]" v-model="tempCreateTime[0]" type="date" />
        <view v-if="visibleCreateTime[0]" class="yd-search-form-date-range-actions">
          <wd-button size="small" variant="plain" @click="visibleCreateTime[0] = false">
            取消
          </wd-button>
          <wd-button size="small" type="primary" @click="handleCreateTime0Confirm">
            确定
          </wd-button>
        </view>
        <wd-datetime-picker-view v-if="visibleCreateTime[1]" v-model="tempCreateTime[1]" type="date" />
        <view v-if="visibleCreateTime[1]" class="yd-search-form-date-range-actions">
          <wd-button size="small" variant="plain" @click="visibleCreateTime[1] = false">
            取消
          </wd-button>
          <wd-button size="small" type="primary" @click="handleCreateTime1Confirm">
            确定
          </wd-button>
        </view>
      </view>
      <view class="yd-search-form-actions">
        <wd-button class="flex-1" variant="plain" @click="handleReset">
          重置
        </wd-button>
        <wd-button class="flex-1" type="primary" @click="handleSearch">
          搜索
        </wd-button>
      </view>
    </view>
  </wd-popup>
</template>

<script lang="ts" setup>
import type { Category } from '@/api/bpm/category'
import type { ProcessDefinition } from '@/api/bpm/definition'
import { computed, onMounted, reactive, ref } from 'vue'
import { getCategorySimpleList } from '@/api/bpm/category'
import { getProcessDefinitionList } from '@/api/bpm/definition'
import { getDictLabel, getIntDictOptions } from '@/hooks/useDict'
import { getTopPopupModalStyle, getTopPopupStyle } from '@/utils'
import { DICT_TYPE } from '@/utils/constants'
import { formatDate, formatDateRange } from '@/utils/date'
import { getWotPickerDisplay } from '@/utils/wot'

const emit = defineEmits<{
  search: [data: Record<string, any>]
  reset: []
}>()

const formData = reactive({
  name: undefined as string | undefined,
  processDefinitionKey: undefined as string | undefined,
  category: undefined as string | undefined,
  status: -1, // -1 表示全部
  createTime: [undefined, undefined] as [number | undefined, number | undefined],
}) // 搜索表单数据
const visible = ref(false) // 搜索弹窗显示状态
const pickerVisible = ref<Record<string, boolean>>({}) // 下拉选择器显示状态

/** 搜索条件 placeholder 拼接 */
const placeholder = computed(() => {
  const conditions: string[] = []
  if (formData.name) {
    conditions.push(`名称:${formData.name}`)
  }
  if (formData.status !== -1) {
    conditions.push(`状态:${getDictLabel(DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS, formData.status)}`)
  }
  if (formData.createTime?.[0] && formData.createTime?.[1]) {
    conditions.push(`时间:${formatDate(formData.createTime[0])}~${formatDate(formData.createTime[1])}`)
  }
  return conditions.length > 0 ? conditions.join(' | ') : '搜索待办任务'
})

const categoryList = ref<Category[]>([]) // 流程分类选项
const processDefinitionList = ref<ProcessDefinition[]>([]) // 流程定义选项

const visibleCreateTime = ref<[boolean, boolean]>([false, false]) // 发起时间选择器状态
const tempCreateTime = ref<[number, number]>([Date.now(), Date.now()]) // 发起时间临时值

/** 确认发起时间开始日期 */
function handleCreateTime0Confirm() {
  formData.createTime = [tempCreateTime.value[0], formData.createTime?.[1]]
  visibleCreateTime.value[0] = false
}

/** 确认发起时间结束日期 */
function handleCreateTime1Confirm() {
  formData.createTime = [formData.createTime?.[0], tempCreateTime.value[1]]
  visibleCreateTime.value[1] = false
}

/** 获取流程分类列表 */
async function getCategoryList() {
  try {
    categoryList.value = await getCategorySimpleList()
  } catch (error) {
    console.error('获取流程分类失败:', error)
  }
}

/** 获取流程定义列表 */
async function getProcessDefinitions() {
  try {
    processDefinitionList.value = await getProcessDefinitionList({ suspensionState: 1 })
  } catch (error) {
    console.error('获取流程定义失败:', error)
  }
}

/** 搜索按钮操作 */
function handleSearch() {
  visible.value = false
  emit('search', {
    ...formData,
    status: formData.status === -1 ? undefined : formData.status,
    createTime: formatDateRange(formData.createTime),
  })
}

/** 重置按钮操作 */
function handleReset() {
  formData.name = undefined
  formData.processDefinitionKey = undefined
  formData.category = undefined
  formData.status = -1
  formData.createTime = [undefined, undefined]
  visible.value = false
  emit('reset')
}

/** 初始化 */
onMounted(() => {
  getCategoryList()
  getProcessDefinitions()
})
</script>
