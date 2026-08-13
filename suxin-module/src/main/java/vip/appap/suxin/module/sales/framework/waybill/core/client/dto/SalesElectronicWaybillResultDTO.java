package vip.appap.suxin.module.sales.framework.waybill.core.client.dto;

import lombok.Data;

/**
 * 快递100电子面单响应包装（对应官方 Demo {@code Result<T>}）
 *
 * @author 书心软件
 */
@Data
public class SalesElectronicWaybillResultDTO<T> {

    /**
     * 响应状态码：200-成功；其他-失败
     */
    private Integer code;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应结果描述
     */
    private String message = "";

    /**
     * 响应耗时：毫秒
     */
    private Long time;

    public boolean isSuccess() {
        return code != null && code == 200;
    }

}
