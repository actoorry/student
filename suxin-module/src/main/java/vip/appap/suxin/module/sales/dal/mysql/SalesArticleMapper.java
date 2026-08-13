package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesArticlePageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesArticlePageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesArticleDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 文章管理 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface SalesArticleMapper extends BaseMapperX<SalesArticleDO> {

    default PageResult<SalesArticleDO> selectPage(SalesArticlePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesArticleDO>()
                .eqIfPresent(SalesArticleDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(SalesArticleDO::getStateId, reqVO.getStateId())
                .eqIfPresent(SalesArticleDO::getTitle, reqVO.getTitle())
                .eqIfPresent(SalesArticleDO::getAuthor, reqVO.getAuthor())
                .eqIfPresent(SalesArticleDO::getStatus, reqVO.getStatus())
                .eqIfPresent(SalesArticleDO::getSpuId, reqVO.getSpuId())
                .eqIfPresent(SalesArticleDO::getRecommendHot, reqVO.getRecommendHot())
                .eqIfPresent(SalesArticleDO::getRecommendBanner, reqVO.getRecommendBanner())
                .betweenIfPresent(SalesArticleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SalesArticleDO::getId));
    }

    default List<SalesArticleDO> selectList(Boolean recommendHot, Boolean recommendBanner) {
        return selectList(new LambdaQueryWrapperX<SalesArticleDO>()
                .eqIfPresent(SalesArticleDO::getRecommendHot, recommendHot)
                .eqIfPresent(SalesArticleDO::getRecommendBanner, recommendBanner));
    }

    default List<SalesArticleDO> selectListByTitle(String title) {
        return selectList(SalesArticleDO::getTitle, title);
    }

    default PageResult<SalesArticleDO> selectPage(AppSalesArticlePageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<SalesArticleDO>()
                .eqIfPresent(SalesArticleDO::getCategoryId, pageReqVO.getCategoryId())
                .eqIfPresent(SalesArticleDO::getStateId, pageReqVO.getStateId())
                .eq(SalesArticleDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByDesc(SalesArticleDO::getSort)
                .orderByDesc(SalesArticleDO::getId));
    }

    default void updateBrowseCount(Long id) {
        update(null, new LambdaUpdateWrapper<SalesArticleDO>()
                .eq(SalesArticleDO::getId, id)
                .setSql("browse_count = browse_count + 1"));
    }

}
