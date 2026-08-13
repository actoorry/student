package vip.appap.suxin.module.hr.service.appointment;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentBatchSaveReqVO;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentRespVO;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentSaveReqVO;

import java.util.List;

public interface AppointmentService {

    Long createAppointment(AppointmentSaveReqVO createReqVO);

    void updateAppointment(AppointmentSaveReqVO updateReqVO);

    void deleteAppointment(Long id);

    AppointmentRespVO getAppointment(Long id);

    PageResult<AppointmentRespVO> getAppointmentPage(AppointmentPageReqVO pageReqVO);

    int batchCreateAppointment(AppointmentBatchSaveReqVO batchReqVO);

    List<AppointmentRespVO> getCurrentAppointmentList(Long partnerId);

}
