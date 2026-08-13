package vip.appap.suxin.module.partner.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerAddressCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerAddressPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerAddressUpdateReqVO;
import vip.appap.suxin.module.partner.convert.PartnerAddressConvert;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerAddressDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerAddressMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.PARTNER_ADDRESS_NOT_EXISTS;

/**
 * 会员地址 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class PartnerAddressServiceImpl implements PartnerAddressService {

    @Resource
    private PartnerAddressMapper partnerAddressMapper;

    @Override
    public Long createAddress(PartnerAddressCreateReqVO createReqVO) {
        PartnerAddressDO address = PartnerAddressConvert.INSTANCE.convert(createReqVO);
        partnerAddressMapper.insert(address);
        return address.getId();
    }

    @Override
    public void updateAddress(PartnerAddressUpdateReqVO updateReqVO) {
        // 校验存在
        validateAddressExists(updateReqVO.getId());
        // 更新
        PartnerAddressDO updateObj = PartnerAddressConvert.INSTANCE.convert(updateReqVO);
        partnerAddressMapper.updateById(updateObj);
    }

    @Override
    public void deleteAddress(Long id) {
        // 校验存在
        validateAddressExists(id);
        // 删除
        partnerAddressMapper.deleteById(id);
    }

    private void validateAddressExists(Long id) {
        if (partnerAddressMapper.selectById(id) == null) {
            throw exception(PARTNER_ADDRESS_NOT_EXISTS);
        }
    }

    @Override
    public PartnerAddressDO getAddress(Long id) {
        return partnerAddressMapper.selectById(id);
    }

    @Override
    public List<PartnerAddressDO> getAddressListByUserId(Long userId) {
        return partnerAddressMapper.selectList(
                new vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX<PartnerAddressDO>()
                        .eq(PartnerAddressDO::getUserId, userId)
                        .orderByDesc(PartnerAddressDO::getDefaulted)
                        .orderByDesc(PartnerAddressDO::getId));
    }

    @Override
    public PartnerAddressDO getDefaultAddress(Long userId) {
        return partnerAddressMapper.selectOne(
                new vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX<PartnerAddressDO>()
                        .eq(PartnerAddressDO::getUserId, userId)
                        .eq(PartnerAddressDO::getDefaulted, true)
                        .last("LIMIT 1"));
    }

    @Override
    public PageResult<PartnerAddressDO> getAddressPage(PartnerAddressPageReqVO pageReqVO) {
        return partnerAddressMapper.selectPage(pageReqVO, new vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX<>());
    }

}
