package vip.appap.suxin.module.hr.service.certificate;

import java.util.List;
import jakarta.validation.*;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.*;
import vip.appap.suxin.framework.common.pojo.PageResult;

/**
 * 人员证书 Service 接口
 *
 * @author admin
 */
public interface CertificateService {

    /**
     * 创建证书
     */
    Long createCertificate(@Valid CertificateSaveReqVO createReqVO);

    /**
     * 更新证书
     */
    void updateCertificate(@Valid CertificateSaveReqVO updateReqVO);

    /**
     * 删除证书
     */
    void deleteCertificate(Long id);

    /**
     * 批量删除证书
     */
    void deleteCertificateListByIds(List<Long> ids);

    /**
     * 获得证书
     */
    CertificateRespVO getCertificate(Long id);

    /**
     * 获得证书分页（证书管理页，全量）
     */
    PageResult<CertificateRespVO> getCertificatePage(CertificatePageReqVO pageReqVO);

    /**
     * 督查台账分页（默认只显示风险证书：已过期/即将到期/考核逾期）
     */
    PageResult<CertificateRespVO> getCertificateLedgerPage(CertificatePageReqVO pageReqVO);

    /**
     * 督查台账汇总（三个风险计数）
     */
    CertificateLedgerSummaryRespVO getCertificateLedgerSummary();

}
