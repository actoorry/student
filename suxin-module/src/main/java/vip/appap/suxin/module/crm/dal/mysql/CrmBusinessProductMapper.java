package vip.appap.suxin.module.crm.dal.mysql;


import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.crm.dal.dataobject.CrmBusinessProductDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商机产品 Mapper
 *
 * @author lzxhqs
 */
@Mapper
public interface CrmBusinessProductMapper extends BaseMapperX<CrmBusinessProductDO> {

    default List<CrmBusinessProductDO> selectListByBusinessId(Long businessId) {
        return selectList(CrmBusinessProductDO::getBusinessId, businessId);
    }

}
