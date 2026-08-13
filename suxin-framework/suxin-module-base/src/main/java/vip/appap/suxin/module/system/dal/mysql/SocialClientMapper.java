package vip.appap.suxin.module.system.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.framework.tenant.core.context.TenantContextHolder;
import vip.appap.suxin.module.system.controller.admin.vo.SocialClientPageReqVO;
import vip.appap.suxin.module.system.dal.dataobject.SocialClientDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SocialClientMapper extends BaseMapperX<SocialClientDO> {

    default SocialClientDO selectBySocialTypeAndUserType(Integer socialType, Integer userType) {
        return selectOne(new LambdaQueryWrapperX<SocialClientDO>()
                .eq(SocialClientDO::getSocialType, socialType)
                .eq(SocialClientDO::getUserType, userType)
                .eq(SocialClientDO::getTenantId, TenantContextHolder.getRequiredTenantId()));
    }

    default PageResult<SocialClientDO> selectPage(SocialClientPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SocialClientDO>()
                .likeIfPresent(SocialClientDO::getName, reqVO.getName())
                .eqIfPresent(SocialClientDO::getSocialType, reqVO.getSocialType())
                .eqIfPresent(SocialClientDO::getUserType, reqVO.getUserType())
                .likeIfPresent(SocialClientDO::getClientId, reqVO.getClientId())
                .eqIfPresent(SocialClientDO::getStatus, reqVO.getStatus())
                .orderByDesc(SocialClientDO::getId));
    }

}
