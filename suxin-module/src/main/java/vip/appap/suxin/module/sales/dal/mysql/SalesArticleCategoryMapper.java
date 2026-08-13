package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesArticleCategoryPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesArticleCategoryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 文章分类 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface SalesArticleCategoryMapper extends BaseMapperX<SalesArticleCategoryDO> {

    default PageResult<SalesArticleCategoryDO> selectPage(SalesArticleCategoryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesArticleCategoryDO>()
                .likeIfPresent(SalesArticleCategoryDO::getName, reqVO.getName())
                .eqIfPresent(SalesArticleCategoryDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SalesArticleCategoryDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SalesArticleCategoryDO::getSort));
    }

    default List<SalesArticleCategoryDO> selectListByStatus(Integer status) {
        return selectList(SalesArticleCategoryDO::getStatus, status);
    }

}
