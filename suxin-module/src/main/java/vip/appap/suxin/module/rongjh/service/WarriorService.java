package vip.appap.suxin.module.rongjh.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.rongjh.controller.admin.vo.WarriorPageReqVO;
import vip.appap.suxin.module.rongjh.controller.admin.vo.WarriorRespVO;
import vip.appap.suxin.module.rongjh.controller.app.vo.AppWarriorStatusRespVO;
import vip.appap.suxin.module.rongjh.controller.app.vo.AppWarriorSubmitReqVO;
import vip.appap.suxin.module.rongjh.dal.dataobject.PartnerWarriorDO;

public interface WarriorService {

    // ========== App ==========

    void submitWarrior(Long partnerId, AppWarriorSubmitReqVO reqVO);

    PartnerWarriorDO getWarrior(Long partnerId);

    AppWarriorStatusRespVO getWarriorStatus(Long partnerId, Integer filterStateId);

    // ========== Admin ==========

    PageResult<WarriorRespVO> getWarriorPage(WarriorPageReqVO reqVO);

    WarriorRespVO getWarriorDetail(Long id);

    void approveWarrior(Long id, String remark);

    void rejectWarrior(Long id, String remark);

    void blacklistWarrior(Long id, String remark);

}
