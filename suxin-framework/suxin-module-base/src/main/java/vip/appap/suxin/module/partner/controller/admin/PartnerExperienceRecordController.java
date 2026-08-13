package vip.appap.suxin.module.partner.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerExperienceRecordPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerExperienceRecordRespVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerExperienceRecordDO;
import vip.appap.suxin.module.partner.service.PartnerExperienceRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 会员经验记录")
@RestController
@RequestMapping("/partner/experience-record")
@Validated
public class PartnerExperienceRecordController {

    @Resource
    private PartnerExperienceRecordService experienceRecordService;

    @GetMapping("/page")
    @Operation(summary = "获得会员经验记录分页")
    @PreAuthorize("@ss.hasPermission('partner:experience-record:query')")
    public CommonResult<PageResult<PartnerExperienceRecordRespVO>> getExperienceRecordPage(@Valid PartnerExperienceRecordPageReqVO pageVO) {
        PageResult<PartnerExperienceRecordDO> pageResult = experienceRecordService.getExperienceRecordPage(pageVO);
        // 简单转换，后续可以使用 MapStruct
        PageResult<PartnerExperienceRecordRespVO> result = new PageResult<>(pageResult.getList().stream().map(record -> {
            PartnerExperienceRecordRespVO respVO = new PartnerExperienceRecordRespVO();
            respVO.setId(record.getId());
            respVO.setUserId(record.getUserId());
            respVO.setBizId(record.getBizId());
            respVO.setBizType(record.getBizType());
            respVO.setTitle(record.getTitle());
            respVO.setDescription(record.getDescription());
            respVO.setExperience(record.getExperience());
            respVO.setTotalExperience(record.getTotalExperience());
            respVO.setCreateTime(record.getCreateTime());
            return respVO;
        }).collect(java.util.stream.Collectors.toList()), pageResult.getTotal());
        return success(result);
    }

}
