package vip.appap.suxin.module.partner.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerCertificationRecordPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerCertificationRecordWithPartnerRespVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerCertificationRecordDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface PartnerCertificationRecordMapper extends BaseMapperX<PartnerCertificationRecordDO> {

    default PageResult<PartnerCertificationRecordDO> selectPage(PartnerCertificationRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PartnerCertificationRecordDO>()
                .eqIfPresent(PartnerCertificationRecordDO::getPartnerId, reqVO.getPartnerId())
                .eqIfPresent(PartnerCertificationRecordDO::getCertType, reqVO.getCertType())
                .eqIfPresent(PartnerCertificationRecordDO::getProviderCode, reqVO.getProviderCode())
                .eqIfPresent(PartnerCertificationRecordDO::getState, reqVO.getState())
                .eqIfPresent(PartnerCertificationRecordDO::getProviderSeqNo, reqVO.getProviderSeqNo())
                .betweenIfPresent(PartnerCertificationRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PartnerCertificationRecordDO::getId));
    }

    default PartnerCertificationRecordDO selectLatestByRequestKeyAndStates(String requestKey, Collection<String> states) {
        return selectOne(new LambdaQueryWrapperX<PartnerCertificationRecordDO>()
                .eq(PartnerCertificationRecordDO::getRequestKey, requestKey)
                .in(PartnerCertificationRecordDO::getState, states)
                .orderByDesc(PartnerCertificationRecordDO::getId)
                .last("LIMIT 1"));
    }

    default PartnerCertificationRecordDO selectLatestByPartnerIdAndCertTypeAndStates(Long partnerId, String certType,
                                                                                      Collection<String> states) {
        return selectOne(new LambdaQueryWrapperX<PartnerCertificationRecordDO>()
                .eq(PartnerCertificationRecordDO::getPartnerId, partnerId)
                .eq(PartnerCertificationRecordDO::getCertType, certType)
                .in(PartnerCertificationRecordDO::getState, states)
                .orderByDesc(PartnerCertificationRecordDO::getId)
                .last("LIMIT 1"));
    }

    default PartnerCertificationRecordDO selectLatestByPartnerIdAndCertType(Long partnerId, String certType) {
        return selectOne(new LambdaQueryWrapperX<PartnerCertificationRecordDO>()
                .eq(PartnerCertificationRecordDO::getPartnerId, partnerId)
                .eq(PartnerCertificationRecordDO::getCertType, certType)
                .orderByDesc(PartnerCertificationRecordDO::getId)
                .last("LIMIT 1"));
    }

    default Long selectCountByPartnerIdAndCertTypeAndCreateTime(Long partnerId, String certType,
                                                                LocalDateTime startTime, LocalDateTime endTime) {
        return selectCount(new LambdaQueryWrapperX<PartnerCertificationRecordDO>()
                .eq(PartnerCertificationRecordDO::getPartnerId, partnerId)
                .eq(PartnerCertificationRecordDO::getCertType, certType)
                .ge(PartnerCertificationRecordDO::getCreateTime, startTime)
                .lt(PartnerCertificationRecordDO::getCreateTime, endTime));
    }

    /**
     * 批量查询指定人物、指定认证类型和状态下已认证的人物 ID 集合
     *
     * @param partnerIds 人物 ID 集合
     * @param certType   认证类型
     * @param states     有效状态集合
     * @return 已认证的人物 ID 集合（去重）；空集合直接返回空，不访问数据库
     */
    default Set<Long> selectMarriageVerifiedPartnerIds(Collection<Long> partnerIds, String certType,
                                                       Collection<String> states) {
        if (partnerIds == null || partnerIds.isEmpty() || states == null || states.isEmpty()) {
            return Collections.emptySet();
        }
        return selectObjs(new LambdaQueryWrapperX<PartnerCertificationRecordDO>()
                .in(PartnerCertificationRecordDO::getPartnerId, partnerIds)
                .eq(PartnerCertificationRecordDO::getCertType, certType)
                .in(PartnerCertificationRecordDO::getState, states)
                .select(PartnerCertificationRecordDO::getPartnerId))
                .stream()
                .filter(Objects::nonNull)
                .map(obj -> Long.valueOf(obj.toString()))
                .collect(Collectors.toSet());
    }

    /**
     * 分页查询认证记录（关联合作伙伴信息）
     */
    IPage<PartnerCertificationRecordWithPartnerRespVO> selectPageWithPartner(Page<?> page, @Param("reqVO") PartnerCertificationRecordPageReqVO reqVO);

}
