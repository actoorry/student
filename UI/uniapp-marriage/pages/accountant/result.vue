<!-- 支付结果页面 -->
<template>
  <s-layout :bgStyle="{ color: '#FFF' }" title="支付结果">
    <view class="pay-result-box ss-flex-col ss-row-center ss-col-center">
      <!-- 信息展示 -->
      <view class="pay-waiting ss-m-b-30" v-if="payResult === 'waiting'" />
      <image
        class="pay-img ss-m-b-30"
        v-if="payResult === 'success'"
        :src="sheep.$url.static('/static/img/shop/order/order_pay_success.gif')"
      />
      <image
        class="pay-img ss-m-b-30"
        v-if="['failed', 'closed'].includes(payResult)"
        :src="sheep.$url.static('/static/img/shop/order/order_paty_fail.gif')"
      />
      <view class="tip-text ss-m-b-30" v-if="payResult === 'success'">支付成功</view>
      <view class="tip-text ss-m-b-30" v-if="payResult === 'failed'">支付失败</view>
      <view class="tip-text ss-m-b-30" v-if="payResult === 'closed'">该订单已关闭</view>
      <view class="tip-text ss-m-b-30" v-if="payResult === 'waiting'">正在查询支付结果...</view>
      <view class="pay-total-num ss-flex" v-if="payResult === 'success'">
        <view>￥{{ fen2yuan(state.orderInfo.price) }}</view>
      </view>
      <view class="virtual-status ss-m-t-20" v-if="state.isVirtual">
        <text v-if="state.virtualResultUnavailable">虚拟支付确认暂不可用，请稍后刷新</text>
        <text v-else-if="payResult === 'success' && state.deliverStatus === 1">
          微信虚拟商品已完成发货确认
        </text>
        <text v-else-if="payResult === 'success' && state.deliverStatus === 2">
          微信发货确认失败，请联系客服
        </text>
        <text v-else-if="payResult === 'success'">支付已确认，微信发货确认中</text>
      </view>

      <!-- 操作区 -->
      <view class="btn-box ss-flex ss-row-center ss-m-t-50">
        <button class="back-btn ss-reset-button" @tap="sheep.$router.go('/pages/index/index')">
          返回首页
        </button>
        <button
          class="check-btn ss-reset-button"
          v-if="payResult === 'failed'"
          @tap="
            sheep.$router.redirect('/pages/accountant/index', { id: state.id, orderType: state.orderType })
          "
        >
          重新支付
        </button>
        <button class="check-btn ss-reset-button" v-if="payResult === 'success'" @tap="onOrder">
          查看订单
        </button>
        <button
          class="check-btn ss-reset-button"
          v-if="payResult === 'success' && state.salesOrder.type === 3"
          @tap="sheep.$router.redirect('/pages/activity/groupon/order')"
        >
          我的拼团
        </button>
      </view>

      <!-- #ifdef MP -->
      <view
        class="subscribe-box ss-flex ss-m-t-44"
        v-if="showSubscribeBtn && state.orderType === 'goods'"
      >
        <image class="subscribe-img" :src="sheep.$url.static('/static/img/shop/order/cargo.png')" />
        <view class="subscribe-title ss-m-r-48 ss-m-l-16">获取实时发货信息与订单状态</view>
        <view class="subscribe-start" @tap="subscribeMessage">立即订阅</view>
      </view>
      <!-- #endif -->
    </view>
  </s-layout>
</template>

<script setup>
  import { onHide, onLoad, onShow, onUnload } from '@dcloudio/uni-app';
  import { computed, reactive, ref } from 'vue';
  import { isEmpty } from 'lodash-es';
  import sheep from '@/sheep';
  import PayOrderApi from '@/sheep/api/accountant/order';
  import WechatVirtualPayApi from '@/sheep/api/accountant/wechat-virtual-pay';
  import { fen2yuan } from '@/sheep/hooks/useGoods';
  import OrderApi from '@/sheep/api/sales/order';
  import { WxaSubscribeTemplate } from '@/sheep/helper/const';

  const state = reactive({
    id: 0, // 支付单号
    orderType: 'goods', // 订单类型
    result: 'unpaid', // 支付状态
    orderInfo: {}, // 支付订单信息
    salesOrder: {}, // 商品订单信息，只有在 orderType 为 goods 才会请求。目的：【我的拼团】按钮的展示
    counter: 0, // 获取结果次数
    isVirtual: false,
    deliverStatus: null,
    virtualResultUnavailable: false,
  });
  let pollTimer;

  function clearPollTimer() {
    if (pollTimer) {
      clearTimeout(pollTimer);
      pollTimer = undefined;
    }
  }

  function scheduleNextQuery(id) {
    clearPollTimer();
    pollTimer = setTimeout(() => {
      getOrderInfo(id);
    }, 2000);
  }

  // 支付结果 result => payResult
  const payResult = computed(() => {
    if (state.result === 'unpaid') {
      return 'waiting';
    }
    if (state.result === 'paid') {
      return 'success';
    }
    if (state.result === 'failed') {
      return 'failed';
    }
    if (state.result === 'closed') {
      return 'closed';
    }
  });

  function showRepayModal() {
    if (state.result !== 'failed') return;
    uni.showModal({
      title: '确认支付',
      content: '未检测到您的支付结果,请确认您是否已经支付完成？',
      cancelText: '取消',
      confirmText: '我已支付',
      success: function (res) {
        if (res.confirm) {
          state.counter = 0;
          setTimeout(() => {
            getOrderInfo(state.id);
          }, 100);
        }
      },
    });
  }

  // 获得订单信息
  async function getOrderInfo(id) {
    state.counter++;
    // 1. 加载订单信息
    const { data, code } = await PayOrderApi.getOrder(id, !state.isVirtual);
    if (code === 0) {
      state.orderInfo = data;
      let authoritativePayStatus = state.orderInfo?.status;
      if (state.isVirtual) {
        try {
          const virtualResult = await WechatVirtualPayApi.getResult(id);
          if (virtualResult.code === 0 && virtualResult.data) {
            authoritativePayStatus = virtualResult.data.payStatus;
            state.deliverStatus = virtualResult.data.deliverStatus;
            state.virtualResultUnavailable = false;
          } else {
            state.virtualResultUnavailable = true;
          }
        } catch (error) {
          state.virtualResultUnavailable = true;
        }
      }
      if (!state.orderInfo || authoritativePayStatus === 30) {
        // 支付关闭
        state.result = 'closed';
        return;
      }
      if (authoritativePayStatus === 10 || authoritativePayStatus === 20) {
        // 非待支付，可能是已支付，可能是已退款
        const firstPaidConfirmation = state.result !== 'paid';
        state.result = 'paid';
        // #ifdef MP
        if (firstPaidConfirmation) {
          uni.showModal({
            title: '支付结果',
            showCancel: false, // 不要取消按钮
            content: '支付成功',
            success: () => {
              // 订阅只能由用户主动触发，只能包一层 showModal 诱导用户点击
              autoSubscribeMessage();
            },
          });
        }

        // #endif
        // 特殊：获得商品订单信息
        if (state.orderType === 'goods') {
          const { data, code } = await OrderApi.getOrderDetail(
            state.orderInfo.merchantOrderId,
            true,
          );
          if (code === 0) {
            state.salesOrder = data;
          }
        }
        if (state.isVirtual && state.deliverStatus === 0 && state.counter < 6) {
          scheduleNextQuery(id);
        }
        return;
      }
    }
    const maxQueryCount = state.isVirtual ? 6 : 5;
    // 2.1 未支付且未达到上限，继续轮询
    if (state.counter < maxQueryCount && state.result === 'unpaid') {
      scheduleNextQuery(id);
    }
    // 普通支付保留原失败交互；虚拟支付超时不推断为失败
    if (!state.isVirtual && state.counter >= maxQueryCount) {
      state.result = 'failed';
      showRepayModal();
    }
  }

  function onOrder() {
    if (state.orderType === 'recharge') {
      sheep.$router.redirect('/pages/accountant/recharge-log');
    } else {
      sheep.$router.redirect('/pages/order/list');
    }
  }

  // #ifdef MP
  const showSubscribeBtn = ref(false); // 默认隐藏
  const SUBSCRIBE_BTN_STATUS_STORAGE_KEY = 'subscribe_btn_status';
  function subscribeMessage() {
    if (state.orderType !== 'goods') {
      return;
    }
    const event = [WxaSubscribeTemplate.TRADE_ORDER_DELIVERY];
    if (state.salesOrder.type === 3) {
      event.push(WxaSubscribeTemplate.PROMOTION_COMBINATION_SUCCESS);
    }
    sheep.$platform.useProvider('wechat').subscribeMessage(event, () => {
      // 订阅后记录一下订阅状态
      uni.removeStorageSync(SUBSCRIBE_BTN_STATUS_STORAGE_KEY);
      uni.setStorageSync(SUBSCRIBE_BTN_STATUS_STORAGE_KEY, '已订阅');
      // 隐藏订阅按钮
      showSubscribeBtn.value = false;
    });
  }
  async function autoSubscribeMessage() {
    // 1. 校验是否手动订阅过
    const subscribeBtnStatus = uni.getStorageSync(SUBSCRIBE_BTN_STATUS_STORAGE_KEY);
    if (!subscribeBtnStatus) {
      showSubscribeBtn.value = true;
      return;
    }

    // 2. 订阅消息
    subscribeMessage();
  }
  // #endif

  onLoad(async (options) => {
    // 支付订单号
    if (options.id) {
      state.id = options.id;
    }
    // 订单类型
    if (options.orderType) {
      state.orderType = options.orderType;
    }
    state.isVirtual = options.virtual === '1' || options.virtual === 1;

    // 支付结果传值过来是失败，则直接显示失败界面
    if (options.payState === 'fail') {
      state.result = 'failed';
    } else {
      // 轮询五次检测订单支付结果
      await getOrderInfo(state.id);
    }
  });

  onShow(() => {
    if (isEmpty(state.orderInfo)) {
      return;
    }
    getOrderInfo(state.id);
  });

  onHide(() => {
    clearPollTimer();
    state.result = 'unpaid';
    state.counter = 0;
  });

  onUnload(() => {
    clearPollTimer();
  });
</script>

<style lang="scss" scoped>
  @keyframes rotation {
    0% {
      transform: rotate(0deg);
    }

    100% {
      transform: rotate(360deg);
    }
  }

  .score-img {
    width: 36rpx;
    height: 36rpx;
    margin: 0 4rpx;
  }

  .pay-result-box {
    padding: 60rpx 0;

    .pay-waiting {
      margin-top: 20rpx;
      width: 60rpx;
      height: 60rpx;
      border: 10rpx solid rgb(233, 231, 231);
      border-bottom-color: rgb(204, 204, 204);
      border-radius: 50%;
      display: inline-block;
      // -webkit-animation: rotation 1s linear infinite;
      animation: rotation 1s linear infinite;
    }

    .pay-img {
      width: 130rpx;
      height: 130rpx;
    }

    .tip-text {
      font-size: 30rpx;
      font-weight: bold;
      color: #333333;
    }

    .pay-total-num {
      font-size: 36rpx;
      font-weight: 500;
      color: #333333;
      font-family: OPPOSANS;
    }

    .virtual-status {
      padding: 0 48rpx;
      color: #666;
      font-size: 26rpx;
      text-align: center;
    }

    .btn-box {
      width: 100%;

      .back-btn {
        width: 190rpx;
        height: 70rpx;
        font-size: 28rpx;
        border: 2rpx solid #dfdfdf;
        border-radius: 35rpx;
        font-weight: 400;
        color: #595959;
      }

      .check-btn {
        width: 190rpx;
        height: 70rpx;
        font-size: 28rpx;
        border: 2rpx solid #dfdfdf;
        border-radius: 35rpx;
        font-weight: 400;
        color: #595959;
        margin-left: 32rpx;
      }
    }

    .subscribe-box {
      .subscribe-img {
        width: 44rpx;
        height: 44rpx;
      }

      .subscribe-title {
        font-weight: 500;
        font-size: 32rpx;
        line-height: 36rpx;
        color: #434343;
      }

      .subscribe-start {
        color: var(--ui-BG-Main);
        font-weight: 700;
        font-size: 32rpx;
        line-height: 36rpx;
      }
    }
  }
</style>
