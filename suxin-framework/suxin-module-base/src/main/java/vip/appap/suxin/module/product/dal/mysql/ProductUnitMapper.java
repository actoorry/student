package vip.appap.suxin.module.product.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.product.controller.admin.vo.ProductUnitPageReqVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductUnitDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductUnitMapper extends BaseMapperX<ProductUnitDO> {

    default PageResult<ProductUnitDO> selectPage(ProductUnitPageReqVO reqVO) {
        LambdaQueryWrapperX<ProductUnitDO> wrapper = new LambdaQueryWrapperX<ProductUnitDO>()
                .likeIfPresent(ProductUnitDO::getName, reqVO.getName())
                .eqIfPresent(ProductUnitDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ProductUnitDO::getType, reqVO.getType())
                .betweenIfPresent(ProductUnitDO::getCreateTime, reqVO.getCreateTime());
        wrapper.ne(ProductUnitDO::getType, 0)
                .isNotNull(ProductUnitDO::getRelativeFactor)
                .ne(ProductUnitDO::getRelativeFactor, "");
        wrapper.orderByDesc(ProductUnitDO::getId);
        return selectPage(reqVO, wrapper);
    }

    default ProductUnitDO selectByName(String name) {
        return selectOne(ProductUnitDO::getName, name);
    }

    default List<ProductUnitDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<ProductUnitDO>()
                .eq(ProductUnitDO::getStatus, status)
                .orderByAsc(ProductUnitDO::getId));
    }

}
