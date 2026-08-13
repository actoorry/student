package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBrokerageUserCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBrokerageUserPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageUserChildSummaryPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageUserChildSummaryRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageUserRankByUserCountRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageUserRankPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageUserDO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 分销用户 Service 接口
 *
 * @author owen
 */
public interface SalesBrokerageUserService {

    /**
     * 获得分销用户
     *
     * @param id 编号
     * @return 分销用户
     */
    SalesBrokerageUserDO getBrokerageUser(Long id);

    /**
     * 获得分销用户分页
     *
     * @param pageReqVO 分页查询
     * @return 分销用户分页
     */
    PageResult<SalesBrokerageUserDO> getBrokerageUserPage(SalesBrokerageUserPageReqVO pageReqVO);

    /**
     * 修改推广员编号
     *
     * @param id         用户编号
     * @param bindUserId 推广员编号
     */
    void updateBrokerageUserId(Long id, Long bindUserId);

    /**
     * 修改推广资格
     *
     * @param id      用户编号
     * @param enabled 推广资格
     */
    void updateBrokerageUserEnabled(Long id, Boolean enabled);

    /**
     * 获得用户的推广人
     *
     * @param id 用户编号
     * @return 用户的推广人
     */
    SalesBrokerageUserDO getBindBrokerageUser(Long id);

    /**
     * 获得或创建分销用户
     *
     * @param id 用户编号
     * @return 分销用户
     */
    SalesBrokerageUserDO getOrCreateBrokerageUser(Long id);

    /**
     * 更新用户佣金
     *
     * @param id    用户编号
     * @param price 用户可用佣金
     * @return 更新结果
     */
    boolean updateUserPrice(Long id, Integer price);

    /**
     * 更新用户冻结佣金
     *
     * @param id          用户编号
     * @param frozenPrice 用户冻结佣金
     */
    void updateUserFrozenPrice(Long id, Integer frozenPrice);

    /**
     * 更新用户冻结佣金（减少），更新用户佣金（增加）
     *
     * @param id          用户编号
     * @param frozenPrice 减少冻结佣金（负数）
     */
    void updateFrozenPriceDecrAndPriceIncr(Long id, Integer frozenPrice);

    /**
     * 获得推广用户数量
     *
     * @param bindUserId 绑定的推广员编号
     * @param level      推广用户等级
     * @return 推广用户数量
     */
    Long getBrokerageUserCountByBindUserId(Long bindUserId, Integer level);

    /**
     * 【会员】绑定推广员
     *
     * @param userId     用户编号
     * @param bindUserId 推广员编号
     * @return 是否绑定
     */
    boolean bindBrokerageUser(@NotNull Long userId, @NotNull Long bindUserId);

    /**
     * 【管理员】创建分销用户
     *
     * @param createReqVO 请求
     * @return 编号
     */
    Long createBrokerageUser(@Valid SalesBrokerageUserCreateReqVO createReqVO);

    /**
     * 申请成为分销用户
     *
     * @param userId 用户编号
     */
    void applyBrokerageUser(@NotNull Long userId);

    /**
     * 获取用户是否有分销资格
     *
     * @param userId 用户编号
     * @return 是否有分销资格
     */
    Boolean getUserBrokerageEnabled(Long userId);

    /**
     * 获得推广人排行
     *
     * @param pageReqVO 分页查询
     * @return 推广人排行
     */
    PageResult<AppSalesBrokerageUserRankByUserCountRespVO> getBrokerageUserRankPageByUserCount(AppSalesBrokerageUserRankPageReqVO pageReqVO);

    /**
     * 获得下级分销统计分页
     *
     * @param pageReqVO 分页查询
     * @param userId    用户编号
     * @return 下级分销统计分页
     */
    PageResult<AppSalesBrokerageUserChildSummaryRespVO> getBrokerageUserChildSummaryPage(AppSalesBrokerageUserChildSummaryPageReqVO pageReqVO, Long userId);

}
