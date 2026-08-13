package vip.appap.suxin.module.rongjh.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

@TableName("partner_warrior")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerWarriorDO {

    @TableId
    private Long id;

    private String type;

    private String name;

    private String idCard;

    private String phone;

    private String province;

    private Integer stateId;

    private String city;

    private String militaryBranch;

    private Integer serviceYears;

    private String serviceUnit;

    private String serviceYear;

    private String description;

    private String certificateImg;

    /** 1-正常 2-拉黑 */
    private Integer status;

    /** 审核备注 */
    private String remark;

    private String creator;

    private LocalDateTime createTime;

    private String updater;

    private LocalDateTime updateTime;

}
