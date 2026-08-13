package vip.appap.suxin.module.partner.service;

import jakarta.validation.Valid;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerMemberGrantReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerMemberPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerMemberRespVO;
import vip.appap.suxin.module.partner.controller.app.vo.AppPartnerMemberConfigRespVO;
import vip.appap.suxin.module.partner.controller.app.vo.AppPartnerMemberRespVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerMemberDO;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;


import java.math.BigDecimal;
import java.util.List;

/**
 * 合作伙伴会员 Service 接口
 *
 * @author 书心软件
 */
public interface PartnerMemberService {

    PartnerMemberDO activateMemberByOrderItem(Long userId, Integer memberType, BigDecimal durationQuantity, Long durationUnitId,
                                              Long orderId, Long orderItemId, Long spuId, Long skuId);

    void activateMemberByPaidOrderItems(Long userId, List<SalesOrderItemDO> orderItems);

    void revokeMemberByRefundedOrderItem(Long userId, SalesOrderItemDO orderItem);

    Long grantMember(@Valid PartnerMemberGrantReqVO reqVO);

    AppPartnerMemberRespVO getMyMembership(Long userId);

    AppPartnerMemberConfigRespVO getMemberConfig();

    PageResult<PartnerMemberRespVO> getMemberPage(PartnerMemberPageReqVO pageReqVO);

    int expireMembers();

    /**
     * 创建会员记录
     *
     * @param partnerId 合作伙伴编号
     * @param ip        注册 IP
     * @param terminal  注册终端
     */
    void createMember(Long partnerId, String ip, Integer terminal);

    /**
     * 获得会员用户的手机号码
     *
     * @param id 会员用户编号
     * @return 手机号码
     */
    String getMemberUserMobile(Long id);

    /**
     * 获得会员用户的邮箱
     *
     * @param id 会员用户编号
     * @return 邮箱
     */
    String getMemberUserEmail(Long id);

}
