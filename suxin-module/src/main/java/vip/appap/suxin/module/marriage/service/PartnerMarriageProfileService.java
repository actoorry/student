package vip.appap.suxin.module.marriage.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMarriageProfilePageReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMarriageProfileRespVO;
import jakarta.validation.Valid;

public interface PartnerMarriageProfileService {

    /**
     * 获取首页推荐会员分页
     *
     * @param pageReqVO     分页参数
     * @param loginUserId   当前登录用户ID（可为null）
     * @param loginUserSex  当前登录用户性别（可为null，null时不过滤性别）
     * @return 推荐会员列表
     */
    PageResult<AppPartnerMarriageProfileRespVO> getPartnerMarriageProfilePage(@Valid AppPartnerMarriageProfilePageReqVO pageReqVO,
                                                                              Long loginUserId,
                                                                              Integer loginUserSex);

    AppPartnerMarriageProfileRespVO getPartnerMarriageProfile(Long id);

    /**
     * 清除推荐会员缓存
     */
    void clearRecommendCache();

}
