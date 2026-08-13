package vip.appap.suxin.module.hr.controller.admin.positionchange.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PositionChangeSaveReqVO {

    private Long id;

    @NotNull(message = "员工不能为空")
    private Long partnerId;

    @NotEmpty(message = "异动类型不能为空")
    private String changeType;

    private Long fromDept;
    private Long toDept;

    @Schema(description = "原岗位编号 system_post.id")
    private Long fromPostId;

    @Schema(description = "新岗位编号 system_post.id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "新岗位不能为空")
    private Long toPostId;

    /** 原岗位名称（快照，后端写入） */
    private String fromPost;
    /** 新岗位名称（快照，后端写入） */
    private String toPost;

    @NotNull(message = "开始日期不能为空")
    private LocalDateTime startDate;

    private LocalDateTime endDate;
    private String reason;
    private String docAttachment;
    private Integer syncEmployee;
    private String remark;

}
