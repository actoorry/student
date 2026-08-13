package vip.appap.suxin.module.hr.controller.admin.my;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.hr.controller.admin.my.HrMyContractController;
import vip.appap.suxin.module.hr.controller.admin.resume.vo.ResumePageReqVO;
import vip.appap.suxin.module.hr.controller.admin.resume.vo.ResumeRespVO;
import vip.appap.suxin.module.hr.controller.admin.resume.vo.ResumeSaveReqVO;
import vip.appap.suxin.module.hr.framework.my.HrMyScopeSupport;
import vip.appap.suxin.module.hr.service.resume.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

/**
 * 职工「我的履历」Controller
 *
 * 路径 /hr/my/resume，权限 hr:my:resume:*。仅可操作本人数据。
 *
 * @author suxin
 * @see HrMyContractController 同模式
 */
@Tag(name = "管理后台 - 职工我的履历")
@RestController
@RequestMapping("/hr/my/resume")
@Validated
public class HrMyResumeController {

    @Resource
    private HrMyScopeSupport myScope;
    @Resource
    private ResumeService resumeService;

    @GetMapping("/page")
    @Operation(summary = "获得我的履历分页")
    @PreAuthorize("@ss.hasPermission('hr:my:resume:query')")
    public CommonResult<PageResult<ResumeRespVO>> getMyResumePage(@Valid ResumePageReqVO pageReqVO) {
        Long partnerId = myScope.requireBoundPartnerId();
        pageReqVO.setPartnerId(partnerId);
        pageReqVO.setName(null);
        return success(resumeService.getResumePage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得我的履历详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hr:my:resume:query')")
    public CommonResult<ResumeRespVO> getMyResume(@RequestParam("id") Long id) {
        ResumeRespVO resume = resumeService.getResume(id);
        myScope.validateBelongsToMe(resume.getPartnerId());
        return success(resume);
    }

    @PostMapping("/create")
    @Operation(summary = "创建我的履历")
    @PreAuthorize("@ss.hasPermission('hr:my:resume:create')")
    public CommonResult<Long> createMyResume(@Valid @RequestBody ResumeSaveReqVO createReqVO) {
        createReqVO.setPartnerId(myScope.requireBoundPartnerId());
        return success(resumeService.createResume(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新我的履历")
    @PreAuthorize("@ss.hasPermission('hr:my:resume:update')")
    public CommonResult<Boolean> updateMyResume(@Valid @RequestBody ResumeSaveReqVO updateReqVO) {
        ResumeRespVO existing = resumeService.getResume(updateReqVO.getId());
        myScope.validateBelongsToMe(existing.getPartnerId());
        updateReqVO.setPartnerId(myScope.requireBoundPartnerId());
        resumeService.updateResume(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除我的履历")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hr:my:resume:delete')")
    public CommonResult<Boolean> deleteMyResume(@RequestParam("id") Long id) {
        ResumeRespVO existing = resumeService.getResume(id);
        myScope.validateBelongsToMe(existing.getPartnerId());
        resumeService.deleteResume(id);
        return success(true);
    }

}
