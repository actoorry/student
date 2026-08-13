package vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.module.system.service.SocialClientService;

import java.util.Objects;
import java.util.function.Function;

/**
 * 微信虚拟支付 access_token 调用工具。
 */
@Slf4j
@UtilityClass
public class WxVirtualPayAccessTokenUtils {

    private static final int INVALID_ACCESS_TOKEN_ERROR_CODE = 40001;

    /**
     * 使用当前小程序 access_token 执行请求。微信明确返回 token 无效时，强制刷新并仅重试一次。
     */
    public static <T> T execute(SocialClientService socialClientService, Function<String, T> request) {
        Integer userType = UserTypeEnum.MEMBER.getValue();
        String accessToken = socialClientService.getWxMaAccessToken(userType);
        try {
            return request.apply(accessToken);
        } catch (WxVirtualPayApiException ex) {
            if (!Objects.equals(ex.getErrcode(), INVALID_ACCESS_TOKEN_ERROR_CODE)) {
                throw ex;
            }
            log.warn("[execute][微信虚拟支付 access_token 已失效，强制刷新后重试一次]");
            String refreshedAccessToken = socialClientService.getWxMaAccessToken(userType, true);
            return request.apply(refreshedAccessToken);
        }
    }

}
