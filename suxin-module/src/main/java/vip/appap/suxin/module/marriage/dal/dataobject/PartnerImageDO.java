package vip.appap.suxin.module.marriage.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 公用相册图片 DO
 */
@TableName(value = "partner_image", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerImageDO {

    @TableId
    private Long id;
    private Long profileId;

    /**
     * 类型：1-婚恋相册
     */
    private Integer type;

    private String imageUrl;
    private Integer sortNo;
    private LocalDateTime createTime;
    private Long tenantId;

}
