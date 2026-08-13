package vip.appap.suxin.module.partner.service;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerLevelCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerLevelPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerLevelUpdateReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerLevelDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 会员等级 Service 接口
 *
 * @author 书心软件
 */
public interface PartnerLevelService {

    /**
     * 创建会员等级
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createLevel(@Valid PartnerLevelCreateReqVO createReqVO);

    /**
     * 更新会员等级
     *
     * @param updateReqVO 更新信息
     */
    void updateLevel(@Valid PartnerLevelUpdateReqVO updateReqVO);

    /**
     * 删除会员等级
     *
     * @param id 编号
     */
    void deleteLevel(Long id);

    /**
     * 获得会员等级
     *
     * @param id 编号
     * @return 会员等级
     */
    PartnerLevelDO getLevel(Long id);

    /**
     * 获得会员等级列表
     *
     * @param ids 编号
     * @return 会员等级列表
     */
    List<PartnerLevelDO> getLevelList(Collection<Long> ids);

    /**
     * 获得会员等级分页
     *
     * @param pageReqVO 分页查询
     * @return 会员等级分页
     */
    PageResult<PartnerLevelDO> getLevelPage(PartnerLevelPageReqVO pageReqVO);

    /**
     * 获得指定状态的会员等级列表
     *
     * @param status 状态
     * @return 会员等级列表
     */
    List<PartnerLevelDO> getLevelListByStatus(Integer status);

    /**
     * 获得开启状态的会员等级列表
     *
     * @return 会员等级列表
     */
    default List<PartnerLevelDO> getEnableLevelList() {
        return getLevelListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

}
