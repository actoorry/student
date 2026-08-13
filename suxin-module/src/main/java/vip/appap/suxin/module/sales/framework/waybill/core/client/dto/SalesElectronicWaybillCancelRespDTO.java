package vip.appap.suxin.module.sales.framework.waybill.core.client.dto;

import lombok.Data;

/**
 * 快递100电子面单取消响应（对应官方 Demo {@code PrintBaseResp}）
 *
 * @author 书心软件
 */
@Data
public class SalesElectronicWaybillCancelRespDTO {

    /**
     * 返回码：200 表示成功
     */
    private String returnCode;

    /**
     * 是否成功
     */
    private Boolean result;

    /**
     * 返回消息
     */
    private String message;

    public boolean isSuccess() {
        return Boolean.TRUE.equals(result) && "200".equals(returnCode);
    }

}
