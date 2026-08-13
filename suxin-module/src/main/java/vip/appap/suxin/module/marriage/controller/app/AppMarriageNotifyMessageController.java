package vip.appap.suxin.module.marriage.controller.app;

import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageNotifyMessagePageReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageNotifyMessageRespVO;
import vip.appap.suxin.module.system.controller.admin.vo.NotifyMessageMyPageReqVO;
import vip.appap.suxin.module.system.dal.dataobject.NotifyMessageDO;
import vip.appap.suxin.module.system.service.NotifyMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "App - 婚恋消息通知")
@RestController
@RequestMapping("/marriage/notify-message")
@Validated
public class AppMarriageNotifyMessageController {

    @Resource
    private NotifyMessageService notifyMessageService;

    @GetMapping("/page")
    @Operation(summary = "获取我的通知消息分页")
    public CommonResult<PageResult<MarriageNotifyMessageRespVO>> getMyNotifyMessagePage(
            @Valid MarriageNotifyMessagePageReqVO pageReqVO) {
        NotifyMessageMyPageReqVO reqVO = new NotifyMessageMyPageReqVO();
        reqVO.setPageNo(pageReqVO.getPageNo());
        reqVO.setPageSize(pageReqVO.getPageSize());
        reqVO.setReadStatus(pageReqVO.getReadStatus());
        PageResult<NotifyMessageDO> pageResult = notifyMessageService.getMyMyNotifyMessagePage(
                reqVO, getLoginUserId(), UserTypeEnum.MEMBER.getValue());
        return success(BeanUtils.toBean(pageResult, MarriageNotifyMessageRespVO.class));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知消息数量")
    public CommonResult<Long> getUnreadNotifyMessageCount() {
        return success(notifyMessageService.getUnreadNotifyMessageCount(
                getLoginUserId(), UserTypeEnum.MEMBER.getValue()));
    }

    @PutMapping("/read-all")
    @Operation(summary = "标记全部通知消息为已读")
    public CommonResult<Boolean> updateAllNotifyMessageRead() {
        notifyMessageService.updateAllNotifyMessageRead(getLoginUserId(), UserTypeEnum.MEMBER.getValue());
        return success(true);
    }

    @PutMapping("/read-template-code")
    @Operation(summary = "按模板编号标记通知消息为已读")
    public CommonResult<Boolean> updateNotifyMessageReadByTemplateCode(@RequestParam("templateCode") String templateCode) {
        notifyMessageService.updateNotifyMessageReadByTemplateCode(
                getLoginUserId(), UserTypeEnum.MEMBER.getValue(), templateCode);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除我的通知消息")
    public CommonResult<Boolean> deleteNotifyMessage(@RequestParam("id") Long id) {
        notifyMessageService.deleteMyNotifyMessage(id, getLoginUserId(), UserTypeEnum.MEMBER.getValue());
        return success(true);
    }

}
