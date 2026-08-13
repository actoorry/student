package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBannerCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBannerRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBannerUpdateReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBannerRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBannerDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SalesBannerConvert {

    SalesBannerConvert INSTANCE = Mappers.getMapper(SalesBannerConvert.class);

    List<SalesBannerRespVO> convertList(List<SalesBannerDO> list);

    PageResult<SalesBannerRespVO> convertPage(PageResult<SalesBannerDO> pageResult);

    SalesBannerRespVO convert(SalesBannerDO banner);

    SalesBannerDO convert(SalesBannerCreateReqVO createReqVO);

    SalesBannerDO convert(SalesBannerUpdateReqVO updateReqVO);

    List<AppSalesBannerRespVO> convertList01(List<SalesBannerDO> bannerList);

}
