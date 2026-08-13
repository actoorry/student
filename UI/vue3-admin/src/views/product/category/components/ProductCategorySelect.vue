<template>
  <el-tree-select
    v-model="selectCategoryId"
    :data="categoryList"
    :props="defaultProps"
    :multiple="multiple"
    :show-checkbox="multiple"
    :check-strictly="checkStrictly"
    :expand-on-click-node="resolvedExpandOnClickNode"
    class="w-1/1"
    node-key="id"
    placeholder="请选择产品分类"
  />
</template>
<script lang="ts" setup>
import { defaultProps, handleTree } from '@/utils/tree'
import * as ProductCategoryApi from '@/api/product/category'
import { oneOfType } from 'vue-types'
import { propTypes } from '@/utils/propTypes'
import { CommonStatusEnum } from '@/utils/constants'

/** 产品分类选择组件 */
defineOptions({ name: 'ProductCategorySelect' })

const props = defineProps({
  // 选中的ID
  modelValue: oneOfType<number | number[]>([Number, Array<Number>]),
  // 是否多选
  multiple: propTypes.bool.def(false),
  // 是否允许选择父分类（有子分类的节点）
  checkStrictly: propTypes.bool.def(false),
  // 点击节点文字时是否展开；false 时仅点击展开图标才会展开
  expandOnClickNode: propTypes.bool.def(undefined),
  // 上级品类的编号
  parentId: propTypes.number.def(undefined),
  // 管理分类页面不使用该选择器；业务选择器默认只读取启用分类。
  status: propTypes.number.def(CommonStatusEnum.ENABLE)
})

/** 点击展开图标展开；点击文字时选中（checkStrictly 模式下） */
const resolvedExpandOnClickNode = computed(() => {
  if (props.expandOnClickNode !== undefined) {
    return props.expandOnClickNode
  }
  return !props.checkStrictly
})

/** 选中的分类 ID */
const selectCategoryId = computed({
  get: () => {
    return props.modelValue
  },
  set: (val: number | number[]) => {
    emit('update:modelValue', val)
  }
})

/** 分类选择 */
const emit = defineEmits(['update:modelValue'])

/** 初始化 **/
const categoryList = ref<ProductCategoryApi.CategoryVO[]>([]) // 分类树
const loadCategoryList = async () => {
  // 获得分类树
  const data = await ProductCategoryApi.getCategoryList({ parentId: props.parentId, status: props.status })
  categoryList.value = handleTree(data, 'id', 'parentId')
}

onMounted(loadCategoryList)
watch(() => [props.parentId, props.status], loadCategoryList)
</script>
