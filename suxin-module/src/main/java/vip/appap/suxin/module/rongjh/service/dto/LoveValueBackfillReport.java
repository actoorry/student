package vip.appap.suxin.module.rongjh.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoveValueBackfillReport {

    private Long startOrderId;
    private Integer batchSize;
    private Integer scannedOrders;
    private Integer appliedOrders;
    private Integer skippedOrders;
    private Long maxOrderId;
    private boolean dryRun;
}
