package vip.appap.suxin.module.partner.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerMemberPageReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerMemberDO;
import vip.appap.suxin.module.partner.enums.MemberStatusEnum;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PartnerMemberMapper extends BaseMapperX<PartnerMemberDO> {

    default PageResult<PartnerMemberDO> selectPage(PartnerMemberPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PartnerMemberDO>()
                .eqIfPresent(PartnerMemberDO::getUserId, reqVO.getUserId())
                .eqIfPresent(PartnerMemberDO::getMemberType, reqVO.getMemberType())
                .eqIfPresent(PartnerMemberDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(PartnerMemberDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PartnerMemberDO::getId));
    }

    default PartnerMemberDO selectByOrderItemId(Long orderItemId, Integer memberType) {
        return selectOne(new LambdaQueryWrapperX<PartnerMemberDO>()
                .eq(PartnerMemberDO::getOrderItemId, orderItemId)
                .eq(PartnerMemberDO::getMemberType, memberType));
    }

    default PartnerMemberDO selectLatestActive(Long userId, Integer memberType) {
        return selectOne(new LambdaQueryWrapperX<PartnerMemberDO>()
                .eq(PartnerMemberDO::getUserId, userId)
                .eq(PartnerMemberDO::getMemberType, memberType)
                .eq(PartnerMemberDO::getStatus, MemberStatusEnum.ACTIVE.getStatus())
                .gt(PartnerMemberDO::getEndTime, LocalDateTime.now())
                .orderByDesc(PartnerMemberDO::getEndTime)
                .last("LIMIT 1"));
    }

    default List<PartnerMemberDO> selectActiveList(Long userId) {
        return selectList(new LambdaQueryWrapperX<PartnerMemberDO>()
                .eq(PartnerMemberDO::getUserId, userId)
                .eq(PartnerMemberDO::getStatus, MemberStatusEnum.ACTIVE.getStatus())
                .gt(PartnerMemberDO::getEndTime, LocalDateTime.now())
                .orderByDesc(PartnerMemberDO::getEndTime));
    }

    default List<PartnerMemberDO> selectExpiredActiveList(Integer limit) {
        return selectList(new LambdaQueryWrapperX<PartnerMemberDO>()
                .eq(PartnerMemberDO::getStatus, MemberStatusEnum.ACTIVE.getStatus())
                .le(PartnerMemberDO::getEndTime, LocalDateTime.now())
                .orderByAsc(PartnerMemberDO::getEndTime)
                .last("LIMIT " + limit));
    }

}
