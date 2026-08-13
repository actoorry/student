package vip.appap.suxin.module.system.api.dto;

import vip.appap.suxin.framework.common.validation.InEnum;
import vip.appap.suxin.framework.common.validation.Mobile;
import vip.appap.suxin.module.system.enums.SmsSceneEnum;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * 短信验证码的校验 Request DTO
 *
 * @author 书心软件
 */
@Data
public class SmsCodeValidateReqDTO {

    /**
     * 手机号
     */
    @Mobile
    @NotEmpty(message = "手机号不能为空")
    private String mobile;
    /**
     * 发送场景
     */
    @NotNull(message = "发送场景不能为空")
    @InEnum(SmsSceneEnum.class)
    private Integer scene;
    /**
     * 验证码
     */
    @NotEmpty(message = "验证码")
    private String code;

}
