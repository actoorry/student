<template>
  <div class="product-row">
    <!-- 标题栏 -->
    <div class="header">
      <div class="header-left">
        <div
          v-if="property.title"
          class="title"
          :style="{ fontSize: `${property.titleSize}px`, color: property.titleColor }"
        >
          {{ property.title }}
        </div>
        <div
          v-if="property.description"
          class="description"
          :style="{ fontSize: `${property.descriptionSize}px`, color: property.descriptionColor }"
        >
          {{ property.description }}
        </div>
      </div>
      <div v-show="property.more.show" class="more" :style="{ color: property.descriptionColor }">
        <span v-if="property.more.type !== 'icon'">{{ property.more.text }}</span>
        <Icon icon="ep:arrow-right" v-if="property.more.type !== 'text'" />
      </div>
    </div>
    <!-- 双列商品（固定一行两个） -->
    <div class="grid" :style="{ gap: `${property.space}px` }">
      <div
        v-for="(spu, index) in spuList"
        :key="index"
        class="product-item"
        :style="{
          borderTopLeftRadius: `${property.borderRadiusTop}px`,
          borderTopRightRadius: `${property.borderRadiusTop}px`,
          borderBottomLeftRadius: `${property.borderRadiusBottom}px`,
          borderBottomRightRadius: `${property.borderRadiusBottom}px`
        }"
      >
        <div v-if="property.badge.show && property.badge.imgUrl" class="badge">
          <el-image fit="cover" :src="property.badge.imgUrl" class="h-26px w-38px" />
        </div>
        <el-image fit="cover" :src="spu.picUrl" class="product-img" />
        <div class="product-info">
          <div
            v-if="property.fields.name.show"
            class="product-name"
            :style="{ color: property.fields.name.color }"
          >
            {{ spu.name }}
          </div>
          <span
            v-if="property.fields.price.show"
            class="product-price"
            :style="{ color: property.fields.price.color }"
          >
            ￥{{ fenToYuan(spu.price as any) }}
          </span>
        </div>
      </div>
      <div v-if="spuList.length === 0" class="empty-tip">请配置商品数据</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { PRODUCT_ROW_DISPLAY_COUNT, ProductRowProperty } from './config'
import * as ProductSpuApi from '@/api/product/spu'
import { fenToYuan } from '@/utils'

defineOptions({ name: 'ProductRow' })

const props = defineProps<{ property: ProductRowProperty }>()
const spuList = ref<ProductSpuApi.Spu[]>([])

const loadSpuList = async () => {
  if (props.property.dataSource === 'manual') {
    const ids = props.property.spuIds?.slice(0, PRODUCT_ROW_DISPLAY_COUNT) || []
    if (ids.length === 0) {
      spuList.value = []
      return
    }
    spuList.value = (await ProductSpuApi.getSpuDetailList(ids)).slice(0, PRODUCT_ROW_DISPLAY_COUNT)
    return
  }
  const rule = props.property.rule || {}
  const { list } = await ProductSpuApi.getSpuPage({
    pageNo: 1,
    pageSize: PRODUCT_ROW_DISPLAY_COUNT,
    tabType: 0,
    categorySales: rule.categorySales || undefined,
    name: rule.keyword || undefined
  })
  spuList.value = list || []
}

watch(() => props.property, loadSpuList, { immediate: true, deep: true })
</script>

<style scoped lang="scss">
.product-row {
  width: 100%;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 8px 10px;
  box-sizing: border-box;

  .header-left {
    flex: 1;
    min-width: 0;
  }

  .title {
    font-weight: 600;
    line-height: 1.4;
  }

  .description {
    margin-top: 2px;
    line-height: 1.4;
  }

  .more {
    display: flex;
    flex-shrink: 0;
    align-items: center;
    gap: 2px;
    font-size: 12px;
    white-space: nowrap;
  }
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  padding: 0 8px 8px;
  box-sizing: border-box;
}

.product-item {
  position: relative;
  overflow: hidden;
  background: #fff;

  .badge {
    position: absolute;
    top: 0;
    left: 0;
    z-index: 1;
  }

  .product-img {
    display: block;
    width: 100%;
    height: 140px;
  }

  .product-info {
    padding: 8px;
    box-sizing: border-box;
  }

  .product-name {
    overflow: hidden;
    font-size: 12px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .product-price {
    display: block;
    margin-top: 4px;
    font-size: 14px;
    font-weight: 600;
  }
}

.empty-tip {
  grid-column: 1 / -1;
  padding: 24px 0;
  color: #969799;
  font-size: 12px;
  text-align: center;
}
</style>
