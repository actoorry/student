package vip.appap.suxin.module.system.convert;

import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.common.util.collection.MapUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.system.controller.admin.vo.DeptSimpleRespVO;
import vip.appap.suxin.module.system.controller.admin.vo.PostSimpleRespVO;
import vip.appap.suxin.module.system.controller.admin.vo.RoleSimpleRespVO;
import vip.appap.suxin.module.system.controller.admin.vo.UserProfileRespVO;
import vip.appap.suxin.module.system.controller.admin.vo.UserRespVO;
import vip.appap.suxin.module.system.controller.admin.vo.UserSimpleRespVO;
import vip.appap.suxin.module.system.dal.dataobject.DeptDO;
import vip.appap.suxin.module.system.dal.dataobject.PostDO;
import vip.appap.suxin.module.system.dal.dataobject.RoleDO;
import vip.appap.suxin.module.system.dal.dataobject.AdminUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    default List<UserRespVO> convertList(List<AdminUserDO> list, Map<Long, DeptDO> deptMap,
                                         Map<Long, PartnerDO> partnerMap) {
        return CollectionUtils.convertList(list, user -> convert(user, deptMap.get(user.getDeptId()),
                partnerMap.get(user.getId())));
    }

    default UserRespVO convert(AdminUserDO user, DeptDO dept, PartnerDO partner) {
        UserRespVO userVO = BeanUtils.toBean(user, UserRespVO.class);
        if (dept != null) {
            userVO.setDeptName(dept.getName());
        }
        // 填充 partner 信息（id 相同）
        if (partner != null) {
            userVO.setNickname(partner.getNickname());
            userVO.setEmail(partner.getEmail());
            userVO.setMobile(partner.getMobile());
            userVO.setSex(partner.getSex());
            userVO.setAvatar(partner.getAvatar());
        }
        return userVO;
    }

    default List<UserSimpleRespVO> convertSimpleList(List<AdminUserDO> list, Map<Long, DeptDO> deptMap,
                                                     Map<Long, PartnerDO> partnerMap) {
        return CollectionUtils.convertList(list, user -> {
            UserSimpleRespVO userVO = BeanUtils.toBean(user, UserSimpleRespVO.class);
            MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> userVO.setDeptName(dept.getName()));
            // 填充 partner 信息（id 相同）
            MapUtils.findAndThen(partnerMap, user.getId(), partner -> {
                userVO.setNickname(partner.getNickname());
            });
            return userVO;
        });
    }

    default UserProfileRespVO convert(AdminUserDO user, List<RoleDO> userRoles,
                                      DeptDO dept, List<PostDO> posts) {
        UserProfileRespVO userVO = BeanUtils.toBean(user, UserProfileRespVO.class);
        userVO.setRoles(BeanUtils.toBean(userRoles, RoleSimpleRespVO.class));
        userVO.setDept(BeanUtils.toBean(dept, DeptSimpleRespVO.class));
        userVO.setPosts(BeanUtils.toBean(posts, PostSimpleRespVO.class));
        return userVO;
    }

}
