package vip.appap.suxin.module.hr.service.certificate;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificateNotifyLogPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificateNotifyLogRespVO;

/**
 * 证书推送日志 Service
 *
 * @author admin
 */
public interface CertificateNotifyLogService {

    /**
     * 推送日志分页查询
     */
    PageResult<CertificateNotifyLogRespVO> getNotifyLogPage(CertificateNotifyLogPageReqVO reqVO);

    /**
     * 手动补推一条失败的推送日志
     */
    void retryNotifyLog(Long id);

}
