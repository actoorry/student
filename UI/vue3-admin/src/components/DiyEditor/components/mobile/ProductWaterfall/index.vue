<template>
  <div class="box-content min-h-30px w-full" ref="containerRef">
    <div class="goods-md-wrap flex flex-row flex-wrap">
      <div class="goods-list-box w-1/2" v-for="side in ['left', 'right']" :key="side">
        <div
          v-for="(spu, index) in side === 'left' ? leftList : rightList"
          :key="spu.id || index"
          class="relative box-content overflow-hidden bg-white"
          :style="{
            marginBottom: `${property.space}px`,
            paddingRight: side === 'left' ? `${property.space / 2}px` : '0',
            paddingLeft: side === 'right' ? `${property.space / 2}px` : '0',
            borderTopLeftRadius: `${property.borderRadiusTop}px`,
            borderTopRightRadius: `${property.borderRadiusTop}px`,
            borderBottomLeftRadius: `${property.borderRadiusBottom}px`,
            borderBottomRightRadius: `${property.borderRadiusBottom}px`
          }"
        >
          <div
            v-if="property.badge.show && property.badge.imgUrl"
            class="absolute left-0 top-0 z-1"
          >
            <el-image fit="cover" :src="property.badge.imgUrl" class="h-26px w-38px" />
          </div>
          <el-image fit="cover" class="h-140px w-full" :src="spu.picUrl" />
          <div class="flex flex-col gap-8px p-8px box-border">
            <div
              v-if="property.fields.name.show"
              class="truncate text-14px"
              :style="{ color: property.fields.name.color }"
            >
              {{ spu.name }}
            </div>
            <div
              v-if="property.fields.introduction.show"
              class="truncate text-12px"
              :style="{ color: property.fields.introduction.color }"
            >
              {{ spu.introduction }}
            </div>
            <div>
              <span
                v-if="property.fields.price.show"
                class="text-16px"
                :style="{ color: property.fields.price.color }"
              >
                ￥{{ fenToYuan(spu.price as any) }}
              </span>
              <span
                v-if="property.fields.marketPrice.show && spu.marketPrice"
                class="ml-4px text-10px line-through"
                :style="{ color: property.fields.marketPrice.color }"
              >
                ￥{{ fenToYuan(spu.marketPrice) }}
              </span>
            </div>
            <div class="text-12px">
              <span
                v-if="property.fields.salesCount.show"
                :style="{ color: property.fields.salesCount.color }"
              >
                已售{{ (spu.salesCount || 0) + (spu.virtualSalesCount || 0) }}件
              </span>
              <span
                v-if="property.fields.stock.show"
                :style="{ color: property.fields.stock.color }"
              >
                库存{{ spu.stock || 0 }}
              </span>
            </div>
          </div>
          <div class="absolute bottom-8px right-8px">
            <span
              v-if="property.btnBuy.type === 'text'"
              class="rounded-full p-x-12px p-y-4px text-12px text-white"
              :style="{
                background: `linear-gradient(to right, ${property.btnBuy.bgBeginColor}, ${property.btnBuy.bgEndColor}`
              }"
            >
              {{ property.btnBuy.text }}
            </span>
            <el-image
              v-else
              class="h-28px w-28px rounded-full"
              fit="cover"
              :src="property.btnBuy.imgUrl"
            />
          </div>
        </div>
      </div>
    </div>
    <div v-if="!spuList.length" class="py-24px text-center text-12px text-gray-400">
      自动获取商品中...
    </div>
  </div>
</template>

<script setup lang="ts">
import { ProductWaterfallProperty } from './config'
import * as ProductSpuApi from '@/api/product/spu'
import { fenToYuan } from '@/utils'

defineOptions({ name: 'ProductWaterfall' })

const props = defineProps<{ property: ProductWaterfallProperty }>()
const containerRef = ref()
const spuList = ref<ProductSpuApi.Spu[]>([])
const leftList = ref<ProductSpuApi.Spu[]>([])
const rightList = ref<ProductSpuApi.Spu[]>([])

const splitWaterfall = (list: ProductSpuApi.Spu[]) => {
  leftList.value = list.filter((_, index) => index % 2 === 0)
  rightList.value = list.filter((_, index) => index % 2 === 1)
}

const loadSpuList = async () => {
  const rule = props.property.rule || {}
  const { list } = await ProductSpuApi.getSpuPage({
    pageNo: 1,
    pageSize: props.property.pageSize || 10,
    tabType: 0,
    categorySales: rule.categorySales || undefined,
    sortField: rule.sortField || undefined,
    sortAsc: rule.sortAsc,
    name: rule.keyword || undefined
  })
  spuList.value = list || []
  splitWaterfall(spuList.value)
}

watch(() => props.property, loadSpuList, { immediate: true, deep: true })
</script>

<style scoped lang="scss">
.goods-md-wrap {
  width: 100%;
  box-sizing: border-box;
}

.goods-list-box {
  box-sizing: border-box;
}
</style>
