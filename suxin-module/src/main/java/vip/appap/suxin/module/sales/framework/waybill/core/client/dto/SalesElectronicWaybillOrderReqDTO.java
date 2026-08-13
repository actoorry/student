package vip.appap.suxin.module.sales.framework.waybill.core.client.dto;

import lombok.Data;

/**
 * 快递100电子面单下单请求（内部模型，序列化为 {@code param}）
 * <p>
 * 对应官方 Demo {@code OrderReq} 的核心字段；以业务订单号作为 orderId 且 reorder=false 实现幂等。
 * 承运商账户字段（partnerId / partnerKey / partnerSecret / net 等）由客户端从账户读取并入参。
 *
 * @author 书心软件
 */
@Data
public class SalesElectronicWaybillOrderReqDTO {

    /**
     * 收件人信息
     */
    private SalesElectronicWaybillManInfoDTO recMan;

    /**
     * 寄件人信息
     */
    private SalesElectronicWaybillManInfoDTO sendMan;

    /**
     * 快递公司编码
     */
    private String kuaidicom;

    /**
     * 订单号（业务销售订单流水号，幂等键）
     */
    private String orderId;

    /**
     * 支付方式：SHIPPER 寄方付 / CONSIGNEE 到付 / MONTHLY 月结 / THIRDPARTY 第三方支付
     */
    private String payType = "MONTHLY";

    /**
     * 物品总数量
     */
    private Integer count = 1;

    /**
     * 备注
     */
    private String remark;

    /**
     * 物品名称
     */
    private String cargo;

    /**
     * 快递100模板 ID
     */
    private String tempId;

    /**
     * 打印类型（HTML / IMAGE）
     */
    private String printType;

}
