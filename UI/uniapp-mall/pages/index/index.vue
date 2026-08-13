<!-- 首页，支持店铺装修与拼多多式分类交互 -->
<template>
  <view v-if="template">
    <s-layout
      title="首页"
      navbar="custom"
      tabbar="/pages/index/index"
      :bgStyle="template.page"
      :navbarStyle="template.navigationBar"
      onShareAppMessage
    >
      <!-- 普通首页：装修组件各渲染一次；拼多多态下仅保留吸顶头部 + 分类面板 -->
      <template>
        <template v-for="(item, index) in template.components" :key="index">
          <template v-if="item && item.id">
            <!--
              吸顶搜索导航头部：两种模式下都保留渲染。
              - intercept-menu-tap：首页接管菜单点击分发（拼多多式交互入口）
              - active-menu-index：装修态高亮“推荐”，拼多多态高亮当前一级分类
            -->
            <template v-if="item.id === 'StickySearchMenuHeader' && item.property?.sticky !== false">
              <su-sticky
                :offsetTop="0"
                :customNavHeight="0"
                bgColor="transparent"
              >
                <s-block :styles="item.property?.style || {}">
                  <s-sticky-search-menu-header
                    :data="item.property || {}"
                    :intercept-menu-tap="true"
                    :active-menu-index="activeMenuIndex"
                    @menuClickItem="onHeaderMenuTap"
                  />
                </s-block>
              </su-sticky>
              <!-- 头部是最后一个有效组件时，循环内没有后续位置，兜底展示商品流 -->
              <view v-if="shouldRenderPanelAfter()" class="pdd-category-section">
                <s-goods-waterfall
                  v-if="pddCategoryId"
                  :data="waterfallDataDefault"
                  :category-id="pddCategoryId"
                />
                <!-- 兜底分支空态：未选中一级分类时提示，与完整面板空态保持一致 -->
                <s-empty v-else icon="/static/soldout-empty.png" text="暂无商品" />
              </view>
            </template>
            <!-- 头部之后第一个有效组件的位置被完整分类面板替换 -->
            <view v-else-if="pddMode && index === firstComponentAfterHeaderIndex" class="pdd-category-section">
              <view v-if="getPrimaryChildren(pddCategoryId).length" class="pdd-secondary-grid">
                <view
                  v-for="secondaryItem in getPrimaryChildren(pddCategoryId)"
                  :key="secondaryItem.id"
                  class="pdd-secondary-item"
                  @tap="onSecondaryCategoryTap(secondaryItem)"
                >
                  <image
                    v-if="secondaryItem.picUrl"
                    class="pdd-secondary-image"
                    :src="sheep.$url.cdn(secondaryItem.picUrl)"
                    mode="aspectFill"
                  />
                  <view v-else class="pdd-secondary-image-placeholder">
                    {{ secondaryItem.name }}
                  </view>
                  <text class="pdd-secondary-name">{{ secondaryItem.name }}</text>
                </view>
              </view>
              <s-goods-waterfall
                v-if="pddCategoryId"
                :data="waterfallDataDefault"
                :category-id="pddCategoryId"
              />
              <s-empty v-else icon="/static/soldout-empty.png" text="暂无商品" />
            </view>
            <!-- 拼多多态隐藏其余装修组件，退出后自动恢复（v-if="!pddMode"） -->
            <s-block
              v-else-if="!pddMode"
              :styles="item.property?.style || {}"
            >
              <s-block-item
                :type="item.id"
                :data="item.property || {}"
                :styles="item.property?.style || {}"
                :source-context="{ source: 'template', id: templateId, slot: 'home', index }"
              />
            </s-block>
          </template>
        </template>
      </template>

    </s-layout>
  </view>
</template>

<script setup>
  import { computed, ref } from 'vue';
  import { onLoad, onShow, onPageScroll, onPullDownRefresh } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import $share from '@/sheep/platform/share';
  import CategoryApi from '@/sheep/api/product/category';
  import { handleTree } from '@/sheep/helper/utils';

  // 隐藏原生tabBar
  uni.hideTabBar({
    fail: () => {},
  });

  const template = computed(() => sheep.$store('app').template?.home);
  const templateId = computed(() => sheep.$store('app').template?.templateId);

  // =====================================================================
  // 拼多多式分类交互（方案 A：首页内联实现）
  //
  // 交互模型：
  //   头部菜单 [推荐][生鲜水果][百货]... 吸顶，选中项高亮
  //   - 点击一级分类（URL 指向 category 页）→ 首页原地切为拼多多态：
  //     隐藏装修组件，显示「二级分类五列图 + 一级分类商品瀑布流」
  //   - 点击"推荐"（URL 指向首页自身）→ 退出拼多多态，恢复装修组件
  //   - 点击二级分类图 → navigateTo category 页（新页面实例）
  //   - 从二级页返回 → 首页实例未销毁，拼多多态与选中项天然保留
  //
  // 状态全部存放在页面实例局部（不写全局 Store），退出首页自动释放。
  // =====================================================================

  /** 是否处于拼多多态：false=装修态（展示完整装修组件），true=分类面板态 */
  const pddMode = ref(false);
  /** 当前选中的一级分类 ID（来自菜单项 URL 的 id 参数），0 表示未选中/解析失败 */
  const pddCategoryId = ref(0);
  /** 当前选中的头部菜单项索引（对应 menu.list 下标），-1 表示不高亮 */
  const pddMenuIndex = ref(-1);
  /** 分类树缓存（首页实例级），首次进入拼多多态时加载一次 */
  const categoryTree = ref([]);
  /** 头部组件在 components 中的位置，面板锚定在其正下方。 */
  const headerComponentIndex = computed(() =>
    (template.value?.components || []).findIndex((c) => c?.id === 'StickySearchMenuHeader'),
  );

  /** 头部之后第一个有效装修组件的位置，作为完整面板的渲染锚点。 */
  const firstComponentAfterHeaderIndex = computed(() => {
    const headerIdx = headerComponentIndex.value;
    if (headerIdx < 0) return -1;
    const components = template.value?.components || [];
    return components.findIndex((component, index) => index > headerIdx && component?.id);
  });

  /** 头部之后是否还有其他有效装修组件。 */
  const hasComponentAfterHeader = computed(() => firstComponentAfterHeaderIndex.value >= 0);

  /** 拼多多态且头部为最后一个有效组件时，循环内兜底渲染商品流。 */
  function shouldRenderPanelAfter() {
    return pddMode.value && !hasComponentAfterHeader.value;
  }

  /** 头部菜单数据源（装修 JSON 的 menu.list），供高亮索引计算使用 */
  const headerMenuList = computed(() => {
    const header = (template.value?.components || []).find(
      (c) => c?.id === 'StickySearchMenuHeader',
    );
    return header?.property?.menu?.list || [];
  });

  /** 拼多多态高亮选中的一级分类，装修态高亮指向首页的推荐项 */
  const activeMenuIndex = computed(() => {
    if (pddMode.value) return pddMenuIndex.value;
    return headerMenuList.value.findIndex(
      (item) => item?.url && item.url.includes('pages/index/index'),
    );
  });

  /** 头部菜单点击统一分发（interceptMenuTap=true，本页接管所有菜单项点击） */
  async function onHeaderMenuTap(payload) {
    const { item, index } = payload || {};
    const url = item?.url || '';

    if (url.includes('pages/index/category')) {
      pddMode.value = true;
      // 进入拼多多态时页面高度塌缩，先把滚动复位到顶部，避免视口落在面板中下部
      uni.pageScrollTo({ scrollTop: 0, duration: 0 });
      pddMenuIndex.value = index;
      const match = url.match(/[?&]id=(\d+)/);
      pddCategoryId.value = match ? Number(match[1]) : 0;
      await loadCategoryTree();
      return;
    }

    if (url.includes('pages/index/index')) {
      if (pddMode.value) exitPddMode();
      return;
    }

    if (url) sheep.$router.go(url);
  }

  /** 懒加载并缓存分类树 */
  async function loadCategoryTree() {
    if (categoryTree.value.length) return;
    try {
      const { code, data } = await CategoryApi.getCategoryList();
      if (code === 0) categoryTree.value = handleTree(data);
    } catch (error) {
      // 失败不阻塞：面板保持可用，再次点击一级分类可重试
      categoryTree.value = [];
    }
  }

  /** 取一级分类下的二级分类列表 */
  function getPrimaryChildren(primaryId) {
    return categoryTree.value.find((item) => item.id === primaryId)?.children || [];
  }

  /** 退出拼多多态，恢复完整装修首页 */
  function exitPddMode() {
    pddMode.value = false;
    pddCategoryId.value = 0;
    pddMenuIndex.value = -1;
    // 退出拼多多态时页面重新展开，先把滚动复位到顶部，避免视口停留在原深度
    uni.pageScrollTo({ scrollTop: 0, duration: 0 });
  }

  /** 二级分类图点击：navigateTo 创建 category 页新实例 */
  function onSecondaryCategoryTap(secondaryItem) {
    sheep.$router.go('/pages/index/category', { id: secondaryItem.id });
  }

  /** 商品瀑布流展示配置，与 category.vue 保持一致 */
  const waterfallDataDefault = {
    fields: {
      name: { show: true, color: '#000' },
      introduction: { show: true, color: '#999' },
      price: { show: true, color: '#ff3000' },
      marketPrice: { show: true, color: '#c4c4c4' },
      salesCount: { show: true, color: '#c4c4c4' },
      stock: { show: false, color: '#c4c4c4' },
    },
    badge: { show: false, imgUrl: '' },
    btnBuy: {
      type: 'text',
      text: '立即购买',
      bgBeginColor: '#FF6000',
      bgEndColor: '#FE832A',
      imgUrl: '',
    },
    borderRadiusTop: 6,
    borderRadiusBottom: 6,
    space: 8,
    pageSize: 10,
    rule: {
      sortField: '',
      sortAsc: false,
      keyword: '',
    },
  };

  onLoad((options) => {
    // #ifdef MP
    // 小程序识别二维码
    if (options.scene) {
      const sceneParams = decodeURIComponent(options.scene).split('=');
      console.log('sceneParams=>', sceneParams);
      options[sceneParams[0]] = sceneParams[1];
    }
    // #endif

    // 二维码分销邀请场景：bindUserId 与 SPM 推广人共用统一待绑定上下文
    if (options.bindUserId) {
      $share.captureBindUserId(options.bindUserId);
    }

    // 预览模板
    if (options.templateId) {
      sheep.$store('app').init(options.templateId);
    }

    // 解析分享信息
    if (options.spm) {
      $share.decryptSpm(options.spm);
    }

    // 进入指定页面(完整页面路径)
    if (options.page) {
      sheep.$router.go(decodeURIComponent(options.page));
    }
  });

  onShow(async () => {
    // #ifdef APP-PLUS
    // ios首次授权网络，需要重新加载一次应用初始化
    if (sheep.$platform.os === 'ios') {
      if (await sheep.$platform.checkNetwork()) {
        await sheep.$store('app').init();
      }
    }
    // #endif
  });

  // 下拉刷新
  onPullDownRefresh(() => {
    sheep.$store('app').init();
    setTimeout(function () {
      uni.stopPullDownRefresh();
    }, 800);
  });
  onPageScroll(() => {});
</script>

<style>
  /* ===== 拼多多模式样式 ===== */
  /* 分类面板容器：头部正下方，二级分类图 + 商品瀑布流 */
  .pdd-category-section {
    background: #f7f7f7;
  }

  .pdd-secondary-grid {
    display: flex;
    flex-wrap: wrap;
    padding: 20rpx;
    background: #fff;
    border-bottom: 1rpx solid #f0f0f0;
  }

  .pdd-secondary-item {
    width: calc((100% - 80rpx) / 5);
    margin: 0 20rpx 20rpx 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .pdd-secondary-item:nth-child(5n) {
    margin-right: 0;
  }

  .pdd-secondary-image {
    width: 100%;
    height: calc((100vw - 120rpx) / 5);
    border-radius: 8rpx;
    background: #f6f6f6;
  }

  .pdd-secondary-image-placeholder {
    width: 100%;
    height: calc((100vw - 120rpx) / 5);
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8rpx;
    background: #f6f6f6;
    color: #999;
    font-size: 24rpx;
  }

  .pdd-secondary-name {
    margin-top: 8rpx;
    font-size: 24rpx;
    color: #333;
    line-height: 32rpx;
  }
</style>
