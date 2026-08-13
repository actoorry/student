package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBargainHelpPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBargainHelpCreateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainHelpDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 砍价助力 Service 接口
 *
 * @author 书心软件
 */
public interface SalesBargainHelpService {

    /**
     * 创建砍价助力（帮人砍价）
     *
     * @param userId 用户编号
     * @param reqVO 请求信息
     * @return 砍价助力记录
     */
    SalesBargainHelpDO createBargainHelp(Long userId, AppSalesBargainHelpCreateReqVO reqVO);

    /**
     * 【砍价活动】获得助力人数 Map
     *
     * @param activityIds 活动编号
     * @return 助力人数 Map
     */
    Map<Long, Integer> getBargainHelpUserCountMapByActivity(Collection<Long> activityIds);

    /**
     * 【砍价记录】获得助力人数 Map
     *
     * @param recordIds 记录编号
     * @return 助力人数 Map
     */
    Map<Long, Integer> getBargainHelpUserCountMapByRecord(Collection<Long> recordIds);

    /**
     * 【砍价活动】获得用户的助力次数
     *
     * @param activityId 活动编号
     * @param userId 用户编号
     * @return 助力次数
     */
    Long getBargainHelpCountByActivity(Long activityId, Long userId);

    /**
     * 获得砍价助力分页
     *
     * @param pageReqVO 分页查询
     * @return 砍价助力分页
     */
    PageResult<SalesBargainHelpDO> getBargainHelpPage(SalesBargainHelpPageReqVO pageReqVO);

    /**
     * 获得指定砍价记录编号，对应的砍价助力列表
     *
     * @param recordId 砍价记录编号
     * @return 砍价助力列表
     */
    List<SalesBargainHelpDO> getBargainHelpListByRecordId(Long recordId);

    /**
     * 获得助力记录
     *
     * @param recordId 砍价记录编号
     * @param userId 用户编号
     * @return 助力记录
     */
    SalesBargainHelpDO getBargainHelp(Long recordId, Long userId);

}
