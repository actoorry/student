package vip.appap.suxin.module.infra.api.logger;


import vip.appap.suxin.framework.common.biz.infra.logger.ApiErrorLogCommonApi;
import vip.appap.suxin.framework.common.biz.infra.logger.dto.ApiErrorLogCreateReqDTO;
import vip.appap.suxin.module.infra.service.logger.ApiErrorLogService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * API 访问日志的 API 接口
 *
 * @author 书心软件
 */
@Service
@Validated
public class ApiErrorLogApiImpl implements ApiErrorLogCommonApi {

    @Resource
    private ApiErrorLogService apiErrorLogService;

    @Override
    public void createApiErrorLog(ApiErrorLogCreateReqDTO createDTO) {
        apiErrorLogService.createApiErrorLog(createDTO);
    }

}
