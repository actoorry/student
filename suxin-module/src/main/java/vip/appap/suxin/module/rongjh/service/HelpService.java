package vip.appap.suxin.module.rongjh.service;

import vip.appap.suxin.module.rongjh.controller.admin.vo.HelpPageReqVO;
import vip.appap.suxin.module.rongjh.controller.admin.vo.HelpRespVO;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.rongjh.controller.app.vo.AppHelpSubmitReqVO;
import vip.appap.suxin.module.rongjh.controller.app.vo.AppHelpTotalAmountRespVO;
import vip.appap.suxin.module.rongjh.dal.dataobject.PartnerHelpDO;

import java.math.BigDecimal;
import java.util.List;

public interface HelpService {

    void createHelp(Long partnerId, AppHelpSubmitReqVO reqVO);

    void cancelHelp(Long partnerId, Long id);

    List<PartnerHelpDO> getHelpList(Long partnerId);

    PartnerHelpDO getHelp(Long partnerId, Long id);

    AppHelpTotalAmountRespVO getTotalAmount(Long userId);

    PageResult<HelpRespVO> getHelpPage(HelpPageReqVO pageReqVO);

    HelpRespVO getHelpDetail(Long id);

    void approveHelp(Long id, BigDecimal actualAmount, String remark);

    void rejectHelp(Long id, String remark);

}
