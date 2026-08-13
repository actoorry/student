import request from '@/sheep/request';
import { buildSalesQuery } from '@/sheep/helper/brokerage-contract';

const BrokerageApi = {
  // 绑定分销用户
  bindBrokerageUser: (data) => {
    return request({
      url: '/sales/brokerage-user/bind',
      method: 'PUT',
      data,
    });
  },
  // 申请成为分销用户
  applyBrokerageUser: () => {
    return request({
      url: '/sales/brokerage-user/apply',
      method: 'POST',
    });
  },
  // 获得个人分销信息
  getBrokerageUser: () => {
    return request({
      url: '/sales/brokerage-user/get',
      method: 'GET',
    });
  },
  // 获得个人分销统计
  getBrokerageUserSummary: () => {
    return request({
      url: '/sales/brokerage-user/get-summary',
      method: 'GET',
    });
  },
  // 获得分销记录分页
  getBrokerageRecordPage: (params = {}) => {
    const queryString = buildSalesQuery(params);
    return request({
      url: `/sales/brokerage-record/page${queryString ? `?${queryString}` : ''}`,
      method: 'GET',
    });
  },
  // 创建分销提现
  createBrokerageWithdraw: (data) => {
    return request({
      url: '/sales/brokerage-withdraw/create',
      method: 'POST',
      data,
    });
  },
  // 获得分销提现分页
  getBrokerageWithdrawPage: (params = {}) => {
    const queryString = buildSalesQuery(params);
    return request({
      url: `/sales/brokerage-withdraw/page${queryString ? `?${queryString}` : ''}`,
      method: 'GET',
    });
  },
  // 获得分销提现详情
  getBrokerageWithdraw: (id) => {
    return request({
      url: '/sales/brokerage-withdraw/get',
      method: 'GET',
      params: { id },
    });
  },
  // 获得商品的分销金额
  getProductBrokeragePrice: (spuId) => {
    return request({
      url: '/sales/brokerage-record/get-product-brokerage-price',
      method: 'GET',
      params: { spuId },
    });
  },
  // 获得分销用户排行（基于佣金）
  getRankByPrice: (params = {}) => {
    const queryString = buildSalesQuery(params);
    return request({
      url: `/sales/brokerage-user/get-rank-by-price${queryString ? `?${queryString}` : ''}`,
      method: 'GET',
    });
  },
  // 获得分销用户排行分页（基于佣金）
  getBrokerageUserChildSummaryPageByPrice: (params = {}) => {
    const queryString = buildSalesQuery(params);
    return request({
      url: `/sales/brokerage-user/rank-page-by-price${queryString ? `?${queryString}` : ''}`,
      method: 'GET',
    });
  },
  // 获得分销用户排行分页（基于用户量）
  getBrokerageUserRankPageByUserCount: (params = {}) => {
    const queryString = buildSalesQuery(params);
    return request({
      url: `/sales/brokerage-user/rank-page-by-user-count${queryString ? `?${queryString}` : ''}`,
      method: 'GET',
    });
  },
  // 获得下级分销统计分页
  getBrokerageUserChildSummaryPage: (params) => {
    return request({
      url: '/sales/brokerage-user/child-summary-page',
      method: 'GET',
      params,
    });
  },
};

export default BrokerageApi;
