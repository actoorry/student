package vip.appap.suxin.module.hr.service.positionchange;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.hr.controller.admin.positionchange.vo.*;

import java.util.List;

public interface PositionChangeService {

    Long createPositionChange(PositionChangeSaveReqVO createReqVO);

    void updatePositionChange(PositionChangeSaveReqVO updateReqVO);

    void deletePositionChange(Long id);

    PositionChangeRespVO getPositionChange(Long id);

    PageResult<PositionChangeRespVO> getPositionChangePage(PositionChangePageReqVO pageReqVO);

    List<PositionTimelineRespVO> getPositionTimeline(Long partnerId);

}
