package vip.appap.suxin.module.system.framework.operatelog.core;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.service.PartnerService;
import vip.appap.suxin.module.system.dal.dataobject.AdminUserDO;
import vip.appap.suxin.module.system.service.AdminUserService;
import com.mzt.logapi.service.IParseFunction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 管理员名字的 {@link IParseFunction} 实现类
 *
 * @author HUIHUI
 */
@Slf4j
@Component
public class AdminUserParseFunction implements IParseFunction {

    public static final String NAME = "getAdminUserById";

    @Resource
    private AdminUserService adminUserService;
    @Resource
    private PartnerService partnerUserService;

    @Override
    public String functionName() {
        return NAME;
    }

    @Override
    public String apply(Object value) {
        if (StrUtil.isEmptyIfStr(value)) {
            return "";
        }

        // 获取用户信息
        Long userId = Convert.toLong(value);
        AdminUserDO user = adminUserService.getUser(userId);
        if (user == null) {
            log.warn("[apply][获取用户{{}}为空", value);
            return "";
        }
        // 从 partner 获取昵称和手机号
        PartnerDO partner = partnerUserService.getPartner(userId);
        String nickname = partner != null ? partner.getNickname() : user.getUsername();
        if (partner == null || StrUtil.isEmpty(partner.getMobile())) {
            return nickname;
        }
        return StrUtil.format("{}({})", nickname, partner.getMobile());
    }

}
