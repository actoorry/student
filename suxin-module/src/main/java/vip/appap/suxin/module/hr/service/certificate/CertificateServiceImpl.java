package vip.appap.suxin.module.hr.service.certificate;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.*;
import vip.appap.suxin.module.hr.dal.dataobject.certificate.CertificateDO;
import vip.appap.suxin.module.hr.dal.dataobject.employee.EmployeeDO;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;

import vip.appap.suxin.module.hr.dal.mysql.certificate.CertificateMapper;
import vip.appap.suxin.module.hr.dal.mysql.employee.EmployeeMapper;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.hr.enums.ErrorCodeConstants.*;

/**
 * 人员证书 Service 实现类
 *
 * 说明：证书只写入 hr_certificate 表，不双写 partner 表。
 * 创建/更新时校验关联员工存在，并自动回填 employee_no、dept。
 *
 * @author admin
 */
@Service
@Validated
public class CertificateServiceImpl implements CertificateService {

    @Resource
    private CertificateMapper certificateMapper;
    @Resource
    private EmployeeMapper employeeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCertificate(CertificateSaveReqVO createReqVO) {
        // 校验员工存在并回填工号、部门
        EmployeeDO employee = validateEmployeeExists(createReqVO.getPartnerId());
        CertificateDO certificate = BeanUtils.toBean(createReqVO, CertificateDO.class);
        certificate.setEmployeeNo(employee.getEmployeeNo());
        certificate.setDept(employee.getDept());
        certificateMapper.insert(certificate);
        return certificate.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCertificate(CertificateSaveReqVO updateReqVO) {
        validateCertificateExists(updateReqVO.getId());
        EmployeeDO employee = validateEmployeeExists(updateReqVO.getPartnerId());
        CertificateDO updateObj = BeanUtils.toBean(updateReqVO, CertificateDO.class);
        updateObj.setEmployeeNo(employee.getEmployeeNo());
        updateObj.setDept(employee.getDept());
        certificateMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCertificate(Long id) {
        validateCertificateExists(id);
        certificateMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCertificateListByIds(List<Long> ids) {
        certificateMapper.deleteByIds(ids);
    }

    private CertificateDO validateCertificateExists(Long id) {
        CertificateDO certificate = certificateMapper.selectById(id);
        if (certificate == null) {
            throw exception(CERTIFICATE_NOT_EXISTS);
        }
        return certificate;
    }

    private EmployeeDO validateEmployeeExists(Long partnerId) {
        EmployeeDO employee = employeeMapper.selectProfileByPartnerId(partnerId);
        if (employee == null) {
            throw exception(CERTIFICATE_EMPLOYEE_NOT_EXISTS);
        }
        return employee;
    }

    @Override
    public CertificateRespVO getCertificate(Long id) {
        CertificateRespVO certificate = certificateMapper.selectByIdJoin(id);
        if (certificate == null) {
            throw exception(CERTIFICATE_NOT_EXISTS);
        }
        return certificate;
    }

    @Override
    public PageResult<CertificateRespVO> getCertificatePage(CertificatePageReqVO pageReqVO) {
        return certificateMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<CertificateRespVO> getCertificateLedgerPage(CertificatePageReqVO pageReqVO) {
        return certificateMapper.selectLedgerPage(pageReqVO);
    }

    @Override
    public CertificateLedgerSummaryRespVO getCertificateLedgerSummary() {
        return certificateMapper.selectLedgerSummary();
    }

}
