package vip.appap.suxin.module.accountant.service;

import vip.appap.suxin.module.accountant.controller.app.vo.AppWechatVirtualPayResultRespVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppWechatVirtualPayCheckoutProfileRespVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppWechatVirtualPaySubmitReqVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppWechatVirtualPaySubmitRespVO;

import java.util.Map;

public interface WechatVirtualPayService {

    AppWechatVirtualPaySubmitRespVO submitWechatVirtualPay(Long userId, AppWechatVirtualPaySubmitReqVO reqVO,
                                                           String userIp);

    AppWechatVirtualPayResultRespVO getWechatVirtualPayResult(Long userId, Long payOrderId);

    AppWechatVirtualPayCheckoutProfileRespVO getWechatVirtualPayCheckoutProfile(Long userId, Long payOrderId);

    Map<String, Object> notifyGoodsDeliver(Map<String, Object> body);

    Map<String, Object> notifyGoodsDeliver(String rawBody, boolean xml);

    Map<String, Object> notifyComplaint(Map<String, Object> body);

    int syncPendingWechatVirtualPayOrders();

}
