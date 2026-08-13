package vip.appap.suxin.module.rongjh.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.rongjh.dal.dataobject.PartnerLoveRecordDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PartnerLoveRecordMapper extends BaseMapperX<PartnerLoveRecordDO> {

    default PartnerLoveRecordDO selectByBizKey(String bizKey) {
        return selectOne(new LambdaQueryWrapperX<PartnerLoveRecordDO>()
                .eq(PartnerLoveRecordDO::getBizKey, bizKey));
    }
}
