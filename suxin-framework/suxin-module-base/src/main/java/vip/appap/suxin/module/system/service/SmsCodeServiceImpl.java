package vip.appap.suxin.module.system.service;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import vip.appap.suxin.module.system.api.dto.SmsCodeSendReqDTO;
import vip.appap.suxin.module.system.api.dto.SmsCodeUseReqDTO;
import vip.appap.suxin.module.system.api.dto.SmsCodeValidateReqDTO;
import vip.appap.suxin.module.system.dal.dataobject.SmsCodeDO;
import vip.appap.suxin.module.system.dal.mysql.SmsCodeMapper;
import vip.appap.suxin.module.system.enums.SmsSceneEnum;
import vip.appap.suxin.module.system.framework.sms.config.SmsCodeProperties;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.randomInt;
import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.date.DateUtils.isToday;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.*;

/**
 * 短信验证码 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class SmsCodeServiceImpl implements SmsCodeService {

    @Resource
    private SmsCodeProperties smsCodeProperties;

    @Resource
    private SmsCodeMapper smsCodeMapper;

    @Resource
    private SmsSendService smsSendService;

    @Override
    public void sendSmsCode(SmsCodeSendReqDTO reqDTO) {
        SmsSceneEnum sceneEnum = SmsSceneEnum.getCodeByScene(reqDTO.getScene());
        Assert.notNull(sceneEnum, "验证码场景({}) 查找不到配置", reqDTO.getScene());
        // 创建验证码
        String code = createSmsCode(reqDTO.getMobile(), reqDTO.getScene(), reqDTO.getCreateIp());
        // 发送验证码
        smsSendService.sendSingleSms(reqDTO.getMobile(), null, null,
                sceneEnum.getTemplateCode(), MapUtil.of("code", code));
    }

    private String createSmsCode(String mobile, Integer scene, String ip) {
        // 校验是否可以发送验证码，不用筛选场景
        SmsCodeDO lastSmsCode = smsCodeMapper.selectLastByMobile(mobile, null, null);
        if (lastSmsCode != null) {
            if (LocalDateTimeUtil.between(lastSmsCode.getCreateTime(), LocalDateTime.now()).toMillis()
                    < smsCodeProperties.getSendFrequency().toMillis()) { // 发送过于频繁
                throw exception(SMS_CODE_SEND_TOO_FAST);
            }
            if (isToday(lastSmsCode.getCreateTime()) && // 必须是今天，才能计算超过当天的上限
                    lastSmsCode.getTodayIndex() >= smsCodeProperties.getSendMaximumQuantityPerDay()) { // 超过当天发送的上限。
                throw exception(SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_DAY);
            }
            // TODO 芋艿：提升，每个 IP 每天可发送数量
            // TODO 芋艿：提升，每个 IP 每小时可发送数量
        }

        // 创建验证码记录
        String code = String.format("%0" + smsCodeProperties.getEndCode().toString().length() + "d",
                randomInt(smsCodeProperties.getBeginCode(), smsCodeProperties.getEndCode() + 1));
        SmsCodeDO newSmsCode = SmsCodeDO.builder().mobile(mobile).code(code).scene(scene)
                .todayIndex(lastSmsCode != null && isToday(lastSmsCode.getCreateTime()) ? lastSmsCode.getTodayIndex() + 1 : 1)
                .createIp(ip).used(false).build();
        smsCodeMapper.insert(newSmsCode);
        return code;
    }

    @Override
    public void useSmsCode(SmsCodeUseReqDTO reqDTO) {
        // 检测验证码是否有效
        SmsCodeDO lastSmsCode = validateSmsCode0(reqDTO.getMobile(), reqDTO.getCode(), reqDTO.getScene());
        // 使用验证码：带“未使用”条件的更新，保证并发下验证码只能被消费一次
        int updateRows = smsCodeMapper.updateUsed(lastSmsCode.getId(), LocalDateTime.now(), reqDTO.getUsedIp());
        if (updateRows == 0) { // 并发场景下，验证码已被其他请求消费
            throw exception(SMS_CODE_USED);
        }
    }

    @Override
    public void validateSmsCode(SmsCodeValidateReqDTO reqDTO) {
        validateSmsCode0(reqDTO.getMobile(), reqDTO.getCode(), reqDTO.getScene());
    }

    private SmsCodeDO validateSmsCode0(String mobile, String code, Integer scene) {
        // 校验验证码：不存在任何测试旁路，所有环境都必须走真实记录校验
        SmsCodeDO lastSmsCode = smsCodeMapper.selectLastByMobile(mobile, code, scene);
        // 若验证码不存在，抛出异常
        if (lastSmsCode == null) {
            throw exception(SMS_CODE_NOT_FOUND);
        }
        // 超过时间
        if (LocalDateTimeUtil.between(lastSmsCode.getCreateTime(), LocalDateTime.now()).toMillis()
                >= smsCodeProperties.getExpireTimes().toMillis()) { // 验证码已过期
            throw exception(SMS_CODE_EXPIRED);
        }
        // 判断验证码是否已被使用
        if (Boolean.TRUE.equals(lastSmsCode.getUsed())) {
            throw exception(SMS_CODE_USED);
        }
        return lastSmsCode;
    }

}
