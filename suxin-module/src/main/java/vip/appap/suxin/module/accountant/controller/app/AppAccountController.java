package vip.appap.suxin.module.accountant.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.accountant.controller.app.vo.AppAccountRespVO;
import vip.appap.suxin.module.accountant.convert.AccountConvert;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountDO;
import vip.appap.suxin.module.accountant.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * @author jason
 */
@Tag(name = "用户 APP - 钱包")
@RestController
@RequestMapping("/accountant/account")
@Validated
@Slf4j
public class AppAccountController {

    @Resource
    private AccountService AccountService;

    @GetMapping("/get")
    @Operation(summary = "获取钱包")
    public CommonResult<AppAccountRespVO> getAccount() {
        AccountDO account = AccountService.getOrCreateAccount(getLoginUserId());
        return success(AccountConvert.INSTANCE.convert(account));
    }

}

