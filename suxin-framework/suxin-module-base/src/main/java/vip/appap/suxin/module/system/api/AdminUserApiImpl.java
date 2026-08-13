package vip.appap.suxin.module.system.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.datapermission.core.annotation.DataPermission;
import vip.appap.suxin.framework.datapermission.core.util.DataPermissionUtils;
import vip.appap.suxin.module.partner.service.PartnerService;
import vip.appap.suxin.module.system.api.dto.AdminUserRespDTO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.system.dal.dataobject.DeptDO;
import vip.appap.suxin.module.system.dal.dataobject.AdminUserDO;
import vip.appap.suxin.module.system.service.DeptService;
import vip.appap.suxin.module.system.service.AdminUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * Admin 用户 API 实现类
 *
 * @author 书心软件
 */
@Service
public class AdminUserApiImpl implements AdminUserApi {

    @Resource
    private AdminUserService userService;
    @Resource
    private PartnerService partnerUserService;
    @Resource
    private DeptService deptService;

    @Override
    @DataPermission(enable = false) // 忽略数据权限，避免因为过滤，导致无法查询用户。类似：https://github.com/YunaiV/ruoyi-vue-pro/issues/1051
    public AdminUserRespDTO getUser(Long id) {
        AdminUserDO user = userService.getUser(id);
        AdminUserRespDTO dto = BeanUtils.toBean(user, AdminUserRespDTO.class);
        // 从 partner 获取个人信息（user.id = partner.id）
        PartnerDO partner = partnerUserService.getPartner(id);
        if (partner != null) {
            dto.setNickname(partner.getNickname());
            dto.setMobile(partner.getMobile());
            dto.setAvatar(partner.getAvatar());
        }
        return dto;
    }

    @Override
    public List<AdminUserRespDTO> getUserListBySubordinate(Long id) {
        // 1.1 获取用户负责的部门
        List<DeptDO> depts = deptService.getDeptListByLeaderUserId(id);
        if (CollUtil.isEmpty(depts)) {
            return Collections.emptyList();
        }
        // 1.2 获取所有子部门
        Set<Long> deptIds = convertSet(depts, DeptDO::getId);
        List<DeptDO> childDeptList = deptService.getChildDeptList(deptIds);
        if (CollUtil.isNotEmpty(childDeptList)) {
            deptIds.addAll(convertSet(childDeptList, DeptDO::getId));
        }

        // 2. 获取部门对应的用户信息
        List<AdminUserDO> users = userService.getUserListByDeptIds(deptIds);
        users.removeIf(item -> ObjUtil.equal(item.getId(), id)); // 排除自己
        return fillPartnerInfo(BeanUtils.toBean(users, AdminUserRespDTO.class));
    }

    @Override
    public List<AdminUserRespDTO> getUserList(Collection<Long> ids) {
        return DataPermissionUtils.executeIgnore(() -> { // 禁用数据权限。原因是，一般基于指定 id 的 API 查询，都是数据拼接为主
            List<AdminUserDO> users = userService.getUserList(ids);
            return fillPartnerInfo(BeanUtils.toBean(users, AdminUserRespDTO.class));
        });
    }

    @Override
    public List<AdminUserRespDTO> getUserListByDeptIds(Collection<Long> deptIds) {
        List<AdminUserDO> users = userService.getUserListByDeptIds(deptIds);
        return fillPartnerInfo(BeanUtils.toBean(users, AdminUserRespDTO.class));
    }

    @Override
    public List<AdminUserRespDTO> getUserListByPostIds(Collection<Long> postIds) {
        List<AdminUserDO> users = userService.getUserListByPostIds(postIds);
        return fillPartnerInfo(BeanUtils.toBean(users, AdminUserRespDTO.class));
    }

    /**
     * 从 partner 表填充个人信息（user.id = partner.id）
     */
    private List<AdminUserRespDTO> fillPartnerInfo(List<AdminUserRespDTO> dtos) {
        if (CollUtil.isEmpty(dtos)) {
            return dtos;
        }
        Set<Long> userIds = convertSet(dtos, AdminUserRespDTO::getId);
        java.util.Map<Long, PartnerDO> partnerMap = partnerUserService.getPartnerMap(userIds);
        dtos.forEach(dto -> {
            PartnerDO partner = partnerMap.get(dto.getId());
            if (partner != null) {
                dto.setNickname(partner.getNickname());
                dto.setMobile(partner.getMobile());
                dto.setAvatar(partner.getAvatar());
            }
        });
        return dtos;
    }

    @Override
    public void validateUserList(Collection<Long> ids) {
        userService.validateUserList(ids);
    }

}
