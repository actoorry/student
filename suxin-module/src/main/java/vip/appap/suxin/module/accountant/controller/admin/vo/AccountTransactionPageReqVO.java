package vip.appap.suxin.module.accountant.controller.admin.vo;

import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 钱包流水分页 Request VO")
@Data
public class AccountTransactionPageReqVO extends PageParam  {

    @Schema(description = "钱包编号", example = "888")
    private Long accountId;

    @Schema(description = "用户编号", example = "1024")
    private Long userId;

    @Schema(description = "用户类型", example = "1")
    @InEnum(UserTypeEnum.class)
    private Integer userType;

}

