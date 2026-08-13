package vip.appap.suxin.module.partner.api;

import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 合作伙伴 API 接口
 */
public interface PartnerApi {

    /**
     * 获得合作伙伴用户信息
     *
     * @param id 用户编号
     * @return 用户信息
     */
    PartnerRespDTO getPartner(Long id);

    /**
     * 获得合作伙伴用户信息列表
     *
     * @param ids 用户编号集合
     * @return 用户信息列表
     */
    List<PartnerRespDTO> getPartnerList(Collection<Long> ids);

    /**
     * 获得合作伙伴用户 Map
     *
     * @param ids 用户编号集合
     * @return 用户 Map，key 为用户编号
     */
    default Map<Long, PartnerRespDTO> getPartnerMap(Collection<Long> ids) {
        List<PartnerRespDTO> list = getPartnerList(ids);
        return convertMap(list, PartnerRespDTO::getId);
    }

    /**
     * 获得用户信息（兼容 trade 模块调用）
     */
    default PartnerRespDTO getUser(Long userId) {
        return getPartner(userId);
    }

    /**
     * 获得用户 Map（兼容 trade/promotion 模块调用）
     */
    default Map<Long, PartnerRespDTO> getUserMap(Collection<Long> ids) {
        return getPartnerMap(ids);
    }

    /**
     * 获得用户列表（兼容 trade 模块调用）
     */
    default List<PartnerRespDTO> getUserList(Collection<Long> ids) {
        return getPartnerList(ids);
    }

    /**
     * 校验用户是否存在（兼容 trade 模块调用）
     */
    default void validateUser(Long id) {
        validatePartner(id);
    }

    /**
     * 基于用户昵称模糊匹配用户列表（兼容 trade 模块调用）
     */
    default List<PartnerRespDTO> getUserListByNickname(String nickname) {
        return getPartnerListByNickname(nickname);
    }

    /**
     * 基于手机号精准匹配用户（兼容 trade 模块调用）
     */
    default PartnerRespDTO getUserByMobile(String mobile) {
        return getPartnerByMobile(mobile);
    }

    /**
     * 基于用户昵称模糊匹配用户列表
     *
     * @param nickname 用户昵称（模糊匹配）
     * @return 用户信息列表
     */
    List<PartnerRespDTO> getPartnerListByNickname(String nickname);

    /**
     * 基于手机号精准匹配用户
     *
     * @param mobile 手机号
     * @return 用户信息
     */
    PartnerRespDTO getPartnerByMobile(String mobile);

    /**
     * 校验用户是否存在
     *
     * @param id 用户编号
     */
    void validatePartner(Long id);

}
