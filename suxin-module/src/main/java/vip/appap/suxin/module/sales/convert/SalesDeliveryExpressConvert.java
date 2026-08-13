package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressExcelVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDeliveryExpressUpdateReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesDeliveryyExpressRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SalesDeliveryExpressConvert {

    SalesDeliveryExpressConvert INSTANCE = Mappers.getMapper(SalesDeliveryExpressConvert.class);

    SalesDeliveryExpressDO convert(SalesDeliveryExpressCreateReqVO bean);

    SalesDeliveryExpressDO convert(SalesDeliveryExpressUpdateReqVO bean);

    SalesDeliveryExpressRespVO convert(SalesDeliveryExpressDO bean);

    List<SalesDeliveryExpressRespVO> convertList(List<SalesDeliveryExpressDO> list);

    PageResult<SalesDeliveryExpressRespVO> convertPage(PageResult<SalesDeliveryExpressDO> page);

    List<SalesDeliveryExpressExcelVO> convertList02(List<SalesDeliveryExpressDO> list);

    List<SalesDeliveryExpressSimpleRespVO> convertList1(List<SalesDeliveryExpressDO> list);

    List<AppSalesDeliveryyExpressRespVO> convertList03(List<SalesDeliveryExpressDO> list);

}
