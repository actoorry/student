/** 套壳 Mock 入会状态：改 export default 可切换 none | pending | approved | rejected */

export const warriorStatusNone = {
	has_application: false,
	is_member: false,
	total_count: 1286,
	member_count: 156,
};

export const warriorStatusPending = {
	has_application: true,
	is_member: false,
	state: 'pending',
	state_display: '审核中',
	name: '张三',
	phone: '13800138000',
	state_id: 4,
	state_name: '安徽省',
	city: '合肥市',
	service_unit: '某部队',
	service_year: '2010-2015',
	create_date: '2026-03-01 10:00:00',
	total_count: 1286,
	member_count: 156,
};

export const warriorStatusApproved = {
	has_application: true,
	is_member: true,
	state: 'approved',
	state_display: '已通过',
	name: '李四',
	state_id: 1,
	state_name: '北京市',
	service_unit: '某旅',
	service_year: '2008-2012',
	approve_date: '2026-02-15 14:00:00',
	total_count: 1286,
	member_count: 156,
};

export default warriorStatusNone;
