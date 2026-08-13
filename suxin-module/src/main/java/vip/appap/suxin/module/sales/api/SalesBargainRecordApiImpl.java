package vip.appap.suxin.module.sales.api;

import vip.appap.suxin.module.sales.api.dto.SalesBargainValidateJoinRespDTO;
import vip.appap.suxin.module.sales.service.SalesBargainRecordService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * 砍价活动 API 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class SalesBargainRecordApiImpl implements SalesBargainRecordApi {

    @Resource
    private SalesBargainRecordService bargainRecordService;

    @Override
    public SalesBargainValidateJoinRespDTO validateJoinBargain(Long userId, Long bargainRecordId, Long skuId) {
        return bargainRecordService.validateJoinBargain(userId, bargainRecordId, skuId);
    }

    @Override
    public void updateBargainRecordOrderId(Long id, Long orderId) {
        bargainRecordService.updateBargainRecordOrderId(id, orderId);
    }

}
