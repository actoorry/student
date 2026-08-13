package vip.appap.suxin.module.hr.service.appointment;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentBatchSaveReqVO;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentRespVO;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentSaveReqVO;
import vip.appap.suxin.module.hr.dal.dataobject.appointment.AppointmentDO;
import vip.appap.suxin.module.hr.dal.dataobject.employee.EmployeeDO;
import vip.appap.suxin.module.hr.dal.mysql.appointment.AppointmentMapper;
import vip.appap.suxin.module.hr.dal.mysql.employee.EmployeeMapper;
import vip.appap.suxin.module.hr.service.employee.EmployeePostSyncService;
import vip.appap.suxin.module.system.dal.dataobject.PostDO;
import vip.appap.suxin.module.system.service.PostService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.hr.enums.ErrorCodeConstants.*;

@Service
@Validated
public class AppointmentServiceImpl implements AppointmentService {

    private static final String STATUS_ACTIVE = "active";

    @Resource
    private AppointmentMapper appointmentMapper;
    @Resource
    private EmployeeMapper employeeMapper;
    @Resource
    private PostService postService;
    @Resource
    private EmployeePostSyncService employeePostSyncService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAppointment(AppointmentSaveReqVO createReqVO) {
        AppointmentDO appointment = buildAppointment(createReqVO);
        appointmentMapper.insert(appointment);
        handleCurrentAppointment(appointment);
        return appointment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAppointment(AppointmentSaveReqVO updateReqVO) {
        validateAppointmentExists(updateReqVO.getId());
        AppointmentDO appointment = buildAppointment(updateReqVO);
        appointmentMapper.updateById(appointment);
        handleCurrentAppointment(appointment);
    }

    @Override
    public void deleteAppointment(Long id) {
        validateAppointmentExists(id);
        appointmentMapper.deleteById(id);
    }

    @Override
    public AppointmentRespVO getAppointment(Long id) {
        AppointmentRespVO resp = appointmentMapper.selectByIdJoin(id);
        if (resp == null) {
            throw exception(APPOINTMENT_NOT_EXISTS);
        }
        return resp;
    }

    @Override
    public PageResult<AppointmentRespVO> getAppointmentPage(AppointmentPageReqVO pageReqVO) {
        return appointmentMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchCreateAppointment(AppointmentBatchSaveReqVO batchReqVO) {
        int count = 0;
        for (Long partnerId : batchReqVO.getPartnerIds()) {
            AppointmentSaveReqVO req = new AppointmentSaveReqVO();
            req.setPartnerId(partnerId);
            req.setPostCategory(batchReqVO.getPostCategory());
            req.setPostLevel(batchReqVO.getPostLevel());
            req.setPostId(batchReqVO.getPostId());
            req.setStartDate(batchReqVO.getStartDate());
            req.setTermYears(batchReqVO.getTermYears());
            req.setAppointmentDoc(batchReqVO.getAppointmentDoc());
            req.setRemark(batchReqVO.getRemark());
            req.setStatus(STATUS_ACTIVE);
            req.setIsCurrent(1);
            createAppointment(req);
            count++;
        }
        return count;
    }

    @Override
    public List<AppointmentRespVO> getCurrentAppointmentList(Long partnerId) {
        return BeanUtils.toBean(appointmentMapper.selectCurrentListByPartnerId(partnerId), AppointmentRespVO.class);
    }

    private AppointmentDO buildAppointment(AppointmentSaveReqVO reqVO) {
        EmployeeDO employee = validateEmployeeExists(reqVO.getPartnerId());
        // 校验系统岗位并读取名称写入快照
        Long postId = reqVO.getPostId();
        if (postId != null) {
            postService.validatePostList(java.util.Collections.singleton(postId));
            PostDO post = postService.getPost(postId);
            reqVO.setPostName(post != null ? post.getName() : null);
        }
        AppointmentDO appointment = BeanUtils.toBean(reqVO, AppointmentDO.class);
        appointment.setEmployeeNo(employee.getEmployeeNo());
        appointment.setDept(employee.getDept());
        if (appointment.getEndDate() == null) {
            appointment.setEndDate(calcEndDate(reqVO.getStartDate(), reqVO.getTermYears()));
        }
        if (appointment.getStatus() == null || appointment.getStatus().isEmpty()) {
            appointment.setStatus(STATUS_ACTIVE);
        }
        if (appointment.getIsCurrent() == null) {
            appointment.setIsCurrent(0);
        }
        return appointment;
    }

    private void handleCurrentAppointment(AppointmentDO appointment) {
        if (appointment.getIsCurrent() != null && appointment.getIsCurrent() == 1
                && STATUS_ACTIVE.equals(appointment.getStatus())) {
            appointmentMapper.clearCurrentByPartnerId(appointment.getPartnerId());
            AppointmentDO currentFlag = new AppointmentDO();
            currentFlag.setId(appointment.getId());
            currentFlag.setIsCurrent(1);
            appointmentMapper.updateById(currentFlag);
            syncEmployeeProfile(appointment);
        }
    }

    private void syncEmployeeProfile(AppointmentDO appointment) {
        EmployeeDO employee = employeeMapper.selectProfileByPartnerId(appointment.getPartnerId());
        if (employee == null) {
            return;
        }
        EmployeeDO update = new EmployeeDO();
        update.setId(employee.getId());
        update.setPostId(appointment.getPostId());
        update.setPosition(appointment.getPostName());
        if (appointment.getStartDate() != null) {
            update.setAppointmentDate(appointment.getStartDate().toLocalDate());
        }
        employeeMapper.updateById(update);
        employeePostSyncService.syncUserPostFromEmployee(appointment.getPartnerId(), update.getPostId());
    }

    private LocalDateTime calcEndDate(LocalDateTime startDate, Integer termYears) {
        if (startDate == null || termYears == null || termYears <= 0) {
            return null;
        }
        return startDate.toLocalDate().plusYears(termYears).minusDays(1).atStartOfDay();
    }

    private AppointmentDO validateAppointmentExists(Long id) {
        AppointmentDO appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw exception(APPOINTMENT_NOT_EXISTS);
        }
        return appointment;
    }

    private EmployeeDO validateEmployeeExists(Long partnerId) {
        EmployeeDO employee = employeeMapper.selectProfileByPartnerId(partnerId);
        if (employee == null) {
            throw exception(APPOINTMENT_EMPLOYEE_NOT_EXISTS);
        }
        return employee;
    }

}
