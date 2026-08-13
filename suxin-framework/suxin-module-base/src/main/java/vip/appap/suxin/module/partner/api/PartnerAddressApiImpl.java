package vip.appap.suxin.module.partner.api;

import vip.appap.suxin.module.partner.api.dto.PartnerAddressRespDTO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerAddressDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerAddressMapper;
import vip.appap.suxin.module.partner.enums.PartnerAddressTypeEnum;
import vip.appap.suxin.module.partner.service.PartnerAddressService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartnerAddressApiImpl implements PartnerAddressApi {

    @Resource
    private PartnerAddressService partnerAddressService;
    @Resource
    private PartnerAddressMapper partnerAddressMapper;

    @Override
    public PartnerAddressRespDTO getAddress(Long addressId, Long userId) {
        PartnerAddressDO address = partnerAddressService.getAddress(addressId);
        if (address == null) {
            return null;
        }
        return convert(address);
    }

    @Override
    public PartnerAddressRespDTO getAddress(Long addressId) {
        PartnerAddressDO address = partnerAddressService.getAddress(addressId);
        if (address == null) {
            return null;
        }
        return convert(address);
    }

    @Override
    public PartnerAddressRespDTO getDefaultAddress(Long userId) {
        PartnerAddressDO address = partnerAddressService.getDefaultAddress(userId);
        if (address == null) {
            return null;
        }
        return convert(address);
    }

    @Override
    public List<PartnerAddressRespDTO> getSenderAddressList() {
        return partnerAddressMapper.selectListByType(PartnerAddressTypeEnum.SENDER.getType())
                .stream().map(this::convert).toList();
    }

    private PartnerAddressRespDTO convert(PartnerAddressDO address) {
        PartnerAddressRespDTO respDTO = new PartnerAddressRespDTO();
        respDTO.setId(address.getId());
        respDTO.setUserId(address.getUserId());
        respDTO.setType(address.getType());
        respDTO.setName(address.getName());
        respDTO.setMobile(address.getMobile());
        respDTO.setAreaId(address.getAreaId() != null ? address.getAreaId().intValue() : null);
        respDTO.setDetailAddress(address.getDetailAddress());
        return respDTO;
    }

}
