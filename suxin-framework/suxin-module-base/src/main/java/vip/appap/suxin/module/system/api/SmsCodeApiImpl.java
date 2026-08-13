package vip.appap.suxin.module.system.api;

import vip.appap.suxin.module.system.api.dto.SmsCodeValidateReqDTO;
import vip.appap.suxin.module.system.api.dto.SmsCodeSendReqDTO;
import vip.appap.suxin.module.system.api.dto.SmsCodeUseReqDTO;
import vip.appap.suxin.module.system.service.SmsCodeService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * 短信验证码 API 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class SmsCodeApiImpl implements SmsCodeApi {

    @Resource
    private SmsCodeService smsCodeService;

    @Override
    public void sendSmsCode(SmsCodeSendReqDTO reqDTO) {
        smsCodeService.sendSmsCode(reqDTO);
    }

    @Override
    public void useSmsCode(SmsCodeUseReqDTO reqDTO) {
        smsCodeService.useSmsCode(reqDTO);
    }

    @Override
    public void validateSmsCode(SmsCodeValidateReqDTO reqDTO) {
        smsCodeService.validateSmsCode(reqDTO);
    }

}
