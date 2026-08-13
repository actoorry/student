package vip.appap.suxin.module.sales.controller.admin;

import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesKeFuMessageListReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesKeFuMessageRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesKeFuMessageSendReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesKeFuMessageDO;
import vip.appap.suxin.module.sales.service.SalesKeFuMessageService;
import vip.appap.suxin.module.system.api.AdminUserApi;
import vip.appap.suxin.module.system.api.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.filterList;
import static vip.appap.suxin.framework.common.util.collection.MapUtils.findAndThen;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 客服消息")
@RestController
@RequestMapping("/sales/promotion/kefu-message")
@Validated
public class SalesKeFuMessageController {

    @Resource
    private SalesKeFuMessageService messageService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/send")
    @Operation(summary = "发送客服消息")
    @PreAuthorize("@ss.hasPermission('sales:sales_kefu_message:send')")
    public CommonResult<Long> sendKeFuMessage(@Valid @RequestBody SalesKeFuMessageSendReqVO sendReqVO) {
        sendReqVO.setSenderId(getLoginUserId()).setSenderType(UserTypeEnum.ADMIN.getValue()); // 设置用户编号和类型
        return success(messageService.sendKefuMessage(sendReqVO));
    }

    @PutMapping("/update-read-status")
    @Operation(summary = "更新客服消息已读状态")
    @Parameter(name = "conversationId", description = "会话编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_kefu_message:update')")
    public CommonResult<Boolean> updateKeFuMessageReadStatus(@RequestParam("conversationId") Long conversationId) {
        messageService.updateKeFuMessageReadStatus(conversationId, getLoginUserId(), UserTypeEnum.ADMIN.getValue());
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获得客服消息列表")
    @PreAuthorize("@ss.hasPermission('sales:sales_kefu_message:query')")
    public CommonResult<List<SalesKeFuMessageRespVO>> getKeFuMessageList(@Valid SalesKeFuMessageListReqVO pageReqVO) {
        // 获得数据
        List<SalesKeFuMessageDO> list = messageService.getKeFuMessageList(pageReqVO);

        // 拼接数据
        List<SalesKeFuMessageRespVO> result = BeanUtils.toBean(list, SalesKeFuMessageRespVO.class);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(filterList(result,
                item -> UserTypeEnum.ADMIN.getValue().equals(item.getSenderType())), SalesKeFuMessageRespVO::getSenderId));
        result.forEach(item -> findAndThen(userMap, item.getSenderId(), user -> item.setSenderAvatar(user.getAvatar())));
        return success(result);
    }

}