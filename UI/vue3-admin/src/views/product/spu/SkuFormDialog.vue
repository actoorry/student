<!-- SKU 维护独立弹窗 -->
<template>
  <el-dialog
    v-model="dialogVisible"
    :close-on-click-modal="false"
    :title="isDetail ? '查看 SKU' : 'SKU 维护'"
    top="5vh"
    width="85%"
  >
    <SkuForm
      v-if="dialogVisible"
      ref="skuFormRef"
      :is-detail="isDetail"
      :prop-form-data="localFormData"
    />
    <template #footer>
      <el-button v-if="!isDetail" type="primary" @click="handleConfirm">确 定</el-button>
      <el-button @click="handleCancel">取 消</el-button>
    </template>
  </el-dialog>
</template>
<script lang="ts" setup>
import { PropType } from 'vue'
import { cloneDeep } from 'lodash-es'
import SkuForm from './form/SkuForm.vue'
import type { Spu } from '@/api/product/spu'

defineOptions({ name: 'SkuFormDialog' })

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  propFormData: {
    type: Object as PropType<Spu>,
    default: () => ({})
  },
  isDetail: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'confirm', data: Partial<Spu>): void
}>()

const message = useMessage()
const skuFormRef = ref()
const dialogVisible = ref(false)

// 本地副本，避免直接修改 prop
const localFormData = ref<Spu>({})

// 同步 visible
watch(
  () => props.visible,
  (val) => {
    dialogVisible.value = val
    if (val) {
      localFormData.value = cloneDeep(props.propFormData)
    }
  }
)

// 同步关闭到父组件
watch(dialogVisible, (val) => {
  if (!val) emit('update:visible', false)
})

/** 确定：验证并回写数据 */
const handleConfirm = async () => {
  try {
    await unref(skuFormRef)?.validate()
    emit('confirm', {
      specType: localFormData.value.specType,
      subCommissionType: localFormData.value.subCommissionType,
      skus: localFormData.value.skus
    })
    dialogVisible.value = false
  } catch {
    // 验证失败，SkuForm 内部已提示错误
  }
}

/** 取消：关闭弹窗不保存 */
const handleCancel = () => {
  dialogVisible.value = false
}

/** 组件销毁时关闭弹窗 */
onBeforeUnmount(() => {
  dialogVisible.value = false
})
</script>
