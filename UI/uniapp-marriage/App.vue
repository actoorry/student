<script setup>
  import { onLaunch, onShow } from '@dcloudio/uni-app';
  import sheep, { ShoproInit } from './sheep';

  onLaunch(async () => {
    // 隐藏原生导航栏 使用自定义底部导航
    uni.hideTabBar({
      fail: () => {},
    });

    // 加载Shopro底层依赖
    await ShoproInit();
    sheep.$store('social').startRealtime();
  });

  onShow(() => {
    sheep.$store('social').startRealtime();
    // #ifdef APP-PLUS
    // 获取urlSchemes参数
    const args = plus.runtime.arguments;
    if (args) {
    }

    // 获取剪贴板
    uni.getClipboardData({
      success: (res) => {},
    });
    // #endif
  });
</script>

<style lang="scss">
  @import '@/sheep/scss/index.scss';
</style>
