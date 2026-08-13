package vip.appap.suxin.module.rongjh.controller.app;

import vip.appap.suxin.module.rongjh.controller.app.vo.AppLoveValueRespVO;
import vip.appap.suxin.module.rongjh.service.LoveValueService;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 戎集汇配置")
@RestController
@RequestMapping("/rongjh/config")
public class RongjhConfigController {

    @Resource
    private LoveValueService loveValueService;

    @GetMapping("/love-value")
    @Operation(summary = "平台爱心贡献值")
    @PermitAll
    public CommonResult<AppLoveValueRespVO> getLoveValue() {
        AppLoveValueRespVO resp = new AppLoveValueRespVO();
        resp.setTotalLoveValues(loveValueService.getPlatformLoveValue());
        return success(resp);
    }

}
