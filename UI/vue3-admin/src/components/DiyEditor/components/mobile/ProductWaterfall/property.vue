<template>
  <ComponentContainerProperty v-model="formData.style">
    <el-form label-width="85px" :model="formData">
      <el-card header="商品数据" class="property-group" shadow="never">
        <el-alert
          title="自动获取商品，无需手工选品，小程序端滚动到底部自动加载更多"
          type="info"
          :closable="false"
          class="mb-12px"
        />
        <el-form-item label="每批数量" prop="pageSize">
          <el-slider
            v-model="formData.pageSize"
            :min="4"
            :max="30"
            show-input
            input-size="small"
            :show-input-controls="false"
          />
        </el-form-item>
        <el-form-item label="销售分类" prop="rule.categorySales">
          <ProductCategorySelect v-model="formData.rule.categorySales" :parent-id="1" />
        </el-form-item>
        <el-form-item label="关键词" prop="rule.keyword">
          <el-input v-model="formData.rule.keyword" placeholder="可选，留空获取全部" clearable />
        </el-form-item>
        <el-form-item label="排序字段" prop="rule.sortField">
          <el-select v-model="formData.rule.sortField" clearable placeholder="默认排序">
            <el-option label="默认排序" value="" />
            <el-option label="销量" value="salesCount" />
            <el-option label="价格" value="price" />
            <el-option label="上架时间" value="createTime" />
          </el-select>
        </el-form-item>
        <el-form-item label="升序排列" prop="rule.sortAsc" v-if="formData.rule.sortField">
          <el-switch v-model="formData.rule.sortAsc" />
        </el-form-item>
      </el-card>

      <el-card header="商品样式" class="property-group" shadow="never">
        <el-form-item label="商品名称" prop="fields.name.show">
          <div class="flex gap-8px">
            <ColorInput v-model="formData.fields.name.color" />
            <el-checkbox v-model="formData.fields.name.show" />
          </div>
        </el-form-item>
        <el-form-item label="商品简介" prop="fields.introduction.show">
          <div class="flex gap-8px">
            <ColorInput v-model="formData.fields.introduction.color" />
            <el-checkbox v-model="formData.fields.introduction.show" />
          </div>
        </el-form-item>
        <el-form-item label="商品价格" prop="fields.price.show">
          <div class="flex gap-8px">
            <ColorInput v-model="formData.fields.price.color" />
            <el-checkbox v-model="formData.fields.price.show" />
          </div>
        </el-form-item>
        <el-form-item label="市场价" prop="fields.marketPrice.show">
          <div class="flex gap-8px">
            <ColorInput v-model="formData.fields.marketPrice.color" />
            <el-checkbox v-model="formData.fields.marketPrice.show" />
          </div>
        </el-form-item>
        <el-form-item label="商品销量" prop="fields.salesCount.show">
          <div class="flex gap-8px">
            <ColorInput v-model="formData.fields.salesCount.color" />
            <el-checkbox v-model="formData.fields.salesCount.show" />
          </div>
        </el-form-item>
        <el-form-item label="商品库存" prop="fields.stock.show">
          <div class="flex gap-8px">
            <ColorInput v-model="formData.fields.stock.color" />
            <el-checkbox v-model="formData.fields.stock.show" />
          </div>
        </el-form-item>
      </el-card>

      <el-card header="角标" class="property-group" shadow="never">
        <el-form-item label="角标" prop="badge.show">
          <el-switch v-model="formData.badge.show" />
        </el-form-item>
        <el-form-item label="角标图" prop="badge.imgUrl" v-if="formData.badge.show">
          <UploadImg v-model="formData.badge.imgUrl" height="44px" width="72px">
            <template #tip>建议尺寸：36 * 22</template>
          </UploadImg>
        </el-form-item>
      </el-card>

      <el-card header="按钮" class="property-group" shadow="never">
        <el-form-item label="按钮类型" prop="btnBuy.type">
          <el-radio-group v-model="formData.btnBuy.type">
            <el-radio-button value="text">文字</el-radio-button>
            <el-radio-button value="img">图片</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <template v-if="formData.btnBuy.type === 'text'">
          <el-form-item label="按钮文字" prop="btnBuy.text">
            <el-input v-model="formData.btnBuy.text" />
          </el-form-item>
          <el-form-item label="左侧背景" prop="btnBuy.bgBeginColor">
            <ColorInput v-model="formData.btnBuy.bgBeginColor" />
          </el-form-item>
          <el-form-item label="右侧背景" prop="btnBuy.bgEndColor">
            <ColorInput v-model="formData.btnBuy.bgEndColor" />
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item label="图片" prop="btnBuy.imgUrl">
            <UploadImg v-model="formData.btnBuy.imgUrl" height="56px" width="56px">
              <template #tip>建议尺寸：56 * 56</template>
            </UploadImg>
          </el-form-item>
        </template>
      </el-card>

      <el-card header="布局" class="property-group" shadow="never">
        <el-form-item label="上圆角" prop="borderRadiusTop">
          <el-slider
            v-model="formData.borderRadiusTop"
            :max="100"
            :min="0"
            show-input
            input-size="small"
            :show-input-controls="false"
          />
        </el-form-item>
        <el-form-item label="下圆角" prop="borderRadiusBottom">
          <el-slider
            v-model="formData.borderRadiusBottom"
            :max="100"
            :min="0"
            show-input
            input-size="small"
            :show-input-controls="false"
          />
        </el-form-item>
        <el-form-item label="列间距" prop="space">
          <el-slider
            v-model="formData.space"
            :max="32"
            :min="0"
            show-input
            input-size="small"
            :show-input-controls="false"
          />
        </el-form-item>
      </el-card>
    </el-form>
  </ComponentContainerProperty>
</template>

<script setup lang="ts">
import { ProductWaterfallProperty } from './config'
import { useVModel } from '@vueuse/core'
import ProductCategorySelect from '@/views/product/category/components/ProductCategorySelect.vue'

defineOptions({ name: 'ProductWaterfallProperty' })

const props = defineProps<{ modelValue: ProductWaterfallProperty }>()
const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)
</script>

<style scoped lang="scss"></style>
