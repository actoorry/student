package vip.appap.suxin.module.hr.service.appointment;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentNotifyLogPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentNotifyLogRespVO;
import vip.appap.suxin.module.hr.dal.mysql.appointment.AppointmentNotifyLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AppointmentNotifyLogServiceImpl implements AppointmentNotifyLogService {

    @Resource
    private AppointmentNotifyLogMapper notifyLogMapper;

    @Override
    public PageResult<AppointmentNotifyLogRespVO> getNotifyLogPage(AppointmentNotifyLogPageReqVO pageReqVO) {
        return notifyLogMapper.selectPage(pageReqVO);
    }

}
