<!-- 产品发布 - 物流设置 -->
<template>
  <el-form ref="formRef" :model="formData" :rules="rules" label-width="120px" :disabled="isDetail">
    <el-form-item label="配送方式" prop="deliveryTypes">
      <el-checkbox-group v-model="formData.deliveryTypes" class="w-80">
        <el-checkbox
          v-for="dict in deliveryTypeOptions"
          :key="dict.value"
          :value="dict.value"
        >
          {{ dict.label }}
        </el-checkbox>
      </el-checkbox-group>
    </el-form-item>
    <el-form-item
      label="运费模板"
      prop="deliveryTemplateId"
      v-if="formData.deliveryTypes?.includes(DeliveryTypeEnum.EXPRESS.type)"
    >
      <el-select placeholder="请选择运费模板" v-model="formData.deliveryTemplateId" class="w-80">
        <el-option
          v-for="item in deliveryTemplateList"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
    </el-form-item>
  </el-form>
</template>
<script lang="ts" setup>
import { PropType } from 'vue'
import { copyValueToTarget } from '@/utils'
import { propTypes } from '@/utils/propTypes'
import type { Spu } from '@/api/product/spu'
import * as ExpressTemplateApi from '@/api/sales/delivery/expressTemplate'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { DeliveryTypeEnum, ProductTypeEnum } from '@/utils/constants'

defineOptions({ name: 'ProductDeliveryForm' })

const message = useMessage() // 消息弹窗

const props = defineProps({
  propFormData: {
    type: Object as PropType<Spu>,
    default: () => {}
  },
  isDetail: propTypes.bool.def(false) // 是否作为详情组件
})
const formRef = ref() // 表单 Ref
const formData = reactive<Spu>({
  deliveryTypes: [], // 配送方式
  deliveryTemplateId: undefined, // 运费模版
  type: ProductTypeEnum.ENTITY.type
})
const validateDeliveryTemplateId = (
  _rule: any,
  value: number | undefined,
  callback: (error?: Error) => void
) => {
  if (formData.deliveryTypes?.includes(DeliveryTypeEnum.EXPRESS.type) && !value) {
    callback(new Error('快递发货必须选择运费模板'))
    return
  }
  callback()
}
const rules = reactive({
  deliveryTypes: [required],
  deliveryTemplateId: [{ validator: validateDeliveryTemplateId, trigger: 'change' }]
})

const deliveryTypeOptions = computed(() => {
  // 配送方式不根据商品类型过滤，展示全部四种配送方式
  return getIntDictOptions(DICT_TYPE.TRADE_DELIVERY_TYPE)
})

const applyDeliveryDefaults = () => {
  // 不再根据商品类型自动过滤或强制默认配送方式，由用户自由选择
  if (!formData.deliveryTypes?.includes(DeliveryTypeEnum.EXPRESS.type)) {
    formData.deliveryTemplateId = undefined
  }
}

/** 将传进来的值赋值给 formData */
watch(
  () => props.propFormData,
  (data) => {
    if (!data) {
      return
    }
    copyValueToTarget(formData, data)
    applyDeliveryDefaults()
  },
  {
    immediate: true
  }
)

watch(() => formData.type, applyDeliveryDefaults)
watch(() => formData.deliveryTypes, applyDeliveryDefaults, { deep: true })

/** 表单校验 */
const emit = defineEmits(['update:activeName'])
const validate = async () => {
  if (!formRef) return
  try {
    await unref(formRef)?.validate()
    // 校验通过更新数据
    Object.assign(props.propFormData, {
      deliveryTypes: formData.deliveryTypes,
      deliveryTemplateId: formData.deliveryTemplateId
    })
  } catch (e) {
    message.error('【物流设置】不完善，请填写相关信息')
    emit('update:activeName', 'delivery')
    throw e // 目的截断之后的校验
  }
}
defineExpose({ validate })

/** 初始化 */
const deliveryTemplateList = ref([]) // 运费模版
onMounted(async () => {
  deliveryTemplateList.value = await ExpressTemplateApi.getSimpleTemplateList()
})
</script>
