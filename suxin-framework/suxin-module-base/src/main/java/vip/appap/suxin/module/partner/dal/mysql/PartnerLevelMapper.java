package vip.appap.suxin.module.partner.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerLevelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 会员等级 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface PartnerLevelMapper extends BaseMapperX<PartnerLevelDO> {

    default List<PartnerLevelDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<PartnerLevelDO>()
                .eqIfPresent(PartnerLevelDO::getStatus, status));
    }

}
