function toNumber(value, fallback = 0) {
  const number = Number(value);
  return Number.isFinite(number) ? number : fallback;
}

function toNonNegativeNumber(value) {
  return Math.max(0, toNumber(value));
}

function normalizeSignInRecord(record) {
  if (!record || typeof record !== 'object') return null;
  return {
    id: record.id ?? null,
    day: toNonNegativeNumber(record.day),
    point: toNumber(record.point),
    experience: toNumber(record.experience),
    createTime: record.createTime ?? null,
  };
}

export function normalizeSignInResult(record) {
  return normalizeSignInRecord(record);
}

export function emptySignInSummary() {
  return {
    signedToday: false,
    currentDay: 0,
    nextRewardDay: 0,
    nextRewardPoint: 0,
    nextRewardExperience: 0,
    todayRecord: null,
    configs: [],
  };
}

export function normalizeSignInSummary(summary) {
  const source = summary && typeof summary === 'object' ? summary : {};
  const configs = Array.isArray(source.configs)
    ? source.configs.map((config) => ({
        id: config?.id ?? null,
        day: toNonNegativeNumber(config?.day),
        point: toNumber(config?.point),
        experience: toNumber(config?.experience),
        status: toNumber(config?.status),
      }))
    : [];
  return {
    signedToday: source.signedToday === true || Number(source.signedToday) === 1,
    currentDay: toNonNegativeNumber(source.currentDay),
    nextRewardDay: toNonNegativeNumber(source.nextRewardDay),
    nextRewardPoint: toNumber(source.nextRewardPoint),
    nextRewardExperience: toNumber(source.nextRewardExperience),
    todayRecord: normalizeSignInRecord(source.todayRecord),
    configs,
  };
}

export function isActiveMineRequest(requestId, activeRequestId, isLoggedIn) {
  return requestId === activeRequestId && isLoggedIn === true;
}

export function canCreateSignIn({ isLoggedIn, loading, submitting, signedToday, hasConfigs }) {
  return isLoggedIn === true && !loading && !submitting && !signedToday && hasConfigs;
}

export function responseMessage(result, fallback) {
  return result?.msg || fallback;
}
