package vip.appap.suxin.module.partner.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerPointRecordPageReqVO;
import vip.appap.suxin.module.partner.controller.app.vo.AppPartnerPointPageRespVO;
import vip.appap.suxin.module.partner.controller.app.vo.AppPartnerPointRecordRespVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerPointRecordDO;
import vip.appap.suxin.module.partner.service.PartnerPointRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.appap.suxin.module.partner.service.PartnerService;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.PARTNER_NOT_EXISTS;

@Tag(name = "用户 App - 用户积分记录")
@RestController
@RequestMapping("/partner/point/record")
@Validated
public class AppPartnerPointRecordController {

    @Resource
    private PartnerPointRecordService pointRecordService;
    @Resource
    private PartnerService partnerService;

    @GetMapping("/page")
    @Operation(summary = "获得积分余额和积分记录分页")
    public CommonResult<AppPartnerPointPageRespVO> getPointRecordPage(@Valid PartnerPointRecordPageReqVO pageReqVO) {
        Long loginUserId = getLoginUserId();
        pageReqVO.setUserId(loginUserId);
        PartnerDO partner = partnerService.getPartner(loginUserId);
        if (partner == null) {
            throw exception(PARTNER_NOT_EXISTS);
        }
        PageResult<PartnerPointRecordDO> pageResult = pointRecordService.getPointRecordPage(pageReqVO);

        AppPartnerPointPageRespVO respVO = new AppPartnerPointPageRespVO();
        respVO.setTotalPoint(partner.getPoint() == null ? 0 : partner.getPoint());
        respVO.setRecords(BeanUtils.toBean(pageResult, AppPartnerPointRecordRespVO.class));
        return success(respVO);
    }

}
