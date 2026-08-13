package vip.appap.suxin.module.rongjh.controller.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 爱心帮扶申请 Response VO")
@Data
public class HelpRespVO {

    @Schema(description = "申请编号")
    private Long id;

    @Schema(description = "申请人 partner.id")
    private Long partnerId;

    @Schema(description = "申请人昵称")
    private String nickname;

    @Schema(description = "申请人手机号")
    private String mobile;

    @Schema(description = "困难人姓名")
    private String name;

    @Schema(description = "困难人联系电话")
    private String phone;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "困难原因/求助说明")
    private String reason;

    @Schema(description = "申请金额")
    private BigDecimal applyAmount;

    @Schema(description = "批准金额")
    private BigDecimal actualAmount;

    @Schema(description = "证明材料图片（可预览 URL 或 base64）")
    private List<String> materialUrls;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "审核备注")
    private String remark;

    @Schema(description = "申请时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
