package vip.appap.suxin.module.partner.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSalesLimitConfigPageReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSalesLimitConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 客户限制配置 Mapper
 *
 * @author Wanwan
 */
@Mapper
public interface PartnerSalesLimitConfigMapper extends BaseMapperX<PartnerSalesLimitConfigDO> {

    default PageResult<PartnerSalesLimitConfigDO> selectPage(PartnerSalesLimitConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PartnerSalesLimitConfigDO>()
                .eqIfPresent(PartnerSalesLimitConfigDO::getType, reqVO.getType())
                .orderByDesc(PartnerSalesLimitConfigDO::getId));
    }

    default List<PartnerSalesLimitConfigDO> selectListByTypeAndUserIdAndDeptId(
            Integer type, Long userId, Long deptId) {
        LambdaQueryWrapperX<PartnerSalesLimitConfigDO> query = new LambdaQueryWrapperX<PartnerSalesLimitConfigDO>()
                .eq(PartnerSalesLimitConfigDO::getType, type);
        query.and(w -> {
            w.apply("FIND_IN_SET({0}, user_ids) > 0", userId);
            if (deptId != null) {
                w.or().apply("FIND_IN_SET({0}, dept_ids) > 0", deptId);
            }
        });
        return selectList(query);
    }

}
