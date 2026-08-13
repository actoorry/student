package vip.appap.suxin.module.system.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.system.controller.admin.vo.OAuth2AccessTokenPageReqVO;
import vip.appap.suxin.module.system.controller.admin.vo.OAuth2AccessTokenRespVO;
import vip.appap.suxin.module.system.dal.dataobject.OAuth2AccessTokenDO;
import vip.appap.suxin.module.system.enums.LoginLogTypeEnum;
import vip.appap.suxin.module.system.service.AdminAuthService;
import vip.appap.suxin.module.system.service.OAuth2TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - OAuth2.0 令牌")
@RestController
@RequestMapping("/system/oauth2-token")
public class OAuth2TokenController {

    @Resource
    private OAuth2TokenService oauth2TokenService;
    @Resource
    private AdminAuthService authService;

    @GetMapping("/page")
    @Operation(summary = "获得访问令牌分页", description = "只返回有效期内的")
    @PreAuthorize("@ss.hasPermission('system:oauth2-token:page')")
    public CommonResult<PageResult<OAuth2AccessTokenRespVO>> getAccessTokenPage(@Valid OAuth2AccessTokenPageReqVO reqVO) {
        PageResult<OAuth2AccessTokenDO> pageResult = oauth2TokenService.getAccessTokenPage(reqVO);
        return success(BeanUtils.toBean(pageResult, OAuth2AccessTokenRespVO.class));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除访问令牌")
    @Parameter(name = "accessToken", description = "访问令牌", required = true, example = "tudou")
    @PreAuthorize("@ss.hasPermission('system:oauth2-token:delete')")
    public CommonResult<Boolean> deleteAccessToken(@RequestParam("accessToken") String accessToken) {
        authService.logout(accessToken, LoginLogTypeEnum.LOGOUT_DELETE.getType());
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除访问令牌")
    @Parameter(name = "accessTokens", description = "访问令牌数组", required = true)
    @PreAuthorize("@ss.hasPermission('system:oauth2-token:delete')")
    public CommonResult<Boolean> deleteAccessTokenList(@RequestParam("accessTokens") List<String> accessTokens) {
        accessTokens.forEach(accessToken ->
                authService.logout(accessToken, LoginLogTypeEnum.LOGOUT_DELETE.getType()));
        return success(true);
    }

}
