export function getResponseData(response, fallbackMessage = '请求失败，请稍后重试') {
  if (!response || response.code !== 0) {
    throw new Error(response?.msg || fallbackMessage);
  }
  return response.data;
}

export function getPageData(response, fallbackMessage) {
  const data = getResponseData(response, fallbackMessage) || {};
  return {
    list: Array.isArray(data.list) ? data.list : [],
    total: Number(data.total) || 0,
  };
}

export function parsePositiveId(value) {
  const id = Number(value);
  return Number.isSafeInteger(id) && id > 0 ? id : 0;
}

export function dedupeBy(list, getKey) {
  const keys = new Set();
  return (list || []).filter((item) => {
    const key = getKey(item);
    if (key == null || keys.has(key)) return false;
    keys.add(key);
    return true;
  });
}

export function parseMessageText(content) {
  if (!content) return '';
  try {
    const parsed = typeof content === 'string' ? JSON.parse(content) : content;
    return typeof parsed?.text === 'string' ? parsed.text : String(content);
  } catch (error) {
    return String(content);
  }
}

export function formatMarriageTime(value) {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value).replace('T', ' ').slice(5, 16);
  const now = new Date();
  const pad = (number) => String(number).padStart(2, '0');
  if (
    date.getFullYear() === now.getFullYear() &&
    date.getMonth() === now.getMonth() &&
    date.getDate() === now.getDate()
  ) {
    return `${pad(date.getHours())}:${pad(date.getMinutes())}`;
  }
  return `${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(
    date.getMinutes(),
  )}`;
}
