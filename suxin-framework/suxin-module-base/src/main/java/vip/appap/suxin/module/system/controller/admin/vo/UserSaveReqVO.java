package vip.appap.suxin.module.system.controller.admin.vo;

import cn.hutool.core.util.ObjectUtil;
import vip.appap.suxin.module.system.enums.SystemValidationConstants;
import vip.appap.suxin.module.system.framework.operatelog.core.DeptParseFunction;
import vip.appap.suxin.module.system.framework.operatelog.core.PostParseFunction;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

@Schema(description = "管理后台 - 用户创建/修改 Request VO")
@Data
public class UserSaveReqVO {

    @Schema(description = "用户编号", example = "1024")
    private Long id;

    @Schema(description = "用户账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "suxin")
    @NotBlank(message = "用户账号不能为空")
    @Pattern(regexp = SystemValidationConstants.USERNAME_PATTERN, message = SystemValidationConstants.USERNAME_PATTERN_MESSAGE)
    @Size(min = SystemValidationConstants.USERNAME_MIN, max = SystemValidationConstants.USERNAME_MAX, message = SystemValidationConstants.USERNAME_LENGTH_MESSAGE)
    @DiffLogField(name = "用户账号")
    private String username;

    @Schema(description = "备注", example = "我是一个用户")
    @DiffLogField(name = "备注")
    private String remark;

    @Schema(description = "部门编号", example = "我是一个用户")
    @DiffLogField(name = "部门", function = DeptParseFunction.NAME)
    private Long deptId;

    @Schema(description = "岗位编号数组", example = "1")
    @DiffLogField(name = "岗位", function = PostParseFunction.NAME)
    private Set<Long> postIds;

    // ========== 仅【创建】时，需要传递的字段 ==========

    @Schema(description = "关联合作伙伴编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "关联合作伙伴不能为空")
    @DiffLogField(name = "关联合作伙伴")
    private Long partnerId;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String password;

    @AssertTrue(message = "密码不能为空")
    @JsonIgnore
    public boolean isPasswordValid() {
        return id != null // 修改时，不需要传递
                || (ObjectUtil.isAllNotEmpty(password)); // 新增时，必须都传递 password
    }

    // ========== 业务角色 ==========

    @Schema(description = "是否销售", example = "false")
    @DiffLogField(name = "是否销售")
    private Boolean isSale;

    @Schema(description = "是否采购", example = "false")
    @DiffLogField(name = "是否采购")
    private Boolean isPurchase;

    @Schema(description = "是否生产", example = "false")
    @DiffLogField(name = "是否生产")
    private Boolean isMes;

    @Schema(description = "是否仓库", example = "false")
    @DiffLogField(name = "是否仓库")
    private Boolean isStock;

}
