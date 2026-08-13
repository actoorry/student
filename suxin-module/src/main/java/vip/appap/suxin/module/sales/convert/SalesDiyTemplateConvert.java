package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesDiyTemplatePropertyRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiyPageDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiyTemplateDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 装修模板 Convert
 *
 * @author owen
 */
@Mapper
public interface SalesDiyTemplateConvert {

    SalesDiyTemplateConvert INSTANCE = Mappers.getMapper(SalesDiyTemplateConvert.class);

    SalesDiyTemplateDO convert(SalesDiyTemplateCreateReqVO bean);

    SalesDiyTemplateDO convert(SalesDiyTemplateUpdateReqVO bean);

    SalesDiyTemplateRespVO convert(SalesDiyTemplateDO bean);

    List<SalesDiyTemplateRespVO> convertList(List<SalesDiyTemplateDO> list);

    PageResult<SalesDiyTemplateRespVO> convertPage(PageResult<SalesDiyTemplateDO> page);

    SalesDiyTemplatePropertyRespVO convertPropertyVo(SalesDiyTemplateDO diyTemplate, List<SalesDiyPageDO> pages);

    AppSalesDiyTemplatePropertyRespVO convertPropertyVo2(SalesDiyTemplateDO diyTemplate, String home, String user);

    SalesDiyTemplateDO convert(SalesDiyTemplatePropertyUpdateRequestVO updateReqVO);

}
