package vip.appap.suxin.module.crm.service;

// TODO bpm: 暂时移除 BPM 依赖，后续迁移到 suxin-module 后按需恢复
// import vip.appap.suxin.module.bpm.api.event.BpmProcessInstanceStatusEvent;
// import vip.appap.suxin.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import vip.appap.suxin.module.crm.service.CrmContractService;
import vip.appap.suxin.module.crm.service.CrmContractServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 合同审批的结果的监听器实现类
 *
 * @author HUIHUI
 */
@Component
public class CrmContractStatusListener { // TODO bpm: extends BpmProcessInstanceStatusEventListener {

    @Resource
    private CrmContractService contractService;

    // TODO bpm: 暂时移除 BPM 依赖，后续迁移到 suxin-module 后按需恢复
    // @Override
    // public String getProcessDefinitionKey() {
    //     return CrmContractServiceImpl.BPM_PROCESS_DEFINITION_KEY;
    // }
    //
    // @Override
    // protected void onEvent(BpmProcessInstanceStatusEvent event) {
    //     contractService.updateContractAuditStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
    // }

}
