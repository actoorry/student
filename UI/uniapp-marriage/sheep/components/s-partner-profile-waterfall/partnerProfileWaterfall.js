export const EMPTY_NAME = '未命名用户';

export function isStrictActive(value) {
  return value === 1;
}

export function displayName(profile) {
  return typeof profile?.name === 'string' && profile.name.trim()
    ? profile.name.trim()
    : EMPTY_NAME;
}

export function displayAge(profile) {
  const age = Number(profile?.age);
  return Number.isInteger(age) && age >= 1 && age <= 150 ? String(age) : '';
}

export function displaySex(profile) {
  return Number(profile?.sex) === 1 ? '男' : Number(profile?.sex) === 2 ? '女' : '';
}

export function displayText(profile, key) {
  return typeof profile?.[key] === 'string' ? profile[key].trim() : '';
}

export function imageCandidates(profile) {
  return [
    ...new Set(
      [
        profile?.mainImage,
        ...(Array.isArray(profile?.albumImages) ? profile.albumImages : []),
        profile?.avatarImage,
      ]
        .filter((item) => typeof item === 'string' && item.trim())
        .map((item) => item.trim()),
    ),
  ];
}

export function visibleBadges(profile, property) {
  const badges = property?.badges || {};
  return [
    badges.member?.show !== false && isStrictActive(profile?.memberActive) ? '会员' : '',
    badges.realName?.show !== false && isStrictActive(profile?.realVerified) ? '实名认证' : '',
    badges.marriage?.show !== false && isStrictActive(profile?.marriageVerified) ? '婚姻认证' : '',
  ].filter(Boolean);
}

/**
 * 只要启用展示的资料字段中存在未填写项，就保留一条统一占位提示。
 * 姓名有代码默认值，不参与资料完整度判断。
 */
export function hasMissingProfileDetails(profile, property) {
  const fields = property?.fields || {};
  const enabledValues = [
    fields.age?.show !== false ? displayAge(profile) : null,
    fields.sex?.show !== false ? displaySex(profile) : null,
    fields.job?.show !== false ? displayText(profile, 'job') : null,
    fields.city?.show !== false ? displayText(profile, 'city') : null,
  ].filter((value) => value !== null);
  return enabledValues.length > 0 && enabledValues.some((value) => !value);
}
