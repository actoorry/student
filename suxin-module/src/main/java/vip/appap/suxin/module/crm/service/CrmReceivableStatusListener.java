package vip.appap.suxin.module.crm.service;

// TODO bpm: 暂时移除 BPM 依赖，后续迁移到 suxin-module 后按需恢复
// import vip.appap.suxin.module.bpm.api.event.BpmProcessInstanceStatusEvent;
// import vip.appap.suxin.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import vip.appap.suxin.module.crm.service.CrmReceivableService;
import vip.appap.suxin.module.crm.service.CrmReceivableServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 回款审批的结果的监听器实现类
 *
 * @author HUIHUI
 */
@Component
public class CrmReceivableStatusListener { // TODO bpm: extends BpmProcessInstanceStatusEventListener {

    @Resource
    private CrmReceivableService receivableService;

    // TODO bpm: 暂时移除 BPM 依赖，后续迁移到 suxin-module 后按需恢复
    // @Override
    // public String getProcessDefinitionKey() {
    //     return CrmReceivableServiceImpl.BPM_PROCESS_DEFINITION_KEY;
    // }
    //
    // @Override
    // public void onEvent(BpmProcessInstanceStatusEvent event) {
    //     receivableService.updateReceivableAuditStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
    // }

}
