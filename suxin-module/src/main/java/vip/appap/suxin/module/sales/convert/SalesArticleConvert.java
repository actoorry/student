package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesArticleCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesArticleRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesArticleUpdateReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesArticleRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesArticleDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 文章管理 Convert
 *
 * @author HUIHUI
 */
@Mapper
public interface SalesArticleConvert {

    SalesArticleConvert INSTANCE = Mappers.getMapper(SalesArticleConvert.class);

    SalesArticleDO convert(SalesArticleCreateReqVO bean);

    SalesArticleDO convert(SalesArticleUpdateReqVO bean);

    SalesArticleRespVO convert(SalesArticleDO bean);

    List<SalesArticleRespVO> convertList(List<SalesArticleDO> list);

    PageResult<SalesArticleRespVO> convertPage(PageResult<SalesArticleDO> page);

    AppSalesArticleRespVO convert01(SalesArticleDO article);

    PageResult<AppSalesArticleRespVO> convertPage02(PageResult<SalesArticleDO> articlePage);

    List<AppSalesArticleRespVO> convertList03(List<SalesArticleDO> articleCategoryListByRecommendHotAndRecommendBanner);

}
