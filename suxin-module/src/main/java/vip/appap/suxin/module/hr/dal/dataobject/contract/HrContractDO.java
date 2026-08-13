package vip.appap.suxin.module.hr.dal.dataobject.contract;

import lombok.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;

/**
 * 劳动合同 DO
 *
 * @author admin
 */
@TableName("hr_contract")
@KeySequence("hr_contract_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrContractDO extends BaseDO {

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
     * 合同编号
     */
    private String contractNo;
    /**
     * 甲方
     */
    private String partyA;
    /**
     * 乙方所在部门
     */
    private String dept;
    /**
     * 合同开始时间
     */
    private LocalDateTime startTime;
    /**
     * 合同结束时间
     */
    private LocalDateTime endTime;
    /**
     * 签订日期-甲方
     */
    private LocalDateTime signDateA;
    /**
     * 签订日期-乙方
     */
    private LocalDateTime signDateB;
    /**
     * 盖章
     */
    private String sign;
    /**
     * 上传纸质合同图片
     */
    private String contractFile;
    /**
     * 上传可视化图片
     */
    private String photo;
    /**
     * 合同状态
     */
    private String status;

}
