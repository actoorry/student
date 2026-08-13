package vip.appap.suxin.module.partner.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerCertificationRecordPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerCertificationRecordWithPartnerRespVO;
import vip.appap.suxin.module.partner.controller.app.vo.*;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerCertificationRecordDO;

import java.util.Collection;
import java.util.Set;

public interface PartnerCertificationRecordService {

    PartnerNameCheckRespVO nameCheck(Long partnerId, PartnerNameCheckReqVO reqVO);

    PartnerMarriageCheckRespVO marriageCheck(Long partnerId, PartnerMarriageCheckReqVO reqVO);

    PartnerMarriageCheckStatusRespVO getMarriageCheckStatus(Long partnerId);

    PartnerCertificationRecordDO getPartnerCertificationRecord(Long id);

    PageResult<PartnerCertificationRecordDO> getPartnerCertificationRecordPage(PartnerCertificationRecordPageReqVO pageReqVO);

    /**
     * 分页查询认证记录（含合作伙伴信息和状态标签）
     */
    PageResult<PartnerCertificationRecordWithPartnerRespVO> getPartnerCertificationRecordWithPartnerPage(PartnerCertificationRecordPageReqVO pageReqVO);

    Integer getRealNameVerifiedStatus(Long partnerId);

    Integer getMarriageVerifiedStatus(Long partnerId);

    /**
     * 批量查询指定人物中已完成婚恋认证的人物 ID 集合
     *
     * @param partnerIds 人物 ID 集合
     * @return 已完成婚恋认证的人物 ID 集合（去重）
     */
    Set<Long> getMarriageVerifiedPartnerIds(Collection<Long> partnerIds);

}
