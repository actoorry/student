package vip.appap.suxin.module.hr.controller.admin.contract.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 劳动合同 Response VO")
@Data
@ExcelIgnoreUnannotated
public class HrContractRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "6233")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "关联员工", requiredMode = Schema.RequiredMode.REQUIRED, example = "10870")
    @ExcelProperty("关联员工")
    private Long partnerId;

    @Schema(description = "姓名", example = "张三")
    @ExcelProperty("姓名")
    private String name;

    @Schema(description = "合同编号")
    @ExcelProperty("合同编号")
    private String contractNo;

    @Schema(description = "甲方")
    @ExcelProperty("甲方")
    private String partyA;

    @Schema(description = "乙方所在部门（部门 ID）")
    private String dept;

    @Schema(description = "乙方所在部门名称")
    @ExcelProperty("乙方所在部门")
    private String deptName;

    @Schema(description = "合同开始时间")
    @ExcelProperty("合同开始时间")
    private LocalDateTime startTime;

    @Schema(description = "合同结束时间")
    @ExcelProperty("合同结束时间")
    private LocalDateTime endTime;

    @Schema(description = "签订日期-甲方")
    @ExcelProperty("签订日期-甲方")
    private LocalDateTime signDateA;

    @Schema(description = "签订日期-乙方")
    @ExcelProperty("签订日期-乙方")
    private LocalDateTime signDateB;

    @Schema(description = "上传纸质合同图片")
    @ExcelProperty("上传纸质合同图片")
    private String contractFile;

    @Schema(description = "上传可视化图片")
    @ExcelProperty("上传可视化图片")
    private String photo;

    @Schema(description = "合同状态", example = "2")
    @ExcelProperty("合同状态")
    private String status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
