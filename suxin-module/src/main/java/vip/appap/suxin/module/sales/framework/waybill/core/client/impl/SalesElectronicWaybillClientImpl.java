package vip.appap.suxin.module.sales.framework.waybill.core.client.impl;

import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.util.json.JsonUtils;
import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillAccountDO;
import vip.appap.suxin.module.sales.framework.waybill.config.SalesElectronicWaybillProperties;
import vip.appap.suxin.module.sales.framework.waybill.core.client.SalesElectronicWaybillClient;
import vip.appap.suxin.module.sales.framework.waybill.core.client.dto.*;
import vip.appap.suxin.module.sales.framework.waybill.core.util.SalesElectronicWaybillSignUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.WAYBILL_ORDER_API_ERROR;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.WAYBILL_ORDER_RESULT_UNKNOWN;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.WAYBILL_ORDER_CREATE_FAILED;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.WAYBILL_ORDER_CANCEL_FAIL;

/**
 * 快递100电子面单 V2 客户端实现
 * <p>
 * 基于官方 Java Demo 的 {@code LabelV2}/{@code OrderReq}/{@code PrintReq}/{@code LabelCancel} 协议：
 * 以表单提交 key、method、t、sign、param，签名公式 MD5(param + t + key + secret) 转大写。
 * 日志与错误摘要不得包含密钥、完整地址、手机号、签名或原始面单内容。
 *
 * @author 书心软件
 */
@Slf4j
@AllArgsConstructor
public class SalesElectronicWaybillClientImpl implements SalesElectronicWaybillClient {

    private static final String METHOD_ORDER = "order";
    private static final String METHOD_PRINT_OLD = "printOld";
    private static final String METHOD_CANCEL = "cancel";
    private static final Pattern MOBILE_PATTERN = Pattern.compile("(?<!\\d)(1\\d{2})\\d{4}(\\d{4})(?!\\d)");

    private final RestTemplate restTemplate;
    private final SalesElectronicWaybillProperties properties;

    @Override
    public SalesElectronicWaybillOrderRespDTO order(SalesElectronicWaybillAccountDO account, SalesElectronicWaybillOrderReqDTO reqDTO) {
        Map<String, Object> paramMap = buildOrderParam(account, reqDTO);
        // 幂等：以业务订单号作为 orderId，reorder=false 避免网络重试造成重复下单
        paramMap.put("reorder", false);
        SalesElectronicWaybillResultDTO<SalesElectronicWaybillOrderRespDTO> result =
                doFormRequest(properties.getOrderUrl(), METHOD_ORDER, account, paramMap,
                        new TypeReference<SalesElectronicWaybillResultDTO<SalesElectronicWaybillOrderRespDTO>>() {}.getType());
        return handleOrderResult(result, true);
    }

    @Override
    public SalesElectronicWaybillOrderRespDTO reprint(SalesElectronicWaybillAccountDO account, SalesElectronicWaybillReprintReqDTO reqDTO) {
        Map<String, Object> paramMap = new LinkedHashMap<>();
        paramMap.put("taskId", reqDTO.getTaskId());
        SalesElectronicWaybillResultDTO<SalesElectronicWaybillOrderRespDTO> result =
                doFormRequest(properties.getOrderUrl(), METHOD_PRINT_OLD, account, paramMap,
                        new TypeReference<SalesElectronicWaybillResultDTO<SalesElectronicWaybillOrderRespDTO>>() {}.getType());
        // 复打响应可能不返回运单号（仅返回可打印内容），不强制校验运单号
        return handleOrderResult(result, false);
    }

    @Override
    public SalesElectronicWaybillCancelRespDTO cancel(SalesElectronicWaybillAccountDO account, SalesElectronicWaybillCancelReqDTO reqDTO) {
        Map<String, Object> paramMap = buildCancelParam(account, reqDTO);
        SalesElectronicWaybillCancelRespDTO respDTO = doFormRequest(properties.getCancelUrl(), METHOD_CANCEL, account,
                paramMap, new TypeReference<SalesElectronicWaybillCancelRespDTO>() {}.getType());
        log.debug("[cancel][账户({}) 运单号({}) 取消响应 returnCode({}) result({})]",
                account.getId(), reqDTO.getKuaidinum(), respDTO == null ? null : respDTO.getReturnCode(),
                respDTO == null ? null : respDTO.getResult());
        if (respDTO == null || !respDTO.isSuccess()) {
            throw exception(WAYBILL_ORDER_CANCEL_FAIL, respDTO == null ? "空响应" : sanitize(respDTO.getMessage()));
        }
        return respDTO;
    }

    // ========== 下单/复打结果处理 ==========

    private SalesElectronicWaybillOrderRespDTO handleOrderResult(SalesElectronicWaybillResultDTO<SalesElectronicWaybillOrderRespDTO> result,
                                                                 boolean requireWaybillNo) {
        if (result == null) {
            throw exception(WAYBILL_ORDER_CREATE_FAILED, "空响应");
        }
        // 业务失败：记录脱敏错误码/消息
        if (!result.isSuccess()) {
            String message = sanitize(result.getMessage());
            log.warn("[waybill][下单失败 code({}) message({})]", result.getCode(), message);
            throw exception(WAYBILL_ORDER_CREATE_FAILED, message);
        }
        // 下单成功但缺少运单号：视为失败，避免写入空运单号（复打不要求运单号）
        if (requireWaybillNo && (result.getData() == null || StrUtil.isBlank(result.getData().getKuaidinum()))) {
            log.warn("[waybill][下单成功但运单号为空 code({})]", result.getCode());
            throw exception(WAYBILL_ORDER_CREATE_FAILED, "未返回运单号");
        }
        return result.getData();
    }

    // ========== 参数构建 ==========

    private Map<String, Object> buildOrderParam(SalesElectronicWaybillAccountDO account, SalesElectronicWaybillOrderReqDTO reqDTO) {
        Map<String, Object> paramMap = new LinkedHashMap<>();
        paramMap.put("recMan", toManInfoMap(reqDTO.getRecMan()));
        paramMap.put("sendMan", toManInfoMap(reqDTO.getSendMan()));
        paramMap.put("kuaidicom", reqDTO.getKuaidicom());
        paramMap.put("orderId", reqDTO.getOrderId());
        paramMap.put("payType", reqDTO.getPayType());
        if (reqDTO.getCount() != null) {
            paramMap.put("count", reqDTO.getCount());
        }
        paramMap.put("tempId", reqDTO.getTempId());
        paramMap.put("printType", reqDTO.getPrintType());
        if (StrUtil.isNotBlank(reqDTO.getRemark())) {
            paramMap.put("remark", reqDTO.getRemark());
        }
        if (StrUtil.isNotBlank(reqDTO.getCargo())) {
            paramMap.put("cargo", reqDTO.getCargo());
        }
        // 承运商账户字段（从账户读取，内存中为解密后的明文）
        paramMap.put("partnerId", account.getPartnerId());
        paramMap.put("partnerKey", account.getPartnerKey());
        paramMap.put("partnerSecret", account.getPartnerSecret());
        if (StrUtil.isNotBlank(account.getNet())) {
            paramMap.put("net", account.getNet());
        }
        if (StrUtil.isNotBlank(account.getCode())) {
            paramMap.put("code", account.getCode());
        }
        if (StrUtil.isNotBlank(account.getPartnerName())) {
            paramMap.put("partnerName", account.getPartnerName());
        }
        if (StrUtil.isNotBlank(account.getCheckMan())) {
            paramMap.put("checkMan", account.getCheckMan());
        }
        if (StrUtil.isNotBlank(account.getExpType())) {
            paramMap.put("expType", account.getExpType());
        }
        return paramMap;
    }

    private Map<String, Object> toManInfoMap(SalesElectronicWaybillManInfoDTO manInfo) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (manInfo == null) {
            return map;
        }
        map.put("name", manInfo.getName());
        if (StrUtil.isNotBlank(manInfo.getMobile())) {
            map.put("mobile", manInfo.getMobile());
        }
        if (StrUtil.isNotBlank(manInfo.getTel())) {
            map.put("tel", manInfo.getTel());
        }
        // printAddr 与 province/city/district/addr 二选一，优先完整地址
        if (StrUtil.isNotBlank(manInfo.getPrintAddr())) {
            map.put("printAddr", manInfo.getPrintAddr());
        } else {
            map.put("province", manInfo.getProvince());
            map.put("city", manInfo.getCity());
            map.put("district", manInfo.getDistrict());
            map.put("addr", manInfo.getAddr());
        }
        return map;
    }

    private Map<String, Object> buildCancelParam(SalesElectronicWaybillAccountDO account, SalesElectronicWaybillCancelReqDTO reqDTO) {
        Map<String, Object> paramMap = new LinkedHashMap<>();
        paramMap.put("partnerId", account.getPartnerId());
        paramMap.put("partnerKey", account.getPartnerKey());
        paramMap.put("partnerSecret", account.getPartnerSecret());
        if (StrUtil.isNotBlank(account.getNet())) {
            paramMap.put("net", account.getNet());
        }
        if (StrUtil.isNotBlank(account.getCode())) {
            paramMap.put("code", account.getCode());
        }
        if (StrUtil.isNotBlank(account.getPartnerName())) {
            paramMap.put("partnerName", account.getPartnerName());
        }
        if (StrUtil.isNotBlank(account.getCheckMan())) {
            paramMap.put("checkMan", account.getCheckMan());
        }
        if (StrUtil.isNotBlank(account.getExpType())) {
            paramMap.put("expType", account.getExpType());
        }
        paramMap.put("kuaidicom", reqDTO.getKuaidicom());
        paramMap.put("kuaidinum", reqDTO.getKuaidinum());
        if (StrUtil.isNotBlank(reqDTO.getOrderId())) {
            paramMap.put("orderId", reqDTO.getOrderId());
        }
        if (StrUtil.isNotBlank(reqDTO.getReason())) {
            paramMap.put("reason", reqDTO.getReason());
        }
        return paramMap;
    }

    // ========== 表单请求 ==========

    /**
     * 提交表单并解析响应
     *
     * @param respType 响应类型（支持泛型，如 Result<OrderResult>）
     */
    private <T> T doFormRequest(String url, String method, SalesElectronicWaybillAccountDO account,
                                Map<String, Object> paramMap, Type respType) {
        String param = JsonUtils.toJsonString(paramMap);
        String t = String.valueOf(System.currentTimeMillis());
        String sign = SalesElectronicWaybillSignUtil.printSign(param, t, account.getKey(), account.getSecret());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("key", account.getKey());
        requestBody.add("method", method);
        requestBody.add("t", t);
        requestBody.add("sign", sign);
        requestBody.add("param", param);

        log.debug("[waybill][method({}) 提交表单，仅记录脱敏摘要 key({})]",
                method, vip.appap.suxin.module.sales.service.SalesElectronicWaybillMaskUtil.logSafe(account.getKey()));
        ResponseEntity<String> responseEntity;
        try {
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        } catch (RestClientException e) {
            // 请求可能已经被第三方受理，不能将超时/断连误判成明确失败
            log.warn("[waybill][method({}) 调用异常]", method, e);
            throw exception(WAYBILL_ORDER_RESULT_UNKNOWN);
        }
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            log.warn("[waybill][method({}) 非 2xx 响应 status({})]", method, responseEntity.getStatusCode());
            throw exception(WAYBILL_ORDER_API_ERROR);
        }
        if (StrUtil.isBlank(responseEntity.getBody())) {
            log.warn("[waybill][method({}) 空响应体，结果未知]", method);
            throw exception(WAYBILL_ORDER_RESULT_UNKNOWN);
        }
        try {
            return JsonUtils.parseObject(responseEntity.getBody(), respType);
        } catch (RuntimeException e) {
            log.warn("[waybill][method({}) 响应无法解析，结果未知]", method, e);
            throw exception(WAYBILL_ORDER_RESULT_UNKNOWN);
        }
    }

    /**
     * 脱敏第三方错误消息：截断长度，避免原始地址/手机号进入日志或持久化
     */
    private String sanitize(String message) {
        if (StrUtil.isBlank(message)) {
            return "";
        }
        String value = MOBILE_PATTERN.matcher(message.trim()).replaceAll("$1****$2");
        return value.length() > 128 ? value.substring(0, 128) + "..." : value;
    }

}
