package vip.appap.suxin.module.accountant.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import vip.appap.suxin.framework.tenant.core.aop.TenantIgnore;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.accountant.controller.app.vo.AppWechatVirtualPayResultRespVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppWechatVirtualPayCheckoutProfileRespVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppWechatVirtualPaySubmitReqVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppWechatVirtualPaySubmitRespVO;
import vip.appap.suxin.module.accountant.enums.ClientPlatformEnum;
import vip.appap.suxin.module.accountant.service.WechatVirtualPayService;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.servlet.ServletUtils.getClientIP;
import static vip.appap.suxin.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

import jakarta.annotation.security.PermitAll;
import java.util.Map;

@Tag(name = "用户 APP - 微信虚拟支付")
@RestController
@RequestMapping("/accountant/wechat-virtual-pay")
@Validated
@Slf4j
public class AppWechatVirtualPayController {

    @Resource
    private WechatVirtualPayService wechatVirtualPayService;

    @PostMapping("/submit")
    @Operation(summary = "提交微信虚拟支付")
    public CommonResult<AppWechatVirtualPaySubmitRespVO> submitWechatVirtualPay(
            @RequestHeader(value = "X-Client-Platform", required = false) String clientPlatform,
            @RequestHeader(value = "X-App-Version", required = false) String appVersion,
            @Valid @RequestBody AppWechatVirtualPaySubmitReqVO reqVO) {
        validateMiniappContext(clientPlatform, appVersion);
        return success(wechatVirtualPayService.submitWechatVirtualPay(getLoginUserId(), reqVO, getClientIP()));
    }

    @GetMapping("/result")
    @Operation(summary = "查询微信虚拟支付结果")
    public CommonResult<AppWechatVirtualPayResultRespVO> getWechatVirtualPayResult(@RequestParam("payOrderId") Long payOrderId) {
        return success(wechatVirtualPayService.getWechatVirtualPayResult(getLoginUserId(), payOrderId));
    }

    @GetMapping("/checkout-profile")
    @Operation(summary = "查询微信虚拟支付收银台画像")
    public CommonResult<AppWechatVirtualPayCheckoutProfileRespVO> getWechatVirtualPayCheckoutProfile(
            @RequestHeader(value = "X-Client-Platform", required = false) String clientPlatform,
            @RequestHeader(value = "X-App-Version", required = false) String appVersion,
            @RequestParam("payOrderId") Long payOrderId) {
        validateMiniappContext(clientPlatform, appVersion);
        return success(wechatVirtualPayService.getWechatVirtualPayCheckoutProfile(getLoginUserId(), payOrderId));
    }

    private void validateMiniappContext(String clientPlatform, String appVersion) {
        ClientPlatformEnum.normalizeVersion(appVersion);
        if (ClientPlatformEnum.normalize(clientPlatform) != ClientPlatformEnum.MP_WEIXIN) {
            throw exception(vip.appap.suxin.module.accountant.enums.ErrorCodeConstants
                    .WECHAT_VIRTUAL_PAY_MINIAPP_CONTEXT_REQUIRED);
        }
    }

    @PostMapping("/notify/goods-deliver")
    @Operation(summary = "微信虚拟支付道具发货回调")
    @PermitAll
    @TenantIgnore
    public ResponseEntity<?> notifyGoodsDeliver(@RequestBody String body,
                                                @RequestHeader(value = "Content-Type", required = false) String contentType) {
        boolean xml = contentType != null && contentType.toLowerCase().contains("xml")
                || body != null && body.trim().startsWith("<");
        Map<String, Object> result;
        try {
            result = wechatVirtualPayService.notifyGoodsDeliver(body, xml);
        } catch (Exception ex) {
            log.warn("[notifyGoodsDeliver][微信虚拟支付发货回调处理失败]", ex);
            result = Map.of("ErrCode", 1, "ErrMsg", "fail");
        }
        if (xml) {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML)
                    .body(toWechatCallbackXml(result));
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/notify/complaint")
    @Operation(summary = "微信虚拟支付投诉回调")
    @PermitAll
    @TenantIgnore
    public Map<String, Object> notifyComplaint(@RequestBody Map<String, Object> body) {
        return wechatVirtualPayService.notifyComplaint(body);
    }

    private String toWechatCallbackXml(Map<String, Object> result) {
        return "<xml><ErrCode>" + result.get("ErrCode") + "</ErrCode><ErrMsg>"
                + result.get("ErrMsg") + "</ErrMsg></xml>";
    }

}
