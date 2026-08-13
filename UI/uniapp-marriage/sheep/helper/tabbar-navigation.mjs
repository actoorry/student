function normalizeTabbarPath(target) {
  const value = typeof target === 'object' && target !== null ? target.url : target;
  if (typeof value !== 'string') return '';
  const path = value.trim().split('?')[0].replace(/\/+$/, '');
  if (!path) return '';
  return path.startsWith('/') ? path : `/${path}`;
}

export function shouldNavigateTabbar(currentTarget, nextTarget) {
  const nextPath = normalizeTabbarPath(nextTarget);
  if (!nextPath) return false;
  return normalizeTabbarPath(currentTarget) !== nextPath;
}
