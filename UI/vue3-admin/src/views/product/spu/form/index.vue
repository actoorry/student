<template>
  <ContentWrap v-loading="formLoading">
    <!-- 瀑布流布局容器 -->
    <el-scrollbar ref="scrollbarRef" class="spu-form-scrollbar">
      <div class="spu-form-waterfall">
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

    <!-- 底部操作栏 -->
    <el-form class="mt-16px">
      <el-form-item style="float: right">
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
        <el-button v-if="!isDetail" :loading="formLoading" type="primary" @click="submitForm">
          保存
        </el-button>
        <el-button @click="close">返回</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- SKU 维护弹窗 -->
  <SkuFormDialog
    v-model:visible="skuDialogVisible"
    :is-detail="isDetail"
    :prop-form-data="formData"
    @confirm="onSkuConfirm"
  />
</template>
<script lang="ts" setup>
import { cloneDeep } from 'lodash-es'
import { useTagsViewStore } from '@/store/modules/tagsView'
import * as ProductSpuApi from '@/api/product/spu'
import InfoForm from './InfoForm.vue'
import DescriptionForm from './DescriptionForm.vue'
import OtherForm from './OtherForm.vue'
import DeliveryForm from './DeliveryForm.vue'
import SkuFormDialog from '@/views/product/spu/SkuFormDialog.vue'
import { convertToInteger, floatToFixed2, formatToFraction } from '@/utils'
import { isEmpty } from '@/utils/is'

defineOptions({ name: 'ProductSpuAdd' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const { push, currentRoute } = useRouter() // 路由
const { params, name } = useRoute() // 查询参数
const { delView } = useTagsViewStore() // 视图操作

const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const isDetail = ref(false) // 是否查看详情
const infoRef = ref() // 产品信息 Ref
const deliveryRef = ref() // 物流设置 Ref
const descriptionRef = ref() // 产品详情 Ref
const otherRef = ref() // 其他设置 Ref
const sectionInfo = ref() // 基础信息区块
const sectionDelivery = ref() // 物流设置区块
const sectionDescription = ref() // 产品详情区块
const sectionOther = ref() // 其他设置区块
const skuDialogVisible = ref(false) // SKU 弹窗可见性

// SPU 表单数据
const formData = ref<ProductSpuApi.Spu>({
  name: '', // 产品名称
  categorySales: undefined, // 产品分类
  keyword: '', // 关键字
  picUrl: '', // 产品封面图
  sliderPicUrls: [], // 产品轮播图
  introduction: '', // 产品简介
  unitId: undefined, // 主单位
  deliveryTypes: [], // 配送方式数组
  deliveryTemplateId: undefined, // 运费模版
  brandId: undefined, // 产品品牌
  specType: false, // 产品规格
  subCommissionType: false, // 分销类型
  skus: [
    {
      name: '', // SKU 名称，提交时会自动使用 SPU 名称
      price: 0, // 产品价格
      marketPrice: 0, // 市场价
      costPrice: 0, // 成本价
      barCode: '', // 产品条码
      picUrl: '', // 图片地址
      stock: 0, // 库存
      quantity: undefined, // 主单位数量
      weight: 0, // 产品重量
      volume: 0, // 产品体积
      firstBrokeragePrice: 0, // 一级分销的佣金
      secondBrokeragePrice: 0 // 二级分销的佣金
    }
  ],
  description: '', // 产品详情
  sort: 0, // 产品排序
  giveIntegral: 0, // 赠送积分
  virtualSalesCount: 0, // 虚拟销量
  isSale: false, // 是否可销售
  isPurchase: false, // 是否可采购
  isMes: false, // 是否 MES 管理
  isMilitary: false, // 是否军创区商品
  type: 1, // 产品类型：1-实体产品
  isWechatMiniappVirtualGoods: false
})

/** 获得详情 */
const getDetail = async () => {
  if ('ProductSpuDetail' === name) {
    isDetail.value = true
  }
  const id = params.id as unknown as number
  if (id) {
    formLoading.value = true
    try {
      const res = (await ProductSpuApi.getSpu(id)) as ProductSpuApi.Spu
      res.skus?.forEach((item) => {
        if (isDetail.value) {
          item.price = floatToFixed2(item.price)
          item.marketPrice = floatToFixed2(item.marketPrice)
          item.costPrice = floatToFixed2(item.costPrice)
          item.firstBrokeragePrice = floatToFixed2(item.firstBrokeragePrice)
          item.secondBrokeragePrice = floatToFixed2(item.secondBrokeragePrice)
        } else {
          // 回显价格分转元
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
}

/** 打开 SKU 弹窗 */
const openSkuDialog = () => {
  infoRef.value?.syncSkuContext()
  skuDialogVisible.value = true
}

/** SKU 弹窗确认回写 */
const onSkuConfirm = (data: Partial<ProductSpuApi.Spu>) => {
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
    other: sectionOther
  }
  const section = sectionMap[sectionName]
  if (section?.value?.$el) {
    section.value.$el.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

/** 提交按钮 */
const submitForm = async () => {
  // 提交请求
  formLoading.value = true
  try {
    // 校验各表单
    await unref(infoRef)?.validate()
    await unref(deliveryRef)?.validate()
    await unref(descriptionRef)?.validate()
    await unref(otherRef)?.validate()
    // 深拷贝一份, 这样最终 server 端不满足，不需要影响原始数据
    const deepCopyFormData = cloneDeep(unref(formData.value)) as ProductSpuApi.Spu
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
      // 给sku name赋值（使用产品名称作为 SKU 名称）
      item.name = deepCopyFormData.name
      // sku相关价格元转分
      item.price = convertToInteger(item.price)
      item.marketPrice = convertToInteger(item.marketPrice)
      item.costPrice = convertToInteger(item.costPrice)
      item.firstBrokeragePrice = convertToInteger(item.firstBrokeragePrice)
      item.secondBrokeragePrice = convertToInteger(item.secondBrokeragePrice)
    })
    // 处理轮播图列表
    const newSliderPicUrls: any[] = []
    deepCopyFormData.sliderPicUrls!.forEach((item: any) => {
      // 如果是前端选的图
      typeof item === 'object' ? newSliderPicUrls.push(item.url) : newSliderPicUrls.push(item)
    })
    deepCopyFormData.sliderPicUrls = newSliderPicUrls
    // 校验都通过后提交表单
    const data = deepCopyFormData as ProductSpuApi.Spu
    const id = params.id as unknown as number
    if (!id) {
      await ProductSpuApi.createSpu(data)
      message.success(t('common.createSuccess'))
    } else {
      await ProductSpuApi.updateSpu(data)
      message.success(t('common.updateSuccess'))
    }
    close()
  } finally {
    formLoading.value = false
  }
}

/** 关闭按钮 */
const close = () => {
  delView(unref(currentRoute))
  push({ name: 'ProductSpu' })
}

/** 初始化 */
onMounted(async () => {
  await getDetail()
})
</script>
<style lang="scss" scoped>
.spu-form-scrollbar {
  max-height: calc(100vh - 200px);
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
