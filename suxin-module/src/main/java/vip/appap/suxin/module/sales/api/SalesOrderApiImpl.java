package vip.appap.suxin.module.sales.api;

import vip.appap.suxin.module.sales.api.dto.SalesOrderRespDTO;
import vip.appap.suxin.module.sales.convert.SalesOrderConvert;
import vip.appap.suxin.module.sales.service.SalesOrderQueryService;
import vip.appap.suxin.module.sales.service.SalesOrderUpdateService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/**
 * 订单 API 接口实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class SalesOrderApiImpl implements SalesOrderApi {

    @Resource
    private SalesOrderUpdateService tradeOrderUpdateService;
    @Resource
    private SalesOrderQueryService tradeOrderQueryService;

    @Override
    public List<SalesOrderRespDTO> getOrderList(Collection<Long> ids) {
        return SalesOrderConvert.INSTANCE.convertList04(tradeOrderQueryService.getOrderList(ids));
    }

    @Override
    public SalesOrderRespDTO getOrder(Long id) {
        return SalesOrderConvert.INSTANCE.convert(tradeOrderQueryService.getOrder(id));
    }

    @Override
    public void cancelPaidOrder(Long userId, Long orderId, Integer cancelType) {
        tradeOrderUpdateService.cancelPaidOrder(userId, orderId, cancelType);
    }

}
