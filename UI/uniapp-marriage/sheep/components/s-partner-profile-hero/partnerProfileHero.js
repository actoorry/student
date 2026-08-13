/**
 * 人物主图组件运行时常量与规范化 helpers
 *
 * 这些函数是纯函数，不依赖 Vue 上下文或业务请求，便于单元测试。
 */

export const DEFAULT_HEIGHT = 1040;
export const MIN_HEIGHT = 640;
export const MAX_HEIGHT = 1400;

export const DEFAULT_AVATAR_SIZE = 92;
export const MIN_AVATAR_SIZE = 64;
export const MAX_AVATAR_SIZE = 160;

export const DEFAULT_NAME_COLOR = '#201917';
export const DEFAULT_AGE_COLOR = '#6F5A55';
export const DEFAULT_AVATAR_URL = '/static/img/shop/default_avatar.png';

export const EMPTY_NAME_PLACEHOLDER = '未命名用户';

export function isValidImageUrl(url) {
  return typeof url === 'string' && url.trim().length > 0;
}

export function normalizeHeight(value) {
  const num = Number(value);
  if (!Number.isFinite(num) || num < MIN_HEIGHT || num > MAX_HEIGHT) {
    return DEFAULT_HEIGHT;
  }
  return Math.round(num);
}

export function normalizeAvatarSize(value) {
  const num = Number(value);
  if (!Number.isFinite(num) || num < MIN_AVATAR_SIZE || num > MAX_AVATAR_SIZE) {
    return DEFAULT_AVATAR_SIZE;
  }
  return Math.round(num);
}

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

export function normalizeNameColor(value) {
  return normalizeColor(value, DEFAULT_NAME_COLOR);
}

export function normalizeAgeColor(value) {
  return normalizeColor(value, DEFAULT_AGE_COLOR);
}

export function formatDisplayName(profile) {
  if (!profile || typeof profile !== 'object') return EMPTY_NAME_PLACEHOLDER;
  const name = profile.name;
  if (typeof name !== 'string') return EMPTY_NAME_PLACEHOLDER;
  const trimmed = name.trim();
  return trimmed || EMPTY_NAME_PLACEHOLDER;
}

export function formatDisplayAge(profile) {
  if (!profile || typeof profile !== 'object') return '';
  const age = Number(profile.age);
  if (!Number.isInteger(age) || age < 1 || age > 150) return '';
  return String(age);
}

export function buildMainCandidates(profile) {
  const candidates = [];
  if (!profile || typeof profile !== 'object') return candidates;

  if (isValidImageUrl(profile.mainImage)) {
    candidates.push(profile.mainImage.trim());
  }
  if (Array.isArray(profile.albumImages)) {
    profile.albumImages.forEach((url) => {
      if (isValidImageUrl(url)) {
        const trimmed = url.trim();
        if (!candidates.includes(trimmed)) {
          candidates.push(trimmed);
        }
      }
    });
  }
  if (isValidImageUrl(profile.avatarImage)) {
    const trimmed = profile.avatarImage.trim();
    if (!candidates.includes(trimmed)) {
      candidates.push(trimmed);
    }
  }
  return candidates;
}

export function buildAvatarCandidates(profile) {
  const list = [];
  if (!profile || typeof profile !== 'object') return list;
  if (isValidImageUrl(profile.avatarImage)) {
    list.push(profile.avatarImage.trim());
  }
  return list;
}

export function resolveAvatarUrl(profile, avatarFailed) {
  if (avatarFailed) return DEFAULT_AVATAR_URL;
  const candidates = buildAvatarCandidates(profile);
  if (candidates.length === 0) return DEFAULT_AVATAR_URL;
  return candidates[0];
}

export function resolveMainUrl(profile, candidateIndex) {
  const candidates = buildMainCandidates(profile);
  if (candidates.length === 0) return '';
  const index = Math.min(Number(candidateIndex) || 0, candidates.length - 1);
  return candidates[index];
}
