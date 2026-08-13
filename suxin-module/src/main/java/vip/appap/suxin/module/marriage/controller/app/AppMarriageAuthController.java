package vip.appap.suxin.module.marriage.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import java.util.List;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriagePreferenceUpdateReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageProfileRespVO;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageProfileUpdateReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageUserInfoRespVO;
import vip.appap.suxin.module.marriage.service.MarriageAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "App - 婚恋用户认证")
@RestController
@RequestMapping("/marriage/auth")
@Validated
public class AppMarriageAuthController {

    @Resource
    private MarriageAuthService marriageAuthService;

    @GetMapping("/get-login-user")
    @Operation(summary = "获取当前登录用户信息")
    public CommonResult<MarriageUserInfoRespVO> getLoginUser() {
        return success(marriageAuthService.getLoginUserInfo(getLoginUserId()));
    }

    @PostMapping("/update-avatar")
    @Operation(summary = "更新用户头像")
    @Parameter(name = "avatarUrl", description = "头像URL", required = true)
    public CommonResult<Boolean> updateAvatar(@RequestParam("avatarUrl") String avatarUrl) {
        marriageAuthService.updateAvatar(getLoginUserId(), avatarUrl);
        return success(true);
    }

    @GetMapping("/get-my-profile")
    @Operation(summary = "获取当前用户的完整婚恋资料")
    public CommonResult<MarriageProfileRespVO> getMyProfile() {
        return success(marriageAuthService.getMyProfile(getLoginUserId()));
    }

    @PutMapping("/update-my-profile")
    @Operation(summary = "更新我的资料")
    public CommonResult<Boolean> updateMyProfile(@Valid @RequestBody MarriageProfileUpdateReqVO updateVO) {
        marriageAuthService.updateMyProfile(getLoginUserId(), updateVO);
        return success(true);
    }

    @PutMapping("/update-my-preference")
    @Operation(summary = "更新择偶条件")
    public CommonResult<Boolean> updateMyPreference(@Valid @RequestBody MarriagePreferenceUpdateReqVO updateVO) {
        marriageAuthService.updateMyPreference(getLoginUserId(), updateVO);
        return success(true);
    }

    @PutMapping("/update-background-image")
    @Operation(summary = "更新背景图")
    public CommonResult<Boolean> updateBackgroundImage(@RequestParam("backgroundImage") String backgroundImage) {
        marriageAuthService.updateBackgroundImage(getLoginUserId(), backgroundImage);
        return success(true);
    }

    @DeleteMapping("/clear-background-image")
    @Operation(summary = "清除背景图（恢复默认）")
    public CommonResult<Boolean> clearBackgroundImage() {
        marriageAuthService.clearBackgroundImage(getLoginUserId());
        return success(true);
    }

    @PostMapping("/add-album-image")
    @Operation(summary = "添加相册图片")
    public CommonResult<Boolean> addAlbumImage(@RequestParam("imageUrl") String imageUrl,
                                               @RequestParam(value = "type", defaultValue = "1") Integer type) {
        marriageAuthService.addAlbumImage(getLoginUserId(), imageUrl, type);
        return success(true);
    }

    @DeleteMapping("/remove-album-image")
    @Operation(summary = "删除相册图片")
    public CommonResult<Boolean> removeAlbumImage(@RequestParam("imageUrl") String imageUrl,
                                                  @RequestParam(value = "type", defaultValue = "1") Integer type) {
        marriageAuthService.removeAlbumImage(getLoginUserId(), imageUrl, type);
        return success(true);
    }

    @GetMapping("/get-album-images")
    @Operation(summary = "获取相册图片列表")
    public CommonResult<List<String>> getAlbumImages(@RequestParam(value = "type", defaultValue = "1") Integer type) {
        return success(marriageAuthService.getAlbumImages(getLoginUserId(), type));
    }

    @GetMapping("/get-real-verified-status")
    @Operation(summary = "获取当前用户的实名认证状态")
    public CommonResult<Integer> getRealVerifiedStatus() {
        return success(marriageAuthService.getRealVerifiedStatus(getLoginUserId()));
    }

    @GetMapping("/get-real-name-info")
    @Operation(summary = "获取当前用户的实名认证信息（姓名+身份证号）")
    public CommonResult<MarriageUserInfoRespVO> getRealNameInfo() {
        return success(marriageAuthService.getRealNameInfo(getLoginUserId()));
    }

}
