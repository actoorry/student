package vip.appap.suxin.module.marriage.service;

import vip.appap.suxin.module.marriage.controller.app.vo.MarriagePreferenceUpdateReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageProfileRespVO;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageProfileUpdateReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageUserInfoRespVO;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 婚恋用户认证 Service 接口
 */
public interface MarriageAuthService {

    /**
     * 获取当前登录用户信息
     *
     * @param userId 登录用户编号
     * @return 用户信息
     */
    MarriageUserInfoRespVO getLoginUserInfo(Long userId);

    /**
     * 更新用户头像
     *
     * @param userId 用户编号
     * @param avatarUrl 头像URL
     */
    void updateAvatar(Long userId, String avatarUrl);

    /**
     * 获取当前用户的完整婚恋资料
     *
     * @param userId 用户编号
     * @return 完整资料
     */
    MarriageProfileRespVO getMyProfile(Long userId);

    /**
     * 更新我的资料
     *
     * @param userId 用户编号
     * @param updateVO 更新参数
     */
    void updateMyProfile(Long userId, @Valid MarriageProfileUpdateReqVO updateVO);

    /**
     * 更新择偶条件
     *
     * @param userId 用户编号
     * @param updateVO 更新参数
     */
    void updateMyPreference(Long userId, @Valid MarriagePreferenceUpdateReqVO updateVO);

    /**
     * 更新背景图
     *
     * @param userId 用户编号
     * @param backgroundImage 背景图URL
     */
    void updateBackgroundImage(Long userId, String backgroundImage);

    /**
     * 清除背景图（恢复默认）
     *
     * @param userId 用户编号
     */
    void clearBackgroundImage(Long userId);

    /**
     * 添加相册图片
     *
     * @param userId 用户编号
     * @param imageUrl 图片URL
     * @param type 类型：1-婚恋相册
     */
    void addAlbumImage(Long userId, String imageUrl, Integer type);

    /**
     * 删除相册图片
     *
     * @param userId 用户编号
     * @param imageUrl 图片URL
     * @param type 类型：1-婚恋相册
     */
    void removeAlbumImage(Long userId, String imageUrl, Integer type);

    /**
     * 获取相册图片列表
     *
     * @param userId 用户编号
     * @param type 类型：1-婚恋相册
     * @return 图片URL列表
     */
    List<String> getAlbumImages(Long userId, Integer type);

    /**
     * 获取当前用户的实名认证状态
     *
     * @param userId 用户编号
     * @return 实名认证状态：0-未认证，1-已认证
     */
    Integer getRealVerifiedStatus(Long userId);

    /**
     * 获取当前用户的实名认证信息（姓名+身份证号）
     * 用于婚姻认证页面回显
     *
     * @param userId 用户编号
     * @return 实名认证信息，包含 name 和 maskedIdCard
     */
    MarriageUserInfoRespVO getRealNameInfo(Long userId);

}
