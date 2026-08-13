<!-- SPU 产品详情/编辑弹窗（瀑布流布局） -->
<template>
  <el-dialog
    v-model="dialogVisible"
    :close-on-click-modal="false"
    :title="dialogTitle"
    top="2vh"
    width="85%"
  >
    <el-scrollbar ref="scrollbarRef" :height="scrollHeight" class="spu-form-scrollbar">
      <div v-loading="formLoading" class="spu-form-waterfall">
        <!-- 基础信息 -->
        <el-card ref="sectionInfo" class="section-card" shadow="never">
          <template #header>
            <div class="section-header">
              <span class="section-title">📋 基础信息</span>
            </div>
          </template>
          <InfoForm
            ref="infoRef"
            :is-detail="isDetail"
            :prop-form-data="formData"
            @update:active-name="onValidationError"
          />
        </el-card>

        <!-- 物流设置 -->
        <el-card ref="sectionDelivery" class="section-card" shadow="never">
          <template #header>
            <div class="section-header">
              <span class="section-title">🚚 物流设置</span>
            </div>
          </template>
          <DeliveryForm
            ref="deliveryRef"
            :is-detail="isDetail"
            :prop-form-data="formData"
            @update:active-name="onValidationError"
          />
        </el-card>

        <!-- 产品详情 -->
        <el-card ref="sectionDescription" class="section-card" shadow="never">
          <template #header>
            <div class="section-header">
              <span class="section-title">📝 产品详情</span>
            </div>
          </template>
          <DescriptionForm
            ref="descriptionRef"
            :is-detail="isDetail"
            :prop-form-data="formData"
            @update:active-name="onValidationError"
          />
        </el-card>

        <!-- 其它设置 -->
        <el-card ref="sectionOther" class="section-card" shadow="never">
          <template #header>
            <div class="section-header">
              <span class="section-title">⚙ 其它设置</span>
            </div>
          </template>
          <OtherForm
            ref="otherRef"
            :is-detail="isDetail"
            :prop-form-data="formData"
            @update:active-name="onValidationError"
          />
        </el-card>
      </div>
    </el-scrollbar>

    <template #footer>
      <el-button
        v-if="!isDetail"
        type="warning"
        @click="openSkuDialog"
      >
        SKU 维护
      </el-button>
      <el-button v-if="isDetail" type="info" @click="openSkuDialog">
        查看 SKU
      </el-button>
      <el-button v-if="!isDetail" :loading="submitting" type="primary" @click="submitForm">
        保 存
      </el-button>
      <el-button @click="handleCancel">取 消</el-button>
    </template>
  </el-dialog>

  <!-- SKU 维护弹窗 -->
  <SkuFormDialog
    v-model:visible="skuDialogVisible"
    :is-detail="isDetail"
    :prop-form-data="formData"
    @confirm="onSkuConfirm"
  />
</template>
<script lang="ts" setup>
import { PropType } from 'vue'
import { cloneDeep } from 'lodash-es'
import * as ProductSpuApi from '@/api/product/spu'
import type { Spu } from '@/api/product/spu'
import { convertToInteger, floatToFixed2, formatToFraction } from '@/utils'
import { isEmpty } from '@/utils/is'
import InfoForm from './form/InfoForm.vue'
import DeliveryForm from './form/DeliveryForm.vue'
import DescriptionForm from './form/DescriptionForm.vue'
import OtherForm from './form/OtherForm.vue'
import SkuFormDialog from './SkuFormDialog.vue'

defineOptions({ name: 'SpuFormDialog' })

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  spuId: {
    type: Number,
    default: undefined
  },
  mode: {
    type: String as PropType<'detail' | 'edit' | 'add'>,
    default: 'add'
  }
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const message = useMessage()
const { t } = useI18n()

// Refs
const scrollbarRef = ref()
const sectionInfo = ref()
const sectionDelivery = ref()
const sectionDescription = ref()
const sectionOther = ref()
const infoRef = ref()
const deliveryRef = ref()
const descriptionRef = ref()
const otherRef = ref()

// State
const dialogVisible = ref(false)
const formLoading = ref(false)
const submitting = ref(false)
const skuDialogVisible = ref(false)

const isDetail = computed(() => props.mode === 'detail')
const dialogTitle = computed(() => {
  if (props.mode === 'detail') return '产品详情'
  if (props.mode === 'edit') return '修改产品'
  return '添加产品'
})

// 弹窗可视区域高度（85% 宽度弹窗的内区高度）
const scrollHeight = computed(() => (window.innerHeight * 0.82) + 'px')

// 表单数据（保持与原 form/index.vue 相同的结构）
const formData = ref<Spu>({
  name: '',
  categorySales: undefined,
  keyword: '',
  picUrl: '',
  sliderPicUrls: [],
  introduction: '',
  unitId: undefined,
  deliveryTypes: [],
  deliveryTemplateId: undefined,
  brandId: undefined,
  specType: false,
  subCommissionType: false,
  skus: [
    {
      name: '',
      price: 0,
      marketPrice: 0,
      costPrice: 0,
      barCode: '',
      picUrl: '',
      stock: 0,
      quantity: undefined,
      weight: 0,
      volume: 0,
      firstBrokeragePrice: 0,
      secondBrokeragePrice: 0
    }
  ],
  description: '',
  sort: 0,
  giveIntegral: 0,
  virtualSalesCount: 0,
  isSale: false,
  isPurchase: false,
  isMes: false,
  isMilitary: false,
  type: 1
})

// 与外部 visible 双向同步
watch(
  () => props.visible,
  (val) => {
    dialogVisible.value = val
    if (val) {
      resetFormData()
      loadData()
    }
  }
)

watch(dialogVisible, (val) => {
  if (!val) {
    emit('update:visible', false)
  }
})

/** 重置表单数据 */
const resetFormData = () => {
  formData.value = {
    name: '',
    categorySales: undefined,
    keyword: '',
    picUrl: '',
    sliderPicUrls: [],
    introduction: '',
    unitId: undefined,
    deliveryTypes: [],
    deliveryTemplateId: undefined,
    brandId: undefined,
    specType: false,
    subCommissionType: false,
    skus: [
      {
        name: '',
        price: 0,
        marketPrice: 0,
        costPrice: 0,
        barCode: '',
        picUrl: '',
        stock: 0,
        quantity: undefined,
        weight: 0,
        volume: 0,
        firstBrokeragePrice: 0,
        secondBrokeragePrice: 0
      }
    ],
    description: '',
    sort: 0,
    giveIntegral: 0,
    virtualSalesCount: 0,
    isSale: false,
    isPurchase: false,
    isMes: false,
    isMilitary: false,
    type: 1
  }
}

/** 加载产品数据（编辑/详情模式） */
const loadData = async () => {
  if (!props.spuId) return
  formLoading.value = true
  try {
    const res = (await ProductSpuApi.getSpu(props.spuId)) as Spu
    res.skus?.forEach((item) => {
      if (isDetail.value) {
        item.price = floatToFixed2(item.price)
        item.marketPrice = floatToFixed2(item.marketPrice)
        item.costPrice = floatToFixed2(item.costPrice)
        item.firstBrokeragePrice = floatToFixed2(item.firstBrokeragePrice)
        item.secondBrokeragePrice = floatToFixed2(item.secondBrokeragePrice)
      } else {
        item.price = formatToFraction(item.price)
        item.marketPrice = formatToFraction(item.marketPrice)
        item.costPrice = formatToFraction(item.costPrice)
        item.firstBrokeragePrice = formatToFraction(item.firstBrokeragePrice)
        item.secondBrokeragePrice = formatToFraction(item.secondBrokeragePrice)
      }
    })
    formData.value = res
  } finally {
    formLoading.value = false
  }
}

/** 打开 SKU 弹窗 */
const openSkuDialog = () => {
  infoRef.value?.syncSkuContext()
  skuDialogVisible.value = true
}

/** SKU 弹窗确认回写 */
const onSkuConfirm = (data: Partial<Spu>) => {
  formData.value.specType = data.specType
  formData.value.subCommissionType = data.subCommissionType
  formData.value.skus = data.skus
}

/** 验证失败：滚动到对应区块 */
const onValidationError = (sectionName: string) => {
  const sectionMap: Record<string, any> = {
    info: sectionInfo,
    delivery: sectionDelivery,
    description: sectionDescription,
    other: sectionOther,
    sku: null // SKU 在独立弹窗，不处理
  }
  const section = sectionMap[sectionName]
  if (section?.value?.$el) {
    section.value.$el.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

/** 提交表单 */
const submitForm = async () => {
  submitting.value = true
  try {
    // 校验各表单
    await unref(infoRef)?.validate()
    await unref(deliveryRef)?.validate()
    await unref(descriptionRef)?.validate()
    await unref(otherRef)?.validate()

    // 深拷贝一份，避免服务端校验不通过时影响原始数据
    const deepCopyFormData = cloneDeep(unref(formData.value)) as Spu

    // 校验产品名称不能为空（用于 SKU name）
    if (isEmpty(deepCopyFormData.name)) {
      message.error('产品名称不能为空')
      return
    }

    // SKU 兜底检查
    if (!deepCopyFormData.skus || deepCopyFormData.skus.length === 0) {
      message.warning('请先维护 SKU 信息')
      return
    }

    deepCopyFormData.skus!.forEach((item) => {
      item.name = deepCopyFormData.name
      item.price = convertToInteger(item.price)
      item.marketPrice = convertToInteger(item.marketPrice)
      item.costPrice = convertToInteger(item.costPrice)
      item.firstBrokeragePrice = convertToInteger(item.firstBrokeragePrice)
      item.secondBrokeragePrice = convertToInteger(item.secondBrokeragePrice)
    })

    // 处理轮播图列表
    const newSliderPicUrls: any[] = []
    deepCopyFormData.sliderPicUrls!.forEach((item: any) => {
      typeof item === 'object' ? newSliderPicUrls.push(item.url) : newSliderPicUrls.push(item)
    })
    deepCopyFormData.sliderPicUrls = newSliderPicUrls

    const data = deepCopyFormData as Spu
    if (!props.spuId) {
      await ProductSpuApi.createSpu(data)
      message.success(t('common.createSuccess'))
    } else {
      await ProductSpuApi.updateSpu(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}

/** 取消按钮 */
const handleCancel = () => {
  dialogVisible.value = false
}

/** 组件销毁时强制关闭弹窗，防止遮罩层残留 */
onBeforeUnmount(() => {
  dialogVisible.value = false
  skuDialogVisible.value = false
})
</script>
<style lang="scss" scoped>
.spu-form-scrollbar {
  padding-right: 12px;
}

.spu-form-waterfall {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-card {
  :deep(.el-card__header) {
    padding: 12px 16px;
    background-color: #f5f7fa;
  }
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
</style>
