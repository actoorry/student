package vip.appap.suxin.module.partner.service;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerGroupCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerGroupPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerGroupUpdateReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerGroupDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 用户分组 Service 接口
 *
 * @author 书心软件
 */
public interface PartnerGroupService {

    /**
     * 创建用户分组
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createGroup(@Valid PartnerGroupCreateReqVO createReqVO);

    /**
     * 更新用户分组
     *
     * @param updateReqVO 更新信息
     */
    void updateGroup(@Valid PartnerGroupUpdateReqVO updateReqVO);

    /**
     * 删除用户分组
     *
     * @param id 编号
     */
    void deleteGroup(Long id);

    /**
     * 获得用户分组
     *
     * @param id 编号
     * @return 用户分组
     */
    PartnerGroupDO getGroup(Long id);

    /**
     * 获得用户分组列表
     *
     * @param ids 编号
     * @return 用户分组列表
     */
    List<PartnerGroupDO> getGroupList(Collection<Long> ids);

    /**
     * 获得用户分组分页
     *
     * @param pageReqVO 分页查询
     * @return 用户分组分页
     */
    PageResult<PartnerGroupDO> getGroupPage(PartnerGroupPageReqVO pageReqVO);

    /**
     * 获得指定状态的用户分组列表
     *
     * @param status 状态
     * @return 用户分组列表
     */
    List<PartnerGroupDO> getGroupListByStatus(Integer status);

    /**
     * 获得开启状态的用户分组列表
     *
     * @return 用户分组列表
     */
    default List<PartnerGroupDO> getEnableGroupList() {
        return getGroupListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

}
