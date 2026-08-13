package vip.appap.suxin.module.marriage.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMomentCommentCreateReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMomentCommentPageReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMomentCommentRespVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMomentCreateReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMomentLikeReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMomentLikeRespVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMomentPageReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMomentRespVO;
import jakarta.validation.Valid;

public interface PartnerMomentService {

    PageResult<AppPartnerMomentRespVO> getPartnerMomentPage(@Valid AppPartnerMomentPageReqVO pageReqVO,
                                                            Long loginPartnerId);

    PageResult<AppPartnerMomentRespVO> getMyPartnerMomentPage(@Valid AppPartnerMomentPageReqVO pageReqVO,
                                                              Long loginPartnerId);

    Long createPartnerMoment(@Valid AppPartnerMomentCreateReqVO createReqVO, Long loginPartnerId);

    void deletePartnerMoment(Long id, Long loginPartnerId);

    AppPartnerMomentLikeRespVO togglePartnerMomentLike(@Valid AppPartnerMomentLikeReqVO reqVO,
                                                       Long loginPartnerId);

    PageResult<AppPartnerMomentCommentRespVO> getPartnerMomentCommentPage(
            @Valid AppPartnerMomentCommentPageReqVO pageReqVO);

    AppPartnerMomentCommentRespVO createPartnerMomentComment(
            @Valid AppPartnerMomentCommentCreateReqVO createReqVO, Long loginPartnerId);

}
