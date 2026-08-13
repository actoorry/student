package vip.appap.suxin.module.partner.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerPointRecordPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerPointRecordRespVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerPointRecordDO;
import vip.appap.suxin.module.partner.service.PartnerPointRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 会员积分记录")
@RestController
@RequestMapping("/partner/point-record")
@Validated
public class PartnerPointRecordController {

    @Resource
    private PartnerPointRecordService pointRecordService;

    @GetMapping("/page")
    @Operation(summary = "获得会员积分记录分页")
    @PreAuthorize("@ss.hasPermission('partner:point-record:query')")
    public CommonResult<PageResult<PartnerPointRecordRespVO>> getPointRecordPage(@Valid PartnerPointRecordPageReqVO pageVO) {
        PageResult<PartnerPointRecordDO> pageResult = pointRecordService.getPointRecordPage(pageVO);
        // 简单转换，后续可以使用 MapStruct
        PageResult<PartnerPointRecordRespVO> result = new PageResult<>(pageResult.getList().stream().map(record -> {
            PartnerPointRecordRespVO respVO = new PartnerPointRecordRespVO();
            respVO.setId(record.getId());
            respVO.setUserId(record.getUserId());
            respVO.setBizId(record.getBizId());
            respVO.setBizType(record.getBizType());
            respVO.setTitle(record.getTitle());
            respVO.setDescription(record.getDescription());
            respVO.setPoint(record.getPoint());
            respVO.setTotalPoint(record.getTotalPoint());
            respVO.setCreateTime(record.getCreateTime());
            return respVO;
        }).collect(java.util.stream.Collectors.toList()), pageResult.getTotal());
        return success(result);
    }

}
