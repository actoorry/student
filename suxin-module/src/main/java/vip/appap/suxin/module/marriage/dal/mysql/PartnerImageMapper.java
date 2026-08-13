package vip.appap.suxin.module.marriage.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.marriage.dal.dataobject.PartnerImageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface PartnerImageMapper extends BaseMapperX<PartnerImageDO> {

    default List<PartnerImageDO> selectListByProfileIds(Collection<Long> profileIds) {
        if (profileIds == null || profileIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<PartnerImageDO>()
                .in(PartnerImageDO::getProfileId, profileIds)
                .orderByAsc(PartnerImageDO::getProfileId)
                .orderByAsc(PartnerImageDO::getSortNo)
                .orderByAsc(PartnerImageDO::getId));
    }

    default List<PartnerImageDO> selectListByProfileIdsAndType(Collection<Long> profileIds, Integer type) {
        if (profileIds == null || profileIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<PartnerImageDO>()
                .in(PartnerImageDO::getProfileId, profileIds)
                .eq(PartnerImageDO::getType, type)
                .orderByAsc(PartnerImageDO::getProfileId)
                .orderByAsc(PartnerImageDO::getSortNo)
                .orderByAsc(PartnerImageDO::getId));
    }

}
