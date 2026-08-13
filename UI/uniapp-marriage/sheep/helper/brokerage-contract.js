/** 共享的婚恋分销边界规则；不持有用户、提现或模板运行时状态。 */
export function toPositiveInteger(value) {
  const number = typeof value === 'string' && value.trim() === '' ? NaN : Number(value);
  return Number.isSafeInteger(number) && number > 0 ? number : 0;
}

export function buildSalesQuery(params = {}) {
  const entries = [];
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return;
    const values = Array.isArray(value) ? value : [value];
    values.forEach((item) => {
      if (item === undefined || item === null || item === '') return;
      entries.push(`${encodeURIComponent(key)}=${encodeURIComponent(item)}`);
    });
  });
  return entries.join('&');
}

export function normalizeWithdrawTypes(value) {
  const values = Array.isArray(value) ? value : String(value || '').split(/[,|]/);
  return values
    .map((item) => Number(item))
    .filter((item) => Number.isSafeInteger(item) && item > 0);
}

export function validateBrokerageWithdraw({
  priceYuan,
  withdrawablePrice = 0,
  minPrice = 0,
  type,
  withdrawTypes = [],
  accountInfo = {},
} = {}) {
  const price = Number(priceYuan);
  const priceFen = Number.isFinite(price) ? Math.round(price * 100) : 0;
  if (!Number.isFinite(price) || price <= 0) {
    return { valid: false, message: '请输入正确的提现金额' };
  }
  if (priceFen < Number(minPrice || 0)) {
    return { valid: false, message: '提现金额低于最低提现金额' };
  }
  if (priceFen > Number(withdrawablePrice || 0)) {
    return { valid: false, message: '提现金额超过可提现佣金' };
  }

  const selectedType = Number(type);
  if (!normalizeWithdrawTypes(withdrawTypes).includes(selectedType)) {
    return { valid: false, message: '请选择已启用的提现方式' };
  }

  if ([2, 6].includes(selectedType) && !String(accountInfo.userAccount || '').trim()) {
    return { valid: false, message: '请输入提现账号' };
  }
  if ([2, 5, 6].includes(selectedType) && !String(accountInfo.userName || '').trim()) {
    return { valid: false, message: '请输入收款真名' };
  }
  if ([3, 4].includes(selectedType) && !String(accountInfo.qrCodeUrl || '').trim()) {
    return { valid: false, message: '请上传收款码' };
  }
  if (selectedType === 2 && !String(accountInfo.bankName || '').trim()) {
    return { valid: false, message: '请选择提现银行' };
  }
  if (selectedType === 2 && !String(accountInfo.bankAddress || '').trim()) {
    return { valid: false, message: '请输入开户地址' };
  }
  return { valid: true, message: '' };
}

export function shouldClearPendingShareId(result) {
  return result?.code === 0 && result?.data === true;
}

export function resolveBrokerageAccess(brokerageUser, config) {
  if (config && config.brokerageEnabled === false) {
    return {
      available: false,
      canApply: false,
      title: '分销功能暂未开放',
      message: '请稍后再来查看分销服务',
      actionLabel: '',
    };
  }
  if (!brokerageUser?.brokerageUserExists) {
    return {
      available: false,
      canApply: config?.brokerageEnabled !== false,
      title: '没有申请资格',
      message: '当前账号还没有申请个人分销资格',
      actionLabel: '申请开通分销',
    };
  }
  if (!brokerageUser.brokerageEnabled) {
    return {
      available: false,
      canApply: false,
      title: '分销资格审核中',
      message: '你的分销申请已提交，正在审核中，请耐心等待',
      actionLabel: '',
    };
  }
  return { available: true, canApply: false, title: '', message: '', actionLabel: '' };
}

function toPriceInteger(value) {
  const price = Number(value);
  return Number.isFinite(price) ? Math.round(price) : 0;
}

/** 明确区分可用佣金与累计已提现，避免页面把 withdrawPrice 当作当前余额。 */
export function resolveBrokerageWalletSummary(summary = {}) {
  const currentPrice = toPriceInteger(summary?.brokeragePrice);
  return {
    currentPrice,
    withdrawablePrice: currentPrice,
    frozenPrice: toPriceInteger(summary?.frozenPrice),
    withdrawnPrice: toPriceInteger(summary?.withdrawPrice),
  };
}

/** 分销明细默认加载全部历史；仅在用户选择完整日期范围后追加时间条件。 */
export function buildBrokerageListParams({ pageNo = 1, pageSize = 8, dateRange = [] } = {}) {
  const params = { pageNo, pageSize };
  if (
    Array.isArray(dateRange) &&
    dateRange.length >= 2 &&
    String(dateRange[0] || '').trim() &&
    String(dateRange[1] || '').trim()
  ) {
    params['createTime[0]'] = `${dateRange[0]} 00:00:00`;
    params['createTime[1]'] = `${dateRange[1]} 23:59:59`;
  }
  return params;
}
