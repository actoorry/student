<template>
  <wd-form-item
    title="岗位"
    title-width="180rpx"
    is-link
    :value="selectedLabel"
    placeholder="请选择岗位"
    @click="visible = true"
  />
  <wd-select-picker
    v-model="selectedIds"
    v-model:visible="visible"
    title="请选择岗位"
    :columns="postList"
    value-key="id"
    label-key="name"
    type="checkbox"
    filterable
    @update:model-value="handleChange"
  />
</template>

<script lang="ts" setup>
import type { Post } from '@/api/system/post'
import { computed, onMounted, ref, watch } from 'vue'
import { getSimplePostList } from '@/api/system/post'

const props = defineProps<{
  modelValue?: number[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: number[]): void
}>()

const postList = ref<Post[]>([])
const selectedIds = ref<number[]>([])
const visible = ref(false) // 选择弹窗显示状态

const selectedLabel = computed(() => {
  if (selectedIds.value.length === 0) {
    return ''
  }
  return selectedIds.value
    .map(id => postList.value.find(post => post.id === id)?.name)
    .filter(Boolean)
    .join('、')
})

watch(
  () => props.modelValue,
  (val) => {
    selectedIds.value = val || []
  },
  { immediate: true },
)

async function loadPostList() {
  postList.value = await getSimplePostList()
}

function handleChange(value: Array<boolean | number | string>) {
  emit('update:modelValue', value.map(Number))
}

onMounted(() => {
  loadPostList()
})
</script>
