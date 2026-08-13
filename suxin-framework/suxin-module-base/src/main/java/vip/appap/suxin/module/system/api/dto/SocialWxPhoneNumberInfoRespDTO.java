package vip.appap.suxin.module.system.api.dto;

import lombok.Data;

/**
 * 微信小程序的手机信息 Response DTO
 *
 * @author 书心软件
 */
@Data
public class SocialWxPhoneNumberInfoRespDTO {

    /**
     * 用户绑定的手机号（国外手机号会有区号）
     */
    private String phoneNumber;

    /**
     * 没有区号的手机号
     */
    private String purePhoneNumber;
    /**
     * 区号
     */
    private String countryCode;

}
