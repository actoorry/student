/**
 * 人物基本资料字段契约。
 * 顺序、标签、人物字段、开关键与单位均由已编译代码持有，不能由 DIY JSON 改写。
 */
export const BASIC_INFO_FIELD_DEFINITIONS = Object.freeze([
  Object.freeze({ toggleKey: 'showCity', profileKey: 'city', label: '现居地区', type: 'text' }),
  Object.freeze({ toggleKey: 'showJob', profileKey: 'job', label: '工作', type: 'text' }),
  Object.freeze({
    toggleKey: 'showHeight',
    profileKey: 'height',
    label: '身高',
    type: 'measurement',
    unit: 'cm',
  }),
  Object.freeze({
    toggleKey: 'showWeight',
    profileKey: 'weight',
    label: '体重',
    type: 'measurement',
    unit: 'kg',
  }),
  Object.freeze({ toggleKey: 'showIncome', profileKey: 'income', label: '月收入', type: 'text' }),
  Object.freeze({
    toggleKey: 'showEducation',
    profileKey: 'education',
    label: '学历',
    type: 'text',
  }),
  Object.freeze({
    toggleKey: 'showHouseStatus',
    profileKey: 'houseStatus',
    label: '住房',
    type: 'text',
  }),
  Object.freeze({
    toggleKey: 'showCarStatus',
    profileKey: 'carStatus',
    label: '车辆',
    type: 'text',
  }),
]);

/** 只有严格布尔值 false 关闭；缺失和错误类型都回退为默认开启 */
export function isBasicInfoFieldVisible(value) {
  return value !== false;
}

export function normalizeBasicInfoText(value) {
  if (typeof value !== 'string') return '';
  return value.trim();
}

export function formatBasicInfoMeasurement(value, unit) {
  if (typeof value !== 'number' && typeof value !== 'string') return '';
  if (typeof value === 'string' && value.trim() === '') return '';

  const number = Number(value);
  if (!Number.isFinite(number) || number <= 0) return '';

  const rounded = Math.round(number);
  if (rounded <= 0) return '';
  return `${rounded}${unit}`;
}

function formatBasicInfoValue(profile, field) {
  const value = profile[field.profileKey];
  if (field.type === 'measurement') {
    return formatBasicInfoMeasurement(value, field.unit);
  }
  return normalizeBasicInfoText(value);
}

/** 按代码固定顺序构造当前人物可展示资料；关闭项和无效项不生成空卡片 */
export function buildPartnerProfileBasicInfoItems(profile, property) {
  if (!profile || typeof profile !== 'object') return [];
  const config = property && typeof property === 'object' ? property : {};

  return BASIC_INFO_FIELD_DEFINITIONS.reduce((items, field) => {
    if (!isBasicInfoFieldVisible(config[field.toggleKey])) return items;
    const value = formatBasicInfoValue(profile, field);
    if (!value) return items;
    items.push({ key: field.profileKey, label: field.label, value });
    return items;
  }, []);
}
