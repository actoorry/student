package vip.appap.suxin.module.hr.dal.dataobject.resume;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;

/**
 * 履历 DO
 *
 * @author admin
 */
@TableName("hr_resume")
@KeySequence("hr_resume_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 关联员工
     */
    private Long partnerId;
    /**
     * 员工工号
     */
    private String employeeNo;
    /**
     * 所在部门
     */
    private String dept;
    /**
     * 个人简历
     */
    private String resumeContent;
    /**
     * 奖惩情况
     */
    private String awardsPunishments;
    /**
     * 证书
     */
    private String certificates;
    /**
     * 论文
     */
    private String papers;
    /**
     * 年度考核情况
     */
    private String annualReview;
    /**
     * 备注
     */
    private String remark;
    /**
     * 省份
     */
    private String province;
    /**
     * 市
     */
    private String city;
    /**
     * 县
     */
    private String county;

}
