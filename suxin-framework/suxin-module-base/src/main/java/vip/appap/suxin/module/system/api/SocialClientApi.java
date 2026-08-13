package vip.appap.suxin.module.system.api;

import vip.appap.suxin.module.system.api.dto.*;
import vip.appap.suxin.module.system.enums.SocialTypeEnum;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 社交应用的 API 接口
 *
 * @author 书心软件
 */
public interface SocialClientApi {

    /**
     * 获得社交平台的授权 URL
     *
     * @param socialType  社交平台的类型 {@link SocialTypeEnum}
     * @param userType    用户类型
     * @param redirectUri 重定向 URL
     * @return 社交平台的授权 URL
     */
    String getAuthorizeUrl(Integer socialType, Integer userType, String redirectUri);

    /**
     * 创建微信公众号 JS SDK 初始化所需的签名
     *
     * @param userType 用户类型
     * @param url      访问的 URL 地址
     * @return 签名
     */
    SocialWxJsapiSignatureRespDTO createWxMpJsapiSignature(Integer userType, String url);

    //======================= 微信小程序独有 =======================

    /**
     * 获得微信小程序的手机信息
     *
     * @param userType  用户类型
     * @param phoneCode 手机授权码
     * @return 手机信息
     */
    SocialWxPhoneNumberInfoRespDTO getWxMaPhoneNumberInfo(Integer userType, String phoneCode);

    /**
     * 获得小程序二维码
     *
     * @param reqVO 请求信息
     * @return 小程序二维码
     */
    byte[] getWxaQrcode(@Valid SocialWxQrcodeReqDTO reqVO);

    /**
     * 获得微信小程订阅模板
     *
     * @return 小程序订阅消息模版
     */
    List<SocialWxaSubscribeTemplateRespDTO> getWxaSubscribeTemplateList(Integer userType);

    /**
     * 发送微信小程序订阅消息
     *
     * @param reqDTO 请求
     */
    void sendWxaSubscribeMessage(SocialWxaSubscribeMessageSendReqDTO reqDTO);

    /**
     * 上传订单发货到微信小程序
     *
     * @param userType 用户类型
     * @param reqDTO 请求
     */
    void uploadWxaOrderShippingInfo(Integer userType, SocialWxaOrderUploadShippingInfoReqDTO reqDTO);

    /**
     * 通知订单收货到微信小程序
     *
     * @param userType 用户类型
     * @param reqDTO 请求
     */
    void notifyWxaOrderConfirmReceive(Integer userType, SocialWxaOrderNotifyConfirmReceiveReqDTO reqDTO);

    /**
     * 异步校验图片/音频是否含有违法违规内容
     *
     * @param userType  用户类型
     * @param openid    用户 openid
     * @param mediaUrl  媒体资源 URL
     * @param mediaType 媒体类型（1-音频，2-图片）
     * @param scene     场景值（1-资料，2-评论，3-论坛，4-社交日志）
     * @return trace_id，用于匹配异步推送结果
     */
    String mediaCheckAsync(Integer userType, String openid, String mediaUrl, Integer mediaType, Integer scene);

}
