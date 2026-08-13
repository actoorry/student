<template>
  <yd-tree-select
    v-model="selectedValue"
    :data="deptOptions"
    :props="treeProps"
    :label="label"
    :label-width="labelWidth"
    :prop="prop || undefined"
    :placeholder="placeholder"
    :disabled="disabled"
    check-strictly
  />
</template>

<script lang="ts" setup>
import type { Dept } from '@/api/system/dept'
import { computed, onMounted, ref, watch } from 'vue'
import { getSimpleDeptList } from '@/api/system/dept'
import { handleTree } from '@/utils/tree'

const props = withDefaults(defineProps<{
  disabled?: boolean
  labelWidth?: string
  modelValue?: number
  label?: string
  placeholder?: string
  prop?: string
  showRoot?: boolean // 是否显示顶级部门节点
}>(), {
  disabled: false,
  label: '上级部门',
  labelWidth: '180rpx',
  placeholder: '请选择部门',
  prop: '',
  showRoot: false,
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: number | undefined): void
}>()

const deptList = ref<Dept[]>([]) // 部门列表
const selectedValue = ref<number | undefined>(props.showRoot ? 0 : undefined) // 当前选中部门编号
const treeProps = {
  children: 'children',
  label: 'name',
  value: 'id',
} // 树字段映射

const deptOptions = computed(() => { // 部门树形选项
  const topDepts = handleTree(deptList.value)
  return props.showRoot
    ? [{ id: 0, name: '顶级部门' }, ...topDepts]
    : topDepts
})

/** 监听外部值变化，回显选中值 */
watch(
  () => props.modelValue,
  (val) => {
    selectedValue.value = val ?? (props.showRoot ? 0 : undefined)
  },
  { immediate: true },
)

/** 监听选中值变化，更新外部值 */
watch(selectedValue, (value) => {
  emit('update:modelValue', value === undefined ? (props.showRoot ? 0 : undefined) : Number(value))
})

/** 加载部门列表 */
async function loadDeptList() {
  deptList.value = await getSimpleDeptList()
}

/** 初始化 */
onMounted(() => {
  loadDeptList()
})
</script>
