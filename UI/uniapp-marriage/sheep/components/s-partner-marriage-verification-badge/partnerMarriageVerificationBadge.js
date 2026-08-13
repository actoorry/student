/**
 * 婚恋认证标志组件运行时常量与规范化 helpers
 */

export const DEFAULT_BADGE_BACKGROUND_COLOR = '#FFF9F0';
export const DEFAULT_ACCENT_COLOR = '#D8A33E';
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
 * 判断婚恋认证状态是否已认证。
 * 只接受严格的数字 1；其他值均视为未认证。
 */
export function isMarriageVerified(profile) {
  return !!(profile && typeof profile === 'object' && profile.marriageVerified === 1);
}
