<!--
  客商选择器（表单内嵌组件）

  只读输入框，点击整行区域弹出 PartnerSelectDialog。
  通过 v-model 绑定 partnerId，选中后自动回显客商名称。

  Props:
    modelValue — partnerId（v-model 双向绑定）
  Events:
    update:modelValue — 选中/清空时触发
    select — 选中客商时触发，返回完整客商对象
  Expose:
    open() — 打开选择弹窗
-->
<template>
  <div class="partner-select" @click="openDialog">
    <el-input
      v-model="displayName"
      readonly
      placeholder="请选择客商"
      class="pointer-input"
    />
    <PartnerSelectDialog ref="dialogRef" :multiple="false" @selected="handleSelected" />
  </div>
</template>

<script setup lang="ts">
import * as PartnerApi from '@/api/partner/partner'
import PartnerSelectDialog from '@/views/partner/partner/components/PartnerSelectDialog.vue'

defineOptions({ name: 'PartnerSelect' })

const props = defineProps<{
  modelValue?: number | undefined // partnerId
}>()

const emit = defineEmits<{
  'update:modelValue': [value: number | undefined]
  select: [row: PartnerApi.PartnerVO | undefined]
}>()

const dialogRef = ref()
const displayName = ref('')

/** 打开弹窗 */
const openDialog = () => {
  dialogRef.value?.open(props.modelValue ? [props.modelValue] : [])
}

/** 选中回调 */
const handleSelected = (rows: PartnerApi.PartnerVO[]) => {
  if (rows.length > 0) {
    const row = rows[0]
    emit('update:modelValue', row.id)
    emit('select', row)
    displayName.value = row.nickname || row.name || ''
  }
}

/** 根据 modelValue 加载显示名称 */
const loadDisplayName = async (id: number | undefined) => {
  if (!id) {
    displayName.value = ''
    return
  }
  try {
    const data = await PartnerApi.getPartner(id)
    displayName.value = data.nickname || data.name || ''
  } catch {
    displayName.value = ''
  }
}

/** 监听 modelValue 变化，回显名称 */
watch(
  () => props.modelValue,
  (val) => {
    loadDisplayName(val)
  },
  { immediate: true }
)

defineExpose({ open: openDialog })
</script>

<style lang="scss" scoped>
.partner-select {
  width: 100%;
  cursor: pointer;

  :deep(.el-input__inner) {
    cursor: pointer;
  }
}
</style>
