package vip.appap.suxin.module.accountant.api;

import vip.appap.suxin.module.accountant.api.dto.PayOrderCreateReqDTO;
import vip.appap.suxin.module.accountant.api.dto.PayOrderRespDTO;
import vip.appap.suxin.module.accountant.convert.PayOrderConvert;
import vip.appap.suxin.module.accountant.dal.dataobject.PayOrderDO;
import vip.appap.suxin.module.accountant.service.PayOrderService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 支付单 API 实现类
 *
 * @author 书心软件
 */
@Service
public class PayOrderApiImpl implements PayOrderApi {

    @Resource
    private PayOrderService payOrderService;

    @Override
    public Long createOrder(PayOrderCreateReqDTO reqDTO) {
        return payOrderService.createOrder(reqDTO);
    }

    @Override
    public PayOrderRespDTO getOrder(Long id) {
        PayOrderDO order = payOrderService.getOrder(id);
        return PayOrderConvert.INSTANCE.convert2(order);
    }

    @Override
    public void updatePayOrderPrice(Long id, Integer payPrice) {
        payOrderService.updatePayOrderPrice(id, payPrice);
    }

}

