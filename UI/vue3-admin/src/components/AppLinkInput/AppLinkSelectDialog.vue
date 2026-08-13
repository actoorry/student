<template>
  <Dialog v-model="dialogVisible" title="选择链接" width="65%">
    <div class="h-500px flex gap-8px">
      <!-- 左侧分组列表 -->
      <el-scrollbar wrap-class="h-full" ref="groupScrollbar" view-class="flex flex-col">
        <el-button
          v-for="(group, groupIndex) in appLinkGroups"
          :key="groupIndex"
          :class="[
            'm-r-16px m-l-0px! justify-start! w-90px',
            { active: activeGroup === group.name }
          ]"
          ref="groupBtnRefs"
          :text="activeGroup !== group.name"
          :type="activeGroup === group.name ? 'primary' : 'default'"
          @click="handleGroupSelected(group.name)"
        >
          {{ group.name }}
        </el-button>
      </el-scrollbar>
      <!-- 右侧链接列表 -->
      <el-scrollbar class="h-full flex-1" @scroll="handleScroll" ref="linkScrollbar">
        <div v-for="(group, groupIndex) in appLinkGroups" :key="groupIndex">
          <!-- 分组标题 -->
          <div class="font-bold" ref="groupTitleRefs">{{ group.name }}</div>
          <div
            v-if="group.name === DIY_PAGE_GROUP_NAME && diyPageLoading"
            class="mb-12px flex items-center gap-8px text-14px text-gray-500"
          >
            <Icon icon="ep:loading" class="is-loading" />
            正在加载自定义页面...
          </div>
          <div
            v-else-if="group.name === DIY_PAGE_GROUP_NAME && diyPageLoadFailed"
            class="mb-12px"
          >
            <el-alert title="自定义页面加载失败" type="error" :closable="false" show-icon>
              <template #default>
                <el-button link type="primary" @click="retryLoadDiyPages">重新加载</el-button>
              </template>
            </el-alert>
          </div>
          <el-empty
            v-else-if="group.name === DIY_PAGE_GROUP_NAME && group.links.length === 0"
            description="暂无自定义页面，请先到装修页面新增"
            :image-size="72"
          />
          <!-- 链接列表 -->
          <el-tooltip
            v-for="(appLink, appLinkIndex) in group.links"
            :key="appLinkIndex"
            :content="appLink.path"
            placement="bottom"
            :show-after="300"
          >
            <el-button
              class="m-b-8px m-r-8px m-l-0px!"
              :type="isSameLink(appLink.path, activeAppLink.path, appLink.exactQuery) ? 'primary' : 'default'"
              @click="handleAppLinkSelected(appLink)"
            >
              {{ appLink.name }}
            </el-button>
          </el-tooltip>
        </div>
      </el-scrollbar>
    </div>
    <!-- 底部对话框操作按钮 -->
    <template #footer>
      <el-button type="primary" :disabled="!activeAppLink.path" @click="handleSubmit">
        确 定
      </el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
  <Dialog v-model="detailSelectDialog.visible" title="" width="50%">
    <el-form class="min-h-200px">
      <el-form-item
        label="选择分类"
        v-if="detailSelectDialog.type === APP_LINK_TYPE_ENUM.PRODUCT_CATEGORY_LIST"
      >
        <ProductCategorySelect
          v-model="detailSelectDialog.id"
          :parent-id="1"
          :check-strictly="true"
          :expand-on-click-node="false"
          @update:model-value="handleProductCategorySelected"
        />
      </el-form-item>
      <el-form-item
        label="选择会员"
        v-if="detailSelectDialog.type === APP_LINK_TYPE_ENUM.MARRIAGE_MEMBER_DETAIL"
      >
        <PartnerSelect
          v-model="detailSelectDialog.id"
          :clearable="false"
          placeholder="请选择会员"
          @update:model-value="handleMarriageMemberSelected"
        />
      </el-form-item>
    </el-form>
  </Dialog>
</template>
<script lang="ts" setup>
import { APP_LINK_GROUP_LIST, APP_LINK_TYPE_ENUM, AppLink, AppLinkGroup } from './data'
import {
  DIY_PAGE_GROUP_NAME,
  buildStandaloneDiyPageLinks,
  loadAllStandaloneDiyPages
} from './diyPageLinks'
import { ButtonInstance, ScrollbarInstance } from 'element-plus'
import { split } from 'lodash-es'
import ProductCategorySelect from '@/views/product/category/components/ProductCategorySelect.vue'
import PartnerSelect from '@/views/partner/partner/components/PartnerSelect.vue'
import { getUrlNumberValue } from '@/utils'
import * as DiyPageApi from '@/api/sales/promotion/diy/page'

// APP 链接选择弹框
defineOptions({ name: 'AppLinkSelectDialog' })
// 选中的分组，默认选中第一个
const activeGroup = ref(APP_LINK_GROUP_LIST[0].name)
// 选中的 APP 链接
const activeAppLink = ref({} as AppLink)
const diyPageLinks = ref<AppLink[]>([])
const diyPageLoading = ref(false)
const diyPageLoadFailed = ref(false)
const openedLink = ref('')
let diyPageLoadGeneration = 0
const appLinkGroups = computed<AppLinkGroup[]>(() => [
  ...APP_LINK_GROUP_LIST,
  {
    name: DIY_PAGE_GROUP_NAME,
    links: diyPageLinks.value
  }
])

const restoreActiveLink = (link: string) => {
  const group = appLinkGroups.value.find((item) =>
    item.links.some((linkItem) => {
      const sameLink = isSameLink(linkItem.path, link, linkItem.exactQuery)
      if (sameLink) {
        activeAppLink.value = { ...linkItem, path: link }
      }
      return sameLink
    })
  )
  if (group) {
    nextTick(() => handleGroupSelected(group.name))
  }
  return Boolean(group)
}

const loadDiyPages = async () => {
  const generation = ++diyPageLoadGeneration
  diyPageLoading.value = true
  diyPageLoadFailed.value = false
  diyPageLinks.value = []
  try {
    const pages = await loadAllStandaloneDiyPages((params) =>
      DiyPageApi.getDiyPagePage(params)
    )
    if (generation !== diyPageLoadGeneration) return
    diyPageLinks.value = buildStandaloneDiyPageLinks(pages)
  } catch {
    if (generation !== diyPageLoadGeneration) return
    diyPageLoadFailed.value = true
  } finally {
    if (generation === diyPageLoadGeneration) {
      diyPageLoading.value = false
    }
  }
}

/** 打开弹窗 */
const dialogVisible = ref(false)
const open = async (link: string) => {
  // 进入页面时先重置 activeAppLink
  activeAppLink.value = { name: '', path: '' }
  openedLink.value = link
  dialogVisible.value = true

  // 静态链接无需等待接口即可返显；动态页面在加载完成后再精确返显。
  const restored = restoreActiveLink(link)
  await loadDiyPages()
  if (dialogVisible.value && !restored && !activeAppLink.value.path) {
    restoreActiveLink(link)
  }
}
defineExpose({ open })

const retryLoadDiyPages = async () => {
  await loadDiyPages()
  if (!activeAppLink.value.path) {
    restoreActiveLink(openedLink.value)
  }
}

// 处理 APP 链接选中
const handleAppLinkSelected = (appLink: AppLink) => {
  if (appLink.type === APP_LINK_TYPE_ENUM.MARRIAGE_MEMBER_DETAIL) {
    detailSelectDialog.value.visible = true
    detailSelectDialog.value.type = appLink.type
    detailSelectDialog.value.id = undefined
    return
  }
  // 只有不同链接时才更新（避免重复触发）
  if (!isSameLink(appLink.path, activeAppLink.value.path, appLink.exactQuery)) {
    // 如果新链接的 path 为空，则沿用当前 activeAppLink 的 path
    const path = appLink.path || activeAppLink.value.path
    activeAppLink.value = { ...appLink, path: path }
  }
  switch (appLink.type) {
    case APP_LINK_TYPE_ENUM.PRODUCT_CATEGORY_LIST:
      detailSelectDialog.value.visible = true
      detailSelectDialog.value.type = appLink.type
      // 返显
      detailSelectDialog.value.id =
        getUrlNumberValue('id', 'http://127.0.0.1' + activeAppLink.value.path) || undefined
      break
    default:
      break
  }
}

// 处理绑定值更新
const emit = defineEmits<{
  change: [link: string]
  appLinkChange: [appLink: AppLink]
}>()
const handleSubmit = () => {
  if (!activeAppLink.value.path) return
  dialogVisible.value = false
  emit('change', activeAppLink.value.path)
  emit('appLinkChange', activeAppLink.value)
}

// 分组标题引用列表
const groupTitleRefs = ref<HTMLInputElement[]>([])
/**
 * 处理右侧链接列表滚动
 * @param scrollTop 滚动条的位置
 */
const handleScroll = ({ scrollTop }: { scrollTop: number }) => {
  const titleEl = groupTitleRefs.value.find((titleEl: HTMLInputElement) => {
    // 获取标题的位置信息
    const { offsetHeight, offsetTop } = titleEl
    // 判断标题是否在可视范围内
    return scrollTop >= offsetTop && scrollTop < offsetTop + offsetHeight
  })
  // 只需处理一次
  if (titleEl && activeGroup.value !== titleEl.textContent) {
    activeGroup.value = titleEl.textContent || ''
    // 同步左侧的滚动条位置
    scrollToGroupBtn(activeGroup.value)
  }
}

// 右侧滚动条
const linkScrollbar = ref<ScrollbarInstance>()
// 处理分组选中
const handleGroupSelected = (group: string) => {
  activeGroup.value = group
  const titleRef = groupTitleRefs.value.find((item: HTMLInputElement) => item.textContent === group)
  if (titleRef) {
    // 滚动分组标题
    linkScrollbar.value?.setScrollTop(titleRef.offsetTop)
  }
}

// 分组滚动条
const groupScrollbar = ref<ScrollbarInstance>()
// 分组引用列表
const groupBtnRefs = ref<ButtonInstance[]>([])
// 自动滚动分组按钮，确保分组按钮保持在可视区域内
const scrollToGroupBtn = (group: string) => {
  const groupBtn = groupBtnRefs.value
    .map((btn: ButtonInstance) => btn['ref'])
    .find((ref: HTMLButtonElement) => ref.textContent === group)
  if (groupBtn) {
    groupScrollbar.value?.setScrollTop(groupBtn.offsetTop)
  }
}

// 固定变体要求精确比较参数，历史/动态链接仍保持原有的路径级匹配语义
const isSameLink = (link1: string, link2: string, exactQuery = false) => {
  return exactQuery ? link1 === link2 : split(link1, '?', 1)[0] === split(link2, '?', 1)[0]
}

// 详情选择对话框
const detailSelectDialog = ref<{
  visible: boolean
  id?: number
  type?: APP_LINK_TYPE_ENUM
}>({
  visible: false,
  id: undefined,
  type: undefined
})
// 处理详情选择
const handleProductCategorySelected = (id: number) => {
  const url = new URL(activeAppLink.value.path, 'http://127.0.0.1')
  // 修改 id 参数
  url.searchParams.set('id', `${id}`)
  // 排除域名
  activeAppLink.value.path = `${url.pathname}${url.search}`
  // 关闭对话框
  detailSelectDialog.value.visible = false
  // 重置 id
  detailSelectDialog.value.id = undefined
}

// 资料详情页必须包含已选择的正整数会员 ID；查询失败或取消选择时不触碰当前链接。
const handleMarriageMemberSelected = (id: number | undefined) => {
  if (!id || id <= 0) {
    return
  }
  activeAppLink.value = {
    name: '资料详情页',
    path: `/pages/member-detail/index?id=${id}`,
    type: APP_LINK_TYPE_ENUM.MARRIAGE_MEMBER_DETAIL
  }
  detailSelectDialog.value.visible = false
  detailSelectDialog.value.id = undefined
}
</script>
<style lang="scss" scoped></style>
