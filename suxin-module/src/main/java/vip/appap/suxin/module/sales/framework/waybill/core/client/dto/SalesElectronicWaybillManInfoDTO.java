package vip.appap.suxin.module.sales.framework.waybill.core.client.dto;

import lombok.Data;

/**
 * 快递100电子面单寄/收件人信息
 * <p>
 * 对应官方 Demo {@code ManInfo}：printAddr 完整地址；或 province/city/district + addr 分字段。
 *
 * @author 书心软件
 */
@Data
public class SalesElectronicWaybillManInfoDTO {

    /**
     * 姓名（必填）
     */
    private String name;

    /**
     * 手机号，手机号和电话号二者其一必填
     */
    private String mobile;

    /**
     * 电话号，手机号和电话号二者其一必填
     */
    private String tel;

    /**
     * 所在完整地址（printAddr 与 province/city/district/addr 二选一）
     */
    private String printAddr;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区/县
     */
    private String district;

    /**
     * 详细地址
     */
    private String addr;

}
