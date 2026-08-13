package vip.appap.suxin.module.accountant.controller.admin.vo;

import vip.appap.suxin.framework.excel.core.annotations.DictFormat;
import vip.appap.suxin.framework.excel.core.convert.DictConvert;
import vip.appap.suxin.framework.excel.core.convert.MoneyConvert;
import vip.appap.suxin.module.accountant.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 退款订单 Excel VO
 *
 * @author aquan
 */
@Data
public class PayRefundExcelVO {

    @ExcelProperty("支付退款编号")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty(value = "支付金额", converter = MoneyConvert.class)
    private Integer payPrice;

    @ExcelProperty(value = "退款金额", converter = MoneyConvert.class)
    private Integer refundPrice;

    @ExcelProperty("商户退款单号")
    private String merchantRefundId;
    @ExcelProperty("退款单号")
    private String no;
    @ExcelProperty("渠道退款单号")
    private String channelRefundNo;

    @ExcelProperty("商户支付单号")
    private String merchantOrderId;
    @ExcelProperty("渠道支付单号")
    private String channelOrderNo;

    @ExcelProperty(value = "退款状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.REFUND_STATUS)
    private Integer status;

    @ExcelProperty(value = "退款渠道", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.CHANNEL_CODE)
    private String channelCode;

    @ExcelProperty("成功时间")
    private LocalDateTime successTime;

    @ExcelProperty(value = "支付应用")
    private String appName;

    @ExcelProperty("退款原因")
    private String reason;

}

