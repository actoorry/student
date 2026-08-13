import request from '@/sheep/request';

const quiet = { showLoading: false, showError: false, auth: true };

const PartnerCertificationApi = {
  verifyRealName: (data) => request({ url: '/partner/name-check/verify', method: 'POST', data, custom: quiet }),
  getMarriageStatus: () => request({ url: '/partner/marriage-check/status', method: 'GET', custom: quiet }),
  verifyMarriage: () => request({ url: '/partner/marriage-check/verify', method: 'POST', data: {}, custom: quiet }),
};

export default PartnerCertificationApi;
