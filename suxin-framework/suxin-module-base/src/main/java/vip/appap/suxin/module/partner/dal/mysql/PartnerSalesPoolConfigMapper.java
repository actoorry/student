package vip.appap.suxin.module.partner.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSalesPoolConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户公海配置 Mapper
 *
 * @author Wanwan
 */
@Mapper
public interface PartnerSalesPoolConfigMapper extends BaseMapperX<PartnerSalesPoolConfigDO> {

    default PartnerSalesPoolConfigDO selectOne() {
        return selectOne(new LambdaQueryWrapperX<PartnerSalesPoolConfigDO>().last("LIMIT 1"));
    }

}
