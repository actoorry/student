package vip.appap.suxin.module.hr.service.employee;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import vip.appap.suxin.module.hr.controller.admin.employee.vo.*;
import vip.appap.suxin.module.hr.dal.dataobject.employee.EmployeeDO;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;

import vip.appap.suxin.module.hr.dal.mysql.employee.EmployeeMapper;
import vip.appap.suxin.module.hr.integration.zhiye.ZhiyeProviderSyncService;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerMapper;
import vip.appap.suxin.module.system.dal.dataobject.PostDO;
import vip.appap.suxin.module.system.service.PostService;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static vip.appap.suxin.module.hr.enums.ErrorCodeConstants.*;

/**
 * 员工 Service 实现类
 *
 * 双写说明：员工的基础信息（姓名、手机、身份证等）存 partner 表，
 * HR 专用字段存 hr_employee 表，通过 partner_id 关联。
 *
 * @author admin
 */
@Service
@Validated
public class EmployeeServiceImpl implements EmployeeService {

    @Resource
    private EmployeeMapper employeeMapper;

    @Resource
    private PartnerMapper partnerMapper;

    @Resource
    private EmployeePostSyncService employeePostSyncService;

    @Resource
    private ZhiyeProviderSyncService zhiyeProviderSyncService;

    @Resource
    private PostService postService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createEmployee(EmployeeSaveReqVO createReqVO) {
        normalizeEmployeeSaveReqVO(createReqVO);
        validateEmployeeNoUnique(null, createReqVO.getEmployeeNo());

        // 1. 插入 partner 基础信息
        PartnerDO partner = buildPartnerDO(createReqVO);
        partnerMapper.insert(partner);

        // 2. 插入 hr_employee 扩展信息
        EmployeeDO employee = buildEmployeeDO(createReqVO);
        employee.setPartnerId(partner.getId());
        employeeMapper.insert(employee);
        employeePostSyncService.syncUserPostFromEmployee(partner.getId(), employee.getPostId());
        zhiyeProviderSyncService.syncRegister(employee.getId());

        return employee.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmployee(EmployeeSaveReqVO updateReqVO) {
        // 1. 校验员工存在
        EmployeeDO employee = validateEmployeeExists(updateReqVO.getId());
        normalizeEmployeeSaveReqVO(updateReqVO);
        validateEmployeeNoUnique(updateReqVO.getId(), updateReqVO.getEmployeeNo());

        // 2. 更新 partner 基础信息
        PartnerDO partner = buildPartnerDO(updateReqVO);
        partner.setId(employee.getPartnerId());
        partnerMapper.updateById(partner);

        // 3. 更新 hr_employee 扩展信息
        EmployeeDO updateObj = buildEmployeeDO(updateReqVO);
        updateObj.setId(updateReqVO.getId());
        updateObj.setPartnerId(employee.getPartnerId());
        employeeMapper.updateById(updateObj);
        employeePostSyncService.syncUserPostFromEmployee(employee.getPartnerId(), updateObj.getPostId());
        zhiyeProviderSyncService.syncUpdate(updateReqVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEmployee(Long id) {
        EmployeeDO employee = validateEmployeeExists(id);
        employeeMapper.deleteById(id);
        partnerMapper.deleteById(employee.getPartnerId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEmployeeListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<EmployeeDO> employees = employeeMapper.selectByIds(ids);
        if (employees.size() != ids.size()) {
            throw exception(EMPLOYEE_NOT_EXISTS);
        }
        employeeMapper.deleteByIds(ids);
        Set<Long> partnerIds = convertSet(employees, EmployeeDO::getPartnerId);
        partnerIds.forEach(partnerMapper::deleteById);
    }

    private EmployeeDO validateEmployeeExists(Long id) {
        EmployeeDO employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw exception(EMPLOYEE_NOT_EXISTS);
        }
        return employee;
    }

    private void normalizeEmployeeSaveReqVO(EmployeeSaveReqVO vo) {
        vo.setName(StrUtil.trim(vo.getName()));
        vo.setIdCard(StrUtil.trim(vo.getIdCard()));
        vo.setDetailAddress(StrUtil.trim(vo.getDetailAddress()));
        vo.setRemark(StrUtil.trim(vo.getRemark()));
        vo.setEmployeeNo(StrUtil.trim(vo.getEmployeeNo()));
        vo.setEmployeeMobile(StrUtil.trim(vo.getEmployeeMobile()));
        vo.setDuty(StrUtil.trim(vo.getDuty()));
        vo.setFullTimeEducation(StrUtil.trim(vo.getFullTimeEducation()));
        vo.setBankCard(StrUtil.trim(vo.getBankCard()));
        vo.setHomeInformation(StrUtil.trim(vo.getHomeInformation()));
        vo.setSchoolMajor(StrUtil.trim(vo.getSchoolMajor()));
        vo.setNameAbbreviation(StrUtil.trim(vo.getNameAbbreviation()));
    }

    private void validateEmployeeNoUnique(Long id, String employeeNo) {
        if (StrUtil.isBlank(employeeNo)) {
            return;
        }
        EmployeeDO exist = employeeMapper.selectOne(EmployeeDO::getEmployeeNo, employeeNo);
        if (exist != null && !exist.getId().equals(id)) {
            throw exception(EMPLOYEE_NO_DUPLICATE);
        }
    }

    @Override
    public EmployeeRespVO getEmployee(Long id) {
        EmployeeRespVO employee = employeeMapper.selectByIdJoin(id);
        if (employee == null) {
            throw exception(EMPLOYEE_NOT_EXISTS);
        }
        return employee;
    }

    @Override
    public PageResult<EmployeeRespVO> getEmployeePage(EmployeePageReqVO pageReqVO) {
        return employeeMapper.selectPage(pageReqVO);
    }

    // ========== 私有方法：组装 DO ==========

    private PartnerDO buildPartnerDO(EmployeeSaveReqVO vo) {
        PartnerDO partner = new PartnerDO();
        partner.setName(vo.getName());
        partner.setNickname(vo.getName() != null ? vo.getName() : "");
        // partner.mobile 不再存员工手机号，避免 uk_tenant_mobile 唯一键冲突；员工手机号存 hr_employee.employee_mobile
        partner.setIdCard(vo.getIdCard());
        partner.setSex(vo.getSex() != null ? vo.getSex() : 0);
        if (vo.getBirthday() != null) {
            partner.setBirthday(vo.getBirthday());
        }
        partner.setAvatar(vo.getAvatar() != null ? vo.getAvatar() : "");
        partner.setDetailAddress(vo.getDetailAddress());
        partner.setRemark(vo.getRemark());
        partner.setStatus(0); // 默认启用
        return partner;
    }

    private EmployeeDO buildEmployeeDO(EmployeeSaveReqVO vo) {
        EmployeeDO employee = new EmployeeDO();
        employee.setId(vo.getId());
        employee.setEmployeeNo(vo.getEmployeeNo());
        employee.setEmployeeMobile(vo.getEmployeeMobile());
        employee.setDept(vo.getDept());
        employee.setPostId(vo.getPostId());
        employee.setPersonnelCategory(vo.getPersonnelCategory());
        employee.setProfessionalTitle(vo.getProfessionalTitle());
        employee.setPoliticalStatus(vo.getPoliticalStatus());
        employee.setPartyJoinDate(toLocalDate(vo.getPartyJoinDate()));
        employee.setCareerStartDate(toLocalDate(vo.getCareerStartDate()));
        employee.setHireDate(toLocalDate(vo.getHireDate()));
        applyPost(vo, employee);
        employee.setDuty(vo.getDuty());
        employee.setAppointmentDate(toLocalDate(vo.getAppointmentDate()));
        employee.setHighestEducation(vo.getHighestEducation());
        employee.setFullTimeEducation(vo.getFullTimeEducation());
        employee.setEstablishmentStatus(vo.getEstablishmentStatus());
        employee.setPersonnelIdentity(vo.getPersonnelIdentity());
        employee.setRecruitmentSource(vo.getRecruitmentSource());
        employee.setEntryMode(vo.getEntryMode());
        employee.setEmploymentStatus(vo.getEmploymentStatus());
        employee.setEthnicity(vo.getEthnicity());
        employee.setNativeProvince(vo.getNativeProvince());
        employee.setNativeCity(vo.getNativeCity());
        employee.setBankCard(vo.getBankCard());
        employee.setHomeInformation(vo.getHomeInformation());
        employee.setSchoolMajor(vo.getSchoolMajor());
        employee.setProfessionalCategory(vo.getProfessionalCategory());
        employee.setNameAbbreviation(vo.getNameAbbreviation());
        return employee;
    }

    private void applyPost(EmployeeSaveReqVO vo, EmployeeDO employee) {
        Long postId = vo.getPostId();
        if (postId != null) {
            postService.validatePostList(Collections.singleton(postId));
            PostDO post = postService.getPost(postId);
            employee.setPostId(postId);
            employee.setPosition(post != null ? post.getName() : vo.getPosition());
        } else {
            employee.setPostId(null);
            employee.setPosition(vo.getPosition());
        }
    }

    private static LocalDate toLocalDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate() : null;
    }

}
