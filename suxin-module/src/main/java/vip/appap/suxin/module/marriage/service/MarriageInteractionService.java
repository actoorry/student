package vip.appap.suxin.module.marriage.service;

import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageFollowRespVO;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageInteractionStatisticsRespVO;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageRelationMemberRespVO;

import java.time.LocalDateTime;

public interface MarriageInteractionService {

    MarriageFollowRespVO follow(Long loginUserId, Long relPartnerId);

    void unfollow(Long loginUserId, Long relPartnerId);

    MarriageInteractionStatisticsRespVO getInteractionStatistics(Long loginUserId);

    PageResult<MarriageRelationMemberRespVO> getFollowMePage(Long loginUserId, PageParam pageParam);

    PageResult<MarriageRelationMemberRespVO> getMyFollowPage(Long loginUserId, PageParam pageParam);

    void recordView(Long loginUserId, Long targetPartnerId);

    PageResult<MarriageRelationMemberRespVO> getViewMePage(Long loginUserId, PageParam pageParam);

    PageResult<MarriageRelationMemberRespVO> getMyViewPage(Long loginUserId, PageParam pageParam);

    int generateViewMeSummaryNotifications(LocalDateTime startTime, LocalDateTime endTime);

}
