package vip.appap.suxin.module.system.api;

import vip.appap.suxin.module.system.api.dto.SmsSendSingleToUserReqDTO;
import vip.appap.suxin.module.system.service.SmsSendService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * 短信发送 API 接口
 *
 * @author 书心软件
 */
@Service
@Validated
public class SmsSendApiImpl implements SmsSendApi {

    @Resource
    private SmsSendService smsSendService;

    @Override
    public Long sendSingleSmsToAdmin(SmsSendSingleToUserReqDTO reqDTO) {
        return smsSendService.sendSingleSmsToAdmin(reqDTO.getMobile(), reqDTO.getUserId(),
                reqDTO.getTemplateCode(), reqDTO.getTemplateParams());
    }

    @Override
    public Long sendSingleSmsToMember(SmsSendSingleToUserReqDTO reqDTO) {
        return smsSendService.sendSingleSmsToMember(reqDTO.getMobile(), reqDTO.getUserId(),
                reqDTO.getTemplateCode(), reqDTO.getTemplateParams());
    }

}
