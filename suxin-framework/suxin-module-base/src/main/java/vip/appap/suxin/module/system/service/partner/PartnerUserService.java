package vip.appap.suxin.module.system.service.partner;

/**
 * 合作伙伴用户 Service 接口
 * <p>
 * 通过反射调用 PartnerApi，避免 system 对 partner 模块的编译期依赖
 *
 * @author 书心软件
 */
public interface PartnerUserService {

    /**
     * 获得合作伙伴用户的手机号码
     *
     * @param id 合作伙伴用户编号
     * @return 手机号码
     */
    String getMemberUserMobile(Long id);

    /**
     * 获得合作伙伴用户的邮箱
     *
     * @param id 合作伙伴用户编号
     * @return 邮箱
     */
    String getMemberUserEmail(Long id);

}
