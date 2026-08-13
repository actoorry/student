package vip.appap.suxin.module.sales.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesDiyTemplatePropertyRespVO;
import vip.appap.suxin.module.sales.convert.SalesDiyTemplateConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiyPageDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiyTemplateDO;
import vip.appap.suxin.module.sales.enums.SalesDiyPageEnum;
import vip.appap.suxin.module.sales.service.SalesDiyPageService;
import vip.appap.suxin.module.sales.service.SalesDiyTemplateService;
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

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.findFirst;

@Tag(name = "用户 APP - 装修模板")
@RestController
@RequestMapping("/sales/promotion/diy-template")
@Validated
public class AppSalesDiyTemplateController {

    @Resource
    private SalesDiyTemplateService diyTemplateService;
    @Resource
    private SalesDiyPageService diyPageService;

    // TODO @疯狂：要不要把 used 和 get 接口合并哈；不传递 id，直接拿默认；
    @GetMapping("/used")
    @Operation(summary = "使用中的装修模板")
    @PermitAll
    public CommonResult<AppSalesDiyTemplatePropertyRespVO> getUsedDiyTemplate() {
        SalesDiyTemplateDO diyTemplate = diyTemplateService.getUsedDiyTemplate();
        return success(buildVo(diyTemplate));
    }

    @GetMapping("/get")
    @Operation(summary = "获得装修模板")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PermitAll
    public CommonResult<AppSalesDiyTemplatePropertyRespVO> getDiyTemplate(@RequestParam("id") Long id) {
        SalesDiyTemplateDO diyTemplate = diyTemplateService.getDiyTemplate(id);
        return success(buildVo(diyTemplate));
    }

    private AppSalesDiyTemplatePropertyRespVO buildVo(SalesDiyTemplateDO diyTemplate) {
        if (diyTemplate == null) {
            return null;
        }
        // 查询模板下的页面
        List<SalesDiyPageDO> pages = diyPageService.getDiyPageByTemplateId(diyTemplate.getId());
        String home = findFirst(pages, page -> SalesDiyPageEnum.INDEX.getName().equals(page.getName()), SalesDiyPageDO::getProperty);
        String user = findFirst(pages, page -> SalesDiyPageEnum.MY.getName().equals(page.getName()), SalesDiyPageDO::getProperty);
        // 拼接返回
        return SalesDiyTemplateConvert.INSTANCE.convertPropertyVo2(diyTemplate, home, user);
    }

}
