package vip.appap.suxin.module.im.controller.admin;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.collection.MapUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.im.controller.admin.vo.ImGroupMemberRespVO;
import vip.appap.suxin.module.im.controller.admin.vo.ImGroupMemberUpdateReqVO;
import vip.appap.suxin.module.im.dal.dataobject.ImGroupMemberDO;
import vip.appap.suxin.module.im.service.ImGroupMemberService;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
import static vip.appap.suxin.module.im.enums.ErrorCodeConstants.GROUP_MEMBER_NOT_IN_GROUP;

@Tag(name = "管理后台 - 群成员")
@RestController
@RequestMapping("/im/group-member")
@Validated
public class ImGroupMemberController {

    @Resource
    private ImGroupMemberService groupMemberService;

    @Resource
    private PartnerApi partnerApi;

    @PutMapping("/update")
    @Operation(summary = "更新群成员")
    public CommonResult<Boolean> updateGroupMember(@Valid @RequestBody ImGroupMemberUpdateReqVO updateReqVO) {
        groupMemberService.updateGroupMember(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得群成员")
    @Parameters({
            @Parameter(name = "id", description = "编号（与 groupId + userId 二选一）", example = "1024"),
            @Parameter(name = "groupId", description = "群编号（与 userId 配合查）", example = "1"),
            @Parameter(name = "userId", description = "用户编号（与 groupId 配合查）", example = "100")
    })
    public CommonResult<ImGroupMemberRespVO> getGroupMember(@RequestParam(value = "id", required = false) Long id,
                                                            @RequestParam(value = "groupId", required = false) Long groupId,
                                                            @RequestParam(value = "userId", required = false) Long userId) {
        ImGroupMemberDO member;
        if (id != null) {
            member = groupMemberService.getGroupMember(id);
        } else if (groupId != null && userId != null) {
            member = groupMemberService.getGroupMember(groupId, userId);
        } else {
            // 避免 selectByGroupIdAndUserId 收到 null 参数走全表扫 / 抛 SQL 异常
            throw new IllegalArgumentException("参数缺失：需传 id 或 (groupId, userId)");
        }
        return success(BeanUtils.toBean(member, ImGroupMemberRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得指定群的成员列表")
    @Parameter(name = "groupId", description = "群编号", required = true, example = "1024")
    public CommonResult<List<ImGroupMemberRespVO>> getGroupMemberList(@RequestParam("groupId") Long groupId) {
        // 1.1 查询群成员列表（包含 DISABLE 已退群的成员，不按时间过滤）
        // 说明：保留已退群成员，是为了前端展示历史消息时，仍能通过该接口拿到已退群成员的昵称 / 头像信息，避免显示为空
        List<ImGroupMemberDO> members = groupMemberService.getGroupMemberListByGroupId(groupId);
        // 1.2 校验当前登录用户是否为群的有效成员，非成员不可查看
        Long loginUserId = getLoginUserId();
        if (CollUtil.findOne(members, member -> loginUserId.equals(member.getUserId())
                && CommonStatusEnum.ENABLE.getStatus().equals(member.getStatus())) == null) {
            throw exception(GROUP_MEMBER_NOT_IN_GROUP);
        }

        // 2.批量聚合 AdminUser 信息（昵称 / 头像）
        Map<Long, PartnerRespDTO> userMap = partnerApi.getUserMap(
                convertList(members, ImGroupMemberDO::getUserId));
        return success(convertList(members, m -> {
            ImGroupMemberRespVO vo = BeanUtils.toBean(m, ImGroupMemberRespVO.class);
            MapUtils.findAndThen(userMap, m.getUserId(), user ->
                    vo.setNickname(user.getNickname()).setAvatar(user.getAvatar()));
            return vo;
        }));
    }

}
