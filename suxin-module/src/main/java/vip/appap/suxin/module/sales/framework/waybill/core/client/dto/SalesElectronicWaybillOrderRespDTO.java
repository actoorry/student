package vip.appap.suxin.module.sales.framework.waybill.core.client.dto;

import lombok.Data;

/**
 * 快递100电子面单下单/复打结果（对应官方 Demo {@code OrderResult}）
 *
 * @author 书心软件
 */
@Data
public class SalesElectronicWaybillOrderRespDTO {

    /**
     * 快递单号
     */
    private String kuaidinum;

    /**
     * 快递公司订单号
     */
    private String kdComOrderNum;

    /**
     * 任务 ID（复打 printOld 使用）
     */
    private String taskId;

    /**
     * 面单短链（printType 为 HTML/IMAGE 时返回）
     */
    private String label;

    /**
     * 回单号
     */
    private String returnNum;

    /**
     * 子单号，多个使用,隔开
     */
    private String childNum;

}
