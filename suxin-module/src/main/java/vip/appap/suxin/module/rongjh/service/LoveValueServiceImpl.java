package vip.appap.suxin.module.rongjh.service;

import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.module.rongjh.dal.dataobject.PartnerLoveRecordDO;
import vip.appap.suxin.module.rongjh.dal.dataobject.PartnerLoveSummaryDO;
import vip.appap.suxin.module.rongjh.dal.mysql.PartnerLoveRecordMapper;
import vip.appap.suxin.module.rongjh.dal.mysql.PartnerLoveSummaryMapper;
import vip.appap.suxin.module.rongjh.service.dto.LoveValueBackfillReport;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesOrderMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Validated
public class LoveValueServiceImpl implements LoveValueService {

    private static final BigDecimal TEN_THOUSAND = new BigDecimal("10000");

    @Resource
    private PartnerLoveRecordMapper loveRecordMapper;
    @Resource
    private PartnerLoveSummaryMapper loveSummaryMapper;
    @Resource
    private SalesOrderMapper tradeOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrderLoveValue(Long userId, Long orderId, Integer amountFen, String bizType, String remark) {
        applyLoveValue(userId, orderId, null, amountFen, bizType, remark, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackOrderLoveValue(Long userId, Long orderId, Long orderItemId, Integer amountFen, String bizType, String remark) {
        applyLoveValue(userId, orderId, orderItemId, amountFen, bizType, remark, true);
    }

    @Override
    public BigDecimal getUserLoveValue(Long userId) {
        if (userId == null) {
            return BigDecimal.ZERO;
        }
        PartnerLoveSummaryDO summary = loveSummaryMapper.selectById(userId);
        return summary != null && summary.getTotalLoveValue() != null
                ? summary.getTotalLoveValue() : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getPlatformLoveValue() {
        return loveSummaryMapper.selectPlatformLoveValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoveValueBackfillReport backfillCompletedOrders(Long startOrderId, Integer batchSize, boolean dryRun) {
        long start = startOrderId == null ? 0L : startOrderId;
        int size = batchSize == null || batchSize <= 0 ? 1000 : batchSize;
        List<SalesOrderDO> orders = tradeOrderMapper.selectCompletedPaidOrdersByIdGt(start, size);
        int applied = 0;
        int skipped = 0;
        long maxOrderId = start;
        for (SalesOrderDO order : orders) {
            maxOrderId = Math.max(maxOrderId, order.getId());
            Integer amountFen = calcEffectiveAmountFen(order);
            if (amountFen <= 0) {
                skipped++;
                continue;
            }
            String bizType = "BACKFILL";
            String bizKey = buildBizKey(order.getId(), null, bizType);
            if (loveRecordMapper.selectByBizKey(bizKey) != null) {
                skipped++;
                continue;
            }
            if (!dryRun) {
                applyLoveValue(order.getUserId(), order.getId(), null, amountFen, bizType, "历史订单回填", false);
            }
            applied++;
        }
        return new LoveValueBackfillReport(start, size, orders.size(), applied, skipped, maxOrderId, dryRun);
    }

    private void applyLoveValue(Long userId, Long orderId, Long orderItemId, Integer amountFen,
                                String bizType, String remark, boolean rollback) {
        if (userId == null || orderId == null || amountFen == null || amountFen <= 0 || StrUtil.isBlank(bizType)) {
            return;
        }
        String bizKey = buildBizKey(orderId, orderItemId, bizType);
        if (loveRecordMapper.selectByBizKey(bizKey) != null) {
            return;
        }
        long signedAmountFen = rollback ? -amountFen.longValue() : amountFen.longValue();
        BigDecimal signedLoveValue = toLoveValue(signedAmountFen);

        PartnerLoveRecordDO record = PartnerLoveRecordDO.builder()
                .userId(userId)
                .orderId(orderId)
                .orderItemId(orderItemId)
                .bizType(bizType)
                .bizKey(bizKey)
                .amountFen(signedAmountFen)
                .loveValue(signedLoveValue)
                .remark(remark)
                .build();
        loveRecordMapper.insert(record);

        PartnerLoveSummaryDO summary = loveSummaryMapper.selectById(userId);
        if (summary == null) {
            PartnerLoveSummaryDO init = PartnerLoveSummaryDO.builder()
                    .userId(userId)
                    .totalAmountFen(signedAmountFen)
                    .totalLoveValue(signedLoveValue)
                    .build();
            loveSummaryMapper.insert(init);
            return;
        }
        PartnerLoveSummaryDO update = new PartnerLoveSummaryDO();
        long currentAmountFen = summary.getTotalAmountFen() == null ? 0L : summary.getTotalAmountFen();
        BigDecimal currentLoveValue = summary.getTotalLoveValue() == null ? BigDecimal.ZERO : summary.getTotalLoveValue();
        update.setUserId(userId);
        update.setTotalAmountFen(currentAmountFen + signedAmountFen);
        update.setTotalLoveValue(currentLoveValue.add(signedLoveValue));
        loveSummaryMapper.updateById(update);
    }

    private Integer calcEffectiveAmountFen(SalesOrderDO order) {
        if (order == null || order.getPayPrice() == null) {
            return 0;
        }
        int refundPrice = order.getRefundPrice() == null ? 0 : order.getRefundPrice();
        return Math.max(order.getPayPrice() - refundPrice, 0);
    }

    private String buildBizKey(Long orderId, Long orderItemId, String bizType) {
        return orderItemId != null
                ? StrUtil.format("love:{}:{}:{}", bizType, orderId, orderItemId)
                : StrUtil.format("love:{}:{}", bizType, orderId);
    }

    private BigDecimal toLoveValue(long amountFen) {
        return BigDecimal.valueOf(amountFen).divide(TEN_THOUSAND, 4, RoundingMode.HALF_UP);
    }
}
