const PENDING_BIND_USER_ID_KEY = 'marriage-pending-bind-user-id';

function positiveInteger(value) {
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : 0;
}

function text(value) {
  return typeof value === 'string' ? value.trim() : '';
}

export function resolveMemberPronoun(sex) {
  return Number(sex) === 1 ? '他' : '她';
}

export function buildMemberShareTitle(profile = {}) {
  const parts = [];
  const age = positiveInteger(profile.age);
  if (age) parts.push(`${age}岁`);
  if (text(profile.city)) parts.push(text(profile.city));
  if (text(profile.job)) parts.push(text(profile.job));
  const prefix = parts.length ? `${parts.join(' · ')}｜` : '';
  return `${prefix}来看看${resolveMemberPronoun(profile.sex)}的个人主页`;
}

export function resolveMemberShareImage(profile = {}) {
  if (text(profile.mainImage)) return text(profile.mainImage);
  const albumImage = Array.isArray(profile.albumImages)
    ? profile.albumImages.find((item) => text(item))
    : '';
  return text(albumImage) || text(profile.avatarImage);
}

export function buildMemberShareQuery(bindUserId, memberId) {
  return `id=${positiveInteger(memberId)}&bindUserId=${positiveInteger(bindUserId)}`;
}

export function buildMemberSharePath(bindUserId, memberId) {
  return `/pages/member-detail/index?${buildMemberShareQuery(bindUserId, memberId)}`;
}

export function buildMemberShareInfo(profile, bindUserId = 0) {
  const memberId = positiveInteger(profile?.id);
  if (!memberId) return null;
  const title = buildMemberShareTitle(profile);
  const image = resolveMemberShareImage(profile);
  return {
    title,
    desc: text(profile?.bio) || title,
    image,
    query: buildMemberShareQuery(bindUserId, memberId),
    forward: { path: buildMemberSharePath(bindUserId, memberId) },
    poster: { type: 'user' },
  };
}

export function capturePendingBindUserId(options, isLogin = false) {
  if (isLogin) {
    clearPendingBindUserId();
    return 0;
  }
  const bindUserId = positiveInteger(options?.bindUserId);
  if (bindUserId) uni.setStorageSync(PENDING_BIND_USER_ID_KEY, bindUserId);
  return bindUserId;
}

export function getPendingBindUserId() {
  return positiveInteger(uni.getStorageSync(PENDING_BIND_USER_ID_KEY));
}

export function clearPendingBindUserId() {
  uni.removeStorageSync(PENDING_BIND_USER_ID_KEY);
}
