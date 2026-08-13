package vip.appap.suxin.module.sales.framework.waybill.core.client.dto;

import lombok.Data;

/**
 * 快递100电子面单复打请求（内部模型，序列化为 {@code param}）
 * <p>
 * 对应官方 Demo {@code RepeatPrintReq}：使用下单返回的 taskId 复打，不创建新单号。
 *
 * @author 书心软件
 */
@Data
public class SalesElectronicWaybillReprintReqDTO {

    /**
     * 任务 ID（下单时返回的 taskId）
     */
    private String taskId;

}
