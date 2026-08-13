/**
 * 人物推荐接口。过滤规则由人物信息卡瀑布流组件配置。
 */
import request from '@/sheep/request';

const RecommendApi = {
  /**
   * 获取首页推荐会员分页
   * @param {Object} params
   * @param {number} params.pageNo 页码
   * @param {number} params.pageSize 页大小
   * @param {boolean} params.realVerifiedOnly 是否仅实名
   * @param {boolean} params.backgroundImageRequired 是否必须有背景图
   * @param {boolean} params.oppositeSexOnly 是否仅异性
   */
  getRecommendPage: ({
    pageNo = 1,
    pageSize = 10,
    realVerifiedOnly = false,
    backgroundImageRequired = false,
    oppositeSexOnly = false,
  } = {}) => {
    return request({
      url: '/marriage/recommend-member/page',
      method: 'GET',
      params: {
        pageNo,
        pageSize,
        realVerifiedOnly,
        backgroundImageRequired,
        oppositeSexOnly,
      },
      custom: {
        showError: false,
        showLoading: false,
      },
    });
  },
};

export default RecommendApi;
