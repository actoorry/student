package vip.appap.suxin.module.accountant.api;

import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.accountant.api.dto.PayRefundCreateReqDTO;
import vip.appap.suxin.module.accountant.api.dto.PayRefundRespDTO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayRefundDO;
import vip.appap.suxin.module.accountant.service.PayRefundService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 退款单 API 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class PayRefundApiImpl implements PayRefundApi {

    @Resource
    private PayRefundService payRefundService;

    @Override
    public Long createRefund(PayRefundCreateReqDTO reqDTO) {
        return payRefundService.createRefund(reqDTO);
    }

    @Override
    public PayRefundRespDTO getRefund(Long id) {
        PayRefundDO refund = payRefundService.getRefund(id);
        return BeanUtils.toBean(refund, PayRefundRespDTO.class);
    }

}

