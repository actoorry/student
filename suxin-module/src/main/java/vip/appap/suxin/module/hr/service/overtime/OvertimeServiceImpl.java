package vip.appap.suxin.module.hr.service.overtime;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import vip.appap.suxin.module.hr.controller.admin.overtime.vo.*;
import vip.appap.suxin.module.hr.dal.dataobject.overtime.OvertimeDO;
import vip.appap.suxin.module.hr.dal.dataobject.employee.EmployeeDO;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;

import vip.appap.suxin.module.hr.dal.mysql.overtime.OvertimeMapper;
import vip.appap.suxin.module.hr.dal.mysql.employee.EmployeeMapper;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.hr.enums.ErrorCodeConstants.*;

/**
 * 加班登记 Service 实现类
 *
 * 说明：加班只写入 hr_overtime 表，不双写 partner 表。
 * 创建/更新时校验关联员工存在，并自动回填 employee_no、dept（员工所属科室）。
 * duration_hours 由后端按 start_time/end_time 计算；end_time 必须晚于 start_time（允许跨日）。
 *
 * @author admin
 */
@Service
@Validated
public class OvertimeServiceImpl implements OvertimeService {

    @Resource
    private OvertimeMapper overtimeMapper;
    @Resource
    private EmployeeMapper employeeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOvertime(OvertimeSaveReqVO createReqVO) {
        validateTimeRange(createReqVO);
        validateHolidayName(createReqVO);
        EmployeeDO employee = validateEmployeeExists(createReqVO.getPartnerId());
        OvertimeDO overtime = BeanUtils.toBean(createReqVO, OvertimeDO.class);
        overtime.setEmployeeNo(employee.getEmployeeNo());
        overtime.setDept(employee.getDept());
        overtime.setDurationHours(calcDurationHours(createReqVO.getStartTime(), createReqVO.getEndTime()));
        overtimeMapper.insert(overtime);
        return overtime.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOvertime(OvertimeSaveReqVO updateReqVO) {
        validateOvertimeExists(updateReqVO.getId());
        validateTimeRange(updateReqVO);
        validateHolidayName(updateReqVO);
        EmployeeDO employee = validateEmployeeExists(updateReqVO.getPartnerId());
        OvertimeDO updateObj = BeanUtils.toBean(updateReqVO, OvertimeDO.class);
        updateObj.setEmployeeNo(employee.getEmployeeNo());
        updateObj.setDept(employee.getDept());
        updateObj.setDurationHours(calcDurationHours(updateReqVO.getStartTime(), updateReqVO.getEndTime()));
        overtimeMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOvertime(Long id) {
        validateOvertimeExists(id);
        overtimeMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOvertimeListByIds(List<Long> ids) {
        overtimeMapper.deleteByIds(ids);
    }

    private OvertimeDO validateOvertimeExists(Long id) {
        OvertimeDO overtime = overtimeMapper.selectById(id);
        if (overtime == null) {
            throw exception(OVERTIME_NOT_EXISTS);
        }
        return overtime;
    }

    private EmployeeDO validateEmployeeExists(Long partnerId) {
        EmployeeDO employee = employeeMapper.selectProfileByPartnerId(partnerId);
        if (employee == null) {
            throw exception(OVERTIME_EMPLOYEE_NOT_EXISTS);
        }
        return employee;
    }

    /** end_time 必须晚于 start_time（允许跨日，如夜班 22:00~次日06:00） */
    private void validateTimeRange(OvertimeSaveReqVO reqVO) {
        if (reqVO.getStartTime() == null || reqVO.getEndTime() == null) {
            return;
        }
        if (!reqVO.getEndTime().isAfter(reqVO.getStartTime())) {
            throw exception(OVERTIME_TIME_INVALID);
        }
    }

    /** 法定节假日值班必须填写节假日名称（兼容旧值 holiday_duty） */
    private void validateHolidayName(OvertimeSaveReqVO reqVO) {
        String type = reqVO.getOvertimeType();
        if (("legal_holiday_duty".equals(type) || "holiday_duty".equals(type))
                && (reqVO.getHolidayName() == null || reqVO.getHolidayName().trim().isEmpty())) {
            throw exception(OVERTIME_HOLIDAY_NAME_REQUIRED);
        }
    }

    /** 时长 = (end - start) 小时，保留 1 位小数 */
    private BigDecimal calcDurationHours(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return null;
        }
        long minutes = Duration.between(startTime, endTime).toMinutes();
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP);
    }

    @Override
    public OvertimeRespVO getOvertime(Long id) {
        OvertimeRespVO overtime = overtimeMapper.selectByIdJoin(id);
        if (overtime == null) {
            throw exception(OVERTIME_NOT_EXISTS);
        }
        return overtime;
    }

    @Override
    public PageResult<OvertimeRespVO> getOvertimePage(OvertimePageReqVO pageReqVO) {
        return overtimeMapper.selectPage(pageReqVO);
    }

}
