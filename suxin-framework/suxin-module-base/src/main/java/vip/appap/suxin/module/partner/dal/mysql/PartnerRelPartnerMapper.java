package vip.appap.suxin.module.partner.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerRelPartnerDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PartnerRelPartnerMapper extends BaseMapperX<PartnerRelPartnerDO> {

    default PartnerRelPartnerDO selectByPartnerIdAndRelPartnerIdAndType(Long partnerId, Long relPartnerId,
                                                                        String type) {
        return selectOne(new LambdaQueryWrapperX<PartnerRelPartnerDO>()
                .eq(PartnerRelPartnerDO::getPartnerId, partnerId)
                .eq(PartnerRelPartnerDO::getRelPartnerId, relPartnerId)
                .eq(PartnerRelPartnerDO::getType, type));
    }

    default List<PartnerRelPartnerDO> selectListByPartnerIdAndType(Long partnerId, String type) {
        if (partnerId == null) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<PartnerRelPartnerDO>()
                .eq(PartnerRelPartnerDO::getPartnerId, partnerId)
                .eq(PartnerRelPartnerDO::getType, type));
    }

    default List<PartnerRelPartnerDO> selectListByPartnerIdAndRelPartnerIdsAndType(Long partnerId,
                                                                                   Collection<Long> relPartnerIds,
                                                                                   String type) {
        if (partnerId == null || relPartnerIds == null || relPartnerIds.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<PartnerRelPartnerDO>()
                .eq(PartnerRelPartnerDO::getPartnerId, partnerId)
                .in(PartnerRelPartnerDO::getRelPartnerId, relPartnerIds)
                .eq(PartnerRelPartnerDO::getType, type));
    }

    default PageResult<PartnerRelPartnerDO> selectPageByPartnerIdAndType(PageParam pageParam, Long partnerId,
                                                                         String type, boolean reverse) {
        LambdaQueryWrapperX<PartnerRelPartnerDO> query = new LambdaQueryWrapperX<>();
        query.eq(PartnerRelPartnerDO::getType, type);
        if (reverse) {
            query.eq(PartnerRelPartnerDO::getRelPartnerId, partnerId);
        } else {
            query.eq(PartnerRelPartnerDO::getPartnerId, partnerId);
        }
        query.orderByDesc(PartnerRelPartnerDO::getCreateTime);
        return selectPage(pageParam, query);
    }

    default List<PartnerRelPartnerDO> selectListByTypeAndCreateTimeBetween(String type, LocalDateTime startTime,
                                                                           LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<PartnerRelPartnerDO>()
                .eq(PartnerRelPartnerDO::getType, type)
                .geIfPresent(PartnerRelPartnerDO::getCreateTime, startTime)
                .ltIfPresent(PartnerRelPartnerDO::getCreateTime, endTime));
    }

    default Long selectCountByPartnerIdAndType(Long partnerId, String type) {
        return selectCount(new LambdaQueryWrapperX<PartnerRelPartnerDO>()
                .eq(PartnerRelPartnerDO::getPartnerId, partnerId)
                .eq(PartnerRelPartnerDO::getType, type));
    }

    default Long selectCountByRelPartnerIdAndType(Long relPartnerId, String type) {
        return selectCount(new LambdaQueryWrapperX<PartnerRelPartnerDO>()
                .eq(PartnerRelPartnerDO::getRelPartnerId, relPartnerId)
                .eq(PartnerRelPartnerDO::getType, type));
    }

    default int deleteByPartnerIdAndRelPartnerIdAndType(Long partnerId, Long relPartnerId, String type) {
        return delete(new LambdaQueryWrapperX<PartnerRelPartnerDO>()
                .eq(PartnerRelPartnerDO::getPartnerId, partnerId)
                .eq(PartnerRelPartnerDO::getRelPartnerId, relPartnerId)
                .eq(PartnerRelPartnerDO::getType, type));
    }

    @Update("UPDATE partner_rel_partner SET deleted = 0, update_time = NOW() " +
            "WHERE partner_id = #{partnerId} AND rel_partner_id = #{relPartnerId} " +
            "AND type = #{type} AND deleted = 1")
    int resurrectDeletedRelation(@Param("partnerId") Long partnerId,
                                 @Param("relPartnerId") Long relPartnerId,
                                 @Param("type") String type);

}
