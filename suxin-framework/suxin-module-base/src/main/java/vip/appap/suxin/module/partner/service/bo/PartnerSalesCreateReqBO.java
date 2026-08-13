package vip.appap.suxin.module.partner.service.bo;

import vip.appap.suxin.framework.common.validation.Mobile;
import vip.appap.suxin.framework.common.validation.Telephone;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户创建 Create Req BO
 *
 * @author HUIHUI
 */
@Data
public class PartnerSalesCreateReqBO {

    @NotEmpty(message = "客户名称不能为空")
    private String name;
    private Boolean followUpStatus;
    private Boolean lockStatus;
    private Boolean dealStatus;
    private Integer industryId;
    private Integer level;
    private Integer source;

    @Mobile
    private String mobile;
    @Telephone
    private String telephone;
    private String qq;
    private String wechat;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 4096, message = "客户描述长度不能超过 4096 个字符")
    private String description;
    private String remark;
    private Long ownerUserId;
    private Integer areaId;
    private String detailAddress;

    private LocalDateTime contactLastTime;
    private String contactLastContent;
    private LocalDateTime contactNextTime;

}
