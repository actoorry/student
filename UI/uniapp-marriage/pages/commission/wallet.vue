<!-- 分销 - 佣金明细 -->
<template>
  <s-layout class="wallet-wrap" title="佣金明细">
    <!-- 钱包卡片 -->
    <view class="header-box ss-flex ss-row-center ss-col-center">
      <view class="card-box ui-BG-Main ui-Shadow-Main">
        <view class="card-head ss-flex ss-col-center">
          <view class="card-title ss-m-r-10">当前佣金（元）</view>
          <view
            @tap="state.showMoney = !state.showMoney"
            class="ss-eye-icon"
            :class="state.showMoney ? 'cicon-eye' : 'cicon-eye-off'"
          />
        </view>
        <view class="ss-flex ss-row-between ss-col-center ss-m-t-30">
          <view class="money-num">{{
            state.showMoney ? fen2yuan(walletSummary.currentPrice) : '*****'
          }}</view>
          <view class="ss-flex">
            <view class="ss-m-r-20">
              <button
                class="ss-reset-button withdraw-btn"
                @tap="sheep.$router.go('/pages/commission/withdraw')"
              >
                提现
              </button>
            </view>
            <button class="ss-reset-button balance-btn ss-m-l-20" @tap="state.showModal = true">
              转余额
            </button>
          </view>
        </view>

        <view class="ss-flex">
          <view class="loading-money">
            <view class="loading-money-title">冻结佣金</view>
            <view class="loading-money-num">
              {{ state.showMoney ? fen2yuan(walletSummary.frozenPrice) : '*****' }}
            </view>
          </view>
          <view class="loading-money ss-m-l-100">
            <view class="loading-money-title">可提现佣金</view>
            <view class="loading-money-num">
              {{ state.showMoney ? fen2yuan(walletSummary.withdrawablePrice) : '*****' }}
            </view>
          </view>
        </view>
      </view>
    </view>

    <su-sticky>
      <!-- 统计 -->
      <view class="filter-box ss-p-x-30 ss-flex ss-col-center ss-row-between">
        <uni-datetime-picker
          v-model="state.date"
          type="daterange"
          @change="onChangeTime"
          :end="state.today"
        >
          <button class="ss-reset-button date-btn">
            <text>{{ dateFilterText }}</text>
            <text class="cicon-drop-down ss-seldate-icon" />
          </button>
        </uni-datetime-picker>

        <view class="total-box">
          <!-- TODO 芋艿：【钱包-可优化】这里暂时不考虑做 -->
          <!-- <view class="ss-m-b-10">总收入￥{{ state.pagination.income.toFixed(2) }}</view> -->
          <!-- <view>总支出￥{{ (-state.pagination.expense).toFixed(2) }}</view> -->
        </view>
      </view>
      <su-tabs
        :list="tabMaps"
        @change="onChangeTab"
        :scrollable="false"
        :current="state.currentTab"
      />
    </su-sticky>
    <view v-if="state.loading" class="page-state">佣金记录加载中…</view>
    <view v-else-if="state.error" class="page-state page-state--error">
      <text>{{ state.error }}</text>
      <text class="retry" @tap="loadWallet">重试</text>
    </view>
    <s-empty
      v-else-if="state.pagination.total === 0"
      icon="/static/data-empty.png"
      text="暂无数据"
    ></s-empty>

    <!-- 转余额弹框 -->
    <su-popup
      :show="state.showModal"
      type="bottom"
      round="20"
      @close="state.showModal = false"
      showClose
    >
      <view class="ss-p-x-20 ss-p-y-30">
        <view class="model-title ss-m-b-30 ss-m-l-20">转余额</view>
        <view class="model-subtitle ss-m-b-100 ss-m-l-20">将您的佣金转到余额中继续消费</view>
        <view class="input-box ss-flex ss-col-center border-bottom ss-m-b-70 ss-m-x-20">
          <view class="unit">￥</view>
          <uni-easyinput
            :inputBorder="false"
            class="ss-flex-1 ss-p-l-10"
            v-model="state.price"
            type="number"
            placeholder="请输入金额"
          />
        </view>
        <button
          class="ss-reset-button model-btn ui-BG-Main-Gradient ui-Shadow-Main"
          :disabled="state.transferring"
          @tap="onConfirm"
        >
          {{ state.transferring ? '提交中…' : '确定' }}
        </button>
      </view>
    </su-popup>

    <!-- 钱包记录 -->
    <view v-if="state.pagination.total > 0">
      <!-- 分佣列表 -->
      <view v-if="state.currentTab === 0">
        <view
          class="wallet-list ss-flex border-bottom"
          v-for="item in state.pagination.list"
          :key="item.id"
        >
          <view class="list-content">
            <view class="title-box ss-flex ss-row-between ss-m-b-20">
              <text class="title ss-line-1">{{ item.title }}</text>
              <view class="money">
                <text v-if="item.price >= 0" class="add">+{{ fen2yuan(item.price) }}</text>
                <text v-else class="minus">{{ fen2yuan(item.price) }}</text>
              </view>
            </view>
            <view class="ss-flex ss-row-between ss-col-center">
              <text class="time">
                {{ sheep.$helper.timeFormat(item.createTime, 'yyyy-mm-dd hh:MM:ss') }}
              </text>
              <view class="ss-flex ss-col-center">
                <text class="status" :class="'status-' + item.status">{{ item.statusName }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      <!-- 提现列表 -->
      <view v-else>
        <view
          class="wallet-list ss-flex border-bottom"
          v-for="item in state.pagination.list"
          :key="item.id"
        >
          <view class="list-content">
            <view class="title-box ss-flex ss-row-between ss-m-b-20">
              <text class="title ss-line-1">{{ item.typeName }}</text>
              <view class="money">
                <text class="minus">{{ fen2yuan(item.price) }}</text>
              </view>
            </view>
            <view class="ss-flex ss-row-between ss-col-center">
              <text class="time">
                {{ sheep.$helper.timeFormat(item.createTime, 'yyyy-mm-dd hh:MM:ss') }}
              </text>
              <button
                v-if="item.status === 10 && item.type === 5 && item.payTransferId > 0"
                class="ss-reset-button confirm-btn ss-m-l-20"
                @tap="onRequestMerchantTransfer(item)"
              >
                确认收款
              </button>
              <text v-else class="status" :class="'status-' + item.status">{{
                item.statusName
              }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- <u-gap></u-gap> -->
    <uni-load-more
      v-if="state.pagination.total > 0"
      :status="state.loadStatus"
      :content-text="{
        contentdown: '上拉加载更多',
      }"
    />
  </s-layout>
</template>

<script setup>
  import { computed, reactive } from 'vue';
  import { onLoad, onReachBottom, onShow } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import dayjs from 'dayjs';
  import { concat } from 'lodash-es';
  import BrokerageApi from '@/sheep/api/sales/brokerage';
  import { fen2yuan } from '@/sheep/hooks/useGoods';
  import { resetPagination } from '@/sheep/helper/utils';
  import PayTransferApi from '@/sheep/api/accountant/transfer';
  import {
    buildBrokerageListParams,
    resolveBrokerageWalletSummary,
  } from '@/sheep/helper/brokerage-contract';

  const headerBg = sheep.$url.css('/static/img/shop/user/wallet_card_bg.png');

  const state = reactive({
    showMoney: false,
    summary: {}, // 分销信息

    today: '',
    date: [],
    currentTab: 0,
    pagination: {
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 8,
    },
    loadStatus: '',

    price: undefined,
    showModal: false,
    loading: false,
    error: '',
    transferring: false,
    transferingId: null,
    requestId: 0,
  });

  const tabMaps = [
    {
      name: '分佣',
      value: '1',
    },
    {
      name: '提现',
      value: '2',
    },
  ];

  const dateFilterText = computed(() => {
    if (!Array.isArray(state.date) || state.date.length < 2) {
      return '全部记录';
    }
    if (state.date[0] === state.date[1]) {
      return state.date[0];
    } else {
      return state.date.join('~');
    }
  });

  async function getLogList() {
    const requestId = ++state.requestId;
    state.loadStatus = 'loading';
    state.loading = true;
    state.error = '';
    try {
      const params = buildBrokerageListParams({
        pageSize: state.pagination.pageSize,
        pageNo: state.pagination.pageNo,
        dateRange: state.date,
      });
      const result = await (state.currentTab === 0
        ? BrokerageApi.getBrokerageRecordPage(params)
        : BrokerageApi.getBrokerageWithdrawPage(params));
      if (requestId !== state.requestId) return;
      if (result?.code !== 0) {
        state.error = '佣金记录暂时无法读取，请重试';
        return;
      }
      const data = result.data || { list: [], total: 0 };
      state.pagination.list = concat(state.pagination.list, data.list || []);
      state.pagination.total = data.total || 0;
      state.loadStatus = state.pagination.list.length < state.pagination.total ? 'more' : 'noMore';
    } catch (error) {
      if (requestId === state.requestId) state.error = error?.message || '佣金记录暂时无法读取，请重试';
    } finally {
      if (requestId === state.requestId) {
        state.loading = false;
        if (state.loadStatus === 'loading') state.loadStatus = 'noMore';
      }
    }
  }

  function onChangeTab(e) {
    resetPagination(state.pagination);
    state.currentTab = e.index;
    getLogList();
  }

  function onChangeTime(e) {
    state.date = Array.isArray(e) && e.length ? [e[0], e[e.length - 1]].filter(Boolean) : [];
    resetPagination(state.pagination);
    getLogList();
  }

  // 确认操作（转账到余额）
  async function onConfirm() {
    if (state.transferring) return;
    const priceFen = Math.round(Number(state.price) * 100);
    if (!Number.isFinite(priceFen) || priceFen <= 0) {
      sheep.$helper.toast('请输入正确的金额');
      return;
    }
    if (priceFen > walletSummary.value.withdrawablePrice) {
      sheep.$helper.toast('金额超过可转余额的佣金');
      return;
    }
    uni.showModal({
      title: '提示',
      content: '确认把您的佣金转入到余额钱包中？',
      success: async function (res) {
        if (!res.confirm) {
          return;
        }
        state.transferring = true;
        try {
          const { code, msg } = await BrokerageApi.createBrokerageWithdraw({
            type: 1, // 钱包
            price: priceFen,
          });
          if (code !== 0) {
            sheep.$helper.toast(msg || '转余额失败，请稍后重试');
            return;
          }
          state.showModal = false;
          state.price = undefined;
          await getAgentInfo();
          onChangeTab({ index: 1 });
        } catch (error) {
          sheep.$helper.toast(error?.message || '转余额失败，请稍后重试');
        } finally {
          state.transferring = false;
        }
      },
    });
  }

  async function getAgentInfo() {
    try {
      const { code, data } = await BrokerageApi.getBrokerageUserSummary();
      if (code !== 0) {
        state.error = '账户摘要暂时无法读取，请重试';
        return;
      }
      state.summary = data || {};
    } catch (error) {
      state.error = error?.message || '账户摘要暂时无法读取，请重试';
    }
  }

  // 微信场景下：用户确认收款
  // 可见 https://pay.weixin.qq.com/doc/v3/merchant/4012716430 文档
  async function onRequestMerchantTransfer(item) {
    if (state.transferingId === item.id) return;
    const requestMerchantTransfer = sheep.$platform.useProvider()
      ? sheep.$platform.useProvider().requestMerchantTransfer
      : undefined;
    if (!requestMerchantTransfer) {
      sheep.$helper.toast('仅微信平台支持该功能');
      return;
    }
    state.transferingId = item.id;
    try {
      // 获取提现详情
      const { code, data } = await BrokerageApi.getBrokerageWithdraw(item.id);
      if (code !== 0) return;
      if (data.status === 11) {
        sheep.$helper.toast('该提现单已确认收款');
        item.status = 11;
        return;
      }
      if (!data.transferChannelMchId || !data.transferChannelPackageInfo) {
        sheep.$helper.toast('提现信息异常，请稍后再试');
        return;
      }
      // 调用微信确认收款
      const payTransferId = data.payTransferId;
      await requestMerchantTransfer(
        data.transferChannelMchId,
        data.transferChannelPackageInfo,
        async (res) => {
          if (res.result !== 'success') {
            sheep.$helper.toast(res.errMsg);
            return;
          }
          try {
            await PayTransferApi.syncTransfer(payTransferId);
          } catch (error) {
            console.warn('syncTransfer failed', error);
          }
          // 查询提现单最新状态，避免把客户端成功误认为后端成功。
          const latest = await BrokerageApi.getBrokerageWithdraw(item.id);
          if (latest.data && latest.data.status !== 11) {
            sheep.$helper.toast('确认收款成功，但数据存在延迟，请以实际【微信支付】到账为准');
            return;
          }
          sheep.$helper.toast('确认收款成功');
          item.status = 11;
        },
      );
    } catch (error) {
      sheep.$helper.toast(error?.message || '确认收款失败，请稍后重试');
    } finally {
      state.transferingId = null;
    }
  }

  onLoad((options) => {
    state.today = dayjs().format('YYYY-MM-DD');
    if (options?.type === '2') {
      // 切换到"提现" tab 下
      state.currentTab = 1;
    }
  });

  const walletSummary = computed(() => resolveBrokerageWalletSummary(state.summary));

  async function loadWallet() {
    resetPagination(state.pagination);
    await Promise.all([getLogList(), getAgentInfo()]);
  }

  onShow(loadWallet);

  onReachBottom(() => {
    if (state.loadStatus === 'noMore') {
      return;
    }
    state.pagination.pageNo++;
    getLogList();
  });
</script>

<style lang="scss" scoped>
  // 钱包
  .header-box {
    background-color: $white;
    padding: 30rpx;

    .card-box {
      width: 100%;
      min-height: 300rpx;
      padding: 40rpx;
      background-size: 100% 100%;
      border-radius: 30rpx;
      overflow: hidden;
      position: relative;
      z-index: 1;
      box-sizing: border-box;

      &::after {
        content: '';
        display: block;
        width: 100%;
        height: 100%;
        z-index: 2;
        position: absolute;
        top: 0;
        left: 0;
        background: v-bind(headerBg) no-repeat;
        pointer-events: none;
      }

      .card-head {
        color: $white;
        font-size: 24rpx;
      }

      .ss-eye-icon {
        font-size: 40rpx;
        color: $white;
      }

      .money-num {
        font-size: 40rpx;
        line-height: normal;
        font-weight: 500;
        color: $white;
        font-family: OPPOSANS;
      }

      .reduce-num {
        font-size: 26rpx;
        font-weight: 400;
        color: $white;
      }

      .withdraw-btn {
        width: 120rpx;
        height: 60rpx;
        line-height: 60rpx;
        border-radius: 30px;
        font-size: 24rpx;
        font-weight: 500;
        background-color: $white;
        color: var(--ui-BG-Main);
      }

      .balance-btn {
        width: 120rpx;
        height: 60rpx;
        line-height: 60rpx;
        border-radius: 30px;
        font-size: 24rpx;
        font-weight: 500;
        color: $white;
        border: 1px solid $white;
      }
    }
  }

  .loading-money {
    margin-top: 56rpx;

    .loading-money-title {
      font-size: 24rpx;
      font-weight: 400;
      color: #ffffff;
      line-height: normal;
      margin-bottom: 30rpx;
    }

    .loading-money-num {
      font-size: 30rpx;
      font-family: OPPOSANS;
      font-weight: 500;
      color: #fefefe;
    }
  }

  // 筛选

  .filter-box {
    height: 120rpx;
    padding: 0 30rpx;
    background-color: $bg-page;

    .total-box {
      font-size: 24rpx;
      font-weight: 500;
      color: $dark-9;
    }

    .date-btn {
      background-color: $white;
      line-height: 54rpx;
      border-radius: 27rpx;
      padding: 0 20rpx;
      font-size: 24rpx;
      font-weight: 500;
      color: $dark-6;

      .ss-seldate-icon {
        font-size: 50rpx;
        color: $dark-9;
      }
    }
  }

  // tab
  .wallet-tab-card {
    .tab-item {
      height: 80rpx;
      position: relative;

      .tab-title {
        font-size: 30rpx;
      }

      .cur-tab-title {
        font-weight: $font-weight-bold;
      }

      .tab-line {
        width: 60rpx;
        height: 6rpx;
        border-radius: 6rpx;
        position: absolute;
        left: 50%;
        transform: translateX(-50%);
        bottom: 2rpx;
        background-color: var(--ui-BG-Main);
      }
    }
  }

  // 钱包记录
  .wallet-list {
    padding: 30rpx;
    background-color: #ffff;

    .head-img {
      width: 70rpx;
      height: 70rpx;
      border-radius: 50%;
      background: $gray-c;
    }

    .list-content {
      justify-content: space-between;
      align-items: flex-start;
      flex: 1;

      .title {
        font-size: 28rpx;
        color: $dark-3;
        width: 400rpx;
      }

      .time {
        color: $gray-c;
        font-size: 22rpx;
      }
    }

    .money {
      font-size: 28rpx;
      font-weight: bold;
      font-family: OPPOSANS;

      .add {
        color: var(--ui-BG-Main);
      }

      .minus {
        color: $dark-3;
      }
    }

    .confirm-btn {
      font-size: 22rpx;
      color: var(--ui-BG-Main);
      background: rgba(var(--ui-BG-Main-rgb), 0.1);
      padding: 4rpx 16rpx;
      margin: 0;
      line-height: 1.4;
      border-radius: 20rpx;
      border: 1px solid var(--ui-BG-Main);
    }
  }

  .model-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333333;
  }

  .model-subtitle {
    font-size: 26rpx;
    color: #c2c7cf;
  }

  .model-btn {
    width: 100%;
    height: 80rpx;
    border-radius: 40rpx;
    font-size: 28rpx;
    font-weight: 500;
    color: #ffffff;
    line-height: normal;
  }

  .input-box {
    height: 100rpx;

    .unit {
      font-size: 48rpx;
      color: #333;
      font-weight: 500;
      line-height: normal;
    }

    .uni-easyinput__placeholder-class {
      font-size: 30rpx;
      height: 40rpx;
      line-height: normal;
    }
  }

  .status {
    font-size: 22rpx;
    &.status-0 {
      color: #ff9900;
    }
    &.status-1 {
      color: #19be6b;
    }
    &.status-2 {
      color: #fa3534;
    }
  }

  .page-state {
    padding: 80rpx 30rpx;
    color: var(--marriage-muted);
    text-align: center;
  }

  .page-state--error {
    display: flex;
    justify-content: center;
    gap: 18rpx;
  }

  .retry {
    color: var(--marriage-primary);
  }
</style>
