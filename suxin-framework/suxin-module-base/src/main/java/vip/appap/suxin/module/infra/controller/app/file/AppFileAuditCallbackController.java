package vip.appap.suxin.module.infra.controller.app.file;

import vip.appap.suxin.module.infra.service.file.FileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户 App - 文件审核回调
 *
 * 用于接收微信内容安全审核的异步回调结果
 *
 * @author 书心软件
 */
@Tag(name = "用户 App - 文件审核回调")
@RestController
@RequestMapping("/infra/file")
@Slf4j
public class AppFileAuditCallbackController {

    @Resource
    private FileService fileService;

    /**
     * 微信内容安全审核回调
     *
     * 参考文档：https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/sec-check/security.mediaCheckAsync.html
     *
     * @param body 回调消息体
     * @return success
     */
    @PostMapping("/audit/callback")
    public String auditCallback(@RequestBody Map<String, Object> body) {
        log.info("[auditCallback][收到微信审核回调：{}]", body);
        try {
            // 解析回调参数
            String event = (String) body.get("Event");
            if (!"wxa_media_check".equals(event)) {
                log.warn("[auditCallback][未知事件类型：{}]", event);
                return "success";
            }

            String traceId = (String) body.get("trace_id");
            Integer errcode = (Integer) body.get("errcode");

            // 检查错误码
            if (errcode != null && errcode != 0) {
                log.error("[auditCallback][审核回调错误：traceId({}) errcode({})]", traceId, errcode);
                // 更新文件审核状态为失败
                fileService.updateFileAuditStatusByTraceId(traceId, 3, "审核回调错误：" + errcode, null);
                return "success";
            }

            // 解析审核结果
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) body.get("result");
            if (result == null) {
                log.warn("[auditCallback][审核结果为空：traceId({})]", traceId);
                return "success";
            }

            String suggest = (String) result.get("suggest");
            Integer label = (Integer) result.get("label");

            // 根据 suggest 更新审核状态
            Integer auditStatus;
            String auditReason = null;
            if ("pass".equals(suggest)) {
                auditStatus = 2; // 审核通过
            } else if ("risky".equals(suggest)) {
                auditStatus = 3; // 审核不通过
                auditReason = "内容风险：" + getLabelName(label);
            } else if ("review".equals(suggest)) {
                auditStatus = 4; // 待复审
                auditReason = "待人工复审";
            } else {
                log.warn("[auditCallback][未知的建议类型：{}]", suggest);
                return "success";
            }

            // 更新文件审核状态
            fileService.updateFileAuditStatusByTraceId(traceId, auditStatus, auditReason, null);
            log.info("[auditCallback][审核结果处理完成：traceId({}) auditStatus({})]", traceId, auditStatus);

        } catch (Exception e) {
            log.error("[auditCallback][处理回调异常]", e);
        }
        return "success";
    }

    /**
     * 获取标签名称
     *
     * @param label 标签值
     * @return 标签名称
     */
    private String getLabelName(Integer label) {
        if (label == null) {
            return "未知";
        }
        return switch (label) {
            case 100 -> "正常";
            case 20001 -> "时政";
            case 20002 -> "色情";
            case 20006 -> "违法犯罪";
            case 21000 -> "其他";
            default -> "未知(" + label + ")";
        };
    }

}
