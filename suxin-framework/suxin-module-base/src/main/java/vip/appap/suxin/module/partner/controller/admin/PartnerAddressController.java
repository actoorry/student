package vip.appap.suxin.module.partner.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerAddressCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerAddressPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerAddressRespVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerAddressUpdateReqVO;
import vip.appap.suxin.module.partner.convert.PartnerAddressConvert;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerAddressDO;
import vip.appap.suxin.module.partner.service.PartnerAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 会员地址")
@RestController
@RequestMapping("/partner/address")
@Validated
public class PartnerAddressController {

    @Resource
    private PartnerAddressService addressService;

    @PostMapping("/create")
    @Operation(summary = "创建会员地址")
    @PreAuthorize("@ss.hasPermission('partner:address:create')")
    public CommonResult<Long> createAddress(@Valid @RequestBody PartnerAddressCreateReqVO createReqVO) {
        return success(addressService.createAddress(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新会员地址")
    @PreAuthorize("@ss.hasPermission('partner:address:update')")
    public CommonResult<Boolean> updateAddress(@Valid @RequestBody PartnerAddressUpdateReqVO updateReqVO) {
        addressService.updateAddress(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除会员地址")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('partner:address:delete')")
    public CommonResult<Boolean> deleteAddress(@RequestParam("id") Long id) {
        addressService.deleteAddress(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得会员地址")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('partner:address:query')")
    public CommonResult<PartnerAddressRespVO> getAddress(@RequestParam("id") Long id) {
        PartnerAddressDO address = addressService.getAddress(id);
        return success(PartnerAddressConvert.INSTANCE.convert(address));
    }

    @GetMapping("/page")
    @Operation(summary = "获得会员地址分页")
    @PreAuthorize("@ss.hasPermission('partner:address:query')")
    public CommonResult<PageResult<PartnerAddressRespVO>> getAddressPage(@Valid PartnerAddressPageReqVO pageVO) {
        PageResult<PartnerAddressDO> pageResult = addressService.getAddressPage(pageVO);
        return success(PartnerAddressConvert.INSTANCE.convertPage(pageResult));
    }

}
