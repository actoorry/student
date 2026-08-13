package vip.appap.suxin.module.partner.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerTagCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerTagPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerTagUpdateReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerTagDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 会员标签 Service 接口
 *
 * @author 书心软件
 */
public interface PartnerTagService {

    /**
     * 创建会员标签
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTag(@Valid PartnerTagCreateReqVO createReqVO);

    /**
     * 更新会员标签
     *
     * @param updateReqVO 更新信息
     */
    void updateTag(@Valid PartnerTagUpdateReqVO updateReqVO);

    /**
     * 删除会员标签
     *
     * @param id 编号
     */
    void deleteTag(Long id);

    /**
     * 获得会员标签
     *
     * @param id 编号
     * @return 会员标签
     */
    PartnerTagDO getTag(Long id);

    /**
     * 获得会员标签列表
     *
     * @param ids 编号
     * @return 会员标签列表
     */
    List<PartnerTagDO> getTagList(Collection<Long> ids);

    /**
     * 获得会员标签分页
     *
     * @param pageReqVO 分页查询
     * @return 会员标签分页
     */
    PageResult<PartnerTagDO> getTagPage(PartnerTagPageReqVO pageReqVO);

    /**
     * 获取标签列表
     *
     * @return 标签列表
     */
    List<PartnerTagDO> getTagList();

}
