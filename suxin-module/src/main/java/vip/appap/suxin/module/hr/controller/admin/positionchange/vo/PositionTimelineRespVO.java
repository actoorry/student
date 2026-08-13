package vip.appap.suxin.module.hr.controller.admin.positionchange.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 岗位变动时间轴条目（含 hr_outbound 合并展示） */
@Data
public class PositionTimelineRespVO {

    private String sourceType;
    private String changeType;
    private String title;
    private String fromDeptName;
    private String toDeptName;
    private Long fromPostId;
    private Long toPostId;
    private String fromPost;
    private String toPost;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String remark;

}
