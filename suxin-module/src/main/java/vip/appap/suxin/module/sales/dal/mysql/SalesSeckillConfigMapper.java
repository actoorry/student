package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesSeckillConfigPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesSeckillConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SalesSeckillConfigMapper extends BaseMapperX<SalesSeckillConfigDO> {

    default PageResult<SalesSeckillConfigDO> selectPage(SalesSeckillConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesSeckillConfigDO>()
                .likeIfPresent(SalesSeckillConfigDO::getName, reqVO.getName())
                .eqIfPresent(SalesSeckillConfigDO::getStatus, reqVO.getStatus())
                .orderByAsc(SalesSeckillConfigDO::getStartTime));
    }

    default List<SalesSeckillConfigDO> selectListByStatus(Integer status) {
        return selectList(SalesSeckillConfigDO::getStatus, status);
    }

}
