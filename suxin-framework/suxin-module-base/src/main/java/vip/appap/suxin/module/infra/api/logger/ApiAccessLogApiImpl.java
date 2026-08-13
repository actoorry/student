package vip.appap.suxin.module.infra.api.logger;


import vip.appap.suxin.framework.common.biz.infra.logger.ApiAccessLogCommonApi;
import vip.appap.suxin.framework.common.biz.infra.logger.dto.ApiAccessLogCreateReqDTO;
import vip.appap.suxin.module.infra.service.logger.ApiAccessLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * API 访问日志的 API 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class ApiAccessLogApiImpl implements ApiAccessLogCommonApi {

    @Resource
    private ApiAccessLogService apiAccessLogService;

    @Override
    public void createApiAccessLog(ApiAccessLogCreateReqDTO createDTO) {
        apiAccessLogService.createApiAccessLog(createDTO);
    }

}
