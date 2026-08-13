package vip.appap.suxin.module.accountant.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.common.util.collection.MapUtils;
import vip.appap.suxin.module.accountant.api.dto.PayOrderCreateReqDTO;
import vip.appap.suxin.module.accountant.api.dto.PayOrderRespDTO;
import vip.appap.suxin.module.accountant.controller.admin.vo.*;
import vip.appap.suxin.module.accountant.dal.dataobject.PayAppDO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayOrderDO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayOrderExtensionDO;
import vip.appap.suxin.module.accountant.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * 支付订单 Convert
 *
 * @author aquan
 */
@Mapper
public interface PayOrderConvert {

    PayOrderConvert INSTANCE = Mappers.getMapper(PayOrderConvert.class);

    PayOrderRespVO convert(PayOrderDO bean);

    PayOrderRespDTO convert2(PayOrderDO order);

    default PayOrderDetailsRespVO convert(PayOrderDO order, PayOrderExtensionDO orderExtension, PayAppDO app) {
        PayOrderDetailsRespVO respVO = convertDetail(order);
        respVO.setExtension(convert(orderExtension));
        if (app != null) {
            respVO.setAppName(app.getName());
        }
        return respVO;
    }
    PayOrderDetailsRespVO convertDetail(PayOrderDO bean);
    PayOrderDetailsRespVO.PayOrderExtension convert(PayOrderExtensionDO bean);

    default PageResult<PayOrderPageItemRespVO> convertPage(PageResult<PayOrderDO> page, Map<Long, PayAppDO> appMap) {
        PageResult<PayOrderPageItemRespVO> result = convertPage(page);
        result.getList().forEach(order -> MapUtils.findAndThen(appMap, order.getAppId(), app -> order.setAppName(app.getName())));
        return result;
    }
    PageResult<PayOrderPageItemRespVO> convertPage(PageResult<PayOrderDO> page);

    default List<PayOrderExcelVO> convertList(List<PayOrderDO> list, Map<Long, PayAppDO> appMap) {
        return CollectionUtils.convertList(list, order -> {
            PayOrderExcelVO excelVO = convertExcel(order);
            MapUtils.findAndThen(appMap, order.getAppId(), app -> excelVO.setAppName(app.getName()));
            return excelVO;
        });
    }
    PayOrderExcelVO convertExcel(PayOrderDO bean);

    PayOrderDO convert(PayOrderCreateReqDTO bean);

    @Mapping(target = "id", ignore = true)
    PayOrderExtensionDO convert(PayOrderSubmitReqVO bean, String userIp);

    PayOrderUnifiedReqDTO convert2(PayOrderSubmitReqVO reqVO, String userIp);

    @Mapping(source = "order.status", target = "status")
    PayOrderSubmitRespVO convert(PayOrderDO order, vip.appap.suxin.module.accountant.framework.pay.core.client.dto.order.PayOrderRespDTO respDTO);

}

