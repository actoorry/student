package vip.appap.suxin.module.rongjh.dal.dataobject;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("partner_help")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerHelpDO {

    @TableId
    private Long id;

    private Long partnerId;

    private String name;

    private String phone;

    /** 身份证号（非必填，插入时始终写入列避免 NOT NULL 表结构报错） */
    @TableField(insertStrategy = FieldStrategy.ALWAYS)
    private String idCard;

    private String reason;

    private BigDecimal applyAmount;

    private BigDecimal actualAmount;

    /** 证明材料 URL 或 JSON，逗号分隔 */
    private String materials;

    /** 0-待审核 1-已通过 2-已驳回 3-已撤销 */
    private Integer status;

    private String remark;

    private String creator;

    private LocalDateTime createTime;

    private String updater;

    private LocalDateTime updateTime;

}
