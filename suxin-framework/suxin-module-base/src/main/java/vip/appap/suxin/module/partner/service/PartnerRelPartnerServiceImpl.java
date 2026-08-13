package vip.appap.suxin.module.partner.service;

import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerRelPartnerDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerRelPartnerMapper;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Validated
public class PartnerRelPartnerServiceImpl implements PartnerRelPartnerService {

    @Resource
    private PartnerRelPartnerMapper partnerRelPartnerMapper;

    @Override
    public PartnerRelPartnerDO getPartnerRelPartner(Long partnerId, Long relPartnerId, String type) {
        return partnerRelPartnerMapper.selectByPartnerIdAndRelPartnerIdAndType(partnerId, relPartnerId, type);
    }

    @Override
    public PartnerRelPartnerDO createPartnerRelPartner(Long partnerId, Long relPartnerId, String type) {
        if (partnerRelPartnerMapper.resurrectDeletedRelation(partnerId, relPartnerId, type) > 0) {
            return partnerRelPartnerMapper.selectByPartnerIdAndRelPartnerIdAndType(partnerId, relPartnerId, type);
        }
        PartnerRelPartnerDO relation = PartnerRelPartnerDO.builder()
                .partnerId(partnerId)
                .relPartnerId(relPartnerId)
                .type(type)
                .build();
        try {
            partnerRelPartnerMapper.insert(relation);
        } catch (DuplicateKeyException e) {
            PartnerRelPartnerDO existing = partnerRelPartnerMapper.selectByPartnerIdAndRelPartnerIdAndType(
                    partnerId, relPartnerId, type);
            if (existing != null) {
                return existing;
            }
            if (partnerRelPartnerMapper.resurrectDeletedRelation(partnerId, relPartnerId, type) > 0) {
                return partnerRelPartnerMapper.selectByPartnerIdAndRelPartnerIdAndType(partnerId, relPartnerId, type);
            }
            throw e;
        }
        return relation;
    }

    @Override
    public PageResult<PartnerRelPartnerDO> getPartnerRelPartnerPage(Long partnerId, String type, PageParam pageParam,
                                                                    boolean reverse) {
        return partnerRelPartnerMapper.selectPageByPartnerIdAndType(pageParam, partnerId, type, reverse);
    }

    @Override
    public Long getPartnerRelPartnerCountByPartnerId(Long partnerId, String type) {
        return partnerRelPartnerMapper.selectCountByPartnerIdAndType(partnerId, type);
    }

    @Override
    public Long getPartnerRelPartnerCountByRelPartnerId(Long relPartnerId, String type) {
        return partnerRelPartnerMapper.selectCountByRelPartnerIdAndType(relPartnerId, type);
    }

    @Override
    public List<PartnerRelPartnerDO> getPartnerRelPartnerListByTypeAndCreateTimeBetween(String type,
                                                                                        LocalDateTime startTime,
                                                                                        LocalDateTime endTime) {
        return partnerRelPartnerMapper.selectListByTypeAndCreateTimeBetween(type, startTime, endTime);
    }

    @Override
    public void deletePartnerRelPartner(Long partnerId, Long relPartnerId, String type) {
        partnerRelPartnerMapper.deleteByPartnerIdAndRelPartnerIdAndType(partnerId, relPartnerId, type);
    }

}
