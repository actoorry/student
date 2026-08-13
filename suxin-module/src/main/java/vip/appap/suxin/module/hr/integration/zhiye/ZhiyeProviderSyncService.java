package vip.appap.suxin.module.hr.integration.zhiye;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vip.appap.suxin.module.hr.controller.admin.employee.vo.EmployeeRespVO;
import vip.appap.suxin.module.hr.dal.mysql.employee.EmployeeMapper;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.hr.enums.ErrorCodeConstants.EMPLOYEE_NOT_EXISTS;
import static vip.appap.suxin.module.hr.enums.ErrorCodeConstants.ZHIYE_SYNC_FAILED;

/**
 * 智业 HIS 医疗卫生人员同步
 */
@Slf4j
@Service
public class ZhiyeProviderSyncService {

    private static final String METHOD_REGISTER = "ProviderInfoRegister";

    @Resource
    private ZhiyeHisProperties zhiyeHisProperties;
    @Resource
    private EmployeeMapper employeeMapper;
    @Resource
    private ZhiyeFieldConverter fieldConverter;
    @Resource
    private ZhiyeXmlBuilder xmlBuilder;
    @Resource
    private ZhiyeHisClient zhiyeHisClient;

    public void syncRegister(Long employeeId) {
        sync(employeeId, METHOD_REGISTER, true);
    }

    public void syncUpdate(Long employeeId) {
        // 与老 OA 花名册 save 事件一致：新增/修改均走 ProviderInfoRegister
        sync(employeeId, METHOD_REGISTER, true);
    }

    private void sync(Long employeeId, String methodName, boolean register) {
        if (!Boolean.TRUE.equals(zhiyeHisProperties.getEnabled())) {
            log.debug("[sync][智业同步未启用 employeeId={} method={}]", employeeId, methodName);
            return;
        }
        EmployeeRespVO employee = employeeMapper.selectByIdJoin(employeeId);
        if (employee == null) {
            throw exception(EMPLOYEE_NOT_EXISTS);
        }
        validateEmployeeForSync(employee, zhiyeHisProperties.getOaCompatible());

        ZhiyeSyncContext context = fieldConverter.buildContext(employee, register);
        String soapRequest = xmlBuilder.buildRequest(methodName, context);
        log.info("[sync][employeeId={} method={} employeeNo={} oaCompatible={}]",
                employeeId, methodName, employee.getEmployeeNo(), zhiyeHisProperties.getOaCompatible());
        zhiyeHisClient.pushUpdate(soapRequest);
    }

    private static void validateEmployeeForSync(EmployeeRespVO employee, Boolean oaCompatible) {
        if (StrUtil.isBlank(employee.getName())) {
            throw exception(ZHIYE_SYNC_FAILED, "姓名为空，无法同步智业");
        }
        if (StrUtil.isBlank(employee.getEmployeeNo())) {
            throw exception(ZHIYE_SYNC_FAILED, "员工工号为空，无法同步智业");
        }
        if (employee.getDept() == null) {
            throw exception(ZHIYE_SYNC_FAILED, "部门为空，无法同步智业");
        }
        if (!Boolean.TRUE.equals(oaCompatible) && StrUtil.isBlank(employee.getDeptName())) {
            throw exception(ZHIYE_SYNC_FAILED, "部门为空，无法同步智业");
        }
    }

}
