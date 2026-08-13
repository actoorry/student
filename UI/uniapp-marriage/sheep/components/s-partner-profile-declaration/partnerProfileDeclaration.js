export const DEFAULT_TITLE = '择偶宣言';
export const TITLE_MAX_LENGTH = 12;
export const DEFAULT_TITLE_COLOR = '#8B7670';
export const DEFAULT_CONTENT_COLOR = '#5E4A46';
export const DEFAULT_CONTENT_BACKGROUND_COLOR = '#FFF9F6';
export const DEFAULT_CONTENT_RADIUS = 40;
export const CONTENT_RADIUS_RANGE = Object.freeze({ min: 0, max: 80 });
export const EMPTY_DECLARATION_TEXT = 'TA 还没有填写择偶宣言';

export function normalizeTitle(value) {
  if (typeof value !== 'string') return DEFAULT_TITLE;
  const trimmed = value.trim();
  if (!trimmed) return DEFAULT_TITLE;
  return Array.from(trimmed).slice(0, TITLE_MAX_LENGTH).join('');
}

export function normalizeColor(value, defaultColor) {
  if (typeof value !== 'string') return defaultColor;
  const trimmed = value.trim();
  return /^#[0-9A-Fa-f]{6}$/.test(trimmed) ? trimmed.toUpperCase() : defaultColor;
}

export function normalizeContentRadius(value) {
  if (value === null || value === '' || typeof value === 'boolean') {
    return DEFAULT_CONTENT_RADIUS;
  }
  const num = Number(value);
  if (
    !Number.isFinite(num) ||
    num < CONTENT_RADIUS_RANGE.min ||
    num > CONTENT_RADIUS_RANGE.max
  ) {
    return DEFAULT_CONTENT_RADIUS;
  }
  return Math.round(num);
}

export function getDeclarationBio(profile) {
  if (!profile || typeof profile.bio !== 'string') return '';
  return profile.bio.trim();
}

export function hasDeclarationBio(profile) {
  return getDeclarationBio(profile).length > 0;
}

export function formatDeclarationBio(profile) {
  return getDeclarationBio(profile) || EMPTY_DECLARATION_TEXT;
}

export function normalizeTitleColor(value) {
  return normalizeColor(value, DEFAULT_TITLE_COLOR);
}

export function normalizeContentColor(value) {
  return normalizeColor(value, DEFAULT_CONTENT_COLOR);
}

export function normalizeContentBackgroundColor(value) {
  return normalizeColor(value, DEFAULT_CONTENT_BACKGROUND_COLOR);
}
