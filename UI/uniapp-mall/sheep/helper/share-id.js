/**
 * 分享推广人 ID 规范化工具（纯函数，无平台依赖，可被单元测试直接加载）
 *
 * 商城分享归因只接受大于零的整数用户 ID；无效、缺失或当前用户自身的值
 * 不得保存待绑定上下文，也不得发起绑定请求。
 */

/**
 * 规范化推广人：只接受大于零的整数用户 ID，返回 Number 或 undefined
 *
 * @param {*} val 推广人值（数字或字符串均可）
 * @returns {number|undefined} 有效正整数；零、负数、小数、非数字、空值返回 undefined
 */
export const normalizeShareId = (val) => {
  if (typeof val === 'undefined' || val === null || val === '') {
    return undefined;
  }
  const text = String(val).trim();
  if (!/^\d+$/.test(text)) {
    return undefined; // 负数、小数、非数字、空串均无效
  }
  const shareId = Number(text);
  return shareId > 0 ? shareId : undefined; // 零无效
};

/**
 * 判断推广人是否为当前用户自身（自绑定不发起请求）
 *
 * @param {number|string} shareId 规范化后的推广人编号
 * @param {number|string} currentUserId 当前登录用户编号
 * @returns {boolean} 相同返回 true
 */
export const isSelfShareId = (shareId, currentUserId) => {
  if (typeof currentUserId === 'undefined' || currentUserId === null || currentUserId === '') {
    return false;
  }
  return Number(shareId) === Number(currentUserId);
};
