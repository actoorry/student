package vip.appap.suxin.module.sales.api.dto;

import lombok.Data;

/**
 * 拼团记录的创建 Response DTO
 *
 * @author HUIHUI
 */
@Data
public class SalesCombinationRecordCreateRespDTO {

    /**
     * 拼团活动编号
     *
     * 关联 SalesCombinationActivityDO 的 id 字段
     */
    private Long combinationActivityId;
    /**
     * 拼团团长编号
     *
     * 关联 SalesCombinationRecordDO 的 headId 字段
     */
    private Long combinationHeadId;
    /**
     * 拼团记录编号
     *
     * 关联 SalesCombinationRecordDO 的 id 字段
     */
    private Long combinationRecordId;

}
