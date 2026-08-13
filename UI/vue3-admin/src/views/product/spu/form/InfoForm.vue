<!-- 产品发布 - 基础设置 -->
<template>
  <el-form ref="formRef" :disabled="isDetail" :model="formData" :rules="rules" label-width="120px">
    <el-form-item label="产品名称" prop="name">
      <el-input
        v-model="formData.name"
        :autosize="{ minRows: 2, maxRows: 2 }"
        :clearable="true"
        :show-word-limit="true"
        class="w-80!"
        maxlength="64"
        placeholder="请输入产品名称"
        type="textarea"
      />
    </el-form-item>
    <el-form-item label="产品分类" prop="categorySaels">
      <el-cascader
        v-model="formData.categorySales"
        :options="categoryList"
        :props="defaultProps"
        class="w-80!"
        clearable
        filterable
        placeholder="请选择产品分类"
      />
      <el-button :icon="RefreshRight" @click="refreshCategoryList" class="ml-1" size="small" />
      <el-alert
        v-if="hasLegacyDirectRootAssignment"
        title="该商品仍直接引用分类维度根，属于待迁移数据；请选择真实销售分类后才能保存。"
        type="warning"
        :closable="false"
        class="mt-8px w-80!"
      />
    </el-form-item>
    <el-form-item label="产品品牌" prop="brandId">
      <el-select v-model="formData.brandId" class="w-80!" placeholder="请选择产品品牌">
        <el-option
          v-for="item in brandList"
          :key="item.id"
          :label="item.name"
          :value="item.id as number"
        />
      </el-select>
      <el-button :icon="RefreshRight" @click="refreshBrandList" class="ml-1" size="small" />
    </el-form-item>
    <el-form-item label="产品类型" prop="type">
      <el-radio-group v-model="formData.type" class="w-80!">
        <el-radio :value="ProductTypeEnum.ENTITY.type">{{ ProductTypeEnum.ENTITY.name }}</el-radio>
        <el-radio :value="ProductTypeEnum.SERVICE.type">{{ ProductTypeEnum.SERVICE.name }}</el-radio>
        <el-radio :value="ProductTypeEnum.COMBINATION.type">{{
          ProductTypeEnum.COMBINATION.name
        }}</el-radio>
        <el-radio :value="ProductTypeEnum.MEMBER.type">{{ ProductTypeEnum.MEMBER.name }}</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="是否军创区" prop="isMilitary">
      <el-radio-group v-model="formData.isMilitary" class="w-80!">
        <el-radio :value="false">否</el-radio>
        <el-radio :value="true">是</el-radio>
      </el-radio-group>
      <div class="w-full text-12px text-gray-500">仅用于军创区专区展示范围，不影响商品类型、配送、价格与履约。</div>
    </el-form-item>
    <el-form-item label="微信虚拟商品" prop="isWechatMiniappVirtualGoods">
      <el-radio-group
        v-model="formData.isWechatMiniappVirtualGoods"
        :disabled="Boolean(formData.id)"
        class="w-80!"
      >
        <el-radio :value="false">否</el-radio>
        <el-radio :value="true">是</el-radio>
      </el-radio-group>
      <div class="w-full text-12px text-gray-500">
        开启后默认下架、禁止加购，仅可在微信小程序直接购买；创建后不可修改，
        如需变更请回收原商品后重新创建。
      </div>
    </el-form-item>
    <el-form-item label="单位类型">
      <el-select
        v-model="selectedUnitType"
        class="w-80!"
        clearable
        placeholder="请先选择单位类型"
        @change="handleUnitTypeChange"
      >
        <el-option
          v-for="item in unitTypeOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="计价单位" prop="unitId">
      <el-select
        v-model="formData.unitId"
        :disabled="selectedUnitType === undefined"
        class="w-80!"
        clearable
        :placeholder="selectedUnitType === undefined ? '请先选择单位类型' : '请选择计价单位'"
      >
        <el-option
          v-for="item in filteredUnitList"
          :key="item.id"
          :label="item.name"
          :value="item.id as number"
        />
      </el-select>
      <el-button :icon="RefreshRight" @click="refreshUnitList" class="ml-1" size="small" />
    </el-form-item>
    <el-form-item label="产品关键字" prop="keyword">
      <el-input v-model="formData.keyword" class="w-80!" placeholder="请输入产品关键字" />
    </el-form-item>
    <el-form-item label="产品简介" prop="introduction">
      <el-input
        v-model="formData.introduction"
        :autosize="{ minRows: 2, maxRows: 2 }"
        :clearable="true"
        :show-word-limit="true"
        class="w-80!"
        maxlength="128"
        placeholder="请输入产品简介"
        type="textarea"
      />
    </el-form-item>
    <el-form-item label="归属城市" prop="cityId">
      <AreaSelect
        v-model="cityAreaPath"
        :level="AreaLevelEnum.CITY"
        placeholder="同城特产展示城市（选到市级）"
        :show-all-levels="true"
      />
    </el-form-item>
    <el-form-item label="产品封面图" prop="picUrl">
      <UploadImg v-model="formData.picUrl" :disabled="isDetail" height="80px" />
    </el-form-item>
    <el-form-item label="产品轮播图" prop="sliderPicUrls">
      <UploadImgs v-model="formData.sliderPicUrls" :disabled="isDetail" />
    </el-form-item>
  </el-form>
</template>
<script lang="ts" setup>
import { PropType } from 'vue'
import { copyValueToTarget } from '@/utils'
import { propTypes } from '@/utils/propTypes'
import { defaultProps, handleTree } from '@/utils/tree'
import type { Spu } from '@/api/product/spu'
import * as ProductCategoryApi from '@/api/product/category'
import { CategoryVO } from '@/api/product/category'
import * as ProductBrandApi from '@/api/product/brand'
import { BrandVO } from '@/api/product/brand'
import * as ProductUnitApi from '@/api/product/unit'
import { UnitVO } from '@/api/product/unit'
import { RefreshRight } from '@element-plus/icons-vue'
import { ProductTypeEnum, AreaLevelEnum, CommonStatusEnum } from '@/utils/constants'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import AreaSelect from '@/components/FormCreate/src/components/AreaSelect.vue'
import { getAreaTree } from '@/api/system/area'
import {
  SALES_CATEGORY_ROOT_ID,
  WAREHOUSE_CATEGORY_ROOT_ID
} from '../../category/categoryDimension'

const route = useRoute() // 路由

defineOptions({ name: 'ProductSpuInfoForm' })
const props = defineProps({
  propFormData: {
    type: Object as PropType<Spu>,
    default: () => {}
  },
  isDetail: propTypes.bool.def(false) // 是否作为详情组件
})

const message = useMessage() // 消息弹窗

const formRef = ref() // 表单 Ref
const formData = reactive<Spu>({
  name: '', // 产品名称
  categorySales: undefined, // 产品分类
  keyword: '', // 关键字
  picUrl: '', // 产品封面图
  sliderPicUrls: [], // 产品轮播图
  introduction: '', // 产品简介
  brandId: undefined, // 产品品牌
  unitId: undefined, // 主单位
  type: ProductTypeEnum.ENTITY.type, // 产品类型
  isWechatMiniappVirtualGoods: false,
  isMilitary: false, // 是否军创区商品
  cityId: undefined // 归属城市
})
const cityAreaPath = ref<number[] | undefined>()
const brandList = ref<BrandVO[]>([]) // 产品品牌列表
const unitList = ref<UnitVO[]>([]) // 产品单位列表
const categoryList = ref<CategoryVO[]>([]) // 产品分类树
const hasLegacyDirectRootAssignment = computed(
  () => formData.categorySales === SALES_CATEGORY_ROOT_ID || formData.categorySales === WAREHOUSE_CATEGORY_ROOT_ID
)
const selectedUnitType = ref<number | undefined>()
const unitTypeOptions = computed(() => getIntDictOptions(DICT_TYPE.PRODUCT_UNIT_TYPE))
const filteredUnitList = computed(() =>
  unitList.value.filter((item) => item.type === selectedUnitType.value)
)

function syncSelectedUnitType() {
  selectedUnitType.value = unitList.value.find((item) => item.id === formData.unitId)?.type
}

function handleUnitTypeChange() {
  formData.unitId = undefined
}

function findAreaPath(nodes: any[], targetId: number, path: number[] = []): number[] | undefined {
  for (const node of nodes) {
    const next = [...path, node.id]
    if (Number(node.id) === Number(targetId)) {
      return next
    }
    if (node.children?.length) {
      const found = findAreaPath(node.children, targetId, next)
      if (found) {
        return found
      }
    }
  }
  return undefined
}

async function syncCityAreaPath(cityId?: number) {
  if (!cityId) {
    cityAreaPath.value = undefined
    return
  }
  const tree = await getAreaTree()
  cityAreaPath.value = findAreaPath(tree || [], cityId)
}

watch(cityAreaPath, (path) => {
  if (!path?.length) {
    formData.cityId = undefined
    return
  }
  formData.cityId = path[path.length - 1]
})
const rules = reactive({
  name: [required],
  keyword: [required],
  introduction: [required],
  picUrl: [required],
  sliderPicUrls: [required],
  brandId: [required],
  type: [required],
  isWechatMiniappVirtualGoods: [required]
})

/** 将传进来的值赋值给 formData */
watch(
  () => props.propFormData,
  (data) => {
    if (!data) {
      return
    }
    copyValueToTarget(formData, data)
    syncCityAreaPath(formData.cityId)
    syncSelectedUnitType()
  },
  {
    immediate: true
  }
)

/** 表单校验 */
const emit = defineEmits(['update:activeName'])
const validate = async () => {
  if (!formRef) return
  try {
    await unref(formRef)?.validate()
    // 校验通过更新数据
    Object.assign(props.propFormData, formData)
  } catch (e) {
    message.error('【基础设置】不完善，请填写相关信息')
    emit('update:activeName', 'info')
    throw e // 目的截断之后的校验
  }
}

/** 打开 SKU 维护前同步其依赖的当前基础信息，避免未保存的单位和类型仍使用旧值 */
const syncSkuContext = () => {
  Object.assign(props.propFormData, {
    unitId: formData.unitId,
    type: formData.type,
    isWechatMiniappVirtualGoods: formData.isWechatMiniappVirtualGoods
  })
}
defineExpose({ validate, syncSkuContext })

/** 初始化 */

// const queryParams = reactive({
//   parentId: null /** 分类的夫菜单*/
// }) // 查询参数
async function refreshCategoryList() {
  // 获得分类树
  const data = await ProductCategoryApi.getCategoryList({
    parentId: SALES_CATEGORY_ROOT_ID,
    status: CommonStatusEnum.ENABLE
  })
  categoryList.value = handleTree(data, 'id')
  // categoryList.value = queryParams.categoryList
}

async function refreshBrandList() {
  brandList.value = await ProductBrandApi.getSimpleBrandList()
}

async function refreshUnitList() {
  unitList.value = await ProductUnitApi.getUnitSimpleList()
  syncSelectedUnitType()
}

const queryParams = reactive({
  name: undefined,
  categoryList: [],
  parentId: SALES_CATEGORY_ROOT_ID
})
onMounted(async () => {
  if (route.query) {
    Object.assign(queryParams, route.query)
  }
  await refreshCategoryList()
  // 获取产品品牌列表
  await refreshBrandList()
  await refreshUnitList()
})
</script>
