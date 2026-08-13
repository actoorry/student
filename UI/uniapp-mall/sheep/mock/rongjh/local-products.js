/** 同城服务 Mock：按城市 ID 分组商品 */
export default {
	banners: {
		default: [{ image: '' }, { image: '' }],
	},
	cities: {
		default: { id: 0, name: '默认城市' },
		110100: { id: 110100, name: '北京市', provinceId: 110000, provinceName: '北京市' },
		440100: { id: 440100, name: '广州市', provinceId: 440000, provinceName: '广东省' },
		340100: { id: 340100, name: '合肥市', provinceId: 340000, provinceName: '安徽省' },
	},
	products: {
		default: [
			{ id: 5001, name: '同城优选 新鲜时蔬礼盒', price: '39.9', image: '', sales: 128, description: '产地直发' },
			{ id: 5002, name: '本地商户 手工豆腐', price: '12.8', image: '', sales: 86, description: '当日现做' },
		],
		110100: [
			{ id: 5101, name: '北京烤鸭礼盒 同城专送', price: '168', image: '', sales: 320, description: '2小时达' },
			{ id: 5102, name: '老北京炸酱面套装', price: '45', image: '', sales: 156, description: '同城配送' },
			{ id: 5103, name: '京郊有机草莓 500g', price: '58', image: '', sales: 89, description: '新鲜采摘' },
		],
		440100: [
			{ id: 5201, name: '广式早茶点心组合', price: '88', image: '', sales: 210, description: '同城速达' },
			{ id: 5202, name: '深圳本地海鲜套餐', price: '198', image: '', sales: 67, description: '活鲜配送' },
		],
		340100: [
			{ id: 5301, name: '合肥老字号臭鳜鱼', price: '128', image: '', sales: 145, description: '徽菜经典' },
			{ id: 5302, name: '黄山毛峰茶叶 200g', price: '168', image: '', sales: 92, description: '产地直供' },
			{ id: 5303, name: '芜湖铁画小摆件', price: '299', image: '', sales: 34, description: '非遗文创' },
			{ id: 5304, name: '六安瓜片 明前茶', price: '218', image: '', sales: 56, description: '同城闪送' },
		],
	},
};
