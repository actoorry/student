package vip.appap.suxin.module.accountant.api;

import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin.WxPayClientConfig;
import vip.appap.suxin.module.accountant.api.dto.PayTransferCreateReqDTO;
import vip.appap.suxin.module.accountant.api.dto.PayTransferCreateRespDTO;
import vip.appap.suxin.module.accountant.api.dto.PayTransferRespDTO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayChannelDO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayTransferDO;
import vip.appap.suxin.module.accountant.service.PayChannelService;
import vip.appap.suxin.module.accountant.service.PayTransferService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 转账单 API 实现类
 *
 * @author jason
 */
@Service
@Validated
public class PayTransferApiImpl implements PayTransferApi {

    @Resource
    private PayTransferService payTransferService;
    @Resource
    private PayChannelService payChannelService;

    @Override
    public PayTransferCreateRespDTO createTransfer(PayTransferCreateReqDTO reqDTO) {
        return payTransferService.createTransfer(reqDTO);
    }

    @Override
    public PayTransferRespDTO getTransfer(Long id) {
        PayTransferDO transfer = payTransferService.getTransfer(id);
        if (transfer == null) {
            return null;
        }
        PayChannelDO channel = payChannelService.getChannel(transfer.getChannelId());
        String mchId = null;
        if (channel != null && channel.getConfig() instanceof WxPayClientConfig) {
            mchId = ((WxPayClientConfig) channel.getConfig()).getMchId();
        }
        return BeanUtils.toBean(transfer, PayTransferRespDTO.class).setChannelMchId(mchId);
    }

}

