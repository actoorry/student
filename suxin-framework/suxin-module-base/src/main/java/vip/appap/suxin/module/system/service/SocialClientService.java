package vip.appap.suxin.module.system.service;

import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.system.api.dto.SocialWxQrcodeReqDTO;
import vip.appap.suxin.module.system.api.dto.SocialWxaOrderNotifyConfirmReceiveReqDTO;
import vip.appap.suxin.module.system.api.dto.SocialWxaOrderUploadShippingInfoReqDTO;
import vip.appap.suxin.module.system.api.dto.SocialWxaSubscribeMessageSendReqDTO;
import vip.appap.suxin.module.system.controller.admin.vo.SocialClientPageReqVO;
import vip.appap.suxin.module.system.controller.admin.vo.SocialClientSaveReqVO;
import vip.appap.suxin.module.system.dal.dataobject.SocialClientDO;
import vip.appap.suxin.module.system.enums.SocialTypeEnum;
import jakarta.validation.Valid;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.bean.subscribemsg.TemplateInfo;
import me.zhyd.oauth.model.AuthUser;

import java.util.List;

/**
 * 社交应用 Service 接口
 *
 * @author 书心软件
 */
public interface SocialClientService {

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
     * 请求社交平台，获得授权的用户
     *
     * @param socialType 社交平台的类型
     * @param userType   用户类型
     * @param code       授权码
     * @param state      授权 state
     * @return 授权的用户
     */
    AuthUser getAuthUser(Integer socialType, Integer userType, String code, String state);

    // =================== 微信公众号独有 ===================

    /**
     * 创建微信公众号的 JS SDK 初始化所需的签名
     *
     * @param userType 用户类型
     * @param url      访问的 URL 地址
     * @return 签名
     */
    WxJsapiSignature createWxMpJsapiSignature(Integer userType, String url);

    // =================== 微信小程序独有 ===================

    /**
     * 获得微信小程序的手机信息
     *
     * @param userType  用户类型
     * @param phoneCode 手机授权码
     * @return 手机信息
     */
    WxMaJscode2SessionResult getWxMaSessionInfo(Integer userType, String code);

    WxMaPhoneNumberInfo getWxMaPhoneNumberInfo(Integer userType, String phoneCode);

    /**
     * 获得微信小程序访问令牌。
     *
     * @param userType 用户类型
     * @return access token
     */
    String getWxMaAccessToken(Integer userType);

    /**
     * 获得微信小程序访问令牌。
     *
     * @param userType     用户类型
     * @param forceRefresh 是否强制刷新缓存令牌
     * @return access token
     */
    String getWxMaAccessToken(Integer userType, boolean forceRefresh);

    /**
     * 获得小程序二维码
     *
     * @param reqVO 请求信息
     * @return 小程序二维码
     */
    byte[] getWxaQrcode(SocialWxQrcodeReqDTO reqVO);

    /**
     * 获得微信小程订阅模板
     *
     * 缓存的目的：考虑到微信小程序订阅消息选择好模版后几乎不会变动，缓存增加查询效率
     *
     * @param userType 用户类型
     * @return 微信小程订阅模板
     */
    List<TemplateInfo> getSubscribeTemplateList(Integer userType);

    /**
     * 发送微信小程序订阅消息
     *
     * @param reqDTO     请求
     * @param templateId 模版编号
     * @param openId     会员 openId
     */
    void sendSubscribeMessage(SocialWxaSubscribeMessageSendReqDTO reqDTO, String templateId, String openId);

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

    // =================== 微信内容安全 ===================

    /**
     * 异步校验图片/音频是否含有违法违规内容
     *
     * 参考文档：https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/sec-check/security.mediaCheckAsync.html
     *
     * @param userType  用户类型
     * @param openid    用户 openid
     * @param mediaUrl  媒体资源 URL
     * @param mediaType 媒体类型（1-音频，2-图片）
     * @param scene     场景值（1-资料，2-评论，3-论坛，4-社交日志）
     * @return trace_id，用于匹配异步推送结果
     */
    String mediaCheckAsync(Integer userType, String openid, String mediaUrl, Integer mediaType, Integer scene);

    // =================== 客户端管理 ===================

    /**
     * 创建社交客户端
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSocialClient(@Valid SocialClientSaveReqVO createReqVO);

    /**
     * 更新社交客户端
     *
     * @param updateReqVO 更新信息
     */
    void updateSocialClient(@Valid SocialClientSaveReqVO updateReqVO);

    /**
     * 删除社交客户端
     *
     * @param id 编号
     */
    void deleteSocialClient(Long id);

    /**
     * 批量删除社交客户端
     *
     * @param ids 编号数组
     */
    void deleteSocialClientList(List<Long> ids);

    /**
     * 获得社交客户端
     *
     * @param id 编号
     * @return 社交客户端
     */
    SocialClientDO getSocialClient(Long id);

    /**
     * 获得社交客户端分页
     *
     * @param pageReqVO 分页查询
     * @return 社交客户端分页
     */
    PageResult<SocialClientDO> getSocialClientPage(SocialClientPageReqVO pageReqVO);

}
