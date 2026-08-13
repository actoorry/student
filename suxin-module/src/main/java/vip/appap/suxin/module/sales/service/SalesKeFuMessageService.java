package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.sales.controller.admin.vo.SalesKeFuMessageListReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesKeFuMessageSendReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesKeFuMessagePageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesKeFuMessageSendReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesKeFuMessageDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 客服消息 Service 接口
 *
 * @author HUIHUI
 */
public interface SalesKeFuMessageService {

    /**
     * 【管理员】发送客服消息
     *
     * @param sendReqVO 信息
     * @return 编号
     */
    Long sendKefuMessage(@Valid SalesKeFuMessageSendReqVO sendReqVO);

    /**
     * 【会员】发送客服消息
     *
     * @param sendReqVO 信息
     * @return 编号
     */
    Long sendKefuMessage(AppSalesKeFuMessageSendReqVO sendReqVO);

    /**
     * 【管理员】更新消息已读状态
     *
     * @param conversationId 会话编号
     * @param userId         用户编号
     * @param userType       用户类型
     */
    void updateKeFuMessageReadStatus(Long conversationId, Long userId, Integer userType);

    /**
     * 获得客服消息分页
     *
     * @param pageReqVO 分页查询
     * @return 客服消息分页
     */
    List<SalesKeFuMessageDO> getKeFuMessageList(SalesKeFuMessageListReqVO pageReqVO);

    /**
     * 【会员】获得客服消息分页
     *
     * @param pageReqVO 请求
     * @param userId    用户编号
     * @return 客服消息分页
     */
    List<SalesKeFuMessageDO> getKeFuMessageList(AppSalesKeFuMessagePageReqVO pageReqVO, Long userId);

}