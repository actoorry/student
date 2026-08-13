package vip.appap.suxin.module.sales.api;

import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.sales.api.dto.SalesCombinationRecordCreateReqDTO;
import vip.appap.suxin.module.sales.api.dto.SalesCombinationRecordCreateRespDTO;
import vip.appap.suxin.module.sales.api.dto.SalesCombinationRecordRespDTO;
import vip.appap.suxin.module.sales.api.dto.SalesCombinationValidateJoinRespDTO;
import vip.appap.suxin.module.sales.convert.SalesCombinationActivityConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationRecordDO;
import vip.appap.suxin.module.sales.service.SalesCombinationRecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 拼团活动 API 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class SalesCombinationRecordApiImpl implements SalesCombinationRecordApi {

    @Resource
    private SalesCombinationRecordService combinationRecordService;

    @Override
    public void validateCombinationRecord(Long userId, Long activityId, Long headId, Long skuId, Integer count) {
        combinationRecordService.validateCombinationRecord(userId, activityId, headId, skuId, count);
    }

    @Override
    public SalesCombinationRecordCreateRespDTO createCombinationRecord(SalesCombinationRecordCreateReqDTO reqDTO) {
        return SalesCombinationActivityConvert.INSTANCE.convert4(combinationRecordService.createCombinationRecord(reqDTO));
    }

    @Override
    public SalesCombinationRecordRespDTO getCombinationRecordByOrderId(Long userId, Long orderId) {
        SalesCombinationRecordDO record = combinationRecordService.getCombinationRecord(userId, orderId);
        return BeanUtils.toBean(record, SalesCombinationRecordRespDTO.class);
    }

    @Override
    public SalesCombinationValidateJoinRespDTO validateJoinCombination(Long userId, Long activityId, Long headId, Long skuId, Integer count) {
        return combinationRecordService.validateJoinCombination(userId, activityId, headId, skuId, count);
    }

}
