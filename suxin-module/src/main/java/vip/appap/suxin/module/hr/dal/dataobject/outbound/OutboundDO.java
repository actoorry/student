package vip.appap.suxin.module.hr.dal.dataobject.outbound;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * HR 人员外出记录 DO
 *
 * 统一管理长期离岗记录：下乡支援、进修、学习、培训。
 * 下乡支援 → 计入服务年限（副高职称评审依据）。
 * 进修/学习/培训 → 计入继教学分。
 *
 * 不走审批流程，由人事自行填写维护；effective=1 计入汇总统计。
 *
 * @author suxin
 */
@TableName("hr_outbound")
@KeySequence("hr_outbound_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 关联 partner.id（员工）
     */
    private Long partnerId;
    /**
     * 外出类型，字典 hr_outbound_type
     * rural_support=下乡支援 training=进修 study=学习 course=培训
     */
    private String recordType;
    /**
     * 省份
     */
    private String province;
    /**
     * 市
     */
    private String city;
    /**
     * 区/县
     */
    private String county;
    /**
     * 进修/培训单位 或 下乡支援单位
     */
    private String organization;
    /**
     * 进修/培训名称（下乡支援可空）
     */
    private String practiceName;
    /**
     * 开始日期
     */
    private LocalDateTime startDate;
    /**
     * 结束日期
     */
    private LocalDateTime endDate;
    /**
     * 外出天数（系统计算：end - start + 1）
     */
    private Integer durationDays;
    /**
     * 服务年限（仅下乡支援：天数 / 365，保留 1 位小数）
     */
    private BigDecimal supportYears;
    /**
     * 继教学分（进修/学习/培训填写）
     */
    private BigDecimal continuingEducationCredit;
    /**
     * 是否计入汇总：1=是 0=否
     */
    private Integer effective;
    /**
     * 总结
     */
    private String summary;

}
