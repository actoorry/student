package vip.appap.suxin.module.partner.api;

import vip.appap.suxin.module.partner.api.dto.PartnerAddressRespDTO;

import java.util.List;

public interface PartnerAddressApi {

    PartnerAddressRespDTO getAddress(Long addressId, Long userId);

    /**
     * 按编号读取地址（租户隔离，由租户拦截器保证当前租户可见性）
     *
     * 用于电子面单等运营能力读取商户寄件地址，不依赖具体用户所有权
     *
     * @param addressId 地址编号
     * @return 地址；不存在或不属于当前租户时返回 null
     */
    PartnerAddressRespDTO getAddress(Long addressId);

    /**
     * 获得当前租户商户寄件地址列表（type=1，租户隔离）
     *
     * 供电子面单账户配置等运营能力选择寄件地址
     *
     * @return 商户寄件地址列表
     */
    List<PartnerAddressRespDTO> getSenderAddressList();

    PartnerAddressRespDTO getDefaultAddress(Long userId);
}
