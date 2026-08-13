package vip.appap.suxin.module.partner.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;

import vip.appap.suxin.module.partner.controller.admin.vo.PartnerCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerRespVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerUpdateReqVO;
import vip.appap.suxin.module.partner.convert.PartnerConvert;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.service.PartnerService;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 合作伙伴")
@RestController
@RequestMapping("/partner/partner")
@Validated
public class PartnerController {

    @Resource
    private PartnerService partnerService;

    @PostMapping("/create")
    @Operation(summary = "创建合作伙伴")
    @PreAuthorize("@ss.hasPermission('partner:partner:create')")
    public CommonResult<Long> createPartner(@Valid @RequestBody PartnerCreateReqVO createReqVO) {
        return success(partnerService.createPartner(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新合作伙伴")
    @PreAuthorize("@ss.hasPermission('partner:partner:update')")
    public CommonResult<Boolean> updatePartner(@Valid @RequestBody PartnerUpdateReqVO updateReqVO) {
        partnerService.updatePartner(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除合作伙伴")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('partner:partner:delete')")
    public CommonResult<Boolean> deletePartner(@RequestParam("id") Long id) {
        partnerService.deletePartner(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得合作伙伴")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('partner:partner:query')")
    public CommonResult<PartnerRespVO> getPartner(@RequestParam("id") Long id) {
        PartnerDO user = partnerService.getPartner(id);
        if (user == null) {
            return success(null);
        }
        return success(PartnerConvert.INSTANCE.convertRespVO(user));
    }

    @GetMapping("/page")
    @Operation(summary = "获得合作伙伴分页")
    @PreAuthorize("@ss.hasPermission('partner:partner:query')")
    public CommonResult<PageResult<PartnerRespVO>> getPartnerPage(@Valid PartnerPageReqVO pageVO) {
        PageResult<PartnerDO> pageResult = partnerService.getPartnerPage(pageVO);
        return success(PartnerConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/list-without-user")
    @Operation(summary = "获取未绑定系统用户的合作伙伴列表")
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<List<PartnerRespVO>> getPartnerListWithoutUser() {
        List<PartnerDO> partners = partnerService.getPartnerListWithoutUser();
        return success(PartnerConvert.INSTANCE.convertList(partners));
    }

}
