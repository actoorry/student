<!-- 收银台 -->
<template>
  <s-layout title="收银台">
    <view class="bg-white ss-modal-box ss-flex-col">
      <!-- 订单信息 -->
      <view class="modal-header ss-flex-col ss-col-center ss-row-center">
        <view class="money-box ss-m-b-20">
          <text class="money-text">{{ fen2yuan(state.orderInfo.price) }}</text>
        </view>
        <view class="time-text">
          <text>{{ payDescText }}</text>
        </view>
      </view>

      <!-- 支付方式 -->
      <view class="modal-content ss-flex-1">
        <view class="pay-title ss-p-l-30 ss-m-y-30">选择支付方式</view>
        <view class="pay-tip ss-p-x-30 ss-m-b-20" v-if="state.unavailableReason">
          {{ state.unavailableReason }}
        </view>
        <radio-group @change="onTapPay">
          <label class="pay-type-item" v-for="item in state.payMethods" :key="item.title">
            <view
              class="pay-item ss-flex ss-col-center ss-row-between ss-p-x-30 border-bottom"
              :class="{ 'disabled-pay-item': item.disabled }"
            >
              <view class="ss-flex ss-col-center">
                <image
                  class="pay-icon"
                  v-if="item.disabled"
                  :src="sheep.$url.static('/static/img/shop/pay/cod_disabled.png')"
                  mode="aspectFit"
                />
                <image
                  class="pay-icon"
                  v-else
                  :src="sheep.$url.static(item.icon)"
                  mode="aspectFit"
                />
                <text class="pay-title">{{ item.title }}</text>
              </view>
              <view class="check-box ss-flex ss-col-center ss-p-l-10">
                <view class="userInfo-money ss-m-r-10" v-if="item.value === 'wallet'">
                  余额: {{ fen2yuan(userWallet.balance) }}元
                </view>
                <radio
                  :value="item.value"
                  color="var(--ui-BG-Main)"
                  style="transform: scale(0.8)"
                  :disabled="item.disabled"
                  :checked="state.payment === item.value"
                />
              </view>
            </view>
          </label>
        </radio-group>
      </view>

      <!-- 工具 -->
      <view class="modal-footer ss-flex ss-row-center ss-col-center ss-m-t-80 ss-m-b-40">
        <button v-if="state.payStatus === 0" class="ss-reset-button past-due-btn">
          检测支付环境中
        </button>
        <button v-else-if="state.payStatus === -1" class="ss-reset-button past-due-btn" disabled>
          支付已过期
        </button>
        <button
          v-else
          class="ss-reset-button save-btn"
          @tap="onPay"
          :disabled="state.payStatus !== 1 || state.isPaying || !state.payment"
          :class="{ 'disabled-btn': state.payStatus !== 1 || state.isPaying || !state.payment }"
        >
          {{ state.isPaying ? '支付处理中' : '立即支付' }}
        </button>
      </view>
    </view>
  </s-layout>
</template>
<script setup>
  import { computed, reactive } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import { fen2yuan, useDurationTime } from '@/sheep/hooks/useGoods';
  import PayOrderApi from '@/sheep/api/accountant/order';
  import PayChannelApi from '@/sheep/api/accountant/channel';
  import WechatVirtualPayApi from '@/sheep/api/accountant/wechat-virtual-pay';
  import {
    resolveWechatMiniProgramCheckout,
    WECHAT_VIRTUAL_CHECKOUT_MODE,
  } from '@/sheep/helper/wechat-virtual-payment';
  import { getPayMethods, goPayResult } from '@/sheep/platform/pay';

  const userWallet = computed(() => sheep.$store('user').userWallet);

  // 检测支付环境
  const state = reactive({
    orderType: 'goods', // 订单类型; goods - 商品订单, recharge - 充值订单
    orderInfo: {}, // 支付单信息
    payStatus: 0, // 0=检测支付环境, -2=未查询到支付单信息， -1=支付已过期， 1=待支付，2=订单已支付
    payMethods: [], // 可选的支付方式
    payment: '', // 选中的支付方式
    checkoutProfileMode: '',
    unavailableReason: '',
    isPaying: false,
  });

  const onPay = () => {
    if (state.isPaying) {
      return;
    }
    if (state.payment === '') {
      sheep.$helper.toast('请选择支付方式');
      return;
    }
    if (state.payment === 'wallet') {
      uni.showModal({
        title: '提示',
        content: '确定要支付吗?',
        success: function (res) {
          if (res.confirm) {
            sheep.$platform.pay(state.payment, state.orderType, state.orderInfo.id);
          }
        },
      });
    } else {
      if (state.payment === 'wechat_virtual') {
        state.isPaying = true;
        sheep.$platform.pay(state.payment, state.orderType, state.orderInfo.id, {
          onFinish: () => {
            state.isPaying = false;
          },
        });
        return;
      }
      sheep.$platform.pay(state.payment, state.orderType, state.orderInfo.id);
    }
  };

  // 支付文案提示
  const payDescText = computed(() => {
    if (state.payStatus === 2) {
      return '该订单已支付';
    }
    if (state.payStatus === 1) {
      const time = useDurationTime(state.orderInfo.expireTime);
      if (time.ms <= 0) {
        state.payStatus = -1;
        return '';
      }
      return `剩余支付时间 ${time.h}:${time.m}:${time.s} `;
    }
    if (state.payStatus === -2) {
      return '未查询到支付单信息';
    }
    return '';
  });

  // 状态转换：payOrder.status => payStatus
  function checkPayStatus() {
    if (state.orderInfo.status === 10 || state.orderInfo.status === 20) {
      // 支付成功
      state.payStatus = 2;
      // 跳转回支付成功页
      uni.showModal({
        title: '提示',
        content: '订单已支付',
        showCancel: false,
        success: function () {
          goPayResult(state.orderInfo.id, state.orderType);
        },
      });
      return;
    }
    if (state.orderInfo.status === 30) {
      // 支付关闭
      state.payStatus = -1;
      return;
    }
    state.payStatus = 1; // 待支付
  }

  // 切换支付方式
  function onTapPay(e) {
    state.payment = e.detail.value;
  }

  // 设置支付订单信息
  async function setOrder(id) {
    // 获得支付订单信息
    const { data, code } = await PayOrderApi.getOrder(id, true);
    if (code !== 0 || !data) {
      state.payStatus = -2;
      return;
    }
    state.orderInfo = data;
    // 设置支付状态
    checkPayStatus();
    // 获得支付方式
    await setPayMethods();
  }

  // 获得支付方式
  async function setPayMethods() {
    state.payment = '';
    state.payMethods = [];
    state.unavailableReason = '';
    if (sheep.$platform.name === 'WechatMiniProgram') {
      let profileResult;
      try {
        profileResult = await WechatVirtualPayApi.getCheckoutProfile(state.orderInfo.id);
      } catch (error) {
        state.unavailableReason = '暂时无法确认订单支付方式，请稍后重试';
        return;
      }
      if (profileResult.code !== 0 || !profileResult.data) {
        state.unavailableReason = '暂时无法确认订单支付方式，请稍后重试';
        return;
      }
      state.checkoutProfileMode = profileResult.data.mode;
      if (profileResult.data.mode === WECHAT_VIRTUAL_CHECKOUT_MODE.VIRTUAL) {
        state.payMethods = [
          {
            icon: '/static/img/shop/pay/wechat.png',
            title: '微信虚拟支付',
            value: 'wechat_virtual',
            disabled: false,
          },
        ];
        state.payment = 'wechat_virtual';
        return;
      }
      if (profileResult.data.mode === WECHAT_VIRTUAL_CHECKOUT_MODE.NOT_READY) {
        const route = resolveWechatMiniProgramCheckout(profileResult.data);
        state.unavailableReason = route.unavailableReason;
        return;
      }
      if (profileResult.data.mode !== WECHAT_VIRTUAL_CHECKOUT_MODE.MERCHANT) {
        state.unavailableReason = '服务端返回了无法识别的支付方式';
        return;
      }
    }
    const { data, code } = await PayChannelApi.getEnableChannelCodeList(state.orderInfo.appId);
    if (code !== 0) {
      state.unavailableReason = '支付渠道暂不可用，请稍后重试';
      return;
    }
    const route = resolveWechatMiniProgramCheckout(
      { mode: WECHAT_VIRTUAL_CHECKOUT_MODE.MERCHANT },
      data,
    );
    state.payMethods = getPayMethods(route.channels);
    state.payMethods.find((item) => {
      if (item.value && !item.disabled) {
        state.payment = item.value;
        return true;
      }
    });
  }

  onLoad((options) => {
    if (
      sheep.$platform.name === 'WechatOfficialAccount' &&
      sheep.$platform.os === 'ios' &&
      !sheep.$platform.landingPage.includes('pages/accountant/index')
    ) {
      location.reload();
      return;
    }
    // 获得支付订单信息
    let id = options.id;
    if (options.orderType) {
      state.orderType = options.orderType;
    }
    setOrder(id);
    // 刷新钱包的缓存
    sheep.$store('user').getWallet();
  });
</script>

<style lang="scss" scoped>
  .pay-icon {
    width: 36rpx;
    height: 36rpx;
    margin-right: 26rpx;
  }

  .ss-modal-box {
    // max-height: 1000rpx;

    .modal-header {
      position: relative;
      padding: 60rpx 20rpx 40rpx;

      .money-text {
        color: $red;
        font-size: 46rpx;
        font-weight: bold;
        font-family: OPPOSANS;

        &::before {
          content: '￥';
          font-size: 30rpx;
        }
      }

      .time-text {
        font-size: 26rpx;
        color: $gray-b;
      }

      .close-icon {
        position: absolute;
        top: 10rpx;
        right: 20rpx;
        font-size: 46rpx;
        opacity: 0.2;
      }
    }

    .modal-content {
      overflow-y: auto;

      .pay-title {
        font-size: 26rpx;
        font-weight: 500;
        color: #333333;
      }

      .pay-tip {
        font-size: 26rpx;
        color: #bbbbbb;
      }

      .pay-item {
        height: 86rpx;
      }
      .disabled-pay-item {
        .pay-title {
          color: #999999;
        }
      }

      .userInfo-money {
        font-size: 26rpx;
        color: #bbbbbb;
        line-height: normal;
      }
    }

    .save-btn {
      width: 710rpx;
      height: 80rpx;
      border-radius: 40rpx;
      background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
      color: $white;
    }
    .disabled-btn {
      background: #e5e5e5;
      color: #999999;
    }

    .past-due-btn {
      width: 710rpx;
      height: 80rpx;
      border-radius: 40rpx;
      background-color: #999;
      color: #fff;
    }
  }
</style>
