package vip.appap.suxin.module.system.convert;

import cn.hutool.core.date.LocalDateTimeUtil;
import vip.appap.suxin.framework.common.core.KeyValue;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils;
import vip.appap.suxin.module.system.controller.admin.vo.OAuth2OpenAccessTokenRespVO;
import vip.appap.suxin.module.system.controller.admin.vo.OAuth2OpenAuthorizeInfoRespVO;
import vip.appap.suxin.module.system.controller.admin.vo.OAuth2OpenCheckTokenRespVO;
import vip.appap.suxin.module.system.dal.dataobject.OAuth2AccessTokenDO;
import vip.appap.suxin.module.system.dal.dataobject.OAuth2ApproveDO;
import vip.appap.suxin.module.system.dal.dataobject.OAuth2ClientDO;
import vip.appap.suxin.module.system.util.oauth2.OAuth2Utils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
public interface OAuth2OpenConvert {

    OAuth2OpenConvert INSTANCE = Mappers.getMapper(OAuth2OpenConvert.class);

    default OAuth2OpenAccessTokenRespVO convert(OAuth2AccessTokenDO bean) {
        OAuth2OpenAccessTokenRespVO respVO = BeanUtils.toBean(bean, OAuth2OpenAccessTokenRespVO.class);
        respVO.setTokenType(SecurityFrameworkUtils.AUTHORIZATION_BEARER.toLowerCase());
        respVO.setExpiresIn(OAuth2Utils.getExpiresIn(bean.getExpiresTime()));
        respVO.setScope(OAuth2Utils.buildScopeStr(bean.getScopes()));
        return respVO;
    }

    default OAuth2OpenCheckTokenRespVO convert2(OAuth2AccessTokenDO bean) {
        OAuth2OpenCheckTokenRespVO respVO = BeanUtils.toBean(bean, OAuth2OpenCheckTokenRespVO.class);
        respVO.setExp(LocalDateTimeUtil.toEpochMilli(bean.getExpiresTime()) / 1000L);
        respVO.setUserType(UserTypeEnum.ADMIN.getValue());
        return respVO;
    }

    default OAuth2OpenAuthorizeInfoRespVO convert(OAuth2ClientDO client, List<OAuth2ApproveDO> approves) {
        // 构建 scopes
        List<KeyValue<String, Boolean>> scopes = new ArrayList<>(client.getScopes().size());
        Map<String, OAuth2ApproveDO> approveMap = CollectionUtils.convertMap(approves, OAuth2ApproveDO::getScope);
        client.getScopes().forEach(scope -> {
            OAuth2ApproveDO approve = approveMap.get(scope);
            scopes.add(new KeyValue<>(scope, approve != null ? approve.getApproved() : false));
        });
        // 拼接返回
        return new OAuth2OpenAuthorizeInfoRespVO(
                new OAuth2OpenAuthorizeInfoRespVO.Client(client.getName(), client.getLogo()), scopes);
    }

}
