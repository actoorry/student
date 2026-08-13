package vip.appap.suxin.module.sales.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - 订单电子面单信息 Response VO
 * <p>
 * 用于订单详情/列表按权限展示电子面单状态与可打印内容；不含账户凭据。
 */
@Schema(description = "管理后台 - 订单电子面单信息 Response VO")
@Data
public class SalesElectronicWaybillRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "状态：0-失败 1-有效 2-已作废", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "状态名称", example = "有效")
    private String statusName;

    @Schema(description = "快递公司编码", example = "shunfeng")
    private String expressCode;

    @Schema(description = "快递公司名称", example = "顺丰速运")
    private String expressName;

    @Schema(description = "快递单号", example = "SF123456789")
    private String waybillNo;

    @Schema(description = "快递公司订单号", example = "KDC123")
    private String kdComOrderNum;

    @Schema(description = "打印类型：HTML / IMAGE", example = "HTML")
    private String printType;

    @Schema(description = "面单短链（可打印内容，浏览器本地打印）", example = "https://...")
    private String labelUrl;

    @Schema(description = "失败错误码（脱敏）", example = "4001")
    private String failCode;

    @Schema(description = "失败摘要（脱敏）", example = "快递单号有误")
    private String failMessage;

    @Schema(description = "下单时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "作废时间")
    private LocalDateTime cancelTime;

    @Schema(description = "作废原因")
    private String cancelReason;

}
