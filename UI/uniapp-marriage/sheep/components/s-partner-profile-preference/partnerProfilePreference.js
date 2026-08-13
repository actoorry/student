export const DEFAULT_TITLE = '择偶条件';
export const TITLE_MAX_LENGTH = 12;
export const DEFAULT_TITLE_COLOR = '#2D2324';
export const DEFAULT_ACCENT_COLOR = '#C84449';
export const DEFAULT_TAG_TEXT_COLOR = '#8F675D';
export const DEFAULT_TAG_BACKGROUND_COLOR = '#FFF2E7';
export const DEFAULT_TAG_RADIUS = 32;
export const TAG_RADIUS_RANGE = Object.freeze({ min: 0, max: 64 });

/**
 * 规范化区块标题，按 Unicode 字符限制长度。
 * 与 admin 端 normalizeTitle 保持同一契约。
 */
export function normalizeTitle(value) {
  if (typeof value !== 'string') return DEFAULT_TITLE;
  const trimmed = value.trim();
  if (!trimmed) return DEFAULT_TITLE;
  return Array.from(trimmed).slice(0, TITLE_MAX_LENGTH).join('');
}

/**
 * 将任意值规范化为 #RRGGBB 大写颜色，非法值回退到 defaultColor。
 * 与 admin 端 normalizeColor 保持同一契约。
 */
export function normalizeColor(value, defaultColor) {
  if (typeof value !== 'string') return defaultColor;
  const trimmed = value.trim();
  return /^#[0-9A-Fa-f]{6}$/.test(trimmed) ? trimmed.toUpperCase() : defaultColor;
}

/**
 * 将任意值规范化为有效标签圆角（rpx）。
 * 与 admin 端 normalizeTagRadius 保持同一契约。
 */
export function normalizeTagRadius(value) {
  if (value === null || value === '' || typeof value === 'boolean') {
    return DEFAULT_TAG_RADIUS;
  }
  const num = Number(value);
  if (
    !Number.isFinite(num) ||
    num < TAG_RADIUS_RANGE.min ||
    num > TAG_RADIUS_RANGE.max
  ) {
    return DEFAULT_TAG_RADIUS;
  }
  return Math.round(num);
}

/**
 * 规范化择偶条件标签数组。
 * 规则：
 * 1. 仅接受数组，非数组返回空数组
 * 2. 仅保留非空字符串
 * 3. 去除首尾空白
 * 4. 按服务端响应顺序保序去重
 * 5. 不排序、不翻译、不补单位、不合并、不截断
 * 6. 不修改原数组
 */
export function normalizeInterestTags(value) {
  if (!Array.isArray(value)) return [];
  const seen = new Set();
  const result = [];
  for (const item of value) {
    if (typeof item !== 'string') continue;
    const trimmed = item.trim();
    if (!trimmed) continue;
    if (seen.has(trimmed)) continue;
    seen.add(trimmed);
    result.push(trimmed);
  }
  return result;
}

/** 判断是否有有效择偶条件标签 */
export function hasInterestTags(profile) {
  if (!profile || typeof profile !== 'object') return false;
  return normalizeInterestTags(profile.interestTags).length > 0;
}

// 便捷的默认值导出函数，避免调用方记忆每个字段的默认值

export function normalizeTitleColor(value) {
  return normalizeColor(value, DEFAULT_TITLE_COLOR);
}

export function normalizeAccentColor(value) {
  return normalizeColor(value, DEFAULT_ACCENT_COLOR);
}

export function normalizeTagTextColor(value) {
  return normalizeColor(value, DEFAULT_TAG_TEXT_COLOR);
}

export function normalizeTagBackgroundColor(value) {
  return normalizeColor(value, DEFAULT_TAG_BACKGROUND_COLOR);
}
