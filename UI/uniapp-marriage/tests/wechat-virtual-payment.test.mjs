import { describe, it } from 'node:test';
import assert from 'node:assert';
import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import {
  buildRequestVirtualPaymentParams,
  buildWechatVirtualPaymentErrorSummary,
  isWechatVirtualPaymentCancelled,
  resolveWechatMiniProgramCheckout,
  WECHAT_VIRTUAL_CHECKOUT_MODE,
} from '../sheep/helper/wechat-virtual-payment.js';
import {
  ClientPlatform,
  getClientContext,
  normalizeClientPlatform,
} from '../sheep/request/client-context.js';

const root = dirname(fileURLToPath(import.meta.url));
const read = (relativePath) => readFileSync(join(root, relativePath), 'utf-8');

describe('Shared client platform context', () => {
  it('uses the compiled sheep platform as a mini-program fallback on real devices', () => {
    const uniApi = {
      getAppBaseInfo: () => ({ appVersion: '1.2.3' }),
      getSystemInfoSync: () => ({ platform: 'ios', system: 'iOS 18.0' }),
    };

    assert.deepStrictEqual(getClientContext('WechatMiniProgram', uniApi), {
      platform: ClientPlatform.MP_WEIXIN,
      appVersion: '1.2.3',
    });
  });

  it('keeps direct uniPlatform mapping and unknown environments isolated', () => {
    assert.strictEqual(
      normalizeClientPlatform({ uniPlatform: 'mp-weixin', platform: 'ios' }),
      ClientPlatform.MP_WEIXIN,
    );
    assert.strictEqual(
      normalizeClientPlatform({ platform: 'ios', system: 'iOS 18.0' }),
      ClientPlatform.UNKNOWN,
    );
  });
});

describe('WeChat mini-program checkout profile routing', () => {
  it('isolates virtual, merchant and not-ready modes', () => {
    assert.deepStrictEqual(
      resolveWechatMiniProgramCheckout(
        { mode: WECHAT_VIRTUAL_CHECKOUT_MODE.VIRTUAL },
        ['wx_lite'],
      ),
      { payment: 'wechat_virtual', channels: [], unavailableReason: '' },
    );
    assert.deepStrictEqual(
      resolveWechatMiniProgramCheckout(
        { mode: WECHAT_VIRTUAL_CHECKOUT_MODE.MERCHANT },
        ['wx_lite'],
      ),
      { payment: '', channels: ['wx_lite'], unavailableReason: '' },
    );
    assert.deepStrictEqual(
      resolveWechatMiniProgramCheckout({
        mode: WECHAT_VIRTUAL_CHECKOUT_MODE.NOT_READY,
        unavailableReason: '道具尚未发布',
      }),
      { payment: '', channels: [], unavailableReason: '道具尚未发布' },
    );
  });

  it('passes the original signData string without parsing or serialization', () => {
    const signData =
      '{"offerId":"1450575102","buyQuantity":2,"env":1,"currencyType":"CNY"}';
    const params = buildRequestVirtualPaymentParams({
      mode: 'short_series_goods',
      signData,
      paySig: 'pay-signature',
      signature: 'user-signature',
    });

    assert.strictEqual(params.signData, signData);
    assert.deepStrictEqual(params, {
      mode: 'short_series_goods',
      signData,
      paySig: 'pay-signature',
      signature: 'user-signature',
    });
    assert.throws(
      () => buildRequestVirtualPaymentParams({ mode: 'short_series_goods' }),
      /INVALID_WECHAT_VIRTUAL_PAYMENT_PARAMS/,
    );
  });

  it('recognizes cancellation without exposing raw payment errors', () => {
    assert.strictEqual(
      isWechatVirtualPaymentCancelled({ errMsg: 'requestVirtualPayment:fail cancel' }),
      true,
    );
    assert.strictEqual(
      isWechatVirtualPaymentCancelled({ errMsg: 'requestVirtualPayment:fail system error' }),
      false,
    );
  });

  it('keeps staged diagnostics useful without exposing identity or signing material', () => {
    assert.deepStrictEqual(
      buildWechatVirtualPaymentErrorSummary('submit', {
        code: 503,
        statusCode: 502,
        errMsg: 'request:fail upstream unavailable',
      }),
      {
        stage: 'submit',
        code: '503',
        statusCode: '502',
        message: 'request:fail upstream unavailable',
      },
    );
    assert.deepStrictEqual(
      buildWechatVirtualPaymentErrorSummary('native', {
        code: 'INVALID_SIGNATURE',
        errMsg: 'signature contains sensitive payment material',
      }),
      {
        stage: 'native',
      },
    );
    assert.doesNotMatch(
      JSON.stringify(
        buildWechatVirtualPaymentErrorSummary('identity', {
          errMsg: 'openid should never be logged',
        }),
      ),
      /openid|signature|paySig|signData|session_key|appKey/i,
    );
  });
});

describe('WeChat virtual payment integration boundaries', () => {
  const api = read('../sheep/api/accountant/wechat-virtual-pay.js');
  const checkout = read('../pages/accountant/index.vue');
  const pay = read('../sheep/platform/pay.js');
  const result = read('../pages/accountant/result.vue');
  const skuSelector = read('../sheep/components/s-select-sku/s-select-sku.vue');

  it('uses the existing sheep request layer for all three server endpoints', () => {
    assert.match(api, /\/accountant\/wechat-virtual-pay\/checkout-profile/);
    assert.match(api, /\/accountant\/wechat-virtual-pay\/submit/);
    assert.match(api, /\/accountant\/wechat-virtual-pay\/result/);
    assert.doesNotMatch(api, /uni\.request|fetch\(|axios/);
  });

  it('routes by server profile and never selects ordinary payment for a virtual order', () => {
    assert.match(checkout, /WechatVirtualPayApi\.getCheckoutProfile/);
    assert.match(checkout, /WECHAT_VIRTUAL_CHECKOUT_MODE\.VIRTUAL/);
    assert.match(checkout, /WECHAT_VIRTUAL_CHECKOUT_MODE\.MERCHANT/);
    assert.match(checkout, /WECHAT_VIRTUAL_CHECKOUT_MODE\.NOT_READY/);
    assert.match(checkout, /state\.payment = 'wechat_virtual'/);
    assert.match(checkout, /state\.isPaying = true/);
    assert.match(checkout, /if \(state\.isPaying\)/);
  });

  it('keeps native APIs isolated and forwards only server signing fields', () => {
    const virtualPay = pay.slice(
      pay.indexOf('async wechatVirtualPay()'),
      pay.indexOf('// 余额支付'),
    );
    assert.match(virtualPay, /useProvider\('wechat'\)\?\.getInfo\?\.\(\)/);
    assert.doesNotMatch(virtualPay, /getOpenid\(true\)/);
    assert.match(virtualPay, /WechatVirtualPayApi\.submit\(this\.id, openid\)/);
    assert.ok(
      virtualPay.indexOf("useProvider('wechat')?.getInfo?.()") <
        virtualPay.indexOf('WechatVirtualPayApi.submit(this.id, openid)'),
    );
    assert.match(virtualPay, /wx\.requestVirtualPayment\(\{\s*\.\.\.paymentParams/);
    assert.doesNotMatch(virtualPay, /JSON\.parse\([^)]*signData/);
    assert.match(pay, /this\.prepay\('wx_lite'\)/);
    assert.match(pay, /uni\.requestPayment\(\{/);
    assert.match(pay, /当前环境不支持虚拟支付/);
  });

  it('distinguishes identity, submit, parameters and native failures', () => {
    assert.match(pay, /logFailure\('identity'/);
    assert.match(pay, /logFailure\('submit'/);
    assert.match(pay, /logFailure\('submit-response'/);
    assert.match(pay, /logFailure\('parameters'/);
    assert.match(pay, /logFailure\('native'/);
    assert.match(pay, /logFailure\('native-invoke'/);
    assert.match(pay, /buildWechatVirtualPaymentErrorSummary/);
    assert.doesNotMatch(pay, /console\.error\([^\n]*(openid|signData|paySig|signature)/);
  });

  it('queries server facts on the virtual result page and preserves cart prohibition', () => {
    assert.match(result, /PayOrderApi\.getOrder\(id,\s*!state\.isVirtual\)/);
    assert.match(result, /WechatVirtualPayApi\.getResult\(id\)/);
    assert.match(result, /state\.isVirtual \? 6 : 5/);
    assert.match(result, /虚拟支付超时不推断为失败/);
    assert.match(result, /clearPollTimer\(\)/);
    assert.match(skuSelector, /v-if="!goodsInfo\.isWechatMiniappVirtualGoods"/);
    assert.match(skuSelector, /if \(props\.goodsInfo\.isWechatMiniappVirtualGoods\)/);
  });
});
