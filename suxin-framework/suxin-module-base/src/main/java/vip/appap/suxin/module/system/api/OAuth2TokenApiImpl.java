package vip.appap.suxin.module.system.api;

import vip.appap.suxin.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import vip.appap.suxin.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCreateReqDTO;
import vip.appap.suxin.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenRespDTO;
import vip.appap.suxin.module.system.dal.dataobject.OAuth2AccessTokenDO;
import vip.appap.suxin.module.system.service.OAuth2TokenService;
import org.springframework.stereotype.Service;

/**
 * OAuth2.0 Token API 实现类
 *
 * @author 书心软件
 */
@Service
public class OAuth2TokenApiImpl implements OAuth2TokenCommonApi {

    private final OAuth2TokenService oauth2TokenService;

    public OAuth2TokenApiImpl(OAuth2TokenService oauth2TokenService) {
        this.oauth2TokenService = oauth2TokenService;
    }

    @Override
    public OAuth2AccessTokenRespDTO createAccessToken(OAuth2AccessTokenCreateReqDTO reqDTO) {
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.createAccessToken(
                reqDTO.getUserId(), reqDTO.getUserType(), reqDTO.getClientId(), reqDTO.getScopes());
        return BeanUtils.toBean(accessTokenDO, OAuth2AccessTokenRespDTO.class);
    }

    @Override
    public OAuth2AccessTokenCheckRespDTO checkAccessToken(String accessToken) {
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.checkAccessToken(accessToken);
        return BeanUtils.toBean(accessTokenDO, OAuth2AccessTokenCheckRespDTO.class);
    }

    @Override
    public OAuth2AccessTokenRespDTO removeAccessToken(String accessToken) {
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.removeAccessToken(accessToken);
        return BeanUtils.toBean(accessTokenDO, OAuth2AccessTokenRespDTO.class);
    }

    @Override
    public OAuth2AccessTokenRespDTO refreshAccessToken(String refreshToken, String clientId) {
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.refreshAccessToken(refreshToken, clientId);
        return BeanUtils.toBean(accessTokenDO, OAuth2AccessTokenRespDTO.class);
    }

}
