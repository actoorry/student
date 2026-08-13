<!-- 产品分类侧边树组件，用于弹窗中选择 -->
<template>
  <el-tree
    ref="treeRef"
    :data="treeData"
    node-key="id"
    :props="{ label: 'name', children: 'children' }"
    :highlight-current="true"
    default-expand-all
    @node-click="handleNodeClick"
  />
</template>

<script setup lang="ts">
import * as ProductCategoryApi from '@/api/product/category'
import { handleTree } from '@/utils/tree'
import { CommonStatusEnum } from '@/utils/constants'

defineOptions({ name: 'ProductCategoryTree' })

const props = withDefaults(
  defineProps<{
    parentId?: number
    status?: number
  }>(),
  {
    parentId: undefined,
    status: CommonStatusEnum.ENABLE
  }
)

const emit = defineEmits<{
  'node-click': [data: ProductCategoryApi.CategoryVO | undefined]
}>()

const treeRef = ref()
const treeData = ref<ProductCategoryApi.CategoryVO[]>([])

/** 当前选中的节点 */
const currentNodeKey = ref<number>()

const handleNodeClick = (data: ProductCategoryApi.CategoryVO) => {
  // 点击同一个节点取消选中
  if (currentNodeKey.value === data.id) {
    currentNodeKey.value = undefined
    treeRef.value?.setCurrentKey(null)
    emit('node-click', undefined)
  } else {
    currentNodeKey.value = data.id
    emit('node-click', data)
  }
}

/** 加载分类树数据 */
const loadTreeData = async () => {
  const params: any = {}
  // 只有当 parentId 有值时才传递
  if (props.parentId !== undefined && props.parentId !== null) {
    params.parentId = props.parentId
  }
  params.status = props.status
  const data = await ProductCategoryApi.getCategoryList(params)
  treeData.value = handleTree(data, 'id','parentId')
}

onMounted(async () => {
  await loadTreeData()
})

// 监听 parentId 变化，支持动态更新
watch(() => [props.parentId, props.status], () => {
  loadTreeData()
})
</script>
