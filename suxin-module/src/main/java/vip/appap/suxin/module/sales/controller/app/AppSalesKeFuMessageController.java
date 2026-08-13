package vip.appap.suxin.module.sales.controller.app;

import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesKeFuMessageRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesKeFuMessagePageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesKeFuMessageSendReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesKeFuMessageDO;
import vip.appap.suxin.module.sales.service.SalesKeFuMessageService;
import vip.appap.suxin.module.system.api.AdminUserApi;
import vip.appap.suxin.module.system.api.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.filterList;
import static vip.appap.suxin.framework.common.util.collection.MapUtils.findAndThen;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 客服消息")
@RestController
@RequestMapping("/sales/promotion/kefu-message")
@Validated
public class AppSalesKeFuMessageController {

    @Resource
    private SalesKeFuMessageService kefuMessageService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/send")
    @Operation(summary = "发送客服消息")
    public CommonResult<Long> sendKefuMessage(@Valid @RequestBody AppSalesKeFuMessageSendReqVO sendReqVO) {
        sendReqVO.setSenderId(getLoginUserId()).setSenderType(UserTypeEnum.MEMBER.getValue()); // 设置用户编号和类型
        return success(kefuMessageService.sendKefuMessage(sendReqVO));
    }

    @PutMapping("/update-read-status")
    @Operation(summary = "更新客服消息已读状态")
    @Parameter(name = "conversationId", description = "会话编号", required = true)
    public CommonResult<Boolean> updateKefuMessageReadStatus(@RequestParam("conversationId") Long conversationId) {
        kefuMessageService.updateKeFuMessageReadStatus(conversationId, getLoginUserId(), UserTypeEnum.MEMBER.getValue());
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获得客服消息列表")
    public CommonResult<List<SalesKeFuMessageRespVO>> getKefuMessageList(@Valid AppSalesKeFuMessagePageReqVO pageReqVO) {
        List<SalesKeFuMessageDO> list = kefuMessageService.getKeFuMessageList(pageReqVO, getLoginUserId());

        // 拼接数据
        List<SalesKeFuMessageRespVO> result = BeanUtils.toBean(list, SalesKeFuMessageRespVO.class);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(filterList(result,
                item -> UserTypeEnum.ADMIN.getValue().equals(item.getSenderType())), SalesKeFuMessageRespVO::getSenderId));
        result.forEach(item -> findAndThen(userMap, item.getSenderId(), user -> item.setSenderAvatar(user.getAvatar())));
        return success(result);
    }

}