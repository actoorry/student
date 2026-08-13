package vip.appap.suxin.framework.ratelimiter.core.keyresolver.impl;

import cn.hutool.crypto.SecureUtil;
import vip.appap.suxin.framework.common.util.string.StrUtils;
import vip.appap.suxin.framework.ratelimiter.core.annotation.NaturalRateLimit;
import vip.appap.suxin.framework.ratelimiter.core.keyresolver.NaturalRateLimiterKeyResolver;
import vip.appap.suxin.framework.web.core.util.WebFrameworkUtils;
import org.aspectj.lang.JoinPoint;

/**
 * 用户级别的自然时间段限流 Key 解析器
 *
 * 使用方法名 + userId + userType，组装成一个 Key
 * 为了避免 Key 过长，使用 MD5 进行"压缩"
 *
 * @author 书心软件
 */
public class UserNaturalRateLimiterKeyResolver implements NaturalRateLimiterKeyResolver {

    @Override
    public String resolver(JoinPoint joinPoint, NaturalRateLimit naturalRateLimit) {
        String methodName = joinPoint.getSignature().toString();
        Long userId = WebFrameworkUtils.getLoginUserId();
        Integer userType = WebFrameworkUtils.getLoginUserType();
        return SecureUtil.md5(methodName + userId + userType);
    }

}
