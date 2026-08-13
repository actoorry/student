package vip.appap.suxin.module.hr.controller.admin.contract.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 劳动合同新增/修改 Request VO")
@Data
public class HrContractSaveReqVO {

    @Schema(description = "主键", example = "6233")
    private Long id;

    @Schema(description = "关联员工", requiredMode = Schema.RequiredMode.REQUIRED, example = "10870")
    @NotNull(message = "关联员工不能为空")
    private Long partnerId;

    @Schema(description = "合同编号")
    private String contractNo;

    @Schema(description = "甲方")
    private String partyA;

    @Schema(description = "乙方所在部门")
    private String dept;

    @Schema(description = "合同开始时间")
    private LocalDateTime startTime;

    @Schema(description = "合同结束时间")
    private LocalDateTime endTime;

    @Schema(description = "签订日期-甲方")
    private LocalDateTime signDateA;

    @Schema(description = "签订日期-乙方")
    private LocalDateTime signDateB;

    @Schema(description = "上传纸质合同图片")
    private String contractFile;

    @Schema(description = "上传可视化图片")
    private String photo;

    @Schema(description = "合同状态", example = "2")
    private String status;

}
