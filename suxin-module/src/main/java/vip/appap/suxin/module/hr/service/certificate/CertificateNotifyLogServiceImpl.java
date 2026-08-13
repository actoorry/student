package vip.appap.suxin.module.hr.service.certificate;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificateNotifyLogPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificateNotifyLogRespVO;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificateRespVO;
import vip.appap.suxin.module.hr.dal.dataobject.certificate.CertificateNotifyLogDO;
import vip.appap.suxin.module.hr.dal.mysql.certificate.CertificateMapper;
import vip.appap.suxin.module.hr.dal.mysql.certificate.CertificateNotifyLogMapper;
import vip.appap.suxin.module.system.service.NotifySendService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.hr.enums.ErrorCodeConstants.*;

/**
 * 证书推送日志 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
@Slf4j
public class CertificateNotifyLogServiceImpl implements CertificateNotifyLogService {

    @Resource
    private CertificateNotifyLogMapper notifyLogMapper;
    @Resource
    private CertificateMapper certificateMapper;
    @Resource
    private NotifySendService notifySendService;

    @Override
    public PageResult<CertificateNotifyLogRespVO> getNotifyLogPage(CertificateNotifyLogPageReqVO reqVO) {
        return notifyLogMapper.selectPage(reqVO);
    }

    @Override
    public void retryNotifyLog(Long id) {
        // 1. 校验日志存在且为失败状态
        CertificateNotifyLogDO logDO = notifyLogMapper.selectById(id);
        if (logDO == null) {
            throw exception(CERTIFICATE_NOTIFY_LOG_NOT_EXISTS);
        }
        if (!"failed".equals(logDO.getPushStatus())) {
            throw exception(CERTIFICATE_NOTIFY_LOG_NOT_RETRYABLE);
        }
        // 2. 加载证书并重建模板参数
        CertificateRespVO cert = certificateMapper.selectByIdJoin(logDO.getCertificateId());
        if (cert == null) {
            throw exception(CERTIFICATE_NOT_EXISTS);
        }
        String templateCode = CertificateNotifyContentBuilder.templateCode(logDO.getNotifyType());
        Map<String, Object> params = CertificateNotifyContentBuilder.buildParams(cert, logDO.getNotifyType());
        String snapshot = CertificateNotifyContentBuilder.snapshot(logDO.getNotifyType(), cert);
        // 3. 重发站内信并更新日志状态
        try {
            Long messageId = notifySendService.sendSingleNotifyToAdmin(logDO.getTargetUserId(), templateCode, params);
            if (messageId == null) {
                notifyLogMapper.updatePushResult(id, "skipped", null, snapshot, "模板已关闭或未发送");
            } else {
                notifyLogMapper.updatePushResult(id, "success", messageId, snapshot, null);
            }
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            notifyLogMapper.updatePushResult(id, "failed", null, snapshot, errorMsg);
            log.error("[retryNotifyLog][补推失败 logId={}]", id, e);
            throw exception(CERTIFICATE_NOTIFY_LOG_NOT_RETRYABLE);
        }
    }

}
