package vip.appap.suxin.module.im.controller.admin;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.MapUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.im.controller.admin.vo.ImGroupRequestManagerPageReqVO;
import vip.appap.suxin.module.im.controller.admin.vo.ImGroupRequestManagerRespVO;
import vip.appap.suxin.module.im.dal.dataobject.ImGroupDO;
import vip.appap.suxin.module.im.dal.dataobject.ImGroupRequestDO;
import vip.appap.suxin.module.im.service.ImGroupRequestService;
import vip.appap.suxin.module.im.service.ImGroupService;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;

@Tag(name = "管理后台 - IM 加群申请管理")
@RestController
@RequestMapping("/im/manager/group-request")
@Validated
public class ImGroupRequestManagerController {

    @Resource
    private ImGroupRequestService groupRequestService;
    @Resource
    private ImGroupService groupService;
    @Resource
    private PartnerApi partnerApi;

    @GetMapping("/page")
    @Operation(summary = "获得加群申请分页")
    @PreAuthorize("@ss.hasPermission('im:manager:group-request:query')")
    public CommonResult<PageResult<ImGroupRequestManagerRespVO>> getGroupRequestPage(
            @Valid ImGroupRequestManagerPageReqVO pageReqVO) {
        // 1. 分页查询
        PageResult<ImGroupRequestDO> pageResult = groupRequestService.getGroupRequestPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 2.1 批量聚合 user / inviter / handler 用户昵称
        Set<Long> userIds = convertSetByFlatMap(pageResult.getList(),
                request -> Stream.of(request.getUserId(), request.getInviterUserId(), request.getHandleUserId())
                        .filter(Objects::nonNull));
        Map<Long, PartnerRespDTO> userMap = partnerApi.getUserMap(userIds);
        // 2.2 批量聚合群信息（取群名）
        Set<Long> groupIds = convertSet(pageResult.getList(), ImGroupRequestDO::getGroupId);
        Map<Long, ImGroupDO> groupMap = groupService.getGroupMap(groupIds);
        return success(BeanUtils.toBean(pageResult, ImGroupRequestManagerRespVO.class, vo -> {
            MapUtils.findAndThen(userMap, vo.getUserId(), user -> vo.setUserNickname(user.getNickname()));
            MapUtils.findAndThen(userMap, vo.getInviterUserId(), user -> vo.setInviterNickname(user.getNickname()));
            MapUtils.findAndThen(userMap, vo.getHandleUserId(), user -> vo.setHandleNickname(user.getNickname()));
            MapUtils.findAndThen(groupMap, vo.getGroupId(), group -> vo.setGroupName(group.getName()));
        }));
    }

}
