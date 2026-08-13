package vip.appap.suxin.module.crm.util;

import cn.hutool.core.lang.Assert;
// TODO bpm: 暂时移除 BPM 依赖，后续迁移到 suxin-module 后按需恢复
// import vip.appap.suxin.module.bpm.enums.task.BpmTaskStatusEnum;
import vip.appap.suxin.module.crm.enums.CrmAuditStatusEnum;

/**
 * CRM 流程工具类
 *
 * @author HUIHUI
 */
public class CrmAuditStatusUtils {

    // TODO bpm: BPM 审批状态常量（原从 BpmTaskStatusEnum 获取）
    private static final int BPM_APPROVE = 2;  // BpmTaskStatusEnum.APPROVE
    private static final int BPM_REJECT = 3;   // BpmTaskStatusEnum.REJECT
    private static final int BPM_CANCEL = 4;   // BpmTaskStatusEnum.CANCEL

    /**
     * BPM 审批结果转换
     *
     * @param bpmResult BPM 审批结果
     */
    public static Integer convertBpmResultToAuditStatus(Integer bpmResult) {
        Integer auditStatus = Integer.valueOf(BPM_APPROVE).equals(bpmResult) ? CrmAuditStatusEnum.APPROVE.getStatus()
                : Integer.valueOf(BPM_REJECT).equals(bpmResult) ? CrmAuditStatusEnum.REJECT.getStatus()
                : Integer.valueOf(BPM_CANCEL).equals(bpmResult) ? CrmAuditStatusEnum.CANCEL.getStatus() : null;
        Assert.notNull(auditStatus, "BPM 审批结果({}) 转换失败", bpmResult);
        return auditStatus;
    }

}
