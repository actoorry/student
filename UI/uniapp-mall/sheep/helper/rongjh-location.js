import AreaApi from '@/sheep/api/system/area';

const STORAGE_KEY = 'rongjh_local_location';

/** 默认城市：合肥市 */
const DEFAULT_LOCATION = {
	cityId: 340100,
	cityName: '合肥市',
	provinceId: 340000,
	provinceName: '安徽省',
	address: '安徽省 合肥市',
	name: '合肥市',
};

/** 读取缓存位置 */
export function getStoredLocation() {
	try {
		const data = uni.getStorageSync(STORAGE_KEY);
		return data && typeof data === 'object' ? data : null;
	} catch (e) {
		return null;
	}
}

/** 写入缓存位置 */
export function setStoredLocation(location) {
	if (!location) return;
	uni.setStorageSync(STORAGE_KEY, location);
}

export function getDefaultLocation() {
	return { ...DEFAULT_LOCATION };
}

/** 将接口/选择结果规范为统一结构 */
export function normalizeCityLocation(data = {}) {
	const cityId = Number(data.cityId || data.id || 0);
	if (!cityId) return null;
	const cityName = data.cityName || data.name || '';
	const provinceId = Number(data.provinceId || 0) || undefined;
	const provinceName = data.provinceName || '';
	const address = data.address || [provinceName, cityName].filter(Boolean).join(' ');
	return {
		cityId,
		cityName,
		provinceId,
		provinceName,
		address,
		name: cityName || address,
	};
}

/**
 * 初始化城市：缓存 → IP 定位 → 微信定位（仅更新缓存供下次）→ 默认合肥
 */
export async function resolveCityLocation() {
	const cached = getStoredLocation();
	if (cached?.cityId) {
		return cached;
	}

	try {
		const res = await AreaApi.getAreaByIp();
		const location = normalizeCityLocation(res?.data || {});
		if (location?.cityId) {
			setStoredLocation(location);
			return location;
		}
	} catch (err) {
		console.warn('IP 定位失败', err);
	}

	try {
		const wxLocation = await detectLocationByWeixin();
		if (wxLocation?.cityId) {
			setStoredLocation(wxLocation);
			return wxLocation;
		}
	} catch (err) {
		console.warn('微信定位失败', err);
	}

	return getDefaultLocation();
}

/** 微信定位：获取经纬度后尝试 IP 接口（小程序请求会带客户端 IP） */
function detectLocationByWeixin() {
	return new Promise((resolve, reject) => {
		uni.getLocation({
			type: 'gcj02',
			success: async () => {
				try {
					const res = await AreaApi.getAreaByIp();
					resolve(normalizeCityLocation(res?.data || {}));
				} catch (err) {
					reject(err);
				}
			},
			fail: reject,
		});
	});
}

/** 加载省市区树（用于手动选城） */
export async function fetchAreaTree() {
	const res = await AreaApi.getAreaTree();
	const list = Array.isArray(res?.data) ? res.data : [];
	return list.filter((item = {}) => {
		const name = String(item.name || '');
		return !/香港|澳门|台湾/.test(name);
	});
}

/**
 * 由省节点 + 市节点构造统一位置对象（纯函数）
 * @param {Object} province 区域树省节点（含 id、name、children）
 * @param {Object} city 区域树市节点（含 id、name）
 * @returns {Object|null} 规范位置对象；缺少市节点时返回 null
 */
export function buildCityLocation(province = {}, city = {}) {
	if (!province || !city || !city.id) return null;
	return normalizeCityLocation({
		id: city.id,
		name: city.name,
		provinceId: province.id,
		provinceName: province.name,
		address: `${province.name} ${city.name}`,
	});
}

// 兼容旧引用
export const resolveInitialLocation = resolveCityLocation;
