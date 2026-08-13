package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.number.MoneyUtils;
import vip.appap.suxin.module.sales.api.dto.SalesRewardActivityMatchRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesRewardActivityCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesRewardActivityPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesRewardActivityUpdateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesRewardActivityDO;
import vip.appap.suxin.module.sales.enums.SalesConditionTypeEnum;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.getSumValue;

/**
 * 满减送活动 Service 接口
 *
 * @author 书心软件
 */
public interface SalesRewardActivityService {

    /**
     * 创建满减送活动
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRewardActivity(@Valid SalesRewardActivityCreateReqVO createReqVO);

    /**
     * 更新满减送活动
     *
     * @param updateReqVO 更新信息
     */
    void updateRewardActivity(@Valid SalesRewardActivityUpdateReqVO updateReqVO);

    /**
     * 关闭满减送活动
     *
     * @param id 活动编号
     */
    void closeRewardActivity(Long id);

    /**
     * 删除满减送活动
     *
     * @param id 编号
     */
    void deleteRewardActivity(Long id);

    /**
     * 获得满减送活动
     *
     * @param id 编号
     * @return 满减送活动
     */
    SalesRewardActivityDO getRewardActivity(Long id);

    /**
     * 获得满减送活动分页
     *
     * @param pageReqVO 分页查询
     * @return 满减送活动分页
     */
    PageResult<SalesRewardActivityDO> getRewardActivityPage(SalesRewardActivityPageReqVO pageReqVO);

    /**
     * 获得 spuId 商品匹配的的满减送活动列表
     *
     * @param spuIds   SPU 编号数组
     * @return 满减送活动列表
     */
    List<SalesRewardActivityMatchRespDTO> getMatchRewardActivityListBySpuIds(Collection<Long> spuIds);

    default String getRewardActivityRuleDescription(Integer conditionType, SalesRewardActivityDO.Rule rule) {
        String description = "";
        if (SalesConditionTypeEnum.PRICE.getType().equals(conditionType)) {
            description += StrUtil.format("满 {} 元", MoneyUtils.fenToYuanStr(rule.getLimit()));
        } else {
            description += StrUtil.format("满 {} 件", rule.getLimit());
        }
        List<String> tips = new ArrayList<>(10);
        if (rule.getDiscountPrice() != null) {
            tips.add(StrUtil.format("减 {}", MoneyUtils.fenToYuanStr(rule.getDiscountPrice())));
        }
        if (Boolean.TRUE.equals(rule.getFreeDelivery())) {
            tips.add("包邮");
        }
        if (rule.getPoint() != null && rule.getPoint() > 0) {
            tips.add(StrUtil.format("送 {} 积分", rule.getPoint()));
        }
        if (CollUtil.isNotEmpty(rule.getGiveCouponTemplateCounts())) {
            tips.add(StrUtil.format("送 {} 张优惠券",
                    getSumValue(rule.getGiveCouponTemplateCounts().values(), count -> count, Integer::sum)));
        }
        return description + StrUtil.join("、", tips);
    }

}
