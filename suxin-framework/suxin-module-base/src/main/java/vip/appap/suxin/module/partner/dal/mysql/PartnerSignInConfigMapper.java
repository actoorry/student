package vip.appap.suxin.module.partner.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSignInConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 会员签到配置 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface PartnerSignInConfigMapper extends BaseMapperX<PartnerSignInConfigDO> {

    default List<PartnerSignInConfigDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<PartnerSignInConfigDO>()
                .eq(PartnerSignInConfigDO::getStatus, status)
                .orderByAsc(PartnerSignInConfigDO::getDay)
                .orderByAsc(PartnerSignInConfigDO::getId));
    }

}
