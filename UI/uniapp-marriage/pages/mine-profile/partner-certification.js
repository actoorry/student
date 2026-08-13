const CHINESE_NAME = /^[\u4e00-\u9fa5]{1,20}$/;
const ID_CARD = /^\d{17}[\dXx]$/;

export function isVerified(value) {
  return Number(value) === 1;
}

export function normalizeIdCard(value) {
  return String(value || '').trim().toUpperCase();
}

export function validateRealName(name, idCard) {
  const normalizedName = String(name || '').trim();
  const normalizedIdCard = normalizeIdCard(idCard);
  if (!CHINESE_NAME.test(normalizedName)) {
    return { valid: false, message: '请输入 1–20 个中文字符的真实姓名' };
  }
  if (!ID_CARD.test(normalizedIdCard)) {
    return { valid: false, message: '请输入 18 位身份证号' };
  }
  return { valid: true, name: normalizedName, idCard: normalizedIdCard };
}

export function clearSensitiveForm(form) {
  form.name = '';
  form.idCard = '';
}
