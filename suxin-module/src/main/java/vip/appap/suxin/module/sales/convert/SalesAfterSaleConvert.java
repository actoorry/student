package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.accountant.api.dto.PayRefundCreateReqDTO;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.product.api.dto.ProductPropertyValueDetailRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesAfterSaleDetailRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesAfterSaleRespPageItemVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesAfterSaleLogRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderPartnerRespVO;
import vip.appap.suxin.module.sales.controller.admin.SalesProductPropertyValueDetailRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderBaseVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesAfterSaleCreateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesAfterSaleDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesAfterSaleLogDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.framework.order.config.SalesOrderProperties;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

@Mapper
public interface SalesAfterSaleConvert {

    SalesAfterSaleConvert INSTANCE = Mappers.getMapper(SalesAfterSaleConvert.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "creator", ignore = true),
            @Mapping(target = "updater", ignore = true),
    })
    SalesAfterSaleDO convert(AppSalesAfterSaleCreateReqVO createReqVO, SalesOrderItemDO tradeOrderItem);

    @Mappings({
            @Mapping(source = "afterSale.orderId", target = "merchantOrderId"),
            @Mapping(source = "afterSale.id", target = "merchantRefundId"),
            @Mapping(source = "afterSale.applyReason", target = "reason"),
            @Mapping(source = "afterSale.refundPrice", target = "price"),
            @Mapping(source = "orderProperties.payAppKey", target = "appKey"),
    })
    PayRefundCreateReqDTO convert(String userIp, SalesAfterSaleDO afterSale, SalesOrderProperties orderProperties);

    SalesOrderPartnerRespVO convert(PartnerRespDTO bean);

    PageResult<SalesAfterSaleRespPageItemVO> convertPage(PageResult<SalesAfterSaleDO> page);

    default PageResult<SalesAfterSaleRespPageItemVO> convertPage(PageResult<SalesAfterSaleDO> pageResult,
                                                            Map<Long, PartnerRespDTO> PartnerUsers) {
        PageResult<SalesAfterSaleRespPageItemVO> voPageResult = convertPage(pageResult);
        // 处理会员
        voPageResult.getList().forEach(afterSale -> afterSale.setUser(
                convert(PartnerUsers.get(afterSale.getUserId()))));
        return voPageResult;
    }

    SalesProductPropertyValueDetailRespVO convert(ProductPropertyValueDetailRespDTO bean);

    default SalesAfterSaleDetailRespVO convert(SalesAfterSaleDO afterSale, SalesOrderDO order, SalesOrderItemDO orderItem,
                                          PartnerRespDTO user, List<SalesAfterSaleLogDO> logs) {
        SalesAfterSaleDetailRespVO respVO = convert02(afterSale);
        // 处理用户信息
        respVO.setUser(convert(user));
        // 处理订单信息
        respVO.setOrder(convert(order));
        respVO.setOrderItem(convert02(orderItem));
        // 处理售后日志
        respVO.setLogs(convertList1(logs));
        return respVO;
    }

    List<SalesAfterSaleLogRespVO> convertList1(List<SalesAfterSaleLogDO> list);
    SalesAfterSaleDetailRespVO convert02(SalesAfterSaleDO bean);
    SalesAfterSaleDetailRespVO.OrderItem convert02(SalesOrderItemDO bean);
    SalesOrderBaseVO convert(SalesOrderDO bean);

}

