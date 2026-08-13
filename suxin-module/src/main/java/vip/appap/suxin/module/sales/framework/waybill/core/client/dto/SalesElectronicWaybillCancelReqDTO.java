package vip.appap.suxin.module.sales.framework.waybill.core.client.dto;

import lombok.Data;

/**
 * 快递100电子面单取消请求（内部模型，序列化为 {@code param}）
 * <p>
 * 对应官方 Demo {@code LabelCancelParam}：使用快递公司订单号（kdComOrderNum）取消。
 * 承运商账户字段（partnerId / partnerKey / partnerSecret / net / code）由客户端从账户读取并入参。
 *
 * @author 书心软件
 */
@Data
public class SalesElectronicWaybillCancelReqDTO {

    /**
     * 快递公司编码
     */
    private String kuaidicom;

    /**
     * 快递单号
     */
    private String kuaidinum;

    /**
     * 快递公司订单号（下单时返回的 kdComOrderNum；如有则必传）
     */
    private String orderId;

    /**
     * 取消原因
     */
    private String reason;

}
