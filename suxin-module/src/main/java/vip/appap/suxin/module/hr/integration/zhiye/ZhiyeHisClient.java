package vip.appap.suxin.module.hr.integration.zhiye;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.hr.enums.ErrorCodeConstants.ZHIYE_SYNC_FAILED;

/**
 * 智业 HIS SOAP HTTP 客户端
 */
@Slf4j
@Component
public class ZhiyeHisClient {

    private static final String SOAP_ACTION = "http://www.zysoft.com.cn/CallInterface";

    private final ZhiyeHisProperties properties;
    private final RestTemplate restTemplate;

    public ZhiyeHisClient(ZhiyeHisProperties properties, RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeoutMs());
        factory.setReadTimeout(properties.getReadTimeoutMs());
        this.restTemplate = restTemplateBuilder
                .requestFactory(() -> factory)
                .setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .build();
    }

    /**
     * 推送 Register / Update 到 Common-service
     */
    public void pushUpdate(String soapRequest) {
        doPost(properties.getUpdateUrl(), soapRequest);
    }

    private void doPost(String url, String soapRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "xml", StandardCharsets.UTF_8));
        headers.add("SOAPAction", SOAP_ACTION);
        HttpEntity<String> requestEntity = new HttpEntity<>(soapRequest, headers);
        try {
            log.info("[ZhiyeHisClient][request url={} SOAPAction={} bodyLength={}]",
                    url, SOAP_ACTION, soapRequest != null ? soapRequest.length() : 0);
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            String body = response.getBody();
            log.info("[ZhiyeHisClient][response status={} bodyLength={}]",
                    response.getStatusCode(), body != null ? body.length() : 0);
            try {
                validateResponse(body);
            } catch (RuntimeException ex) {
                log.warn("[ZhiyeHisClient][ACK 校验失败 reason={}]", ex.getMessage());
                log.warn("[ZhiyeHisClient][request body]\n{}", soapRequest);
                log.warn("[ZhiyeHisClient][response body]\n{}", body);
                throw ex;
            }
            log.info("[ZhiyeHisClient][ACK 校验通过 typeCode=AA]");
        } catch (RestClientException ex) {
            log.error("[ZhiyeHisClient][HTTP 调用失败 url={}]", url, ex);
            throw exception(ZHIYE_SYNC_FAILED, "网络请求失败: " + ex.getMessage());
        }
    }

    /**
     * 解析智业 ACK：typeCode=AA 成功，AE 失败
     */
    private void validateResponse(String body) {
        if (StrUtil.isBlank(body)) {
            throw exception(ZHIYE_SYNC_FAILED, "智业返回空响应");
        }
        String plainError = extractPlainTextError(body);
        if (StrUtil.isNotBlank(plainError)) {
            throw exception(ZHIYE_SYNC_FAILED, plainError);
        }
        if (isWsdlDocument(body)) {
            throw exception(ZHIYE_SYNC_FAILED, "智业返回 WSDL 文档而非业务 ACK，请检查 SOAP 报文格式");
        }
        if (containsSoapFault(body)) {
            String fault = extractSoapFaultString(body);
            throw exception(ZHIYE_SYNC_FAILED, StrUtil.isNotBlank(fault) ? fault : "智业 SOAP Fault");
        }
        if (containsAckType(body, "AE")) {
            String detail = extractAckText(body);
            throw exception(ZHIYE_SYNC_FAILED, StrUtil.isNotBlank(detail) ? detail : "智业返回 AE 业务失败");
        }
        if (!containsAckType(body, "AA")) {
            String hint = extractResponseHint(body);
            throw exception(ZHIYE_SYNC_FAILED, StrUtil.isNotBlank(hint)
                    ? "智业响应未包含 AA：" + hint
                    : "智业响应未包含 AA 成功标识");
        }
    }

    /** 智业网关非 HL7 的纯文本错误，如 error:系统拒绝了你的访问 */
    private static String extractPlainTextError(String body) {
        String trimmed = body.trim();
        if (trimmed.startsWith("<")) {
            return "";
        }
        if (trimmed.startsWith("error:") || trimmed.startsWith("error：")) {
            return trimmed.substring(6).trim();
        }
        if (trimmed.contains("系统拒绝") || trimmed.contains("请联系管理员")) {
            return trimmed;
        }
        return "";
    }

    private static boolean isWsdlDocument(String body) {
        String lower = body.toLowerCase();
        return lower.contains("wsdl:definitions") || lower.contains(":definitions");
    }

    private static boolean containsSoapFault(String body) {
        String lower = body.toLowerCase();
        return lower.contains(":fault>") || lower.contains("<fault>");
    }

    private static String extractSoapFaultString(String body) {
        String marker = "faultstring";
        int idx = body.toLowerCase().indexOf(marker);
        if (idx < 0) {
            return "";
        }
        int gt = body.indexOf('>', idx);
        int lt = body.indexOf('<', gt + 1);
        if (gt >= 0 && lt > gt) {
            return body.substring(gt + 1, lt).trim();
        }
        return "";
    }

    private static String extractResponseHint(String body) {
        String ackText = extractAckText(body);
        if (StrUtil.isNotBlank(ackText)) {
            return ackText;
        }
        return truncateForLog(body, 200);
    }

    private static String truncateForLog(String body, int maxLen) {
        if (body == null) {
            return "";
        }
        String normalized = body.replaceAll("\\s+", " ").trim();
        return normalized.length() <= maxLen ? normalized : normalized.substring(0, maxLen) + "...";
    }

    private static boolean containsAckType(String body, String typeCode) {
        String upper = body.toUpperCase();
        String code = typeCode.toUpperCase();
        return upper.contains("TYPECODE=\"" + code + "\"")
                || upper.contains("TYPECODE='" + code + "'")
                || upper.contains("TYPECODE=" + code);
    }

    private static String extractAckText(String body) {
        int idx = body.indexOf("<text value=\"");
        if (idx >= 0) {
            int start = idx + "<text value=\"".length();
            int end = body.indexOf('"', start);
            if (end > start) {
                return body.substring(start, end);
            }
        }
        return "";
    }

}
