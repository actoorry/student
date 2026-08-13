<!-- 表单详情：流程表单/业务表单 -->
<template>
  <view class="mx-24rpx mt-24rpx overflow-hidden rounded-16rpx bg-white">
    <!-- 标题 -->
    <view class="px-24rpx pt-24rpx text-28rpx text-[#333] font-bold">
      审批详情
    </view>
    <!-- 表单内容：业务表单 -->
    <template v-if="processDefinition?.formType === BpmModelFormType.CUSTOM">
      <!-- OA 请假详情 -->
      <LeaveDetail
        v-if="processDefinition?.formCustomViewPath === '/bpm/oa/leave/detail'"
        :id="processInstance?.businessKey"
        embedded
      />
      <!-- 未配置的业务表单 -->
      <view v-else class="px-24rpx py-32rpx text-26rpx text-[#999]">
        暂不支持该业务表单，请参考 LeaveDetail 配置
      </view>
    </template>
    <!-- 表单内容：流程表单 -->
    <template v-else-if="processDefinition?.formType === BpmModelFormType.NORMAL">
      <FormCreate
        v-if="normalForm.rule.length > 0"
        v-model="normalForm.value"
        v-model:api="normalFormApi"
        :option="normalForm.option"
        :rule="normalForm.rule"
      />
      <view v-else class="px-24rpx py-32rpx text-26rpx text-[#999]">
        暂无流程表单数据
      </view>
    </template>
  </view>
</template>

<script lang="ts" setup>
import type { ProcessDefinition, ProcessInstance } from '@/api/bpm/processInstance'
import type { FormCreateApi } from '@/pages-bpm/components/form-create/packages/wot-ui/types'
import type { FormCreatePreview } from '@/pages-bpm/utils'
import { nextTick, ref, watch } from 'vue'
// 特殊：业务表单组件（uniapp 小程序不支持动态组件，需要静态导入）
import FormCreate from '@/pages-bpm/components/form-create/packages/wot-ui/src/index.vue'
import { FORM_FIELD_PERMISSION } from '@/pages-bpm/components/form-create/types/typing'
import LeaveDetail from '@/pages-bpm/oa/leave/detail/index.vue'
import { setConfAndFields2 } from '@/pages-bpm/utils'
import { BpmModelFormType } from '@/utils/constants'

const props = defineProps<{
  /** 流程定义 */
  processDefinition?: ProcessDefinition
  /** 流程实例 */
  processInstance?: ProcessInstance
  /** 流程表单字段权限 */
  formFieldsPermission?: Record<string, string>
}>()

const normalFormApi = ref<FormCreateApi>()
const writableFields = ref<string[]>([])
const normalForm = ref<FormCreatePreview>({
  option: {},
  rule: [],
  value: {},
})

/** 初始化 */
watch(
  () => [
    props.processDefinition?.formConf,
    props.processDefinition?.formFields,
    props.processDefinition?.formType,
    props.processInstance?.formVariables,
  ],
  () => {
    if (props.processDefinition?.formType !== BpmModelFormType.NORMAL) {
      normalForm.value = { option: {}, rule: [], value: {} }
      writableFields.value = []
      return
    }
    setConfAndFields2(
      normalForm,
      props.processDefinition?.formConf,
      props.processDefinition?.formFields,
      props.processInstance?.formVariables || {},
    )
    normalForm.value.option = {
      ...normalForm.value.option,
      submitBtn: false,
      resetBtn: false,
    }
    applyFieldPermission()
  },
  { deep: true, immediate: true },
)

/** 表单 API 或字段权限变化后重新应用权限 */
watch(
  () => [normalFormApi.value, props.formFieldsPermission, normalForm.value.rule],
  () => applyFieldPermission(),
  { deep: true, immediate: true },
)

/** 应用流程表单字段权限 */
async function applyFieldPermission() {
  await nextTick()
  const api = normalFormApi.value
  if (!api || props.processDefinition?.formType !== BpmModelFormType.NORMAL) {
    return
  }

  writableFields.value = []
  // 默认展示全部字段并禁用编辑，再按后端字段权限逐项放开或隐藏
  api.hidden(false)
  api.disabled(true)

  Object.entries(props.formFieldsPermission || {}).forEach(([field, permission]) => {
    api.setFieldPermission(field, permission)
    if (permission === FORM_FIELD_PERMISSION.WRITE) {
      writableFields.value.push(field)
    }
  })
}

/** 校验当前流程表单 */
async function validate() {
  if (!normalFormApi.value) {
    return { valid: true, errors: [] }
  }
  return normalFormApi.value.validate()
}

/** 获取当前节点允许编辑的流程变量 */
function getWritableVariables() {
  const formData = normalFormApi.value?.formData() || normalForm.value.value || {}
  return writableFields.value.reduce<Record<string, any>>((variables, field) => {
    variables[field] = formData[field]
    return variables
  }, {})
}

defineExpose({
  validate,
  getWritableVariables,
  writableFields,
})
</script>
