package vip.appap.suxin.module.system.service.partner;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 合作伙伴用户 Service 实现类
 * <p>
 * 通过反射调用 PartnerApi，避免 system 对 partner 模块的编译期依赖
 *
 * @author 书心软件
 */
@Service
public class PartnerUserServiceImpl implements PartnerUserService {

    @Value("${suxin.info.base-package}")
    private String basePackage;

    private volatile Object partnerApi;

    @Override
    public String getMemberUserMobile(Long id) {
        Object user = getPartnerUser(id);
        if (user == null) {
            return null;
        }
        return ReflectUtil.invoke(user, "getMobile");
    }

    @Override
    public String getMemberUserEmail(Long id) {
        Object user = getPartnerUser(id);
        if (user == null) {
            return null;
        }
        return ReflectUtil.invoke(user, "getEmail");
    }

    private Object getPartnerUser(Long id) {
        if (id == null) {
            return null;
        }
        return ReflectUtil.invoke(getPartnerApi(), "getPartner", id);
    }

    private Object getPartnerApi() {
        if (partnerApi == null) {
            partnerApi = SpringUtil.getBean(ClassUtil.loadClass(String.format("%s.module.partner.api.PartnerApi", basePackage)));
        }
        return partnerApi;
    }

}
