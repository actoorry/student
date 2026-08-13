package vip.appap.suxin.module.sales.service;

import jakarta.validation.Valid;
import vip.appap.suxin.module.partner.controller.app.vo.*;

/**
 * 合作伙伴认证 Service 接口
 *
 * @author 书心软件
 */
public interface PartnerAuthService {

    /**
     * 手机 + 密码登录
     *
     * @param reqVO 登录信息
     * @return 登录结果
     */
    PartnerLoginRespVO login(@Valid PartnerLoginReqVO reqVO);

    /**
     * 手机 + 密码注册
     *
     * @param reqVO 注册信息
     * @return 用户编号
     */
    Long register(@Valid PartnerRegisterReqVO reqVO);

    /**
     * 基于 token 退出登录
     *
     * @param token token
     */
    void logout(String token);

    /**
     * 手机 + 验证码登陆
     *
     * @param reqVO 登陆信息
     * @return 登录结果
     */
    PartnerLoginRespVO smsLogin(@Valid PartnerSmsLoginReqVO reqVO);

    /**
     * 社交登录，使用 code 授权码
     *
     * @param reqVO 登录信息
     * @return 登录结果
     */
    PartnerLoginRespVO socialLogin(@Valid PartnerSocialLoginReqVO reqVO);

    /**
     * 微信小程序一键登录
     *
     * @param reqVO 登录信息
     * @return 登录结果
     */
    PartnerLoginRespVO weixinMiniLogin(@Valid PartnerWxMiniLoginReqVO reqVO);

    /**
     * 刷新访问令牌
     *
     * @param refreshToken 刷新令牌
     * @return 登录结果
     */
    PartnerLoginRespVO refreshToken(String refreshToken);

    /**
     * 给用户发送短信验证码
     *
     * @param reqVO 发送信息
     */
    void sendSmsCode(@Valid PartnerSmsSendReqVO reqVO);

}
