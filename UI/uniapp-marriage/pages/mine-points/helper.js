function toNumber(value, fallback = 0) {
  const number = Number(value);
  return Number.isFinite(number) ? number : fallback;
}

function toNonNegativeNumber(value) {
  return Math.max(0, toNumber(value));
}

function normalizePointRecord(record) {
  if (!record || typeof record !== 'object') return null;
  return {
    id: record.id ?? null,
    title: String(record.title || ''),
    description: String(record.description || ''),
    point: toNumber(record.point),
    totalPoint: toNumber(record.totalPoint),
    createTime: record.createTime ?? null,
  };
}

export function normalizePointPage(payload) {
  const source = payload && typeof payload === 'object' ? payload : {};
  const records = source.records && typeof source.records === 'object' ? source.records : {};
  const list = Array.isArray(records.list)
    ? records.list.map(normalizePointRecord).filter(Boolean)
    : [];
  return {
    totalPoint: toNumber(source.totalPoint),
    records: {
      list,
      total: toNonNegativeNumber(records.total),
    },
  };
}

export function appendUniquePointRecords(currentRecords, incomingRecords) {
  const merged = Array.isArray(currentRecords) ? [...currentRecords] : [];
  const existingIds = new Set(merged.map((record) => String(record?.id)));
  (Array.isArray(incomingRecords) ? incomingRecords : []).forEach((record) => {
    if (!record || record.id === null || record.id === undefined) return;
    const id = String(record.id);
    if (!existingIds.has(id)) {
      existingIds.add(id);
      merged.push(record);
    }
  });
  return merged;
}

export function isActiveMineRequest(requestId, activeRequestId, isLoggedIn) {
  return requestId === activeRequestId && isLoggedIn === true;
}

export function canLoadMorePointRecords({ isLoggedIn, loading, hasMore }) {
  return isLoggedIn === true && !loading && hasMore;
}

export function nextPointPage({ confirmedPage, failedPage }) {
  const failed = toNonNegativeNumber(failedPage);
  return failed || toNonNegativeNumber(confirmedPage) + 1;
}

export function responseMessage(result, fallback) {
  return result?.msg || fallback;
}
