package vip.appap.suxin.module.partner.service;

import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerRelPartnerDO;

import java.time.LocalDateTime;
import java.util.List;

public interface PartnerRelPartnerService {

    PartnerRelPartnerDO getPartnerRelPartner(Long partnerId, Long relPartnerId, String type);

    PartnerRelPartnerDO createPartnerRelPartner(Long partnerId, Long relPartnerId, String type);

    PageResult<PartnerRelPartnerDO> getPartnerRelPartnerPage(Long partnerId, String type, PageParam pageParam,
                                                             boolean reverse);

    Long getPartnerRelPartnerCountByPartnerId(Long partnerId, String type);

    Long getPartnerRelPartnerCountByRelPartnerId(Long relPartnerId, String type);

    List<PartnerRelPartnerDO> getPartnerRelPartnerListByTypeAndCreateTimeBetween(String type,
                                                                                 LocalDateTime startTime,
                                                                                 LocalDateTime endTime);

    void deletePartnerRelPartner(Long partnerId, Long relPartnerId, String type);

}
