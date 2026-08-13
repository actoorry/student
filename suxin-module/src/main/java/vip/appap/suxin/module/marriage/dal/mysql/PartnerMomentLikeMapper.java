package vip.appap.suxin.module.marriage.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.marriage.dal.dataobject.PartnerMomentLikeDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface PartnerMomentLikeMapper extends BaseMapperX<PartnerMomentLikeDO> {

    default PartnerMomentLikeDO selectByMomentIdAndPartnerId(Long momentId, Long partnerId) {
        return selectOne(new LambdaQueryWrapperX<PartnerMomentLikeDO>()
                .eq(PartnerMomentLikeDO::getMomentId, momentId)
                .eq(PartnerMomentLikeDO::getPartnerId, partnerId));
    }

    default List<PartnerMomentLikeDO> selectListByMomentIdsAndPartnerId(Collection<Long> momentIds, Long partnerId) {
        if (momentIds == null || momentIds.isEmpty() || partnerId == null) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<PartnerMomentLikeDO>()
                .in(PartnerMomentLikeDO::getMomentId, momentIds)
                .eq(PartnerMomentLikeDO::getPartnerId, partnerId));
    }

    @Delete("DELETE FROM partner_moment_like WHERE id = #{id} AND tenant_id = #{tenantId}")
    int deleteByIdPhysically(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Delete("DELETE FROM partner_moment_like WHERE moment_id = #{momentId} " +
            "AND partner_id = #{partnerId} AND tenant_id = #{tenantId}")
    int deleteByMomentIdAndPartnerIdPhysically(@Param("momentId") Long momentId,
                                               @Param("partnerId") Long partnerId,
                                               @Param("tenantId") Long tenantId);

}
