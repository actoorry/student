package vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin;

import org.junit.jupiter.api.Test;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.module.system.service.SocialClientService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WxVirtualPayAccessTokenUtilsTest {

    private final SocialClientService socialClientService = mock(SocialClientService.class);

    @Test
    void execute_invalidAccessToken_refreshesAndRetriesOnce() {
        when(socialClientService.getWxMaAccessToken(UserTypeEnum.MEMBER.getValue()))
                .thenReturn("expired-token");
        when(socialClientService.getWxMaAccessToken(UserTypeEnum.MEMBER.getValue(), true))
                .thenReturn("fresh-token");
        List<String> usedTokens = new ArrayList<>();

        String result = WxVirtualPayAccessTokenUtils.execute(socialClientService, token -> {
            usedTokens.add(token);
            if ("expired-token".equals(token)) {
                throw new WxVirtualPayApiException(40001, "invalid credential");
            }
            return "success";
        });

        assertEquals("success", result);
        assertEquals(List.of("expired-token", "fresh-token"), usedTokens);
        verify(socialClientService).getWxMaAccessToken(UserTypeEnum.MEMBER.getValue(), true);
    }

    @Test
    void execute_nonCredentialError_doesNotRefreshOrRetry() {
        when(socialClientService.getWxMaAccessToken(UserTypeEnum.MEMBER.getValue()))
                .thenReturn("current-token");
        List<String> usedTokens = new ArrayList<>();

        WxVirtualPayApiException exception = assertThrows(WxVirtualPayApiException.class,
                () -> WxVirtualPayAccessTokenUtils.execute(socialClientService, token -> {
                    usedTokens.add(token);
                    throw new WxVirtualPayApiException(268490003, "签名错误");
                }));

        assertEquals(268490003, exception.getErrcode());
        assertEquals(List.of("current-token"), usedTokens);
        verify(socialClientService, never()).getWxMaAccessToken(UserTypeEnum.MEMBER.getValue(), true);
    }

    @Test
    void execute_refreshedTokenStillInvalid_doesNotRetryAgain() {
        when(socialClientService.getWxMaAccessToken(UserTypeEnum.MEMBER.getValue()))
                .thenReturn("expired-token");
        when(socialClientService.getWxMaAccessToken(UserTypeEnum.MEMBER.getValue(), true))
                .thenReturn("also-invalid-token");
        List<String> usedTokens = new ArrayList<>();

        WxVirtualPayApiException exception = assertThrows(WxVirtualPayApiException.class,
                () -> WxVirtualPayAccessTokenUtils.execute(socialClientService, token -> {
                    usedTokens.add(token);
                    throw new WxVirtualPayApiException(40001, "invalid credential");
                }));

        assertEquals(40001, exception.getErrcode());
        assertEquals(List.of("expired-token", "also-invalid-token"), usedTokens);
        verify(socialClientService).getWxMaAccessToken(UserTypeEnum.MEMBER.getValue(), true);
    }

}
