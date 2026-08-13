package vip.appap.suxin.module.sales.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesDiyPagePropertyRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiyPageDO;
import vip.appap.suxin.module.sales.service.SalesDiyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 装修页面")
@RestController
@RequestMapping("/sales/promotion/diy-page")
@Validated
public class AppSalesDiyPageController {

    @Resource
    private SalesDiyPageService diyPageService;

    @GetMapping("/get")
    @Operation(summary = "获得装修页面")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PermitAll
    public CommonResult<AppSalesDiyPagePropertyRespVO> getDiyPage(@RequestParam("id") Long id) {
        SalesDiyPageDO diyPage = diyPageService.getDiyPage(id);
        return success(BeanUtils.toBean(diyPage, AppSalesDiyPagePropertyRespVO.class));
    }

}
