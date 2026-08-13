/**
 * 实名认证标志组件运行时常量与规范化 helpers
 *
 * 这些函数是纯函数，不依赖 Vue 上下文或业务请求，便于单元测试。
 */

export const DEFAULT_BADGE_BACKGROUND_COLOR = '#FFF5EF';
export const DEFAULT_ACCENT_COLOR = '#C84449';
export const DEFAULT_TITLE_COLOR = '#4F3433';
export const DEFAULT_DESCRIPTION_COLOR = '#776361';

export function normalizeColor(value, defaultColor) {
  if (typeof value !== 'string') {
    return defaultColor;
  }
  const trimmed = value.trim();
  if (/^#[0-9A-Fa-f]{6}$/.test(trimmed)) {
    return trimmed;
  }
  return defaultColor;
}

export function normalizeBadgeBackgroundColor(value) {
  return normalizeColor(value, DEFAULT_BADGE_BACKGROUND_COLOR);
}

export function normalizeAccentColor(value) {
  return normalizeColor(value, DEFAULT_ACCENT_COLOR);
}

export function normalizeTitleColor(value) {
  return normalizeColor(value, DEFAULT_TITLE_COLOR);
}

export function normalizeDescriptionColor(value) {
  return normalizeColor(value, DEFAULT_DESCRIPTION_COLOR);
}

/**
 * 从脱敏身份证号中安全提取前四位数字。
 * 仅当去空白后开头为四位 ASCII 数字时返回该四位字符串，否则返回空字符串。
 */
export function extractIdCardPrefix(maskedIdCard) {
  if (typeof maskedIdCard !== 'string') {
    return '';
  }
  const trimmed = maskedIdCard.trim();
  if (trimmed.length < 4) {
    return '';
  }
  const prefix = trimmed.substring(0, 4);
  if (/^\d{4}$/.test(prefix)) {
    return prefix;
  }
  return '';
}

/**
 * 判断实名认证状态是否已认证。
 * 只接受严格的数字 1；其他值（字符串、布尔、null、其他数字）均视为未认证。
 */
export function isRealVerified(profile) {
  return !!(profile && typeof profile === 'object' && profile.realVerified === 1);
}

/**
 * 构建实名认证说明文案。
 * 能提取到四位数字前缀时返回“身份证前四位：XXXX”，否则回退“身份已核验”。
 */
export function buildRealNameDescription(profile) {
  const prefix = extractIdCardPrefix(profile?.maskedIdCard);
  if (prefix) {
    return `身份证前四位：${prefix}`;
  }
  return '身份已核验';
}
