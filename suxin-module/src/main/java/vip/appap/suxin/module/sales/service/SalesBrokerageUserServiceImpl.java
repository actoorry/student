package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.date.LocalDateTimeUtils;
import vip.appap.suxin.framework.common.pojo.SortingField;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.mybatis.core.util.MyBatisUtils;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBrokerageUserCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBrokerageUserPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageUserChildSummaryPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageUserChildSummaryRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageUserRankByUserCountRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageUserRankPageReqVO;
import vip.appap.suxin.module.sales.convert.SalesBrokerageUserConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageUserDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesConfigDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesBrokerageUserMapper;
import vip.appap.suxin.module.sales.enums.SalesBrokerageBindModeEnum;
import vip.appap.suxin.module.sales.enums.SalesBrokerageEnabledConditionEnum;
import vip.appap.suxin.module.sales.enums.SalesBrokerageRecordBizTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesBrokerageRecordStatusEnum;
import vip.appap.suxin.module.sales.service.SalesConfigService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMapByFilter;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.*;

/**
 * 分销用户 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class SalesBrokerageUserServiceImpl implements SalesBrokerageUserService {

    @Resource
    private SalesBrokerageUserMapper salesBrokerageUserMapper;

    @Resource
    private SalesConfigService salesConfigService;

    @Resource
    private PartnerApi PartnerApi;

    @Override
    public SalesBrokerageUserDO getBrokerageUser(Long id) {
        return salesBrokerageUserMapper.selectById(id);
    }

    @Override
    public PageResult<SalesBrokerageUserDO> getBrokerageUserPage(SalesBrokerageUserPageReqVO pageReqVO) {
        List<Long> childIds = getChildUserIdsByLevel(pageReqVO.getBindUserId(), pageReqVO.getLevel());
        // 有"绑定用户编号"查询条件时，没有查到下级会员，直接返回空
        if (pageReqVO.getBindUserId() != null && CollUtil.isEmpty(childIds)) {
            return PageResult.empty();
        }
        return salesBrokerageUserMapper.selectPage(pageReqVO, childIds);
    }

    @Override
    public void updateBrokerageUserId(Long id, Long bindUserId) {
        // 校验存在
        SalesBrokerageUserDO brokerageUser = validateBrokerageUserExists(id);
        // 绑定关系未发生变化
        if (Objects.equals(brokerageUser.getBindUserId(), bindUserId)) {
            return;
        }

        // 情况一：清除推广员
        if (bindUserId == null) {
            // 清除推广员
            salesBrokerageUserMapper.updateBindUserIdAndBindUserTimeToNull(id);
            return;
        }

        // 情况二：修改推广员
        validateCanBindUser(brokerageUser, bindUserId);
        salesBrokerageUserMapper.updateById(fillBindUserData(bindUserId, new SalesBrokerageUserDO().setId(id)));
    }

    @Override
    public void updateBrokerageUserEnabled(Long id, Boolean enabled) {
        // 校验存在
        validateBrokerageUserExists(id);
        if (BooleanUtil.isTrue(enabled)) {
            // 开通推广资格
            salesBrokerageUserMapper.updateById(new SalesBrokerageUserDO().setId(id)
                    .setBrokerageEnabled(true).setBrokerageTime(LocalDateTime.now()));
        } else {
            // 取消推广资格
            salesBrokerageUserMapper.updateEnabledFalseAndBrokerageTimeToNull(id);
        }
    }

    private SalesBrokerageUserDO validateBrokerageUserExists(Long id) {
        SalesBrokerageUserDO brokerageUserDO = salesBrokerageUserMapper.selectById(id);
        if (brokerageUserDO == null) {
            throw exception(BROKERAGE_USER_NOT_EXISTS);
        }
        return brokerageUserDO;
    }

    @Override
    public SalesBrokerageUserDO getBindBrokerageUser(Long id) {
        return Optional.ofNullable(id)
                .map(this::getBrokerageUser)
                .map(SalesBrokerageUserDO::getBindUserId)
                .map(this::getBrokerageUser)
                .orElse(null);
    }

    @Override
    public SalesBrokerageUserDO getOrCreateBrokerageUser(Long id) {
        SalesBrokerageUserDO brokerageUser = salesBrokerageUserMapper.selectById(id);
        // 特殊：人人分销的情况下，如果分销人为空则创建分销人
        if (brokerageUser == null && ObjUtil.equal(SalesBrokerageEnabledConditionEnum.ALL.getCondition(),
                salesConfigService.getTradeConfig().getBrokerageEnabledCondition())) {
            brokerageUser = new SalesBrokerageUserDO().setId(id).setBrokerageEnabled(true).setBrokeragePrice(0)
                    .setBrokerageTime(LocalDateTime.now()).setFrozenPrice(0);
            salesBrokerageUserMapper.insert(brokerageUser);
        }
        return brokerageUser;
    }

    @Override
    public boolean updateUserPrice(Long id, Integer price) {
        if (price > 0) {
            salesBrokerageUserMapper.updatePriceIncr(id, price);
        } else if (price < 0) {
            return salesBrokerageUserMapper.updatePriceDecr(id, price) > 0;
        }
        return true;
    }

    @Override
    public void updateUserFrozenPrice(Long id, Integer frozenPrice) {
        if (frozenPrice > 0) {
            salesBrokerageUserMapper.updateFrozenPriceIncr(id, frozenPrice);
        } else if (frozenPrice < 0) {
            salesBrokerageUserMapper.updateFrozenPriceDecr(id, frozenPrice);
        }
    }

    @Override
    public void updateFrozenPriceDecrAndPriceIncr(Long id, Integer frozenPrice) {
        Assert.isTrue(frozenPrice < 0);
        int updateRows = salesBrokerageUserMapper.updateFrozenPriceDecrAndPriceIncr(id, frozenPrice);
        if (updateRows == 0) {
            throw exception(BROKERAGE_USER_FROZEN_PRICE_NOT_ENOUGH);
        }
    }

    @Override
    public Long getBrokerageUserCountByBindUserId(Long bindUserId, Integer level) {
        List<Long> childIds = getChildUserIdsByLevel(bindUserId, level);
        return (long) CollUtil.size(childIds);
    }

    @Override
    public boolean bindBrokerageUser(Long userId, Long bindUserId) {
        // 1. 获得分销用户
        boolean isNewBrokerageUser = false;
        SalesBrokerageUserDO brokerageUser = salesBrokerageUserMapper.selectById(userId);
        if (brokerageUser == null) { // 分销用户不存在的情况：1. 新注册；2. 旧数据；3. 分销功能关闭后又打开
            isNewBrokerageUser = true;
            brokerageUser = new SalesBrokerageUserDO().setId(userId).setBrokerageEnabled(false).setBrokeragePrice(0).setFrozenPrice(0);
        }

        // 2.1 校验是否能绑定用户
        boolean validated = isUserCanBind(brokerageUser);
        if (!validated) {
            return false;
        }
        // 2.3 校验能否绑定
        validateCanBindUser(brokerageUser, bindUserId);
        // 2.3 绑定用户
        if (isNewBrokerageUser) {
            Integer enabledCondition = salesConfigService.getTradeConfig().getBrokerageEnabledCondition();
            if (SalesBrokerageEnabledConditionEnum.ALL.getCondition().equals(enabledCondition)) { // 人人分销：用户默认就有分销资格
                brokerageUser.setBrokerageEnabled(true).setBrokerageTime(LocalDateTime.now());
            } else {
                brokerageUser.setBrokerageEnabled(false).setBrokerageTime(LocalDateTime.now());
            }
            salesBrokerageUserMapper.insert(fillBindUserData(bindUserId, brokerageUser));
        } else {
            salesBrokerageUserMapper.updateById(fillBindUserData(bindUserId, new SalesBrokerageUserDO().setId(userId)));
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBrokerageUser(SalesBrokerageUserCreateReqVO createReqVO) {
        // 1.1 校验分销用户是否已存在
        SalesBrokerageUserDO brokerageUser = salesBrokerageUserMapper.selectById(createReqVO.getUserId());
        if (brokerageUser != null) {
            throw exception(BROKERAGE_CREATE_USER_EXISTS);
        }
        // 1.2 校验是否能绑定用户
        brokerageUser = BeanUtils.toBean(createReqVO, SalesBrokerageUserDO.class).setId(createReqVO.getUserId())
                .setBrokerageTime(LocalDateTime.now());
        validateCanBindUser(brokerageUser, createReqVO.getBindUserId());

        // 2. 创建分销人
        salesBrokerageUserMapper.insert(brokerageUser);
        return brokerageUser.getId();
    }

    /**
     * 补全绑定用户的字段
     *
     * @param bindUserId    绑定的用户编号
     * @param brokerageUser update 对象
     * @return 补全后的 update 对象
     */
    private SalesBrokerageUserDO fillBindUserData(Long bindUserId, SalesBrokerageUserDO brokerageUser) {
        return brokerageUser.setBindUserId(bindUserId).setBindUserTime(LocalDateTime.now());
    }

    @Override
    public void applyBrokerageUser(Long userId) {
        // 1. 如果分销用户已存在，直接返回（幂等）
        SalesBrokerageUserDO brokerageUser = salesBrokerageUserMapper.selectById(userId);
        if (brokerageUser != null) {
            return;
        }

        // 2. 创建分销用户，默认关闭推广资格，等待后台开通
        brokerageUser = new SalesBrokerageUserDO().setId(userId)
                .setBrokerageEnabled(false).setBrokeragePrice(0).setFrozenPrice(0);
        salesBrokerageUserMapper.insert(brokerageUser);
    }

    @Override
    public Boolean getUserBrokerageEnabled(Long userId) {
        // 全局分销功能是否开启
        SalesConfigDO salesConfig = salesConfigService.getTradeConfig();
        if (salesConfig == null || BooleanUtil.isFalse(salesConfig.getBrokerageEnabled())) {
            return false;
        }

        // 用户是否有分销资格
        return Optional.ofNullable(getBrokerageUser(userId))
                .map(SalesBrokerageUserDO::getBrokerageEnabled)
                .orElse(false);
    }

    @Override
    public PageResult<AppSalesBrokerageUserRankByUserCountRespVO> getBrokerageUserRankPageByUserCount(AppSalesBrokerageUserRankPageReqVO pageReqVO) {
        IPage<AppSalesBrokerageUserRankByUserCountRespVO> pageResult = salesBrokerageUserMapper.selectCountPageGroupByBindUserId(MyBatisUtils.buildPage(pageReqVO),
                ArrayUtil.get(pageReqVO.getTimes(), 0), ArrayUtil.get(pageReqVO.getTimes(), 1));
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }

    @Override
    public PageResult<AppSalesBrokerageUserChildSummaryRespVO> getBrokerageUserChildSummaryPage(AppSalesBrokerageUserChildSummaryPageReqVO pageReqVO, Long userId) {
        // 1.1 查询下级用户编号列表
        List<Long> childIds = getChildUserIdsByLevel(userId, pageReqVO.getLevel());
        if (CollUtil.isEmpty(childIds)) {
            return PageResult.empty();
        }
        // 1.2 根据昵称过滤下级用户
        List<PartnerRespDTO> users = PartnerApi.getUserList(childIds);
        String nickname = pageReqVO.getNickname();
        Map<Long, PartnerRespDTO> userMap = convertMapByFilter(users,
                user -> StrUtil.isBlank(nickname) || StrUtil.contains(user.getNickname(), nickname),
                PartnerRespDTO::getId);
        if (CollUtil.isEmpty(userMap)) {
            return PageResult.empty();
        }

        // 2. 分页查询
        IPage<AppSalesBrokerageUserChildSummaryRespVO> pageResult = salesBrokerageUserMapper.selectSummaryPageByUserId(
                MyBatisUtils.buildPage(pageReqVO), SalesBrokerageRecordBizTypeEnum.ORDER.getType(),
                SalesBrokerageRecordStatusEnum.SETTLEMENT.getStatus(), userMap.keySet(), new SortingField(pageReqVO.getSortField(),
                        Boolean.TRUE.equals(pageReqVO.getSortAsc()) ? SortingField.ORDER_ASC : SortingField.ORDER_DESC)
        );

        // 3. 拼接数据并返回
        SalesBrokerageUserConvert.INSTANCE.copyTo(pageResult.getRecords(), userMap);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }

    private boolean isUserCanBind(SalesBrokerageUserDO user) {
        // 校验分销功能是否启用
        SalesConfigDO salesConfig = salesConfigService.getTradeConfig();
        if (salesConfig == null || !BooleanUtil.isTrue(salesConfig.getBrokerageEnabled())) {
            return false;
        }

        // 校验分销关系绑定模式
        if (SalesBrokerageBindModeEnum.REGISTER.getMode().equals(salesConfig.getBrokerageBindMode())) {
            // 判断是否为新用户：注册时间在 30 秒内的，都算新用户
            if (!isNewRegisterUser(user.getId())) {
                throw exception(BROKERAGE_BIND_MODE_REGISTER); // 只有在注册时可以绑定
            }
        } else if (SalesBrokerageBindModeEnum.ANYTIME.getMode().equals(salesConfig.getBrokerageBindMode())) {
            if (user.getBindUserId() != null) {
                throw exception(BROKERAGE_BIND_OVERRIDE); // 已绑定了推广人
            }
        }
        return true;
    }

    /**
     * 判断是否为新用户
     * <p>
     * 标准：注册时间在 30 秒内的，都算新用户
     * <p>
     * 疑问：为什么通过这样的方式实现？
     * 回答：因为注册在 member 模块，希望它和 trade 模块解耦，所以只能用这种约定的逻辑。
     *
     * @param userId 用户编号
     * @return 是否新用户
     */
    private boolean isNewRegisterUser(Long userId) {
        PartnerRespDTO user = PartnerApi.getUser(userId);
        return user != null && LocalDateTimeUtils.afterNow(user.getCreateTime().plusSeconds(30));
    }

    private void validateCanBindUser(SalesBrokerageUserDO user, Long bindUserId) {
        // 1.1 校验推广人是否存在
        PartnerRespDTO bindUserInfo = PartnerApi.getUser(bindUserId);
        if (bindUserInfo == null) {
            throw exception(BROKERAGE_USER_NOT_EXISTS);
        }
        // 1.2 校验要绑定的用户有无推广资格
        SalesBrokerageUserDO bindUser = getOrCreateBrokerageUser(bindUserId);
        if (bindUser == null || BooleanUtil.isFalse(bindUser.getBrokerageEnabled())) {
            throw exception(BROKERAGE_BIND_USER_NOT_ENABLED);
        }

        // 2. 校验绑定自己
        if (Objects.equals(user.getId(), bindUserId)) {
            throw exception(BROKERAGE_BIND_SELF);
        }

        // 3. 下级不能绑定自己的上级
        for (int i = 0; i <= Short.MAX_VALUE; i++) {
            if (Objects.equals(bindUser.getBindUserId(), user.getId())) {
                throw exception(BROKERAGE_BIND_LOOP);
            }
            bindUser = getBrokerageUser(bindUser.getBindUserId());
            // 找到根节点，结束循环
            if (bindUser == null || bindUser.getBindUserId() == null) {
                break;
            }
        }
    }

    /**
     * 根据绑定用户编号，获得下级用户编号列表
     *
     * @param bindUserId 绑定用户编号
     * @param level      下级用户的层级。
     *                   如果 level 为空，则查询 1+2 两个层级
     * @return 下级用户编号列表
     */
    private List<Long> getChildUserIdsByLevel(Long bindUserId, Integer level) {
        if (bindUserId == null) {
            return Collections.emptyList();
        }
        // 先查第 1 级
        List<Long> bindUserIds = salesBrokerageUserMapper.selectIdListByBindUserIdIn(Collections.singleton(bindUserId));
        if (CollUtil.isEmpty(bindUserIds)) {
            return Collections.emptyList();
        }

        // 情况一：level 为空，查询所有级别
        if (level == null) {
            // 再查第 2 级，并合并结果
            bindUserIds.addAll(salesBrokerageUserMapper.selectIdListByBindUserIdIn(bindUserIds));
            return bindUserIds;
        }
        // 情况二：level 为 1，只查询第 1 级
        if (level == 1) {
            return bindUserIds;
        }
        // 情况三：level 为 1，只查询第 2 级
        if (level == 2) {
            return salesBrokerageUserMapper.selectIdListByBindUserIdIn(bindUserIds);
        }
        throw exception(BROKERAGE_USER_LEVEL_NOT_SUPPORT);
    }

}
