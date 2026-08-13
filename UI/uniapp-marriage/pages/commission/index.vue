<!-- 婚恋分销中心：只消费 sales 分销接口返回的运行时数据。 -->
<template>
  <s-layout
    navbar="inner"
    class="index-wrap"
    title="分销中心"
    :bgStyle="bgStyle"
    :onShareAppMessage="shareInfo"
  >
    <view v-if="state.loading" class="center-state">
      <text class="state-title">分销信息加载中</text>
      <text class="state-copy">正在读取你的账户状态，请稍候</text>
    </view>

    <view v-else-if="state.error && !state.hasData" class="center-state center-state--error">
      <text class="state-title">分销信息加载失败</text>
      <text class="state-copy">{{ state.error }}</text>
      <button class="state-button" :disabled="state.refreshing" @tap="refresh">
        {{ state.refreshing ? '重试中…' : '重新加载' }}
      </button>
    </view>

    <template v-else>
      <commission-auth
        :show="state.authVisible"
        :title="state.access.title"
        :message="state.access.message"
        @close="state.authVisible = false"
      />

      <view v-if="!state.access.available" class="center-state center-state--access">
        <text class="state-title">{{ state.access.title }}</text>
        <text class="state-copy">{{ state.access.message }}</text>
        <button
          v-if="state.access.canApply"
          class="state-button"
          :disabled="state.applying"
          @tap="applyBrokerage"
        >
          {{ state.applying ? '提交中…' : state.access.actionLabel }}
        </button>
        <button class="state-button state-button--plain" :disabled="state.refreshing" @tap="refresh">
          {{ state.refreshing ? '刷新中…' : '刷新状态' }}
        </button>
      </view>

      <template v-else>
        <commission-info :user-info="userInfo" />
        <account-info :summary="state.summary" :loading="state.sectionLoading.summary" />
        <commission-menu :disabled="state.refreshing" />
        <commission-log
          :records="state.records"
          :loading="state.sectionLoading.records"
          :error="state.sectionErrors.records"
          @retry="refreshSection('records')"
        />
        <view v-if="state.error" class="partial-error">
          <text>{{ state.error }}</text>
          <text class="partial-error__retry" @tap="refresh">重试</text>
        </view>
      </template>
    </template>
  </s-layout>
</template>

<script setup>
  import { computed, reactive, ref } from 'vue';
  import { onShow } from '@dcloudio/uni-app';
  import commissionInfo from './components/commission-info.vue';
  import accountInfo from './components/account-info.vue';
  import commissionLog from './components/commission-log.vue';
  import commissionMenu from './components/commission-menu.vue';
  import commissionAuth from './components/commission-auth.vue';
  import sheep from '@/sheep';
  import BrokerageApi from '@/sheep/api/sales/brokerage';
  import SalesConfigApi from '@/sheep/api/sales/config';
  import { SharePageEnum } from '@/sheep/helper/const';
  import { resolveBrokerageAccess } from '@/sheep/helper/brokerage-contract';

  const userStore = sheep.$store('user');
  const userInfo = computed(() => userStore.userInfo || {});
  const refreshGeneration = ref(0);
  const state = reactive({
    loading: true,
    refreshing: false,
    applying: false,
    error: '',
    hasData: false,
    authVisible: false,
    access: {
      available: false,
      canApply: false,
      title: '暂未开通分销',
      message: '当前账号暂时没有可用的分销资格',
      actionLabel: '',
    },
    brokerageUser: null,
    config: null,
    summary: {},
    records: [],
    sectionLoading: { summary: false, records: false },
    sectionErrors: { summary: '', records: '' },
  });

  const shareInfo = computed(() =>
    sheep.$platform.share.getShareInfo(
      { params: { page: SharePageEnum.HOME.value } },
      { type: 'user' },
    ),
  );

  const bgStyle = { color: 'var(--marriage-soft)' };

  function resultData(result) {
    return result && result.code === 0 ? result.data : null;
  }

  async function refresh() {
    const generation = ++refreshGeneration.value;
    state.refreshing = true;
    state.loading = !state.hasData;
    state.error = '';
    state.sectionErrors.summary = '';
    state.sectionErrors.records = '';

    let brokerageResult;
    let configResult;
    try {
      [brokerageResult, configResult] = await Promise.all([
        BrokerageApi.getBrokerageUser(),
        SalesConfigApi.getSalesConfig(),
      ]);
    } catch (error) {
      if (generation !== refreshGeneration.value) return;
      state.error = error?.message || '分销状态暂时无法读取';
      state.loading = false;
      state.refreshing = false;
      return;
    }
    if (generation !== refreshGeneration.value) return;

    const brokerageUser = resultData(brokerageResult);
    const config = resultData(configResult);
    state.brokerageUser = brokerageUser;
    state.config = config;
    state.hasData = Boolean(brokerageUser || config);
    state.access = resolveBrokerageAccess(brokerageUser, config);
    if (brokerageResult?.code !== 0 || configResult?.code !== 0) {
      state.error = '分销状态读取不完整，请重试';
    }

    if (!state.access.available) {
      state.loading = false;
      state.refreshing = false;
      return;
    }

    state.sectionLoading.summary = true;
    state.sectionLoading.records = true;
    const [summaryResult, recordResult] = await Promise.allSettled([
      BrokerageApi.getBrokerageUserSummary(),
      BrokerageApi.getBrokerageRecordPage({ pageNo: 1, pageSize: 8 }),
    ]);
    if (generation !== refreshGeneration.value) return;

    if (summaryResult.status === 'fulfilled' && summaryResult.value?.code === 0) {
      state.summary = summaryResult.value.data || {};
    } else {
      state.sectionErrors.summary = '账户摘要暂时无法读取';
      state.error = '部分分销信息读取失败';
    }
    if (recordResult.status === 'fulfilled' && recordResult.value?.code === 0) {
      state.records = recordResult.value.data?.list || [];
    } else {
      state.sectionErrors.records = '佣金动态暂时无法读取';
      state.error = '部分分销信息读取失败';
    }
    state.sectionLoading.summary = false;
    state.sectionLoading.records = false;
    state.loading = false;
    state.refreshing = false;
  }

  async function refreshSection(section) {
    const generation = ++refreshGeneration.value;
    if (section === 'records') state.sectionLoading.records = true;
    try {
      const result = await BrokerageApi.getBrokerageRecordPage({ pageNo: 1, pageSize: 8 });
      if (generation !== refreshGeneration.value) return;
      if (result?.code === 0) {
        state.records = result.data?.list || [];
        state.sectionErrors.records = '';
      } else {
        state.sectionErrors.records = '佣金动态暂时无法读取';
      }
    } catch (error) {
      if (generation === refreshGeneration.value) {
        state.sectionErrors.records = error?.message || '佣金动态暂时无法读取';
      }
    } finally {
      if (generation === refreshGeneration.value) state.sectionLoading.records = false;
    }
  }

  async function applyBrokerage() {
    if (state.applying || !state.access.canApply) return;
    state.applying = true;
    try {
      const result = await BrokerageApi.applyBrokerageUser();
      if (result?.code === 0) {
        sheep.$helper.toast('申请已提交，请等待审核');
        await refresh();
      } else {
        sheep.$helper.toast('申请提交失败，请稍后重试');
      }
    } catch (error) {
      sheep.$helper.toast(error?.message || '申请提交失败，请稍后重试');
    } finally {
      state.applying = false;
    }
  }

  onShow(refresh);
</script>

<style lang="scss" scoped>
  .index-wrap {
    color: var(--marriage-text);
  }

  .center-state {
    display: flex;
    min-height: 560rpx;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 18rpx;
    margin: 24rpx;
    padding: 60rpx 36rpx;
    background: var(--marriage-surface);
    border-radius: 24rpx;
    box-shadow: var(--marriage-shadow);
    text-align: center;
  }

  .state-title {
    color: var(--marriage-text);
    font-size: 32rpx;
    font-weight: 700;
  }

  .state-copy {
    color: var(--marriage-muted);
    font-size: 25rpx;
    line-height: 1.6;
  }

  .state-button {
    min-width: 300rpx;
    margin-top: 16rpx;
    padding: 0 36rpx;
    color: #fff;
    background: var(--marriage-primary);
    border-radius: 999rpx;
    font-size: 27rpx;
  }

  .state-button--plain {
    margin-top: 0;
    color: var(--marriage-primary);
    background: var(--marriage-primary-soft);
  }

  .partial-error {
    display: flex;
    justify-content: center;
    gap: 18rpx;
    margin: 0 30rpx 24rpx;
    padding: 18rpx 24rpx;
    color: var(--marriage-muted);
    background: var(--marriage-surface);
    border-radius: 16rpx;
    font-size: 23rpx;
  }

  .partial-error__retry {
    color: var(--marriage-primary);
  }
</style>
