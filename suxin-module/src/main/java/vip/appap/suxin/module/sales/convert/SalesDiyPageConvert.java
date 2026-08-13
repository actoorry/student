package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiyPageDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 装修页面 Convert
 *
 * @author owen
 */
@Mapper
public interface SalesDiyPageConvert {

    SalesDiyPageConvert INSTANCE = Mappers.getMapper(SalesDiyPageConvert.class);

    SalesDiyPageDO convert(SalesDiyPageCreateReqVO bean);

    SalesDiyPageDO convert(SalesDiyPageUpdateReqVO bean);

    SalesDiyPageRespVO convert(SalesDiyPageDO bean);

    List<SalesDiyPageRespVO> convertList(List<SalesDiyPageDO> list);

    PageResult<SalesDiyPageRespVO> convertPage(PageResult<SalesDiyPageDO> page);

    SalesDiyPageCreateReqVO convertCreateVo(Long templateId, String name, String remark);

    SalesDiyPagePropertyRespVO convertPropertyVo(SalesDiyPageDO diyPage);

    SalesDiyPageDO convert(SalesDiyPagePropertyUpdateRequestVO updateReqVO);

}
