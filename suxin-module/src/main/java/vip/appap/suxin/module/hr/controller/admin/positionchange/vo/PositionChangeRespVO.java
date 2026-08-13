package vip.appap.suxin.module.hr.controller.admin.positionchange.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ExcelIgnoreUnannotated
public class PositionChangeRespVO {

    private Long id;
    private Long partnerId;

    @ExcelProperty("员工姓名")
    private String partnerName;

    @ExcelProperty("员工工号")
    private String employeeNo;

    @ExcelProperty("异动类型")
    private String changeType;

    private Long fromDept;
    private Long toDept;

    private Long fromPostId;
    private Long toPostId;

    @ExcelProperty("原部门")
    private String fromDeptName;

    @ExcelProperty("新部门")
    private String toDeptName;

    @ExcelProperty("原岗位")
    private String fromPost;

    @ExcelProperty("新岗位")
    private String toPost;

    @ExcelProperty("开始日期")
    private LocalDateTime startDate;

    @ExcelProperty("结束日期")
    private LocalDateTime endDate;

    @ExcelProperty("变动原因")
    private String reason;

    private String docAttachment;
    private Integer syncEmployee;
    private String remark;
    private LocalDateTime createTime;

}
