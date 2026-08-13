package vip.appap.suxin.module.partner.dal.mysql;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerMemberConfigDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PartnerMemberConfigMapper extends BaseMapperX<PartnerMemberConfigDO> {

    default PartnerMemberConfigDO selectEnabledByScene(String scene) {
        return selectOne(new LambdaQueryWrapperX<PartnerMemberConfigDO>()
                .eq(PartnerMemberConfigDO::getScene, scene)
                .eq(PartnerMemberConfigDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .last("LIMIT 1"));
    }

}
