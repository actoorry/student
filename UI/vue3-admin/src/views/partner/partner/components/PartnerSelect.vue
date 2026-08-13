<!--
  客商选择器（统一封装）：只读输入框 + 点击弹窗选择

  复用了 PartnerSelectDialog，通过 partnerKind 控制筛选客户/供应商。

  Props:
    modelValue  — 绑定的 partner ID（v-model）
    disabled    — 是否禁用
    clearable   — 是否允许清空（鼠标悬停时显示清除图标）
    placeholder — 占位文字
    partnerKind — 'customer' | 'supplier' | 'both'（默认 'both'）
  Events:
    update:modelValue — v-model 更新
    change(item)      — 选中变化时触发，传递完整 PartnerVO（清空时为 undefined）
-->
<template>
  <div
    v-bind="attrs"
    class="w-full"
    :class="disabled ? 'cursor-not-allowed' : 'cursor-pointer'"
    @click="handleClick"
    @mouseenter="hovering = true"
    @mouseleave="hovering = false"
  >
    <el-tooltip :disabled="!selectedItem" placement="top" :show-after="500">
      <template #content>
        <div v-if="selectedItem" class="leading-6">
          <div>名称：{{ selectedItem.name || selectedItem.nickname || '-' }}</div>
          <div>手机：{{ selectedItem.mobile || '-' }}</div>
          <div>电话：{{ selectedItem.telephone || '-' }}</div>
        </div>
      </template>
      <el-input
        :model-value="displayLabel"
        :placeholder="placeholder"
        :disabled="disabled"
        readonly
        :suffix-icon="suffixIcon"
        :class="disabled ? 'is-select-disabled' : 'is-select-clickable'"
      />
    </el-tooltip>
  </div>
  <!-- 弹窗必须放在 div 外部，否则弹窗内的点击事件会冒泡到 div 触发 handleClick -->
  <PartnerSelectDialog
    ref="dialogRef"
    :multiple="false"
    :is-customer="dialogIsCustomer"
    :is-supplier="dialogIsSupplier"
    @selected="handleSelected"
  />
</template>

<script setup lang="ts">
import * as PartnerApi from '@/api/partner/partner'
import { Search, CircleClose } from '@element-plus/icons-vue'
import PartnerSelectDialog from './PartnerSelectDialog.vue'

const attrs = useAttrs()

defineOptions({ name: 'PartnerSelect', inheritAttrs: false })

const props = withDefaults(
  defineProps<{
    modelValue?: number
    disabled?: boolean
    clearable?: boolean
    placeholder?: string
    partnerKind?: 'customer' | 'supplier' | 'both'
  }>(),
  {
    disabled: false,
    clearable: true,
    placeholder: '请选择客商',
    partnerKind: 'both'
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: number | undefined]
  change: [item: PartnerApi.PartnerVO | undefined]
}>()

const dialogRef = ref()
const hovering = ref(false)

/** 根据 partnerKind 计算弹窗筛选条件 */
const dialogIsCustomer = computed(() => props.partnerKind === 'customer' ? true : undefined)
const dialogIsSupplier = computed(() => props.partnerKind === 'supplier' ? true : undefined)

// ==================== 名称回显 ====================
const selectedItem = ref<PartnerApi.PartnerVO | undefined>()

const displayLabel = computed(() => {
  return selectedItem.value?.name || selectedItem.value?.nickname || ''
})

const showClear = computed(() => {
  return props.clearable && !props.disabled && hovering.value && props.modelValue != null
})

const suffixIcon = computed(() => {
  return showClear.value ? CircleClose : Search
})

/** 根据 ID 单条查询客商（用于编辑回显） */
const resolveItemById = async (id: number | undefined) => {
  if (id == null) {
    selectedItem.value = undefined
    return
  }
  if (selectedItem.value?.id === id) {
    return
  }
  try {
    selectedItem.value = await PartnerApi.getPartner(id)
  } catch (e) {
    console.error('[PartnerSelect] resolveItemById failed:', e)
  }
}

watch(
  () => props.modelValue,
  (val) => {
    resolveItemById(val)
  },
  { immediate: true }
)

// ==================== 点击交互 ====================

const handleClick = (e: MouseEvent) => {
  if (props.disabled) {
    return
  }
  const target = e.target as HTMLElement
  if (showClear.value && target.closest('.el-input__suffix')) {
    e.stopPropagation()
    selectedItem.value = undefined
    emit('update:modelValue', undefined)
    emit('change', undefined)
    return
  }
  const selectedIds = props.modelValue != null ? [props.modelValue] : []
  dialogRef.value.open(selectedIds)
}

const handleSelected = (rows: PartnerApi.PartnerVO[]) => {
  if (!rows || rows.length === 0) {
    return
  }
  const item = rows[0]
  selectedItem.value = item
  emit('update:modelValue', item.id)
  emit('change', item)
}
</script>

<style lang="scss" scoped>
.is-select-clickable {
  :deep(.el-input__wrapper),
  :deep(.el-input__inner) {
    cursor: pointer;
  }
}

.is-select-disabled {
  :deep(.el-input__wrapper),
  :deep(.el-input__inner) {
    cursor: not-allowed;
  }
}
</style>
