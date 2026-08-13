package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiscountActivityCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiscountActivityPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiscountActivityUpdateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiscountActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiscountProductDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * 限时折扣 Service 接口
 *
 * @author 书心软件
 */
public interface SalesDiscountActivityService {

    /**
     * 基于指定 SKU 编号数组，获得匹配的限时折扣商品
     *
     * 注意，匹配的条件，仅仅是日期符合，并且处于开启状态
     *
     * @param skuIds SKU 编号数组
     * @return 匹配的限时折扣商品
     */
    List<SalesDiscountProductDO> getMatchDiscountProductListBySkuIds(Collection<Long> skuIds);

    /**
     * 创建限时折扣活动
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDiscountActivity(@Valid SalesDiscountActivityCreateReqVO createReqVO);

    /**
     * 更新限时折扣活动
     *
     * @param updateReqVO 更新信息
     */
    void updateDiscountActivity(@Valid SalesDiscountActivityUpdateReqVO updateReqVO);

    /**
     * 关闭限时折扣活动
     *
     * @param id 编号
     */
    void closeDiscountActivity(Long id);

    /**
     * 删除限时折扣活动
     *
     * @param id 编号
     */
    void deleteDiscountActivity(Long id);

    /**
     * 获得限时折扣活动
     *
     * @param id 编号
     * @return 限时折扣活动
     */
    SalesDiscountActivityDO getDiscountActivity(Long id);

    /**
     * 获得限时折扣活动分页
     *
     * @param pageReqVO 分页查询
     * @return 限时折扣活动分页
     */
    PageResult<SalesDiscountActivityDO> getDiscountActivityPage(SalesDiscountActivityPageReqVO pageReqVO);

    /**
     * 获得活动编号，对应对应的商品列表
     *
     * @param activityId 活动编号
     * @return 活动的商品列表
     */
    List<SalesDiscountProductDO> getDiscountProductsByActivityId(Long activityId);

    /**
     * 获得活动编号，对应对应的商品列表
     *
     * @param activityIds 活动编号
     * @return 活动的商品列表
     */
    List<SalesDiscountProductDO> getDiscountProductsByActivityId(Collection<Long> activityIds);

}
