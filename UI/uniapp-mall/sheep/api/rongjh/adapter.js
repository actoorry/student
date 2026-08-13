/**
 * 戎集汇 API 适配层：Mock 返回 Odoo 字段形状；联调时请求芋道 API 并 normalize
 */
import { RONGJH_USE_MOCK } from '@/sheep/config/rongjh';
import { baseUrl } from '@/sheep/config';
import $url from '@/sheep/url';
import request from '@/sheep/request';
import AreaApi from '@/sheep/api/system/area';
import SpuApi from '@/sheep/api/product/spu';
import UserApi from '@/sheep/api/partner/user';
import WarriorApi from '@/sheep/api/rongjh/warrior';
import HelpApi from '@/sheep/api/rongjh/help';
import mockStates from '@/sheep/mock/rongjh/states';
import warriorStatusMock from '@/sheep/mock/rongjh/warrior-status';
import articlesWarriorMock from '@/sheep/mock/rongjh/articles-warrior';
import foundationRecordsMock from '@/sheep/mock/rongjh/foundation-records';
import noticesMock from '@/sheep/mock/rongjh/notices';
import zoneProductsMock from '@/sheep/mock/rongjh/zone-products';
import aboutMock from '@/sheep/mock/rongjh/about';
import protocolMock from '@/sheep/mock/rongjh/protocol';
import contactServiceMock from '@/sheep/mock/rongjh/contact-service';
import localProductsMock from '@/sheep/mock/rongjh/local-products';
import zoneSpecialtyMock from '@/sheep/mock/rongjh/zone-specialty';

const ARTICLE_CATEGORY = {
	WARRIOR: 4,
	FOUNDATION: 5,
	NOTICE: 6,
};

const mockOk = (data) => Promise.resolve({ code: 200, data });

const apiOk = (data, extra = {}) => Promise.resolve({ code: 200, data, ...extra });

const padZero = (n) => String(n).padStart(2, '0');

/**
 * 统一解析时间为 Date 对象，兼容：
 * - 毫秒/秒级时间戳（芋道 LocalDateTime 默认序列化为毫秒数字）
 * - "YYYY-MM-DD HH:mm:ss" / ISO 字符串（iOS 需将 - 替换为 /）
 */
const toDate = (value) => {
	if (value == null || value === '') return null;
	if (typeof value === 'number' || /^\d+$/.test(String(value).trim())) {
		let ts = Number(value);
		if (String(Math.trunc(ts)).length <= 10) ts *= 1000; // 秒级 -> 毫秒级
		const d = new Date(ts);
		return Number.isNaN(d.getTime()) ? null : d;
	}
	const d = new Date(String(value).replace(/-/g, '/').replace('T', ' '));
	return Number.isNaN(d.getTime()) ? null : d;
};

const formatDateTime = (value) => {
	const d = toDate(value);
	if (!d) return '';
	return `${d.getFullYear()}-${padZero(d.getMonth() + 1)}-${padZero(d.getDate())} ${padZero(d.getHours())}:${padZero(d.getMinutes())}:${padZero(d.getSeconds())}`;
};

/**
 * 将时间格式化为「刚刚 / x分钟前 / x小时前 / x天前」等相对时间，
 * 超过 30 天则回退为日期（今年省略年份）
 */
export const formatRelativeTime = (value) => {
	const date = toDate(value);
	if (!date) return '';
	const diff = Math.floor((Date.now() - date.getTime()) / 1000);
	if (diff < 0) return formatDateTime(value).slice(0, 10);
	if (diff < 60) return '刚刚';
	if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`;
	if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`;
	if (diff < 2592000) return `${Math.floor(diff / 86400)}天前`;
	const m = padZero(date.getMonth() + 1);
	const d = padZero(date.getDate());
	return date.getFullYear() === new Date().getFullYear()
		? `${m}-${d}`
		: `${date.getFullYear()}-${m}-${d}`;
};

const paginate = (list, page = 1, limit = 10) => {
	const p = Number(page) || 1;
	const size = Number(limit) || 10;
	const start = (p - 1) * size;
	const slice = list.slice(start, start + size);
	return {
		list: slice,
		pagination: {
			total_pages: Math.max(1, Math.ceil(list.length / size)),
			page: p,
			limit: size,
		},
	};
};

const toPageParams = (params = {}) => ({
	pageNo: Number(params.page) || 1,
	pageSize: Number(params.limit) || 10,
});

const normalizeArticle = (item = {}) => ({
	id: item.id,
	name: item.title || item.name || '',
	summary: item.introduction || item.summary || '',
	content: item.content || '',
	cover_url: item.picUrl || item.cover_url || '',
	publish_date: formatDateTime(item.createTime || item.publish_date),
	view_count: item.browseCount ?? item.view_count ?? 0,
	like_count: item.likeCount ?? item.like_count ?? 0,
	is_top: item.recommendHot ?? item.is_top ?? false,
	state_id: item.stateId ?? item.state_id ?? 0,
	state_name: item.stateName || item.state_name || '',
	publish_user_name: item.author || item.publish_user_name || item.publish_user || '',
	image_ids: item.image_ids || [],
	video_ids: item.video_ids || [],
});

const normalizeFoundationRecord = (item = {}) => {
	const base = normalizeArticle(item);
	const coverUrl = resolveRongjhMediaUrl(item.picUrl || item.cover_url || item.coverUrl || '');
	let amount = Number(item.helpAmount ?? item.help_amount ?? item.amount ?? item.applyAmount ?? 0);
	if (!amount && item.introduction) {
		const match = String(item.introduction).match(/(?:金额|帮扶金额)[：:]\s*([\d.]+)/);
		if (match) {
			amount = Number(match[1]);
		}
	}
	const attachments = [];
	if (coverUrl) {
		attachments.push({ url: coverUrl, file_type: 'image' });
	}
	return {
		...base,
		amount,
		cover_url: coverUrl,
		applicant_name: item.applicantName || item.applicant_name || item.author || '',
		execution_status: item.executionStatus || item.execution_status || '',
		applicant_phone: item.applicantPhone || item.applicant_phone || '',
		publish_user: item.author || item.publish_user || '',
		attachments,
		content: item.content || base.summary || '',
	};
};

const HELP_STATUS_TEXT = {
	0: '待审核',
	1: '已通过',
	2: '已驳回',
	3: '已撤销',
};

const parseHelpMaterials = (materials) => {
	if (!materials) return [];
	try {
		const list = typeof materials === 'string' ? JSON.parse(materials) : materials;
		if (!Array.isArray(list)) return [];
		return list
			.map((item) => {
				const rawUrl = item.url || item.datas || '';
				const url = String(rawUrl).startsWith('data:')
					? rawUrl
					: resolveRongjhMediaUrl(rawUrl);
				return {
					filename: item.filename || item.name || '附件',
					url,
					file_type: item.file_type || item.fileType || 'image',
				};
			})
			.filter((item) => item.url);
	} catch (err) {
		return [];
	}
};

const normalizeHelpApplication = (item = {}) => ({
	id: item.id,
	name: item.name || '',
	phone: item.phone || '',
	reason: item.reason || '',
	apply_amount: Number(item.applyAmount ?? item.apply_amount ?? 0),
	actual_amount: Number(item.actualAmount ?? item.actual_amount ?? 0),
	status: Number(item.status ?? 0),
	status_text: HELP_STATUS_TEXT[item.status] || '未知',
	remark: item.remark || '',
	materials: parseHelpMaterials(item.materials),
	create_time: formatDateTime(item.createTime || item.create_time),
});

/** 将本地开发环境文件地址替换为当前 API 域名 */
const fixDevFileUrl = (value = '') => {
	const url = String(value || '').trim();
	if (!url || !/^https?:\/\//i.test(url) || !/127\.0\.0\.1|localhost/i.test(url)) {
		return url;
	}
	const pathMatch = url.match(/(\/admin-api\/.+)$/);
	if (!pathMatch) return url;
	const origin = String(baseUrl || '').replace(/\/$/, '');
	return origin ? origin + pathMatch[1] : url;
};

/** 统一解析戎集汇图片/附件地址（供公告、战友会等页面复用） */
export const resolveRongjhMediaUrl = (url) => {
	if (url == null || url === '') return '';
	const raw = typeof url === 'object' ? url.url || url.datas || url.picUrl || '' : url;
	const value = fixDevFileUrl(String(raw).trim());
	if (!value) return '';
	if (/^https?:\/\//i.test(value)) return value;
	return $url.cdn(value);
};

/** 修正富文本中指向本机 admin-api 的图片链接 */
export const fixRongjhContentHtml = (html = '') => {
	if (!html) return '';
	const origin = String(baseUrl || '').replace(/\/$/, '');
	if (!origin) return html;
	return String(html)
		.replace(
			/(src|href)=(["'])(https?:\/\/(?:127\.0\.0\.1|localhost)(?::\d+)?)(\/admin-api\/[^"']+)\2/gi,
			(_, attr, quote, _host, path) => `${attr}=${quote}${origin}${path}${quote}`,
		)
		.replace(
			/(src|href)=(["'])(\/admin-api\/[^"']+)\2/gi,
			(_, attr, quote, path) => `${attr}=${quote}${origin}${path}${quote}`,
		);
};

const buildNoticeImages = (item = {}) => {
	const images = Array.isArray(item.images) ? item.images : [];
	if (images.length) return images;
	const pic = item.picUrl || item.cover_image || item.cover_url || '';
	return pic ? [{ url: pic }] : [];
};

const normalizeNotice = (item = {}) => {
	const cover = item.picUrl || item.cover_image || item.cover_url || '';
	return {
		...normalizeArticle(item),
		announcement_type_name: item.announcementTypeName || item.announcement_type_name || '公告',
		cover_image: cover,
		images: buildNoticeImages(item),
	};
};

const normalizeWarriorStatus = (item = {}) => {
	const hasApplication = item.hasApplication ?? item.has_application ?? false
	const isMember = item.isMember ?? item.is_member ?? false
	let state = item.state || ''
	if (!state) {
		if (isMember) {
			state = 'approved'
		} else if (hasApplication) {
			state = 'pending'
		}
	}
	return {
		has_application: hasApplication,
		is_member: isMember,
		total_count: Number(item.totalCount ?? item.total_count ?? 0),
		member_count: Number(item.memberCount ?? item.member_count ?? 0),
		name: item.name || '',
		phone: item.phone || '',
		state_id: item.stateId ?? item.state_id ?? null,
		state_name: item.stateName || item.state_name || '',
		city: item.city || '',
		service_unit: item.serviceUnit || item.service_unit || '',
		service_year: item.serviceYear || item.service_year || '',
		description: item.description || '',
		state,
		state_display: item.stateDisplay || item.state_display || '',
		create_date: item.createDate || item.create_date || '',
		approve_date: item.approveDate || item.approve_date || '',
	}
};

const normalizeSpu = (item = {}) => ({
	id: item.id,
	name: item.name || '',
	price: item.price != null ? Number(item.price) / 100 : 0,
	image: item.picUrl || item.image || '',
	description: item.introduction || item.description || '',
	sales: (item.salesCount ?? item.sales ?? 0) + (item.virtualSalesCount ?? 0),
});

const fetchArticlePage = async (categoryId, params = {}) => {
	const pageParams = toPageParams(params);
	const res = await request({
		url: '/sales/promotion/article/page',
		method: 'GET',
		params: { categoryId, ...pageParams, stateId: params.state_id || undefined },
		custom: { showLoading: false, showError: false },
	});
	if (res.code !== 0) {
		throw new Error(res.msg || '文章加载失败');
	}
	return res.data || { list: [], total: 0 };
};

const fetchArticleDetail = async (id) => {
	const res = await request({
		url: '/sales/promotion/article/get',
		method: 'GET',
		params: { id },
		custom: { showLoading: false, showError: false },
	});
	if (res.code !== 0) {
		return { code: 404, msg: res.msg || '文章不存在', data: null };
	}
	return res;
};

const fetchWarriorStatus = async (stateId) => {
	const res = await WarriorApi.getStatus(stateId);
	if (res.code !== 0) {
		throw new Error(res.msg || '状态加载失败');
	}
	return normalizeWarriorStatus(res.data || {});
};

const fetchProvinceList = async () => {
	const res = await AreaApi.getAreaTree();
	if (res.code !== 0) {
		throw new Error(res.msg || '省份加载失败');
	}
	const provinces = Array.isArray(res.data) ? res.data : [];
	return provinces
		.filter((item = {}) => {
			const name = String(item.name || '');
			return !/香港|澳门|台湾/.test(name);
		})
		.map((item) => ({ id: item.id, name: item.name }));
};

const fetchFullAreaTree = async () => {
	const res = await AreaApi.getAreaTree();
	if (res.code !== 0) {
		throw new Error(res.msg || '地区加载失败');
	}
	return (Array.isArray(res.data) ? res.data : []).filter((item = {}) => {
		const name = String(item.name || '');
		return !/香港|澳门|台湾/.test(name);
	});
};

const fetchSpuPage = async (params = {}) => {
	const pageParams = toPageParams(params);
	const query = {
		...pageParams,
		keyword: params.keyword,
		categorySales: params.categoryId || params.category_id,
		categoryIds: params.categoryIds,
		cityId: params.cityId ?? params.city_id ?? params.areaId,
		provinceId: params.provinceId ?? (params.province_id ? Number(params.province_id) : undefined),
		specialtyOnly: params.specialtyOnly,
		isMilitary: params.isMilitary,
		sortField: params.sortField,
		sortAsc: params.sortAsc,
	};
	const res = await SpuApi.getSpuPage(query);
	if (res.code !== 0) {
		throw new Error(res.msg || '商品加载失败');
	}
	const data = res.data || { list: [], total: 0 };
	const list = (data.list || []).map(normalizeSpu);
	const pageSize = pageParams.pageSize;
	const totalPages = Math.max(1, Math.ceil(Number(data.total || 0) / pageSize));
	return {
		list,
		pagination: {
			page: pageParams.pageNo,
			limit: pageSize,
			total_pages: totalPages,
		},
		total: data.total,
	};
};

/** 省份列表 */
export function getComradeStatesApi() {
	if (RONGJH_USE_MOCK) {
		return mockOk({ list: mockStates.list });
	}
	return fetchProvinceList().then((list) => mockOk({ list }));
}

/** 入会/申请状态 */
export function getComradeApplyStatusApi(params = {}) {
	if (RONGJH_USE_MOCK) {
		return mockOk({ ...warriorStatusMock });
	}
	return fetchWarriorStatus(params.state_id).then((data) => mockOk(data));
}

/** 军人及三属身份认证状态（个人中心横幅用） */
export const getIdentityCertStatusApi = getComradeApplyStatusApi;

/** 提交身份认证（战友 + 三属，type 区分） */
export function submitIdentityCertApi(data = {}) {
	if (RONGJH_USE_MOCK) {
		console.log('[rongjh mock] submitIdentityCertApi', data);
		return Promise.resolve(mockOk({ success: true }));
	}
	return WarriorApi.submit({
		type: data.type || 'SELF',
		name: data.name,
		phone: data.phone,
		stateId: data.state_id,
		stateName: data.state_name,
		city: data.city,
		serviceUnit: data.service_unit,
		serviceYear: data.service_year,
		description: data.description,
	}).then((res) => {
		if (res.code !== 0) {
			return Promise.reject(new Error(res.msg || '提交失败'));
		}
		return mockOk({ success: true });
	});
}

/** 提交入会申请 */
export function submitComradeApplyApi(data = {}) {
	if (RONGJH_USE_MOCK) {
		console.log('[rongjh mock] submitComradeApplyApi', data);
		return mockOk({ success: true });
	}
	return submitIdentityCertApi({ ...data, type: 'SELF' });
}

/** 战友会文章列表 */
export async function getComradeArticleListApi(params = {}) {
	if (RONGJH_USE_MOCK) {
		let list = [...articlesWarriorMock.list];
		if (params.state_id) {
			list = list.filter((item) => Number(item.state_id) === Number(params.state_id));
		}
		const { list: pageList, pagination } = paginate(list, params.page, params.limit);
		return mockOk({
			list: pageList,
			pagination: {
				...articlesWarriorMock.pagination,
				...pagination,
				state_users_count: params.state_id ? pageList.length + 50 : articlesWarriorMock.pagination.state_users_count,
			},
		});
	}
	const pageData = await fetchArticlePage(ARTICLE_CATEGORY.WARRIOR, params);
	let list = (pageData.list || []).map(normalizeArticle);
	if (params.state_id) {
		list = list.filter((item) => !item.state_id || Number(item.state_id) === Number(params.state_id));
	}
	const status = await fetchWarriorStatus(params.state_id);
	const pageSize = Number(params.limit) || 10;
	const total = Number(pageData.total || list.length);
	return mockOk({
		list,
		pagination: {
			page: Number(params.page) || 1,
			limit: pageSize,
			total_pages: Math.max(1, Math.ceil(total / pageSize)),
			comrade_users_count: status.total_count,
			state_users_count: params.state_id ? status.member_count : status.total_count,
		},
	});
}

/** 战友会文章详情 */
export async function getComradeArticleDetailApi(articleId) {
	if (RONGJH_USE_MOCK) {
		const item = articlesWarriorMock.list.find((a) => Number(a.id) === Number(articleId));
		return item ? mockOk(item) : Promise.resolve({ code: 404, msg: '文章不存在', data: null });
	}
	const res = await fetchArticleDetail(articleId);
	if (res.code !== 0) {
		return { code: 404, msg: res.msg || '文章不存在', data: null };
	}
	return mockOk(normalizeArticle(res.data));
}

/** 文章点赞 */
export function likeComradeArticleApi(articleId) {
	if (RONGJH_USE_MOCK) {
		const item = articlesWarriorMock.list.find((a) => Number(a.id) === Number(articleId));
		const likeCount = (item?.like_count || 0) + 1;
		const likedArticles = uni.getStorageSync('likedArticles') || [];
		if (!likedArticles.includes(Number(articleId))) {
			likedArticles.push(Number(articleId));
			uni.setStorageSync('likedArticles', likedArticles);
		}
		return mockOk({ like_count: likeCount });
	}
	return mockOk({ like_count: 0 });
}

/** 专区轮播 + 商品列表 */
export async function getZoneProductListApi(params = {}) {
	if (RONGJH_USE_MOCK) {
		const zoneName = params.zone_name || params.name || 'default';
		const key = zoneProductsMock.products[zoneName] ? zoneName : 'default';
		let list = [...(zoneProductsMock.products[key] || [])];
		const kw = String(params.keyword || '').trim();
		if (kw) {
			list = list.filter((item) => String(item.name || '').includes(kw));
		}
		const banners = zoneProductsMock.banners[key] || zoneProductsMock.banners.default;
		return mockOk({ list, banners, total: list.length });
	}
	const pageData = await fetchSpuPage({
		page: 1,
		limit: 50,
		keyword: params.keyword,
		categoryId: params.categoryId || params.category_id,
	});
	return mockOk({
		list: pageData.list,
		banners: [{ image: '' }],
		total: pageData.total || pageData.list.length,
	});
}

/** 提交帮扶申请 */
export function submitFoundationAssistanceApi(data = {}) {
	if (RONGJH_USE_MOCK) {
		console.log('[rongjh mock] submitFoundationAssistanceApi', data);
		return Promise.resolve({ code: 200, msg: '提交成功', data: { success: true } });
	}
	return HelpApi.submit({
		name: data.name,
		phone: data.phone,
		province_id: data.province_id,
		city: data.city,
		description: data.description,
		reason: data.description,
		attachments: data.attachments,
	}).then((res) => {
		if (res.code !== 0) {
			return Promise.resolve({ code: res.code, msg: res.msg || '提交失败', data: null });
		}
		return Promise.resolve({ code: 200, msg: '提交成功', data: { success: true } });
	});
}

/** 帮扶记录列表 */
export async function getFoundationRecordListApi(params = {}) {
	if (RONGJH_USE_MOCK) {
		const { list: pageList } = paginate(foundationRecordsMock.list, params.page, params.limit);
		return Promise.resolve({
			code: 200,
			data: { list: pageList, total: foundationRecordsMock.total },
			total: foundationRecordsMock.total,
		});
	}
	const pageData = await fetchArticlePage(ARTICLE_CATEGORY.FOUNDATION, params);
	const list = (pageData.list || []).map(normalizeFoundationRecord);
	const total = pageData.total || list.length;
	return Promise.resolve({
		code: 200,
		data: { list, total },
		total,
	});
}

/** 帮扶记录详情 */
export async function getFoundationRecordDetailApi(id) {
	if (RONGJH_USE_MOCK) {
		const item = foundationRecordsMock.list.find((r) => Number(r.id) === Number(id));
		return item ? mockOk(item) : Promise.resolve({ code: 404, msg: '记录不存在', data: null });
	}
	const res = await fetchArticleDetail(id);
	if (res.code !== 0) {
		return { code: 404, msg: res.msg || '记录不存在', data: null };
	}
	return mockOk(normalizeFoundationRecord(res.data));
}

/** 帮扶总金额汇总 */
export async function getFoundationRecordTotalAmountApi() {
	if (RONGJH_USE_MOCK) {
		return mockOk(foundationRecordsMock.totalAmount);
	}
	const res = await request({
		url: '/rongjh/help/total-amount',
		method: 'GET',
		custom: { auth: true, showLoading: false, showError: false },
	});
	if (res.code !== 0) {
		return mockOk({
			contribution_amount: 0,
			total_amount: 0,
			deadline: '',
		});
	}
	const data = res.data || {};
	return mockOk({
		contribution_amount: data.contributionAmount ?? data.contribution_amount ?? 0,
		total_amount: data.totalAmount ?? data.total_amount ?? data.contributionAmount ?? data.contribution_amount ?? 0,
		deadline: data.deadline || '',
	});
}

/** 我的帮扶申请列表 */
export async function getMyHelpApplicationsApi() {
	if (RONGJH_USE_MOCK) {
		return mockOk([
			{
				id: 1001,
				name: '张三',
				phone: '13800000001',
				reason: '医疗费用求助',
				apply_amount: 5000,
				actual_amount: 0,
				status: 0,
				status_text: '待审核',
				remark: '',
				materials: [],
				create_time: '2026-03-01 10:00:00',
			},
		]);
	}
	const res = await HelpApi.list();
	if (res.code !== 0) {
		return Promise.resolve({ code: res.code, msg: res.msg || '加载失败', data: [] });
	}
	const list = (res.data || []).map(normalizeHelpApplication);
	return mockOk(list);
}

/** 撤销帮扶申请 */
export async function cancelHelpApplicationApi(id) {
	if (RONGJH_USE_MOCK) {
		return mockOk({ success: true });
	}
	const res = await HelpApi.cancel(id);
	if (res.code !== 0) {
		return Promise.resolve({ code: res.code, msg: res.msg || '撤销失败', data: null });
	}
	return mockOk({ success: true });
}

/** 平台爱心值 */
export async function getPartnerLoveValueApi() {
	if (RONGJH_USE_MOCK) {
		return mockOk({ total_love_values: 98652.35 });
	}
	const res = await request({
		url: '/rongjh/config/love-value',
		method: 'GET',
		custom: { showLoading: false, showError: false },
	});
	if (res.code !== 0) {
		throw new Error(res.msg || '爱心值加载失败');
	}
	return mockOk({
		total_love_values: res.data?.totalLoveValues ?? 0,
	});
}

export const getPartnerLoveValuesApi = getPartnerLoveValueApi;

/** 公告列表 */
export async function getNoticeListApi(params = {}) {
	if (RONGJH_USE_MOCK) {
		const { list: pageList } = paginate(noticesMock.list, params.page, params.limit);
		return mockOk({ list: pageList, total: noticesMock.total });
	}
	const pageData = await fetchArticlePage(ARTICLE_CATEGORY.NOTICE, params);
	const list = (pageData.list || []).map(normalizeNotice);
	return mockOk({ list, total: pageData.total || list.length });
}

/** 公告详情 */
export async function getNoticeDetailApi(id) {
	if (RONGJH_USE_MOCK) {
		const item = noticesMock.list.find((n) => Number(n.id) === Number(id));
		return item ? mockOk(item) : Promise.resolve({ code: 404, msg: '公告不存在', data: null });
	}
	const res = await fetchArticleDetail(id);
	if (res.code !== 0) {
		return { code: 404, msg: res.msg || '公告不存在', data: null };
	}
	return mockOk(normalizeNotice(res.data));
}

/** 增加文章浏览量（战友会 / 帮扶 / 公告 等所有文章通用） */
export async function addArticleBrowseCountApi(id) {
	if (RONGJH_USE_MOCK || !id) {
		return mockOk(true);
	}
	return request({
		url: '/sales/promotion/article/add-browse-count',
		method: 'PUT',
		params: { id },
		custom: { showLoading: false, showError: false },
	});
}

/** 增加公告浏览量（兼容旧调用，等同 addArticleBrowseCountApi） */
export const addNoticeBrowseCountApi = addArticleBrowseCountApi;

/** 关于我们静态页 */
export function getAboutPageApi() {
	if (RONGJH_USE_MOCK) {
		return mockOk({ ...aboutMock });
	}
	return mockOk({ ...aboutMock });
}

/** 用户协议 / 隐私政策 */
export function getProtocolContentApi(type = 'user') {
	if (RONGJH_USE_MOCK) {
		const key = type === 'privacy' ? 'privacy' : 'user';
		return mockOk(protocolMock[key] || protocolMock.user);
	}
	const key = type === 'privacy' ? 'privacy' : 'user';
	return mockOk(protocolMock[key] || protocolMock.user);
}

/** 联系我们 / 客服中心 */
export function getContactServiceApi() {
	if (RONGJH_USE_MOCK) {
		return mockOk({ ...contactServiceMock });
	}
	return mockOk({ ...contactServiceMock });
}

/** 修改密码（场景 3 短信改密：code / password） */
export function changePasswordApi(data = {}) {
	if (RONGJH_USE_MOCK) {
		console.log('[rongjh mock] changePasswordApi', data);
		return Promise.resolve({ code: 200, msg: '修改成功' });
	}
	return UserApi.updateUserPassword({
		code: data.code,
		password: data.password || data.newPassword,
	}).then((res) => {
		if (res.code !== 0) {
			return Promise.resolve({ code: res.code, msg: res.msg || '修改失败' });
		}
		return Promise.resolve({ code: 200, msg: '修改成功' });
	});
}

/** 省份/区域列表 */
export function getRegionListApi() {
	return getComradeStatesApi();
}

/** 同城服务轮播 */
export function getLocalServiceBannersApi() {
	if (RONGJH_USE_MOCK) {
		return mockOk({ list: localProductsMock.banners.default || [] });
	}
	return mockOk({ list: [{ image: '' }] });
}

/** 全国特产轮播 */
export function getSpecialtyZoneBannersApi() {
	if (RONGJH_USE_MOCK) {
		return mockOk({ list: zoneSpecialtyMock.banners || [{ image: '' }] });
	}
	return mockOk({ list: [{ image: '' }] });
}

/** 全国特产商品（按省，分页） */
export async function getSpecialtyZoneProductsApi(params = {}) {
	if (RONGJH_USE_MOCK) {
		const provinceId = Number(params.province_id || 0);
		const key = zoneSpecialtyMock.products[provinceId] !== undefined ? provinceId : 0;
		let list = [...(zoneSpecialtyMock.products[key] || zoneSpecialtyMock.products[0] || [])];
		const kw = String(params.keyword || '').trim();
		if (kw) {
			list = list.filter(
				(item) =>
					String(item.name || '').includes(kw) || String(item.description || '').includes(kw),
			);
		}
		const { list: pageList, pagination } = paginate(list, params.page, params.limit);
		return mockOk({ list: pageList, pagination });
	}
	return getProvinceProductsApi(params);
}

/** 全国特产商品（按省，分页；省级编号走 provinceId，全国特产不得传 cityId） */
export async function getProvinceProductsApi(params = {}) {
	if (RONGJH_USE_MOCK) {
		const provinceId = Number(params.province_id || 0);
		const key = zoneSpecialtyMock.products[provinceId] !== undefined ? provinceId : 0;
		let list = [...(zoneSpecialtyMock.products[key] || zoneSpecialtyMock.products[0] || [])];
		const kw = String(params.keyword || '').trim();
		if (kw) {
			list = list.filter(
				(item) =>
					String(item.name || '').includes(kw) || String(item.description || '').includes(kw),
			);
		}
		const { list: pageList, pagination } = paginate(list, params.page, params.limit);
		return mockOk({ list: pageList, pagination });
	}
	// 「全部」：不传地区，仅 specialtyOnly=true；「某省」：provinceId + specialtyOnly=true，后端负责省→市展开
	const provinceId = Number(params.province_id || 0);
	const pageData = await fetchSpuPage({
		...params,
		...(provinceId ? { provinceId } : {}),
		specialtyOnly: true,
	});
	let province = { id: provinceId, name: '' };
	if (provinceId) {
		const provinces = await fetchProvinceList();
		province = provinces.find((item) => Number(item.id) === provinceId) || province;
	}
	return mockOk({
		list: pageData.list,
		pagination: pageData.pagination,
		province,
	});
}

/** 同城服务商品（按城市） */
export async function getCityProductsApi(params = {}) {
	const cityId = Number(params.cityId || params.city_id || 0);
	if (RONGJH_USE_MOCK) {
		const key = localProductsMock.products[cityId] ? cityId : 'default';
		let list = [...(localProductsMock.products[key] || localProductsMock.products.default || [])];
		const kw = String(params.keyword || '').trim();
		if (kw) {
			list = list.filter(
				(item) =>
					String(item.name || '').includes(kw) || String(item.description || '').includes(kw),
			);
		}
		if (params.fetchAll) {
			return mockOk({
				list,
				pagination: { page: 1, total_pages: 1, limit: list.length },
				city: localProductsMock.cities[cityId] || localProductsMock.cities.default,
			});
		}
		const { list: pageList, pagination } = paginate(list, params.page, params.limit);
		return mockOk({
			list: pageList,
			pagination,
			city: localProductsMock.cities[cityId] || localProductsMock.cities.default,
		});
	}
	const pageData = await fetchSpuPage({ ...params, cityId });
	let city = { id: cityId, name: '' };
	if (cityId) {
		const tree = await fetchFullAreaTree();
		for (const province of tree) {
			const children = province.children || [];
			const matched = children.find((item) => Number(item.id) === cityId);
			if (matched) {
				city = {
					id: matched.id,
					name: matched.name,
					provinceId: province.id,
					provinceName: province.name,
				};
				break;
			}
		}
	}
	if (params.fetchAll) {
		return mockOk({
			list: pageData.list,
			pagination: { page: 1, total_pages: 1, limit: pageData.list.length },
			city,
		});
	}
	return mockOk({
		list: pageData.list,
		pagination: pageData.pagination,
		city,
	});
}
