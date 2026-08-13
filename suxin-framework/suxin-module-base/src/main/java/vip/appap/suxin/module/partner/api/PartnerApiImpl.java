package vip.appap.suxin.module.partner.api;

import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.partner.convert.PartnerConvert;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import vip.appap.suxin.module.partner.service.PartnerService;

import java.util.Collection;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.USER_MOBILE_NOT_EXISTS;

/**
 * 合作伙伴 API 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class PartnerApiImpl implements PartnerApi {

    @Resource
    private PartnerService partnerService;

    @Override
    public PartnerRespDTO getPartner(Long id) {
        PartnerDO user = partnerService.getPartner(id);
        return PartnerConvert.INSTANCE.convertDTO(user);
    }

    @Override
    public List<PartnerRespDTO> getPartnerList(Collection<Long> ids) {
        return PartnerConvert.INSTANCE.convertDTOList(partnerService.getPartnerList(ids));
    }

    @Override
    public List<PartnerRespDTO> getPartnerListByNickname(String nickname) {
        return PartnerConvert.INSTANCE.convertDTOList(partnerService.getPartnerListByNickname(nickname));
    }

    @Override
    public PartnerRespDTO getPartnerByMobile(String mobile) {
        return PartnerConvert.INSTANCE.convertDTO(partnerService.getPartnerByMobile(mobile));
    }

    @Override
    public void validatePartner(Long id) {
        PartnerDO user = partnerService.getPartner(id);
        if (user == null) {
            throw exception(USER_MOBILE_NOT_EXISTS);
        }
    }

}
