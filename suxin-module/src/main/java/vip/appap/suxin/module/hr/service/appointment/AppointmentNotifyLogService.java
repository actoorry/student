package vip.appap.suxin.module.hr.service.appointment;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentNotifyLogPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentNotifyLogRespVO;

public interface AppointmentNotifyLogService {

    PageResult<AppointmentNotifyLogRespVO> getNotifyLogPage(AppointmentNotifyLogPageReqVO pageReqVO);

}
