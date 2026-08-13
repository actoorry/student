package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBrokerageRecordPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageProductPriceRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageUserRankByPriceRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageUserRankPageReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageRecordDO;
import vip.appap.suxin.module.sales.enums.SalesBrokerageRecordBizTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesBrokerageRecordStatusEnum;
import vip.appap.suxin.module.sales.service.bo.SalesBrokerageAddReqBO;
import vip.appap.suxin.module.sales.service.bo.SalesBrokerageUserSummaryRespBO;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 佣金记录 Service 接口
 *
 * @author owen
 */
public interface SalesBrokerageRecordService {

    /**
     * 获得佣金记录
     *
     * @param id 编号
     * @return 佣金记录
     */
    SalesBrokerageRecordDO getBrokerageRecord(Long id);

    /**
     * 获得佣金记录分页
     *
     * @param pageReqVO 分页查询
     * @return 佣金记录分页
     */
    PageResult<SalesBrokerageRecordDO> getBrokerageRecordPage(SalesBrokerageRecordPageReqVO pageReqVO);

    /**
     * 增加佣金【多级分佣】
     *
     * @param userId  会员编号
     * @param bizType 业务类型
     * @param list    请求参数列表
     */
    void addBrokerage(Long userId, SalesBrokerageRecordBizTypeEnum bizType, @Valid List<SalesBrokerageAddReqBO> list);

    /**
     * 增加佣金【只针对自己】
     *
     * @param userId         会员编号
     * @param bizType        业务类型
     * @param bizId          业务编号
     * @param brokeragePrice 佣金
     * @param title          标题
     */
    void addBrokerage(Long userId, SalesBrokerageRecordBizTypeEnum bizType, String bizId, Integer brokeragePrice, String title);

    /**
     * 减少佣金【只针对自己】
     *
     * @param userId         会员编号
     * @param bizType        业务类型
     * @param bizId          业务编号
     * @param brokeragePrice 佣金
     * @param title          标题
     */
    default void reduceBrokerage(Long userId, SalesBrokerageRecordBizTypeEnum bizType, String bizId, Integer brokeragePrice, String title) {
        addBrokerage(userId, bizType, bizId, -brokeragePrice, title);
    }

    /**
     * 取消佣金：将佣金记录，状态修改为已失效
     *
     * @param bizType 业务类型
     * @param bizId   业务编号
     */
    void cancelBrokerage(SalesBrokerageRecordBizTypeEnum bizType, String bizId);

    /**
     * 解冻佣金：将待结算的佣金记录，状态修改为已结算
     *
     * @return 解冻佣金的数量
     */
    int unfreezeRecord();

    /**
     * 按照 userId，汇总每个用户的佣金
     *
     * @param userIds 用户编号
     * @param bizType 业务类型
     * @param status  佣金状态
     * @return 用户佣金汇总 List
     */
    List<SalesBrokerageUserSummaryRespBO> getUserBrokerageSummaryListByUserId(Collection<Long> userIds,
                                                                         Integer bizType, Integer status);

    /**
     * 按照 userId，汇总每个用户的佣金
     *
     * @param userIds 用户编号
     * @param bizType 业务类型
     * @param status  佣金状态
     * @return 用户佣金汇总 Map
     */
    default Map<Long, SalesBrokerageUserSummaryRespBO> getUserBrokerageSummaryMapByUserId(Collection<Long> userIds,
                                                                                     Integer bizType, Integer status) {
        return convertMap(getUserBrokerageSummaryListByUserId(userIds, bizType, status),
                SalesBrokerageUserSummaryRespBO::getUserId);
    }

    /**
     * 获得用户佣金合计
     *
     * @param userId    用户编号
     * @param bizType   业务类型
     * @param status    状态
     * @param beginTime 开始时间
     * @param endTime   截止时间
     * @return 用户佣金合计
     */
    Integer getSummaryPriceByUserId(Long userId, SalesBrokerageRecordBizTypeEnum bizType, SalesBrokerageRecordStatusEnum status,
                                    LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获得用户佣金排行分页列表（基于佣金总数）
     *
     * @param pageReqVO 分页查询
     * @return 排行榜分页
     */
    PageResult<AppSalesBrokerageUserRankByPriceRespVO> getBrokerageUserChildSummaryPageByPrice(
            AppSalesBrokerageUserRankPageReqVO pageReqVO);

    /**
     * 获取用户的排名（基于佣金总数）
     *
     * @param userId 用户编号
     * @param times  时间范围
     * @return 用户的排名
     */
    Integer getUserRankByPrice(Long userId, LocalDateTime[] times);

    /**
     * 计算商品被购买后，推广员可以得到的佣金
     *
     * @param userId 用户编号
     * @param spuId  商品编号
     * @return 用户佣金
     */
    AppSalesBrokerageProductPriceRespVO calculateProductBrokeragePrice(Long userId, Long spuId);

}
