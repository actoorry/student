// 婚恋动态信息流的纯函数辅助：对象规范化、正整数 ID、1..9 图布局、按 ID 合并详情回传。
// 保持无 DOM、无平台 API、无副作用，便于在 Node 测试中直接验证。

export const MAX_MOMENT_IMAGES = 9;
export const DEFAULT_NICKNAME = '处佳缘用户';

/**
 * 正整数 ID：仅接受可安全表达的 > 0 整数，其余返回 0。
 */
export function positiveId(value) {
  const id = Number(value);
  return Number.isSafeInteger(id) && id > 0 ? id : 0;
}

function countNumber(value) {
  const count = Number(value);
  return Number.isSafeInteger(count) && count > 0 ? count : 0;
}

/**
 * 规范化图片列表：仅保留去空白的有效字符串，去重并截断到 9 张。
 */
export function normalizeImageUrls(value) {
  if (!Array.isArray(value)) return [];
  const urls = [];
  for (const item of value) {
    if (typeof item !== 'string') continue;
    const trimmed = item.trim();
    if (!trimmed || urls.includes(trimmed)) continue;
    urls.push(trimmed);
    if (urls.length >= MAX_MOMENT_IMAGES) break;
  }
  return urls;
}

/**
 * 规范化列表/详情共享的动态对象，输出确定字段。
 * 未知字段原样保留，供组件按需读取；不做深度拷贝之外的裁剪。
 */
export function normalizeMomentItem(item) {
  const source = item && typeof item === 'object' ? item : {};
  return {
    ...source,
    id: positiveId(source.id),
    partnerId: positiveId(source.partnerId),
    nickname:
      typeof source.nickname === 'string' && source.nickname.trim()
        ? source.nickname.trim()
        : DEFAULT_NICKNAME,
    avatar: typeof source.avatar === 'string' ? source.avatar.trim() : '',
    verifiedLabel:
      typeof source.verifiedLabel === 'string' && source.verifiedLabel.trim()
        ? source.verifiedLabel.trim()
        : '',
    content: typeof source.content === 'string' ? source.content : '',
    imageUrls: normalizeImageUrls(source.imageUrls),
    liked: !!source.liked,
    likeCount: countNumber(source.likeCount),
    commentCount: countNumber(source.commentCount),
    shareCount: countNumber(source.shareCount),
    followed: !!source.followed,
    mine: !!source.mine,
    publishTime: source.publishTime || '',
  };
}

/**
 * 1..9 张有效图片的确定布局：
 * 1 张为受边界约束的大图；2..4 张为两列；5..9 张为三列宫格。
 * 返回布局类别与样式类名，列表与详情共用同一规则。
 */
export function momentLayout(imageCount) {
  const count = Math.min(Math.max(Number(imageCount) || 0, 0), MAX_MOMENT_IMAGES);
  if (count === 1) return { columns: 1, className: 'moment-images--single' };
  if (count >= 2 && count <= 4) return { columns: 2, className: 'moment-images--pair' };
  if (count >= 5) return { columns: 3, className: 'moment-images--grid' };
  return { columns: 0, className: 'moment-images--empty' };
}

/**
 * 按正整数动态 ID 合并详情回传，只覆盖展示字段（liked/likeCount/commentCount），
 * 不回填作者/正文/媒体/权限字段。change.removed 为 true 时按 ID 移除该动态。
 * 目标不存在或没有变化时返回原数组引用，调用方据此决定是否触发重渲染。
 */
export function mergeMomentChange(list, change = {}) {
  const id = positiveId(change?.id);
  if (!id || !Array.isArray(list)) return list;
  let changed = false;
  const next = list.map((item) => {
    if (positiveId(item?.id) !== id) return item;
    if (change.removed === true) {
      changed = true;
      return null;
    }
    let touched = false;
    const merged = { ...item };
    if (typeof change.liked === 'boolean' && merged.liked !== change.liked) {
      merged.liked = change.liked;
      touched = true;
    }
    if (change.likeCount != null && countNumber(merged.likeCount) !== countNumber(change.likeCount)) {
      merged.likeCount = countNumber(change.likeCount);
      touched = true;
    }
    if (
      change.commentCount != null &&
      countNumber(merged.commentCount) !== countNumber(change.commentCount)
    ) {
      merged.commentCount = countNumber(change.commentCount);
      touched = true;
    }
    if (touched) changed = true;
    return touched ? merged : item;
  });
  if (!changed) return list;
  return next.filter(Boolean);
}

/**
 * 校验详情路由 ID 与 EventChannel 快照的一致性，返回可展示的规范化快照。
 * 路由 ID 无效、快照缺失或 ID 不一致时返回 null（详情页展示稳定错误态）。
 */
export function validateMomentSnapshot(routeId, snapshot) {
  const id = positiveId(routeId);
  if (!id) return null;
  const item = normalizeMomentItem(snapshot);
  return positiveId(item.id) === id ? item : null;
}

/**
 * 格式化展示计数：超过 999 时以 k 表示，其余原样。
 */
export function formatMomentCount(value) {
  const count = countNumber(value);
  return count > 999 ? `${(count / 1000).toFixed(1)}k` : String(count);
}

/**
 * 头像占位字：取昵称首字符，缺省用“缘”。
 */
export function avatarText(name) {
  return typeof name === 'string' && name.trim() ? name.trim().slice(0, 1) : '缘';
}
