package vip.appap.suxin.module.system.controller.admin;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.datapermission.core.annotation.DataPermission;
import vip.appap.suxin.module.partner.service.PartnerService;
import vip.appap.suxin.module.system.controller.admin.vo.UserProfileRespVO;
import vip.appap.suxin.module.system.controller.admin.vo.UserProfileUpdatePasswordReqVO;
import vip.appap.suxin.module.system.controller.admin.vo.UserProfileUpdateReqVO;
import vip.appap.suxin.module.system.convert.UserConvert;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.system.dal.dataobject.DeptDO;
import vip.appap.suxin.module.system.dal.dataobject.PostDO;
import vip.appap.suxin.module.system.dal.dataobject.RoleDO;
import vip.appap.suxin.module.system.dal.dataobject.AdminUserDO;
import vip.appap.suxin.module.system.service.DeptService;
import vip.appap.suxin.module.system.service.PostService;
import vip.appap.suxin.module.system.service.PermissionService;
import vip.appap.suxin.module.system.service.RoleService;
import vip.appap.suxin.module.system.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 用户个人中心")
@RestController
@RequestMapping("/system/user/profile")
@Validated
@Slf4j
public class UserProfileController {

    @Resource
    private AdminUserService userService;
    @Resource
    private PartnerService partnerUserService;
    @Resource
    private DeptService deptService;
    @Resource
    private PostService postService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private RoleService roleService;

    @GetMapping("/get")
    @Operation(summary = "获得登录用户信息")
    @DataPermission(enable = false) // 关闭数据权限，避免只查看自己时，查询不到部门。
    public CommonResult<UserProfileRespVO> getUserProfile() {
        // 获得用户基本信息
        AdminUserDO user = userService.getUser(getLoginUserId());
        // 获得用户角色
        List<RoleDO> userRoles = roleService.getRoleListFromCache(permissionService.getUserRoleIdListByUserId(user.getId()));
        // 获得部门信息
        DeptDO dept = user.getDeptId() != null ? deptService.getDept(user.getDeptId()) : null;
        // 获得岗位信息
        List<PostDO> posts = CollUtil.isNotEmpty(user.getPostIds()) ? postService.getPostList(user.getPostIds()) : null;
        UserProfileRespVO respVO = UserConvert.INSTANCE.convert(user, userRoles, dept, posts);
        // 从 partner 获取个人信息（user.id = partner.id）
        PartnerDO partner = partnerUserService.getPartner(user.getId());
        if (partner != null) {
            respVO.setNickname(partner.getNickname());
            respVO.setEmail(partner.getEmail());
            respVO.setMobile(partner.getMobile());
            respVO.setSex(partner.getSex());
            respVO.setAvatar(partner.getAvatar());
        }
        return success(respVO);
    }

    @PutMapping("/update")
    @Operation(summary = "修改用户个人信息")
    public CommonResult<Boolean> updateUserProfile(@Valid @RequestBody UserProfileUpdateReqVO reqVO) {
        userService.updateUserProfile(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/update-password")
    @Operation(summary = "修改用户个人密码")
    public CommonResult<Boolean> updateUserProfilePassword(@Valid @RequestBody UserProfileUpdatePasswordReqVO reqVO) {
        userService.updateUserPassword(getLoginUserId(), reqVO);
        return success(true);
    }

}
