package vip.appap.suxin.module.im.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.im.controller.admin.vo.ImFriendUpdateReqVO;
import vip.appap.suxin.module.im.controller.admin.vo.ImFriendRequestApplyReqVO;
import vip.appap.suxin.module.im.controller.app.vo.AppImFriendRespVO;
import vip.appap.suxin.module.im.dal.dataobject.ImFriendDO;
import vip.appap.suxin.module.im.dal.dataobject.ImFriendRequestDO;
import vip.appap.suxin.module.im.service.ImFriendRequestService;
import vip.appap.suxin.module.im.service.ImFriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * App 端 - IM 好友 Controller
 */
@Tag(name = "App 端 - IM 好友")
@RestController
@RequestMapping("/app-api/im/friend")
@Validated
public class AppImFriendController {

    @Resource
    private ImFriendService friendService;

    @Resource
    private ImFriendRequestService friendRequestService;

    @GetMapping("/list")
    @Operation(summary = "获取好友列表")
    public CommonResult<List<AppImFriendRespVO>> getFriendList() {
        List<ImFriendDO> friends = friendService.getEnableFriendList(getLoginUserId());
        return success(BeanUtils.toBean(friends, AppImFriendRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获取好友详情")
    @Parameter(name = "friendUserId", description = "好友用户编号", required = true, example = "2")
    public CommonResult<AppImFriendRespVO> getFriend(@RequestParam("friendUserId") Long friendUserId) {
        ImFriendDO friend = friendService.getFriend(getLoginUserId(), friendUserId);
        return success(BeanUtils.toBean(friend, AppImFriendRespVO.class));
    }

    @PutMapping("/update")
    @Operation(summary = "更新好友备注等信息")
    public CommonResult<Boolean> updateFriend(@Valid @RequestBody ImFriendUpdateReqVO reqVO) {
        friendService.updateFriend(getLoginUserId(), reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除好友")
    @Parameter(name = "friendUserId", description = "好友用户编号", required = true, example = "2")
    @Parameter(name = "clear", description = "是否级联清理本端相关数据", example = "true")
    public CommonResult<Boolean> deleteFriend(
            @RequestParam("friendUserId") Long friendUserId,
            @RequestParam(value = "clear", defaultValue = "false") Boolean clear) {
        friendService.deleteFriend(getLoginUserId(), friendUserId, clear);
        return success(true);
    }

    @PutMapping("/block")
    @Operation(summary = "拉黑好友")
    @Parameter(name = "friendUserId", description = "好友用户编号", required = true, example = "2")
    public CommonResult<Boolean> blockFriend(@RequestParam("friendUserId") Long friendUserId) {
        friendService.blockFriend(getLoginUserId(), friendUserId);
        return success(true);
    }

    @PutMapping("/unblock")
    @Operation(summary = "取消拉黑")
    @Parameter(name = "friendUserId", description = "好友用户编号", required = true, example = "2")
    public CommonResult<Boolean> unblockFriend(@RequestParam("friendUserId") Long friendUserId) {
        friendService.unblockFriend(getLoginUserId(), friendUserId);
        return success(true);
    }

    // ==================== 好友申请相关 ====================

    @PostMapping("/apply")
    @Operation(summary = "发起好友申请")
    public CommonResult<Long> applyFriend(@Valid @RequestBody ImFriendRequestApplyReqVO reqVO) {
        ImFriendRequestDO request = friendRequestService.applyFriend(getLoginUserId(), reqVO);
        return success(request.getId());
    }

    @PutMapping("/request/agree")
    @Operation(summary = "同意好友申请")
    @Parameter(name = "requestId", description = "申请记录编号", required = true, example = "1")
    public CommonResult<Boolean> agreeFriendRequest(@RequestParam("requestId") Long requestId) {
        friendRequestService.agreeFriendRequest(getLoginUserId(), requestId);
        return success(true);
    }

    @PutMapping("/request/refuse")
    @Operation(summary = "拒绝好友申请")
    @Parameter(name = "requestId", description = "申请记录编号", required = true, example = "1")
    @Parameter(name = "handleContent", description = "拒绝理由", example = "暂不添加")
    public CommonResult<Boolean> refuseFriendRequest(
            @RequestParam("requestId") Long requestId,
            @RequestParam(value = "handleContent", required = false) String handleContent) {
        friendRequestService.refuseFriendRequest(getLoginUserId(), requestId, handleContent);
        return success(true);
    }

    @GetMapping("/request/list")
    @Operation(summary = "获取好友申请列表")
    @Parameter(name = "lastRequestId", description = "最后一条申请编号（用于分页）", example = "0")
    @Parameter(name = "limit", description = "拉取数量", example = "20")
    public CommonResult<List<ImFriendRequestDO>> getFriendRequestList(
            @RequestParam(value = "lastRequestId", required = false) Long lastRequestId,
            @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        return success(friendRequestService.getMyFriendRequestList(getLoginUserId(), lastRequestId, limit));
    }

}
