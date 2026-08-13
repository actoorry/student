package vip.appap.suxin.module.partner.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerAddressCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerAddressRespVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerAddressUpdateReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerAddressDO;
import vip.appap.suxin.module.partner.service.PartnerAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 用户收件地址")
@RestController
@RequestMapping("/partner/address")
@Validated
public class AppPartnerAddressController {

    @Resource
    private PartnerAddressService addressService;

    @PostMapping("/create")
    @Operation(summary = "创建收件地址")
    public CommonResult<Long> createAddress(@RequestBody PartnerAddressCreateReqVO createReqVO) {
        createReqVO.setUserId(getLoginUserId());
        createReqVO.setDefaulted(false);
        createReqVO.setType(vip.appap.suxin.module.partner.enums.PartnerAddressTypeEnum.MEMBER.getType()); // App 仅能创建会员收件地址
        return success(addressService.createAddress(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改收件地址")
    public CommonResult<Boolean> updateAddress(@RequestBody @Valid PartnerAddressUpdateReqVO updateReqVO) {
        addressService.updateAddress(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除收件地址")
    public CommonResult<Boolean> deleteAddress(@RequestParam("id") Long id) {
        addressService.deleteAddress(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得收件地址")
    public CommonResult<PartnerAddressRespVO> getAddress(@RequestParam("id") Long id) {
        PartnerAddressDO address = addressService.getAddress(id);
        return success(BeanUtils.toBean(address, PartnerAddressRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得收件地址列表")
    public CommonResult<List<PartnerAddressRespVO>> getAddressList() {
        List<PartnerAddressDO> list = addressService.getAddressListByUserId(getLoginUserId());
        return success(BeanUtils.toBean(list, PartnerAddressRespVO.class));
    }

}
