package vip.appap.suxin.module.marriage.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerCertificationRecordPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerCertificationRecordRespVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerCertificationRecordWithPartnerRespVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerCertificationRecordDO;
import vip.appap.suxin.module.partner.service.PartnerCertificationRecordService;


import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 合作伙伴认证记录")
@RestController
@RequestMapping("/partner/certification-record")
@Validated
public class PartnerCertificationRecordController {

    @Resource
    private PartnerCertificationRecordService partnerCertificationRecordService;

    @GetMapping("/get")
    @Operation(summary = "获得合作伙伴认证记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('partner:certification-record:query')")
    public CommonResult<PartnerCertificationRecordRespVO> getPartnerCertificationRecord(@RequestParam("id") Long id) {
        PartnerCertificationRecordDO record = partnerCertificationRecordService.getPartnerCertificationRecord(id);
        return success(BeanUtils.toBean(record, PartnerCertificationRecordRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得合作伙伴认证记录分页")
    @PreAuthorize("@ss.hasPermission('partner:certification-record:query')")
    public CommonResult<PageResult<PartnerCertificationRecordWithPartnerRespVO>> getPartnerCertificationRecordPage(
            @Valid PartnerCertificationRecordPageReqVO pageVO) {
        return success(partnerCertificationRecordService.getPartnerCertificationRecordWithPartnerPage(pageVO));
    }

}
