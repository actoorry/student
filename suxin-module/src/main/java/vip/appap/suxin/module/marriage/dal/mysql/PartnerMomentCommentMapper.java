package vip.appap.suxin.module.marriage.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.marriage.dal.dataobject.PartnerMomentCommentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface PartnerMomentCommentMapper extends BaseMapperX<PartnerMomentCommentDO> {

    default PageResult<PartnerMomentCommentDO> selectRootPage(PageParam reqVO, Long momentId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PartnerMomentCommentDO>()
                .eq(PartnerMomentCommentDO::getMomentId, momentId)
                .eq(PartnerMomentCommentDO::getStatus, 1)
                .eq(PartnerMomentCommentDO::getParentId, 0L)
                .orderByDesc(PartnerMomentCommentDO::getId));
    }

    default List<PartnerMomentCommentDO> selectListByParentIds(Long momentId, Collection<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<PartnerMomentCommentDO>()
                .eq(PartnerMomentCommentDO::getMomentId, momentId)
                .eq(PartnerMomentCommentDO::getStatus, 1)
                .in(PartnerMomentCommentDO::getParentId, parentIds)
                .orderByAsc(PartnerMomentCommentDO::getId));
    }

}
