package vip.appap.suxin.module.partner.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSignInRecordPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSignInRecordRespVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSignInRecordDO;
import vip.appap.suxin.module.partner.service.PartnerSignInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 会员签到记录")
@RestController
@RequestMapping("/partner/sign-in-record")
@Validated
public class PartnerSignInRecordController {

    @Resource
    private PartnerSignInService signInService;

    @GetMapping("/page")
    @Operation(summary = "获得会员签到记录分页")
    @PreAuthorize("@ss.hasPermission('partner:sign-in-record:query')")
    public CommonResult<PageResult<PartnerSignInRecordRespVO>> getSignInRecordPage(@Valid PartnerSignInRecordPageReqVO pageReqVO) {
        PageResult<PartnerSignInRecordDO> pageResult = signInService.getSignInRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, PartnerSignInRecordRespVO.class));
    }

}
