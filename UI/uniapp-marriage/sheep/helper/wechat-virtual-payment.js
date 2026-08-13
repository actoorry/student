export const WECHAT_VIRTUAL_CHECKOUT_MODE = Object.freeze({
  VIRTUAL: 'WECHAT_VIRTUAL',
  MERCHANT: 'WECHAT_MERCHANT',
  NOT_READY: 'VIRTUAL_NOT_READY',
});

export function resolveWechatMiniProgramCheckout(profile, merchantChannels = []) {
  if (profile?.mode === WECHAT_VIRTUAL_CHECKOUT_MODE.VIRTUAL) {
    return {
      payment: 'wechat_virtual',
      channels: [],
      unavailableReason: '',
    };
  }
  if (profile?.mode === WECHAT_VIRTUAL_CHECKOUT_MODE.MERCHANT) {
    return {
      payment: '',
      channels: merchantChannels,
      unavailableReason: '',
    };
  }
  return {
    payment: '',
    channels: [],
    unavailableReason: profile?.unavailableReason || '当前订单暂不支持支付',
  };
}

export function buildRequestVirtualPaymentParams(submitResponse) {
  if (
    submitResponse?.mode !== 'short_series_goods' ||
    typeof submitResponse.signData !== 'string' ||
    !submitResponse.signData ||
    typeof submitResponse.paySig !== 'string' ||
    !submitResponse.paySig ||
    typeof submitResponse.signature !== 'string' ||
    !submitResponse.signature
  ) {
    throw new Error('INVALID_WECHAT_VIRTUAL_PAYMENT_PARAMS');
  }
  return {
    mode: submitResponse.mode,
    signData: submitResponse.signData,
    paySig: submitResponse.paySig,
    signature: submitResponse.signature,
  };
}

export function isWechatVirtualPaymentCancelled(error) {
  const message = String(error?.errMsg || error?.message || '').toLowerCase();
  return message.includes('cancel');
}

const SENSITIVE_DIAGNOSTIC_PATTERN =
  /openid|sign\s*data|signdata|pay\s*sig|paysig|signature|session[_\s-]*key|app[_\s-]*key/i;

function normalizeDiagnosticValue(value, maxLength) {
  if (value === undefined || value === null || value === '') return undefined;
  const text = String(value).replace(/\s+/g, ' ').trim();
  if (!text || SENSITIVE_DIAGNOSTIC_PATTERN.test(text)) return undefined;
  return text.slice(0, maxLength);
}

/**
 * 仅提取允许记录的微信虚拟支付错误信息，禁止把请求、响应或签名对象直接写入日志。
 */
export function buildWechatVirtualPaymentErrorSummary(stage, error = {}) {
  const summary = {
    stage,
  };
  const code = normalizeDiagnosticValue(error?.code ?? error?.errCode, 64);
  const statusCode = normalizeDiagnosticValue(error?.statusCode, 16);
  const message = normalizeDiagnosticValue(error?.errMsg ?? error?.message ?? error?.msg, 160);
  if (code) summary.code = code;
  if (statusCode) summary.statusCode = statusCode;
  if (message) summary.message = message;
  return summary;
}
