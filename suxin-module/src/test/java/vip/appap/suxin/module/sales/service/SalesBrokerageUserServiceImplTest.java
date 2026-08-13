package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageUserDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesConfigDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesBrokerageUserMapper;
import vip.appap.suxin.module.sales.enums.SalesBrokerageBindModeEnum;
import vip.appap.suxin.module.sales.enums.SalesBrokerageEnabledConditionEnum;
import vip.appap.suxin.module.sales.service.SalesConfigService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;

import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.BROKERAGE_BIND_LOOP;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.BROKERAGE_BIND_MODE_REGISTER;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.BROKERAGE_BIND_OVERRIDE;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.BROKERAGE_BIND_SELF;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.BROKERAGE_BIND_USER_NOT_ENABLED;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.BROKERAGE_USER_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SalesBrokerageUserServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SalesBrokerageUserServiceImpl brokerageUserService;

    @Mock
    private SalesBrokerageUserMapper brokerageUserMapper;

    @Mock
    private SalesConfigService salesConfigService;

    @Mock
    private PartnerApi partnerApi;

    @Test
    void applyBrokerageUser_notExists_createsDisabledRecord() {
        // 准备参数
        when(brokerageUserMapper.selectById(1L)).thenReturn(null);

        // 调用
        brokerageUserService.applyBrokerageUser(1L);

        // 断言
        ArgumentCaptor<SalesBrokerageUserDO> captor = ArgumentCaptor.forClass(SalesBrokerageUserDO.class);
        verify(brokerageUserMapper).insert(captor.capture());
        SalesBrokerageUserDO record = captor.getValue();
        assertEquals(1L, record.getId());
        assertFalse(record.getBrokerageEnabled());
        assertEquals(0, record.getBrokeragePrice());
        assertEquals(0, record.getFrozenPrice());
        assertEquals(null, record.getBrokerageTime());
    }

    @Test
    void applyBrokerageUser_notExists_createsDisabledRecordWhenRepeated() {
        // 准备参数
        when(brokerageUserMapper.selectById(1L)).thenReturn(null);

        // 调用
        brokerageUserService.applyBrokerageUser(1L);

        // 断言
        ArgumentCaptor<SalesBrokerageUserDO> captor = ArgumentCaptor.forClass(SalesBrokerageUserDO.class);
        verify(brokerageUserMapper).insert(captor.capture());
        SalesBrokerageUserDO record = captor.getValue();
        assertEquals(1L, record.getId());
        assertFalse(record.getBrokerageEnabled());
        assertEquals(0, record.getBrokeragePrice());
        assertEquals(0, record.getFrozenPrice());
        assertEquals(null, record.getBrokerageTime());
    }

    @Test
    void applyBrokerageUser_pendingExists_returnsIdempotently() {
        // 准备参数
        SalesBrokerageUserDO existing = new SalesBrokerageUserDO().setId(1L).setBrokerageEnabled(false);
        when(brokerageUserMapper.selectById(1L)).thenReturn(existing);

        // 调用
        brokerageUserService.applyBrokerageUser(1L);

        // 断言
        verify(brokerageUserMapper, never()).insert(any(SalesBrokerageUserDO.class));
    }

    @Test
    void applyBrokerageUser_approvedExists_returnsIdempotently() {
        // 准备参数
        SalesBrokerageUserDO existing = new SalesBrokerageUserDO().setId(1L).setBrokerageEnabled(true);
        when(brokerageUserMapper.selectById(1L)).thenReturn(existing);

        // 调用
        brokerageUserService.applyBrokerageUser(1L);

        // 断言
        verify(brokerageUserMapper, never()).insert(any(SalesBrokerageUserDO.class));
        verify(brokerageUserMapper, never()).updateById(any(SalesBrokerageUserDO.class));
    }

    @Test
    void getUserBrokerageEnabled_globalDisabled_returnsFalse() {
        // 准备参数
        when(salesConfigService.getTradeConfig()).thenReturn(salesConfig(false));

        // 调用并断言
        assertFalse(brokerageUserService.getUserBrokerageEnabled(1L));
        verify(brokerageUserMapper, never()).selectById(any());
    }

    @Test
    void getUserBrokerageEnabled_noRecord_returnsFalse() {
        // 准备参数
        when(salesConfigService.getTradeConfig()).thenReturn(salesConfig(true));
        when(brokerageUserMapper.selectById(1L)).thenReturn(null);

        // 调用并断言
        assertFalse(brokerageUserService.getUserBrokerageEnabled(1L));
    }

    @Test
    void getUserBrokerageEnabled_disabledRecord_returnsFalse() {
        // 准备参数
        when(salesConfigService.getTradeConfig()).thenReturn(salesConfig(true));
        when(brokerageUserMapper.selectById(1L)).thenReturn(new SalesBrokerageUserDO().setId(1L).setBrokerageEnabled(false));

        // 调用并断言
        assertFalse(brokerageUserService.getUserBrokerageEnabled(1L));
    }

    @Test
    void getUserBrokerageEnabled_enabledRecord_returnsTrue() {
        // 准备参数
        when(salesConfigService.getTradeConfig()).thenReturn(salesConfig(true));
        when(brokerageUserMapper.selectById(1L)).thenReturn(new SalesBrokerageUserDO().setId(1L).setBrokerageEnabled(true));

        // 调用并断言
        assertTrue(brokerageUserService.getUserBrokerageEnabled(1L));
    }

    // ========== 绑定规则：分销关闭 / 资格 / 自绑定 / 循环 / 跨租户（任务 3.1） ==========

    @Test
    void bindBrokerageUser_globalDisabled_returnsFalseWithoutChange() {
        // 配置缺失：分销关闭
        when(salesConfigService.getTradeConfig()).thenReturn(null);
        when(brokerageUserMapper.selectById(100L)).thenReturn(null);

        assertFalse(brokerageUserService.bindBrokerageUser(100L, 300L));
        verify(brokerageUserMapper, never()).insert(any(SalesBrokerageUserDO.class));
        verify(brokerageUserMapper, never()).updateById(any(SalesBrokerageUserDO.class));

        // 显式关闭分销
        when(salesConfigService.getTradeConfig()).thenReturn(salesConfig(false));
        assertFalse(brokerageUserService.bindBrokerageUser(100L, 300L));
        verify(brokerageUserMapper, never()).insert(any(SalesBrokerageUserDO.class));
    }

    @Test
    void bindBrokerageUser_inviterNotFound_throwsUserNotExists() {
        // 邀请人不存在（跨租户隔离：租户外用户不可查询）→ 拒绝且不落库
        when(salesConfigService.getTradeConfig()).thenReturn(salesConfig(true, SalesBrokerageBindModeEnum.ANYTIME.getMode(), null));
        when(brokerageUserMapper.selectById(100L)).thenReturn(null);
        when(partnerApi.getUser(300L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> brokerageUserService.bindBrokerageUser(100L, 300L));
        assertEquals(BROKERAGE_USER_NOT_EXISTS.getCode(), ex.getCode());
        verify(brokerageUserMapper, never()).insert(any(SalesBrokerageUserDO.class));
    }

    @Test
    void bindBrokerageUser_inviterNoQualification_throwsNotEnabled() {
        // 邀请人无推广资格：指定分销模式下未开通的推广员
        when(salesConfigService.getTradeConfig()).thenReturn(salesConfig(true, SalesBrokerageBindModeEnum.ANYTIME.getMode(),
                SalesBrokerageEnabledConditionEnum.ADMIN.getCondition()));
        when(brokerageUserMapper.selectById(100L)).thenReturn(null);
        when(partnerApi.getUser(300L)).thenReturn(newPartner(300L, "inviter"));
        when(brokerageUserMapper.selectById(300L)).thenReturn(null); // 指定分销：不自动创建，视为无资格

        ServiceException ex = assertThrows(ServiceException.class,
                () -> brokerageUserService.bindBrokerageUser(100L, 300L));
        assertEquals(BROKERAGE_BIND_USER_NOT_ENABLED.getCode(), ex.getCode());
        verify(brokerageUserMapper, never()).insert(any(SalesBrokerageUserDO.class));
    }

    @Test
    void bindBrokerageUser_selfBind_throwsSelf() {
        // 自绑定：用户已有推广账户并尝试绑定自己
        when(salesConfigService.getTradeConfig()).thenReturn(salesConfig(true, SalesBrokerageBindModeEnum.ANYTIME.getMode(), null));
        SalesBrokerageUserDO self = new SalesBrokerageUserDO().setId(100L).setBrokerageEnabled(true);
        when(brokerageUserMapper.selectById(100L)).thenReturn(self);
        when(partnerApi.getUser(100L)).thenReturn(newPartner(100L, "self"));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> brokerageUserService.bindBrokerageUser(100L, 100L));
        assertEquals(BROKERAGE_BIND_SELF.getCode(), ex.getCode());
        verify(brokerageUserMapper, never()).insert(any(SalesBrokerageUserDO.class));
    }

    @Test
    void bindBrokerageUser_loopBind_throwsLoop() {
        // 循环绑定：推广人 300 的上线是用户 100，100 不能绑 300
        when(salesConfigService.getTradeConfig()).thenReturn(salesConfig(true, SalesBrokerageBindModeEnum.ANYTIME.getMode(), null));
        when(brokerageUserMapper.selectById(100L)).thenReturn(null);
        when(partnerApi.getUser(300L)).thenReturn(newPartner(300L, "inviter"));
        when(brokerageUserMapper.selectById(300L))
                .thenReturn(new SalesBrokerageUserDO().setId(300L).setBrokerageEnabled(true).setBindUserId(100L));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> brokerageUserService.bindBrokerageUser(100L, 300L));
        assertEquals(BROKERAGE_BIND_LOOP.getCode(), ex.getCode());
        verify(brokerageUserMapper, never()).insert(any(SalesBrokerageUserDO.class));
    }

    @Test
    void bindBrokerageUser_multiLevelLoop_throwsLoop() {
        // 多级循环：100 -> 300 -> 400 -> 100
        when(salesConfigService.getTradeConfig()).thenReturn(salesConfig(true, SalesBrokerageBindModeEnum.ANYTIME.getMode(), null));
        when(brokerageUserMapper.selectById(100L)).thenReturn(null);
        when(partnerApi.getUser(300L)).thenReturn(newPartner(300L, "inviter"));
        when(brokerageUserMapper.selectById(300L))
                .thenReturn(new SalesBrokerageUserDO().setId(300L).setBrokerageEnabled(true).setBindUserId(400L));
        when(brokerageUserMapper.selectById(400L))
                .thenReturn(new SalesBrokerageUserDO().setId(400L).setBrokerageEnabled(true).setBindUserId(100L));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> brokerageUserService.bindBrokerageUser(100L, 300L));
        assertEquals(BROKERAGE_BIND_LOOP.getCode(), ex.getCode());
    }

    // ========== 绑定模式配置矩阵（任务 3.2） ==========

    @Test
    void bindBrokerageUser_anytimeMode_firstBind_success() {
        // 首次绑定模式：无关系用户可绑定
        when(salesConfigService.getTradeConfig()).thenReturn(salesConfig(true, SalesBrokerageBindModeEnum.ANYTIME.getMode(), null));
        when(brokerageUserMapper.selectById(100L)).thenReturn(null);
        when(partnerApi.getUser(300L)).thenReturn(newPartner(300L, "inviter"));
        when(brokerageUserMapper.selectById(300L))
                .thenReturn(new SalesBrokerageUserDO().setId(300L).setBrokerageEnabled(true));

        assertTrue(brokerageUserService.bindBrokerageUser(100L, 300L));

        ArgumentCaptor<SalesBrokerageUserDO> captor = ArgumentCaptor.forClass(SalesBrokerageUserDO.class);
        verify(brokerageUserMapper).insert(captor.capture());
        assertEquals(100L, captor.getValue().getId());
        assertEquals(300L, captor.getValue().getBindUserId());
        assertNotNull(captor.getValue().getBindUserTime());
    }

    @Test
    void bindBrokerageUser_anytimeMode_alreadyBound_throwsOverride() {
        // 首次绑定模式：已有关系不允许覆盖
        when(salesConfigService.getTradeConfig()).thenReturn(salesConfig(true, SalesBrokerageBindModeEnum.ANYTIME.getMode(), null));
        when(brokerageUserMapper.selectById(100L))
                .thenReturn(new SalesBrokerageUserDO().setId(100L).setBindUserId(200L));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> brokerageUserService.bindBrokerageUser(100L, 300L));
        assertEquals(BROKERAGE_BIND_OVERRIDE.getCode(), ex.getCode());
        verify(brokerageUserMapper, never()).updateById(any(SalesBrokerageUserDO.class));
    }

    @Test
    void bindBrokerageUser_anytimeMode_repeatedSameInviter_rejectedIdempotently() {
        // 首次绑定模式：重复提交同一邀请人时已有关系，拒绝且不产生第二条关系
        when(salesConfigService.getTradeConfig()).thenReturn(salesConfig(true, SalesBrokerageBindModeEnum.ANYTIME.getMode(), null));
        when(brokerageUserMapper.selectById(100L))
                .thenReturn(new SalesBrokerageUserDO().setId(100L).setBindUserId(300L));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> brokerageUserService.bindBrokerageUser(100L, 300L));
        assertEquals(BROKERAGE_BIND_OVERRIDE.getCode(), ex.getCode());
        verify(brokerageUserMapper, never()).insert(any(SalesBrokerageUserDO.class));
        verify(brokerageUserMapper, never()).updateById(any(SalesBrokerageUserDO.class));
    }

    @Test
    void bindBrokerageUser_registerMode_notNewUser_throwsRegister() {
        // 注册绑定模式：非新用户不得绕过注册窗口
        when(salesConfigService.getTradeConfig()).thenReturn(salesConfig(true, SalesBrokerageBindModeEnum.REGISTER.getMode(), null));
        when(brokerageUserMapper.selectById(100L)).thenReturn(null);
        when(partnerApi.getUser(100L)).thenReturn(newPartnerCreatedBefore(100L, "buyer", 1));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> brokerageUserService.bindBrokerageUser(100L, 300L));
        assertEquals(BROKERAGE_BIND_MODE_REGISTER.getCode(), ex.getCode());
        verify(brokerageUserMapper, never()).insert(any(SalesBrokerageUserDO.class));
    }

    @Test
    void bindBrokerageUser_registerMode_newUser_bindsSuccess() {
        // 注册绑定模式：注册窗口内的新用户允许绑定
        when(salesConfigService.getTradeConfig()).thenReturn(salesConfig(true, SalesBrokerageBindModeEnum.REGISTER.getMode(), null));
        when(brokerageUserMapper.selectById(100L)).thenReturn(null);
        when(partnerApi.getUser(100L)).thenReturn(newPartnerCreatedBefore(100L, "buyer", -1));
        when(partnerApi.getUser(300L)).thenReturn(newPartner(300L, "inviter"));
        when(brokerageUserMapper.selectById(300L))
                .thenReturn(new SalesBrokerageUserDO().setId(300L).setBrokerageEnabled(true));

        assertTrue(brokerageUserService.bindBrokerageUser(100L, 300L));

        ArgumentCaptor<SalesBrokerageUserDO> captor = ArgumentCaptor.forClass(SalesBrokerageUserDO.class);
        verify(brokerageUserMapper).insert(captor.capture());
        assertEquals(300L, captor.getValue().getBindUserId());
    }

    @Test
    void bindBrokerageUser_overrideMode_updatesExistingRelation() {
        // 覆盖绑定模式：允许替换已有关系
        when(salesConfigService.getTradeConfig()).thenReturn(salesConfig(true, SalesBrokerageBindModeEnum.OVERRIDE.getMode(), null));
        when(brokerageUserMapper.selectById(100L))
                .thenReturn(new SalesBrokerageUserDO().setId(100L).setBindUserId(200L));
        when(partnerApi.getUser(300L)).thenReturn(newPartner(300L, "inviter"));
        when(brokerageUserMapper.selectById(300L))
                .thenReturn(new SalesBrokerageUserDO().setId(300L).setBrokerageEnabled(true));

        assertTrue(brokerageUserService.bindBrokerageUser(100L, 300L));

        ArgumentCaptor<SalesBrokerageUserDO> captor = ArgumentCaptor.forClass(SalesBrokerageUserDO.class);
        verify(brokerageUserMapper).updateById(captor.capture());
        assertEquals(100L, captor.getValue().getId());
        assertEquals(300L, captor.getValue().getBindUserId());
    }

    @Test
    void bindBrokerageUser_overrideMode_sameInviter_idempotent() {
        // 覆盖绑定模式：重复提交同一邀请人，仅更新为相同值，不产生第二条关系或佣金
        when(salesConfigService.getTradeConfig()).thenReturn(salesConfig(true, SalesBrokerageBindModeEnum.OVERRIDE.getMode(), null));
        when(brokerageUserMapper.selectById(100L))
                .thenReturn(new SalesBrokerageUserDO().setId(100L).setBindUserId(300L));
        when(partnerApi.getUser(300L)).thenReturn(newPartner(300L, "inviter"));
        when(brokerageUserMapper.selectById(300L))
                .thenReturn(new SalesBrokerageUserDO().setId(300L).setBrokerageEnabled(true));

        assertTrue(brokerageUserService.bindBrokerageUser(100L, 300L));

        ArgumentCaptor<SalesBrokerageUserDO> captor = ArgumentCaptor.forClass(SalesBrokerageUserDO.class);
        verify(brokerageUserMapper).updateById(captor.capture());
        assertEquals(300L, captor.getValue().getBindUserId());
        verify(brokerageUserMapper, never()).insert(any(SalesBrokerageUserDO.class));
    }

    @Test
    void bindBrokerageUser_allCondition_newUserAutoEnablesBrokerage() {
        // 人人分销：新用户绑定后自动获得推广资格
        when(salesConfigService.getTradeConfig()).thenReturn(salesConfig(true, SalesBrokerageBindModeEnum.ANYTIME.getMode(),
                SalesBrokerageEnabledConditionEnum.ALL.getCondition()));
        when(brokerageUserMapper.selectById(100L)).thenReturn(null);
        when(partnerApi.getUser(300L)).thenReturn(newPartner(300L, "inviter"));
        when(brokerageUserMapper.selectById(300L))
                .thenReturn(new SalesBrokerageUserDO().setId(300L).setBrokerageEnabled(true));

        assertTrue(brokerageUserService.bindBrokerageUser(100L, 300L));

        ArgumentCaptor<SalesBrokerageUserDO> captor = ArgumentCaptor.forClass(SalesBrokerageUserDO.class);
        verify(brokerageUserMapper).insert(captor.capture());
        assertTrue(captor.getValue().getBrokerageEnabled());
        assertNotNull(captor.getValue().getBrokerageTime());
    }

    @Test
    void bindBrokerageUser_adminCondition_newUserKeepsDisabled() {
        // 指定分销：新用户绑定后不自动获得推广资格
        when(salesConfigService.getTradeConfig()).thenReturn(salesConfig(true, SalesBrokerageBindModeEnum.ANYTIME.getMode(),
                SalesBrokerageEnabledConditionEnum.ADMIN.getCondition()));
        when(brokerageUserMapper.selectById(100L)).thenReturn(null);
        when(partnerApi.getUser(300L)).thenReturn(newPartner(300L, "inviter"));
        when(brokerageUserMapper.selectById(300L))
                .thenReturn(new SalesBrokerageUserDO().setId(300L).setBrokerageEnabled(true));

        assertTrue(brokerageUserService.bindBrokerageUser(100L, 300L));

        ArgumentCaptor<SalesBrokerageUserDO> captor = ArgumentCaptor.forClass(SalesBrokerageUserDO.class);
        verify(brokerageUserMapper).insert(captor.capture());
        assertFalse(captor.getValue().getBrokerageEnabled());
    }

    private SalesConfigDO salesConfig(boolean brokerageEnabled) {
        SalesConfigDO config = new SalesConfigDO();
        config.setBrokerageEnabled(brokerageEnabled);
        return config;
    }

    private SalesConfigDO salesConfig(boolean brokerageEnabled, Integer bindMode, Integer enabledCondition) {
        SalesConfigDO config = salesConfig(brokerageEnabled);
        config.setBrokerageBindMode(bindMode);
        config.setBrokerageEnabledCondition(enabledCondition);
        return config;
    }

    private PartnerRespDTO newPartner(Long id, String nickname) {
        PartnerRespDTO user = new PartnerRespDTO();
        user.setId(id);
        user.setNickname(nickname);
        user.setCreateTime(LocalDateTime.now());
        return user;
    }

    /**
     * 构造注册时间相对当前时间偏移的伙伴
     *
     * @param id       用户编号
     * @param nickname 昵称
     * @param hoursAgo 注册时间距当前的小时数（负数表示未来，用于模拟新注册窗口内）
     */
    private PartnerRespDTO newPartnerCreatedBefore(Long id, String nickname, int hoursAgo) {
        PartnerRespDTO user = newPartner(id, nickname);
        user.setCreateTime(LocalDateTime.now().minusHours(hoursAgo));
        return user;
    }

}
