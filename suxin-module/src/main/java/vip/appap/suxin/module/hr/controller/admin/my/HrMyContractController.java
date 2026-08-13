package vip.appap.suxin.module.hr.controller.admin.my;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.hr.controller.admin.contract.vo.HrContractPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.contract.vo.HrContractRespVO;
import vip.appap.suxin.module.hr.controller.admin.contract.vo.HrContractSaveReqVO;
import vip.appap.suxin.module.hr.framework.my.HrMyScopeSupport;
import vip.appap.suxin.module.hr.service.contract.HrContractService;
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
 * 职工「我的合同」Controller
 *
 * 路径 /hr/my/contract，权限 hr:my:contract:*。
 * 仅可操作本人数据：partnerId 由后端强制为 getLoginUserId()，忽略前端入参。
 *
 * @author suxin
 */
@Tag(name = "管理后台 - 职工我的合同")
@RestController
@RequestMapping("/hr/my/contract")
@Validated
public class HrMyContractController {

    @Resource
    private HrMyScopeSupport myScope;
    @Resource
    private HrContractService contractService;

    @GetMapping("/page")
    @Operation(summary = "获得我的合同分页")
    @PreAuthorize("@ss.hasPermission('hr:my:contract:query')")
    public CommonResult<PageResult<HrContractRespVO>> getMyContractPage(@Valid HrContractPageReqVO pageReqVO) {
        Long partnerId = myScope.requireBoundPartnerId();
        pageReqVO.setPartnerId(partnerId);
        pageReqVO.setName(null); // 越权按姓名搜索一律忽略
        return success(contractService.getContractPage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得我的合同详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hr:my:contract:query')")
    public CommonResult<HrContractRespVO> getMyContract(@RequestParam("id") Long id) {
        HrContractRespVO contract = contractService.getContract(id);
        myScope.validateBelongsToMe(contract.getPartnerId());
        return success(contract);
    }

    @PostMapping("/create")
    @Operation(summary = "创建我的合同")
    @PreAuthorize("@ss.hasPermission('hr:my:contract:create')")
    public CommonResult<Long> createMyContract(@Valid @RequestBody HrContractSaveReqVO createReqVO) {
        createReqVO.setPartnerId(myScope.requireBoundPartnerId());
        return success(contractService.createContract(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新我的合同")
    @PreAuthorize("@ss.hasPermission('hr:my:contract:update')")
    public CommonResult<Boolean> updateMyContract(@Valid @RequestBody HrContractSaveReqVO updateReqVO) {
        HrContractRespVO existing = contractService.getContract(updateReqVO.getId());
        myScope.validateBelongsToMe(existing.getPartnerId());
        updateReqVO.setPartnerId(myScope.requireBoundPartnerId());
        contractService.updateContract(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除我的合同")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hr:my:contract:delete')")
    public CommonResult<Boolean> deleteMyContract(@RequestParam("id") Long id) {
        HrContractRespVO existing = contractService.getContract(id);
        myScope.validateBelongsToMe(existing.getPartnerId());
        contractService.deleteContract(id);
        return success(true);
    }

}
