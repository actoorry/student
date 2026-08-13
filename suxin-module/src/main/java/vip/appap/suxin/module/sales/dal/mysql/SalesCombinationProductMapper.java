package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationProductPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationProductDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 拼团商品 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface SalesCombinationProductMapper extends BaseMapperX<SalesCombinationProductDO> {

    default PageResult<SalesCombinationProductDO> selectPage(SalesCombinationProductPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesCombinationProductDO>()
                .eqIfPresent(SalesCombinationProductDO::getActivityId, reqVO.getActivityId())
                .eqIfPresent(SalesCombinationProductDO::getSpuId, reqVO.getSpuId())
                .eqIfPresent(SalesCombinationProductDO::getSkuId, reqVO.getSkuId())
                .eqIfPresent(SalesCombinationProductDO::getActivityStatus, reqVO.getActivityStatus())
                .betweenIfPresent(SalesCombinationProductDO::getActivityStartTime, reqVO.getActivityStartTime())
                .betweenIfPresent(SalesCombinationProductDO::getActivityEndTime, reqVO.getActivityEndTime())
                .eqIfPresent(SalesCombinationProductDO::getCombinationPrice, reqVO.getActivePrice())
                .betweenIfPresent(SalesCombinationProductDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SalesCombinationProductDO::getId));
    }

    default List<SalesCombinationProductDO> selectListByActivityIds(Collection<Long> ids) {
        return selectList(SalesCombinationProductDO::getActivityId, ids);
    }

}
