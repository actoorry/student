package vip.appap.suxin.module.product.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.product.dal.dataobject.ProductDisplayConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ProductDisplayConfigMapper extends BaseMapperX<ProductDisplayConfigDO> {

    default ProductDisplayConfigDO selectEnabledBySceneCode(String sceneCode, Integer status) {
        return selectOne(new LambdaQueryWrapperX<ProductDisplayConfigDO>()
                .eq(ProductDisplayConfigDO::getSceneCode, sceneCode)
                .eq(ProductDisplayConfigDO::getStatus, status)
                .orderByAsc(ProductDisplayConfigDO::getSort)
                .orderByDesc(ProductDisplayConfigDO::getId)
                .last("LIMIT 1"));
    }

    /**
     * 根据场景编码集合查询当前租户下的原始配置行（管理后台使用）
     *
     * @param sceneCodes 场景编码集合
     * @return 原始配置行
     */
    default List<ProductDisplayConfigDO> selectListBySceneCodes(Collection<String> sceneCodes) {
        return selectList(new LambdaQueryWrapperX<ProductDisplayConfigDO>()
                .in(ProductDisplayConfigDO::getSceneCode, sceneCodes)
                .orderByAsc(ProductDisplayConfigDO::getSort)
                .orderByDesc(ProductDisplayConfigDO::getId));
    }

}
