package vip.appap.suxin.module.marriage.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.marriage.controller.app.vo.AppInteractionNotificationPageReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppInteractionNotificationRespVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppInteractionNotificationUnreadCountRespVO;
import vip.appap.suxin.module.marriage.dal.dataobject.AppInteractionNotificationDO;
import vip.appap.suxin.module.marriage.enums.AppInteractionNotificationSceneEnum;
import vip.appap.suxin.module.marriage.service.AppInteractionNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "App - 婚恋互动通知")
@RestController
@RequestMapping("/marriage/interaction-notification")
@Validated
public class AppMarriageInteractionNotificationController {

    @Resource
    private AppInteractionNotificationService appInteractionNotificationService;

    @GetMapping("/page")
    @Operation(summary = "获取婚恋互动通知分页")
    public CommonResult<PageResult<AppInteractionNotificationRespVO>> getNotificationPage(
            @Valid AppInteractionNotificationPageReqVO pageReqVO) {
        PageResult<AppInteractionNotificationDO> pageResult = appInteractionNotificationService.getNotificationPage(
                AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, getLoginUserId(), pageReqVO);
        return success(BeanUtils.toBean(pageResult, AppInteractionNotificationRespVO.class));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取婚恋互动通知未读数")
    public CommonResult<Long> getUnreadCount() {
        return success(appInteractionNotificationService.getUnreadCount(
                AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, getLoginUserId()));
    }

    @GetMapping("/unread-count-group")
    @Operation(summary = "获取婚恋互动通知按场景未读数")
    public CommonResult<List<AppInteractionNotificationUnreadCountRespVO>> getUnreadCountGroupByScene() {
        return success(appInteractionNotificationService.getUnreadCountGroupByScene(
                AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, getLoginUserId()));
    }

    @PutMapping("/read-scene")
    @Operation(summary = "按场景标记婚恋互动通知已读")
    @Parameter(name = "scene", description = "场景", required = true, example = "FOLLOW")
    public CommonResult<Boolean> readByScene(@RequestParam("scene") String scene) {
        appInteractionNotificationService.readByScene(
                AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, getLoginUserId(), scene);
        return success(true);
    }

    @PutMapping("/read-all")
    @Operation(summary = "标记全部婚恋互动通知已读")
    public CommonResult<Boolean> readAll() {
        appInteractionNotificationService.readAll(
                AppInteractionNotificationSceneEnum.BIZ_TYPE_MARRIAGE, getLoginUserId());
        return success(true);
    }

}
