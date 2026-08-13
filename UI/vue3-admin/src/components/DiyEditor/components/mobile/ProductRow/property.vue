<template>
  <ComponentContainerProperty v-model="formData.style">
    <el-form label-width="85px" :model="formData">
      <el-card header="标题" class="property-group" shadow="never">
        <el-form-item label="主标题" prop="title">
          <InputWithColor v-model="formData.title" v-model:color="formData.titleColor" maxlength="20" />
        </el-form-item>
        <el-form-item label="标题大小" prop="titleSize">
          <el-slider v-model="formData.titleSize" :min="12" :max="24" show-input input-size="small" />
        </el-form-item>
        <el-form-item label="副标题" prop="description">
          <InputWithColor
            v-model="formData.description"
            v-model:color="formData.descriptionColor"
            maxlength="30"
          />
        </el-form-item>
        <el-form-item label="副标大小" prop="descriptionSize">
          <el-slider
            v-model="formData.descriptionSize"
            :min="10"
            :max="18"
            show-input
            input-size="small"
          />
        </el-form-item>
      </el-card>

      <el-card header="展开更多" class="property-group" shadow="never">
        <el-form-item label="是否显示" prop="more.show">
          <el-checkbox v-model="formData.more.show" />
        </el-form-item>
        <template v-if="formData.more.show">
          <el-form-item label="样式" prop="more.type">
            <el-radio-group v-model="formData.more.type">
              <el-radio value="text">文字</el-radio>
              <el-radio value="icon">图标</el-radio>
              <el-radio value="all">文字+图标</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="按钮文字" prop="more.text" v-show="formData.more.type !== 'icon'">
            <el-input v-model="formData.more.text" />
          </el-form-item>
          <el-form-item label="跳转链接" prop="more.url">
            <AppLinkInput v-model="formData.more.url" />
          </el-form-item>
        </template>
      </el-card>

      <el-card header="商品数据" class="property-group" shadow="never">
        <el-form-item label="数据来源" prop="dataSource">
          <el-radio-group v-model="formData.dataSource">
            <el-radio value="manual">手工选品</el-radio>
            <el-radio value="rule">规则获取</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="formData.dataSource === 'manual'">
          <el-alert
            title="双列单行固定展示 2 个商品，请至少选择 2 个商品"
            type="info"
            :closable="false"
            class="mb-12px"
          />
          <SpuShowcase v-model="formData.spuIds" />
        </template>
        <template v-else>
          <el-form-item label="销售分类" prop="rule.categorySales">
            <ProductCategorySelect v-model="formData.rule.categorySales" :parent-id="1" />
          </el-form-item>
          <el-form-item label="关键词" prop="rule.keyword">
            <el-input v-model="formData.rule.keyword" placeholder="可选" clearable />
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
        </template>
      </el-card>

      <el-card header="商品样式" class="property-group" shadow="never">
        <el-form-item label="商品名称" prop="fields.name.show">
          <div class="flex gap-8px">
            <ColorInput v-model="formData.fields.name.color" />
            <el-checkbox v-model="formData.fields.name.show" />
          </div>
        </el-form-item>
        <el-form-item label="商品价格" prop="fields.price.show">
          <div class="flex gap-8px">
            <ColorInput v-model="formData.fields.price.color" />
            <el-checkbox v-model="formData.fields.price.show" />
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
import { ProductRowProperty } from './config'
import { useVModel } from '@vueuse/core'
import SpuShowcase from '@/views/product/spu/components/SpuShowcase.vue'
import ProductCategorySelect from '@/views/product/category/components/ProductCategorySelect.vue'

defineOptions({ name: 'ProductRowProperty' })

const props = defineProps<{ modelValue: ProductRowProperty }>()
const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)
</script>

<style scoped lang="scss"></style>
