package vip.appap.suxin.module.rongjh.service;

import vip.appap.suxin.module.rongjh.service.dto.LoveValueBackfillReport;

import java.math.BigDecimal;

public interface LoveValueService {

    void addOrderLoveValue(Long userId, Long orderId, Integer amountFen, String bizType, String remark);

    void rollbackOrderLoveValue(Long userId, Long orderId, Long orderItemId, Integer amountFen, String bizType, String remark);

    BigDecimal getUserLoveValue(Long userId);

    BigDecimal getPlatformLoveValue();

    LoveValueBackfillReport backfillCompletedOrders(Long startOrderId, Integer batchSize, boolean dryRun);
}
