package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesArticleCategoryRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesArticleCategoryDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 文章分类 Convert
 *
 * @author HUIHUI
 */
@Mapper
public interface SalesArticleCategoryConvert {

    SalesArticleCategoryConvert INSTANCE = Mappers.getMapper(SalesArticleCategoryConvert.class);

    SalesArticleCategoryDO convert(SalesArticleCategoryCreateReqVO bean);

    SalesArticleCategoryDO convert(SalesArticleCategoryUpdateReqVO bean);

    SalesArticleCategoryRespVO convert(SalesArticleCategoryDO bean);

    List<SalesArticleCategoryRespVO> convertList(List<SalesArticleCategoryDO> list);

    PageResult<SalesArticleCategoryRespVO> convertPage(PageResult<SalesArticleCategoryDO> page);

    List<SalesArticleCategorySimpleRespVO> convertList03(List<SalesArticleCategoryDO> list);

    List<AppSalesArticleCategoryRespVO> convertList04(List<SalesArticleCategoryDO> categoryList);

}
