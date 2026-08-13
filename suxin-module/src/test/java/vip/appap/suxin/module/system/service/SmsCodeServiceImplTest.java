package vip.appap.suxin.module.system.service;

import cn.hutool.core.map.MapUtil;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.system.api.dto.SmsCodeSendReqDTO;
import vip.appap.suxin.module.system.api.dto.SmsCodeUseReqDTO;
import vip.appap.suxin.module.system.api.dto.SmsCodeValidateReqDTO;
import vip.appap.suxin.module.system.dal.dataobject.SmsCodeDO;
import vip.appap.suxin.module.system.dal.mysql.SmsCodeMapper;
import vip.appap.suxin.module.system.enums.SmsSceneEnum;
import vip.appap.suxin.module.system.framework.sms.config.SmsCodeProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.Duration;
import java.time.LocalDateTime;

import static vip.appap.suxin.framework.test.core.util.AssertUtils.assertServiceException;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SmsCodeServiceImpl} 的单元测试
 *
 * 覆盖原版验证码生命周期：发送成功、发送过快、每日上限、验证码不存在、过期、已使用和正确消费；
 * 并增加迁移断点测试：任意验证码不能通过、场景不可交叉使用、消费必须更新真实记录且不存在空主键更新。
 */
class SmsCodeServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SmsCodeServiceImpl smsCodeService;

    @Mock
    private SmsCodeProperties smsCodeProperties;
    @Mock
    private SmsCodeMapper smsCodeMapper;
    @Mock
    private SmsSendService smsSendService;

    private static final String MOBILE = "15601691300";

    private static SmsCodeDO code(Long id, String code, Integer scene, boolean used, LocalDateTime createTime) {
        SmsCodeDO smsCode = SmsCodeDO.builder().id(id).mobile(MOBILE).code(code).scene(scene).used(used).build();
        smsCode.setCreateTime(createTime);
        return smsCode;
    }

    @BeforeEach
    void setUp() {
        lenient().when(smsCodeProperties.getExpireTimes()).thenReturn(Duration.ofMinutes(5));
        lenient().when(smsCodeProperties.getSendFrequency()).thenReturn(Duration.ofMinutes(1));
        lenient().when(smsCodeProperties.getSendMaximumQuantityPerDay()).thenReturn(10);
        lenient().when(smsCodeProperties.getBeginCode()).thenReturn(100000);
        lenient().when(smsCodeProperties.getEndCode()).thenReturn(999999);
    }

    @Test
    void sendSmsCode_success() {
        SmsCodeSendReqDTO reqDTO = new SmsCodeSendReqDTO()
                .setMobile(MOBILE).setScene(SmsSceneEnum.MEMBER_LOGIN.getScene()).setCreateIp("127.0.0.1");
        when(smsCodeMapper.selectLastByMobile(MOBILE, null, null)).thenReturn(null);
        when(smsCodeMapper.insert(any(SmsCodeDO.class))).thenAnswer(invocation -> {
            SmsCodeDO code = invocation.getArgument(0);
            code.setId(100L);
            return 1;
        });

        smsCodeService.sendSmsCode(reqDTO);

        // 验证码必须为六位随机数，不允许固定通用码
        verify(smsCodeMapper).insert(any(SmsCodeDO.class));
        verify(smsSendService).sendSingleSms(eq(MOBILE), eq(null), eq(null),
                eq(SmsSceneEnum.MEMBER_LOGIN.getTemplateCode()), anyMap());
    }

    @Test
    void sendSmsCode_codeIsSixDigitsRandom() {
        SmsCodeSendReqDTO reqDTO = new SmsCodeSendReqDTO()
                .setMobile(MOBILE).setScene(SmsSceneEnum.MEMBER_LOGIN.getScene()).setCreateIp("127.0.0.1");
        when(smsCodeMapper.selectLastByMobile(MOBILE, null, null)).thenReturn(null);

        SmsCodeDO[] captured = new SmsCodeDO[1];
        when(smsCodeMapper.insert(any(SmsCodeDO.class))).thenAnswer(invocation -> {
            captured[0] = invocation.getArgument(0);
            return 1;
        });

        smsCodeService.sendSmsCode(reqDTO);

        assertEquals(6, captured[0].getCode().length());
        assertNotEquals("9999", captured[0].getCode());
        assertNotEquals("999999", captured[0].getCode());
        int codeValue = Integer.parseInt(captured[0].getCode());
        assertTrue(codeValue >= 100000 && codeValue <= 999999);
    }

    @Test
    void sendSmsCode_tooFast() {
        when(smsCodeMapper.selectLastByMobile(MOBILE, null, null))
                .thenReturn(code(100L, "123456", SmsSceneEnum.MEMBER_LOGIN.getScene(), false, LocalDateTime.now()));
        SmsCodeSendReqDTO reqDTO = new SmsCodeSendReqDTO()
                .setMobile(MOBILE).setScene(SmsSceneEnum.MEMBER_LOGIN.getScene()).setCreateIp("127.0.0.1");

        assertServiceException(() -> smsCodeService.sendSmsCode(reqDTO), SMS_CODE_SEND_TOO_FAST);
        verify(smsCodeMapper, never()).insert(any(SmsCodeDO.class));
    }

    @Test
    void sendSmsCode_exceedDay() {
        SmsCodeDO lastCode = code(100L, "123456", SmsSceneEnum.MEMBER_LOGIN.getScene(), false, LocalDateTime.now());
        lastCode.setTodayIndex(10);
        when(smsCodeMapper.selectLastByMobile(MOBILE, null, null)).thenReturn(lastCode);
        when(smsCodeProperties.getSendFrequency()).thenReturn(Duration.ofMillis(0));
        SmsCodeSendReqDTO reqDTO = new SmsCodeSendReqDTO()
                .setMobile(MOBILE).setScene(SmsSceneEnum.MEMBER_LOGIN.getScene()).setCreateIp("127.0.0.1");

        assertServiceException(() -> smsCodeService.sendSmsCode(reqDTO),
                SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_DAY);
        verify(smsCodeMapper, never()).insert(any(SmsCodeDO.class));
    }

    @Test
    void useSmsCode_success() {
        SmsCodeDO lastCode = code(100L, "123456", SmsSceneEnum.MEMBER_LOGIN.getScene(), false, LocalDateTime.now());
        when(smsCodeMapper.selectLastByMobile(MOBILE, "123456", SmsSceneEnum.MEMBER_LOGIN.getScene()))
                .thenReturn(lastCode);
        when(smsCodeMapper.updateUsed(eq(100L), any(LocalDateTime.class), eq("127.0.0.1"))).thenReturn(1);

        smsCodeService.useSmsCode(new SmsCodeUseReqDTO().setMobile(MOBILE).setCode("123456")
                .setScene(SmsSceneEnum.MEMBER_LOGIN.getScene()).setUsedIp("127.0.0.1"));

        // 必须使用真实记录的 ID 更新，不存在空主键更新
        verify(smsCodeMapper).updateUsed(eq(100L), any(LocalDateTime.class), eq("127.0.0.1"));
    }

    @Test
    void useSmsCode_arbitraryCodeRejected() {
        // 数据库中只有 123456，提交任意验证码 000000：验证码不存在，必须拒绝
        when(smsCodeMapper.selectLastByMobile(MOBILE, "000000", SmsSceneEnum.MEMBER_LOGIN.getScene()))
                .thenReturn(null);

        assertServiceException(() -> smsCodeService.useSmsCode(new SmsCodeUseReqDTO().setMobile(MOBILE)
                .setCode("000000").setScene(SmsSceneEnum.MEMBER_LOGIN.getScene()).setUsedIp("127.0.0.1")),
                SMS_CODE_NOT_FOUND);
        verify(smsCodeMapper, never()).updateUsed(any(Long.class), any(LocalDateTime.class), any(String.class));
    }

    @Test
    void useSmsCode_sceneIsolation() {
        // 验证码是场景 1 生成的，提交到场景 2：不得通过（场景不可交叉使用）
        when(smsCodeMapper.selectLastByMobile(MOBILE, "123456", SmsSceneEnum.MEMBER_UPDATE_MOBILE.getScene()))
                .thenReturn(null);

        assertServiceException(() -> smsCodeService.useSmsCode(new SmsCodeUseReqDTO().setMobile(MOBILE)
                .setCode("123456").setScene(SmsSceneEnum.MEMBER_UPDATE_MOBILE.getScene()).setUsedIp("127.0.0.1")),
                SMS_CODE_NOT_FOUND);
        verify(smsCodeMapper, never()).updateUsed(any(Long.class), any(LocalDateTime.class), any(String.class));
    }

    @Test
    void useSmsCode_expired() {
        SmsCodeDO lastCode = code(100L, "123456", SmsSceneEnum.MEMBER_LOGIN.getScene(), false, LocalDateTime.now().minusMinutes(6));
        when(smsCodeMapper.selectLastByMobile(MOBILE, "123456", SmsSceneEnum.MEMBER_LOGIN.getScene()))
                .thenReturn(lastCode);

        assertServiceException(() -> smsCodeService.useSmsCode(new SmsCodeUseReqDTO().setMobile(MOBILE)
                .setCode("123456").setScene(SmsSceneEnum.MEMBER_LOGIN.getScene()).setUsedIp("127.0.0.1")),
                SMS_CODE_EXPIRED);
        verify(smsCodeMapper, never()).updateUsed(any(Long.class), any(LocalDateTime.class), any(String.class));
    }

    @Test
    void useSmsCode_used() {
        SmsCodeDO lastCode = code(100L, "123456", SmsSceneEnum.MEMBER_LOGIN.getScene(), true, LocalDateTime.now());
        when(smsCodeMapper.selectLastByMobile(MOBILE, "123456", SmsSceneEnum.MEMBER_LOGIN.getScene()))
                .thenReturn(lastCode);

        assertServiceException(() -> smsCodeService.useSmsCode(new SmsCodeUseReqDTO().setMobile(MOBILE)
                .setCode("123456").setScene(SmsSceneEnum.MEMBER_LOGIN.getScene()).setUsedIp("127.0.0.1")),
                SMS_CODE_USED);
        verify(smsCodeMapper, never()).updateUsed(any(Long.class), any(LocalDateTime.class), any(String.class));
    }

    @Test
    void useSmsCode_concurrentRepeatConsumptionRejected() {
        // 并发：校验时记录未使用，但更新时影响行数为 0（已被其他请求消费），必须拒绝
        SmsCodeDO lastCode = code(100L, "123456", SmsSceneEnum.MEMBER_LOGIN.getScene(), false, LocalDateTime.now());
        when(smsCodeMapper.selectLastByMobile(MOBILE, "123456", SmsSceneEnum.MEMBER_LOGIN.getScene()))
                .thenReturn(lastCode);
        when(smsCodeMapper.updateUsed(eq(100L), any(LocalDateTime.class), eq("127.0.0.1"))).thenReturn(0);

        assertServiceException(() -> smsCodeService.useSmsCode(new SmsCodeUseReqDTO().setMobile(MOBILE)
                .setCode("123456").setScene(SmsSceneEnum.MEMBER_LOGIN.getScene()).setUsedIp("127.0.0.1")),
                SMS_CODE_USED);
    }

    @Test
    void validateSmsCode_success() {
        when(smsCodeMapper.selectLastByMobile(MOBILE, "123456", SmsSceneEnum.MEMBER_LOGIN.getScene()))
                .thenReturn(code(100L, "123456", SmsSceneEnum.MEMBER_LOGIN.getScene(), false, LocalDateTime.now()));

        assertDoesNotThrow(() -> smsCodeService.validateSmsCode(new SmsCodeValidateReqDTO()
                .setMobile(MOBILE).setCode("123456").setScene(SmsSceneEnum.MEMBER_LOGIN.getScene())));
    }

    @Test
    void validateSmsCode_notFound() {
        when(smsCodeMapper.selectLastByMobile(MOBILE, "123456", SmsSceneEnum.MEMBER_LOGIN.getScene()))
                .thenReturn(null);

        assertServiceException(() -> smsCodeService.validateSmsCode(new SmsCodeValidateReqDTO()
                .setMobile(MOBILE).setCode("123456").setScene(SmsSceneEnum.MEMBER_LOGIN.getScene())),
                SMS_CODE_NOT_FOUND);
    }

    @Test
    void validateSmsCode_expired() {
        when(smsCodeMapper.selectLastByMobile(MOBILE, "123456", SmsSceneEnum.MEMBER_LOGIN.getScene()))
                .thenReturn(code(100L, "123456", SmsSceneEnum.MEMBER_LOGIN.getScene(), false, LocalDateTime.now().minusMinutes(6)));

        assertServiceException(() -> smsCodeService.validateSmsCode(new SmsCodeValidateReqDTO()
                .setMobile(MOBILE).setCode("123456").setScene(SmsSceneEnum.MEMBER_LOGIN.getScene())),
                SMS_CODE_EXPIRED);
    }

    @Test
    void validateSmsCode_used() {
        when(smsCodeMapper.selectLastByMobile(MOBILE, "123456", SmsSceneEnum.MEMBER_LOGIN.getScene()))
                .thenReturn(code(100L, "123456", SmsSceneEnum.MEMBER_LOGIN.getScene(), true, LocalDateTime.now()));

        assertServiceException(() -> smsCodeService.validateSmsCode(new SmsCodeValidateReqDTO()
                .setMobile(MOBILE).setCode("123456").setScene(SmsSceneEnum.MEMBER_LOGIN.getScene())),
                SMS_CODE_USED);
    }

}

