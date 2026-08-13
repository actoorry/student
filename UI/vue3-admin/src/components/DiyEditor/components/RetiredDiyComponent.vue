<template>
  <div class="retired-diy-component">
    <div class="retired-diy-component__title">已下线组件（保留数据）</div>
    <div class="retired-diy-component__description">
      此组件已从组件库下线，当前页面的历史配置会保留且不会在商城端展示。若确认不再需要，可使用组件工具栏删除。
    </div>
  </div>
</template>

<script lang="ts" setup>
defineProps<{
  componentId?: string
  property?: unknown
  modelValue?: unknown
}>()
</script>

<style lang="scss" scoped>
.retired-diy-component {
  padding: 16px;
  color: var(--el-text-color-regular);
  background: var(--el-fill-color-light);
  border: 1px dashed var(--el-border-color);
  border-radius: 4px;

  &__title {
    margin-bottom: 8px;
    color: var(--el-color-warning);
    font-weight: 600;
  }

  &__description {
    font-size: 13px;
    line-height: 1.6;
  }
}
</style>
