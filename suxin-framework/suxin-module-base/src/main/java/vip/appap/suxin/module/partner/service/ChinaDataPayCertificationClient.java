package vip.appap.suxin.module.partner.service;

import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.util.json.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static vip.appap.suxin.module.partner.enums.PartnerCertificationConstants.CHINA_DATA_PAY_SUCCESS_CODE;
import static vip.appap.suxin.module.partner.enums.PartnerCertificationConstants.MARRIAGE_STATE_DIVORCED;
import static vip.appap.suxin.module.partner.enums.PartnerCertificationConstants.MARRIAGE_STATE_MARRIED;
import static vip.appap.suxin.module.partner.enums.PartnerCertificationConstants.MARRIAGE_STATE_UNMARRIED;
import static vip.appap.suxin.module.partner.enums.PartnerCertificationConstants.REAL_NAME_STATE_EXCEPTION;
import static vip.appap.suxin.module.partner.enums.PartnerCertificationConstants.REAL_NAME_STATE_NOT_PASS;
import static vip.appap.suxin.module.partner.enums.PartnerCertificationConstants.REAL_NAME_STATE_PASS;

/**
 * 数据宝认证客户端
 */
@Component
@Slf4j
public class ChinaDataPayCertificationClient {

    private static final String REAL_NAME_API_URL = "https://api.chinadatapay.com/communication/personal/1979";
    private static final String REAL_MARRIAGE_API_URL = "https://api.chinadatapay.com/communication/personal/10149";

    @Value("${suxin.partner.name-check.api-key:}")
    private String nameCheckApiKey;

    @Value("${suxin.partner.single-check.api-key:}")
    private String singleCheckApiKey;

    public PartnerCertificationCallResult realNameCheck(String name, String idCard, String mobile) throws Exception {
        if (StrUtil.isBlank(nameCheckApiKey)) {
            log.warn("[realNameCheck] provider credential is not configured");
            return failure("CONFIG_MISSING", "实名认证服务暂不可用", REAL_NAME_STATE_EXCEPTION);
        }

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("key", nameCheckApiKey));
        params.add(new BasicNameValuePair("name", name));
        params.add(new BasicNameValuePair("idcard", idCard));
        params.add(new BasicNameValuePair("mobile", mobile));
        return executeFormPost(REAL_NAME_API_URL, params, REAL_NAME_STATE_EXCEPTION);
    }

    public PartnerCertificationCallResult marriageCheck(String name, String idCard) throws Exception {
        if (StrUtil.isBlank(singleCheckApiKey)) {
            log.warn("[marriageCheck] provider credential is not configured");
            return failure("CONFIG_MISSING", "婚姻认证服务暂不可用", null);
        }

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("key", singleCheckApiKey));
        params.add(new BasicNameValuePair("name", name));
        params.add(new BasicNameValuePair("certNum", idCard));
        return executeFormPost(REAL_MARRIAGE_API_URL, params, null);
    }

    public boolean isRealNameCharged(PartnerCertificationCallResult result) {
        return CHINA_DATA_PAY_SUCCESS_CODE.equals(result.getCode())
                && (REAL_NAME_STATE_PASS.equals(result.getState()) || REAL_NAME_STATE_NOT_PASS.equals(result.getState()));
    }

    public boolean isMarriageCharged(PartnerCertificationCallResult result) {
        return CHINA_DATA_PAY_SUCCESS_CODE.equals(result.getCode())
                && (MARRIAGE_STATE_MARRIED.equals(result.getState())
                || MARRIAGE_STATE_UNMARRIED.equals(result.getState())
                || MARRIAGE_STATE_DIVORCED.equals(result.getState()));
    }

    protected PartnerCertificationCallResult executeFormPost(String url, List<NameValuePair> params,
                                                             String fallbackState) throws Exception {
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(30000)
                .setConnectTimeout(30000)
                .build();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(url);
            request.setConfig(requestConfig);
            request.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            HttpResponse response = client.execute(request);
            String responseBody = response.getEntity() == null ? "" : EntityUtils.toString(response.getEntity(), "UTF-8");
            if (response.getStatusLine().getStatusCode() != 200) {
                return PartnerCertificationCallResult.builder()
                        .code(String.valueOf(response.getStatusLine().getStatusCode()))
                        .message("HTTP 请求失败")
                        .state(fallbackState)
                        .responseBody(responseBody)
                        .build();
            }
            return parseResult(responseBody);
        }
    }

    protected PartnerCertificationCallResult parseResult(String responseBody) {
        try {
            JsonNode rootNode = JsonUtils.parseTree(responseBody);
            JsonNode dataNode = rootNode.path("data");
            String state = dataNode.isObject() ? dataNode.path("state").asText(null) : null;
            return PartnerCertificationCallResult.builder()
                    .code(rootNode.path("code").asText())
                    .message(rootNode.path("message").asText())
                    .state(state)
                    .providerSeqNo(rootNode.path("seqNo").asText(null))
                    .responseBody(responseBody)
                    .build();
        } catch (Exception e) {
            log.warn("[parseResult] provider response parsing failed: {}", e.getClass().getSimpleName());
            return PartnerCertificationCallResult.builder()
                    .code("PARSE_ERROR")
                    .message("认证服务响应异常")
                    .responseBody(responseBody)
                    .build();
        }
    }

    private PartnerCertificationCallResult failure(String code, String message, String state) {
        return PartnerCertificationCallResult.builder().code(code).message(message).state(state).build();
    }

}
