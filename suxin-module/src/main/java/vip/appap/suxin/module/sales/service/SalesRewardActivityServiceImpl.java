package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.product.api.ProductCategoryApi;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.api.dto.SalesRewardActivityMatchRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesRewardActivityBaseVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesRewardActivityCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesRewardActivityPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesRewardActivityUpdateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesRewardActivityDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesRewardActivityMapper;
import vip.appap.suxin.module.sales.enums.SalesProductScopeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.hutool.core.collection.CollUtil.intersectionDistinct;
import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.*;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.*;

/**
 * 满减送活动 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class SalesRewardActivityServiceImpl implements SalesRewardActivityService {

    @Resource
    private SalesRewardActivityMapper rewardActivityMapper;

    @Resource
    private ProductCategoryApi productCategoryApi;
    @Resource
    private ProductSpuApi productSpuApi;

    @Override
    public Long createRewardActivity(SalesRewardActivityCreateReqVO createReqVO) {
        // 1.1 校验商品范围
        validateProductScope(createReqVO.getProductScope(), createReqVO.getProductScopeValues());
        // 1.2 校验商品是否冲突
        validateRewardActivitySpuConflicts(null, createReqVO);

        // 插入
        SalesRewardActivityDO rewardActivity = BeanUtils.toBean(createReqVO, SalesRewardActivityDO.class)
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        rewardActivityMapper.insert(rewardActivity);
        // 返回
        return rewardActivity.getId();
    }

    @Override
    public void updateRewardActivity(SalesRewardActivityUpdateReqVO updateReqVO) {
        // 1.1 校验存在
        SalesRewardActivityDO dbRewardActivity = validateRewardActivityExists(updateReqVO.getId());
        if (dbRewardActivity.getStatus().equals(CommonStatusEnum.DISABLE.getStatus())) { // 已关闭的活动，不能修改噢
            throw exception(REWARD_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED);
        }
        // 1.2 校验商品范围
        validateProductScope(updateReqVO.getProductScope(), updateReqVO.getProductScopeValues());
        // 1.3 校验商品是否冲突
        validateRewardActivitySpuConflicts(updateReqVO.getId(), updateReqVO);

        // 2. 更新
        SalesRewardActivityDO updateObj = BeanUtils.toBean(updateReqVO, SalesRewardActivityDO.class);
        rewardActivityMapper.updateById(updateObj);
    }

    @Override
    public void closeRewardActivity(Long id) {
        // 校验存在
        SalesRewardActivityDO dbRewardActivity = validateRewardActivityExists(id);
        if (dbRewardActivity.getStatus().equals(CommonStatusEnum.DISABLE.getStatus())) { // 已关闭的活动，不能关闭噢
            throw exception(REWARD_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED);
        }

        // 更新
        rewardActivityMapper.updateById(new SalesRewardActivityDO().setId(id).setStatus(CommonStatusEnum.DISABLE.getStatus()));
    }

    @Override
    public void deleteRewardActivity(Long id) {
        // 校验存在
        SalesRewardActivityDO dbRewardActivity = validateRewardActivityExists(id);
        if (dbRewardActivity.getStatus().equals(CommonStatusEnum.ENABLE.getStatus())) { // 未关闭的活动，不能删除噢
            throw exception(REWARD_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED);
        }

        // 删除
        rewardActivityMapper.deleteById(id);
    }

    private SalesRewardActivityDO validateRewardActivityExists(Long id) {
        SalesRewardActivityDO activity = rewardActivityMapper.selectById(id);
        if (activity == null) {
            throw exception(REWARD_ACTIVITY_NOT_EXISTS);
        }
        return activity;
    }

    /**
     * 校验商品参加的活动是否冲突
     *
     * @param id             活动编号
     * @param rewardActivity 请求
     */
    private void validateRewardActivitySpuConflicts(Long id, SalesRewardActivityBaseVO rewardActivity) {
        // 1. 获得开启的所有的活动
        List<SalesRewardActivityDO> list = rewardActivityMapper.selectList(SalesRewardActivityDO::getStatus, CommonStatusEnum.ENABLE.getStatus());
        if (id != null) { // 排除自己这个活动
            list.removeIf(activity -> id.equals(activity.getId()));
        }

        // 2. 完全不允许重叠
        for (SalesRewardActivityDO item : list) {
            // 2.1 校验满减送活动时间是否冲突，如果时段不冲突那么不同的时间段内则可以存在相同的商品范围
            if (!LocalDateTimeUtil.isOverlap(item.getStartTime(), item.getEndTime(),
                    rewardActivity.getStartTime(), rewardActivity.getEndTime())) {
                continue;
            }
            // 2.2 校验商品范围是否重叠
            // 情况一：如果与该时间段内商品范围为全部的活动冲突，或 rewardActivity 商品范围为全部，那么则直接校验不通过
            // 例如说，rewardActivity 是全部活动，结果有个 db 里的 activity 是某个分类，它也是冲突的。也就是说，当前时间段内，有且仅有只能有一个活动！
            if (SalesProductScopeEnum.isAll(item.getProductScope()) ||
                    SalesProductScopeEnum.isAll(rewardActivity.getProductScope())) {
                throw exception(REWARD_ACTIVITY_SCOPE_EXISTS, item.getName(),
                        SalesProductScopeEnum.isAll(item.getProductScope()) ?
                                "该活动商品范围为全部已覆盖包含本活动范围" : "本活动商品范围为全部已覆盖包含了该活动商品范围");
            }
            // 情况二：如果与该时间段内商品范围为类别的活动冲突
            if (SalesProductScopeEnum.isCategory(item.getProductScope())) {
                // 校验分类是否冲突
                if (SalesProductScopeEnum.isCategory(rewardActivity.getProductScope())) {
                    if (!intersectionDistinct(item.getProductScopeValues(), rewardActivity.getProductScopeValues()).isEmpty()) {
                        throw exception(REWARD_ACTIVITY_SCOPE_EXISTS, item.getName(), "商品分类范围重叠");
                    }
                }
                // 校验商品分类是否冲突
                if (SalesProductScopeEnum.isSpu(rewardActivity.getProductScope())) {
                    List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(rewardActivity.getProductScopeValues());
                    if (!intersectionDistinct(item.getProductScopeValues(),
                            convertSet(spuList, ProductSpuRespDTO::getCategorySales)).isEmpty()) {
                        throw exception(REWARD_ACTIVITY_SCOPE_EXISTS, item.getName(), "该活动商品分类范围已包含本活动所选商品");
                    }
                }
            }
            // 情况三：如果与该时间段内商品范围为商品的活动冲突
            if (SalesProductScopeEnum.isSpu(item.getProductScope())) {
                // 校验商品是否冲突
                if (SalesProductScopeEnum.isSpu(rewardActivity.getProductScope())) {
                    if (!intersectionDistinct(item.getProductScopeValues(), rewardActivity.getProductScopeValues()).isEmpty()) {
                        throw exception(REWARD_ACTIVITY_SCOPE_EXISTS, item.getName(), "活动商品范围所选商品重叠");
                    }
                }
                // 校验商品分类是否冲突
                if (SalesProductScopeEnum.isCategory(rewardActivity.getProductScope())) {
                    List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(item.getProductScopeValues());
                    if (!intersectionDistinct(rewardActivity.getProductScopeValues(),
                            convertSet(spuList, ProductSpuRespDTO::getCategorySales)).isEmpty()) {
                        throw exception(REWARD_ACTIVITY_SCOPE_EXISTS, item.getName(), "本活动商品分类范围包含了该活动所选商品");
                    }
                }
            }
        }
    }

    private void validateProductScope(Integer productScope, List<Long> productScopeValues) {
        if (Objects.equals(SalesProductScopeEnum.SPU.getScope(), productScope)) {
            productSpuApi.validateSpuList(productScopeValues);
        } else if (Objects.equals(SalesProductScopeEnum.CATEGORY.getScope(), productScope)) {
            productCategoryApi.validateCategoryList(productScopeValues);
        }
    }

    @Override
    public SalesRewardActivityDO getRewardActivity(Long id) {
        return rewardActivityMapper.selectById(id);
    }

    @Override
    public PageResult<SalesRewardActivityDO> getRewardActivityPage(SalesRewardActivityPageReqVO pageReqVO) {
        return rewardActivityMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SalesRewardActivityMatchRespDTO> getMatchRewardActivityListBySpuIds(Collection<Long> spuIds) {
        // 1. 查询商品分类
        List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(spuIds);
        if (CollUtil.isEmpty(spuList)) {
            return Collections.emptyList();
        }
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);

        // 2. 查询出指定 spuId 的 spu 参加的活动
        List<SalesRewardActivityDO> activityList = rewardActivityMapper.selectListBySpuIdAndStatusAndNow(
                spuIds, convertSet(spuList, ProductSpuRespDTO::getCategorySales), CommonStatusEnum.ENABLE.getStatus());
        if (CollUtil.isEmpty(activityList)) {
            return Collections.emptyList();
        }

        // 3. 转换成 Response DTO
        return convertList(activityList, activity -> {
            SalesRewardActivityMatchRespDTO activityDTO = BeanUtils.toBean(activity, SalesRewardActivityMatchRespDTO.class);
            // 3.1 设置对应匹配的 spuIds
            activityDTO.setSpuIds(new ArrayList<>());
            for (Long spuId : spuIds) {
                if (SalesProductScopeEnum.isAll(activityDTO.getProductScope())) {
                    activityDTO.getSpuIds().add(spuId);
                } else if (SalesProductScopeEnum.isSpu(activityDTO.getProductScope())) {
                    if (CollUtil.contains(activityDTO.getProductScopeValues(), spuId)) {
                        activityDTO.getSpuIds().add(spuId);
                    }
                } else if (SalesProductScopeEnum.isCategory(activityDTO.getProductScope())) {
                    ProductSpuRespDTO spu = spuMap.get(spuId);
                    if (spu != null && CollUtil.contains(activityDTO.getProductScopeValues(), spu.getCategorySales())) {
                        activityDTO.getSpuIds().add(spuId);
                    }
                }
            }

            // 3.2 设置每个 Rule 的描述
            activityDTO.setRules(convertList(activity.getRules(), rule ->
                    BeanUtils.toBean(rule, SalesRewardActivityMatchRespDTO.Rule.class)
                            .setDescription(getRewardActivityRuleDescription(activityDTO.getConditionType(), rule))));
            return activityDTO;
        });
    }

}
