package vip.appap.suxin.module.partner.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerAddressCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerAddressPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerAddressUpdateReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerAddressDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 会员地址 Service 接口
 *
 * @author 书心软件
 */
public interface PartnerAddressService {

    /**
     * 创建会员地址
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAddress(@Valid PartnerAddressCreateReqVO createReqVO);

    /**
     * 更新会员地址
     *
     * @param updateReqVO 更新信息
     */
    void updateAddress(@Valid PartnerAddressUpdateReqVO updateReqVO);

    /**
     * 删除会员地址
     *
     * @param id 编号
     */
    void deleteAddress(Long id);

    /**
     * 获得会员地址
     *
     * @param id 编号
     * @return 会员地址
     */
    PartnerAddressDO getAddress(Long id);

    /**
     * 获得用户地址列表
     *
     * @param userId 用户编号
     * @return 地址列表
     */
    List<PartnerAddressDO> getAddressListByUserId(Long userId);

    /**
     * 获得用户默认地址
     *
     * @param userId 用户编号
     * @return 默认地址
     */
    PartnerAddressDO getDefaultAddress(Long userId);

    /**
     * 获得会员地址分页
     *
     * @param pageReqVO 分页查询
     * @return 会员地址分页
     */
    PageResult<PartnerAddressDO> getAddressPage(PartnerAddressPageReqVO pageReqVO);

}
